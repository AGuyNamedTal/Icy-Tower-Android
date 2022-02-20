package com.talv.icytower.game;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.talv.icytower.ImageHelper;
import com.talv.icytower.R;
import com.talv.icytower.RectHelper;
import com.talv.icytower.firebase.FirebaseHelper;
import com.talv.icytower.firebase.GameStats;
import com.talv.icytower.firebase.UserProfileInfo;
import com.talv.icytower.game.platform.DisappearingPlatform;
import com.talv.icytower.game.platform.Platform;
import com.talv.icytower.game.player.Character;
import com.talv.icytower.game.player.Player;
import com.talv.icytower.gui.graphiccontrols.ClockControl;
import com.talv.icytower.gui.graphiccontrols.Control;
import com.talv.icytower.gui.graphiccontrols.OnClockTimeUpListener;
import com.talv.icytower.gui.graphiccontrols.UpdatingControl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.talv.icytower.gui.GUI.CONTROLS.CHOOSE_PLAYER_CONTROLS;
import static com.talv.icytower.gui.GUI.CONTROLS.CLOCK;
import static com.talv.icytower.gui.GUI.CONTROLS.GAMEPLAY_CONTROLS;
import static com.talv.icytower.gui.GUI.CONTROLS.GAME_LOST_CONTROLS;
import static com.talv.icytower.gui.GUI.CONTROLS.GAME_STATS_TXT;
import static com.talv.icytower.gui.GUI.CONTROLS.MAX_FLAGS;
import static com.talv.icytower.gui.GUI.CONTROLS.NEW_HIGH_SCORE_TXT;
import static com.talv.icytower.gui.GUI.CONTROLS.PAUSE_MENU_CONTROLS;
import static com.talv.icytower.gui.GUI.CONTROLS.PERSONAL_HIGH_SCORE_TXT;
import static com.talv.icytower.gui.GUI.CONTROLS.YOUR_SCORE_TXT;

public class MultiplayerEngine extends Engine {

    private Matrix topMatrix;
    private Matrix bottomMatrix;
    private int halfRenderHeight;



    public MultiplayerEngine(int renderWidth, int renderHeight, Resources resources, GameCanvas gameCanvas, Context context) {
        super(renderWidth, renderHeight, resources, gameCanvas, context);
        halfRenderHeight = renderHeight / 2;
        initializeMatrices();
    }


    public void updateFrame() {
        // draw on frame
        Canvas bitmapCanvas = new Canvas(frame);
        // draw top

        bitmapCanvas.setMatrix(topMatrix);
        bitmapCanvas.save();
//         draw background
        bitmapCanvas.drawBitmap(backgroundImg, 0, 0, gamePaint);
//        // draw platforms

        for (Platform platform : platforms) {
            platform.render(bitmapCanvas, this);
        }
        //draw player
        player.render(bitmapCanvas, this);



        // draw bottom
        bitmapCanvas.restore();
        bitmapCanvas.setMatrix(bottomMatrix);
        bitmapCanvas.save();
        // draw background
        bitmapCanvas.drawBitmap(backgroundImg, 0, 0, gamePaint);
//         draw platforms
        for (Platform platform : platforms) {
            platform.render(bitmapCanvas, this);
        }
        //draw player
        player.render(bitmapCanvas, this);


        // final render (stretch)
        frameScaled = ImageHelper.stretch(frame, renderWidth, renderHeight, false);

        // add controls
        Canvas finalFrameCanvas = new Canvas(frameScaled);
        if (currentGameState == GameState.PAUSED || currentGameState == GameState.LOST) {
            // reduce brightness of background game
            finalFrameCanvas.drawRect(0, 0, renderWidth, renderHeight, pausePaint);
        }
        gameCanvas.renderControls(finalFrameCanvas);
    }

    private void initializeMatrices(){
        topMatrix = new Matrix();
        bottomMatrix = new Matrix();
        topMatrix.preScale(1, 0.5f);
        topMatrix.setRotate(180);
        topMatrix.postTranslate(cameraWidth, cameraHeight / 2f);
        bottomMatrix.setScale(1, 0.5f);
        bottomMatrix.postTranslate(0, cameraHeight / 2f);
    }
}
