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

    private Paint mPaint;

    public static PointF midPoint(PointF p1, PointF p2) {
        return new PointF((p1.x + p2.x) * 0.5f, (p1.y + p2.y) * 0.5f);
    }

    public SketchData(int id, int strokeColor, float strokeWidth) {
        this.id = id;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
    }

    public SketchData(int id, int strokeColor, float strokeWidth, ArrayList<PointF> points) {
        this.id = id;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.points.addAll(points);
    }

    public Rect addPoint(PointF p) {
        points.add(p);

        RectF updateRect;

        int pointsCount = points.size();
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
        int pointsCount = points.size();
        for (int i = 0; i < pointsCount; i++) {
            draw(canvas, i);
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
            drawQuadCurve(canvas, prevMid, b, currentMid);
        } else if (pointsCount >= 2 && pointIndex >= 1) {
            PointF a = points.get(pointIndex - 1);
            PointF b = points.get(pointIndex);
            PointF mid = midPoint(a, b);

            // Draw a line to the middle of points a and b
            // This is so the next draw which uses a curve looks correct and continues from there
            drawLine(canvas, a, mid);
        } else if (pointsCount >= 1) {
            PointF a = points.get(pointIndex);

            // Draw a single point
            drawPoint(canvas, a);
        }
    }

    private void drawPoint(Canvas canvas, PointF point) {
        canvas.drawPoint(point.x, point.y, getPaint());
    }

    private void drawLine(Canvas canvas, PointF fromPoint, PointF toPoint) {
        canvas.drawLine(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y, getPaint());
    }

    private void drawQuadCurve(Canvas canvas, PointF startPoint, PointF controlPoint, PointF endPoint) {
        Path path = new Path();
        path.moveTo(startPoint.x, startPoint.y);
        path.quadTo(controlPoint.x, controlPoint.y, endPoint.x, endPoint.y);

        canvas.drawPath(path, getPaint());
    }
}
