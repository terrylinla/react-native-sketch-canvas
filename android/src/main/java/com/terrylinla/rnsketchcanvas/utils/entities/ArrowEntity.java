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

public class ArrowEntity extends MotionEntity {
    private int mWidth;
    private int mHeight;
    private float mBordersPadding;
    private float mStrokeWidth;
    private int mStrokeColor;

    private Paint mArrowPaint;
    private Bitmap mArrowBitmap;
    private Canvas mArrowCanvas;

    public ArrowEntity(@NonNull Layer layer,
                        @IntRange(from = 1) int canvasWidth,
                        @IntRange(from = 1) int canvasHeight,
                        @IntRange(from = 1) int width,
                        @IntRange(from = 1) int height,
                        @Nullable Float bordersPadding,
                        @Nullable Float strokeWidth,
                        @Nullable Integer strokeColor) {
        super(layer, canvasWidth, canvasHeight);

        this.mWidth = width;
        this.mHeight = height;
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
        configureArrowBitmap(null);

        float width = this.mArrowBitmap.getWidth();
        float height = this.mArrowBitmap.getHeight();

        float widthAspect = 1.0F * canvasWidth / this.mArrowBitmap.getWidth();
        float heightAspect = 1.0F * canvasHeight / this.mArrowBitmap.getHeight();

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

    private void configureArrowBitmap(@Nullable Paint paint) {
        updatePaint(paint);
        if (this.mArrowBitmap == null) {
            this.mArrowBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            this.mArrowCanvas = new Canvas(this.mArrowBitmap);
        }
        this.mArrowCanvas.save();
        this.mArrowCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        this.drawArrow();
        this.mArrowCanvas.restore();
    }

    private void drawArrow() {
        int halfWidth = mWidth / 2;
        int oneThirdWidth = mWidth / 3;
        int halfHeight = mHeight / 2;
        int oneThirdHeight = mHeight / 3;

        float centerX = getLayer().getX() + halfWidth;
        float centerY = getLayer().getY() + halfHeight;

        Path arrowPath = new Path();

        // Arrow with adjacents to centerY
        // arrowPath.moveTo(centerX, centerY + halfHeight - mBordersPadding); // Start at bottom center
        // arrowPath.lineTo(centerX, getLayer().getY() + mBordersPadding); // Draw -- from bottom up
        // arrowPath.lineTo(centerX - halfWidth + mBordersPadding, centerY); // Draw left adjacent from top
        // arrowPath.lineTo(centerX, getLayer().getY() + mBordersPadding); // Go Back to top
        // arrowPath.lineTo(centerX + halfWidth - mBordersPadding, centerY); // Draw right adjacent from top

        // Arrow with adjacents to Y + 1/3 of the height
        arrowPath.moveTo(centerX, centerY + halfHeight - mBordersPadding); // Start at bottom center
        arrowPath.lineTo(centerX, getLayer().getY() + mBordersPadding); // Draw -- from bottom up
        arrowPath.lineTo(centerX - oneThirdWidth + mBordersPadding, getLayer().getY() + oneThirdHeight); // Draw left adjacent from top
        arrowPath.lineTo(centerX, getLayer().getY() + mBordersPadding); // Go Back to top
        arrowPath.lineTo(centerX + oneThirdWidth - mBordersPadding, getLayer().getY() + oneThirdHeight); // Draw right adjacent from top

        this.mArrowCanvas.drawPath(arrowPath, mArrowPaint);
    }

    private void updatePaint(@Nullable Paint paint) {
        if (paint != null && isSelected()) {
            this.mStrokeColor = paint.getColor();
            this.mStrokeWidth = paint.getStrokeWidth();
        }

        this.mArrowPaint = new Paint();
        this.mArrowPaint.setColor(this.mStrokeColor);
        this.mArrowPaint.setStrokeWidth(this.mStrokeWidth / getLayer().getScale());

        // This is essential for the overlapping paths to not result in a weird artefact
        this.mArrowPaint.setStrokeJoin(Paint.Join.BEVEL);

        // TODO: Arrow Border gets pixelated because it's just done once (initially)!
        this.mArrowPaint.setAntiAlias(true);

        // When scaling the ArrowShape the border gets pixelated, this helps a bit against it.
        // TODO: FIX THIS by somehow scaling the shape as well and not just the bitmap...
        this.mArrowPaint.setFilterBitmap(true);
        this.mArrowPaint.setDither(true);
        this.mArrowPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void drawContent(@NonNull Canvas canvas, @Nullable Paint drawingPaint) {
        configureArrowBitmap(drawingPaint);
        canvas.drawBitmap(this.mArrowBitmap, matrix, this.mArrowPaint);
    }

    @Override
    @NonNull
    public Layer getLayer() {
        return layer;
    }

    @Override
    public int getWidth() {
        return this.mWidth;
    }

    @Override
    public int getHeight() {
        return this.mHeight;
    }

    public void updateEntity() {
        updateEntity(true);
    }

    @Override
    public void release() {
        if (this.mArrowBitmap != null && !this.mArrowBitmap.isRecycled()) {
            this.mArrowBitmap.recycle();
        }
    }
}
