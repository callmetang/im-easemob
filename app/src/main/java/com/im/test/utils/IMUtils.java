package com.im.test.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.hyphenate.chat.ChatClient;
import com.hyphenate.chat.ChatManager;
import com.hyphenate.chat.Conversation;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.Message;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.helpdesk.callback.Callback;
import com.hyphenate.helpdesk.callback.ValueCallBack;
import com.im.test.beans.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by a on 2019/1/24.
 */

public class IMUtils {
    public static String KEFU_ID = "kefuchannelimid_012700";//客服id
    public static String SELF_ID = "tang";//自己的id

    public static void isLoggedInBefore(final Context context) {
        if (ChatClient.getInstance().isLoggedInBefore()) {
            //已经登录，可以直接进入会话界面
            Log.e("IMUtils", "已经登录");


            addMessageListener();

            getEnterpriseWelcome();
        } else {
            //未登录，需要登录后，再进入会话界面
            Log.e("IMUtils", "未登录");
//            register();

            login(context);
        }
    }

    /**
     * 获取企业欢迎语
     */
    public static void getEnterpriseWelcome() {
        ChatClient.getInstance().chatManager().getEnterpriseWelcome(new ValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {

                Log.e("IMUtils", "获取企业欢迎语 成功: " + value);
                MessageEvent<String> messageEvent = new MessageEvent<>();
                messageEvent.t = value;
                EventBus.getDefault().post(messageEvent);
            }

            @Override
            public void onError(int error, String errorMsg) {

                Log.e("IMUtils", "获取企业欢迎语 失败: " + errorMsg);
            }
        });
    }

    public static void register() {
        ChatClient.getInstance().register(SELF_ID, "123456", new Callback() {
            @Override
            public void onSuccess() {

                Log.e("IMUtils", "注册成功");
            }

            @Override
            public void onError(int code, String error) {


//        ErrorCode:
//        Error.NETWORK_ERROR 网络不可用
//        Error.USER_ALREADY_EXIST  用户已存在
//        Error.USER_AUTHENTICATION_FAILED 无开放注册权限（后台管理界面设置[开放|授权]）
//        Error.USER_ILLEGAL_ARGUMENT 用户名非法
                Log.e("IMUtils", "注册失败code : " + code);
                Log.e("IMUtils", "注册失败error: " + error);
            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
    }

    public static void login(final Context context) {
        ChatClient.getInstance().login("tang", "123456", new Callback() {
            @Override
            public void onSuccess() {

                Log.e("IMUtils", "登录成功");
                Activity activity = (Activity) context;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();
                    }
                });

                addMessageListener();

                getEnterpriseWelcome();
            }

            @Override
            public void onError(int code, String error) {
                Log.e("IMUtils", "登录失败code : " + code);
                Log.e("IMUtils", "登录失败error: " + error);
            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
    }

    /**
     * 消息监听
     */
    public static void addMessageListener() {
        ChatClient.getInstance().getChat().addMessageListener(new ChatManager.MessageListener() {
            @Override
            public void onMessage(List<Message> list) {
                //收到普通消息
                if (list != null) {
                    Log.e("IMUtils", "收到普通消息");

                    MessageEvent messageEvent = new MessageEvent<>();
                    messageEvent.code = 1001;
                    EventBus.getDefault().post(messageEvent);

                    for (Message msg : list) {
                        Log.e("IMUtils", "收到普通消息: " + msg.toString());

//                        if (msg.getType() == Message.Type.TXT) {
//                            EMTextMessageBody emTextMessageBody = (EMTextMessageBody) msg.getBody();
//
//                            String value = "客服普通消息: " + emTextMessageBody.getMessage();
//
//                            MessageEvent<String> messageEvent = new MessageEvent<>();
//                            messageEvent.t = value;
//                            EventBus.getDefault().post(messageEvent);
//                        }

                    }
                }
            }

            @Override
            public void onCmdMessage(List<Message> list) {
                //收到命令消息，命令消息不存数据库，一般用来作为系统通知，例如留言评论更新，
                //会话被客服接入，被转接，被关闭提醒
                if (list != null) {
                    Log.e("IMUtils", "收到命令消息");

                    for (Message msg : list) {
                        Log.e("IMUtils", "收到命令消息: " + msg.toString());

//                        if (msg.getType() == Message.Type.TXT) {
//                            EMTextMessageBody emTextMessageBody = (EMTextMessageBody) msg.getBody();
//
//                            String value = "客服命令消息: " + emTextMessageBody.getMessage();
//
//                            MessageEvent<String> messageEvent = new MessageEvent<>();
//                            messageEvent.t = value;
//                            EventBus.getDefault().post(messageEvent);
//                        }
                    }
                }
            }

            @Override
            public void onMessageStatusUpdate() {
                //消息的状态修改，一般可以用来刷新列表，显示最新的状态
            }

            @Override
            public void onMessageSent() {
                //发送消息后，会调用，可以在此刷新列表，显示最新的消息
            }
        });
    }


    /**
     * 发送文本消息
     */
    public static void createTxtSendMessage(final Context content, String text, String toChatUsername) {
        //发送一条文本消息， content 为消息文字内容， toChatUsername为客服设置的IM服务号
        Message message = Message.createTxtSendMessage(text, toChatUsername);
        ChatClient.getInstance().chatManager().sendMessage(message, new Callback() {
            @Override
            public void onSuccess() {

                MessageEvent messageEvent = new MessageEvent<>();
                messageEvent.code = 1001;
                EventBus.getDefault().post(messageEvent);

                Log.e("IMUtils", "文本消息发送成功");
//                Toast.makeText(content, "文本消息发送成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int code, String error) {

//                Toast.makeText(content, error, Toast.LENGTH_SHORT).show();

                Log.e("IMUtils", "文本消息发送失败code : " + code);
                Log.e("IMUtils", "文本消息发送失败error: " + error);
            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
    }
    /**
     * 发送图片消息
     */
    public static void createImageSendMessage(final Context content, String path,boolean sendOriginalImage, String toChatUsername) {
        ////filePath为图片路径， false为不发送原图(默认超过100k的图片都会压缩后发给对方)，需要发送原图传true， toChatUsername为IM服务号
        final Message message = Message.createImageSendMessage(path, sendOriginalImage, toChatUsername);
        ChatClient.getInstance().chatManager().sendMessage(message, new Callback() {
            @Override
            public void onSuccess() {

                MessageEvent messageEvent = new MessageEvent<>();
                messageEvent.code = 1001;
                EventBus.getDefault().post(messageEvent);

                Log.e("IMUtils", "图片消息发送成功");
//                Toast.makeText(content, "文本消息发送成功", Toast.LENGTH_SHORT).show();
                sendImageProgress(100,message);

            }

            @Override
            public void onError(int code, String error) {

//                Toast.makeText(content, error, Toast.LENGTH_SHORT).show();

                Log.e("IMUtils", "图片消息发送失败code : " + code);
                Log.e("IMUtils", "图片消息发送失败error: " + error);
            }

            @Override
            public void onProgress(int progress, String status) {

                Log.e("IMUtils", "图片消息progress : " + progress);


                sendImageProgress(progress, message);
            }
        });
    }

    /**
     * 发送图片的进度
     * @param progress
     * @param message
     */
    public static void sendImageProgress(int progress, Message message) {
        message.setProgress(progress);

        MessageEvent<Message> messageEvent = new MessageEvent<>();
        messageEvent.t = message;
        messageEvent.code = 2001;
        EventBus.getDefault().post(messageEvent);
    }

    /**
     * 重新发送消息
     */
    public static void reStartMessage(final Message message,Callback callback) {
        ChatClient.getInstance().chatManager().sendMessage(message, callback);
    }
    public static void getAllMessages() {

        Conversation conversation = ChatClient.getInstance().chatManager().getConversation(KEFU_ID);
        if (conversation != null) {
            //获取此会话的所有消息

            List<Message> list = conversation.getAllMessages();

            if (list != null) {
                MessageEvent<List<Message>> messageEvent = new MessageEvent<>();
                messageEvent.code = 1000;
                messageEvent.t = list;
                EventBus.getDefault().post(messageEvent);
            }
        }
    }

    public static void loadMessagesByStartMsgId(String startMsgId) {

        Conversation conversation = ChatClient.getInstance().chatManager().getConversation(KEFU_ID);


        if (conversation != null) {
            //SDK初始化加载的聊天记录为20条，到顶时需要去DB里获取更多
            //获取startMsgId之前的pagesize条消息，此方法获取的messages SDK会自动存入到此会话中，APP中无需再次把获取到的messages添加到会话中
            int pageSize = 10;

            List<Message> messages = conversation.loadMessages(startMsgId, pageSize);

            if (messages != null) {
                MessageEvent<List<Message>> messageEvent = new MessageEvent<>();
                messageEvent.code = 1002;
                messageEvent.t = messages;
                EventBus.getDefault().post(messageEvent);
            }

        }

    }
    /**
     * 是否是时间间隔消息
     * @param item
     * @return
     */
    public static boolean isTimeLine(Message item) {
        boolean showTimeLine = false;
        try {
            showTimeLine = item.getBooleanAttribute("showTimeLine");
        } catch (HyphenateException e) {
        }
        return showTimeLine;
    }

}
