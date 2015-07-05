package com.myministry.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.myministry.FragmentActivityStatus;
import com.myministry.R;
import com.myministry.adapters.NavDrawerMenuItemAdapter;
import com.myministry.adapters.TimeEntryAdapter;
import com.myministry.dialogfragments.PublisherNewDialogFragment;
import com.myministry.dialogfragments.PublisherNewDialogFragment.PublisherNewDialogFragmentListener;
import com.myministry.model.NavDrawerMenuItem;
import com.myministry.provider.MinistryContract.Publisher;
import com.myministry.provider.MinistryContract.Time;
import com.myministry.provider.MinistryDatabase;
import com.myministry.provider.MinistryService;
import com.myministry.ui.MainActivity;
import com.myministry.util.PrefUtils;
import com.myministry.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeEntriesFragment extends ListFragment {
	public static String ARG_YEAR = "year";
	public static String ARG_MONTH = "month";
	public static String ARG_PUBLISHER_ID = "publisher_id";
	public static String ARG_IS_MONTH = "is_month";

    public final static int DIRECTION_NO_CHANGE = 0;
    private final int DIRECTION_CHANGE_TITLES = 1;
    private final int DIRECTION_INCREASE = 2;
    private final int DIRECTION_DECREASE = 3;
    
    private boolean is_dual_pane = false;
    private boolean is_month = true;
	
	private static final int ANIMATION_DURATION = 200;
	
	private String mMonth, mYear = "";
	
	private Spinner publishers, monthly_or_yearly, view_type;
	private TextView month, year;
	private LinearLayout monthNavigation;
	
	private NavDrawerMenuItemAdapter pubsAdapter;
	
	private FragmentActivityStatus fragmentActivityStatus;
	
	private FragmentManager fm;
	
	private final SimpleDateFormat buttonFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
	
	private MinistryService database;
	private Cursor entries = null;
	//private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	private TimeEntryAdapter adapter = null;
	private int publisherId = 0;
	private Calendar date = Calendar.getInstance(Locale.getDefault());
	private Calendar serviceYear = Calendar.getInstance(Locale.getDefault());
	
	private String dbDateFormatted = "";
	private String dbTimeFrame = "";
	
	public TimeEntriesFragment newInstance(int month, int year, int _publisherID, boolean is_month) {
    	TimeEntriesFragment f = new TimeEntriesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_PUBLISHER_ID, _publisherID);
        args.putBoolean(ARG_IS_MONTH, is_month);
        f.setArguments(args);
        return f;
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.time_entries, menu);
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        fragmentActivityStatus = (FragmentActivityStatus)activity;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = fragmentActivityStatus.isDrawerOpen();
        
        if(menu.findItem(R.id.time_entries_add_item) != null)
    		menu.findItem(R.id.time_entries_add_item).setVisible(!drawerOpen);
    	
    	super.onPrepareOptionsMenu(menu);
    }
    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.time_entries, container, false);
        Bundle args = getArguments();
        
        fm = getActivity().getSupportFragmentManager();
        
        date.set(Calendar.DAY_OF_MONTH, 1);
        
        if(args != null) {
	        if(args.containsKey(ARG_YEAR)) {
	        	date.set(Calendar.YEAR, args.getInt(ARG_YEAR));
	        	serviceYear.set(Calendar.YEAR, args.getInt(ARG_YEAR));
	        }
	        if(args.containsKey(ARG_MONTH)) {
	        	date.set(Calendar.MONTH, args.getInt(ARG_MONTH));
	        	serviceYear.set(Calendar.MONTH, args.getInt(ARG_MONTH));
	        }
	        if(args.containsKey(ARG_PUBLISHER_ID))
	        	setPublisherId(args.getInt(ARG_PUBLISHER_ID));

	        if(args.containsKey(ARG_IS_MONTH))
	        	is_month = args.getBoolean(ARG_IS_MONTH);
        }
        
        view.findViewById(R.id.tv_add_item).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int LAYOUT_ID = (is_dual_pane) ? R.id.secondary_fragment_container : R.id.primary_fragment_container;
				
				FragmentTransaction ft = fm.beginTransaction();
		    	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		    	
		    	Fragment frag = fm.findFragmentById(LAYOUT_ID);
		    	TimeEditorFragment f = new TimeEditorFragment().newInstanceForPublisher(publisherId);
		    	
		    	if(frag != null)
		    		ft.remove(frag);
		    	
		    	ft.add(LAYOUT_ID, f);
		    	ft.addToBackStack(null);
		    	
		    	ft.commit();
			}
		});
        
        publishers = (Spinner) view.findViewById(R.id.publishers);
		monthly_or_yearly = (Spinner) view.findViewById(R.id.monthly_or_yearly);
		view_type = (Spinner) view.findViewById(R.id.view_type);
        monthNavigation = (LinearLayout) view.findViewById(R.id.monthNavigation);
        month = (TextView) view.findViewById(R.id.month);
    	year = (TextView) view.findViewById(R.id.year);
    	
    	view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(is_month)
					adjustMonth(1);
				else
					adjustYear(1);
				
				calculateValues();
				//animatePage(DIRECTION_INCREASE);
			}
		});
    	
    	view.findViewById(R.id.prev).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(is_month)
					adjustMonth(-1);
				else
					adjustYear(-1);
				
				calculateValues();
				//animatePage(DIRECTION_DECREASE);
			}
		});
        
        database = new MinistryService(getActivity().getApplicationContext());
        adapter = new TimeEntryAdapter(getActivity().getApplicationContext(), entries);
        pubsAdapter = new NavDrawerMenuItemAdapter(getActivity().getApplicationContext());
    	setListAdapter(adapter);
    	

    	ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this.getActivity().getApplicationContext(), R.layout.li_spinner_item);
    	spinnerArrayAdapter.setDropDownViewResource(R.layout.li_spinner_item_dropdown);
    	
    	for(String name : getResources().getStringArray(R.array.summary_time_span)) {
    		spinnerArrayAdapter.add(name);
    	}
    	
    	monthly_or_yearly.setAdapter(spinnerArrayAdapter);
    	monthly_or_yearly.setSelection((is_month) ? 0 : 1);
    	monthly_or_yearly.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				if(position == 0) {
					switchToMonthList();
				} else {
					switchToYearList();
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
    	
    	ArrayAdapter<String> spinnerArrayAdapterType = new ArrayAdapter<String>(this.getActivity().getApplicationContext(), R.layout.li_spinner_item);
    	spinnerArrayAdapterType.setDropDownViewResource(R.layout.li_spinner_item_dropdown);
    	
    	for(String name : getResources().getStringArray(R.array.summary_nav_view_type)) {
    		spinnerArrayAdapterType.add(name);
    	}
    	
    	view_type.setAdapter(spinnerArrayAdapterType);
    	view_type.setSelection(1);
    	view_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				if(position == 0) {
					((MainActivity)getActivity()).setTitle(R.string.menu_entries);
					
					FragmentTransaction ft = fm.beginTransaction();
			    	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					
					new SummaryFragment();
					SummaryFragment f1 = SummaryFragment.newInstance(PrefUtils.getPublisherId(getActivity().getApplicationContext()));
					
					ft.replace(R.id.primary_fragment_container, f1);
					ft.commit();
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
    	
		return view;
	}
	
	public void updateList() {
		month.setText(mMonth);
    	year.setText(mYear);
    	
    	database.openWritable();
		if(PrefUtils.shouldCalculateRolloverTime(getActivity())) {
			entries = database.fetchTimeEntriesByPublisherAndMonth(publisherId, dbDateFormatted, dbTimeFrame);
		} else {
			entries = database.fetchTimeEntriesByPublisherAndMonthNoRollover(publisherId, dbDateFormatted, dbTimeFrame);
		}
		adapter.changeCursor(entries);
		database.close();
	}

	public void setPublisherId(int _id) {
		publisherId = _id;
	}
    
    public void switchDate(Calendar _date) {
    	setDate(_date);
    	if(is_dual_pane)
			updateList();
		//else
			//animatePage(DIRECTION_NO_CHANGE);
    }
	
	private void setDate(Calendar _date) {
		date.set(Calendar.YEAR, _date.get(Calendar.YEAR));
		date.set(Calendar.MONTH, _date.get(Calendar.MONTH));
	}
    
    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
    	entries.moveToPosition(position);
    	if(entries.getInt(entries.getColumnIndex(Time.ENTRY_TYPE_ID)) != MinistryDatabase.ID_ROLLOVER) {
    		int LAYOUT_ID = (is_dual_pane) ? R.id.secondary_fragment_container : R.id.primary_fragment_container;
    		
    		FragmentTransaction ft = fm.beginTransaction();
        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    		
        	Fragment frag = fm.findFragmentById(LAYOUT_ID);
        	TimeEditorFragment f = new TimeEditorFragment().newInstance((int) id, publisherId);
        	
        	if(frag != null)
        		ft.remove(frag);
        	
        	ft.add(LAYOUT_ID, f);
        	ft.addToBackStack(null);
        	
        	ft.commit();
    	}
    }
    
	@TargetApi(Build.VERSION_CODES.FROYO)
	@SuppressWarnings("deprecation")
	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		
		is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;
		
		setHasOptionsMenu((is_dual_pane) ? false : true);
    	
    	if(is_dual_pane)
    		monthNavigation.setVisibility(View.GONE);
    	
    	if(!is_dual_pane) {
    		loadPublisherAdapter();
    		/*
    		LayoutParams lp;
    		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
    			lp = new LayoutParams(LayoutParams.MATCH_PARENT,(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
    		} else {
    			lp = new LayoutParams(LayoutParams.FILL_PARENT,(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
    		}
    		
    		View v = new View(getActivity().getApplicationContext());
    		//v.setPadding(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()), 0, 0);
    		v.setBackgroundResource(R.color.actionbar_background_darker);
    		
    		v.setLayoutParams(lp);
    		monthNavigation.addView(v);
    		//monthNavigation.setPadding(30, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));
    		*/
    	}
    	
    	calculateValues();
    	updateList();
	}
	
    private void adjustMonth(int addValue) {
		date.add(Calendar.MONTH, addValue);
		saveSharedPrefs();
	}
	
	public void adjustYear(int addValue) {
		serviceYear.add(Calendar.YEAR, addValue);
	}
	
	private void saveSharedPrefs() {
		if(getActivity() != null) {
			PrefUtils.setSummaryMonthAndYear(getActivity(), date);
		}
	}
	
	public void switchToYearList(Calendar _date) {
		is_month = false;
		
		serviceYear = _date;
		
		if(serviceYear.get(Calendar.MONTH) < SummaryFragment.SERVICE_YEAR_START_MONTH) {
			serviceYear.set(serviceYear.get(Calendar.YEAR) -1, SummaryFragment.SERVICE_YEAR_START_MONTH, 1);
		} else {
			serviceYear.set(serviceYear.get(Calendar.YEAR), SummaryFragment.SERVICE_YEAR_START_MONTH, 1);
		}
		
		calculateValues();
		refresh(DIRECTION_CHANGE_TITLES);
	}
	
	public void switchToMonthList(Calendar _date) {
		is_month = true;
		
		date = _date;
		
		calculateValues();
		refresh(DIRECTION_CHANGE_TITLES);
	}
	
	public void switchToYearList() {
		is_month = false;
		
		serviceYear.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), 1);
		
		if(serviceYear.get(Calendar.MONTH) < SummaryFragment.SERVICE_YEAR_START_MONTH) {
			serviceYear.set(serviceYear.get(Calendar.YEAR) -1, SummaryFragment.SERVICE_YEAR_START_MONTH, 1);
		} else {
			serviceYear.set(serviceYear.get(Calendar.YEAR), SummaryFragment.SERVICE_YEAR_START_MONTH, 1);
		}
		
		calculateValues();
		refresh(DIRECTION_CHANGE_TITLES);
	}
	
	public void switchToMonthList() {
		is_month = true;
		
		date.set(serviceYear.get(Calendar.YEAR), serviceYear.get(Calendar.MONTH), 1);
		
		calculateValues();
		refresh(DIRECTION_CHANGE_TITLES);
	}
	
	public void calculateValues() {
		if(is_month) {
			mMonth = buttonFormat.format(date.getTime()).toString().toUpperCase(Locale.getDefault());
	    	mYear = String.valueOf(date.get(Calendar.YEAR)).toUpperCase(Locale.getDefault());
	    	
	    	dbDateFormatted = TimeUtils.dbDateFormat.format(date.getTime());
	    	dbTimeFrame = "month";
		} else {
			mMonth = getActivity().getApplicationContext().getResources().getString(R.string.service_year).toUpperCase(Locale.getDefault());
			mYear = serviceYear.get(Calendar.YEAR) + " - " + String.valueOf(serviceYear.get(Calendar.YEAR) + 1);
			
			dbDateFormatted = TimeUtils.dbDateFormat.format(serviceYear.getTime());
			dbTimeFrame = "year";
		}
	}
	
	public void refresh(final int changeDirection) {
		//animatePage(changeDirection);
	}
	
	private void loadPublisherAdapter() {
		int initialSelection = 0;
		// Add new publisher item
		pubsAdapter.addItem(new NavDrawerMenuItem(getActivity().getApplicationContext().getString(R.string.menu_add_new_publisher), R.drawable.ic_drawer_publisher, MinistryDatabase.CREATE_ID));
		
		database.openWritable();
		final Cursor cursor = database.fetchActivePublishers();
        while(cursor.moveToNext()) {
        	if(cursor.getInt(cursor.getColumnIndex(Publisher._ID)) == publisherId)
        		initialSelection = pubsAdapter.getCount();
        	pubsAdapter.addItem(new NavDrawerMenuItem(cursor.getString(cursor.getColumnIndex(Publisher.NAME)), R.drawable.ic_drawer_publisher, cursor.getInt(cursor.getColumnIndex(Publisher._ID))));
        }
        cursor.close();
        database.close();
        
        pubsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		publishers.setAdapter(pubsAdapter);
		if(initialSelection != 0)
			publishers.setSelection(initialSelection);
		publishers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				if(pubsAdapter.getItem(position).getID() == MinistryDatabase.CREATE_ID) {
					PublisherNewDialogFragment f = PublisherNewDialogFragment.newInstance();
					f.setPositiveButton(new PublisherNewDialogFragmentListener() {
						@Override
						public void setPositiveButton(int _ID, String _name) {
							pubsAdapter.addItem(new NavDrawerMenuItem(_name, R.drawable.ic_drawer_publisher, _ID));
							publishers.setSelection(pubsAdapter.getCount() - 1);
						}
					});
					f.show(fm, PublisherNewDialogFragment.TAG);
				}
				else {
					setPublisherId(pubsAdapter.getItem(position).getID());
					PrefUtils.setPublisherId(getActivity().getApplicationContext(), pubsAdapter.getItem(position).getID());
					calculateValues();
					//animatePage(DIRECTION_NO_CHANGE);
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
        
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.time_entries_add_item:
				((MainActivity)getActivity()).goToNavDrawerItem(MainActivity.TIME_ENTRY_ID);
				
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
}