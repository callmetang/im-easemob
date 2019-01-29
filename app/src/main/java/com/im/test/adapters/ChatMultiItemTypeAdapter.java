package com.im.test.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.Message;
import com.hyphenate.helpdesk.callback.Callback;
import com.im.test.BuildConfig;
import com.im.test.R;
import com.im.test.dialogs.DialogUtils;
import com.im.test.utils.ImUtils;
import com.im.test.utils.TimeUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.text.ParseException;
import java.util.List;

/**
 * @author a
 * @date 2019/1/28
 */

public class ChatMultiItemTypeAdapter extends MultiItemTypeAdapter<Message> {
    public ChatMultiItemTypeAdapter(Context context, List<Message> datas) {
        super(context, datas);
        addItemViewDelegate(new TextSendItemViewDelegate());
        addItemViewDelegate(new TextReceiveItemViewDelegate());
        addItemViewDelegate(new ImageSendItemViewDelegate());
        addItemViewDelegate(new ImageReceiveItemViewDelegate());
        addItemViewDelegate(new TimeLineViewDelegate());


    }

    /**
     * 时间间隔
     */
    class TimeLineViewDelegate implements ItemViewDelegate<Message> {

        @Override
        public int getItemViewLayoutId() {
            return R.layout.chat_time_line;
        }

        @Override
        public boolean isForViewType(Message item, int position) {
            return item.getType().equals(Message.Type.TXT) && ImUtils.isTimeLine(item);
        }

