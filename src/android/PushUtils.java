package com.alipush;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PushUtils {

    private SharedPreferences preference;

    public PushUtils(Context context) {
        this.preference = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // public static String getCurProcessName(Context context) {
    //     int pid = android.os.Process.myPid();
    //     ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    //     for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
    //         if (appProcess.pid == pid) {
    //             return appProcess.processName;
    //         }
    //     }
    //     return null;
    // }

    /**
     * 获取推送配置信息
     *
     * @param applicationContext
     */
    private static void getConfig(final Context applicationContext) throws PackageManager.NameNotFoundException {
        ApplicationInfo appInfo = applicationContext.getPackageManager()
                .getApplicationInfo(applicationContext.getPackageName(), PackageManager.GET_META_DATA);

        // XiaoMiAppId = appInfo.metaData.get("XiaoMiAppId") + "";
        // XiaoMiAppKey = appInfo.metaData.get("XiaoMiAppKey") + "";

        // OPPOAppKey = appInfo.metaData.get("OPPOAppKey") + "";
        // OPPOAppSecret = appInfo.metaData.get("OPPOAppSecret") + "";

        // MeizuAppId = appInfo.metaData.get("MeizuAppId") + "";
        // MeizuAppKey = appInfo.metaData.get("MeizuAppKey") + "";
    }

    /**
     * 解决androidP 第一次打开程序出现莫名弹窗 弹窗内容“detected problems with api ”
     */
    // private void closeAndroidPDialog() {
    // try {
    // Class aClass = Class.forName("android.content.pm.PackageParser$Package");
    // Constructor declaredConstructor =
    // aClass.getDeclaredConstructor(String.class);
    // declaredConstructor.setAccessible(true);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // try {
    // Class cls = Class.forName("android.app.ActivityThread");
    // Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
    // declaredMethod.setAccessible(true);
    // Object activityThread = declaredMethod.invoke(null);
    // Field mHiddenApiWarningShown =
    // cls.getDeclaredField("mHiddenApiWarningShown");
    // mHiddenApiWarningShown.setAccessible(true);
    // mHiddenApiWarningShown.setBoolean(activityThread, true);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

    public void setNoticeJsonData(String jsonObject) {
        // response为后台返回的json数据
        preference.edit().putString("NoticeJsonData", jsonObject).apply(); // 存入json串
    }

    public String getNotice() {
        String jsonData = preference.getString("NoticeJsonData", "");
        // 每次取到json数据后，将其清空
        preference.edit().putString("NoticeJsonData", "").apply();
        try {
            JSONObject data = new JSONObject(jsonData);
            AliyunPush.pushData(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonData;
    }

    // public void setIsShowNoticePermissions(boolean isShow) {
    // preference.edit().putBoolean("ShowNoticePermissions", isShow).apply();
    // }

    // public boolean getIsShowNoticePermissions() {
    // return preference.getBoolean("ShowNoticePermissions", true);
    // }

    /**
     * 请求通知权限
     */
    public void isShowNoticeDialog(Activity context, String msg) {
        NotificationManagerCompat notification = NotificationManagerCompat.from(context);
        boolean isEnabled = notification.areNotificationsEnabled();
        if (msg == null) {
            msg = "建议你开启通知权限，第一时间收到提醒";
        }
        // 未打开通知
        if (!isEnabled) {
            try {
                showDialog(context, msg);
            } catch (Exception ex) {
            }
        }

    }

    public void showDialog(Activity context, String msg) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(context).setTitle("开启推送通知").setMessage(msg)
                        .setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // preference.edit().putBoolean("ShowNoticePermissions", false).apply();
                            }
                        }).setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                Intent intent = new Intent();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                                    intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 5.0
                                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                                    intent.putExtra("app_package", context.getPackageName());
                                    intent.putExtra("app_uid", context.getApplicationInfo().uid);
                                    context.startActivity(intent);
                                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) { // 4.4
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                                } else if (Build.VERSION.SDK_INT >= 15) {
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                    intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                                }
                                context.startActivity(intent);

                            }
                        }).create();
                if (context.isFinishing())
                    return;
                alertDialog.show();
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            }
        }, 5000);

    }
}
