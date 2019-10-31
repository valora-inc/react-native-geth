package com.reactnativegeth;

/**
 * Created by yaska on 17-09-29.
 */

import android.util.Log;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RNGethPackage implements ReactPackage {

    private GethHolder gethHolder;

    public RNGethPackage() {
        this.gethHolder = new GethHolder();
    }

    public List<Class<? extends JavaScriptModule>> createJSModules() {
        Log.w("RNGeth", "createJSModules");
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        Log.w("RNGeth", "createViewManagers");
        return Collections.emptyList();
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        gethHolder.setReactContext(reactContext);
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new RNGethModule(reactContext, gethHolder));
        return modules;
    }

}
