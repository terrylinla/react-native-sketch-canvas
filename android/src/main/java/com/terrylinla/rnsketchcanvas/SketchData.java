package com.terrylinla.rnsketchcanvas;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

        RectF updateRect = new RectF(p.x, p.y, p.x, p.y);
        if (points.size() > 1) {
            PointF prevPoint = points.get(points.size() - 2);
            updateRect.union(prevPoint.x, prevPoint.y);
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
        } else if (pointsCount < 2) {
            drawPoint(canvas, points.get(0));
            return;
        }

        PointF fromPoint = points.get(pointsCount - 2);
        PointF toPoint = points.get(pointsCount - 1);

        drawLine(canvas, fromPoint, toPoint);
    }

    public void draw(Canvas canvas) {
        if (points.size() == 1) {
            drawPoint(canvas, points.get(0));
            return;
        }

        PointF prevPoint = null;
        for (PointF point: points) {
            if (prevPoint == null) {
                prevPoint = point;
                continue;
            }

            drawLine(canvas, prevPoint, point);
            prevPoint = point;
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

    private void drawPoint(Canvas canvas, PointF point) {
        canvas.drawPoint(point.x, point.y, getPaint());
    }

    private void drawLine(Canvas canvas, PointF fromPoint, PointF toPoint) {
        canvas.drawLine(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y, getPaint());
    }
}
