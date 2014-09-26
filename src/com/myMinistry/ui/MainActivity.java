package com.myMinistry.ui;

import static com.myMinistry.util.LogUtils.LOGE;
import static com.myMinistry.util.LogUtils.makeLogTag;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.myMinistry.FragmentActivityStatus;
import com.myMinistry.R;
import com.myMinistry.dialogfragments.PublisherDialogFragment;
import com.myMinistry.dialogfragments.PublisherDialogFragment.PublisherDialogFragmentListener;
import com.myMinistry.dialogfragments.PublisherNewDialogFragment;
import com.myMinistry.dialogfragments.PublisherNewDialogFragment.PublisherNewDialogFragmentListener;
import com.myMinistry.fragments.DBBackupsFragment;
import com.myMinistry.fragments.DBBackupsListFragment;
import com.myMinistry.fragments.DBScheduleFragment;
import com.myMinistry.fragments.EntryTypeManagerFrag;
import com.myMinistry.fragments.HouseholdersFragment;
import com.myMinistry.fragments.PublicationFragment;
import com.myMinistry.fragments.PublicationManagerFrag;
import com.myMinistry.fragments.PublishersFragment;
import com.myMinistry.fragments.SummaryFragment;
import com.myMinistry.fragments.TimeEntriesFragment;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.util.HelpUtils;
import com.myMinistry.util.PrefUtils;
import com.myMinistry.util.UIUtils;

public class MainActivity extends ActionBarActivity implements FragmentActivityStatus, TabListener {
	private static final String TAG = makeLogTag(MainActivity.class);
	private TextView nameTextView = null;
	//private TextView emailTextView = null;
	
	private boolean execute_tab = false;
	private boolean is_dual_pane = false;
	
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
    protected static final int NAVDRAWER_ITEM_INVALID = -1;
    protected static final int NAVDRAWER_ITEM_SEPARATOR = -2;
    
    protected static final int NAVDRAWER_ITEM_DEFAULT = NAVDRAWER_ITEM_SUMMARY;

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
    
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    
    private Boolean firstLoad = true;
    
