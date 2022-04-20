package com.talv.icytower.game.player;

import android.graphics.Bitmap;

import com.talv.icytower.activities.GameActivity;
import com.talv.icytower.game.utils.BitmapUtils;

import java.util.Arrays;

public class PlayerAnimation {

    private final Bitmap[] bitmapsRight;
    private final Bitmap[] bitmapsLeft;
    private final int[] switchIndexes;

    public Bitmap[] getBitmapsRight() {
        return bitmapsRight;
    }

    public Bitmap[] getBitmapsLeft() {
        return bitmapsLeft;
    }

    private int currentTime = 0;
    private final int maxIndex;

    public PlayerAnimation(Bitmap[] bitmapsRight, boolean createLeftReflection) {
        this(bitmapsRight, createLeftReflection, defaultSwitchIndexes(bitmapsRight.length));
    }

    public PlayerAnimation(Bitmap[] bitmapsRight, boolean createLeftReflection, int timeBetweenSwitch) {
        this(bitmapsRight, createLeftReflection, switchIndexesConstantTime(timeBetweenSwitch, bitmapsRight.length));
    }

    public PlayerAnimation(Bitmap[] bitmapsRight, boolean createLeftReflection, int[] switchIndexes) {
        this.bitmapsRight = bitmapsRight;
        if (createLeftReflection) {
            bitmapsLeft = BitmapUtils.flipX(bitmapsRight);
        } else {
            bitmapsLeft = bitmapsRight;
        }
        this.switchIndexes = switchIndexes;
        maxIndex = switchIndexes[switchIndexes.length - 1] + 1;
    }

    private static int[] switchIndexesConstantTime(int time, int length) {
        int[] switchIndexes = new int[length];
        for (int i = 0; i < switchIndexes.length; i++) {
            switchIndexes[i] = time * (i + 1);
        }
        return switchIndexes;
    }

    private static int[] defaultSwitchIndexes(int length) {
        int[] switchIndexes = new int[length];
        for (int i = 0; i < switchIndexes.length; i++) {
            switchIndexes[i] = GameActivity.FRAME_WAIT * (i + 1);
        }
        return switchIndexes;
    }


    public void updateTime(int msPassed) {
        currentTime += msPassed;
        if (currentTime >= maxIndex) {
            currentTime = currentTime % maxIndex;
        }
    }

    public void resetTime() {
        currentTime = 0;
    }

    public Bitmap getCurrentBitmap(Player.Direction direction) {
        int index = bitmapsRight.length - 1;
        int previousTime = -1;
        for (int i = 0; i < switchIndexes.length; i++) {
            if (currentTime > previousTime && currentTime <= switchIndexes[i]) {
                index = i;
                break;
            }
            previousTime = switchIndexes[i];
        }
        if (direction == Player.Direction.RIGHT) {
            return bitmapsRight[index];
        }
        return bitmapsLeft[index];
    }


    @Override
    public String toString() {
        return "PlayerAnimation{" +
                "bitmapsRight=" + Arrays.toString(bitmapsRight) +
                ", bitmapsLeft=" + Arrays.toString(bitmapsLeft) +
                ", switchIndexes=" + Arrays.toString(switchIndexes) +
                ", currentTime=" + currentTime +
                ", maxIndex=" + maxIndex +
                '}';
    }

}