        @Override
        public void convert(ViewHolder holder, final Message message, int position) {

            TextView textView = holder.getView(R.id.tv_content);
            String time = "";
            try {
                time = TimeUtils.longToString(message.messageTime(), TimeUtils.FORMAT_TYPE_1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            textView.setText(time);
        }
    }


    /**
     * 文字类型 用户本人
     */
    class TextSendItemViewDelegate implements ItemViewDelegate<Message> {

        @Override
        public int getItemViewLayoutId() {
            return R.layout.chat_text_send;
        }

        @Override
        public boolean isForViewType(Message item, int position) {
            return item.getType().equals(Message.Type.TXT) && item.direct().equals(Message.Direct.SEND) && !ImUtils.isTimeLine(item);
        }

        @Override
        public void convert(ViewHolder holder, final Message message, int position) {

            TextView textView = holder.getView(R.id.tv_content);
            ImageView ivFail = holder.getView(R.id.iv_fail);

            EMTextMessageBody emTextMessageBody = (EMTextMessageBody) message.body();

            String value = emTextMessageBody.getMessage();


            textView.setText(value);


            if (message.status().equals(Message.Status.FAIL)) {
                ivFail.setVisibility(View.VISIBLE);
            } else {
                ivFail.setVisibility(View.GONE);
            }

            ivFail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DialogUtils.showDialog(mContext, "提示", "消息发送失败,是否重新发送?",
                            "取消", "重发",
                            null, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();

                                    ImUtils.reStartMessage(message, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            message.setStatus(Message.Status.SUCCESS);
                                            notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onError(int code, String error) {
                                            message.setStatus(Message.Status.FAIL);
                                            notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onProgress(int progress, String status) {

                                        }
                                    });
                                }
                            });
                }
            });

            Log.e("TextSendItemViewDelegat", "消息状态: " + message.status());
        }
    }

    /**
     * 文字类型 客服
     */
    class TextReceiveItemViewDelegate implements ItemViewDelegate<Message> {

        @Override
        public int getItemViewLayoutId() {
            return R.layout.chat_text_receive;
        }

        @Override
        public boolean isForViewType(Message item, int position) {
            return item.getType().equals(Message.Type.TXT) && item.direct().equals(Message.Direct.RECEIVE) && !ImUtils.isTimeLine(item);
        }

        @Override
        public void convert(ViewHolder holder, Message message, int position) {

            TextView textView = holder.getView(R.id.tv_content);

            EMTextMessageBody emTextMessageBody = (EMTextMessageBody) message.body();
            String value = emTextMessageBody.getMessage();

            textView.setText(value);
        }
    }

    private static final int PROGRESS_VALUE_START = 0;
    private static final int PROGRESS_VALUE_END = 100;

    /**
     * 图片类型 客服
     */
    class ImageReceiveItemViewDelegate implements ItemViewDelegate<Message> {

        @Override
        public int getItemViewLayoutId() {
            return R.layout.chat_image_receive;
        }

        @Override
        public boolean isForViewType(Message item, int position) {
            return item.getType().equals(Message.Type.IMAGE) && item.direct().equals(Message.Direct.RECEIVE);
        }

        @Override
        public void convert(ViewHolder holder, final Message message, int position) {

            ImageView ivContent = holder.getView(R.id.iv_content);
            ImageView ivFail = holder.getView(R.id.iv_fail);

            TextView mTvProgress = (TextView) holder.getView(R.id.tv_progress);


            int progress = message.getProgress();
            if (progress == PROGRESS_VALUE_START || progress == PROGRESS_VALUE_END) {
                mTvProgress.setVisibility(View.GONE);
            } else {
                mTvProgress.setVisibility(View.VISIBLE);
                mTvProgress.setText(message.getProgress() + "%");
            }

            EMImageMessageBody emImageMessageBody = (EMImageMessageBody) message.body();

            if (BuildConfig.DEBUG) {
                Log.e("ImageSendItemViewDelega", "图片消息: " + emImageMessageBody.toString());
            }

            ImageLoader.getInstance().displayImage("file://" + emImageMessageBody.thumbnailLocalPath(), ivContent);

            if (message.status().equals(Message.Status.FAIL)) {
                ivFail.setVisibility(View.VISIBLE);
            } else {
                ivFail.setVisibility(View.GONE);
            }

            ivFail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DialogUtils.showDialog(mContext, "提示", "消息发送失败,是否重新发送?",
                            "取消", "重发",
                            null, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();

                                    ImUtils.reStartMessage(message, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            message.setStatus(Message.Status.SUCCESS);
                                            notifyDataSetChanged();

                                            message.setProgress(PROGRESS_VALUE_END);
                                        }

                                        @Override
                                        public void onError(int code, String error) {
                                            message.setStatus(Message.Status.FAIL);
                                            notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onProgress(int progress, String status) {
                                            message.setProgress(progress);
                                        }
                                    });
                                }
                            });
                }
            });

            Log.e("TextSendItemViewDelegat", "消息状态: " + message.status());
        }
    }

    /**
     * 图片类型 用户本人
     */
    class ImageSendItemViewDelegate implements ItemViewDelegate<Message> {

        @Override
        public int getItemViewLayoutId() {
            return R.layout.chat_image_send;
        }

        @Override
        public boolean isForViewType(Message item, int position) {
            return item.getType().equals(Message.Type.IMAGE) && item.direct().equals(Message.Direct.SEND);
        }

        @Override
        public void convert(ViewHolder holder, final Message message, int position) {

            ImageView ivContent = holder.getView(R.id.iv_content);
            ImageView ivFail = holder.getView(R.id.iv_fail);

            TextView mTvProgress = (TextView) holder.getView(R.id.tv_progress);


            int progress = message.getProgress();
            if (progress == PROGRESS_VALUE_START || progress == PROGRESS_VALUE_END) {
                mTvProgress.setVisibility(View.GONE);
            } else {
                mTvProgress.setVisibility(View.VISIBLE);
                mTvProgress.setText(message.getProgress() + "%");
            }

            EMImageMessageBody emImageMessageBody = (EMImageMessageBody) message.body();

            if (BuildConfig.DEBUG) {
                Log.e("ImageSendItemViewDelega", "图片消息: " + emImageMessageBody.toString());
            }

            ImageLoader.getInstance().displayImage("file://" + emImageMessageBody.thumbnailLocalPath(), ivContent);

            if (message.status().equals(Message.Status.FAIL)) {
                ivFail.setVisibility(View.VISIBLE);
            } else {
                ivFail.setVisibility(View.GONE);
            }

            ivFail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DialogUtils.showDialog(mContext, "提示", "消息发送失败,是否重新发送?",
                            "取消", "重发",
                            null, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();

                                    ImUtils.reStartMessage(message, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            message.setStatus(Message.Status.SUCCESS);
                                            notifyDataSetChanged();

                                            message.setProgress(100);
                                        }

                                        @Override
                                        public void onError(int code, String error) {
                                            message.setStatus(Message.Status.FAIL);
                                            notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onProgress(int progress, String status) {
                                            message.setProgress(progress);
                                        }
                                    });
                                }
                            });
                }
            });

            Log.e("TextSendItemViewDelegat", "消息状态: " + message.status());
        }
    }

}
