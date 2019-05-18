package com.terrylinla.rnsketchcanvas;

import android.annotation.TargetApi;
import android.graphics.Picture;
import android.graphics.Region;
import android.graphics.Typeface;
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
import android.view.ViewGroup;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nullable;

class CanvasText {
    public String text;
    public Paint paint;
    public PointF anchor, position, drawPosition, lineOffset;
    public boolean isAbsoluteCoordinate;
    public Rect textBounds;
    public float height;
}

public class SketchCanvas extends View {
    //final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private ArrayList<SketchData> mPaths = new ArrayList<SketchData>();
    private SketchData mCurrentPath = null;

    private ThemedReactContext mContext;
    private boolean mDisableHardwareAccelerated = false;

    private Paint mPaint = new Paint();

    private int mOriginalWidth, mOriginalHeight;
    private Picture mBackgroundImage;
    private String mContentMode;

    private ArrayList<CanvasText> mArrCanvasText = new ArrayList<CanvasText>();
    private ArrayList<CanvasText> mArrTextOnSketch = new ArrayList<CanvasText>();
    private ArrayList<CanvasText> mArrSketchOnText = new ArrayList<CanvasText>();

    private int mTouchRadius = 0;

    public final static String TAG = "RNSketchCanvas";

    public SketchCanvas(ThemedReactContext context) {
        super(context);
        mContext = context;
    }

