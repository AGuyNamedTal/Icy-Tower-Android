package com.talv.icytower.game.player;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.talv.icytower.ImageHelper;
import com.talv.icytower.R;
import com.talv.icytower.game.Debug;

import java.util.HashMap;

public class CoolGuy extends Player {


    public CoolGuy(Resources resources, float playerSizeMultiple) {
        super(resources, playerSizeMultiple);
    }

    @Override
    void initializeAnimations(Resources resources) {

        Bitmap[] bitmaps = ImageHelper.stretch(
                ImageHelper.decodeAnimations(BitmapFactory.decodeResource(resources, R.drawable.cool_guy_animations), true),
                playerSizeMultiple, true);
        animations = new HashMap<>(bitmaps.length);
        animations.put(PlayerState.STANDING, new BitmapAnimation(new Bitmap[]{
                bitmaps[0],
                bitmaps[1],
                bitmaps[2],
                // bitmaps[1]
        }, false, 400));
        animations.put(PlayerState.SIDE_STAND, new BitmapAnimation(new Bitmap[]{
                bitmaps[3]
        }, true));
        animations.put(PlayerState.MOVING, new BitmapAnimation(new Bitmap[]{
                bitmaps[3],
                bitmaps[4],
                bitmaps[5],
                bitmaps[6]
        }, true, 100));
        animations.put(PlayerState.JUMPING, new BitmapAnimation(new Bitmap[]{
                bitmaps[7]
        }, true));
        animations.put(PlayerState.JUMP_MOVE, new BitmapAnimation(new Bitmap[]{
                bitmaps[8],
                bitmaps[9],
                bitmaps[10]
        }, true, 200));
        animations.put(PlayerState.STARRING, new BitmapAnimation(new Bitmap[]{
                bitmaps[11]
        }, true));

    }

    @Override
    void updateStateAndAnimation(PlayerState newState, int msPassed) {
        if (currentState == newState) {
            animations.get(currentState).increaseTime(msPassed);
        } else {
            currentState = newState;
            animations.get(currentState).resetTime();
            stateUpdateTime = (int) System.currentTimeMillis();
        }

    }


    @Override
    Bitmap getCurrentImage() {
        if (Debug.LOG_ANIMATION)
            Log.d("animation", currentState.toString());
        return animations.get(currentState).getCurrentBitmap(currentDirection);
    }

}
