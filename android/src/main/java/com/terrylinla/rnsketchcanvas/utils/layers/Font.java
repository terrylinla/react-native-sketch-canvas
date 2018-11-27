package com.terrylinla.rnsketchcanvas.utils.layers;

import com.facebook.react.uimanager.ThemedReactContext;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.graphics.Typeface;

public class Font {
    private int color;
    private float size;
    private Typeface typeface;

    public Font(ThemedReactContext context, @Nullable String typefaceName) {
        if (TextUtils.isEmpty(typefaceName)) {
            typeface = Typeface.DEFAULT;
        } else {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), typefaceName);
            } catch(Exception ex) {
                typeface = Typeface.create(typefaceName, Typeface.NORMAL);
            }
        }
    }

    public void increaseSize(float diff) {
        size = size + diff;
    }

    public void decreaseSize(float diff) {
        if (size - diff >= Limits.MIN_FONT_SIZE) {
            size = size - diff;
        }
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Typeface getTypeface() {
        return typeface;
    }

    public void setTypeface(Typeface typeface) {
        this.typeface = typeface;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    private interface Limits {
        float MIN_FONT_SIZE = 0.01F;
    }
}