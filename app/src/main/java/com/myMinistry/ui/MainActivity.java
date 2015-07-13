package com.myMinistry.ui;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.myMinistry.R;
import com.myMinistry.fragments.DBBackupsFragment;
import com.myMinistry.fragments.DBBackupsListFragment;
import com.myMinistry.fragments.DBScheduleFragment;
import com.myMinistry.fragments.EntryTypeManagerFrag;
import com.myMinistry.fragments.HouseholdersFragment;
import com.myMinistry.fragments.PublicationFragment;
import com.myMinistry.fragments.PublicationManagerFrag;
import com.myMinistry.fragments.PublishersFragment;
import com.myMinistry.fragments.SummaryFragment;
import com.myMinistry.fragments.TimeEditorFragment;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.util.HelpUtils;
import com.myMinistry.util.PrefUtils;

import java.util.Calendar;
import java.util.Locale;

import static com.myMinistry.util.LogUtils.LOGE;
import static com.myMinistry.util.LogUtils.makeLogTag;

public class MainActivity extends AppCompatActivity implements TabListener {
    private static final String TAG = makeLogTag(MainActivity.class);

    private boolean execute_tab = false;
    private boolean is_dual_pane = false;

    private Toolbar toolbar;

    private ViewGroup mDrawerItemsListContainer;

    // symbols for navdrawer items (indices must correspond to array below). This is
    // not a list of items that are necessarily *present* in the Nav Drawer; rather,
    // it's a list of all possible items.
    protected static final int NAVDRAWER_ITEM_TIME_ENTRY = 8;

    public static final int TIME_ENTRY_ID = NAVDRAWER_ITEM_TIME_ENTRY;

    // fade in and fade out durations for the main content when switching between
    // different Activities of the app through the Nav Drawer
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    private FragmentManager fm;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private Boolean firstLoad = true;

    private int getDefaultNavDrawerItem() { return R.id.drawer_entry_types; }

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

        is_dual_pane = findViewById(R.id.secondary_fragment_container) != null;

        initToolbar();
        setupDrawerLayout();

        // Default item selected
        goToNavDrawerItem(getDefaultNavDrawerItem());

