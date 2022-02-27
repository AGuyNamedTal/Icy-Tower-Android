package com.talv.icytower.game.engine;

import android.content.Context;
import android.content.res.Resources;

import com.talv.icytower.game.GameCanvas;

public class SingleplayerEngine extends Engine {
    public SingleplayerEngine(int renderWidth, int renderHeight, Resources resources, GameCanvas gameCanvas, Context context) {
        super(renderWidth, renderHeight, resources, gameCanvas, context);
    }

    @Override
    int minPlayerY() {
        return player.rect.top;
    }

    @Override
    int maxPlayerY() {
        return player.rect.top;
    }

}