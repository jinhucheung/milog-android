package me.hijinhu.milog.activities;

import android.app.Activity;
import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.ArrayList;
import java.util.List;

import me.hijinhu.milog.utils.ToastUtil;

/**
 * AppManager: Manage activity collection. eg. finish all activity
 *
 * Created by kumho on 17-1-15.
 */
public final class AppManager extends Application {

    private List<Activity> activities = new ArrayList<>();

    private static AppManager instance;

    public static AppManager getInstance() {
        return instance == null? instance = new AppManager() : instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        ToastUtil.initialize(this);
    }

    public void addActivity(Activity activity) {
        if (activity != null) { activities.add(activity); }
    }

    public boolean removeActivity(Activity activity) { return activities.remove(activity); }

    public void exit() {
        for(Activity activity: activities) { activity.finish(); }
        activities.clear();
        System.exit(0);
    }
}
