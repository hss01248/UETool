package me.ele.uetool;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import com.zy.logcat.LogcatDialogUtil;
import me.ele.uetool.base.Application;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class UETool {

    private static volatile UETool instance;
    private Set<String> filterClassesSet = new HashSet<>();
    private Set<String> attrsProviderSet = new LinkedHashSet<String>() {
        {
            add(UETCore.class.getName());
            add("me.ele.uetool.fresco.UETFresco");
        }
    };
    private Activity targetActivity;
    private UETMenu uetMenu;

    private UETool() {

    }

    static UETool getInstance() {
        if (instance == null) {
            synchronized (UETool.class) {
                if (instance == null) {
                    instance = new UETool();
                }
            }
        }
        return instance;
    }

    public static void init(android.app.Application application){
        LogcatDialogUtil.init(application);
        application.registerActivityLifecycleCallbacks(new android.app.Application.ActivityLifecycleCallbacks() {

            private int visibleActivityCount;
            private int uetoolDismissY = 10;

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                visibleActivityCount++;
                if (visibleActivityCount == 1 && uetoolDismissY >= 0) {
                    UETool.showUETMenu(uetoolDismissY);
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                visibleActivityCount--;
                if (visibleActivityCount == 0) {
                    uetoolDismissY = UETool.dismissUETMenu();
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

    }

    public static void putFilterClass(Class clazz) {
        putFilterClass(clazz.getName());
    }

    public static void putFilterClass(String className) {
        getInstance().putFilterClassName(className);
    }

    public static void putAttrsProviderClass(Class clazz) {
        putAttrsProviderClass(clazz.getName());
    }

    public static void putAttrsProviderClass(String className) {
        getInstance().putAttrsProviderClassName(className);
    }

    public static boolean showUETMenu() {
        return getInstance().showMenu();
    }

    public static boolean showUETMenu(int y) {
        return getInstance().showMenu(y);
    }

    public static int dismissUETMenu() {
        return getInstance().dismissMenu();
    }

    private void putFilterClassName(String className) {
        filterClassesSet.add(className);
    }

    private void putAttrsProviderClassName(String className) {
        attrsProviderSet.add(className);
    }

    private boolean showMenu() {
        return showMenu(10);
    }

    private boolean showMenu(int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(Application.getApplicationContext())) {
                requestPermission(Application.getApplicationContext());
                Toast.makeText(Application.getApplicationContext(), "After grant this permission, re-enable UETool", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        if (uetMenu == null) {
            uetMenu = new UETMenu(Application.getApplicationContext(), y);
        }
        uetMenu.show();
        return true;
    }

    private int dismissMenu() {
        if (uetMenu != null) {
            int y = uetMenu.dismiss();
            uetMenu = null;
            return y;
        }
        return -1;
    }

    public Set<String> getFilterClasses() {
        return filterClassesSet;
    }

    public Activity getTargetActivity() {
        return targetActivity;
    }

    public void setTargetActivity(Activity targetActivity) {
        this.targetActivity = targetActivity;
    }

    public Set<String> getAttrsProvider() {
        return attrsProviderSet;
    }

    void release() {
        targetActivity = null;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }




}
