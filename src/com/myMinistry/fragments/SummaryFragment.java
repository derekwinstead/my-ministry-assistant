package com.myMinistry.fragments;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.myMinistry.FragmentActivityStatus;
import com.myMinistry.R;
import com.myMinistry.Techniques;
import com.myMinistry.YoYo;
import com.myMinistry.adapters.NavDrawerMenuItemAdapter;
import com.myMinistry.dialogfragments.PublisherNewDialogFragment;
import com.myMinistry.dialogfragments.PublisherNewDialogFragment.PublisherNewDialogFragmentListener;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.provider.MinistryContract.EntryType;
import com.myMinistry.provider.MinistryContract.LiteratureType;
import com.myMinistry.provider.MinistryContract.Publisher;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.ui.MainActivity;
import com.myMinistry.util.PrefUtils;
import com.myMinistry.util.TimeUtils;
import com.nineoldandroids.animation.Animator;

public class SummaryFragment extends Fragment {
    public static String ARG_PUBLISHER_ID = "publisher_id";
    
    public final static int SERVICE_YEAR_START_MONTH = Calendar.SEPTEMBER;

    public final static int DIRECTION_NO_CHANGE = 0;
    private final int DIRECTION_CHANGE_TITLES = 1;
    private final int DIRECTION_INCREASE = 2;
    private final int DIRECTION_DECREASE = 3;
    
    private boolean is_dual_pane = false;
    private boolean is_month_summary = true;
    
    private String mMonth, mYear, mTotalHoursCount, mPublicationText0, mPublicationText1, mPublicationText2, mPublicationText3, mPublicationText4, mPublicationCount0, mPublicationCount1, mPublicationCount2, mPublicationCount3, mPublicationCount4, mRVText, mRVCount, mBSText, mBSCount, mRBCText, mRBCCount = "";
    
    private Spinner publishers;
    private TextView month, year, total_hours_count, tv_pub_text_0, tv_pub_text_1, tv_pub_text_2, tv_pub_text_3, tv_pub_text_4, tv_pub_count_0, tv_pub_count_1, tv_pub_count_2, tv_pub_count_3, tv_pub_count_4, return_visits_text, return_visits_count, bible_studies_text, bible_studies_count, rbc_text, rbc_count;
	private Calendar monthPicked = Calendar.getInstance();
	private Calendar serviceYear = Calendar.getInstance();
	private int publisherId = 0;
	private final SimpleDateFormat buttonFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
	
	private NavDrawerMenuItemAdapter pubsAdapter;
	
	private FragmentManager fm;
	
	private FragmentActivityStatus fragmentActivityStatus;
	private MinistryService database;
	
	private static final int ANIMATION_DURATION = 200;
	
	private String dbDateFormatted = "";
	private String dbTimeFrame = "";
	
	public static SummaryFragment newInstance(int _publisherID) {
		SummaryFragment f = new SummaryFragment();
		Bundle args = new Bundle();
        args.putInt(ARG_PUBLISHER_ID, _publisherID);
        f.setArguments(args);
        return f;
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.summary, menu);
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        fragmentActivityStatus = (FragmentActivityStatus)activity;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = fragmentActivityStatus.isDrawerOpen();
        
        if(menu.findItem(R.id.summary_send_report) != null)
    		menu.findItem(R.id.summary_send_report).setVisible((menu.findItem(R.id.menu_save) != null) ? false : !drawerOpen);
    	if(menu.findItem(R.id.summary_add_item) != null)
    		menu.findItem(R.id.summary_add_item).setVisible((menu.findItem(R.id.menu_save) != null) ? false : !drawerOpen);
    	
