package com.terrylinla.rnsketchcanvas.utils.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.terrylinla.rnsketchcanvas.utils.layers.Layer;

public class TriangleEntity extends MotionEntity {
    private int mSideLength;
    private float mBordersPadding;
    private float mStrokeWidth;
    private int mStrokeColor;

    private Paint mTrianglePaint;
    private Bitmap mTriangleBitmap;
    private Canvas mTriangleCanvas;

    public TriangleEntity(@NonNull Layer layer,
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
        configureTriangleBitmap(null);

        float width = this.mTriangleBitmap.getWidth();
        float height = this.mTriangleBitmap.getHeight();

        float widthAspect = 1.0F * canvasWidth / this.mTriangleBitmap.getWidth();
        float heightAspect = 1.0F * canvasHeight / this.mTriangleBitmap.getHeight();

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

    private void configureTriangleBitmap(@Nullable Paint paint) {
        updatePaint(paint);
        if (this.mTriangleBitmap == null) {
            this.mTriangleBitmap = Bitmap.createBitmap(getWidth()+(int)this.mBordersPadding, getHeight()+(int)this.mBordersPadding, Bitmap.Config.ARGB_8888);
            this.mTriangleCanvas = new Canvas(this.mTriangleBitmap);
        }
        this.mTriangleCanvas.save();
        this.mTriangleCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        this.drawTriangle();
        this.mTriangleCanvas.restore();
    }

    private void drawTriangle() {
        int halfWidth = mSideLength / 2;

        float centerX = getLayer().getX() + halfWidth;
        float centerY = getLayer().getY() + halfWidth;
        
        Path trianglePath = new Path();
        trianglePath.moveTo(centerX, getLayer().getY() + mBordersPadding); // Top
        trianglePath.lineTo(centerX + mBordersPadding - halfWidth, centerY + halfWidth); // Bottom Left
        trianglePath.lineTo(centerX - mBordersPadding + halfWidth, centerY + halfWidth); // Bottom Right
        trianglePath.lineTo(centerX, getLayer().getY() + mBordersPadding); // Back to Top
        
        trianglePath.close();
        this.mTriangleCanvas.drawPath(trianglePath, mTrianglePaint);
    }

    private void updatePaint(@Nullable Paint paint) {
        if (paint != null && isSelected()) {
            this.mStrokeColor = paint.getColor();
            this.mStrokeWidth = paint.getStrokeWidth();
        }
        
        this.mTrianglePaint = new Paint();
        this.mTrianglePaint.setColor(this.mStrokeColor);
        this.mTrianglePaint.setStrokeWidth(this.mStrokeWidth / getLayer().getScale());

        // TODO: Triangle Border gets pixelated because it's just done once (initially)!
        this.mTrianglePaint.setAntiAlias(true);

        // When scaling the TriangleShape the border gets pixelated, this helps a bit against it.
        // TODO: FIX THIS by somehow scaling the shape as well and not just the bitmap...
        this.mTrianglePaint.setFilterBitmap(true);
        this.mTrianglePaint.setDither(true);
        this.mTrianglePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void drawContent(@NonNull Canvas canvas, @Nullable Paint drawingPaint) {
        configureTriangleBitmap(drawingPaint);
        canvas.drawBitmap(this.mTriangleBitmap, matrix, this.mTrianglePaint);
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
        if (this.mTriangleBitmap != null && !this.mTriangleBitmap.isRecycled()) {
            this.mTriangleBitmap.recycle();
        }
    }
}