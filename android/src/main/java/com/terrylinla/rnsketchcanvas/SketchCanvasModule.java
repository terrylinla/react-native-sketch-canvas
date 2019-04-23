package com.terrylinla.rnsketchcanvas;

import android.annotation.TargetApi;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.uimanager.NativeViewHierarchyManager;
import com.facebook.react.uimanager.UIBlock;
import com.facebook.react.uimanager.UIManagerModule;

public class SketchCanvasModule extends ReactContextBaseJavaModule {
    SketchCanvasModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "SketchCanvasModule";
    }

    @ReactMethod
    public void transferToBase64(final int tag, final String type, final boolean transparent, 
        final boolean includeImage, final boolean includeText, final boolean cropToImageSize, final Callback callback){
        try {
            final ReactApplicationContext context = getReactApplicationContext();
            UIManagerModule uiManager = context.getNativeModule(UIManagerModule.class);
            uiManager.addUIBlock(new UIBlock() {
                public void execute(NativeViewHierarchyManager nvhm) {
                    SketchCanvas view = (SketchCanvas) nvhm.resolveView(tag);
                    view.getBase64(type, transparent, includeImage, includeText, cropToImageSize, callback);
                }
            });
        } catch (Exception e) {
            callback.invoke(e.getMessage(), null);
        }
    }

    @ReactMethod
    @TargetApi(19)
    public void isPointOnPath(final int tag, final int x, final int y, final int pathId, final Callback callback){
        try {
            final ReactApplicationContext context = getReactApplicationContext();
            UIManagerModule uiManager = context.getNativeModule(UIManagerModule.class);
            uiManager.addUIBlock(new UIBlock() {
                public void execute(NativeViewHierarchyManager nvhm) {
                    SketchCanvas view = (SketchCanvas) nvhm.resolveView(tag);
                    callback.invoke(null, pathId == -1? view.isPointOnPath(x, y): view.isPointOnPath(x, y, pathId));
                }
            });
        } catch (Exception e) {
            callback.invoke(e.getMessage(), null);
        }
    }

    @ReactMethod
    public void setTouchRadius(final int tag, final int r, final Callback callback){
        try {
            final ReactApplicationContext context = getReactApplicationContext();
            UIManagerModule uiManager = context.getNativeModule(UIManagerModule.class);
            uiManager.addUIBlock(new UIBlock() {
                public void execute(NativeViewHierarchyManager nvhm) {
                    SketchCanvas view = (SketchCanvas) nvhm.resolveView(tag);
                    view.setTouchRadius(r);
                    callback.invoke(null, true);
                }
            });
        } catch (Exception e) {
            callback.invoke(e.getMessage(), null);
        }
    }
}