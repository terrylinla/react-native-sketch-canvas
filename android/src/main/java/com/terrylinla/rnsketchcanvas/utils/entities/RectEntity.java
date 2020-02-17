package com.terrylinla.rnsketchcanvas.utils.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.terrylinla.rnsketchcanvas.utils.layers.Layer;

public class RectEntity extends MotionEntity {
    private int mRectWidth;
    private int mRectHeight;
    private float mBordersPadding;
    private float mStrokeWidth;
    private int mStrokeColor;

    private Paint mRectPaint;
    private Bitmap mRectBitmap;
    private Canvas mRectCanvas;

    public RectEntity(@NonNull Layer layer,
                        @IntRange(from = 1) int canvasWidth,
                        @IntRange(from = 1) int canvasHeight,
                        @IntRange(from = 1) int rectWidth,
                        @IntRange(from = 1) int rectHeight,
                        @Nullable Float bordersPadding,
                        @Nullable Float strokeWidth,
                        @Nullable Integer strokeColor) {
        super(layer, canvasWidth, canvasHeight);

        this.mRectWidth = rectWidth;
        this.mRectHeight = rectHeight;
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
        configureRectBitmap(null);

        float width = this.mRectBitmap.getWidth();
        float height = this.mRectBitmap.getHeight();

        float widthAspect = 1.0F * canvasWidth / this.mRectBitmap.getWidth();
        float heightAspect = 1.0F * canvasHeight / this.mRectBitmap.getHeight();

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

    private void configureRectBitmap(@Nullable Paint paint) {
        updatePaint(paint);
        if (this.mRectBitmap == null) {
            this.mRectBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            this.mRectCanvas = new Canvas(this.mRectBitmap);
        }
        this.mRectCanvas.save();
        this.mRectCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        this.mRectCanvas.drawRect(this.mBordersPadding, this.mBordersPadding, getWidth() - this.mBordersPadding, getHeight() - this.mBordersPadding, this.mRectPaint);
        this.mRectCanvas.restore();
    }

    private void updatePaint(@Nullable Paint paint) {
        if (paint != null && isSelected()) {
            this.mStrokeColor = paint.getColor();
            this.mStrokeWidth = paint.getStrokeWidth();
        }

        this.mRectPaint = new Paint();
        this.mRectPaint.setColor(this.mStrokeColor);
        if (getWidth() == getHeight()) {
            this.mRectPaint.setStrokeWidth(this.mStrokeWidth / getLayer().getScale());
        } else {
            // If we draw a rect the strokeWidth was scaled higher than it should've been as width != height on a rect...
            this.mRectPaint.setStrokeWidth((this.mStrokeWidth-2.5f) / getLayer().getScale());
        }

        // TODO: Rect Border gets pixelated because it's just done once (initially)!
        this.mRectPaint.setAntiAlias(true);

        // When scaling the RectShape the border gets pixelated, this helps a bit against it.
        // TODO: FIX THIS by somehow scaling the shape as well and not just the bitmap...
        this.mRectPaint.setFilterBitmap(true);
        this.mRectPaint.setDither(true);
        this.mRectPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void drawContent(@NonNull Canvas canvas, @Nullable Paint drawingPaint) {
        configureRectBitmap(drawingPaint);
        canvas.drawBitmap(this.mRectBitmap, matrix, this.mRectPaint);
    }

    @Override
    @NonNull
    public Layer getLayer() {
        return layer;
    }

    @Override
    public int getWidth() {
        return this.mRectWidth;
    }

    @Override
    public int getHeight() {
        return this.mRectHeight;
    }

    public void updateEntity() {
        updateEntity(true);
    }

    @Override
    public void release() {
        if (this.mRectBitmap != null && !this.mRectBitmap.isRecycled()) {
            this.mRectBitmap.recycle();
        }
    }
}
