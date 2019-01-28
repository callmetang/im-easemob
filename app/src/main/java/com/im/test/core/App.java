package com.im.test.core;

import android.app.Application;

import com.hyphenate.chat.ChatClient;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by a on 2019/1/24.
 */

public class App extends Application {
    private static final String APPKEY  ="1400190123061625#kefuchannelapp65143";
    private static final String TENANTID  ="65143";

    @Override
    public void onCreate() {
        super.onCreate();


        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));

        ChatClient.Options options = new ChatClient.Options();
        options.setAppkey(APPKEY);//必填项，appkey获取地址：kefu.easemob.com，“管理员模式 > 渠道管理 > 手机APP”页面的关联的“AppKey”
        options.setTenantId(TENANTID);//必填项，tenantId获取地址：kefu.easemob.com，“管理员模式 > 设置 > 企业信息”页面的“租户ID”


        //设置为true后，将打印日志到logcat, 发布APP时应关闭该选项
        options.setConsoleLog(true);
        // Kefu SDK 初始化
        if (!ChatClient.getInstance().init(this, options)){
            return;
        }
        //后面可以设置其他属性
    }
}
