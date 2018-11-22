package org.cryse.lkong.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.cryse.lkong.R;
import org.cryse.lkong.application.LKongApplication;
import org.cryse.lkong.ui.common.AbstractSwipeBackActivity;
import org.cryse.lkong.utils.AnalyticsUtils;

import butterknife.ButterKnife;
import butterknife.BindView;

public class SettingsActivity extends AbstractSwipeBackActivity {
    public static final String LOG_TAG = SettingsActivity.class.getName();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        injectThis();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setUpToolbar(mToolbar);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        Fragment fragment = null;
        if(getIntent().hasExtra("type")) {
            if(getIntent().getStringExtra("type").compareTo("about") == 0) {
                fragment = new AboutFragment();
                this.setTitle(getString(R.string.settings_about_activity_title));
            }
        }
        if(fragment == null) {
            fragment = new SettingsFragment();
            this.setTitle(getString(R.string.drawer_item_settings));
        }
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.commit();

    }

    protected void setUpToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void injectThis() {
        LKongApplication.get(this).simpleActivityComponent().inject(this);
    }

    @Override
    protected void analyticsTrackEnter() {
        AnalyticsUtils.trackFragmentActivityEnter(this, LOG_TAG);
    }

    @Override
    protected void analyticsTrackExit() {
        AnalyticsUtils.trackFragmentActivityExit(this, LOG_TAG);
    }

    @Override
    public void onBackPressed() {
        if(!getSupportFragmentManager().popBackStackImmediate()) {
            this.finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                closeActivityWithTransition();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
