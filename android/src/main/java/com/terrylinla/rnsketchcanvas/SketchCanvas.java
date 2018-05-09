package com.terrylinla.rnsketchcanvas;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import android.view.View;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.os.Environment;
import android.util.Base64;

import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import javax.annotation.Nullable;

public class SketchCanvas extends View {

    private ArrayList<SketchData> _paths = new ArrayList<SketchData>();
    private SketchData _currentPath = null;

    private ThemedReactContext mContext;
    private boolean _disableHardwareAccelerated = false;

    public SketchCanvas(ThemedReactContext context) {
        super(context);
        mContext = context;
    }

    public void clear() {
        this._paths.clear();
        this._currentPath = null;
        invalidateCanvas(true);
    }

    public void newPath(int id, int strokeColor, float strokeWidth) {
        this._currentPath = new SketchData(id, strokeColor, strokeWidth);
        this._paths.add(this._currentPath);
        boolean isErase = strokeColor == Color.TRANSPARENT;
        if (isErase && this._disableHardwareAccelerated == false) {
            this._disableHardwareAccelerated = true;
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        invalidateCanvas(true);
    }

    public void addPoint(float x, float y) {
        this._currentPath.addPoint(new PointF(x, y));
        invalidateCanvas(false);
    }

    public void addPath(int id, int strokeColor, float strokeWidth, ArrayList<PointF> points) {
        boolean exist = false;
        for(SketchData data: this._paths) {
            if (data.id == id) {
                exist = true;
                break;
            }
        }

        if (!exist) {
            this._paths.add(new SketchData(id, strokeColor, strokeWidth, points));
            boolean isErase = strokeColor == Color.TRANSPARENT;
            if (isErase && this._disableHardwareAccelerated == false) {
                this._disableHardwareAccelerated = true;
                this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
            invalidateCanvas(true);
        }
    }

    public void deletePath(int id) {
        int index = -1;
        for(int i=0; i<this._paths.size(); i++) {
            if (this._paths.get(i).id == id) {
                index = i;
                break;
            }
        }

        if (index > -1) {
            this._paths.remove(index);
            invalidateCanvas(true);
        }
    }

    public void onSaved(boolean success) {
        WritableMap event = Arguments.createMap();
        event.putBoolean("success", success);
        mContext.getJSModule(RCTEventEmitter.class).receiveEvent(
            getId(),
            "topChange",
            event);
    }

    public void save(String format, String folder, String filename, boolean transparent) {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + folder);
        boolean success = true;
        if (!f.exists())   success = f.mkdirs();
        if (success) {
            Bitmap  bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            if (format.equals("png")) {
                canvas.drawARGB(transparent ? 0 : 255, 255, 255, 255);
            } else {
                canvas.drawARGB(255, 255, 255, 255);
            }
            this.drawPath(canvas);

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                File.separator + folder + File.separator + filename + (format.equals("png") ? ".png" : ".jpg"));
            try {
                bitmap.compress(
                    format.equals("png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG,
                    format.equals("png") ? 100 : 90,
                    new FileOutputStream(file));
                this.onSaved(true);
            } catch (Exception e) {
                e.printStackTrace();
                this.onSaved(false);
            }
        } else {
            Log.e("SketchCanvas", "Failed to create folder!");
            this.onSaved(false);
        }
    }

    public void end() {
        if (this._currentPath != null) {
            this._currentPath.end();
        }
    }

    public String getBase64(String format, boolean transparent) {
        WritableMap event = Arguments.createMap();
        Bitmap  bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (format.equals("png")) {
            canvas.drawARGB(transparent ? 0 : 255, 255, 255, 255);
        } else {
            canvas.drawARGB(255, 255, 255, 255);
        }
        this.drawPath(canvas);

        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        bitmap.compress(
            format.equals("png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG,
            format.equals("png") ? 100 : 90,
            byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.drawPath(canvas);
    }

    private void invalidateCanvas(boolean shouldDispatchEvent) {
        if (shouldDispatchEvent) {
            WritableMap event = Arguments.createMap();
            event.putInt("pathsUpdate", this._paths.size());
            mContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                "topChange",
                event);
        }
        invalidate();
    }

    private void drawPath(Canvas canvas) {
        for(SketchData path: this._paths) {
            Paint paint = new Paint();
            boolean isErase = path.strokeColor == Color.TRANSPARENT;
            paint.setColor(path.strokeColor);
            paint.setStrokeWidth(path.strokeWidth);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setAntiAlias(true);
            paint.setXfermode(new PorterDuffXfermode(isErase ? PorterDuff.Mode.CLEAR : PorterDuff.Mode.SRC_OVER));

            if (path.path != null) {

                if (path.points.size() == 1) {
                    // draw if only 1 point
                    PointF origin = path.points.get(0);
                    canvas.drawPoint(origin.x, origin.y, paint);
                }

                // draw path
                canvas.drawPath(path.path, paint);
            } else {
                canvas.drawPath(path.evaluatePath(), paint);
            }
        }
    }
}
