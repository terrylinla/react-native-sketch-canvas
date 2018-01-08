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
import android.graphics.PointF;
import android.graphics.Bitmap;
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
    
    public SketchCanvas(ThemedReactContext context) {  
        super(context);
        mContext = context;
    }

    public void clear() {
        this._paths.clear();
        this._currentPath = null;
        invalidate();
    }

    public void newPath(int id, String strokeColor, int strokeWidth) {
        this._currentPath = new SketchData(id, strokeColor, strokeWidth);
        this._paths.add(this._currentPath);
    }

    public void addPoint(float x, float y) {
        this._currentPath.addPoint(new PointF(x, y));
        invalidate();
    }

    public void addPath(int id, String strokeColor, int strokeWidth, ArrayList<PointF> points) {
        boolean exist = false;
        for(SketchData data: this._paths) {
            if (data.id == id) {
                exist = true;
                break;
            }
        }

        if (!exist) {
            this._paths.add(new SketchData(id, strokeColor, strokeWidth, points));
            invalidate();
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
            invalidate();
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

    public void getBase64(String format, boolean transparent) {
        WritableMap event = Arguments.createMap();
        Bitmap  bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (format.equals("png")) {
            canvas.drawARGB(transparent ? 0 : 255, 255, 255, 255);
        } else {
            canvas.drawARGB(255, 255, 255, 255);
        }
        this.drawPath(canvas);

        try {
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            bitmap.compress(
                format.equals("png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 
                format.equals("png") ? 100 : 90, 
                byteArrayOS);
            event.putString("base64", Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT));
        } catch (Exception e) {
            e.printStackTrace();
            this.onSaved(false);
            event.putString("base64", null);
        }   

        mContext.getJSModule(RCTEventEmitter.class).receiveEvent(
            getId(),
            "topChange",
            event);
    }

    @Override  
    protected void onDraw(Canvas canvas) {  
        super.onDraw(canvas);
        this.drawPath(canvas);
    }

    private void drawPath(Canvas canvas) {
        for(SketchData path: this._paths) {
            Paint paint = new Paint();
            paint.setColor(path.strokeColor); 
            paint.setStrokeWidth(path.strokeWidth);
            paint.setStyle(Paint.Style.STROKE);

            if (path.path != null) {
                canvas.drawPath(path.path, paint);    
            } else {
                Path canvasPath = new Path();
                for(PointF p: path.points) {
                    if (canvasPath.isEmpty()) canvasPath.moveTo(p.x, p.y);
                    else canvasPath.lineTo(p.x, p.y);
                }

                canvas.drawPath(canvasPath, paint);
            }
        }
    }
}  