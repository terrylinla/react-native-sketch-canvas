package com.terrylinla.utils.shapes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.terrylinla.utils.CircleLayer;
import com.terrylinla.utils.MotionEntity;

public class CircleEntity extends MotionEntity {
    private final Paint mCirclePaint;
    private float mCircleRadius;

    private Bitmap mCircleBitmap;
    private Canvas mCircleCanvas;

    public CircleEntity(@NonNull CircleLayer layer,
                        @IntRange(from = 1) int canvasWidth,
                        @IntRange(from = 1) int canvasHeight, int circleRadius) {
        super(layer, canvasWidth, canvasHeight);

        this.mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mCirclePaint.setFilterBitmap(true);
        this.mCirclePaint.setDither(true);
        this.mCirclePaint.setStyle(Paint.Style.STROKE);
        this.mCirclePaint.setStrokeWidth(3);
        this.mCircleRadius = circleRadius;

        updateEntity(false);
    }

    private void updateEntity(boolean moveToPreviousCenter) {

        PointF oldCenter = absoluteCenter();

        createCircleBitmap();

        float width = mCircleBitmap.getWidth();
        float height = mCircleBitmap.getHeight();

        float widthAspect = 1.0F * canvasWidth / mCircleBitmap.getWidth();
        float heightAspect = 1.0F * canvasHeight / mCircleBitmap.getHeight();

        // fit the smallest size
        this.holyScale = Math.min(widthAspect, heightAspect);

        // initial position of the entity
        srcPoints[0] = 0;
        srcPoints[1] = 0;
        srcPoints[2] = width;
        srcPoints[3] = 0;
        srcPoints[4] = width;
        srcPoints[5] = height;
        srcPoints[6] = 0;
        srcPoints[7] = height;
        srcPoints[8] = 0;
        srcPoints[8] = 0;

        if (moveToPreviousCenter) {
            // move to previous center
            moveCenterTo(oldCenter);
        }
    }

    private void createCircleBitmap() {
        if (mCircleBitmap == null) {
            mCircleBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mCircleCanvas = new Canvas(mCircleBitmap);
        }
        mCirclePaint.setStrokeWidth(3 / getLayer().getScale());
        mCircleCanvas.save();
        mCircleCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mCircleCanvas.drawCircle(getLayer().getX() + mCircleRadius, getLayer().getY() + mCircleRadius, mCircleRadius, mCirclePaint);
        mCircleCanvas.restore();
    }

    @Override
    protected void drawContent(@NonNull Canvas canvas, @Nullable Paint drawingPaint) {
        createCircleBitmap();
        if (drawingPaint != null) {
            canvas.drawBitmap(mCircleBitmap, matrix, drawingPaint);
        } else {
            canvas.drawBitmap(mCircleBitmap, matrix, mCirclePaint);
        }
    }

    @Override
    @NonNull
    public CircleLayer getLayer() {
        return (CircleLayer) layer;
    }

    @Override
    public int getWidth() {
        return (int) mCircleRadius * 2;
    }

    @Override
    public int getHeight() {
        return (int) mCircleRadius * 2;
    }

    public void updateEntity() {
        updateEntity(true);
    }

    @Override
    public void release() {
        if (mCircleBitmap != null && !mCircleBitmap.isRecycled()) {
            mCircleBitmap.recycle();
        }
    }

}
