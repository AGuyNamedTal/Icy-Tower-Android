package com.talv.icytower.game.musicService;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MusicServiceConnection implements ServiceConnection {
    public MediaPlayerService mediaPlayerService;
    public boolean serviceBounded = false;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceBounded = true;
        MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
        mediaPlayerService = binder.getInstance();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        serviceBounded = false;
        mediaPlayerService = null;
    }

    public void start() {
        if (mediaPlayerService == null) return;
        mediaPlayerService.start();
    }

    public void stop() {
        if (mediaPlayerService == null) return;
        mediaPlayerService.stop();
    }

    public void increaseSpeed() {
        if (mediaPlayerService == null) return;
        mediaPlayerService.increaseSpeed();
    }

    public void resetSpeed() {
        if (mediaPlayerService == null) return;
        mediaPlayerService.resetSpeed();
    }

    public void pause() {
        if (mediaPlayerService == null) return;
        mediaPlayerService.pause();
    }

    public boolean isPlaying() {
        if (mediaPlayerService == null) return false;
        return mediaPlayerService.isPlaying();
    }
}
