package me.hijinhu.milog.activities;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.basecamp.turbolinks.TurbolinksAdapter;
import com.basecamp.turbolinks.TurbolinksSession;
import com.basecamp.turbolinks.TurbolinksView;

import me.hijinhu.milog.Constants;


/**
 * BaseActivity : Implement Turbolinks-Android
 *
 * Created by kumho on 17-1-8.
 */
public class BaseActivity extends AppCompatActivity implements TurbolinksAdapter {
    protected static final boolean DEBUG = Constants.DEBUG;
    protected static String TAG = BaseActivity.class.getSimpleName();

    protected static final String BASE_URL = Constants.HOST_URL;
    protected static final String INTENT_URL = "intentUrl";

    protected String location;
    protected TurbolinksView mturbolinksView;

    // -----------------------------------------------------------------------
    // Activity overrides
    // -----------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        location = getIntent().getStringExtra(INTENT_URL) != null ? getIntent().getStringExtra(INTENT_URL) : BASE_URL;

        TurbolinksSession.getDefault(this)
                .activity(this)
                .adapter(this);

        AppManager.getInstance().addActivity(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        TurbolinksSession.getDefault(this)
                .activity(this)
                .adapter(this)
                .restoreWithCachedSnapshot(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // -----------------------------------------------------------------------
    // TurbolinksAdapter overrides
    // -----------------------------------------------------------------------

    @Override
    public void onPageFinished() {

    }

    @Override
    public void onReceivedError(int errorCode) {
        handleError(errorCode);
    }

    @Override
    public void pageInvalidated() {

    }

    @Override
    public void requestFailedWithStatusCode(int statusCode) {
        handleError(statusCode);
    }

    @Override
    public void visitCompleted() {

    }

    @Override
    public void visitProposedToLocationWithAction(String location, String action) {
        Intent intent =  new Intent(this, MainActivity.class);
        intent.putExtra(INTENT_URL, location);
        this.startActivity(intent);
    }

    protected void handleError(int code) {
        Log.d(TAG, "handleError: " + code);
    }
}
