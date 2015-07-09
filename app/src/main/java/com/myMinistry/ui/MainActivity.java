package com.myMinistry.ui;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.myMinistry.FragmentActivityStatus;
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
import com.myMinistry.util.UIUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static com.myMinistry.util.LogUtils.LOGE;
import static com.myMinistry.util.LogUtils.makeLogTag;

public class MainActivity extends AppCompatActivity implements FragmentActivityStatus, TabListener {
    private static final String TAG = makeLogTag(MainActivity.class);

    private boolean execute_tab = false;
    private boolean is_dual_pane = false;

    private Toolbar toolbar;

    private Handler mHandler;

    private ViewGroup mDrawerItemsListContainer;

    // symbols for navdrawer items (indices must correspond to array below). This is
    // not a list of items that are necessarily *present* in the Nav Drawer; rather,
    // it's a list of all possible items.
    protected static final int NAVDRAWER_ITEM_SUMMARY = 0;
    protected static final int NAVDRAWER_ITEM_PUBLICATIONS = 1;
    protected static final int NAVDRAWER_ITEM_HOUSEHOLDERS = 2;
    protected static final int NAVDRAWER_ITEM_PUBLISHERS = 3;
    protected static final int NAVDRAWER_ITEM_ENTRY_TYPES = 4;
    protected static final int NAVDRAWER_ITEM_BACKUPS = 5;
    protected static final int NAVDRAWER_ITEM_SETTINGS = 6;
    protected static final int NAVDRAWER_ITEM_HELP = 7;
    protected static final int NAVDRAWER_ITEM_TIME_ENTRY = 8;
    protected static final int NAVDRAWER_ITEM_INVALID = -1;
    protected static final int NAVDRAWER_ITEM_SEPARATOR = -2;

    public static final int SUMMARY_ID = NAVDRAWER_ITEM_SUMMARY;
    public static final int TIME_ENTRY_ID = NAVDRAWER_ITEM_TIME_ENTRY;

    protected static final int NAVDRAWER_ITEM_DEFAULT = NAVDRAWER_ITEM_PUBLICATIONS;//NAVDRAWER_ITEM_SUMMARY;

    // titles for navdrawer items (indices must correspond to the above)
   private static final int[] NAVDRAWER_TITLE_RES_ID = new int[]{
            R.string.navdrawer_item_summary,
            R.string.navdrawer_item_publications,
            R.string.navdrawer_item_householders,
            R.string.navdrawer_item_publishers,
            R.string.navdrawer_item_entry_types,
            R.string.navdrawer_item_backups,
            R.string.navdrawer_item_settings,
            R.string.navdrawer_item_help
    };

    // icons for navdrawer items (indices must correspond to above array)
    private static final int[] NAVDRAWER_ICON_RES_ID = new int[] {
            R.drawable.ic_drawer_report,
            R.drawable.ic_drawer_publications,
            R.drawable.ic_drawer_householders,
            R.drawable.ic_drawer_publisher,
            R.drawable.ic_drawer_entry_types,
            R.drawable.ic_drawer_db,
            R.drawable.ic_drawer_settings,
            R.drawable.ic_drawer_help
    };

    // delay to launch navdrawer item, to allow close animation to play
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;

    // fade in and fade out durations for the main content when switching between
    // different Activities of the app through the Nav Drawer
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    // list of navdrawer items that were actually added to the navdrawer, in order
    private ArrayList<Integer> mNavDrawerItems = new ArrayList<Integer>();

    // views that correspond to each navdrawer item, null if not yet created
    private View[] mNavDrawerItemViews = null;

    private FragmentManager fm;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    //private CharSequence mTitle;
    //private CharSequence mDrawerTitle;

    private Boolean firstLoad = true;

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
        mHandler = new Handler();
        fm = getSupportFragmentManager();

        is_dual_pane = findViewById(R.id.secondary_fragment_container) != null;

