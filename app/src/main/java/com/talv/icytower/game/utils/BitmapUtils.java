package com.talv.icytower.game.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import java.util.ArrayList;

public class BitmapUtils {

    public static Bitmap stretch(Bitmap bitmap, int newWidth, int newHeight, boolean dispose) {
        Bitmap output = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        if (dispose) {
            bitmap.recycle();
        }
        return output;
    }

    public static Bitmap stretch(Bitmap bitmap, float multipleFactor, boolean dispose) {
        int newWidth = (int) (bitmap.getWidth() * multipleFactor);
        int newHeight = (int) (bitmap.getHeight() * multipleFactor);
        return stretch(bitmap, newWidth, newHeight, dispose);
    }


    public static Bitmap[] stretch(Bitmap[] bitmaps, float multipleFactor, boolean dispose) {
        Bitmap[] stretched = new Bitmap[bitmaps.length];
        for (int i = 0; i < stretched.length; i++) {
            stretched[i] = stretch(bitmaps[i], multipleFactor, dispose);
        }
        return stretched;
    }

    public static Bitmap stretchToHeight(Bitmap bitmap, int height, boolean dispose) {
        return stretch(bitmap, bitmap.getWidth(), height, dispose);
    }

    public static Bitmap flipX(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap[] flipX(Bitmap[] bitmaps) {
        Bitmap[] bitmapsFlip = new Bitmap[bitmaps.length];
        for (int i = 0; i < bitmaps.length; i++) {
            bitmapsFlip[i] = flipX(bitmaps[i]);
        }
        return bitmapsFlip;
    }

    public static Bitmap[] decodeAnimations(Bitmap bitmap, boolean dispose) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        ArrayList<Bitmap> output = new ArrayList<>();
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int startingX = -1;
        for (int x = 0; x < width; x++) {
            boolean emptyLine = true;
            for (int y = 0; y < height; y++) {
                int index = y * width + x;
                if (pixels[index] != 0) {
                    emptyLine = false;
                    break;
                }
            }
            if (emptyLine && startingX != -1) {
                int endX = x;
                output.add(Bitmap.createBitmap(bitmap, startingX, 0, endX - startingX - 1, height));
                startingX = -1;
            } else if (!emptyLine && startingX == -1) {
                startingX = x;
            }
        }

        if (dispose) {
            bitmap.recycle();
        }
        return output.toArray(new Bitmap[0]);
    }

    public static Bitmap tileImageX(Bitmap bitmap, int newWidth, int newHeight, boolean dispose) {
        int oldWidth = bitmap.getWidth();
        int oldHeight = bitmap.getHeight();
        Bitmap bitmapStretched;
        if (oldHeight == newHeight) {
            bitmapStretched = bitmap;
        } else {
            bitmapStretched = stretch(bitmap, oldWidth, newHeight, false);
        }
        Bitmap newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888, true);
        // use new bitmaps graphics
        Canvas canvas = new Canvas(newBitmap);
        int left = 0;
        while (left < newWidth) {
            canvas.drawBitmap(bitmapStretched, left, 0, null);
            left += oldWidth;
        }
        if (dispose) {
            bitmap.recycle();
        }
        if (bitmapStretched != bitmap) {
            bitmapStretched.recycle();
        }
        return newBitmap;
    }


    public static Bitmap reflectBitmap(Bitmap bitmap, boolean dispose) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix reflectionMatrix = new Matrix();
        reflectionMatrix.preRotate(180);
        reflectionMatrix.postTranslate(width, height);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, reflectionMatrix, true);
        if (dispose) {
            bitmap.recycle();
        }
        return newBitmap;
    }

    public static int measureLinesWithPixels(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int linesFilled = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (pixels[index] != 0) {
                    linesFilled++;
                    break;
                }
            }
        }
        return linesFilled;
    }


}
