package com.terrylinla.rnsketchcanvas.utils.layers;

public class CircleLayer extends Layer {
    private float mCircleRadius;

    public CircleLayer() {
    }

    @Override
    protected void reset() {
        super.reset();
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
        return Limits.INITIAL_ENTITY_SCALE;
    }

    public interface Limits {
        float MIN_SCALE = 0.06F;
        float MAX_SCALE = 4.0F;
        float INITIAL_ENTITY_SCALE = 0.2F;
    }
}