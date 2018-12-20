package com.myMinistry.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.navigation.NavigationView;
import com.myMinistry.R;
import com.myMinistry.fragments.EntryTypeManagerFrag;
import com.myMinistry.fragments.PublicationFragment;
import com.myMinistry.fragments.PublicationManagerFragment;
import com.myMinistry.fragments.PublishersFragment;
import com.myMinistry.fragments.TimeEditorFragment;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.ui.backups.BackupFragment;
import com.myMinistry.ui.householders.HouseholdersListFragment;
import com.myMinistry.ui.report.ReportSummaryFragment;
import com.myMinistry.utils.ActivityUtils;
import com.myMinistry.utils.HelpUtils;
import com.myMinistry.utils.PrefUtils;

import java.util.Calendar;
import java.util.Locale;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity {
    protected static final int NAVDRAWER_ITEM_TIME_ENTRY = 8;
    protected static final int NAVDRAWER_ITEM_PUBLICATION_MANAGER = 9;
    public static final int TIME_ENTRY_ID = NAVDRAWER_ITEM_TIME_ENTRY;
    public static final int PUBLICATION_MANAGER_ID = NAVDRAWER_ITEM_PUBLICATION_MANAGER;

    private DrawerLayout mDrawerLayout;

    private Boolean firstLoad = true;

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Set up the toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_menu); // Hamburger Icon instead of default back arrow

        // Set up the navigation drawer.
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.primary_dark);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        // Launch default fragment
        BackupFragment reportSummaryFragment = (BackupFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (reportSummaryFragment == null) {
            setTitle(R.string.navdrawer_item_report);
            // Create the fragment
            //reportSummaryFragment = new ReportSummaryFragment().newInstance(PrefUtils.getPublisherId(this));
            reportSummaryFragment = new BackupFragment().newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), reportSummaryFragment, R.id.contentFrame);
        }




        /*
        mProgressDialog = CommonUtils.showLoadingDialog(this);

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
        */
        // TODO confirm this needs to happen still and how
        if (HelpUtils.isApplicationUpdated(this)) {
            MinistryDatabase.getInstance(getApplicationContext()).getWritableDatabase();
            final ProgressDialog ringProgressDialog = ProgressDialog.show(this, getResources().getString(R.string.updating_app), getResources().getString(R.string.please_be_patient), true);
            ringProgressDialog.setCancelable(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        HelpUtils.doApplicationUpdatedWork(getApplicationContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ringProgressDialog.dismiss();
                }
            }).start();
        }
    }


    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        goToNavDrawerItem(menuItem.getItemId());
                        // Close the navigation drawer when an item is selected.
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // TODO Should this be used?
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        View mainContent = findViewById(R.id.contentFrame);
        if (mainContent != null) {
            int MAIN_CONTENT_FADEIN_DURATION = 250;
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);

        } else {
            Log.e("MainActivity", "No view with ID contentFrame to fade in.");
        }
    }

    public boolean goToNavDrawerItem(int itemId) {
        switch (itemId) {
            case R.id.drawer_report:
                Calendar date = Calendar.getInstance(Locale.getDefault());
                if (!(getSupportFragmentManager().findFragmentById(R.id.contentFrame) instanceof ReportSummaryFragment)) {
                    setTitle(R.string.navdrawer_item_report);

                    if (firstLoad) {
                        PrefUtils.setSummaryMonthAndYear(this, date);
                        firstLoad = false;
                    }

                    ActivityUtils.replaceFragmentForActivity(
                            getSupportFragmentManager()
                            , new ReportSummaryFragment().newInstance(PrefUtils.getPublisherId(this))
                            , R.id.contentFrame
                    );

                } else {
                    date.set(Calendar.MONTH, PrefUtils.getSummaryMonth(this, date));
                    date.set(Calendar.YEAR, PrefUtils.getSummaryYear(this, date));
                }

                return true;
            case R.id.drawer_publications:
                if (!(getSupportFragmentManager().findFragmentById(R.id.contentFrame) instanceof PublicationFragment)) {
                    setTitle(R.string.navdrawer_item_publications);

                    ActivityUtils.replaceFragmentForActivity(
                            getSupportFragmentManager()
                            , new PublicationFragment().newInstance()
                            , R.id.contentFrame
                    );
                }

                return true;
            case R.id.drawer_householders:
                setTitle(R.string.navdrawer_item_householders);

                if (!(getSupportFragmentManager().findFragmentById(R.id.contentFrame) instanceof HouseholdersListFragment)) {
                    ActivityUtils.replaceFragmentForActivity(
                            getSupportFragmentManager()
                            , new HouseholdersListFragment().newInstance()
                            , R.id.contentFrame
                    );
                }

                return true;
            case R.id.drawer_publishers:
                setTitle(R.string.navdrawer_item_publishers);

                if (!(getSupportFragmentManager().findFragmentById(R.id.contentFrame) instanceof PublishersFragment)) {
                    ActivityUtils.replaceFragmentForActivity(
                            getSupportFragmentManager()
                            , new PublishersFragment().newInstance()
                            , R.id.contentFrame
                    );
                }

                return true;
            case R.id.drawer_entry_types:
                setTitle(R.string.navdrawer_item_entry_types);

                if (!(getSupportFragmentManager().findFragmentById(R.id.contentFrame) instanceof EntryTypeManagerFrag)) {
                    ActivityUtils.replaceFragmentForActivity(
                            getSupportFragmentManager()
                            , new EntryTypeManagerFrag().newInstance()
                            , R.id.contentFrame
                    );
                }

                return true;
            case R.id.drawer_db:
                setTitle(R.string.navdrawer_item_backups);

                if (!(getSupportFragmentManager().findFragmentById(R.id.contentFrame) instanceof BackupFragment)) {
                    ActivityUtils.replaceFragmentForActivity(
                            getSupportFragmentManager()
                            , new BackupFragment().newInstance()
                            , R.id.contentFrame
                    );
                }

                return true;
            case R.id.drawer_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;

            case R.id.drawer_help:
                String url = getApplicationContext().getString(R.string.link_faqs);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);

                return true;
            case NAVDRAWER_ITEM_TIME_ENTRY:
                if (!(getSupportFragmentManager().findFragmentById(R.id.contentFrame) instanceof TimeEditorFragment)) {
                    ActivityUtils.replaceFragmentForActivity(
                            getSupportFragmentManager()
                            , new TimeEditorFragment().newInstanceForPublisher(PrefUtils.getPublisherId(this))
                            , R.id.contentFrame
                    );
                }

                return true;
            case NAVDRAWER_ITEM_PUBLICATION_MANAGER:
                if (!(getSupportFragmentManager().findFragmentById(R.id.contentFrame) instanceof PublicationManagerFragment)) {
                    ActivityUtils.replaceFragmentForActivity(
                            getSupportFragmentManager()
                            , new PublicationManagerFragment().newInstance()
                            , R.id.contentFrame
                    );
                }

                return true;
        }

        return false;
    }

    public void setPublisherId(int _ID, String _name) {
        goToNavDrawerItem(R.id.drawer_report);
    }
}