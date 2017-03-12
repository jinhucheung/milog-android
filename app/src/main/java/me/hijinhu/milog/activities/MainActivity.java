package me.hijinhu.milog.activities;


import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.TextView;

import com.basecamp.turbolinks.TurbolinksSession;
import com.basecamp.turbolinks.TurbolinksView;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import me.hijinhu.milog.R;
import me.hijinhu.milog.utils.ToastUtil;
import me.hijinhu.milog.widget.TurbolinksSwipeRefreshLayout;


/**
 * MainActivity: Milog Index
 * <p/>
 * Created by kumho on 17-1-15.
 */
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener, TurbolinksSwipeRefreshLayout.TurbolinksScrollUpCallback {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int TIME_WAIT_EXIT = 2500;
    private AtomicBoolean isExited = new AtomicBoolean(false);

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private NavigationView mNavigationView;
    private SimpleDraweeView mUserAvatarDraweeView;
    private TextView mUserNameTextView;
    private TextView mUserEmailTextView;

    private TextView mNotificationTextView;

    private TurbolinksSwipeRefreshLayout mSwipeRefreshLayout;

    private JSONObject mCurrentUserMeta;

    private boolean mSearched = false;

    // -----------------------------------------------------------------------
    // Activity overrides
    // -----------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        View headerView = mNavigationView.getHeaderView(0);
        mUserAvatarDraweeView = (SimpleDraweeView) headerView.findViewById(R.id.user_avatar);
        mUserNameTextView = (TextView) headerView.findViewById(R.id.user_name);
        mUserEmailTextView = (TextView) headerView.findViewById(R.id.user_email);


        mTurbolinksView = (TurbolinksView) findViewById(R.id.turbolinks_view);

        location = HOST_URL + "/community";

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
                    TurbolinksSession.getDefault(MainActivity.this).visit(location);
                }
            }
        );
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        if (isExited.compareAndSet(false, true)) {
            ToastUtil.showShort("再按一次退出");
            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExited.set(false);
                }
            }, TIME_WAIT_EXIT);
        } else {
            this.finishAffinity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(this);
        MenuItemCompat.setOnActionExpandListener(searchItem, new SearchExpandListener(this));

        MenuItem notifyItem = menu.findItem(R.id.action_notification);
        mNotificationTextView = (TextView) MenuItemCompat.getActionView(notifyItem).findViewById(R.id.notification_text);
        return true;
    }

    // -----------------------------------------------------------------------
    // NavigationView overrides
    // -----------------------------------------------------------------------
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        try {
            switch (item.getItemId()) {
                case R.id.nav_sign_up:
                    visitProposedToLocationWithAction(HOST_URL + "/signup", ACT_ADVANCE);
                    break;
                case R.id.nav_sign_in:
                    visitProposedToLocationWithAction(HOST_URL + "/signin", ACT_ADVANCE);
                    break;
                case R.id.nav_sign_out:
                    signOut();
                    break;
                case R.id.nav_profiles:
                    visitProposedToLocationWithAction(HOST_URL + "/account/edit", ACT_ADVANCE);
                    break;
                case R.id.nav_userspace:
                    visitProposedToLocationWithAction(HOST_URL + "/" + mCurrentUserMeta.get("username"), ACT_ADVANCE);
                    break;
                case R.id.nav_blog:
                    visitProposedToLocationWithAction(HOST_URL + "/" + mCurrentUserMeta.get("username") + "/blog", ACT_ADVANCE);
                    break;
                case R.id.nav_drafts:
                    visitProposedToLocationWithAction(HOST_URL + "/" + mCurrentUserMeta.get("username") + "/drafts", ACT_ADVANCE);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void addArticle(View v) {
        visitProposedToLocationWithAction(HOST_URL + "/articles/new", ACT_ADVANCE);
    }

    public void visitNotification(View v) {
        visitProposedToLocationWithAction(HOST_URL + "/notifications", ACT_ADVANCE);
    }

    // -----------------------------------------------------------------------
    // TurbolinksAdapter overrides
    // -----------------------------------------------------------------------

    @Override
    public void visitCompleted() {
        mSwipeRefreshLayout.setRefreshing(false);
        TurbolinksSession.getDefault(this).getWebView().evaluateJavascript(
                "$('meta[name=\"current-user\"]').data()",
                new VisitCompletedCallback(this));
        super.visitCompleted();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public boolean canChildScrollUp() {
        return TurbolinksSession.getDefault(this).getWebView().getScrollY() > 0;
    }


    class VisitCompletedCallback implements ValueCallback<String> {
        MainActivity mActivity;

        public VisitCompletedCallback(MainActivity activity) {
            mActivity = activity;
        }

        @Override
        public void onReceiveValue(String value) {
            if (DEBUG) { Log.d(TAG, value); }
            try {
                if (value.equalsIgnoreCase("null")) {
                    mActivity.setCurrentUserMeta(null);
                } else {
                    mActivity.setCurrentUserMeta(new JSONObject(value));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mActivity.updateView();
        }
    }

    // -----------------------------------------------------------------------
    // Getter and Setter
    // -----------------------------------------------------------------------
    public void setCurrentUserMeta(JSONObject userMeta) {
        mCurrentUserMeta = userMeta;
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------
    private void updateView() {
        Menu naviMenu = mNavigationView.getMenu();
        if (mCurrentUserMeta != null) {
            naviMenu.setGroupVisible(R.id.group_user, true);
            naviMenu.setGroupVisible(R.id.group_guest, false);

            try {
                mUserNameTextView.setText(mCurrentUserMeta.getString("username"));
                mUserEmailTextView.setText(mCurrentUserMeta.getString("email"));
                mUserAvatarDraweeView.setImageURI(mCurrentUserMeta.getString("avatarUrl"));

                updateNotificationText(mCurrentUserMeta.getInt("notifyCount"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            naviMenu.setGroupVisible(R.id.group_user, false);
            naviMenu.setGroupVisible(R.id.group_guest, true);

            mUserNameTextView.setText("Guest");
            mUserEmailTextView.setText("guest@hijinhu.me");
            mUserAvatarDraweeView.setImageResource(R.drawable.ic_account_circle_white_48dp);
        }
    }

    public void updateNotificationText(final int unread_count) {
        if (mNotificationTextView == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (unread_count == 0)
                    mNotificationTextView.setVisibility(View.INVISIBLE);
                else {
                    mNotificationTextView.setVisibility(View.VISIBLE);
                    mNotificationTextView.setText(Integer.toString(unread_count));
                }
            }
        });
    }

    // -----------------------------------------------------------------------
    // Search
    // -----------------------------------------------------------------------
    class SearchExpandListener implements MenuItemCompat.OnActionExpandListener {
        private MainActivity mActivity;

        public SearchExpandListener(MainActivity activity) {
            mActivity = activity;
        }

        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            if (mSearched) {
                mActivity.searchCLose();
            }
            return true;
        }
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        try {
            mSearched = true;
            location = HOST_URL + "/community/search?token=" + URLEncoder.encode(query, "UTF-8");
            TurbolinksSession.getDefault(this).visit(location);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void searchCLose() {
        location = HOST_URL + "/community";
        TurbolinksSession.getDefault(this).visit(location);
        mSearched = false;
    }
}
