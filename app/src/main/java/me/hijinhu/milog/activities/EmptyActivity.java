package me.hijinhu.milog.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.webkit.ValueCallback;

import com.basecamp.turbolinks.TurbolinksSession;
import com.basecamp.turbolinks.TurbolinksView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import me.hijinhu.milog.R;

/**
 * EmptyActivity : render tmp page and could back to index
 * Created by kumho on 17-1-31.
 */
public class EmptyActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.empty_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        mTurbolinksView = (TurbolinksView) findViewById(R.id.empty_turbolinks_view);

        TurbolinksSession.getDefault(this)
                .view(mTurbolinksView)
                .visit(location);
    }

    @Override
    public void visitCompleted() {
        TurbolinksSession.getDefault(this).getWebView().evaluateJavascript(
                "$('meta[name=\"current-blog\"]').data()",
                new VisitCompletedCallback(this));
        super.visitCompleted();
    }

    class VisitCompletedCallback implements ValueCallback<String> {
        EmptyActivity mActivity;

        public VisitCompletedCallback(EmptyActivity activity){
            mActivity = activity;
        }

        @Override
        public void onReceiveValue(String value) {
            if (DEBUG) { Log.d(TAG, value); }
            try {
                if (!value.equalsIgnoreCase("null")) {
                    JSONObject currentBlogMeta = new JSONObject(value);
                    String title = currentBlogMeta.getString("title");
                    mActivity.setTitle(title);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
