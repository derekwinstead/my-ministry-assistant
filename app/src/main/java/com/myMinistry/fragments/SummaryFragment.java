package com.myMinistry.fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.provider.MinistryContract.EntryType;
import com.myMinistry.provider.MinistryContract.LiteratureType;
import com.myMinistry.provider.MinistryContract.Publisher;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.ui.MainActivity;
import com.myMinistry.util.PrefUtils;
import com.myMinistry.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SummaryFragment extends Fragment {
    public static String ARG_PUBLISHER_ID = "publisher_id";

    private String mBSText, mRVText, mTotalHoursCount, mPlacementsCount, mVideoShowings, mRVCount, mBSCount, mRBCText, mRBCCount = "";

	private FloatingActionButton fab;
    
    private TextView total_hours_count, return_visits_text, return_visits_count, bible_studies_text, bible_studies_count, rbc_text, rbc_count, placements_count, video_showings;
	private LinearLayout placement_list;
	private Calendar monthPicked = Calendar.getInstance();
	private int publisherId = 0;
	private final SimpleDateFormat buttonFormat = new SimpleDateFormat("MMMM", Locale.getDefault());

	private MinistryService database;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.summary, container, false);
		
		Bundle args = getArguments();
		
		if(args != null && args.containsKey(ARG_PUBLISHER_ID))
			publisherId = args.getInt(ARG_PUBLISHER_ID);
		
		setHasOptionsMenu(true);
		
		database = new MinistryService(getActivity().getApplicationContext());
		
		monthPicked.set(Calendar.MONTH, PrefUtils.getSummaryMonth(getActivity(), monthPicked));
		monthPicked.set(Calendar.YEAR, PrefUtils.getSummaryYear(getActivity(), monthPicked));
		
		placements_count = (TextView) root.findViewById(R.id.placements_count);
		video_showings = (TextView) root.findViewById(R.id.video_showings);

		fab = (FloatingActionButton) root.findViewById(R.id.fab);
		
		return_visits_text = (TextView) root.findViewById(R.id.return_visits_text);
		return_visits_count = (TextView) root.findViewById(R.id.return_visits_count);
		bible_studies_text = (TextView) root.findViewById(R.id.bible_studies_text);
		bible_studies_count = (TextView) root.findViewById(R.id.bible_studies_count);
		rbc_text = (TextView) root.findViewById(R.id.rbc_text);
    	rbc_count = (TextView) root.findViewById(R.id.rbc_count);
    	total_hours_count = (TextView) root.findViewById(R.id.total_hours_count);
		placement_list = (LinearLayout) root.findViewById(R.id.placement_list);

		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this.getActivity().getApplicationContext(), R.layout.li_spinner_item);
    	spinnerArrayAdapter.setDropDownViewResource(R.layout.li_spinner_item_dropdown);
    	
    	for(String name : getResources().getStringArray(R.array.summary_time_span)) {
    		spinnerArrayAdapter.add(name);
    	}
    	
    	ArrayAdapter<String> spinnerArrayAdapterType = new ArrayAdapter<>(this.getActivity().getApplicationContext(), R.layout.li_spinner_item);
    	spinnerArrayAdapterType.setDropDownViewResource(R.layout.li_spinner_item_dropdown);
    	
    	for(String name : getResources().getStringArray(R.array.summary_nav_view_type)) {
    		spinnerArrayAdapterType.add(name);
    	}

    	return root;
    }
	
	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);

        boolean is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;

        if(is_dual_pane) {
			fab.setVisibility(View.GONE);
		} else {
			fab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((MainActivity)getActivity()).goToNavDrawerItem(MainActivity.TIME_ENTRY_ID);
				}
			});
		}

		calculateSummaryValues();
		fillPublisherSummary();
	}

	public void setDate(Calendar _date) {
		monthPicked.set(Calendar.YEAR, _date.get(Calendar.YEAR));
		monthPicked.set(Calendar.MONTH, _date.get(Calendar.MONTH));

		saveSharedPrefs();
	}

	private void saveSharedPrefs() {
		if(getActivity() != null)
			PrefUtils.setSummaryMonthAndYear(getActivity(), monthPicked);
	}
	
	public void fillPublisherSummary() {
		total_hours_count.setText(mTotalHoursCount);

		placements_count.setText(mPlacementsCount);
		video_showings.setText(mVideoShowings);
    	
    	bible_studies_text.setText(mBSText);
    	bible_studies_count.setText(mBSCount);
    	return_visits_text.setText(mRVText);
    	return_visits_count.setText(mRVCount);
    	rbc_text.setText(mRBCText);
    	rbc_count.setText(mRBCCount);
    }
	
	public void calculateSummaryValues() {
    	if(!database.isOpen())
    		database.openWritable();
		
		String dbDateFormatted = TimeUtils.dbDateFormat.format(monthPicked.getTime());
		String dbTimeFrame = "month";
		
    	if(!database.isOpen())
    		database.openWritable();
		
		if(PrefUtils.shouldCalculateRolloverTime(getActivity()))
			mTotalHoursCount = TimeUtils.getTimeLength(database.fetchListOfHoursForPublisher(dbDateFormatted, publisherId, dbTimeFrame), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity()));
		else
			mTotalHoursCount = TimeUtils.getTimeLength(database.fetchListOfHoursForPublisherNoRollover(dbDateFormatted, publisherId, dbTimeFrame), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity()));

    	if(!database.isOpen())
    		database.openWritable();

		mPlacementsCount = String.valueOf(database.fetchPlacementsCountForPublisher(publisherId, dbDateFormatted, dbTimeFrame));

		if(!database.isOpen())
			database.openWritable();

        placement_list.removeAllViews();
		Cursor literatureTypes = database.fetchTypesOfLiteratureCountsForPublisher(publisherId, dbDateFormatted, dbTimeFrame);
		for(literatureTypes.moveToFirst();!literatureTypes.isAfterLast();literatureTypes.moveToNext()) {
			LinearLayout ll = new LinearLayout(getContext());
			ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

			RelativeLayout rl = new RelativeLayout(getContext());
			rl.setLayoutParams(new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

			TextView tv1 = new TextView(getContext());
			TextView tv2 = new TextView(getContext());
			tv1.setLayoutParams(new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			tv2.setLayoutParams(new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            tv1.setText(literatureTypes.getString(literatureTypes.getColumnIndex(LiteratureType.NAME)));
            tv1.setTextColor(ContextCompat.getColor(getContext(), R.color.default_text));
			tv2.setText(String.valueOf(literatureTypes.getInt(2)));
            tv2.setTextColor(ContextCompat.getColor(getContext(), R.color.default_text));
            tv2.setGravity(Gravity.RIGHT);

			rl.addView(tv1);
			rl.addView(tv2);

			ll.addView(rl);

			placement_list.addView(ll);
		}

		mVideoShowings = String.valueOf(database.fetchVideoShowingsCountForPublisher(publisherId, dbDateFormatted, dbTimeFrame));

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
		String formattedDate = TimeUtils.dbDateFormat.format(monthPicked.getTime());
		
		if(!database.isOpen())
			database.openWritable();
		
		/** Set the date */
		retVal.append(buttonFormat.format(monthPicked.getTime())).append(" ").append(monthPicked.get(Calendar.YEAR));
		
		Cursor pubs = database.fetchActivePublishers();
		/** Loop over all the active publishers */
		for(pubs.moveToFirst();!pubs.isAfterLast();pubs.moveToNext()) {
			if(pubs.getPosition() > 0)
				retVal.append("\n");
			
			/** Set publisher's name */
			retVal.append("\n").append(pubs.getString(pubs.getColumnIndex(Publisher.NAME)));
			
			/** Set total hours */
			retVal.append("\n").append(getResources().getString(R.string.total_time)).append(": ");
			if(PrefUtils.shouldCalculateRolloverTime(getActivity()))
				retVal.append(TimeUtils.getTimeLength(database.fetchListOfHoursForPublisher(formattedDate, pubs.getInt(pubs.getColumnIndex(Publisher._ID)), "month"), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity())));
	    	else
	    		retVal.append(TimeUtils.getTimeLength(database.fetchListOfHoursForPublisherNoRollover(formattedDate, pubs.getInt(pubs.getColumnIndex(Publisher._ID)), "month"), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity())));
			
			/** Fill all the publication amounts */
			Cursor lit = database.fetchTypesOfLiteratureCountsForPublisher(pubs.getInt(pubs.getColumnIndex(Publisher._ID)), formattedDate, "month");
			for(lit.moveToFirst();!lit.isAfterLast();lit.moveToNext()) {
				if(lit.getInt(2) > 0) {
					retVal.append("\n").append(lit.getString(lit.getColumnIndex(LiteratureType.NAME))).append(": ");
					retVal.append(String.valueOf(lit.getInt(2)));
				}
			}
			lit.close();
	    	
	    	/** Now for the other entry types */
	    	Cursor entryTypes = database.fetchEntryTypeCountsForPublisher(pubs.getInt(pubs.getColumnIndex(Publisher._ID)), formattedDate, "month");
	    	for(entryTypes.moveToFirst();!entryTypes.isAfterLast();entryTypes.moveToNext()) {
	    		if(entryTypes.getInt(2) > 0) {
					retVal.append("\n").append(entryTypes.getString(lit.getColumnIndex(EntryType.NAME))).append(": ");
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
				Intent share = new Intent(Intent.ACTION_SEND);
	    		share.setType("text/plain");
	    		share.setFlags((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ? Intent.FLAG_ACTIVITY_NEW_DOCUMENT : Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET); 
	    		share.putExtra(Intent.EXTRA_TEXT, populateShareString());
	    		share.putExtra(Intent.EXTRA_SUBJECT, buttonFormat.format(monthPicked.getTime()) + " " + monthPicked.get(Calendar.YEAR));
	    		startActivity(Intent.createChooser(share, getResources().getString(R.string.menu_send_report)));
	        	
	    		return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
}