        initToolbar();
        setupDrawerLayout();
        // Default item selected
        onNavDrawerItemClicked(R.id.drawer_summary);

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
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
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
        onNavDrawerItemClicked(getDefaultNavDrawerItem());
    }

    @Override
    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    // Sets up the navigation drawer as appropriate.
    private void setupNavDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout == null) {
            return;
        }

        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                //setTitle(mTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                //setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.navdrawer_item_summary);

        // populate the navdrawer with the correct items
        populateNavDrawer();

        mDrawerToggle.syncState();

        // When the user runs the app for the first time, we want to land them with the
        // navigation drawer open. But just the first time.
        if (!PrefUtils.hasOpenedBefore(this)) {
            // first run of the app starts with the navdrawer open
            PrefUtils.markOpenedBefore(this);
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    /** Populates the navigation drawer with the appropriate items. */
    private void populateNavDrawer() {
        mNavDrawerItems.clear();

        mNavDrawerItems.add(NAVDRAWER_ITEM_SUMMARY);
        mNavDrawerItems.add(NAVDRAWER_ITEM_PUBLICATIONS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_HOUSEHOLDERS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_PUBLISHERS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_ENTRY_TYPES);
        mNavDrawerItems.add(NAVDRAWER_ITEM_BACKUPS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SEPARATOR);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SETTINGS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_HELP);

        createNavDrawerItems();
    }

    private void createNavDrawerItems() {
        mDrawerItemsListContainer = (ViewGroup) findViewById(R.id.navdrawer_items_list);
        if (mDrawerItemsListContainer == null) {
            return;
        }

        mNavDrawerItemViews = new View[mNavDrawerItems.size()];
        mDrawerItemsListContainer.removeAllViews();
        int i = 0;
        for (int itemId : mNavDrawerItems) {
            mNavDrawerItemViews[i] = makeNavDrawerItem(itemId, mDrawerItemsListContainer);
            mDrawerItemsListContainer.addView(mNavDrawerItemViews[i]);
            ++i;
        }
    }

    private View makeNavDrawerItem(final int itemId, ViewGroup container) {
        boolean selected = getDefaultNavDrawerItem() == itemId;
        int layoutToInflate = 0;

        if (itemId == NAVDRAWER_ITEM_SEPARATOR) {
            layoutToInflate = R.layout.navdrawer_separator;
        } else {
            layoutToInflate = R.layout.navdrawer_item;
        }

        View view = getLayoutInflater().inflate(layoutToInflate, container, false);

        if (isSeparator(itemId)) {
            // we are done
            UIUtils.setAccessibilityIgnore(view);
            return view;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        int iconId = itemId >= 0 && itemId < NAVDRAWER_ICON_RES_ID.length ? NAVDRAWER_ICON_RES_ID[itemId] : 0;
        int titleId = itemId >= 0 && itemId < NAVDRAWER_TITLE_RES_ID.length ? NAVDRAWER_TITLE_RES_ID[itemId] : 0;

        // set icon and text
        iconView.setVisibility(iconId > 0 ? View.VISIBLE : View.GONE);
        if (iconId > 0) {
            iconView.setImageResource(iconId);
        }
        titleView.setText(getString(titleId));

        formatNavDrawerItem(view, itemId, selected);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavDrawerItemClicked(itemId);
            }
        });

        return view;
    }

    private boolean isSeparator(int itemId) {
        return itemId == NAVDRAWER_ITEM_SEPARATOR;
    }

    private int getDefaultNavDrawerItem() {
        return NAVDRAWER_ITEM_DEFAULT;
    }

    private void formatNavDrawerItem(View view, int itemId, boolean selected) {
        if (isSeparator(itemId)) {
            // not applicable
            return;
        }

        //ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        // configure its appearance according to whether or not it's selected
        titleView.setTextColor(selected ?
                getResources().getColor(R.color.navdrawer_text_color_selected) :
                getResources().getColor(R.color.navdrawer_text_color));
        /*
        iconView.setColorFilter(selected ?
                getResources().getColor(R.color.navdrawer_icon_tint_selected) :
                getResources().getColor(R.color.navdrawer_icon_tint));
                */
        if(selected)
            setTitle(titleView.getText());

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void onNavDrawerItemClicked(final int itemId) {
        // launch the target Activity after a short delay, to allow the close animation to play
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                goToNavDrawerItem(itemId);
            }
        }, NAVDRAWER_LAUNCH_DELAY);

        // change the active item on the list so the user can see the item changed
        setSelectedNavDrawerItem(itemId);
        // fade out the main content
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            View mainContent = findViewById(R.id.primary_fragment_container);
            if (mainContent != null) {
                mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
                mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
            }
        }

        if(isDrawerOpen())
            mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    /**
     * Sets up the given navdrawer item's appearance to the selected state. Note: this could
     * also be accomplished (perhaps more cleanly) with state-based layouts.
     */
    private void setSelectedNavDrawerItem(int itemId) {
        if (mNavDrawerItemViews != null) {
            for (int i = 0; i < mNavDrawerItemViews.length; i++) {
                if (i < mNavDrawerItems.size()) {
                    int thisItemId = mNavDrawerItems.get(i);
                    formatNavDrawerItem(mNavDrawerItemViews[i], thisItemId, itemId == thisItemId);
                }
            }
        }
    }

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