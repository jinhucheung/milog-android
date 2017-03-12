package me.hijinhu.milog.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.webkit.ValueCallback;

import com.basecamp.turbolinks.TurbolinksSession;
import com.basecamp.turbolinks.TurbolinksView;

import org.json.JSONException;
import org.json.JSONObject;

import me.hijinhu.milog.R;
import me.hijinhu.milog.widget.TurbolinksSwipeRefreshLayout;

/**
 * EmptyActivity : render tmp page and could back to index
 * Created by kumho on 17-1-31.
 */
public class EmptyActivity extends BaseActivity implements TurbolinksSwipeRefreshLayout.TurbolinksScrollUpCallback {

    private TurbolinksSwipeRefreshLayout mSwipeRefreshLayout;

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

        mSwipeRefreshLayout = (TurbolinksSwipeRefreshLayout) findViewById(R.id.swipeRefresh_layout);
        mSwipeRefreshLayout.setProgressViewOffset(true, 50, 200);
        mSwipeRefreshLayout.setCallback(this);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        TurbolinksSession.getDefault(EmptyActivity.this).visit(location);
                    }
                }
        );
    }

    @Override
    public void visitCompleted() {
        mSwipeRefreshLayout.setRefreshing(false);
        TurbolinksSession.getDefault(this).getWebView().evaluateJavascript(
                "$('meta[name=\"current-blog\"]').data()",
                new VisitCompletedCallback(this));
        super.visitCompleted();
    }

    @Override
    public boolean canChildScrollUp() {
        return TurbolinksSession.getDefault(this).getWebView().getScrollY() > 0;
    }

    class VisitCompletedCallback implements ValueCallback<String> {
        EmptyActivity mActivity;

        public VisitCompletedCallback(EmptyActivity activity) {
            mActivity = activity;
        }

        @Override
        public void onReceiveValue(String value) {
            if (DEBUG) {
                Log.d(TAG, value);
            }
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
