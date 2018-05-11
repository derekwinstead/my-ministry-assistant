package com.myMinistry.ui;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.myMinistry.R;
import com.myMinistry.fragments.DBBackupsListFragment;
import com.myMinistry.fragments.EntryTypeManagerFrag;
import com.myMinistry.ui.householders.HouseholdersListFragment;
import com.myMinistry.fragments.PublicationFragment;
import com.myMinistry.fragments.PublicationManagerFragment;
import com.myMinistry.fragments.PublishersFragment;
import com.myMinistry.ui.report.ReportSummaryFragment;
import com.myMinistry.fragments.TimeEditorFragment;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.utils.HelpUtils;
import com.myMinistry.utils.PrefUtils;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;

    protected static final int NAVDRAWER_ITEM_TIME_ENTRY = 8;
    protected static final int NAVDRAWER_ITEM_PUBLICATION_MANAGER = 9;
    public static final int TIME_ENTRY_ID = NAVDRAWER_ITEM_TIME_ENTRY;
    public static final int PUBLICATION_MANAGER_ID = NAVDRAWER_ITEM_PUBLICATION_MANAGER;
    private final int MAIN_CONTENT_FADEIN_DURATION = 250;

    private FragmentManager fm;

    private ProgressDialog mProgressDialog;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private Boolean firstLoad = true;

    private int getDefaultNavDrawerItem() {
        return R.id.drawer_report;
    }

    @Override
    public void onResume() {
        super.onResume();

        changeLang(PrefUtils.getLocale(getApplicationContext()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        changeLang(PrefUtils.getLocale(getApplicationContext()));

        fm = getSupportFragmentManager();

        initToolbar();
        setupDrawerLayout();

        // Default item selected
        goToNavDrawerItem(getDefaultNavDrawerItem());

/*
        mProgressDialog = CommonUtils.showLoadingDialog(this);

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
*/
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

    private void setupDrawerLayout() {
        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView view = findViewById(R.id.navigation_view);
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Handle menu item clicks here.
                menuItem.setChecked(true);
                setTitle(menuItem.getTitle());
                goToNavDrawerItem(menuItem.getItemId());
                mDrawerLayout.closeDrawers();  // CLOSE DRAWER
                return true;
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white));
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            // Defaults to Summary on launch. Changes with navigation selection afterwards.
            actionBar.setTitle(R.string.navdrawer_item_report);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();

        View mainContent = findViewById(R.id.primary_fragment_container);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        } else {
            Log.e("MainActivity", "No view with ID primary_fragment_container to fade in.");
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null)
            mDrawerToggle.onConfigurationChanged(newConfig); // Pass any configuration change to the drawer toggles
    }


    public boolean goToNavDrawerItem(int itemId) {
        Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);

        // TODO put a condition around this to not always set the visibility and only do it if the backup item was selected and then it changed.
        findViewById(R.id.primary_fragment_container).setVisibility(View.VISIBLE);

        switch (itemId) {
            case R.id.drawer_report:
                Calendar date = Calendar.getInstance(Locale.getDefault());

                if (!(frag instanceof ReportSummaryFragment)) {
                    if (firstLoad)
                        PrefUtils.setSummaryMonthAndYear(this, date);

                    ReportSummaryFragment f = new ReportSummaryFragment().newInstance(PrefUtils.getPublisherId(this));
                    FragmentTransaction transaction = fm.beginTransaction();

                    transaction.replace(R.id.primary_fragment_container, f, "main");
                    transaction.commit();
                } else {
                    date.set(Calendar.MONTH, PrefUtils.getSummaryMonth(this, date));
                    date.set(Calendar.YEAR, PrefUtils.getSummaryYear(this, date));
                }

                firstLoad = false;
                return true;
            case R.id.drawer_publications:
                if (!(frag instanceof PublicationFragment)) {
                    PublicationFragment f = new PublicationFragment().newInstance();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.primary_fragment_container, f, "main");
                    transaction.commit();
                }

                return true;
            case R.id.drawer_householders:
                if (!(frag instanceof HouseholdersListFragment)) {
                    HouseholdersListFragment f = new HouseholdersListFragment().newInstance();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.primary_fragment_container, f, "main");
                    transaction.commit();
                }

                return true;
            case R.id.drawer_publishers:
                if (!(frag instanceof PublishersFragment)) {
                    PublishersFragment f = new PublishersFragment().newInstance();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.primary_fragment_container, f, "main");
                    transaction.commit();
                }

                return true;
            case R.id.drawer_entry_types:
                if (!(frag instanceof EntryTypeManagerFrag)) {
                    EntryTypeManagerFrag f = new EntryTypeManagerFrag().newInstance();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.primary_fragment_container, f, "main");
                    transaction.commit();
                }

                return true;
            case R.id.drawer_db:
                if (!(frag instanceof DBBackupsListFragment)) {
                    DBBackupsListFragment f = new DBBackupsListFragment().newInstance();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.primary_fragment_container, f, "main");
                    transaction.commit();
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
                if (!(frag instanceof TimeEditorFragment)) {
                    TimeEditorFragment f = new TimeEditorFragment().newInstanceForPublisher(PrefUtils.getPublisherId(this));
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.primary_fragment_container, f, "main");
                    transaction.commit();
                }

                return true;
            case NAVDRAWER_ITEM_PUBLICATION_MANAGER:
                if (!(frag instanceof PublicationManagerFragment)) {
                    PublicationManagerFragment f1 = new PublicationManagerFragment().newInstance();
                    FragmentTransaction transaction1 = fm.beginTransaction();
                    transaction1.replace(R.id.primary_fragment_container, f1, "main");
                    transaction1.commit();
                }

                return true;
        }

        return false;
    }

    public void setPublisherId(int _ID, String _name) {
        goToNavDrawerItem(getDefaultNavDrawerItem());
    }

    @SuppressWarnings("deprecation")
    public void changeLang(String lang) {
        if (lang.equalsIgnoreCase(""))
            return;
        Locale myLocale = new Locale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(myLocale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
}