package com.talv.icytower.game.musicService;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MusicServiceConnection implements ServiceConnection {
    private MediaPlayerService mediaPlayerService;
    private boolean serviceBounded = false;

    public boolean isServiceBounded() {
        return serviceBounded;
    }

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

    @Override
    public String toString() {
        return "MusicServiceConnection{" +
                "mediaPlayerService=" + mediaPlayerService +
                ", serviceBounded=" + serviceBounded +
                '}';
    }


}
