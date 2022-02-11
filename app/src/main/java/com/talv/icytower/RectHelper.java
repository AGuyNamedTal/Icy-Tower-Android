package com.talv.icytower;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.talv.icytower.game.Debug;
import com.talv.icytower.game.PlayerPlatformsIntersection;

public class RectHelper {

    public static void setRectX(Rect rect, int x) {
        rect.set(x, rect.top, x + rect.width(), rect.bottom);
    }

    public static void setRectY(Rect rect, int y) {
        if (Debug.LOG_JUMP)
            Log.d("jump", "Y = " + y);
        rect.set(rect.left, y, rect.right, y + rect.height());
    }

    public static void setRectPos(Rect rect, int x, int y) {
        if (Debug.LOG_JUMP)
            Log.d("jump", "Y = " + y);
        rect.set(x, y, x + rect.width(), y + rect.height());
    }


    public static boolean doRectsIntersect(Rect rect1, Rect rect2) {
        return rect2.left < rect1.right && rect2.right > rect1.left && rect2.top < rect1.bottom && rect2.bottom > rect1.top;
    }

    public static boolean isRectBelowRect(Rect above, Rect below) {
        return below.left <= above.right && below.right >= above.left && below.top - above.bottom <= 0;
    }

    public static boolean isRectOnRect(Rect above, Rect below) {
        return below.left <= above.right && below.right >= above.left && below.top == above.bottom;
    }

    public static boolean isPointInRect(Rect rect, Point point) {
        return point.x >= rect.left && point.x <= rect.right && point.y >= rect.top && point.y <= rect.bottom;
    }

    public static boolean isPointInRect(Rect rect, int x, int y) {
        return x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom;
    }

    public static Rect rectFromWidthHeight(int x, int y, int width, int height) {
        return new Rect(x, y, x + width, y + height);
    }

    public static PlayerPlatformsIntersection doesPlatformIntersectWithMovementY(Rect oldRect, int newY, Rect platform) {
        // check X intersection
        if (platform.left >= oldRect.right || platform.right <= oldRect.left) {
            return new PlayerPlatformsIntersection(newY);
        }

        boolean moveDown = newY > oldRect.top;
        if (moveDown) {
            int newBottom = newY + oldRect.height();
            if (oldRect.bottom <= platform.top && newBottom >= platform.top) {
                return new PlayerPlatformsIntersection(platform.top - oldRect.height(), true);
            }
        } else {
            // move up
            if (oldRect.top >= platform.bottom && newY <= platform.bottom) {
                return new PlayerPlatformsIntersection(platform.bottom, true);
            }
        }
        return new PlayerPlatformsIntersection(newY);
    }
}
