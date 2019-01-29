package com.im.test.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.im.test.BuildConfig;
import com.im.test.R;
import com.im.test.utils.DensityUtil;
import com.im.test.utils.KeyBoardUtils;

/**
 *
 * @author a
 * @date 2019/1/28
 */

public class InputMessageView extends FrameLayout {
    public InputMessageView(@NonNull Context context) {
        this(context, null);
    }

    public InputMessageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public InputMessageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.mContext = context;
        inits();
    }


    private Context mContext;

    private ImageView mIvAdd;
    private ImageView mIvSmile;
    private EditText mEdInput;
    private TextView mTvSend;
    private LinearLayout mLayoutAdd;
    private LinearLayout mLayoutSmile;

    private SendCallback sendCallback;
    private FrameLayout mLayoutExtend;


    private LinearLayout mLayoutPicture;

    private MenuOpenStatusCallback menuOpenStatusCallback;

    public void setMenuOpenStatusCallback(MenuOpenStatusCallback menuOpenStatusCallback) {
        this.menuOpenStatusCallback = menuOpenStatusCallback;
    }

    public void setPictureListener(OnClickListener pictureListener) {
        mLayoutPicture.setOnClickListener(pictureListener);
    }

    public void setSendCallback(SendCallback sendCallback) {
        this.sendCallback = sendCallback;
    }

    private void inits() {
        View.inflate(mContext, R.layout.view_input_message, this);


        mLayoutExtend = (FrameLayout) findViewById(R.id.layout_extend);

        mIvAdd = (ImageView) findViewById(R.id.iv_add);
        mIvSmile = (ImageView) findViewById(R.id.iv_smile);
        mEdInput = (EditText) findViewById(R.id.ed_input);
        mTvSend = (TextView) findViewById(R.id.tv_send);
        mLayoutAdd = (LinearLayout) findViewById(R.id.layout_add);
        mLayoutSmile = (LinearLayout) findViewById(R.id.layout_smile);
        mLayoutPicture = (LinearLayout) findViewById(R.id.layout_picture);


        hideExtendView(false);

        initEvents();
    }

    /**
     * 隐藏扩展view
     */
    public void hideExtendView(boolean anim) {

        if (anim && mLayoutExtend.getVisibility() == VISIBLE) {

            if (!isHideing) {
                hideExtendAnim();
            }

        } else {
            mLayoutExtend.setVisibility(GONE);

        }


    }


    private void initEvents() {
        mIvAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {



                showExtendAnim();

                KeyBoardUtils.closeKeyboard(mEdInput, mContext);

                mLayoutExtend.setVisibility(VISIBLE);
                mLayoutAdd.setVisibility(VISIBLE);
                mLayoutSmile.setVisibility(GONE);

                mEdInput.setFocusable(false);
                mEdInput.setFocusableInTouchMode(false);
            }
        });
        mIvSmile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                showExtendAnim();

                KeyBoardUtils.closeKeyboard(mEdInput, mContext);
                mLayoutExtend.setVisibility(VISIBLE);
                mLayoutAdd.setVisibility(GONE);
                mLayoutSmile.setVisibility(VISIBLE);

                mEdInput.setFocusable(false);
                mEdInput.setFocusableInTouchMode(false);
            }
        });

        mEdInput.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                hideExtendView(true);


                mEdInput.setFocusable(true);
                mEdInput.setFocusableInTouchMode(true);
                mEdInput.requestFocus();
                mEdInput.findFocus();
                return false;
            }
        });

        mTvSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sendCallback != null) {
                    sendCallback.send(mEdInput);
                }
            }
        });

    }

    /**
     * 是否在隐藏动画中
     */
    private boolean isHideing = false;

    /**
     * 隐藏动画
     */
    private void hideExtendAnim() {

        isHideing = true;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(DensityUtil.dip2px(mContext, 120), 0);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(300);
        valueAnimator.start();


        mLayoutExtend.postDelayed(new Runnable() {
            @Override
            public void run() {
                isHideing = false;
                mLayoutExtend.setVisibility(GONE);
                if (menuOpenStatusCallback != null) {
                    menuOpenStatusCallback.close();
                }
            }
        }, 300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {


            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                if (mLayoutExtend.getLayoutParams() != null) {
                    int currentValue = (Integer) valueAnimator.getAnimatedValue();
                    mLayoutExtend.getLayoutParams().height = currentValue;
                    mLayoutExtend.requestLayout();
                    if (BuildConfig.DEBUG){

                        Log.d("InputMessageView", "currentValue:" + currentValue);
                    }
                }


            }
        });
    }

    /**
     * 显示动画
     */
    private void showExtendAnim() {

        if (mLayoutExtend.getVisibility() == VISIBLE){
            return;
        }
        final int height = DensityUtil.dip2px(mContext, 120);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, height);
        valueAnimator.setDuration(300);

        valueAnimator.start();

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (mLayoutExtend.getLayoutParams() != null) {

                    int currentValue = (Integer) valueAnimator.getAnimatedValue();

                    mLayoutExtend.getLayoutParams().height = currentValue;
                    mLayoutExtend.requestLayout();


                    if (currentValue == height) {
                        if (menuOpenStatusCallback != null) {
                            menuOpenStatusCallback.open();
                        }
                    }

                    if (BuildConfig.DEBUG){
                        Log.d("InputMessageView", "currentValue:" + currentValue);
                    }
                }
            }
        });
    }
}
