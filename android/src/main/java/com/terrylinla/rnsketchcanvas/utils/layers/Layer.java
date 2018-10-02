package com.terrylinla.rnsketchcanvas.utils.layers;

import android.support.annotation.FloatRange;

public class Layer {

    /**
     * rotation relative to the layer center, in degrees
     */
    @FloatRange(from = 0.0F, to = 360.0F)
    private float mRotationInDegrees;

    private float mScale;
    /**
     * top left X coordinate, relative to parent canvas
     */
    private float x;
    /**
     * top left Y coordinate, relative to parent canvas
     */
    private float y;
    /**
     * is layer flipped horizontally (by X-coordinate)
     */
    private boolean mIsFlipped;

    public Layer() {
        reset();
    }

    protected void reset() {
        this.mRotationInDegrees = 0.0F;
        this.mScale = 1.0F;
        this.mIsFlipped = false;
        this.x = 0.0F;
        this.y = 0.0F;
    }

    public void postScale(float scaleDiff) {
        float newVal = mScale + scaleDiff;
        if (newVal >= getMinScale() && newVal <= getMaxScale()) {
            mScale = newVal;
        }
    }

    protected float getMaxScale() {
        return Limits.MAX_SCALE;
    }

    protected float getMinScale() {
        return Limits.MIN_SCALE;
    }

    public void postRotate(float rotationInDegreesDiff) {
        this.mRotationInDegrees += rotationInDegreesDiff;
        this.mRotationInDegrees %= 360.0F;
    }

    public void postTranslate(float dx, float dy) {
        this.x += dx;
        this.y += dy;
    }

    public void flip() {
        this.mIsFlipped = !mIsFlipped;
    }

    public float initialScale() {
        return Limits.INITIAL_ENTITY_SCALE;
    }

    public float getRotationInDegrees() {
        return mRotationInDegrees;
    }

    public void setRotationInDegrees(@FloatRange(from = 0.0, to = 360.0) float rotationInDegrees) {
        this.mRotationInDegrees = rotationInDegrees;
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale) {
        this.mScale = scale;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public boolean isFlipped() {
        return mIsFlipped;
    }

    public void setFlipped(boolean flipped) {
        mIsFlipped = flipped;
    }

    interface Limits {
        float MIN_SCALE = 0.06F;
        float MAX_SCALE = 4.0F;
        float INITIAL_ENTITY_SCALE = 0.4F;
    }
}