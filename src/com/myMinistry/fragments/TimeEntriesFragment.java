package com.myMinistry.fragments;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.myMinistry.FragmentActivityStatus;
import com.myMinistry.R;
import com.myMinistry.Techniques;
import com.myMinistry.YoYo;
import com.myMinistry.model.TimeEntryAdapter;
import com.myMinistry.provider.MinistryContract.Time;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.util.PrefUtils;
import com.myMinistry.util.TimeUtils;
import com.nineoldandroids.animation.Animator;

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
	
	private TextView month, year;
	private LinearLayout monthNavigation;
	
	private FragmentManager fm;
	
	private final SimpleDateFormat buttonFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
	
	private MinistryService database;
	private Cursor entries = null;
	//private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	private TimeEntryAdapter adapter = null;
	private int publisherID = 0;
	private Calendar date = Calendar.getInstance(Locale.getDefault());
	private Calendar serviceYear = Calendar.getInstance(Locale.getDefault());
	
	private String dbDateFormatted = "";
	private String dbTimeFrame = "";
	
	private FragmentActivityStatus fragmentActivityStatus;
	/*
	public TimeEntriesFragment newInstance(int month, int year, int _publisherID) {
    	TimeEntriesFragment f = new TimeEntriesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_PUBLISHER_ID, _publisherID);
        f.setArguments(args);
        return f;
    }
	*/
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
	public void onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = fragmentActivityStatus.isDrawerOpen();
		
		if(is_dual_pane)
			menu.removeItem(R.id.time_entries_summary);
		
		if(menu.findItem(R.id.time_entries_summary) != null)
    		menu.findItem(R.id.time_entries_summary).setVisible(!drawerOpen);
    	
    	super.onPrepareOptionsMenu(menu);
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        fragmentActivityStatus = (FragmentActivityStatus)activity;
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.time_entries, menu);
	}
    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.time_entries, container, false);
        Bundle args = getArguments();
        
        setHasOptionsMenu(true);
        
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
	        	setPublisher(args.getInt(ARG_PUBLISHER_ID));

	        if(args.containsKey(ARG_IS_MONTH))
	        	is_month = args.getBoolean(ARG_IS_MONTH);
        }
        
        view.findViewById(R.id.tv_add_item).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int LAYOUT_ID = (is_dual_pane) ? R.id.secondary_fragment_container : R.id.primary_fragment_container;
				
				FragmentTransaction ft = fm.beginTransaction();
		    	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		    	
		    	Fragment frag = fm.findFragmentById(LAYOUT_ID);
		    	TimeEditorFragment f = new TimeEditorFragment().newInstanceForPublisher(publisherID);
		    	
		    	if(frag != null)
		    		ft.remove(frag);
		    	
		    	ft.add(LAYOUT_ID, f);
		    	ft.addToBackStack(null);
		    	
		    	ft.commit();
			}
		});
        
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
				animatePage(DIRECTION_INCREASE);
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
				animatePage(DIRECTION_DECREASE);
			}
		});
        
        database = new MinistryService(getActivity().getApplicationContext());
        adapter = new TimeEntryAdapter(getActivity().getApplicationContext(), entries);
    	setListAdapter(adapter);
    	
		return view;
	}
	
	public void updateList() {
		month.setText(mMonth);
    	year.setText(mYear);
    	
    	database.openWritable();
		if(PrefUtils.shouldCalculateRolloverTime(getActivity())) {
			entries = database.fetchTimeEntriesByPublisherAndMonth(publisherID, dbDateFormatted, dbTimeFrame);
		} else {
			entries = database.fetchTimeEntriesByPublisherAndMonthNoRollover(publisherID, dbDateFormatted, dbTimeFrame);
		}
		adapter.changeCursor(entries);
		database.close();
	}
	
	public void animatePage(final int changeDirection) {
		final Handler mHandler = new Handler();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Techniques anim_out_year = Techniques.Pulse;
				
				if(changeDirection == DIRECTION_INCREASE) {
					if(date.get(Calendar.MONTH) == Calendar.JANUARY || !is_month) {
						anim_out_year = Techniques.SlideOutLeft;
					}
				} else if(changeDirection == DIRECTION_DECREASE) {
					if(date.get(Calendar.MONTH) == Calendar.DECEMBER || !is_month) {
						anim_out_year = Techniques.SlideOutRight;
					}
				} else if(changeDirection == DIRECTION_CHANGE_TITLES) {
					anim_out_year = Techniques.SlideOutDown;
				}
				
				YoYo.with(anim_out_year).withListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationStart(Animator arg0) {
						if(changeDirection == DIRECTION_INCREASE) {
							if(is_month) {
								YoYo.with(Techniques.SlideOutLeft).duration(ANIMATION_DURATION).playOn(month);
							}
						} else if(changeDirection == DIRECTION_DECREASE) {
							if(is_month) {
								YoYo.with(Techniques.SlideOutRight).duration(ANIMATION_DURATION).playOn(month);
							}
						} else if(changeDirection == DIRECTION_CHANGE_TITLES) {
							YoYo.with(Techniques.SlideOutUp).duration(ANIMATION_DURATION).playOn(month);
						}
					}
					
					@Override
					public void onAnimationRepeat(Animator arg0) { }
					
					@Override
					public void onAnimationEnd(Animator arg0) {
						updateList();
						
						if(changeDirection == DIRECTION_INCREASE) {
							if(is_month) {
								YoYo.with(Techniques.SlideInRight).duration(ANIMATION_DURATION).playOn(month);
							}
							if(date.get(Calendar.MONTH) == Calendar.JANUARY || !is_month) {
								YoYo.with(Techniques.SlideInRight).duration(ANIMATION_DURATION).playOn(year);
							}
						} else if(changeDirection == DIRECTION_DECREASE) {
							if(is_month) {
								YoYo.with(Techniques.SlideInLeft).duration(ANIMATION_DURATION).playOn(month);
							}
							if(date.get(Calendar.MONTH) == Calendar.DECEMBER || !is_month) {
								YoYo.with(Techniques.SlideInLeft).duration(ANIMATION_DURATION).playOn(year);
							}
						} else if(changeDirection == DIRECTION_CHANGE_TITLES) {
							YoYo.with(Techniques.SlideInDown).duration(ANIMATION_DURATION).playOn(month);
							YoYo.with(Techniques.SlideInUp).duration(ANIMATION_DURATION).playOn(year);
						}
					}
					
					@Override
					public void onAnimationCancel(Animator arg0) { }
				})
				.duration(ANIMATION_DURATION)
				.playOn(year);
			}
		}, ANIMATION_DURATION);
    }
	
	public void setPublisher(int _id) {
		publisherID = _id;
	}
    
    public void switchDate(Calendar _date) {
    	setDate(_date);
    	if(is_dual_pane)
			updateList();
		else
			animatePage(DIRECTION_NO_CHANGE);
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
        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    		
        	Fragment frag = fm.findFragmentById(LAYOUT_ID);
        	TimeEditorFragment f = new TimeEditorFragment().newInstance((int) id, publisherID);
        	
        	if(frag != null)
        		ft.remove(frag);
        	
        	ft.add(LAYOUT_ID, f);
        	ft.addToBackStack(null);
        	
        	ft.commit();
    	}
    }
    
	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		
		is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;
    	
    	if(is_dual_pane)
    		monthNavigation.setVisibility(View.GONE);
    	
    	calculateValues();
    	updateList();
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.time_entries_summary:
				if(!is_dual_pane) {
					PrefUtils.setSummaryMonthAndYear(getActivity(), date);
		        	FragmentTransaction ft = fm.beginTransaction();
		        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					
					Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
					new SummaryFragment();
					SummaryFragment f = SummaryFragment.newInstance(publisherID);
					
					if(frag != null)
						ft.remove(frag);
					
					ft.add(R.id.primary_fragment_container, f);
		        	ft.addToBackStack(null);
		        	
		        	ft.commit();
		        }
				
	        	return true;
		}
		
		return super.onOptionsItemSelected(item);
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
		animatePage(changeDirection);
	}
}