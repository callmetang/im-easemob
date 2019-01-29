package com.im.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hyphenate.chat.Message;
import com.im.test.adapters.ChatMultiItemTypeAdapter;
import com.im.test.beans.MessageEvent;
import com.im.test.utils.ImUtils;
import com.im.test.utils.ImageUtils;
import com.im.test.utils.TimeUtils;
import com.im.test.views.InputMessageView;
import com.im.test.views.MenuOpenStatusCallback;
import com.im.test.views.SendCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author a
 */
public class MainActivity extends AppCompatActivity {

    private InputMessageView mInputMessageView;
    private RecyclerView mRecyclerView;

    private SwipeRefreshLayout mSwipeRefresh;


    private ChatMultiItemTypeAdapter chatMultiItemTypeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("当前登录用户: " + ImUtils.SELF_ID);

        mInputMessageView = (InputMessageView) findViewById(R.id.input_message_view);
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        ImUtils.isLoggedInBefore(this);

        mInputMessageView.setSendCallback(new SendCallback() {
            @Override
            public void send(EditText editText) {
                String text = editText.getText().toString().trim();
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(MainActivity.this, "消息为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                ImUtils.createTxtSendMessage(MainActivity.this, text, ImUtils.KEFU_ID);

                editText.setText("");
            }
        });


        initRecyclerView();

        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (chatMessages != null && chatMessages.size() > 0) {
                    String startMsgId = chatMessages.get(0).messageId();
                    Log.e("MainActivity", "startMsgId: " + startMsgId);
                    ImUtils.loadMessagesByStartMsgId(startMsgId);
                }
            }
        });


        mInputMessageView.setPictureListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_SELECT_PICK);

            }
        });

    }
    private static final int REQUEST_CODE_SELECT_PICK = 222;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_PICK && data != null) {
            Uri imageUri = data.getData();
            File file = ImageUtils.getFileByUri(imageUri, MainActivity.this);
            if (file != null) {

                String path = file.getAbsolutePath();
                Log.e("TAG", path);
                if (imageUri != null) {
                    ImUtils.createImageSendMessage(MainActivity.this, path, false, ImUtils.KEFU_ID);
                }
            }
        }
    }

    private List<Message> chatMessages = new ArrayList<>();

    private void initRecyclerView() {
        chatMultiItemTypeAdapter = new ChatMultiItemTypeAdapter(this, chatMessages);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);


        mRecyclerView.setAdapter(chatMultiItemTypeAdapter);

        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {


                mInputMessageView.hideExtendView(true);

                return false;
            }
        });


        mInputMessageView.setMenuOpenStatusCallback(new MenuOpenStatusCallback() {
            @Override
            public void open() {

                if (BuildConfig.DEBUG) {
                    Log.d("MainActivity", "open");
                }
                if (chatMessages != null && chatMessages.size() > 0) {
                    mRecyclerView.scrollToPosition(chatMessages.size() - 1);
                }
            }

            @Override
            public void close() {
                if (BuildConfig.DEBUG) {
                    Log.d("MainActivity", "close");
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }


        ImUtils.getAllMessages();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {


        switch (event.code) {


            case 1000:
                List<Message> messages = (List<Message>) event.t;
                if (messages != null && messages.size() > 0) {
                    chatMessages.clear();
                    chatMessages.addAll(messages);

                    addTimeLine();

                    chatMultiItemTypeAdapter.notifyDataSetChanged();

                    mRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);


                    for (Message msg : chatMessages) {
                        Log.e("chatMessages", "消息列表: " + msg.toString() + "\n消息id: " + msg.messageId());
                    }
                }
                break;
            case 1001:
                ImUtils.getAllMessages();
                break;
            case 1002:
                Toast.makeText(this, "刷新成功", Toast.LENGTH_SHORT).show();
                mSwipeRefresh.setRefreshing(false);

                List<Message> list = (List<Message>) event.t;
                if (list != null && list.size() > 0) {


                    int size = list.size();

                    chatMessages.addAll(list);

                    Collections.sort(chatMessages, new Comparator<Message>() {
                        @Override
                        public int compare(Message lhs, Message rhs) {
                            return (int) (lhs.messageTime() - rhs.messageTime());
                        }
                    });

                    addTimeLine();

                    chatMultiItemTypeAdapter.notifyDataSetChanged();
                    if (size > 0) {
                        mRecyclerView.smoothScrollToPosition(size - 1);
                    }
                } else {
                    Toast.makeText(this, "没有更多数据了", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2001:
                Message message = (Message) event.t;

                if (message != null) {
                    for (Message msg : chatMessages) {
                        if (msg.messageId().equals(message.messageId())) {
                            msg.setProgress(message.getProgress());
                        }
                    }

                    chatMultiItemTypeAdapter.notifyDataSetChanged();
                }
                break;
            default:
        }

    }

    /**
     * 两条消息之间时间间隔大于5分钟显示该消息的时间
     * <p>
     * 实现时间间隔的显示
     * 最好的做法是ChatMultiItemTypeAdapter 自定义类型 这里偷懒使用ChatMultiItemTypeAdapter<Message>
     */
    private void addTimeLine() {
        if (chatMessages != null && chatMultiItemTypeAdapter != null) {

            //刷新之前清空时间线

            for (Message msg : chatMessages) {
                Log.d("MainActivity", "msg:" + msg.toString());
                if (ImUtils.isTimeLine(msg)) {
                    chatMessages.remove(msg);
                }
            }

            int size = chatMessages.size() - 1;

            for (int i = 0; i < size; i++) {

                Message message1 = chatMessages.get(i);
                Message message2 = chatMessages.get(i + 1);


                long diff = TimeUtils.diff(message2.messageTime(), message1.messageTime());

                if (diff >= 5) {
                    Message message = Message.createTxtSendMessage("", "");
                    message.setMessageTime(message2.messageTime());
                    //实现时间间隔的显示
                    //最好的做法是ChatMultiItemTypeAdapter 自定义类型 这里偷懒使用ChatMultiItemTypeAdapter<Message>
                    message.setAttribute("showTimeLine", true);

                    chatMessages.add(i + 1, message);
                }

                if (BuildConfig.DEBUG){

                    Log.d("MainActivity", i + " message1:" + message1.toString());
                }
                if (BuildConfig.DEBUG){

                    Log.d("MainActivity", i + " message2:" + message2.toString());
                }
            }


        }
    }


}
