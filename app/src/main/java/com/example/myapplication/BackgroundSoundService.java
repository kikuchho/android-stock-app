package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.Nullable;

public class BackgroundSoundService extends Service {
    MediaPlayer musicPlayer;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        musicPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
        musicPlayer.setLooping(true); // Set looping
        musicPlayer.setVolume(100,100);



    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        musicPlayer.start();
        return Service.START_STICKY;
    }
    @Override
    public void onDestroy() {
        musicPlayer.stop();
        musicPlayer.release();
    }


}
