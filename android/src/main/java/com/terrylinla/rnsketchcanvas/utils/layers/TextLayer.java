package com.terrylinla.rnsketchcanvas.utils.layers;

import com.facebook.react.uimanager.ThemedReactContext;

public class TextLayer extends Layer {

    private String mText;
    private Font mFont;
    private ThemedReactContext mContext;

    public TextLayer(ThemedReactContext context) {
        mContext = context;
    }

    @Override
    protected void reset() {
        super.reset();
        this.mText = "";
        this.mFont = new Font(mContext, null);
    }

    @Override
    protected float getMaxScale() {
        return Limits.MAX_SCALE;
    }

    @Override
    protected float getMinScale() {
        return Limits.MIN_SCALE;
    }

    @Override
    public float initialScale() {
        return Limits.INITIAL_SCALE;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
    }

    public Font getFont() {
        return mFont;
    }

    public void setFont(Font font) {
        this.mFont = font;
    }

    public interface Limits {
        /**
         * limit text size to view bounds
         * so that users don't put small font size and scale it 100+ times
         */
        float MAX_SCALE = 1.0F;
        float MIN_SCALE = 0.2F;

        float MIN_BITMAP_HEIGHT = 0.13F;

        float FONT_SIZE_STEP = 0.008F;

        float INITIAL_FONT_SIZE = 0.075F;
        int INITIAL_FONT_COLOR = 0xff000000;

        float INITIAL_SCALE = 0.8F; // set the same to avoid text scaling
    }
}