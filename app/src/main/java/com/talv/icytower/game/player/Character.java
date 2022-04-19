package com.talv.icytower.game.player;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.talv.icytower.R;
import com.talv.icytower.game.utils.BitmapUtils;

import java.util.HashMap;

public class Character {

    public final HashMap<Player.PlayerState, PlayerAnimation> animations;
    public final int width;
    public final int height;

    private Character(HashMap<Player.PlayerState, PlayerAnimation> animations, int width, int height) {
        this.animations = animations;
        this.width = width;
        this.height = height;
    }

    public static Character loadPlayer1(Resources resources, float playerSizeMultiple) {
        Bitmap animationSheet = BitmapFactory.decodeResource(resources, R.drawable.player_1_animations);
        Bitmap[] bitmaps = BitmapUtils.stretch(
                BitmapUtils.decodeAnimations(animationSheet, true),
                playerSizeMultiple, true);
        int width = Math.round(bitmaps[0].getWidth());
        int height = Math.round(animationSheet.getHeight() * playerSizeMultiple * 0.93f);
        return new Character(loadPlayerAnimations(bitmaps), width, height);
    }

    public static Character loadPlayer2(Resources resources, float playerSizeMultiple) {
        Bitmap animationSheet = BitmapFactory.decodeResource(resources, R.drawable.player_2_animations);

        Bitmap[] bitmaps = BitmapUtils.stretch(
                BitmapUtils.decodeAnimations(animationSheet, true),
                playerSizeMultiple, true);

        // add missing 4th image
        Bitmap[] newBitmaps = new Bitmap[bitmaps.length + 1];
        System.arraycopy(bitmaps, 0, newBitmaps, 0, 3);
        newBitmaps[3] = bitmaps[5];
        System.arraycopy(bitmaps, 3, newBitmaps, 4, newBitmaps.length - 4);
        int width = Math.round(bitmaps[0].getWidth() * playerSizeMultiple);
        int height = Math.round(animationSheet.getHeight() * playerSizeMultiple * 0.93f);
        return new Character(loadPlayerAnimations(newBitmaps), width, height);

    }


    private static HashMap<Player.PlayerState, PlayerAnimation> loadPlayerAnimations(Bitmap[] bitmaps) {
        HashMap<Player.PlayerState, PlayerAnimation> animations = new HashMap<>(bitmaps.length);
        animations.put(Player.PlayerState.STANDING, new PlayerAnimation(new Bitmap[]{
                bitmaps[0],
                bitmaps[1],
                bitmaps[2],
                // bitmaps[1]
        }, false, 300));
        animations.put(Player.PlayerState.SIDE_STAND, new PlayerAnimation(new Bitmap[]{
                bitmaps[3]
        }, true));
        animations.put(Player.PlayerState.MOVING, new PlayerAnimation(new Bitmap[]{
                bitmaps[3],
                bitmaps[4],
                bitmaps[5],
                bitmaps[6]
        }, true, 100));
        animations.put(Player.PlayerState.JUMPING, new PlayerAnimation(new Bitmap[]{
                bitmaps[7]
        }, true));
        animations.put(Player.PlayerState.JUMP_MOVE, new PlayerAnimation(new Bitmap[]{
                bitmaps[8],
                bitmaps[9],
                bitmaps[10]
        }, true, 200));
        animations.put(Player.PlayerState.STARRING, new PlayerAnimation(new Bitmap[]{
                bitmaps[11]
        }, true));
        return animations;
    }

    @Override
    public String toString() {
        return "Character{" +
                "animations=" + animations +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
