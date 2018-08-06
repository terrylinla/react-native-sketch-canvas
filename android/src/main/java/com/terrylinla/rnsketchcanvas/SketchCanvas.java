package com.terrylinla.rnsketchcanvas;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private Bitmap mDrawingBitmap = null, mTranslucentDrawingBitmap = null;
    private Canvas mDrawingCanvas = null, mTranslucentDrawingCanvas = null;

    private boolean mNeedsFullRedraw = true;

    private int mOriginalWidth, mOriginalHeight;
    private Bitmap mBackgroundImage;
    private String mContentMode;

    public SketchCanvas(ThemedReactContext context) {
        super(context);
        mContext = context;
    }

    public boolean openImageFile(String filename, String directory, String mode) {
        if(filename != null) {
            int res = mContext.getResources().getIdentifier(
                filename.lastIndexOf('.') == -1 ? filename : filename.substring(0, filename.lastIndexOf('.')), 
                "drawable", 
                mContext.getPackageName());
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            Bitmap bitmap = res == 0 ? 
                BitmapFactory.decodeFile(new File(filename, directory == null ? "" : directory).toString(), bitmapOptions) :
                BitmapFactory.decodeResource(mContext.getResources(), res);
            if(bitmap != null) {
                mBackgroundImage = bitmap;
                mOriginalHeight = bitmap.getHeight();
                mOriginalWidth = bitmap.getWidth();
                mContentMode = mode;

                invalidateCanvas(true);

                return true;
            }
        }
        return false;
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

        if (mCurrentPath.isTranslucent) {
            mTranslucentDrawingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
            mCurrentPath.draw(mTranslucentDrawingCanvas);
        } else {
            mCurrentPath.drawLastPoint(mDrawingCanvas);
        }
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

    public void end() {
        if (mCurrentPath != null) {
            if (mCurrentPath.isTranslucent) {
                mCurrentPath.draw(mDrawingCanvas);
                mTranslucentDrawingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
            }
            mCurrentPath = null;
        }
    }

    public void onSaved(boolean success, String path) {
        WritableMap event = Arguments.createMap();
        event.putBoolean("success", success);
        event.putString("path", path);
        mContext.getJSModule(RCTEventEmitter.class).receiveEvent(
            getId(),
            "topChange",
            event);
    }

    public void save(String format, String folder, String filename, boolean transparent, boolean includeImage, boolean cropToImageSize) {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + folder);
        boolean success = f.exists() ? true : f.mkdirs();
        if (success) {
            Bitmap bitmap = createImage(format.equals("png") && transparent, includeImage, cropToImageSize);

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                File.separator + folder + File.separator + filename + (format.equals("png") ? ".png" : ".jpg"));
            try {
                bitmap.compress(
                    format.equals("png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG,
                    format.equals("png") ? 100 : 90,
                    new FileOutputStream(file));
                this.onSaved(true, file.getPath());
            } catch (Exception e) {
                e.printStackTrace();
                onSaved(false, null);
            }
        } else {
            Log.e("SketchCanvas", "Failed to create folder!");
            onSaved(false, null);
        }
    }

    public String getBase64(String format, boolean transparent, boolean includeImage, boolean cropToImageSize) {
        WritableMap event = Arguments.createMap();
        Bitmap bitmap = createImage(format.equals("png") && transparent, includeImage, cropToImageSize);
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
        mTranslucentDrawingBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        mTranslucentDrawingCanvas = new Canvas(mTranslucentDrawingBitmap);
        
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

        if (mBackgroundImage != null) {
            Rect dstRect = new Rect();
            canvas.getClipBounds(dstRect);
            canvas.drawBitmap(mBackgroundImage, null, 
                Utility.fillImage(mBackgroundImage.getWidth(), mBackgroundImage.getHeight(), dstRect.width(), dstRect.height(), mContentMode), 
                null);
        }

        if (mDrawingBitmap != null) {
            canvas.drawBitmap(mDrawingBitmap, 0, 0, mPaint);
        }

        if (mTranslucentDrawingBitmap != null && mCurrentPath != null && mCurrentPath.isTranslucent) {
            canvas.drawBitmap(mTranslucentDrawingBitmap, 0, 0, mPaint);
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

    private Bitmap createImage(boolean transparent, boolean includeImage, boolean cropToImageSize) {
        Bitmap bitmap = Bitmap.createBitmap(
            mBackgroundImage != null && cropToImageSize ? mOriginalWidth : getWidth(),
            mBackgroundImage != null && cropToImageSize ? mOriginalHeight : getHeight(), 
            Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(transparent ? 0 : 255, 255, 255, 255);

        if (mBackgroundImage != null && includeImage) {
            Rect targetRect = new Rect();
            Utility.fillImage(mBackgroundImage.getWidth(), mBackgroundImage.getHeight(), 
                bitmap.getWidth(), bitmap.getHeight(), "AspectFit").roundOut(targetRect);
            canvas.drawBitmap(mBackgroundImage, null, targetRect, null);
        }


        if (mBackgroundImage != null && cropToImageSize) {
            Rect targetRect = new Rect();
            Utility.fillImage(mDrawingBitmap.getWidth(), mDrawingBitmap.getHeight(), 
                bitmap.getWidth(), bitmap.getHeight(), "AspectFill").roundOut(targetRect);
            canvas.drawBitmap(mDrawingBitmap, null, targetRect, mPaint);
        } else {
            canvas.drawBitmap(mDrawingBitmap, 0, 0, mPaint);
        }
        return bitmap;
    }
}
