package com.terrylinla.rnsketchcanvas.utils;

import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

public class CanvasText {
    public String text;
    public Paint paint;
    public PointF anchor, position, drawPosition, lineOffset;
    public boolean isAbsoluteCoordinate;
    public Rect textBounds;
    public float height;
}