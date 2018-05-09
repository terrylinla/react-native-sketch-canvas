package com.terrylinla.rnsketchcanvas;

import android.graphics.PointF;
import android.graphics.Color;
import android.graphics.Path;

import java.util.ArrayList;

public class SketchData {
    public ArrayList<PointF> points = new ArrayList<PointF>();
    public int id, strokeColor;
    public float strokeWidth;
    public Path path;

    public SketchData(int id, int strokeColor, float strokeWidth) {
        this.id = id;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.path = new Path();
    }

    public SketchData(int id, int strokeColor, float strokeWidth, ArrayList<PointF> points) {
        this.id = id;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.points.addAll(points);
        this.path = evaluatePath();
    }

    public void addPoint(PointF p) {
        this.points.add(p);
        if (this.points.size() == 1) {
            addPointToPath(this.path, p, p, p);
        } else if (this.points.size() == 2) {
            addPointToPath(this.path, this.points.get(0), this.points.get(0), p);
        } else {
            addPointToPath(this.path, 
                this.points.get(this.points.size() - 3), 
                this.points.get(this.points.size() - 2),
                p);
        }
    }

    public void end() {
    }

    public Path evaluatePath() {
        Path canvasPath = new Path();
        PointF tertiaryPoint = null, previousPoint = null;
        for(PointF p: this.points) {
            if (tertiaryPoint == null && previousPoint == null) {
                // first point
                addPointToPath(canvasPath, p, p, p);
            } else if (tertiaryPoint == null) {
                // second point
                addPointToPath(canvasPath, previousPoint, previousPoint, p);
            } else {
                addPointToPath(canvasPath, tertiaryPoint, previousPoint, p);
            }
            tertiaryPoint = previousPoint;
            previousPoint = p;
        }
        return canvasPath;
    }

    private void addPointToPath(Path path, PointF tPoint, PointF pPoint, PointF point) {
        PointF mid1 = new PointF((pPoint.x + tPoint.x) * 0.5f, (pPoint.y + tPoint.y) * 0.5f);
        PointF mid2 = new PointF((point.x + pPoint.x) * 0.5f, (point.y + pPoint.y) * 0.5f);
        path.moveTo(mid1.x, mid1.y);
        path.quadTo(pPoint.x, pPoint.y, mid2.x, mid2.y);
    }
}
