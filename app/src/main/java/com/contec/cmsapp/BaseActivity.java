package com.contec.cmsapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.contec.helper.WhLogger;

import java.util.List;

public class BaseActivity extends AppCompatActivity {

    private Context mContext;
    private Boolean islockScreen = false;
    private Boolean isPlaying = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    public void setPlayPrepare(){
        isPlaying = false;
    }
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    //    WhLogger.e("BaseActivity_stop","isAppOnForeground()"+isAppOnForeground());
        //   WhLogger.e("BaseActivity_stop","islockScreen"+islockScreen);
        if (!isAppOnForeground()|| islockScreen) {
            //app 进入后台
            WhLogger.e("isPlaying",isPlaying+"");
            if (isPlaying){ return;}
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final MediaPlayer player = MediaPlayer.create(mContext, R.raw.warning_info);

                    isPlaying = true;
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                            isPlaying = false;
                        }
                    });
             //       player.prepareAsync();
                    player.start();
                }
            }).start();
            //全局变量isActive = false 记录当前已经进入后台
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //if (!isActive) {
        //app 从后台唤醒，进入前台

        //isActive = true;
        //}
    }

    /**
     * 程序是否在前台运行
     *
     * @return
     */
    public boolean isAppOnForeground() {
        // Returns a list of application processes that are running on the
        // device

        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

}
