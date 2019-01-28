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
import com.im.test.utils.IMUtils;
import com.im.test.utils.ImageUtils;
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

public class MainActivity extends AppCompatActivity {

    private InputMessageView mInputMessageView;
    private RecyclerView mRecyclerView;

    private SwipeRefreshLayout mSwipeRefresh;


    private ChatMultiItemTypeAdapter chatMultiItemTypeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mInputMessageView = (InputMessageView) findViewById(R.id.input_message_view);
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        IMUtils.isLoggedInBefore(this);

        mInputMessageView.setSendCallback(new SendCallback() {
            @Override
            public void send(EditText editText) {
                String text = editText.getText().toString().trim();
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(MainActivity.this, "消息为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                IMUtils.createTxtSendMessage(MainActivity.this, text, IMUtils.KEFU_ID);

                editText.setText("");
            }
        });


        initRecyclerView();

        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (chatMessages != null && chatMessages.size() > 0) {
                    String startMsgId = chatMessages.get(0).getMsgId();
                    Log.e("MainActivity", "startMsgId: " + startMsgId);
                    IMUtils.loadMessagesByStartMsgId(startMsgId);
                }
            }
        });


        mInputMessageView.setPictureListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/*");

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 222);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 222 && data != null) {
            Uri imageUri = data.getData();
            File file = ImageUtils.getFileByUri(imageUri, MainActivity.this);
            if (file != null) {

                String path = file.getAbsolutePath();
                Log.e("TAG", path);
                if (imageUri != null) {
                    IMUtils.createImageSendMessage(MainActivity.this, path, false, IMUtils.KEFU_ID);
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

                if (BuildConfig.DEBUG) Log.d("MainActivity", "open");
                if (chatMessages != null && chatMessages.size() > 0) {
                    mRecyclerView.scrollToPosition(chatMessages.size() - 1);
                }
            }

            @Override
            public void close() {
                if (BuildConfig.DEBUG) Log.d("MainActivity", "close");
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }


        IMUtils.getAllMessages();
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

                    chatMultiItemTypeAdapter.notifyDataSetChanged();

                    mRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);


                    for (Message msg : chatMessages) {
                        Log.e("chatMessages", "消息列表: " + msg.toString() + "\n消息id: " + msg.getMsgId());
                    }
                }
                break;
            case 1001:
                IMUtils.getAllMessages();
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
                            return (int) (lhs.getMsgTime() - rhs.getMsgTime());
                        }
                    });

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
                        if (msg.getMsgId().equals(message.getMsgId())) {
                            msg.setProgress(message.getProgress());
                        }
                    }

                    chatMultiItemTypeAdapter.notifyDataSetChanged();
                }
                break;
        }

    }


}
