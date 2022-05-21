package com.talv.icytower.game.musicService;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Binder;
import android.os.IBinder;

import com.talv.icytower.R;

public class MediaPlayerService extends Service {

    private MediaPlayer musicPlayer;
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MediaPlayerService getInstance() {
            return MediaPlayerService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        musicPlayer = MediaPlayer.create(getApplicationContext(), R.raw.background_music);
        AudioAttributes attr = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
        musicPlayer.setAudioAttributes(attr);
        musicPlayer.setLooping(true);
        musicPlayer.setVolume(0.4f, 0.4f);

    }



    public void start() {
        musicPlayer.start();
    }

    public void stop() {
        musicPlayer.pause();
        musicPlayer.seekTo(0);
    }

    public void increaseSpeed() {
        musicPlayer.setPlaybackParams(new PlaybackParams().setSpeed(musicPlayer.getPlaybackParams().getSpeed() * 1.1f));
    }

    public void resetSpeed() {
        musicPlayer.setPlaybackParams(new PlaybackParams().setSpeed(1f));
    }

    public void pause() {
        musicPlayer.pause();
    }

    public boolean isPlaying() {
        if (musicPlayer == null) return false;
        return musicPlayer.isPlaying();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (musicPlayer != null) musicPlayer.release();
    }

    @Override
    public String toString() {
        return "MediaPlayerService{" +
                "musicPlayer=" + musicPlayer +
                ", mBinder=" + binder +
                "} " + super.toString();
    }
}
