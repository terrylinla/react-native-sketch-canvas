package com.terrylinla.rnsketchcanvas;

import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class SketchCanvasModule extends ReactContextBaseJavaModule {
    SketchCanvasModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "SketchCanvasModule";
    }

    @ReactMethod
    public void transferToBase64(int tag, String type, boolean transparent, Callback callback){
        try {
            String base64 = SketchCanvasManager.Canvas.getBase64(type, transparent);
            callback.invoke(null, base64);
        } catch (Exception e) {
            callback.invoke(e.getMessage(), null);
        }
    }
}