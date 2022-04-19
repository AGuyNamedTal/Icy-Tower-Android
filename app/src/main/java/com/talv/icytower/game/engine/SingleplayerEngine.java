package com.talv.icytower.game.engine;

import android.content.Context;
import android.content.res.Resources;

import com.talv.icytower.game.GameCanvas;
import com.talv.icytower.game.gui.GUI;
import com.talv.icytower.game.musicService.MusicServiceConnection;
import com.talv.icytower.game.player.Player;

public class SingleplayerEngine extends Engine {
    public SingleplayerEngine(int renderWidth, int renderHeight, Resources resources, GameCanvas gameCanvas, Context context, MusicServiceConnection musicServiceConnection) {
        super(renderWidth, renderHeight, resources, gameCanvas, context, musicServiceConnection);
        GameState.PLAYING.controlGroup = GUI.CONTROLS.GAMEPLAY_CONTROLS;
    }

    @Override
    int minPlayerY() {
        return player.rect.top;
    }

    @Override
    int maxPlayerY() {
        return player.rect.top;
    }

    @Override
    Player getWinningPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return "SingleplayerEngine{} " + super.toString();
    }
}
