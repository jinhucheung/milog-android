package me.hijinhu.milog.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.basecamp.turbolinks.TurbolinksSession;
import com.basecamp.turbolinks.TurbolinksView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

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

}
