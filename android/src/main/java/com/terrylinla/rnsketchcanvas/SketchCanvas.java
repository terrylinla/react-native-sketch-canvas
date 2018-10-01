package com.terrylinla.rnsketchcanvas;

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
import android.view.ScaleGestureDetector;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.support.v4.view.GestureDetectorCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.terrylinla.utils.CircleLayer;
import com.terrylinla.utils.Layer;
import com.terrylinla.utils.MotionEntity;
import com.terrylinla.utils.TextEntity;
import com.terrylinla.utils.TextLayer;
import com.terrylinla.utils.gestureDetectors.MoveGestureDetector;
import com.terrylinla.utils.gestureDetectors.RotateGestureDetector;
import com.terrylinla.utils.shapes.CircleEntity;

class CanvasText {
    public String text;
    public Paint paint;
    public PointF anchor, position, drawPosition, lineOffset;
    public boolean isAbsoluteCoordinate;
    public Rect textBounds;
    public float height;
}

public class SketchCanvas extends View {

    // Data
    private ArrayList<SketchData> mPaths = new ArrayList<SketchData>();
    private SketchData mCurrentPath = null;

    // Gesture Detection
    private ScaleGestureDetector mScaleGestureDetector;
    private RotateGestureDetector mRotateGestureDetector;
    private MoveGestureDetector mMoveGestureDetector;
    private GestureDetectorCompat mGestureDetectorCompat;

    // TODO: Shapes: Just TextEntity works atm, working on shapes now!
    private final ArrayList<MotionEntity> mEntities = new ArrayList<MotionEntity>();
    private MotionEntity mSelectedEntity;

    // TODO: Text: can be changed to TextEntity to make it scalable, movable and
    // rotatable :).
    // We could even add DoubleTap to edit text :D
    private ArrayList<CanvasText> mArrCanvasText = new ArrayList<CanvasText>();
    private ArrayList<CanvasText> mArrTextOnSketch = new ArrayList<CanvasText>();
    private ArrayList<CanvasText> mArrSketchOnText = new ArrayList<CanvasText>();

    // Bitmap
    // TODO: We could add ImageEntities to add image stickers besides the bg-image
    private Bitmap mDrawingBitmap = null, mTranslucentDrawingBitmap = null;
    private Bitmap mBackgroundImage;
    private Canvas mDrawingCanvas = null, mTranslucentDrawingCanvas = null;
    private int mOriginalBitmapWidth, mOriginalBitmapHeight;
    private String mBitmapContentMode;

    // General
    private float mStrokeWidth = 5;
    private int mStrokeColor = Color.BLACK;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Canvas mSketchCanvas = null;
    private ThemedReactContext mContext;
    private boolean mDisableHardwareAccelerated = false;
    private boolean mNeedsFullRedraw = true;

    public SketchCanvas(ThemedReactContext context) {
        super(context);
        mContext = context;

        this.mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        this.mRotateGestureDetector = new RotateGestureDetector(context, new RotateListener());
        this.mMoveGestureDetector = new MoveGestureDetector(context, new MoveListener());
        this.mGestureDetectorCompat = new GestureDetectorCompat(context, new TapsListener());

        // Is initialized at bottom of class w/ other GestureDetectors
        setOnTouchListener(mOnTouchListener);
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
                mOriginalBitmapHeight = bitmap.getHeight();
                mOriginalBitmapWidth = bitmap.getWidth();
                mBitmapContentMode = mode;

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
        mNeedsFullRedraw = true;
        invalidateCanvas(true);
    }

