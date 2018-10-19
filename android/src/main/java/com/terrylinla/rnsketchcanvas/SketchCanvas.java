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

import com.terrylinla.rnsketchcanvas.utils.CanvasText;
import com.terrylinla.rnsketchcanvas.utils.Utility;
import com.terrylinla.rnsketchcanvas.utils.layers.CircleLayer;
import com.terrylinla.rnsketchcanvas.utils.layers.Font;
import com.terrylinla.rnsketchcanvas.utils.layers.Layer;
import com.terrylinla.rnsketchcanvas.utils.layers.TextLayer;
import com.terrylinla.rnsketchcanvas.utils.entities.BorderStyle;
import com.terrylinla.rnsketchcanvas.utils.entities.EntityType;
import com.terrylinla.rnsketchcanvas.utils.entities.CircleEntity;
import com.terrylinla.rnsketchcanvas.utils.entities.MotionEntity;
import com.terrylinla.rnsketchcanvas.utils.entities.TextEntity;
import com.terrylinla.rnsketchcanvas.utils.gestureDetectors.MoveGestureDetector;
import com.terrylinla.rnsketchcanvas.utils.gestureDetectors.RotateGestureDetector;

public class SketchCanvas extends View {
    // Data
    private ArrayList<SketchData> mPaths = new ArrayList<SketchData>();
    private SketchData mCurrentPath = null;

    // Gesture Detection
    private ScaleGestureDetector mScaleGestureDetector;
    private RotateGestureDetector mRotateGestureDetector;
    private MoveGestureDetector mMoveGestureDetector;
    private GestureDetectorCompat mGestureDetectorCompat;

    // Shapes/Entities
    private final ArrayList<MotionEntity> mEntities = new ArrayList<MotionEntity>();
    private MotionEntity mSelectedEntity;
    private int mEntityBorderColor = Color.TRANSPARENT;
    private BorderStyle mEntityBorderStyle = BorderStyle.DASHED;
    private float mEntityBorderStrokeWidth = 1;
    private float mEntityStrokeWidth = 5;
    private int mEntityStrokeColor = Color.BLACK;

    // Text
    private ArrayList<CanvasText> mArrCanvasText = new ArrayList<CanvasText>();
    private ArrayList<CanvasText> mArrTextOnSketch = new ArrayList<CanvasText>();
    private ArrayList<CanvasText> mArrSketchOnText = new ArrayList<CanvasText>();
    private Typeface mTypeface;

    // Bitmap
    private Bitmap mDrawingBitmap = null, mTranslucentDrawingBitmap = null;
    private Bitmap mBackgroundImage;
    private Canvas mDrawingCanvas = null, mTranslucentDrawingCanvas = null;
    private int mOriginalBitmapWidth, mOriginalBitmapHeight;
    private String mBitmapContentMode;

    // General
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

    /**
     *
     * Canvas/Draw related code
     *
     **/
    public void clear() {
        mPaths.clear();
        mCurrentPath = null;
        mNeedsFullRedraw = true;
        invalidateCanvas(true);
    }

