package com.terrylinla.rnsketchcanvas;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.ArrayList;

public class SketchData {
    public final ArrayList<PointF> points = new ArrayList<PointF>();
    public final int id, strokeColor;
    public final float strokeWidth;
    public final boolean isTranslucent;

    private Paint mPaint;
    private Path mPath;
    private RectF mDirty = null;

    public static PointF midPoint(PointF p1, PointF p2) {
        return new PointF((p1.x + p2.x) * 0.5f, (p1.y + p2.y) * 0.5f);
    }

    public SketchData(int id, int strokeColor, float strokeWidth) {
        this.id = id;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.isTranslucent = ((strokeColor >> 24) & 0xff) != 255 && strokeColor != Color.TRANSPARENT;
        mPath = this.isTranslucent ? new Path() : null;
    }

    public SketchData(int id, int strokeColor, float strokeWidth, ArrayList<PointF> points) {
        this.id = id;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.points.addAll(points);
        this.isTranslucent = ((strokeColor >> 24) & 0xff) != 255 && strokeColor != Color.TRANSPARENT;
        mPath = this.isTranslucent ? evaluatePath() : null;
    }

    public Rect addPoint(PointF p) {
        points.add(p);

        RectF updateRect;

        int pointsCount = points.size();

        if (this.isTranslucent) {
            if (pointsCount >= 3) {
                addPointToPath(mPath, 
                    this.points.get(pointsCount - 3), 
                    this.points.get(pointsCount - 2),
                    p);
            } else if (pointsCount >= 2) {
                addPointToPath(mPath, this.points.get(0), this.points.get(0), p);
            } else {
                addPointToPath(mPath, p, p, p);
            }

            float x = p.x, y = p.y;
            if (mDirty == null) {
                mDirty = new RectF(x, y, x + 1, y + 1);
                updateRect = new RectF(x - this.strokeWidth, y - this.strokeWidth, 
                    x + this.strokeWidth, y + this.strokeWidth);
            } else {
                mDirty.union(x, y);
                updateRect = new RectF(
                                    mDirty.left - this.strokeWidth, mDirty.top - this.strokeWidth,
                                    mDirty.right + this.strokeWidth, mDirty.bottom + this.strokeWidth
                                    );
            }
        } else {
            if (pointsCount >= 3) {
                PointF a = points.get(pointsCount - 3);
                PointF b = points.get(pointsCount - 2);
                PointF c = p;
                PointF prevMid = midPoint(a, b);
                PointF currentMid = midPoint(b, c);

                updateRect = new RectF(prevMid.x, prevMid.y, prevMid.x, prevMid.y);
                updateRect.union(b.x, b.y);
                updateRect.union(currentMid.x, currentMid.y);
            } else if (pointsCount >= 2) {
                PointF a = points.get(pointsCount - 2);
                PointF b = p;
                PointF mid = midPoint(a, b);

                updateRect = new RectF(a.x, a.y, a.x, a.y);
                updateRect.union(mid.x, mid.y);
            } else {
                updateRect = new RectF(p.x, p.y, p.x, p.y);
            }

            updateRect.inset(-strokeWidth * 2, -strokeWidth * 2);

        }
        Rect integralRect = new Rect();
        updateRect.roundOut(integralRect);
        
        return integralRect;
    }

    public void drawLastPoint(Canvas canvas) {
        int pointsCount = points.size();
        if (pointsCount < 1) {
            return;
        }

        draw(canvas, pointsCount - 1);
    }

    public void draw(Canvas canvas) {
        if (this.isTranslucent) {
            canvas.drawPath(mPath, getPaint());
        } else {
            int pointsCount = points.size();
            for (int i = 0; i < pointsCount; i++) {
                draw(canvas, i);
            }
        }
    }

    private Paint getPaint() {
        if (mPaint == null) {
            boolean isErase = strokeColor == Color.TRANSPARENT;

            mPaint = new Paint();
            mPaint.setColor(strokeColor);
            mPaint.setStrokeWidth(strokeWidth);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setAntiAlias(true);
            mPaint.setXfermode(new PorterDuffXfermode(isErase ? PorterDuff.Mode.CLEAR : PorterDuff.Mode.SRC_OVER));
        }
        return mPaint;
    }

    private void draw(Canvas canvas, int pointIndex) {
        int pointsCount = points.size();
        if (pointIndex >= pointsCount) {
            return;
        }

        if (pointsCount >= 3 && pointIndex >= 2) {
            PointF a = points.get(pointIndex - 2);
            PointF b = points.get(pointIndex - 1);
            PointF c = points.get(pointIndex);
            PointF prevMid = midPoint(a, b);
            PointF currentMid = midPoint(b, c);

            // Draw a curve
            Path path = new Path();
            path.moveTo(prevMid.x, prevMid.y);
            path.quadTo(b.x, b.y, currentMid.x, currentMid.y);

            canvas.drawPath(path, getPaint());
        } else if (pointsCount >= 2 && pointIndex >= 1) {
            PointF a = points.get(pointIndex - 1);
            PointF b = points.get(pointIndex);
            PointF mid = midPoint(a, b);

            // Draw a line to the middle of points a and b
            // This is so the next draw which uses a curve looks correct and continues from there
            canvas.drawLine(a.x, a.y, mid.x, mid.y, getPaint());
        } else if (pointsCount >= 1) {
            PointF a = points.get(pointIndex);

            // Draw a single point
            canvas.drawPoint(a.x, a.y, getPaint());
        }
    }

    private Path evaluatePath() {
        int pointsCount = points.size();
        Path path = new Path();

        for(int pointIndex=0; pointIndex<pointsCount; pointIndex++) {
            if (pointsCount >= 3 && pointIndex >= 2) {
                PointF a = points.get(pointIndex - 2);
                PointF b = points.get(pointIndex - 1);
                PointF c = points.get(pointIndex);
                PointF prevMid = midPoint(a, b);
                PointF currentMid = midPoint(b, c);
                
                // Draw a curve
                path.moveTo(prevMid.x, prevMid.y);
                path.quadTo(b.x, b.y, currentMid.x, currentMid.y);
            } else if (pointsCount >= 2 && pointIndex >= 1) {
                PointF a = points.get(pointIndex - 1);
                PointF b = points.get(pointIndex);
                PointF mid = midPoint(a, b);
                
                // Draw a line to the middle of points a and b
                // This is so the next draw which uses a curve looks correct and continues from there
                path.moveTo(a.x, a.y);
                path.lineTo(mid.x, mid.y);
            } else if (pointsCount >= 1) {
                PointF a = points.get(pointIndex);
                
                // Draw a single point
                path.moveTo(a.x, a.y);
                path.lineTo(a.x, a.y);
            }
        }
        return path;
    }

    private void addPointToPath(Path path, PointF tPoint, PointF pPoint, PointF point) {
        PointF mid1 = new PointF((pPoint.x + tPoint.x) * 0.5f, (pPoint.y + tPoint.y) * 0.5f);
        PointF mid2 = new PointF((point.x + pPoint.x) * 0.5f, (point.y + pPoint.y) * 0.5f);
        path.moveTo(mid1.x, mid1.y);
        path.quadTo(pPoint.x, pPoint.y, mid2.x, mid2.y);
    }
}
