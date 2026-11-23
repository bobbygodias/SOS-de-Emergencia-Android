package org.enterprise.sostoque;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
public final class SosApp extends Application {
    static int visibleActivities;
    @Override public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            public void onActivityStarted(Activity a) { visibleActivities++; OverlayService.refreshVisibility(); }
            public void onActivityStopped(Activity a) { visibleActivities=Math.max(0,visibleActivities-1); OverlayService.refreshVisibility(); }
            public void onActivityCreated(Activity a, Bundle b) {}
            public void onActivityResumed(Activity a) {}
            public void onActivityPaused(Activity a) {}
            public void onActivitySaveInstanceState(Activity a, Bundle b) {}
            public void onActivityDestroyed(Activity a) {}
        });
    }
}
