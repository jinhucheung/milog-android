package me.hijinhu.milog.activities;


import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
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
import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import me.hijinhu.milog.R;
import me.hijinhu.milog.utils.ToastUtil;

/**
 * MainActivity: Milog Index
 * <p>
 * Created by kumho on 17-1-15.
 */
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int TIME_WAIT_EXIT = 2500;
    private AtomicBoolean isExited = new AtomicBoolean(false);

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private NavigationView mNavigationView;
    private SimpleDraweeView mUserAvatarDraweeView;
    private TextView mUserNameTextView;
    private TextView mUserEmailTextView;

    private JSONObject mCurrentUserMeta;


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


        return true;
    }

    // -----------------------------------------------------------------------
    // NavigationView overrides
    // -----------------------------------------------------------------------
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.nav_sign_up:
                visitProposedToLocationWithAction(HOST_URL + "/signup", ACT_ACCOUNT);
                break;
            case R.id.nav_sign_in:
                visitProposedToLocationWithAction(HOST_URL + "/signin", ACT_ACCOUNT);
                break;
            case R.id.nav_sign_out:
                signOut();
                break;
            case R.id.nav_settings:
                visitProposedToLocationWithAction(HOST_URL + "/account/edit", ACT_ACCOUNT);
                break;
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // TurbolinksAdapter overrides
    // -----------------------------------------------------------------------

    @Override
    public void visitCompleted() {
        TurbolinksSession.getDefault(this).getWebView().evaluateJavascript(
                "$('meta[name=\"current-user\"]').data()",
                new VisitCompletedCallback(this));
        super.visitCompleted();
    }

    class VisitCompletedCallback implements ValueCallback<String> {
        MainActivity mActivity;

        public VisitCompletedCallback(MainActivity activity){
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
            mActivity.updateNavigationView();
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
    private void updateNavigationView(){
        Menu naviMenu = mNavigationView.getMenu();
        if (mCurrentUserMeta != null) {
            naviMenu.setGroupVisible(R.id.group_user, true);
            naviMenu.setGroupVisible(R.id.group_guest, false);

            try {
                mUserNameTextView.setText(mCurrentUserMeta.getString("username"));
                mUserEmailTextView.setText(mCurrentUserMeta.getString("email"));
                mUserAvatarDraweeView.setImageURI(mCurrentUserMeta.getString("avatarUrl"));
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
}