    public void newPath(int id, int strokeColor, float strokeWidth) {
        mCurrentPath = new SketchData(id, strokeColor, strokeWidth);
        if (strokeColor != Color.TRANSPARENT) {
            mEntityStrokeColor = strokeColor;
        }
        mEntityStrokeWidth = Utility.convertPxToDpAsFloat(mContext.getResources().getDisplayMetrics(), strokeWidth);
        mPaths.add(mCurrentPath);
        boolean isErase = strokeColor == Color.TRANSPARENT;
        if (isErase && mDisableHardwareAccelerated == false) {
            mDisableHardwareAccelerated = true;
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        invalidateCanvas(true);
    }

    public void addPoint(float x, float y, boolean isMove) {
        if (mSelectedEntity == null && (findEntityAtPoint(x, y) == null || isMove)) {
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
        if (strokeColor != Color.TRANSPARENT) {
            mEntityStrokeColor = strokeColor;
        }

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

        if (!mEntities.isEmpty()) {
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

    /**
     *
     * Outgoing Events related code
     *
     **/
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

    public void onSaved(boolean success, String path) {
        WritableMap event = Arguments.createMap();
        event.putBoolean("success", success);
        event.putString("path", path);
        mContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                "topChange",
                event);
    }

    /**
     *
     * Incoming Events related code
     *
     **/
    public void setShapeConfiguration(ReadableMap shapeConfiguration) {
        if (shapeConfiguration.hasKey("shapeBorderColor")) {
            int color = shapeConfiguration.getInt("shapeBorderColor");
            if (color != Color.TRANSPARENT) {
                mEntityBorderColor = color;
            }
        }
        if (shapeConfiguration.hasKey("shapeBorderStyle")) {
            String borderStyle = shapeConfiguration.getString("shapeBorderStyle");
            switch(borderStyle) {
                case "Dashed":
                    mEntityBorderStyle = BorderStyle.DASHED;
                    break;
                case "Solid":
                    mEntityBorderStyle = BorderStyle.SOLID;
                    break;
                default:
                    mEntityBorderStyle = BorderStyle.DASHED;
                    break;
            }
        }
        if (shapeConfiguration.hasKey("shapeBorderStrokeWidth")) {
            mEntityBorderStrokeWidth = shapeConfiguration.getInt("shapeBorderStrokeWidth");
        }
        if (shapeConfiguration.hasKey("shapeColor")) {
            int color = shapeConfiguration.getInt("shapeColor");
            if (color != Color.TRANSPARENT) {
                mEntityStrokeColor = color;
            }
        }
        if (shapeConfiguration.hasKey("shapeStrokeWidth")) {
            mEntityStrokeWidth = shapeConfiguration.getInt("shapeStrokeWidth");
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
                            try {
                                mTypeface = Typeface.createFromAsset(mContext.getAssets(), property.getString("font"));
                            } catch(Exception ex) {
                                mTypeface = Typeface.create(property.getString("font"), Typeface.NORMAL);
                            }
                            p.setTypeface(mTypeface);
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

    /**
     *
     * MotionEntities related code
     *
     **/
    public void addEntity(EntityType shapeType, String textShapeFontType, int textShapeFontSize, String textShapeText, String imageShapeAsset) {
        switch(shapeType) {
            case CIRCLE:
                addCircleEntity();
                break;
            case TEXT:
                addTextEntity(textShapeFontType, textShapeFontSize, textShapeText);
                break;
            case RECT:
                // TODO: Doesn't exist yet
                break;
            case TRIANGLE:
                // TODO: Doesn't exist yet
                break;
            case ARROW:
                // TODO: Doesn't exist yet
                break;
            case IMAGE:
                // TODO: Doesn't exist yet
                break;
            default:
                addCircleEntity();
                break;
        }
    }
    
    protected void addCircleEntity() {
        CircleLayer circleLayer = new CircleLayer();
        CircleEntity circleEntity = null;
        if (mSketchCanvas.getWidth() < 100 || mSketchCanvas.getHeight() < 100) {
            circleEntity = new CircleEntity(circleLayer, mDrawingCanvas.getWidth(), mDrawingCanvas.getHeight(), 300, 20f, mEntityStrokeWidth, mEntityStrokeColor);
        } else {
            circleEntity = new CircleEntity(circleLayer, mSketchCanvas.getWidth(), mSketchCanvas.getHeight(), 300, 20f, mEntityStrokeWidth, mEntityStrokeColor);
        }
        addEntityAndPosition(circleEntity);

        PointF center = circleEntity.absoluteCenter();
        center.y = center.y * 0.5F;
        circleEntity.moveCenterTo(center);

        invalidateCanvas(true);
    }

    protected void addTextEntity(String fontType, int fontSize, String text) {
        TextLayer textLayer = createTextLayer(fontType, fontSize);
        if (text != null) {
            textLayer.setText(text);
        } else {
            textLayer.setText("No Text provided!");
        }

        TextEntity textEntity = null;
        if (mSketchCanvas.getWidth() < 100 || mSketchCanvas.getHeight() < 100) {
            textEntity = new TextEntity(textLayer, mDrawingCanvas.getWidth(), mDrawingCanvas.getHeight());
        } else {
            textEntity = new TextEntity(textLayer, mSketchCanvas.getWidth(), mSketchCanvas.getHeight());
        }
        addEntityAndPosition(textEntity);

        PointF center = textEntity.absoluteCenter();
        center.y = center.y * 0.5F;
        textEntity.moveCenterTo(center);

        invalidateCanvas(true);
    }

    private TextLayer createTextLayer(String fontType, int fontSize) {
        TextLayer textLayer = new TextLayer(mContext);
        Font font = new Font(mContext, null);
        font.setColor(mEntityStrokeColor);

        if (fontSize > 0) {
            float convertedFontSize = (float)fontSize / 200;
            font.setSize(convertedFontSize);
        } else {
            font.setSize(TextLayer.Limits.INITIAL_FONT_SIZE);
        }

        if (fontType != null) {
            Typeface typeFace = null;
            try {
                typeFace = Typeface.createFromAsset(mContext.getAssets(), fontType);
            } catch(Exception ex) {
                typeFace = Typeface.create(fontType, Typeface.NORMAL);
            }
            font.setTypeface(typeFace);
        }

        textLayer.setFont(font);
        return textLayer;
    }

    public void addEntityAndPosition(MotionEntity entity) {
        if (entity != null) {
            if (mEntityBorderStyle == BorderStyle.DASHED) {
                // Make DashPathEffect work with drawLines (drawSelectedBg in MotionEntity)
                mDisableHardwareAccelerated = true;
                setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }

            initEntityBorder(entity);
            initialTranslateAndScale(entity);
            mEntities.add(entity);
            onShapeSelectionChanged(entity);
            selectEntity(entity);
        }
    }

    private void initEntityBorder(MotionEntity entity) {
        int strokeSize = Utility.convertDpToPx(mContext.getResources().getDisplayMetrics(), mEntityBorderStrokeWidth);
        Paint borderPaint = new Paint();
        borderPaint.setStrokeWidth(strokeSize);
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(mEntityBorderColor);
        entity.setBorderPaint(borderPaint);
        entity.setBorderStyle(mEntityBorderStyle);
    }

    private void drawAllEntities(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(mEntityStrokeColor);
        paint.setStrokeWidth(mEntityStrokeWidth);

        for (int i = 0; i < mEntities.size(); i++) {
            mEntities.get(i).draw(canvas, paint);
        }
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
                invalidateCanvas(true);
            }
        }
    }

    private void initialTranslateAndScale(MotionEntity entity) {
        entity.moveToCanvasCenter();
        entity.getLayer().setScale(entity.getLayer().initialScale());
    }

    private void selectEntity(MotionEntity entity) {
        if (mSelectedEntity != null) {
            mSelectedEntity.setIsSelected(false);
        }
        if (entity != null) {
            entity.setIsSelected(true);
        }
        mSelectedEntity = entity;
        invalidateCanvas(true);
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
        if (mEntities.remove(entity)) {
            mEntities.add(entity);
            invalidateCanvas(true);
        }
    }

    private void updateSelectionOnTap(MotionEvent e) {
        MotionEntity entity = findEntityAtPoint(e.getX(), e.getY());
        onShapeSelectionChanged(entity);
        selectEntity(entity);
    }

    private void updateOnLongPress(MotionEvent e) {
        if (mSelectedEntity != null) {
            PointF p = new PointF(e.getX(), e.getY());
            if (mSelectedEntity.pointInLayerRect(p)) {
                bringLayerToFront(mSelectedEntity);
            }
        }
    }

    public void releaseSelectedEntity() {
        MotionEntity toRemoveEntity = null;
        for (MotionEntity entity : mEntities) {
            if (entity.isSelected()) {
                toRemoveEntity = entity;
                break;
            }
        }
        if (toRemoveEntity != null) {
            if (mEntities.remove(toRemoveEntity)) {
                toRemoveEntity.release();
                toRemoveEntity = null;
                onShapeSelectionChanged(toRemoveEntity);
                invalidateCanvas(true);
            }
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
                mGestureDetectorCompat.onTouchEvent(event);
                mScaleGestureDetector.onTouchEvent(event);
                mRotateGestureDetector.onTouchEvent(event);
                mMoveGestureDetector.onTouchEvent(event);
                return true;
            } else {
              return false;
            }
        }
    };

    private class TapsListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mSelectedEntity != null) {
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
            // Update mSelectedEntity. 
            // Fires onShapeSelectionChanged (JS-PanResponder enabling/disabling)
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
                invalidateCanvas(true);
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
                invalidateCanvas(true);
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
