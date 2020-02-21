package com.terrylinla.rnsketchcanvas.utils.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.terrylinla.rnsketchcanvas.utils.layers.Layer;

public class TapEntity extends MotionEntity {
    private int mSideLength;
    private float mBordersPadding;
    private float mStrokeWidth;
    private int mStrokeColor;

    private Paint mTapPaint;
    private Bitmap mTapBitmap;
    private Canvas mTapCanvas;

    public TapEntity(@NonNull Layer layer,
                          @IntRange(from = 1) int canvasWidth,
                          @IntRange(from = 1) int canvasHeight,
                          @IntRange(from = 1) int sideLength,
                          @Nullable Float bordersPadding,
                          @Nullable Float strokeWidth,
                          @Nullable Integer strokeColor) {
        super(layer, canvasWidth, canvasHeight);

        this.mSideLength = sideLength;
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
        configureTapBitmap(null);

        float width = this.mTapBitmap.getWidth();
        float height = this.mTapBitmap.getHeight();

        float widthAspect = 1.0F * canvasWidth / this.mTapBitmap.getWidth();
        float heightAspect = 1.0F * canvasHeight / this.mTapBitmap.getHeight();

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

    private void configureTapBitmap(@Nullable Paint paint) {
        updatePaint(paint);
        if (this.mTapBitmap == null) {
            this.mTapBitmap = Bitmap.createBitmap(getWidth()+(int)this.mBordersPadding, getHeight()+(int)this.mBordersPadding, Bitmap.Config.ARGB_8888);
            this.mTapCanvas = new Canvas(this.mTapBitmap);
        }
        this.mTapCanvas.save();
        this.mTapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        this.drawTap();
        this.mTapCanvas.restore();
    }

    private void drawTap() {
        int halfWidth = mSideLength / 2;
        int thirdWidth = mSideLength / 3;

        float centerX = getLayer().getX() + halfWidth;
        float centerY = getLayer().getY() + halfWidth;

        Path TapPath = new Path();
        TapPath.moveTo(centerX + mBordersPadding - halfWidth, getLayer().getY() + mBordersPadding); // Top Left
        TapPath.lineTo(centerX + mBordersPadding - halfWidth, centerY + thirdWidth); // Bottom Left
        TapPath.lineTo(centerX - mBordersPadding + halfWidth, getLayer().getY() + mBordersPadding); // Top Right
        TapPath.lineTo(centerX - mBordersPadding + halfWidth, centerY + thirdWidth); // Bottom Right
        TapPath.lineTo(centerX + mBordersPadding - halfWidth, getLayer().getY() + mBordersPadding); // Back to Top Left

        TapPath.close();
        this.mTapCanvas.drawPath(TapPath, mTapPaint);
    }

    private void updatePaint(@Nullable Paint paint) {
        if (paint != null && isSelected()) {
            this.mStrokeColor = paint.getColor();
            this.mStrokeWidth = paint.getStrokeWidth();
        }

        this.mTapPaint = new Paint();
        this.mTapPaint.setColor(this.mStrokeColor);
        this.mTapPaint.setStrokeWidth(this.mStrokeWidth / getLayer().getScale());

        // TODO: Tap Border gets pixelated because it's just done once (initially)!
        this.mTapPaint.setAntiAlias(true);

        // When scaling the TapShape the border gets pixelated, this helps a bit against it.
        // TODO: FIX THIS by somehow scaling the shape as well and not just the bitmap...
        this.mTapPaint.setFilterBitmap(true);
        this.mTapPaint.setDither(true);
        this.mTapPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void drawContent(@NonNull Canvas canvas, @Nullable Paint drawingPaint) {
        configureTapBitmap(drawingPaint);
        canvas.drawBitmap(this.mTapBitmap, matrix, this.mTapPaint);
    }

    @Override
    @NonNull
    public Layer getLayer() {
        return layer;
    }

    @Override
    public int getWidth() {
        return this.mSideLength;
    }

    @Override
    public int getHeight() {
        return this.mSideLength;
    }

    public void updateEntity() {
        updateEntity(true);
    }

    @Override
    public void release() {
        if (this.mTapBitmap != null && !this.mTapBitmap.isRecycled()) {
            this.mTapBitmap.recycle();
        }
    }
}