    private int publisherId = MinistryDatabase.CREATE_ID;
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.main_activity);
    	
    	mHandler = new Handler();
    	
    	fm = getSupportFragmentManager();
    	
    	mTitle = mDrawerTitle = getTitle();
    	
    	is_dual_pane = findViewById(R.id.secondary_fragment_container) != null;
    	
    	getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    	
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
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
    	
    	return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void setTitle(CharSequence title)
    {
    	if(title != mDrawerTitle)
            mTitle = title;
    	
    	getSupportActionBar().setTitle(title);
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
    	super.onPostCreate(savedInstanceState);
    	
    	setPublisherId(PrefUtils.getPublisherId(this),PrefUtils.getPublisherName(this));
    	
    	setupNavDrawer();
    	setupAccountBox();
    	
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
    	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    	
    	Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
    	
    	switch (itemId) {
    		case NAVDRAWER_ITEM_SUMMARY:
    			getSupportActionBar().removeAllTabs();
    			
    			getSupportActionBar().addTab(getSupportActionBar().newTab().setText(R.string.tab_item_monthly).setTabListener(this));
    	    	getSupportActionBar().addTab(getSupportActionBar().newTab().setText(R.string.tab_item_yearly).setTabListener(this));
    	    	
    	    	if(!is_dual_pane)
    	    		getSupportActionBar().addTab(getSupportActionBar().newTab().setText(R.string.menu_entries).setTabListener(this));
    	    	
    			Calendar date = Calendar.getInstance(Locale.getDefault());
    			if(!(frag instanceof SummaryFragment)) {
    				if(firstLoad)
    					PrefUtils.setSummaryMonthAndYear(this, date);
    				
    				new SummaryFragment();
					SummaryFragment f = SummaryFragment.newInstance(publisherId);
		        	
		        	if(frag != null)
		        		ft.remove(frag);
					
					ft.add(R.id.primary_fragment_container, f);
		        	
		        	if(!firstLoad)
		        		ft.addToBackStack(null);
		        	
					ft.commit();
    			}
    			else {
		        	date.set(Calendar.MONTH, PrefUtils.getSummaryMonth(this, date));
		        	date.set(Calendar.YEAR, PrefUtils.getSummaryYear(this, date));
					
    				SummaryFragment f = (SummaryFragment) fm.findFragmentById(R.id.primary_fragment_container);
    				f.setPublisherId(publisherId);
    				f.setDate(date);
    				f.refresh(SummaryFragment.DIRECTION_NO_CHANGE);
    			}	
	        	
    			firstLoad = false;
	        	return true;
	        case NAVDRAWER_ITEM_PUBLICATIONS:
    			getSupportActionBar().removeAllTabs();
    			
    			getSupportActionBar().addTab(getSupportActionBar().newTab().setText(R.string.tab_item_publications).setTabListener(this));
    	    	getSupportActionBar().addTab(getSupportActionBar().newTab().setText(R.string.tab_item_publication_types).setTabListener(this));
    	    	
    	    	if(!(frag instanceof PublicationFragment)) {
    				PublicationFragment f = new PublicationFragment().newInstance();
    				
    				if(frag != null)
		        		ft.remove(frag);
    				
    				ft.add(R.id.primary_fragment_container, f);
    				ft.addToBackStack(null);
    				
					ft.commit();
    			}
    			
    			return true;
	        case NAVDRAWER_ITEM_HOUSEHOLDERS:
	        	getSupportActionBar().removeAllTabs();
	        	
	        	if(!is_dual_pane) {
	        		getSupportActionBar().addTab(getSupportActionBar().newTab().setText(R.string.tab_item_householders).setTabListener(this));
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
	        case NAVDRAWER_ITEM_PUBLISHERS:
	        	getSupportActionBar().removeAllTabs();
	        	
	        	if(!is_dual_pane) {
	        		getSupportActionBar().addTab(getSupportActionBar().newTab().setText(R.string.tab_item_publishers).setTabListener(this));
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
	        case NAVDRAWER_ITEM_ENTRY_TYPES:
	        	getSupportActionBar().removeAllTabs();
	        	
	        	if(!is_dual_pane) {
	        		getSupportActionBar().addTab(getSupportActionBar().newTab().setText(R.string.tab_item_entry_types).setTabListener(this));
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
	        case NAVDRAWER_ITEM_BACKUPS:
	        	getSupportActionBar().removeAllTabs();
	        	
	        	getSupportActionBar().addTab(getSupportActionBar().newTab().setText(R.string.tab_item_backups).setTabListener(this));
	        	getSupportActionBar().addTab(getSupportActionBar().newTab().setText(R.string.tab_item_schedule_backups).setTabListener(this));
	        	
	        	boolean is_dual_pane = findViewById(R.id.secondary_fragment_container) != null;
	    		
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
	        case NAVDRAWER_ITEM_SETTINGS:
	        	startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
    			return true;
    			
	        case NAVDRAWER_ITEM_HELP:
	        	String url = getApplicationContext().getString(R.string.link_faqs);  
        		Intent i = new Intent(Intent.ACTION_VIEW);  
        		i.setData(Uri.parse(url));  
        		startActivity(i);
        		
        		return true;
	    }
		
    	return false;
	}

	public void setPublisherId(int _ID,String _name) {
		publisherId = _ID;
		
		setAccountBoxPublisherName(_name);
		
		onNavDrawerItemClicked(getDefaultNavDrawerItem());
	}

	public void savePublisherNameAndIdPrefs(int _ID,String _name) {
		PrefUtils.setPublisherId(this, _ID);
		PrefUtils.setPublisherName(this, _name);
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
        
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.drawable.ic_navigation_drawer,R.string.drawer_open,R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
            	setTitle(mTitle);
        		supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            	setTitle(mDrawerTitle);
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

    /**
     * Sets up the account box. The account box is the area at the top of the nav drawer that
     * shows which account the user is logged in as, and lets them switch accounts.
     */
    private void setupAccountBox() {
        final View chosenAccountView = findViewById(R.id.chosen_account_view);
        
        //ImageView coverImageView = (ImageView) chosenAccountView.findViewById(R.id.profile_cover_image);
        //ImageView profileImageView = (ImageView) chosenAccountView.findViewById(R.id.profile_image);
        nameTextView = (TextView) chosenAccountView.findViewById(R.id.profile_name_text);
        //emailTextView = (TextView) chosenAccountView.findViewById(R.id.profile_email_text);
        
        //profileImageView.setImageResource(R.drawable.ic_action_profile_v2);
        
        setAccountBoxPublisherName(PrefUtils.getPublisherName(this));
        //emailTextView.setText("Pioneer");
        
        chosenAccountView.setEnabled(true);
        
        chosenAccountView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// launch the target Activity after a short delay, to allow the close animation to play
		        mHandler.postDelayed(new Runnable() {
		            @Override
		            public void run() {
		            	showPublisherPicker();
		            }
		        }, NAVDRAWER_LAUNCH_DELAY);
		        
		        if(isDrawerOpen())
		        	mDrawerLayout.closeDrawer(GravityCompat.START);
			}
		});
    }
    
    private void setAccountBoxPublisherName(String publisherName) {
    	if(nameTextView != null)
    		nameTextView.setText(publisherName);
    }
    
    private void showPublisherPicker() {
		PublisherDialogFragment f = PublisherDialogFragment.newInstance();
		f.setPublisherDialogFragmentListener(new PublisherDialogFragmentListener() {
			@Override
			public void publisherDialogFragmentSet(int _ID, String _name) {
				if(_ID == PublisherDialogFragment.CREATE_ID) {
					PublisherNewDialogFragment f = PublisherNewDialogFragment.newInstance();
					f.setPositiveButton(new PublisherNewDialogFragmentListener() {
						@Override
						public void setPositiveButton(int _ID, String _name) {
							savePublisherNameAndIdPrefs(_ID,_name);
							setPublisherId(_ID,_name);
						}
					});
					f.show(fm, PublisherNewDialogFragment.TAG);
				}
				else {
					savePublisherNameAndIdPrefs(_ID,_name);
					setPublisherId(_ID,_name);
				}
			}
		});
		f.show(fm, PublisherDialogFragment.TAG);
    }

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if(execute_tab) {
			if(fm.findFragmentById(R.id.primary_fragment_container) != null) {
				if(tab.getText().equals(getApplicationContext().getResources().getString(R.string.tab_item_monthly))) {
					if(fm.findFragmentById(R.id.primary_fragment_container) instanceof SummaryFragment) {
						SummaryFragment f = (SummaryFragment) fm.findFragmentById(R.id.primary_fragment_container);
						f.updatePublisherSummaryMonthly();
					} else if(fm.findFragmentById(R.id.primary_fragment_container) instanceof TimeEntriesFragment) {
						TimeEntriesFragment f = (TimeEntriesFragment) fm.findFragmentById(R.id.primary_fragment_container);
						f.switchToMonthList();
					}
				} else if(tab.getText().equals(getApplicationContext().getResources().getString(R.string.tab_item_yearly))) {
					if(fm.findFragmentById(R.id.primary_fragment_container) instanceof SummaryFragment) {
						SummaryFragment f = (SummaryFragment) fm.findFragmentById(R.id.primary_fragment_container);
						f.updatePublisherSummaryYearly();
					} else if(fm.findFragmentById(R.id.primary_fragment_container) instanceof TimeEntriesFragment) {
						TimeEntriesFragment f = (TimeEntriesFragment) fm.findFragmentById(R.id.primary_fragment_container);
						f.switchToYearList();
					}
				} else if(tab.getText().equals(getApplicationContext().getResources().getString(R.string.menu_entries))) {
					Calendar date = Calendar.getInstance(Locale.getDefault());
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					
					Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
					boolean is_month_summary = true;
					
					TimeEntriesFragment f1 = new TimeEntriesFragment().newInstance(PrefUtils.getSummaryMonth(getApplicationContext(), date), PrefUtils.getSummaryYear(getApplicationContext(), date), publisherId, is_month_summary);
					
					if(frag != null)
		        		ft.remove(frag);
		        	
		        	ft.add(R.id.primary_fragment_container, f1);
		        	
		        	ActionBar ab = getSupportActionBar();
		        	
		        	Tab t = ab.getTabAt(0);
					ab.selectTab(t);
					tab.setText(R.string.navdrawer_item_summary);
					setTitle(R.string.menu_entries);
				} else if(tab.getText().equals(getApplicationContext().getResources().getString(R.string.navdrawer_item_summary))) {
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					
					Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
					new SummaryFragment();
					SummaryFragment f = SummaryFragment.newInstance(PrefUtils.getPublisherId(getApplicationContext()));
					
					if(frag != null)
						ft.remove(frag);
					
					ft.add(R.id.primary_fragment_container, f);
					
					ActionBar ab = getSupportActionBar();
					Tab t = ab.getTabAt(0);
					ab.selectTab(t);
					tab.setText(R.string.menu_entries);
					setTitle(R.string.navdrawer_item_summary);
				} else if(tab.getText().equals(getApplicationContext().getResources().getString(R.string.tab_item_publications))) {
					Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
		    		PublicationFragment f = new PublicationFragment().newInstance();;
		        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		        	
		        	if(frag != null)
		        		ft.remove(frag);
		        	
		        	ft.add(R.id.primary_fragment_container, f);
				} else if(tab.getText().equals(getApplicationContext().getResources().getString(R.string.tab_item_publication_types))) {
					Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
		    		PublicationManagerFrag f = new PublicationManagerFrag().newInstance();;
		        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		        	
		        	if(frag != null)
		        		ft.remove(frag);
		        	
		        	ft.add(R.id.primary_fragment_container, f);
				} else if(tab.getText().equals(getApplicationContext().getResources().getString(R.string.tab_item_backups))) {
					int LAYOUT_ID = (is_dual_pane) ? R.id.secondary_fragment_container : R.id.primary_fragment_container;
					
					Fragment frag = fm.findFragmentById(LAYOUT_ID);
					DBBackupsListFragment f = new DBBackupsListFragment().newInstance();;
		        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		        	
		        	if(frag != null)
		        		ft.remove(frag);
		        	
		        	ft.add(LAYOUT_ID, f);
				} else if(tab.getText().equals(getApplicationContext().getResources().getString(R.string.tab_item_schedule_backups))) {
					int LAYOUT_ID = (is_dual_pane) ? R.id.secondary_fragment_container : R.id.primary_fragment_container;
					
					Fragment frag = fm.findFragmentById(LAYOUT_ID);
					DBScheduleFragment f = new DBScheduleFragment().newInstance();;
		        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		        	
		        	if(frag != null)
		        		ft.remove(frag);
		        	
		        	ft.add(LAYOUT_ID, f);
				}
			}
		}
		
		execute_tab = true;
	}
	
	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}
}