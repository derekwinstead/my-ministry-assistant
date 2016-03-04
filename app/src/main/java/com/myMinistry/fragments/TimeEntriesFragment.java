package com.myMinistry.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.myMinistry.R;
import com.myMinistry.adapters.TimeEntryAdapter;
import com.myMinistry.provider.MinistryContract.Time;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.ui.MainActivity;
import com.myMinistry.util.PrefUtils;
import com.myMinistry.util.TimeUtils;

import java.util.Calendar;
import java.util.Locale;

public class TimeEntriesFragment extends ListFragment {
	public static String ARG_YEAR = "year";
	public static String ARG_MONTH = "month";
	public static String ARG_PUBLISHER_ID = "publisher_id";
    
    private boolean is_dual_pane = false;
	
	private FragmentManager fm;

	private FloatingActionButton fab;
	
	private MinistryService database;
	private Cursor entries = null;
	private TimeEntryAdapter adapter = null;
	private int publisherId = 0;
	private Calendar date = Calendar.getInstance(Locale.getDefault());
	
	private String dbDateFormatted = "";
	private String dbTimeFrame = "";
	
	public TimeEntriesFragment newInstance(int month, int year, int _publisherID) {
    	TimeEntriesFragment f = new TimeEntriesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_PUBLISHER_ID, _publisherID);
        f.setArguments(args);
        return f;
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
			}
			if(args.containsKey(ARG_MONTH)) {
				date.set(Calendar.MONTH, args.getInt(ARG_MONTH));
			}
	        if(args.containsKey(ARG_PUBLISHER_ID))
	        	setPublisherId(args.getInt(ARG_PUBLISHER_ID));
        }

        database = new MinistryService(getActivity().getApplicationContext());
        adapter = new TimeEntryAdapter(getActivity().getApplicationContext(), entries);
    	setListAdapter(adapter);
    	

    	ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this.getActivity().getApplicationContext(), R.layout.li_spinner_item);
    	spinnerArrayAdapter.setDropDownViewResource(R.layout.li_spinner_item_dropdown);
    	
    	for(String name : getResources().getStringArray(R.array.summary_time_span)) {
    		spinnerArrayAdapter.add(name);
    	}
    	
    	ArrayAdapter<String> spinnerArrayAdapterType = new ArrayAdapter<>(this.getActivity().getApplicationContext(), R.layout.li_spinner_item);
    	spinnerArrayAdapterType.setDropDownViewResource(R.layout.li_spinner_item_dropdown);

		fab = (FloatingActionButton) view.findViewById(R.id.fab);

		return view;
	}
	
	public void updateList() {
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
    
	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		
		is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).goToNavDrawerItem(MainActivity.TIME_ENTRY_ID);
            }
        });

    	calculateValues();
    	updateList();
	}

	public void switchToMonthList(Calendar _date) {
		date = _date;
		
		calculateValues();
		refresh();
	}

	public void calculateValues() {
		dbDateFormatted = TimeUtils.dbDateFormat.format(date.getTime());
		dbTimeFrame = "month";
	}
	
	public void refresh() {
		updateList();
	}
}