    	super.onPrepareOptionsMenu(menu);
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.summary, container, false);
		
		Bundle args = getArguments();
		
		if(args != null && args.containsKey(ARG_PUBLISHER_ID))
			publisherId = args.getInt(ARG_PUBLISHER_ID);
		
		setHasOptionsMenu(true);
		
		fm = getActivity().getSupportFragmentManager();
		
		database = new MinistryService(getActivity().getApplicationContext());
		
		monthPicked.set(Calendar.MONTH, PrefUtils.getSummaryMonth(getActivity(), monthPicked));
		monthPicked.set(Calendar.YEAR, PrefUtils.getSummaryYear(getActivity(), monthPicked));
		
		setPublisherId(PrefUtils.getPublisherId(getActivity().getApplicationContext()));
		
		publishers = (Spinner) root.findViewById(R.id.publishers);
		tv_pub_text_0 = (TextView) root.findViewById(R.id.tv_pub_text_0);
		tv_pub_count_0 = (TextView) root.findViewById(R.id.tv_pub_count_0);
		tv_pub_text_1 = (TextView) root.findViewById(R.id.tv_pub_text_1);
		tv_pub_count_1 = (TextView) root.findViewById(R.id.tv_pub_count_1);
		tv_pub_text_2 = (TextView) root.findViewById(R.id.tv_pub_text_2);
		tv_pub_count_2 = (TextView) root.findViewById(R.id.tv_pub_count_2);
		tv_pub_text_3 = (TextView) root.findViewById(R.id.tv_pub_text_3);
		tv_pub_count_3 = (TextView) root.findViewById(R.id.tv_pub_count_3);
		tv_pub_text_4 = (TextView) root.findViewById(R.id.tv_pub_text_4);
		tv_pub_count_4 = (TextView) root.findViewById(R.id.tv_pub_count_4);
		
		return_visits_text = (TextView) root.findViewById(R.id.return_visits_text);
		return_visits_count = (TextView) root.findViewById(R.id.return_visits_count);
		bible_studies_text = (TextView) root.findViewById(R.id.bible_studies_text);
		bible_studies_count = (TextView) root.findViewById(R.id.bible_studies_count);
		rbc_text = (TextView) root.findViewById(R.id.rbc_text);
    	rbc_count = (TextView) root.findViewById(R.id.rbc_count);
    	
    	month = (TextView) root.findViewById(R.id.month);
    	year = (TextView) root.findViewById(R.id.year);
    	total_hours_count = (TextView) root.findViewById(R.id.total_hours_count);
    	
    	root.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(is_month_summary)
					adjustMonth(1);
				else
					adjustYear(1);
				
				calculateSummaryValues(is_month_summary);
				animatePage(DIRECTION_INCREASE);
			}
		});
    	
    	root.findViewById(R.id.prev).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(is_month_summary)
					adjustMonth(-1);
				else
					adjustYear(-1);
				
				calculateSummaryValues(is_month_summary);
				animatePage(DIRECTION_DECREASE);
			}
		});
    	
    	root.findViewById(R.id.monthYear).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refresh(DIRECTION_NO_CHANGE);
			}
		});
    	
    	pubsAdapter = new NavDrawerMenuItemAdapter(getActivity().getApplicationContext());
    	
    	return root;
    }
	
	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		
		is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;
		
		calculateSummaryValues(is_month_summary);
		fillPublisherSummary();
		
		loadPublisherAdapter();
	}
	
	public void setPublisherId(int _id) {
		if(pubsAdapter != null) {
			for(int i = 0; i <= pubsAdapter.getCount(); i++) {
				if(_id == pubsAdapter.getItem(i).getID()) {
					publishers.setSelection(i);
					break;
				}
			}
		}
		
		publisherId = _id;
	}
	
	public void adjustMonth(int addValue) {
		monthPicked.add(Calendar.MONTH, addValue);
		saveSharedPrefs();
	}
	
	public void adjustYear(int addValue) {
		serviceYear.add(Calendar.YEAR, addValue);
	}
	
	public void setDate(Calendar _date) {
		monthPicked.set(Calendar.YEAR, _date.get(Calendar.YEAR));
		monthPicked.set(Calendar.MONTH, _date.get(Calendar.MONTH));

		serviceYear.set(Calendar.YEAR, _date.get(Calendar.YEAR));
		serviceYear.set(Calendar.MONTH, _date.get(Calendar.MONTH));
		
		saveSharedPrefs();
	}
	
	public void resetDate() {
		monthPicked = Calendar.getInstance(Locale.getDefault());
		saveSharedPrefs();
	}
	
	public void refresh(final int changeDirection) {
		animatePage(changeDirection);
	}
	
	private void saveSharedPrefs() {
		if(getActivity() != null)
			PrefUtils.setSummaryMonthAndYear(getActivity(), monthPicked);
	}
	
	public void animatePage(final int changeDirection) {
		final Handler mHandler = new Handler();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				YoYo.with(Techniques.FlipOutX).withListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationStart(Animator arg0) {
						if(changeDirection == DIRECTION_INCREASE) {
							if(is_month_summary) {
								YoYo.with(Techniques.SlideOutLeft).duration(ANIMATION_DURATION).playOn(month);
							}
							if(monthPicked.get(Calendar.MONTH) == Calendar.JANUARY || !is_month_summary) {
								YoYo.with(Techniques.SlideOutLeft).duration(ANIMATION_DURATION).playOn(year);
							}
						} else if(changeDirection == DIRECTION_DECREASE) {
							if(is_month_summary) {
								YoYo.with(Techniques.SlideOutRight).duration(ANIMATION_DURATION).playOn(month);
							}
							if(monthPicked.get(Calendar.MONTH) == Calendar.DECEMBER || !is_month_summary) {
								YoYo.with(Techniques.SlideOutRight).duration(ANIMATION_DURATION).playOn(year);
							}
						} else if(changeDirection == DIRECTION_CHANGE_TITLES) {
							YoYo.with(Techniques.SlideOutUp).duration(ANIMATION_DURATION).playOn(month);
							YoYo.with(Techniques.SlideOutDown).duration(ANIMATION_DURATION).playOn(year);
						}
						
						YoYo.with(Techniques.FlipOutX).duration(ANIMATION_DURATION).playOn(tv_pub_count_0);
						YoYo.with(Techniques.FlipOutX).duration(ANIMATION_DURATION).playOn(tv_pub_count_1);
						YoYo.with(Techniques.FlipOutX).duration(ANIMATION_DURATION).playOn(tv_pub_count_2);
						YoYo.with(Techniques.FlipOutX).duration(ANIMATION_DURATION).playOn(tv_pub_count_3);
						YoYo.with(Techniques.FlipOutX).duration(ANIMATION_DURATION).playOn(tv_pub_count_4);
						YoYo.with(Techniques.FlipOutX).duration(ANIMATION_DURATION).playOn(return_visits_count);
						YoYo.with(Techniques.FlipOutX).duration(ANIMATION_DURATION).playOn(bible_studies_count);
						YoYo.with(Techniques.FlipOutX).duration(ANIMATION_DURATION).playOn(rbc_count);
					}
					
					@Override
					public void onAnimationRepeat(Animator arg0) { }
					
					@Override
					public void onAnimationEnd(Animator arg0) {
						fillPublisherSummary();
						displayTimeEntries();

						if(changeDirection == DIRECTION_INCREASE) {
							if(is_month_summary) {
								YoYo.with(Techniques.SlideInRight).duration(ANIMATION_DURATION).playOn(month);
							}
							if(monthPicked.get(Calendar.MONTH) == Calendar.JANUARY || !is_month_summary) {
								YoYo.with(Techniques.SlideInRight).duration(ANIMATION_DURATION).playOn(year);
							}
						} else if(changeDirection == DIRECTION_DECREASE) {
							if(is_month_summary) {
								YoYo.with(Techniques.SlideInLeft).duration(ANIMATION_DURATION).playOn(month);
							}
							if(monthPicked.get(Calendar.MONTH) == Calendar.DECEMBER || !is_month_summary) {
								YoYo.with(Techniques.SlideInLeft).duration(ANIMATION_DURATION).playOn(year);
							}
						} else if(changeDirection == DIRECTION_CHANGE_TITLES) {
							YoYo.with(Techniques.SlideInDown).duration(ANIMATION_DURATION).playOn(month);
							YoYo.with(Techniques.SlideInUp).duration(ANIMATION_DURATION).playOn(year);
						}
						
						YoYo.with(Techniques.FlipInX).duration(ANIMATION_DURATION).playOn(total_hours_count);
						YoYo.with(Techniques.FlipInX).duration(ANIMATION_DURATION).playOn(tv_pub_count_0);
						YoYo.with(Techniques.FlipInX).duration(ANIMATION_DURATION).playOn(tv_pub_count_1);
						YoYo.with(Techniques.FlipInX).duration(ANIMATION_DURATION).playOn(tv_pub_count_2);
						YoYo.with(Techniques.FlipInX).duration(ANIMATION_DURATION).playOn(tv_pub_count_3);
						YoYo.with(Techniques.FlipInX).duration(ANIMATION_DURATION).playOn(tv_pub_count_4);
						YoYo.with(Techniques.FlipInX).duration(ANIMATION_DURATION).playOn(return_visits_count);
						YoYo.with(Techniques.FlipInX).duration(ANIMATION_DURATION).playOn(bible_studies_count);
						YoYo.with(Techniques.FlipInX).duration(ANIMATION_DURATION).playOn(rbc_count);
					}
					
					@Override
					public void onAnimationCancel(Animator arg0) { }
				})
				.duration(ANIMATION_DURATION)
				.playOn(total_hours_count);
			}
		}, ANIMATION_DURATION);
	}
	
	public void displayTimeEntries() {
		if (is_dual_pane) {
        	Fragment rf = fm.findFragmentById(R.id.secondary_fragment_container);
        	
        	if(rf instanceof TimeEntriesFragment) {
        		TimeEntriesFragment f = (TimeEntriesFragment) fm.findFragmentById(R.id.secondary_fragment_container);
        		
        		f.setPublisherId(publisherId);
        		
        		if(is_month_summary) {
        			f.switchToMonthList(monthPicked);
        		} else {
        			f.switchToYearList(serviceYear);
        		}
        	}
        	else {
        		TimeEntriesFragment f = new TimeEntriesFragment().newInstance(monthPicked.get(Calendar.MONTH), monthPicked.get(Calendar.YEAR), publisherId, is_month_summary);
        		FragmentTransaction ft = fm.beginTransaction();
        		if(rf != null)
        			ft.remove(rf);
        		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        		ft.add(R.id.secondary_fragment_container, f);
        		ft.commit();
        	}
    	}
    }
	
	public void fillPublisherSummary() {
		month.setText(mMonth);
    	year.setText(mYear);
    	
    	total_hours_count.setText(mTotalHoursCount);
    	
    	tv_pub_text_0.setText(mPublicationText0);
    	tv_pub_count_0.setText(mPublicationCount0);
    	tv_pub_text_1.setText(mPublicationText1);
    	tv_pub_count_1.setText(mPublicationCount1);
    	tv_pub_text_2.setText(mPublicationText2);
    	tv_pub_count_2.setText(mPublicationCount2);
    	tv_pub_text_3.setText(mPublicationText3);
    	tv_pub_count_3.setText(mPublicationCount3);
    	tv_pub_text_4.setText(mPublicationText4);
    	tv_pub_count_4.setText(mPublicationCount4);
    	
    	bible_studies_text.setText(mBSText);
    	bible_studies_count.setText(mBSCount);
    	return_visits_text.setText(mRVText);
    	return_visits_count.setText(mRVCount);
    	rbc_text.setText(mRBCText);
    	rbc_count.setText(mRBCCount);
    }
	
	public void calculateSummaryValues() {
		calculateSummaryValues(is_month_summary);
	}
	
	public void calculateSummaryValues(boolean is_month) {
    	if(!database.isOpen())
    		database.openWritable();
		
		if(is_month) {
			mMonth = buttonFormat.format(monthPicked.getTime()).toString().toUpperCase(Locale.getDefault());
	    	mYear = String.valueOf(monthPicked.get(Calendar.YEAR)).toUpperCase(Locale.getDefault());
	    	
	    	dbDateFormatted = TimeUtils.dbDateFormat.format(monthPicked.getTime());
	    	dbTimeFrame = "month";
		} else {
			mMonth = getActivity().getApplicationContext().getResources().getString(R.string.service_year).toUpperCase(Locale.getDefault());
			mYear = serviceYear.get(Calendar.YEAR) + " - " + String.valueOf(serviceYear.get(Calendar.YEAR) + 1);
			
			dbDateFormatted = TimeUtils.dbDateFormat.format(serviceYear.getTime());
			dbTimeFrame = "year";
		}
		
    	if(!database.isOpen())
    		database.openWritable();
		
		if(PrefUtils.shouldCalculateRolloverTime(getActivity()))
			mTotalHoursCount = TimeUtils.getTimeLength(database.fetchListOfHoursForPublisher(dbDateFormatted, publisherId, dbTimeFrame), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity()));
		else
			mTotalHoursCount = TimeUtils.getTimeLength(database.fetchListOfHoursForPublisherNoRollover(dbDateFormatted, publisherId, dbTimeFrame), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity()));

    	if(!database.isOpen())
    		database.openWritable();
    	
		Cursor literatureTypes = database.fetchTypesOfLiteratureCountsForPublisher(publisherId, dbDateFormatted, dbTimeFrame);
    	for(literatureTypes.moveToFirst();!literatureTypes.isAfterLast();literatureTypes.moveToNext()) {
    		switch(literatureTypes.getPosition()) {
	        	case 0:
	        		mPublicationText0 = literatureTypes.getString(literatureTypes.getColumnIndex(LiteratureType.NAME));
	        		mPublicationCount0 = String.valueOf(literatureTypes.getInt(2));
	        		break;
	        	case 1:
	        		mPublicationText1 = literatureTypes.getString(literatureTypes.getColumnIndex(LiteratureType.NAME));
	        		mPublicationCount1 = String.valueOf(literatureTypes.getInt(2));
	        		break;
	        	case 2:
	        		mPublicationText2 = literatureTypes.getString(literatureTypes.getColumnIndex(LiteratureType.NAME));
	        		mPublicationCount2 = String.valueOf(literatureTypes.getInt(2));
	        		break;
	        	case 3:
	        		mPublicationText3 = literatureTypes.getString(literatureTypes.getColumnIndex(LiteratureType.NAME));
	        		mPublicationCount3 = String.valueOf(literatureTypes.getInt(2));
	        		break;
	        	case 4:
	        		mPublicationText4 = literatureTypes.getString(literatureTypes.getColumnIndex(LiteratureType.NAME));
	        		mPublicationCount4 = String.valueOf(literatureTypes.getInt(2));
	        		break;
        	}
    	}
    	literatureTypes.close();
    	
    	if(!database.isOpen())
    		database.openWritable();
    	
    	Cursor entryTypes = database.fetchEntryTypeCountsForPublisher(publisherId, dbDateFormatted, dbTimeFrame);
    	for(entryTypes.moveToFirst();!entryTypes.isAfterLast();entryTypes.moveToNext()) {
    		switch(entryTypes.getInt(entryTypes.getColumnIndex(EntryType._ID))) {
        	case MinistryDatabase.ID_BIBLE_STUDY:
        		mBSText = entryTypes.getString(entryTypes.getColumnIndex(EntryType.NAME));
        		mBSCount = String.valueOf(entryTypes.getInt(2));
        		break;
        	case MinistryDatabase.ID_RETURN_VISIT:
        		mRVText = entryTypes.getString(entryTypes.getColumnIndex(EntryType.NAME));
        		mRVCount = String.valueOf(entryTypes.getInt(2));
        		break;
        	case MinistryDatabase.ID_RBC:
        		mRBCText = entryTypes.getString(entryTypes.getColumnIndex(EntryType.NAME));
        		mRBCCount = TimeUtils.getTimeLength(database.fetchListOfRBCHoursForPublisher(dbDateFormatted, publisherId, dbTimeFrame), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity()));
        		break;
        	}
        }
    	entryTypes.close();
    	database.close();
	}
	
	private String populateShareString() {
		StringBuilder retVal = new StringBuilder();
		String formattedDate = TimeUtils.dbDateFormat.format(monthPicked.getTime()).toString();
		
		if(!database.isOpen())
			database.openWritable();
		
		/** Set the date */
		retVal.append(buttonFormat.format(monthPicked.getTime()).toString() + " " + monthPicked.get(Calendar.YEAR));
		
		Cursor pubs = database.fetchActivePublishers();
		/** Loop over all the active publishers */
		for(pubs.moveToFirst();!pubs.isAfterLast();pubs.moveToNext()) {
			if(pubs.getPosition() > 0)
				retVal.append("\n");
			
			/** Set publisher's name */
			retVal.append("\n" + pubs.getString(pubs.getColumnIndex(Publisher.NAME)));
			
			/** Set total hours */
			retVal.append("\n" + getResources().getString(R.string.total_time) + ": ");
			if(PrefUtils.shouldCalculateRolloverTime(getActivity()))
				retVal.append(TimeUtils.getTimeLength(database.fetchListOfHoursForPublisher(formattedDate, pubs.getInt(pubs.getColumnIndex(Publisher._ID)), "month"), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity())));
	    	else
	    		retVal.append(TimeUtils.getTimeLength(database.fetchListOfHoursForPublisherNoRollover(formattedDate, pubs.getInt(pubs.getColumnIndex(Publisher._ID)), "month"), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity())));
			
			/** Fill all the publication amounts */
			Cursor lit = database.fetchTypesOfLiteratureCountsForPublisher(pubs.getInt(pubs.getColumnIndex(Publisher._ID)), formattedDate, "month");
			for(lit.moveToFirst();!lit.isAfterLast();lit.moveToNext()) {
				if(lit.getInt(2) > 0) {
					retVal.append("\n" + lit.getString(lit.getColumnIndex(LiteratureType.NAME)) + ": ");
					retVal.append(String.valueOf(lit.getInt(2)));
				}
	    	}
	    	lit.close();
	    	
	    	/** Now for the other entry types */
	    	Cursor entryTypes = database.fetchEntryTypeCountsForPublisher(pubs.getInt(pubs.getColumnIndex(Publisher._ID)), formattedDate, "month");
	    	for(entryTypes.moveToFirst();!entryTypes.isAfterLast();entryTypes.moveToNext()) {
	    		if(entryTypes.getInt(2) > 0) {
					retVal.append("\n" + entryTypes.getString(lit.getColumnIndex(EntryType.NAME)) + ": ");
					if(entryTypes.getInt(entryTypes.getColumnIndex(EntryType._ID)) == MinistryDatabase.ID_RBC)
						retVal.append(TimeUtils.getTimeLength(database.fetchListOfRBCHoursForPublisher(formattedDate, pubs.getInt(pubs.getColumnIndex(Publisher._ID)), "month"), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity())));
					else
						retVal.append(String.valueOf(entryTypes.getInt(2)));
				}
	        }
	    	entryTypes.close();
		}
		database.close();
    	
    	return retVal.toString();
	}
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.summary_send_report:
				Intent share = new Intent(android.content.Intent.ACTION_SEND);
	    		share.setType("text/plain");
	    		share.setFlags((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ? Intent.FLAG_ACTIVITY_NEW_DOCUMENT : Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET); 
	    		share.putExtra(android.content.Intent.EXTRA_TEXT, populateShareString());
	    		share.putExtra(android.content.Intent.EXTRA_SUBJECT, buttonFormat.format(monthPicked.getTime()).toString() + " " + monthPicked.get(Calendar.YEAR));
	    		startActivity(Intent.createChooser(share, getResources().getString(R.string.menu_send_report)));
	        	
	    		return true;
	        	
			case R.id.summary_add_item:
				((MainActivity)getActivity()).goToNavDrawerItem(MainActivity.TIME_ENTRY_ID);
				
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public void updatePublisherSummaryYearly() {
		is_month_summary = false;
		
		serviceYear.set(monthPicked.get(Calendar.YEAR), monthPicked.get(Calendar.MONTH), 1);
		
		if(serviceYear.get(Calendar.MONTH) < SERVICE_YEAR_START_MONTH) {
			serviceYear.set(serviceYear.get(Calendar.YEAR) -1, SERVICE_YEAR_START_MONTH, 1);
		} else {
			serviceYear.set(serviceYear.get(Calendar.YEAR), SERVICE_YEAR_START_MONTH, 1);
		}
		
		calculateSummaryValues(is_month_summary);
		refresh(DIRECTION_CHANGE_TITLES);
	}
	
	public void updatePublisherSummaryMonthly() {
		is_month_summary = true;
		
		monthPicked.set(serviceYear.get(Calendar.YEAR), serviceYear.get(Calendar.MONTH), 1);
		
		calculateSummaryValues(is_month_summary);
		refresh(DIRECTION_CHANGE_TITLES);
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
					calculateSummaryValues(is_month_summary);
					animatePage(DIRECTION_NO_CHANGE);
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
	}
}