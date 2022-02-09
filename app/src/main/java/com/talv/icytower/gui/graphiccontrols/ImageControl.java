package com.talv.icytower.gui.graphiccontrols;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.talv.icytower.game.Engine;
import com.talv.icytower.ImageHelper;

public class ImageControl extends Control {
    public Bitmap image;

    public ImageControl(boolean isEnabled, boolean isVisible, Rect rect, Bitmap image) {
        this.isEnabled = isEnabled;
        this.isVisible = isVisible;
        this.rect = rect;
        this.image = image;
    }

    public ImageControl(Rect rect, Bitmap image) {
        this(true, true, rect, image);
    }

    public ImageControl(boolean isEnabled, boolean isVisible, Rect rect, Resources resources, int resourceID, int width, int height) {
        this(isEnabled, isVisible, rect, ImageHelper.stretch(BitmapFactory.decodeResource(resources, resourceID), width, height, true));
    }

    public ImageControl(Rect rect, Resources resources, int resourceID, int width, int height) {
        this(rect, ImageHelper.stretch(BitmapFactory.decodeResource(resources, resourceID), width, height, true));
    }


    private static final float BTN_TEXT_PADDING_MULTIPLE = 0.05f;

    public static ImageControl buildBtn(Rect rect, String text, int backgroundColor, int textColor) {
        return new ImageControl(rect, createBtnImage(rect, text, backgroundColor, textColor));
    }

    public static ImageControl buildBtn(Rect rect, String text, int backgroundColor, int textColor, boolean enabled, boolean visible) {
        return new ImageControl(enabled, visible, rect, createBtnImage(rect, text, backgroundColor, textColor));
    }

    public static ImageControl buildDisabledBtn(Rect rect, String text, int backgroundColor, int textColor) {
        return new ImageControl(false, false, rect, createBtnImage(rect, text, backgroundColor, textColor));
    }

    private static Bitmap createBtnImage(Rect rect, String text, int backgroundColor, int textColor) {
        Bitmap image = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888, false);
        Canvas canvas = new Canvas(image);


        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, rect.width(), rect.height(), backgroundPaint);

        int horizontalPadding = (int) (rect.width() * BTN_TEXT_PADDING_MULTIPLE);

        int desiredTextWidth = rect.width() - horizontalPadding * 2;
        Paint textPaint = new Paint();

        // find fitting text size
        int textSize = 10;
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textSize = (int) (textSize * desiredTextWidth / textPaint.measureText(text));
        textPaint.setTextSize(textSize);

        textPaint.setColor(textColor);

        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);

        canvas.drawText(text,
                horizontalPadding + (desiredTextWidth - bounds.width()) / 2 - bounds.left,
                (rect.height() - bounds.height()) / 2 - bounds.top,
                textPaint);
        return image;
    }

    @Override
    public void render(Canvas canvas) {
        canvas.drawBitmap(image, rect.left, rect.top, Engine.gamePaint);
    }
}