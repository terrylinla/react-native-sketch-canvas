package com.terrylinla.rnsketchcanvas;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class SketchCanvas extends View {

    private ArrayList<SketchData> mPaths = new ArrayList<SketchData>();
    private SketchData mCurrentPath = null;

    private ThemedReactContext mContext;
    private boolean mDisableHardwareAccelerated = false;

    private Paint mPaint = new Paint();
    private Bitmap mDrawingBitmap = null;
    private Canvas mDrawingCanvas = null;

    private boolean mNeedsFullRedraw = true;

    public SketchCanvas(ThemedReactContext context) {
        super(context);
        mContext = context;
    }

    public void clear() {
        mPaths.clear();
        mCurrentPath = null;
        mNeedsFullRedraw = true;
        invalidateCanvas(true);
    }

    public void newPath(int id, int strokeColor, float strokeWidth) {
        mCurrentPath = new SketchData(id, strokeColor, strokeWidth);
        mPaths.add(mCurrentPath);
        boolean isErase = strokeColor == Color.TRANSPARENT;
        if (isErase && mDisableHardwareAccelerated == false) {
            mDisableHardwareAccelerated = true;
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        invalidateCanvas(true);
    }

    public void addPoint(float x, float y) {
        Rect updateRect = mCurrentPath.addPoint(new PointF(x, y));

        mCurrentPath.drawLastPoint(mDrawingCanvas);

        invalidate(updateRect);
    }

    public void addPath(int id, int strokeColor, float strokeWidth, ArrayList<PointF> points) {
        boolean exist = false;
        for(SketchData data: mPaths) {
            if (data.id == id) {
                exist = true;
                break;
            }
        }

        if (!exist) {
            SketchData newPath = new SketchData(id, strokeColor, strokeWidth, points);
            mPaths.add(newPath);
            boolean isErase = strokeColor == Color.TRANSPARENT;
            if (isErase && mDisableHardwareAccelerated == false) {
                mDisableHardwareAccelerated = true;
                setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
            newPath.draw(mDrawingCanvas);
            invalidateCanvas(true);
        }
    }

    public void deletePath(int id) {
        int index = -1;
        for(int i = 0; i<mPaths.size(); i++) {
            if (mPaths.get(i).id == id) {
                index = i;
                break;
            }
        }

        if (index > -1) {
            mPaths.remove(index);
            mNeedsFullRedraw = true;
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
            Bitmap  bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            if (format.equals("png")) {
                canvas.drawARGB(transparent ? 0 : 255, 255, 255, 255);
            } else {
                canvas.drawARGB(255, 255, 255, 255);
            }
            canvas.drawBitmap(mDrawingBitmap, 0, 0, mPaint);

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                File.separator + folder + File.separator + filename + (format.equals("png") ? ".png" : ".jpg"));
            try {
                bitmap.compress(
                    format.equals("png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG,
                    format.equals("png") ? 100 : 90,
                    new FileOutputStream(file));
                onSaved(true);
            } catch (Exception e) {
                e.printStackTrace();
                onSaved(false);
            }
        } else {
            Log.e("SketchCanvas", "Failed to create folder!");
            onSaved(false);
        }
    }

    public void end() {
        if (mCurrentPath != null) {
            mCurrentPath = null;
        }
    }

    public String getBase64(String format, boolean transparent) {
        WritableMap event = Arguments.createMap();
        Bitmap  bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (format.equals("png")) {
            canvas.drawARGB(transparent ? 0 : 255, 255, 255, 255);
        } else {
            canvas.drawARGB(255, 255, 255, 255);
        }
        canvas.drawBitmap(mDrawingBitmap, 0, 0, mPaint);

        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        bitmap.compress(
            format.equals("png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG,
            format.equals("png") ? 100 : 90,
            byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mDrawingBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        mDrawingCanvas = new Canvas(mDrawingBitmap);

        mNeedsFullRedraw = true;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mNeedsFullRedraw && mDrawingCanvas != null) {
            mDrawingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
            for(SketchData path: mPaths) {
                path.draw(mDrawingCanvas);
            }
            mNeedsFullRedraw = false;
        }

        if (mDrawingBitmap != null) {
            canvas.drawBitmap(mDrawingBitmap, 0, 0, mPaint);
        }
    }

    private void invalidateCanvas(boolean shouldDispatchEvent) {
        if (shouldDispatchEvent) {
            WritableMap event = Arguments.createMap();
            event.putInt("pathsUpdate", mPaths.size());
            mContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                "topChange",
                event);
        }
        invalidate();
    }
}
