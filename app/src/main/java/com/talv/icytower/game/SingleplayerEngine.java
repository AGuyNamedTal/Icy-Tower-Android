package com.talv.icytower.game;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.Log;

import com.talv.icytower.ImageHelper;
import com.talv.icytower.game.platform.Platform;

import static com.talv.icytower.gui.GUI.CONTROLS.PAUSE_BTN;
import static com.talv.icytower.gui.GUI.CONTROLS.checkActive;

public class SingleplayerEngine extends Engine {
    public SingleplayerEngine(int renderWidth, int renderHeight, Resources resources, GameCanvas gameCanvas, Context context) {
        super(renderWidth, renderHeight, resources, gameCanvas, context);
    }

}