    public void setHardwareAccelerated(boolean useHardwareAccelerated) {
        mDisableHardwareAccelerated = !useHardwareAccelerated;
        if(useHardwareAccelerated) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else{
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
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
                Picture picture = new Picture();
                mOriginalHeight = bitmap.getHeight();
                mOriginalWidth = bitmap.getWidth();
                Canvas canvas = picture.beginRecording(mOriginalWidth, mOriginalHeight);
                canvas.drawBitmap(bitmap, 0, 0, null);
                picture.endRecording();

                mBackgroundImage = picture;
                mContentMode = mode;
                bitmap.recycle();

                invalidateCanvas(true);

                return true;
            }
        }
        return false;
    }

    public void setCanvasText(ReadableArray aText) {
        mArrCanvasText.clear();
        mArrSketchOnText.clear();
        mArrTextOnSketch.clear();

        if (aText != null) {
            for (int i=0; i<aText.size(); i++) {
                ReadableMap property = aText.getMap(i);
                if (property.hasKey("text")) {
                    String alignment = property.hasKey("alignment") ? property.getString("alignment") : "Left";
                    int lineOffset = 0, maxTextWidth = 0;
                    String[] lines = property.getString("text").split("\n");
                    ArrayList<CanvasText> textSet = new ArrayList<CanvasText>(lines.length);
                    for (String line: lines) {
                        ArrayList<CanvasText> arr = property.hasKey("overlay") && "TextOnSketch".equals(property.getString("overlay")) ? mArrTextOnSketch : mArrSketchOnText;
                        CanvasText text = new CanvasText();
                        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                        p.setTextAlign(Paint.Align.LEFT);
                        text.text = line;
                        if (property.hasKey("font")) {
                            Typeface font;
                            try {
                                font = Typeface.createFromAsset(mContext.getAssets(), property.getString("font"));
                            } catch(Exception ex) {
                                font = Typeface.create(property.getString("font"), Typeface.NORMAL);
                            }
                            p.setTypeface(font);
                        }
                        p.setTextSize(property.hasKey("fontSize") ? (float)property.getDouble("fontSize") : 12);
                        p.setColor(property.hasKey("fontColor") ? property.getInt("fontColor") : 0xFF000000);
                        text.anchor = property.hasKey("anchor") ? new PointF((float)property.getMap("anchor").getDouble("x"), (float)property.getMap("anchor").getDouble("y")) : new PointF(0, 0);
                        text.position = property.hasKey("position") ? new PointF((float)property.getMap("position").getDouble("x"), (float)property.getMap("position").getDouble("y")) : new PointF(0, 0);
                        text.paint = p;
                        text.isAbsoluteCoordinate = !(property.hasKey("coordinate") && "Ratio".equals(property.getString("coordinate")));
                        text.textBounds = new Rect();
                        p.getTextBounds(text.text, 0, text.text.length(), text.textBounds);

                        text.lineOffset = new PointF(0, lineOffset);
                        lineOffset += text.textBounds.height() * 1.5 * (property.hasKey("lineHeightMultiple") ? property.getDouble("lineHeightMultiple") : 1);
                        maxTextWidth = Math.max(maxTextWidth, text.textBounds.width());

                        arr.add(text);
                        mArrCanvasText.add(text);
                        textSet.add(text);
                    }
                    for(CanvasText text: textSet) {
                        text.height = lineOffset;
                        if (text.textBounds.width() < maxTextWidth) {
                            float diff = maxTextWidth - text.textBounds.width();
                            text.textBounds.left += diff * text.anchor.x;
                            text.textBounds.right += diff * text.anchor.x;
                        }
                    }
                    if (getWidth() > 0 && getHeight() > 0) {
                        for(CanvasText text: textSet) {
                            text.height = lineOffset;
                            PointF position = new PointF(text.position.x, text.position.y);
                            if (!text.isAbsoluteCoordinate) {
                                position.x *= getWidth();
                                position.y *= getHeight();
                            }
                            position.x -= text.textBounds.left;
                            position.y -= text.textBounds.top;
                            position.x -= (text.textBounds.width() * text.anchor.x);
                            position.y -= (text.height * text.anchor.y);
                            text.drawPosition = position;
                        }
                    }
                    if (lines.length > 1) {
                        for(CanvasText text: textSet) {
                            switch(alignment) {
                                case "Left":
                                default:
                                    break;
                                case "Right":
                                    text.lineOffset.x = (maxTextWidth - text.textBounds.width());
                                    break;
                                case "Center":
                                    text.lineOffset.x = (maxTextWidth - text.textBounds.width()) / 2;
                                    break;
                            }
                        }
                    }
                }
            }
        }

        invalidateCanvas(false);
    }

    public void clear() {
        mPaths.clear();
        mCurrentPath = null;
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
        invalidate();
    }

    public void  addPaths(@Nullable ReadableArray paths){
        for (int k = 0; k < paths.size(); k++){
            ReadableArray path = paths.getArray(k);
            addPath(path.getInt(0), path.getInt(1), (float)path.getInt(2), SketchCanvasManager.parsePathCoords(path.getArray(3)));
        }
        invalidateCanvas(true);
    }

    private void addPath(int id, int strokeColor, float strokeWidth, ArrayList<PointF> points) {
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
            invalidate();
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
            invalidateCanvas(true);
        }
    }

    public void end() {
        if (mCurrentPath != null) {
            mCurrentPath = null;
            invalidate();
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

    public void save(final String format, String folder, String filename, boolean transparent, boolean includeImage, boolean includeText, boolean cropToImageSize) {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + folder);
        boolean success = f.exists() ? true : f.mkdirs();
        if (success) {
            final Bitmap bitmap = createImage(format.equals("png") && transparent, includeImage, includeText, cropToImageSize);

            final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                File.separator + folder + File.separator + filename + (format.equals("png") ? ".png" : ".jpg"));

            new Thread(new Runnable() {
                public void run() {
                    try {
                        bitmap.compress(
                                format.equals("png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG,
                                format.equals("png") ? 100 : 90,
                                new FileOutputStream(file));
                        bitmap.recycle();
                        post(new Runnable() {
                            public void run() {
                                onSaved(true, file.getPath());
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        post(new Runnable() {
                            public void run() {
                                onSaved(false, null);
                            }
                        });
                    }
                }
            }).start();
        } else {
            Log.e(SketchCanvas.TAG, "SketchCanvas: Failed to create folder!");
            onSaved(false, null);
        }
    }

    public void getBase64(final String format, boolean transparent, boolean includeImage, boolean includeText, boolean cropToImageSize, final Callback callback) {
        WritableMap event = Arguments.createMap();
        final Bitmap bitmap = createImage(format.equals("png") && transparent, includeImage, includeText, cropToImageSize);
        final ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();

        new Thread(new Runnable() {
            public void run() {
                bitmap.compress(
                        format.equals("png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG,
                        format.equals("png") ? 100 : 90,
                        byteArrayOS);
                bitmap.recycle();
                post(new Runnable() {
                    public void run() {
                        String base64 = Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
                        callback.invoke(null, base64);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (getWidth() > 0 && getHeight() > 0 && (w != oldw || h != oldh)) {
            for(CanvasText text: mArrCanvasText) {
                PointF position = new PointF(text.position.x, text.position.y);
                if (!text.isAbsoluteCoordinate) {
                    position.x *= getWidth();
                    position.y *= getHeight();
                }

                position.x -= text.textBounds.left;
                position.y -= text.textBounds.top;
                position.x -= (text.textBounds.width() * text.anchor.x);
                position.y -= (text.height * text.anchor.y);
                text.drawPosition = position;

            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawOnCanvas(canvas);
    }

    private Canvas drawOnCanvas(Canvas canvas){
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);

        if (mBackgroundImage != null) {
            Rect dstRect = new Rect();
            canvas.getClipBounds(dstRect);
            Utility.fillImage(mBackgroundImage.getWidth(), mBackgroundImage.getHeight(), dstRect.width(), dstRect.height(), mContentMode).roundOut(dstRect);
            canvas.drawPicture(mBackgroundImage, dstRect);
        }

        for(CanvasText text: mArrSketchOnText) {
            canvas.drawText(text.text, text.drawPosition.x + text.lineOffset.x, text.drawPosition.y + text.lineOffset.y, text.paint);
        }

        for(SketchData path: mPaths) {
            path.draw(canvas);
        }

        for(CanvasText text: mArrTextOnSketch) {
            canvas.drawText(text.text, text.drawPosition.x + text.lineOffset.x, text.drawPosition.y + text.lineOffset.y, text.paint);
        }

        return canvas;
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

    private Bitmap createImage(boolean transparent, boolean includeImage, boolean includeText, boolean cropToImageSize) {
        Bitmap bitmap = Bitmap.createBitmap(
            mBackgroundImage != null && cropToImageSize ? mOriginalWidth : getWidth(),
            mBackgroundImage != null && cropToImageSize ? mOriginalHeight : getHeight(), 
            Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(transparent ? 0 : 255, 255, 255, 255);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);

        if (mBackgroundImage != null && includeImage) {
            Rect targetRect = new Rect();
            if(cropToImageSize){
                Utility.fillImage(mBackgroundImage.getWidth(), mBackgroundImage.getHeight(),
                        bitmap.getWidth(), bitmap.getHeight(), "AspectFit").roundOut(targetRect);
            }
            else{
                Utility.fillImage(mBackgroundImage.getWidth(), mBackgroundImage.getHeight(), bitmap.getWidth(), bitmap.getHeight(), mContentMode).roundOut(targetRect);
            }
            canvas.drawPicture(mBackgroundImage, targetRect);

        }

        if (includeText) {
            for(CanvasText text: mArrSketchOnText) {
                canvas.drawText(text.text, text.drawPosition.x + text.lineOffset.x, text.drawPosition.y + text.lineOffset.y, text.paint);
            }
        }

        for(SketchData path: mPaths) {
            path.draw(canvas);
        }

        if (includeText) {
            for(CanvasText text: mArrTextOnSketch) {
                canvas.drawText(text.text, text.drawPosition.x + text.lineOffset.x, text.drawPosition.y + text.lineOffset.y, text.paint);
            }
        }

        return bitmap;
    }

    private int getPathIndex(int pathId){
        for (int i=0; i < mPaths.size(); i++) {
            if(pathId == mPaths.get(i).id) {
                return i;
            }
        }
        return -1;
    }

    @TargetApi(19)
    private Region getRegion(){
        return new Region(getLeft(), getTop(), getRight(), getBottom());
    }

    public void setTouchRadius(int value){
        mTouchRadius = value;
    }

    private int getTouchRadius(float strokeWidth){
        return mTouchRadius <= 0 && strokeWidth > 0? (int)(strokeWidth * 0.5): mTouchRadius;
    }

    @TargetApi(19)
    public boolean isPointUnderTransparentPath(int x, int y, int pathId){
        int beginAt = Math.min(getPathIndex(pathId) + 1, mPaths.size() - 1);
        for (int i = getPathIndex(pathId); i < mPaths.size(); i++){
            SketchData mPath = mPaths.get(i);
            if(mPath.isPointOnPath(x, y, getTouchRadius(mPath.strokeWidth), getRegion()) && mPath.strokeColor == Color.TRANSPARENT) {
                return true;
            }
        }
        return false;
    }

    @TargetApi(19)
    public boolean isPointOnPath(int x, int y, int pathId){
        if(isPointUnderTransparentPath(x, y, pathId)) {
            return false;
        }
        else {
            SketchData mPath = mPaths.get(getPathIndex(pathId));
            return mPath.isPointOnPath(x, y, getTouchRadius(mPath.strokeWidth), getRegion());
        }
    }

    @TargetApi(19)
    public WritableArray isPointOnPath(int x, int y){
        WritableArray array = Arguments.createArray();
        Region mRegion = getRegion();
        SketchData mPath;
        int r;
        for (int i=0; i < mPaths.size(); i++) {
            mPath = mPaths.get(i);
            r = getTouchRadius(mPath.strokeWidth);
            if(mPath.isPointOnPath(x, y, r, mRegion) && !isPointUnderTransparentPath(x, y, mPath.id)){
                array.pushInt(mPath.id);
            }
        }

        return array;
    }

    public void tearDown(){

    }
}
