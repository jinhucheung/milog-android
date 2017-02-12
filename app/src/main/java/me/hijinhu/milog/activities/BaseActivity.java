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

    protected static final String HOST_URL = Constants.HOST_URL;
    protected static final String INTENT_URL = "intentUrl";

    protected static final String ACT_ADVANCE = "advance";

    protected String location;
    protected TurbolinksView mTurbolinksView;

    private ValueCallback<Uri[]> mFilePathCallback;
    private boolean onSelectFileCallback = false;

    class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            mFilePathCallback = filePathCallback;
            startActivityForResult(fileChooserParams.createIntent(), Constants.REQUEST_SELECT_FILE);
            return true;
        }
    }

    // -----------------------------------------------------------------------
    // Activity overrides
    // -----------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        location = getIntent().getStringExtra(INTENT_URL) != null ? getIntent().getStringExtra(INTENT_URL) : HOST_URL;

        TurbolinksSession.getDefault(this)
                .activity(this)
                .adapter(this)
                .getWebView()
                .setWebChromeClient(new WebChromeClient());

        AppManager.getInstance().addActivity(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!onSelectFileCallback && mTurbolinksView != null) {
            TurbolinksSession.getDefault(this)
                    .activity(this)
                    .adapter(this)
                    .restoreWithCachedSnapshot(true)
                    .view(mTurbolinksView)
                    .visit(location);
        } else {
            onSelectFileCallback = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_SELECT_FILE:
                if (resultCode == RESULT_OK && data != null) {
                    mFilePathCallback.onReceiveValue(new Uri[] { data.getData() });
                } else {
                    mFilePathCallback.onReceiveValue(null);
                }
                onSelectFileCallback = true;
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
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
        Intent intent =  new Intent(this, EmptyActivity.class);
        intent.putExtra(INTENT_URL, location);
        this.startActivity(intent);
    }

    protected void handleError(int code) {
        Log.d(TAG, "handleError: " + code);
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------
    protected  void signOut() {
        TurbolinksSession.getDefault(this)
                         .getWebView()
                         .evaluateJavascript(
                                 "$.ajax({url: '/signout', method: 'DELETE'})",
                                 null);
    }
}
