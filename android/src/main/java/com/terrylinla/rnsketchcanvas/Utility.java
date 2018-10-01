package com.terrylinla.rnsketchcanvas;

import android.util.Log;
import android.util.DisplayMetrics;
import android.graphics.RectF;

public final class Utility {
    public static RectF fillImage(float imgWidth, float imgHeight, float targetWidth, float targetHeight, String mode) {
        float imageAspectRatio = imgWidth / imgHeight;
        float targetAspectRatio = targetWidth / targetHeight;
        switch (mode) {
            case "AspectFill": {
                float scaleFactor = targetAspectRatio < imageAspectRatio ? targetHeight / imgHeight : targetWidth / imgWidth;
                float w = imgWidth * scaleFactor, h = imgHeight * scaleFactor;
                return new RectF((targetWidth - w) / 2, (targetHeight - h) / 2,
                    w + (targetWidth - w) / 2, h + (targetHeight - h) / 2);
            }
            case "AspectFit":
            default: {
                float scaleFactor = targetAspectRatio > imageAspectRatio ? targetHeight / imgHeight : targetWidth / imgWidth;
                float w = imgWidth * scaleFactor, h = imgHeight * scaleFactor;
                return new RectF((targetWidth - w) / 2, (targetHeight - h) / 2,
                    w + (targetWidth - w) / 2, h + (targetHeight - h) / 2);
            }
            case "ScaleToFill": {
                return  new RectF(0, 0, targetWidth, targetHeight);
            }
        }
    }

    public static int convertDpToPx(DisplayMetrics displayMetrics, float dp) {
        return (int) (dp * displayMetrics.density);
    }

    public static float convertDpToPxAsFloat(DisplayMetrics displayMetrics, float dp) {
      return dp * displayMetrics.density;
    }

    public static int convertPxToDp(DisplayMetrics displayMetrics, float px) {
        return (int) (px / displayMetrics.density);
    }

    public static float convertPxToDpAsFloat(DisplayMetrics displayMetrics, float px) {
        return px / displayMetrics.density;
    }
}
