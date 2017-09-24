package com.terrylinla.rnsketchcanvas;

import android.graphics.PointF;
import android.graphics.Color;
import android.graphics.Path;

import java.util.ArrayList;

public class SketchData {
    public ArrayList<PointF> points = new ArrayList<PointF>();
    public int id, strokeColor, strokeWidth;
    public Path path;

    public SketchData(int id, String strokeColor, int strokeWidth) {
        this.id = id;
        this.strokeColor = (Color.parseColor(strokeColor));
        this.strokeWidth = strokeWidth;
    }

    public SketchData(int id, String strokeColor, int strokeWidth, ArrayList<PointF> points) {
        this.id = id;
        this.strokeColor = (Color.parseColor(strokeColor));
        this.strokeWidth = strokeWidth;
        this.points.addAll(points);
    }

    public void addPoint(PointF p) {
        this.points.add(p);
    }

    public void end() {
        Path canvasPath = new Path();
        for(PointF p: this.points) {
            if (canvasPath.isEmpty()) canvasPath.moveTo(p.x, p.y);
            else canvasPath.lineTo(p.x, p.y);
        }

        this.path = canvasPath;
    }
}