package com.terrylinla.rnsketchcanvas;

import android.graphics.PointF;
import android.graphics.Color;
import android.graphics.Path;

import java.util.ArrayList;

public class SketchData {
    public ArrayList<PointF> points = new ArrayList<PointF>();
    public int id, strokeColor, strokeWidth;
    public Path path;

    public SketchData(int id, int strokeColor, int strokeWidth) {
        this.id = id;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
    }

    public SketchData(int id, int strokeColor, int strokeWidth, ArrayList<PointF> points) {
        this.id = id;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.points.addAll(points);
    }

    public void addPoint(PointF p) {
        this.points.add(p);
    }

    public void end() {
        // TODO: centralize path making with SketchCanvas.java:drawPath()
        Path canvasPath = new Path();
        PointF previousPoint = null;
        for(PointF p: this.points) {
            if (canvasPath.isEmpty()) {
              canvasPath.moveTo(p.x, p.y);
            } else {
              float midX = (previousPoint.x + p.x) / 2;
              float midY = (previousPoint.y + p.y) / 2;
              canvasPath.quadTo(previousPoint.x, previousPoint.y, midX, midY);
            }
            previousPoint = p;
        }

        this.path = canvasPath;
    }
}
