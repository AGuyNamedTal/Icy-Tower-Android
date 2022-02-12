package com.talv.icytower.game.player;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.talv.icytower.ImageHelper;
import com.talv.icytower.R;

import java.util.HashMap;

public class Characters {

    public static HashMap<Player.PlayerState, BitmapAnimation> loadPlayer1Animations(Resources resources, float playerSizeMultiple) {
        Bitmap[] bitmaps = ImageHelper.stretch(
                ImageHelper.decodeAnimations(BitmapFactory.decodeResource(resources, R.drawable.player_1_animations), true),
                playerSizeMultiple, true);
        return loadPlayerAnimations(bitmaps);
    }

    public static HashMap<Player.PlayerState, BitmapAnimation> loadPlayer2Animations(Resources resources, float playerSizeMultiple) {
        Bitmap[] bitmaps = ImageHelper.stretch(
                ImageHelper.decodeAnimations(BitmapFactory.decodeResource(resources, R.drawable.player_2_animations), true),
                playerSizeMultiple, true);
        // add missing 4th image
        Bitmap[] newBitmaps = new Bitmap[bitmaps.length + 1];
        for (int i = 0; i < 3; i++) {
            newBitmaps[i] = bitmaps[i];
        }
        newBitmaps[3] = bitmaps[5];
        for (int i = 4; i < newBitmaps.length; i++) {
            newBitmaps[i] = bitmaps[i - 1];
        }
        return loadPlayerAnimations(newBitmaps);
    }

    private static HashMap<Player.PlayerState, BitmapAnimation> loadPlayerAnimations(Bitmap[] bitmaps) {
        HashMap<Player.PlayerState, BitmapAnimation> animations = new HashMap<>(bitmaps.length);
        animations.put(Player.PlayerState.STANDING, new BitmapAnimation(new Bitmap[]{
                bitmaps[0],
                bitmaps[1],
                bitmaps[2],
                // bitmaps[1]
        }, false, 400));
        animations.put(Player.PlayerState.SIDE_STAND, new BitmapAnimation(new Bitmap[]{
                bitmaps[3]
        }, true));
        animations.put(Player.PlayerState.MOVING, new BitmapAnimation(new Bitmap[]{
                bitmaps[3],
                bitmaps[4],
                bitmaps[5],
                bitmaps[6]
        }, true, 100));
        animations.put(Player.PlayerState.JUMPING, new BitmapAnimation(new Bitmap[]{
                bitmaps[7]
        }, true));
        animations.put(Player.PlayerState.JUMP_MOVE, new BitmapAnimation(new Bitmap[]{
                bitmaps[8],
                bitmaps[9],
                bitmaps[10]
        }, true, 200));
        animations.put(Player.PlayerState.STARRING, new BitmapAnimation(new Bitmap[]{
                bitmaps[11]
        }, true));
        return animations;
    }
}