    public void newPath(int id, int strokeColor, float strokeWidth) {
        mCurrentPath = new SketchData(id, strokeColor, strokeWidth);
        mStrokeColor = strokeColor;
        mStrokeWidth = Utility.convertPxToDpAsFloat(mContext.getResources().getDisplayMetrics(), strokeWidth);
        mPaths.add(mCurrentPath);
        boolean isErase = strokeColor == Color.TRANSPARENT;
        if (isErase && mDisableHardwareAccelerated == false) {
            mDisableHardwareAccelerated = true;
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        invalidateCanvas(true);
    }

    public void addPoint(float x, float y) {
        if (mSelectedEntity == null && findEntityAtPoint(x, y) == null) {
            Rect updateRect = mCurrentPath.addPoint(new PointF(x, y));
            if (mCurrentPath.isTranslucent) {
                mTranslucentDrawingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
                mCurrentPath.draw(mTranslucentDrawingCanvas);
            } else {
                mCurrentPath.drawLastPoint(mDrawingCanvas);
            }
            invalidate(updateRect);
        }
    }

    public void addPath(int id, int strokeColor, float strokeWidth, ArrayList<PointF> points) {
        mStrokeColor = strokeColor;

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

    public void onShapeSelectionChanged(MotionEntity nextSelectedEntity) {
        final WritableMap event = Arguments.createMap();
        boolean isShapeSelected = nextSelectedEntity != null;
        event.putBoolean("isShapeSelected", isShapeSelected);

        if (!isShapeSelected) {
            // This is ugly and actually was my last resort to fix the "do not draw when deselecting" problem
            // without breaking existing functionality
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    mContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                            getId(),
                            "topChange",
                            event);
                }
            }, 250);
        } else {
            mContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                    getId(),
                    "topChange",
                    event);
        }
    }

    public void save(String format, String folder, String filename, boolean transparent, boolean includeImage, boolean includeText, boolean cropToImageSize) {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + folder);
        boolean success = f.exists() ? true : f.mkdirs();
        if (success) {
            Bitmap bitmap = createImage(format.equals("png") && transparent, includeImage, includeText, cropToImageSize);

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

    public String getBase64(String format, boolean transparent, boolean includeImage, boolean includeText, boolean cropToImageSize) {
        WritableMap event = Arguments.createMap();
        Bitmap bitmap = createImage(format.equals("png") && transparent, includeImage, includeText, cropToImageSize);
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

        if (getWidth() > 0 && getHeight() > 0) {
            mDrawingBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                    Bitmap.Config.ARGB_8888);
            mDrawingCanvas = new Canvas(mDrawingBitmap);
            mTranslucentDrawingBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                    Bitmap.Config.ARGB_8888);
            mTranslucentDrawingCanvas = new Canvas(mTranslucentDrawingBitmap);

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

            mNeedsFullRedraw = true;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mSketchCanvas = canvas;

        if (mNeedsFullRedraw && mDrawingCanvas != null) {
            mDrawingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);

            for(SketchData path: mPaths) {
                path.draw(mDrawingCanvas);
            }
            mNeedsFullRedraw = false;
        }

        if (mBackgroundImage != null) {
            Rect dstRect = new Rect();
            mSketchCanvas.getClipBounds(dstRect);
            mSketchCanvas.drawBitmap(mBackgroundImage, null,
                    Utility.fillImage(mBackgroundImage.getWidth(), mBackgroundImage.getHeight(), dstRect.width(), dstRect.height(), mBitmapContentMode),
                    null);
        }

        for(CanvasText text: mArrSketchOnText) {
            mSketchCanvas.drawText(text.text, text.drawPosition.x + text.lineOffset.x, text.drawPosition.y + text.lineOffset.y, text.paint);
        }

        if (mDrawingBitmap != null) {
            mSketchCanvas.drawBitmap(mDrawingBitmap, 0, 0, mPaint);
        }

        if (mTranslucentDrawingBitmap != null && mCurrentPath != null && mCurrentPath.isTranslucent) {
            mSketchCanvas.drawBitmap(mTranslucentDrawingBitmap, 0, 0, mPaint);
        }

        for(CanvasText text: mArrTextOnSketch) {
            mSketchCanvas.drawText(text.text, text.drawPosition.x + text.lineOffset.x, text.drawPosition.y + text.lineOffset.y, text.paint);
        }

        if (mEntities.isEmpty()) {
            addTextSticker();
            addTextSticker();
            addCircleShape();
        } else {
            drawAllEntities(mSketchCanvas);
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

    private Bitmap createImage(boolean transparent, boolean includeImage, boolean includeText, boolean cropToImageSize) {
        Bitmap bitmap = Bitmap.createBitmap(
                mBackgroundImage != null && cropToImageSize ? mOriginalBitmapWidth : getWidth(),
                mBackgroundImage != null && cropToImageSize ? mOriginalBitmapHeight : getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(transparent ? 0 : 255, 255, 255, 255);

        if (mBackgroundImage != null && includeImage) {
            Rect targetRect = new Rect();
            Utility.fillImage(mBackgroundImage.getWidth(), mBackgroundImage.getHeight(),
                    bitmap.getWidth(), bitmap.getHeight(), "AspectFit").roundOut(targetRect);
            canvas.drawBitmap(mBackgroundImage, null, targetRect, null);
        }

        if (includeText) {
            for(CanvasText text: mArrSketchOnText) {
                canvas.drawText(text.text, text.drawPosition.x + text.lineOffset.x, text.drawPosition.y + text.lineOffset.y, text.paint);
            }
        }

        if (mBackgroundImage != null && cropToImageSize) {
            Rect targetRect = new Rect();
            Utility.fillImage(mDrawingBitmap.getWidth(), mDrawingBitmap.getHeight(),
                    bitmap.getWidth(), bitmap.getHeight(), "AspectFill").roundOut(targetRect);
            canvas.drawBitmap(mDrawingBitmap, null, targetRect, mPaint);
        } else {
            canvas.drawBitmap(mDrawingBitmap, 0, 0, mPaint);
        }

        if (includeText) {
            for(CanvasText text: mArrTextOnSketch) {
                canvas.drawText(text.text, text.drawPosition.x + text.lineOffset.x, text.drawPosition.y + text.lineOffset.y, text.paint);
            }
        }

        drawAllEntities(canvas);

        return bitmap;
    }

    protected void addCircleShape() {
        CircleLayer shapeLayer = createCircleLayer();
        CircleEntity circleEntity = new CircleEntity(shapeLayer, mSketchCanvas.getWidth(), mSketchCanvas.getHeight(), 300, 20f, Utility.convertDpToPxAsFloat(mContext.getResources().getDisplayMetrics(), mStrokeWidth), mStrokeColor);
        addEntityAndPosition(circleEntity);

        PointF center = circleEntity.absoluteCenter();
        center.y = center.y * 0.5F;
        circleEntity.moveCenterTo(center);

        updateUI();
    }

    // Shape entity related code
    protected void addTextSticker() {
        TextLayer textLayer = createTextLayer();
        textLayer.setText("This is just a test");
        TextEntity textEntity = new TextEntity(textLayer, mSketchCanvas.getWidth(), mSketchCanvas.getHeight() );
        addEntityAndPosition(textEntity);

        // move text sticker up so that its not hidden under keyboard
        PointF center = textEntity.absoluteCenter();
        center.y = center.y * 0.5F;
        textEntity.moveCenterTo(center);

        // redraw
        updateUI();

        // startTextEntityEditing();
    }

    private TextLayer createTextLayer() {
        TextLayer textLayer = new TextLayer();
        return textLayer;
    }

    private Layer createLayer() {
        Layer layer = new Layer();
        return layer;
    }

    private CircleLayer createCircleLayer() {
        CircleLayer circleLayer = new CircleLayer();
        return circleLayer;
    }

    public void addEntity(MotionEntity entity) {
        if (entity != null) {
            mEntities.add(entity);
            selectEntity(entity, true);
        }
    }

    public void addEntityAndPosition(MotionEntity entity) {
        if (entity != null) {
            initEntityBorder(entity);
            initialTranslateAndScale(entity);
            mEntities.add(entity);
            selectEntity(entity, true);
        }
    }

    private void initEntityBorder(MotionEntity entity) {
        int strokeSize = Utility.convertDpToPx(mContext.getResources().getDisplayMetrics(), 3);
        Paint borderPaint = new Paint();
        borderPaint.setStrokeWidth(strokeSize);
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(Color.BLACK);
        entity.setBorderPaint(borderPaint);
    }

    private void drawAllEntities(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(mStrokeColor);
        paint.setStrokeWidth(mStrokeWidth);

        for (int i = 0; i < mEntities.size(); i++) {
            mEntities.get(i).draw(canvas, paint);
        }
    }

    private void updateUI() {
        invalidateCanvas(true);
    }

    private void handleTranslate(PointF delta) {
        if (mSelectedEntity != null) {
            float newCenterX = mSelectedEntity.absoluteCenterX() + delta.x;
            float newCenterY = mSelectedEntity.absoluteCenterY() + delta.y;
            // limit entity center to screen bounds
            boolean needUpdateUI = false;
            if (newCenterX >= 0 && newCenterX <= getWidth()) {
                mSelectedEntity.getLayer().postTranslate(delta.x / getWidth(), 0.0F);
                needUpdateUI = true;
            }
            if (newCenterY >= 0 && newCenterY <= getHeight()) {
                mSelectedEntity.getLayer().postTranslate(0.0F, delta.y / getHeight());
                needUpdateUI = true;
            }
            if (needUpdateUI) {
                updateUI();
            }
        }
    }

    private void initialTranslateAndScale(MotionEntity entity) {
        entity.moveToCanvasCenter();
        entity.getLayer().setScale(entity.getLayer().initialScale());
    }

    private void selectEntity(MotionEntity entity, boolean updateCallback) {
        if (mSelectedEntity != null) {
            mSelectedEntity.setIsSelected(false);
        }
        if (entity != null) {
            entity.setIsSelected(true);
        }
        mSelectedEntity = entity;
        updateUI();
        if (updateCallback) {
        }
    }

    private MotionEntity findEntityAtPoint(float x, float y) {
        MotionEntity selected = null;
        PointF p = new PointF(x, y);
        for (int i = mEntities.size() - 1; i >= 0; i--) {
            if (mEntities.get(i).pointInLayerRect(p)) {
                selected = mEntities.get(i);
                break;
            }
        }
        return selected;
    }

    private void bringLayerToFront(MotionEntity entity) {
        // removing and adding brings layer to front
        if (mEntities.remove(entity)) {
            mEntities.add(entity);
            updateUI();
        }
    }

    private void updateSelectionOnTap(MotionEvent e) {
        MotionEntity entity = findEntityAtPoint(e.getX(), e.getY());
        onShapeSelectionChanged(entity);
        selectEntity(entity, true);
    }

    private void updateOnLongPress(MotionEvent e) {
        // if layer is currently selected and point inside layer - move it to front
        if (mSelectedEntity != null) {
            PointF p = new PointF(e.getX(), e.getY());
            if (mSelectedEntity.pointInLayerRect(p)) {
                bringLayerToFront(mSelectedEntity);
            }
        }
    }

    // memory
    public void release() {
        for (MotionEntity entity : mEntities) {
            entity.release();
        }
    }


    /**
     *
     * Gesture Listeners
     *
     * Connect the gesture detectors to the native touch listener. The
     * JS-PanResponder is disabled while a MotionEntity is selected immediately. The
     * JS-PanResponder is enabled again with a 150ms delay, through the
     * onShapeSelectionChanged event, when the MotionEntity is deselected.
     *
     * The 100-150ms delay is there to ensure no point is drawn when deselecting a
     * shape.
     *
     **/
    private final View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mScaleGestureDetector != null) {
                mScaleGestureDetector.onTouchEvent(event);
                mRotateGestureDetector.onTouchEvent(event);
                mMoveGestureDetector.onTouchEvent(event);
                mGestureDetectorCompat.onTouchEvent(event);
            }
            return true;
        }
    };

    private class TapsListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mSelectedEntity != null) {
                // Double tap happened
                return true;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // TODO: We may not need this...
            updateOnLongPress(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // Update mSelectedEntity. Fires onShapeSelectionChanged (JS-PanResponder
            // enabling/disabling)
            updateSelectionOnTap(e);
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (mSelectedEntity != null) {
                float scaleFactorDiff = detector.getScaleFactor();
                mSelectedEntity.getLayer().postScale(scaleFactorDiff - 1.0F);
                updateUI();
                return true;
            }
            return false;
        }
    }

    private class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {
        @Override
        public boolean onRotate(RotateGestureDetector detector) {
            if (mSelectedEntity != null) {
                mSelectedEntity.getLayer().postRotate(-detector.getRotationDegreesDelta());
                updateUI();
                return true;
            }
            return false;
        }
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            if (mSelectedEntity != null) {
                handleTranslate(detector.getFocusDelta());
                return true;
            }
            return false;
        }
    }
}
