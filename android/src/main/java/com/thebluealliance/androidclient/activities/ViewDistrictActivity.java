package com.thebluealliance.androidclient.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;

import com.thebluealliance.androidclient.NfcUris;
import com.thebluealliance.androidclient.R;
import com.thebluealliance.androidclient.Utilities;
import com.thebluealliance.androidclient.adapters.ViewDistrictFragmentPagerAdapter;
import com.thebluealliance.androidclient.eventbus.ActionBarTitleEvent;
import com.thebluealliance.androidclient.helpers.ConnectionDetector;
import com.thebluealliance.androidclient.helpers.DistrictHelper;
import com.thebluealliance.androidclient.types.ModelType;
import com.thebluealliance.androidclient.views.SlidingTabs;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ViewDistrictActivity extends MyTBASettingsActivity
  implements ViewPager.OnPageChangeListener {

    public static final String DISTRICT_ABBREV = "districtKey";
    public static final String YEAR = "year";

    private String mDistrictKey;
    private int mYear;

    public static Intent newInstance(Context c, String districtAbbrev, int year) {
        Intent intent = new Intent(c, ViewDistrictActivity.class);
        intent.putExtra(DISTRICT_ABBREV, districtAbbrev);
        intent.putExtra(YEAR, year);
        return intent;
    }

    public static Intent newInstance(Context c, String districtKey) {
        int year = Integer.parseInt(districtKey.substring(0, 4));
        String abbrev = districtKey.substring(4);
        Intent intent = new Intent(c, ViewDistrictActivity.class);
        intent.putExtra(DISTRICT_ABBREV, abbrev);
        intent.putExtra(YEAR, year);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String districtAbbrev;
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(DISTRICT_ABBREV)) {
            districtAbbrev = getIntent().getExtras().getString(DISTRICT_ABBREV, "");
        } else {
            throw new IllegalArgumentException("ViewDistrictActivity must be constructed with a key");
        }
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(YEAR)) {
            mYear = getIntent().getExtras().getInt(YEAR, -1);
        } else {
            throw new IllegalArgumentException("ViewDistrictActivity must be constructed with a year");
        }

        mDistrictKey = DistrictHelper.generateKey(districtAbbrev, mYear);
        setModelKey(mDistrictKey, ModelType.DISTRICT);
        setContentView(R.layout.activity_view_district);

        ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
        ViewDistrictFragmentPagerAdapter adapter = new ViewDistrictFragmentPagerAdapter(getSupportFragmentManager(), mDistrictKey);
        pager.setAdapter(adapter);
        // To support refreshing, all pages must be held in memory at once
        // This should be increased if we ever add more pages
        pager.setOffscreenPageLimit(10);
        pager.setPageMargin(Utilities.getPixelsFromDp(this, 16));

        SlidingTabs tabs = (SlidingTabs) findViewById(R.id.tabs);
        tabs.setViewPager(pager);
        tabs.setOnPageChangeListener(this);
        ViewCompat.setElevation(tabs, getResources().getDimension(R.dimen.toolbar_elevation));

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setupActionBar();

        if (!ConnectionDetector.isConnectedToInternet(this)) {
            showWarningMessage(BaseActivity.WARNING_OFFLINE);
        }

        setBeamUri(String.format(NfcUris.URI_DISTRICT, districtAbbrev));

        setSettingsToolbarTitle("District Settings");
    }

    @Override
    public void onCreateNavigationDrawer() {
        useActionBarToggle(false);
        encourageLearning(false);
    }

    private void setupActionBar() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            setActionBarTitle("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionsMenu = menu;
        getMenuInflater().inflate(R.menu.district_point_math, menu);
        menu.findItem(R.id.points_help).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.points_help:
                Utilities.showHelpDialog(this, R.raw.district_points_help, getString(R.string.district_points_help));
                return true;
            case android.R.id.home:
                if (isDrawerOpen()) {
                    closeDrawer();
                    return true;
                }

                // If this tasks exists in the back stack, it will be brought to the front and all other activities
                // will be destroyed. HomeActivity will be delivered this intent via onNewIntent().
                startActivity(HomeActivity.newInstance(this, R.id.nav_item_districts).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (mOptionsMenu != null) {
            MenuItem pointsHelp = mOptionsMenu.findItem(R.id.points_help);
            if (position == 1) {
                pointsHelp.setVisible(true);
            } else {
                pointsHelp.setVisible(false);
            }
        }

        // hide the FAB if we aren't on the first page
        if (position != 0) {
            hideFab(true);
        } else {
            syncFabVisibilityWithMyTbaEnabled(true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActionBarTitleUpdated(ActionBarTitleEvent event) {
        setActionBarTitle(event.getTitle());
    }
}
