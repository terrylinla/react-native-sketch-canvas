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
import android.util.Log;

import com.terrylinla.utils.CircleLayer;
import com.terrylinla.utils.MotionEntity;

public class CircleEntity extends MotionEntity {
    private Paint mCirclePaint;
    private float mCircleRadius;
    private float mBordersPadding;
    private float mStrokeWidth;
    private int mStrokeColor;

    private Bitmap mCircleBitmap;
    private Canvas mCircleCanvas;

    public CircleEntity(@NonNull CircleLayer layer,
                        @IntRange(from = 1) int canvasWidth,
                        @IntRange(from = 1) int canvasHeight, int circleRadius, @Nullable Float bordersPadding, @Nullable Float strokeWidth, @Nullable Integer strokeColor) {
        super(layer, canvasWidth, canvasHeight);

        this.mCircleRadius = circleRadius;
        this.mStrokeWidth = 5;
        this.mBordersPadding = 10;
        this.mStrokeColor = Color.BLACK;

        if (bordersPadding != null) {
            this.mBordersPadding = bordersPadding;
        }
        if (strokeWidth != null) {
            this.mStrokeWidth = strokeWidth;
        }
        if (strokeColor != null) {
            this.mStrokeColor = strokeColor;
        }

        updateEntity(false);
    }

    private void updateEntity(boolean moveToPreviousCenter) {
        configureCircleBitmap(null);

        float width = this.mCircleBitmap.getWidth();
        float height = this.mCircleBitmap.getHeight();

        float widthAspect = 1.0F * canvasWidth / this.mCircleBitmap.getWidth();
        float heightAspect = 1.0F * canvasHeight / this.mCircleBitmap.getHeight();

        // fit the smallest size
        holyScale = Math.min(widthAspect, heightAspect);

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
            moveCenterTo(absoluteCenter());
        }
    }

    private void configureCircleBitmap(@Nullable Paint paint) {
        updatePaint(paint);
        if (this.mCircleBitmap == null) {
            this.mCircleBitmap = Bitmap.createBitmap(getWidth()+(int)this.mBordersPadding, getHeight()+(int)this.mBordersPadding, Bitmap.Config.ARGB_8888);
            this.mCircleCanvas = new Canvas(this.mCircleBitmap);
        }
        this.mCircleCanvas.save();
        this.mCircleCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        this.mCircleCanvas.drawCircle(getLayer().getX() + this.mCircleRadius+this.mBordersPadding/2, getLayer().getY() + this.mCircleRadius+this.mBordersPadding/2, this.mCircleRadius-this.mBordersPadding, this.mCirclePaint);
        this.mCircleCanvas.restore();
    }

    private void updatePaint(@Nullable Paint paint) {
        if (paint != null && isSelected()) {
            this.mStrokeColor = paint.getColor();
            this.mStrokeWidth = paint.getStrokeWidth();
        }
        
        this.mCirclePaint = new Paint();
        this.mCirclePaint.setColor(this.mStrokeColor);
        this.mCirclePaint.setStrokeWidth(this.mStrokeWidth / getLayer().getScale());

        // TODO: Circle Border gets pixelated because it's just done once (initially)!
        this.mCirclePaint.setAntiAlias(true);

        // When scaling the CircleShape the border gets pixelated, this helps a bit against it.
        // TODO: FIX THIS by somehow scaling the shape as well and not just the bitmap...
        this.mCirclePaint.setFilterBitmap(true);
        this.mCirclePaint.setDither(true);
        this.mCirclePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void drawContent(@NonNull Canvas canvas, @Nullable Paint drawingPaint) {
        configureCircleBitmap(drawingPaint);
        canvas.drawBitmap(this.mCircleBitmap, matrix, this.mCirclePaint);
    }

    @Override
    @NonNull
    public CircleLayer getLayer() {
        return (CircleLayer) layer;
    }

    @Override
    public int getWidth() {
        return (int) this.mCircleRadius * 2;
    }

    @Override
    public int getHeight() {
        return (int) this.mCircleRadius * 2;
    }

    public void updateEntity() {
        updateEntity(true);
    }

    @Override
    public void release() {
        if (this.mCircleBitmap != null && !this.mCircleBitmap.isRecycled()) {
            this.mCircleBitmap.recycle();
        }
    }

}