        if(HelpUtils.isApplicationUpdated(this)) {
            MinistryDatabase.getInstance(getApplicationContext()).getWritableDatabase();
            final ProgressDialog ringProgressDialog = ProgressDialog.show(this, getResources().getString(R.string.updating_app), getResources().getString(R.string.please_be_patient), true);
            ringProgressDialog.setCancelable(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        HelpUtils.doApplicationUpdatedWork(getApplicationContext());
                    } catch (Exception e) {

                    }
                    ringProgressDialog.dismiss();
                }
            }).start();
        }
    }

    private void setupDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
                // Handle menu item clicks here.
                menuItem.setChecked(true);
                setTitle(menuItem.getTitle());
                goToNavDrawerItem(menuItem.getItemId());
                mDrawerLayout.closeDrawers();  // CLOSE DRAWER
                return true;
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                //invalidateOptionsMenu();
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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            // Defaults to Summary on launch. Changes with navigation selection afterwards.
            actionBar.setTitle(R.string.navdrawer_item_summary);
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
    public void setTitle(CharSequence title)
    {
        getSupportActionBar().setTitle(title);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            View mainContent = findViewById(R.id.primary_fragment_container);
            if (mainContent != null) {
                mainContent.setAlpha(0);
                mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
            } else {
                LOGE(TAG, "No view with ID primary_fragment_container to fade in.");
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(mDrawerToggle != null)
            mDrawerToggle.onConfigurationChanged(newConfig); // Pass any configuration change to the drawer toggles
    }


    public boolean goToNavDrawerItem(int itemId) {
        execute_tab = false;

        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);

        boolean is_dual_pane = findViewById(R.id.secondary_fragment_container) != null;

        switch (itemId) {
            case R.id.drawer_summary:
                Calendar date = Calendar.getInstance(Locale.getDefault());
                if(!(frag instanceof SummaryFragment)) {
                    if(firstLoad)
                        PrefUtils.setSummaryMonthAndYear(this, date);

                    new SummaryFragment();
                    SummaryFragment f = SummaryFragment.newInstance(PrefUtils.getPublisherId(this));
                    // test
                    ft.replace(R.id.primary_fragment_container, f);

                    if(!firstLoad)
                        ft.addToBackStack(null);

                    ft.commit();
                }
                else {
                    date.set(Calendar.MONTH, PrefUtils.getSummaryMonth(this, date));
                    date.set(Calendar.YEAR, PrefUtils.getSummaryYear(this, date));

                    SummaryFragment f = (SummaryFragment) fm.findFragmentById(R.id.primary_fragment_container);
                    f.setPublisherId(PrefUtils.getPublisherId(this));
                    f.setDate(date);
                    f.calculateSummaryValues();
                    f.refresh(SummaryFragment.DIRECTION_NO_CHANGE);
                }

                firstLoad = false;
                return true;
            case R.id.drawer_publications:
                //getSupportActionBar().removeAllTabs();

                //getSupportActionBar().addTab(getSupportActionBar().newTab().setText(R.string.tab_item_publications).setTabListener(this));
                //getSupportActionBar().addTab(getSupportActionBar().newTab().setText(R.string.tab_item_publication_types).setTabListener(this));

                if(!(frag instanceof PublicationFragment)) {
                    PublicationFragment f = new PublicationFragment().newInstance();

                    if(frag != null)
                        ft.remove(frag);

                    ft.add(R.id.primary_fragment_container, f);
                    ft.addToBackStack(null);

                    ft.commit();
                }

                return true;
            case R.id.drawer_householders:
                //getSupportActionBar().removeAllTabs();

                if(!is_dual_pane) {
                    //getSupportActionBar().addTab(getSupportActionBar().newTab().setText(R.string.tab_item_householders).setTabListener(this));
                }

                if(!(frag instanceof HouseholdersFragment)) {
                    HouseholdersFragment f = new HouseholdersFragment().newInstance();

                    if(frag != null)
                        ft.remove(frag);

                    ft.add(R.id.primary_fragment_container, f);
                    ft.addToBackStack(null);

                    ft.commit();
                }

                return true;
            case R.id.drawer_publishers:
                //getSupportActionBar().removeAllTabs();

                if(!is_dual_pane) {
                    //getSupportActionBar().addTab(getSupportActionBar().newTab().setText(R.string.tab_item_publishers).setTabListener(this));
                }

                if(!(frag instanceof PublishersFragment)) {
                    PublishersFragment f = new PublishersFragment().newInstance();

                    if(frag != null)
                        ft.remove(frag);

                    ft.add(R.id.primary_fragment_container, f);
                    ft.addToBackStack(null);

                    ft.commit();
                }

                return true;
            case R.id.drawer_entry_types:
                //getSupportActionBar().removeAllTabs();

                if(!is_dual_pane) {
                    //getSupportActionBar().addTab(getSupportActionBar().newTab().setText(R.string.tab_item_entry_types).setTabListener(this));
                }

                if(!(frag instanceof EntryTypeManagerFrag)) {
                    EntryTypeManagerFrag f = new EntryTypeManagerFrag().newInstance();

                    if(frag != null)
                        ft.remove(frag);

                    ft.add(R.id.primary_fragment_container, f);
                    ft.addToBackStack(null);

                    ft.commit();
                }

                return true;
            case R.id.drawer_db:
                //getSupportActionBar().removeAllTabs();

                //getSupportActionBar().addTab(getSupportActionBar().newTab().setText(R.string.tab_item_backups).setTabListener(this));
                //getSupportActionBar().addTab(getSupportActionBar().newTab().setText(R.string.tab_item_schedule_backups).setTabListener(this));

                if(!(frag instanceof DBBackupsFragment)) {
                    if(is_dual_pane) {
                        DBBackupsFragment f = new DBBackupsFragment().newInstance();

                        if(frag != null)
                            ft.remove(frag);

                        ft.add(R.id.primary_fragment_container, f);
                        ft.addToBackStack(null);

                        ft.commit();
                    }
                    else {
                        DBBackupsListFragment f = new DBBackupsListFragment().newInstance();

                        if(frag != null)
                            ft.remove(frag);

                        ft.add(R.id.primary_fragment_container, f);
                        ft.addToBackStack(null);

                        ft.commit();
                    }
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
                int LAYOUT_ID = (is_dual_pane) ? R.id.secondary_fragment_container : R.id.primary_fragment_container;

                frag = fm.findFragmentById(LAYOUT_ID);
                TimeEditorFragment f = new TimeEditorFragment().newInstanceForPublisher(PrefUtils.getPublisherId(this));

                ft.replace(LAYOUT_ID, f);

                ft.commit();

                return true;
        }

        return false;
    }

    public void setPublisherId(int _ID,String _name) {
        goToNavDrawerItem(getDefaultNavDrawerItem());
    }
/*
    @Override
    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }
*/
    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) { }
    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) { }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        if(execute_tab) {
            if(fm.findFragmentById(R.id.primary_fragment_container) != null) {
                if(tab.getText().equals(getApplicationContext().getResources().getString(R.string.tab_item_publications))) {
                    Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
                    PublicationFragment f = new PublicationFragment().newInstance();;
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                    if(frag != null)
                        ft.remove(frag);

                    ft.add(R.id.primary_fragment_container, f);
                } else if(tab.getText().equals(getApplicationContext().getResources().getString(R.string.tab_item_publication_types))) {
                    Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
                    PublicationManagerFrag f = new PublicationManagerFrag().newInstance();;
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                    if(frag != null)
                        ft.remove(frag);

                    ft.add(R.id.primary_fragment_container, f);
                } else if(tab.getText().equals(getApplicationContext().getResources().getString(R.string.tab_item_backups))) {
                    int LAYOUT_ID = (is_dual_pane) ? R.id.secondary_fragment_container : R.id.primary_fragment_container;

                    Fragment frag = fm.findFragmentById(LAYOUT_ID);
                    DBBackupsListFragment f = new DBBackupsListFragment().newInstance();;
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                    if(frag != null)
                        ft.remove(frag);

                    ft.add(LAYOUT_ID, f);
                } else if(tab.getText().equals(getApplicationContext().getResources().getString(R.string.tab_item_schedule_backups))) {
                    int LAYOUT_ID = (is_dual_pane) ? R.id.secondary_fragment_container : R.id.primary_fragment_container;

                    Fragment frag = fm.findFragmentById(LAYOUT_ID);
                    DBScheduleFragment f = new DBScheduleFragment().newInstance();;
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                    if(frag != null)
                        ft.remove(frag);

                    ft.add(LAYOUT_ID, f);
                }
            }
        }

        execute_tab = true;
    }

    public void changeLang(String lang) {
        if (lang.equalsIgnoreCase(""))
            return;
        Locale myLocale = new Locale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
}