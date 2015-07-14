package com.myMinistry.fragments;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment.NumberPickerDialogHandler;
import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.adapters.NavDrawerMenuItemAdapter;
import com.myMinistry.adapters.TimeEditorEntryAdapter;
import com.myMinistry.dialogfragments.DatePickerDialogFragment;
import com.myMinistry.dialogfragments.DatePickerDialogFragment.DatePickerDialogFragmentListener;
import com.myMinistry.dialogfragments.HouseholderDialogFragment;
import com.myMinistry.dialogfragments.HouseholderDialogFragment.HouseholderDialogFragmentListener;
import com.myMinistry.dialogfragments.HouseholderNewDialogFragment;
import com.myMinistry.dialogfragments.HouseholderNewDialogFragment.HouseholderNewDialogFragmentListener;
import com.myMinistry.dialogfragments.NotesDialogFragment;
import com.myMinistry.dialogfragments.NotesDialogFragment.NotesDialogFragmentListener;
import com.myMinistry.dialogfragments.PublicationDialogFragment;
import com.myMinistry.dialogfragments.PublicationDialogFragment.LiteratureDialogFragmentListener;
import com.myMinistry.dialogfragments.PublicationNewDialogFragment;
import com.myMinistry.dialogfragments.PublicationNewDialogFragment.LiteratureNewDialogFragmentListener;
import com.myMinistry.dialogfragments.PublicationTypeDialogFragment;
import com.myMinistry.dialogfragments.PublicationTypeDialogFragment.LiteratureTypeDialogFragmentListener;
import com.myMinistry.dialogfragments.TimePickerDialogFragment;
import com.myMinistry.dialogfragments.TimePickerDialogFragment.TimePickerDialogFragmentListener;
import com.myMinistry.model.HouseholderForTime;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.model.QuickLiterature;
import com.myMinistry.provider.MinistryContract.EntryType;
import com.myMinistry.provider.MinistryContract.Householder;
import com.myMinistry.provider.MinistryContract.Literature;
import com.myMinistry.provider.MinistryContract.LiteraturePlaced;
import com.myMinistry.provider.MinistryContract.Notes;
import com.myMinistry.provider.MinistryContract.Publisher;
import com.myMinistry.provider.MinistryContract.Time;
import com.myMinistry.provider.MinistryContract.TimeHouseholder;
import com.myMinistry.provider.MinistryContract.UnionsNameAsRef;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.ui.MainActivity;
import com.myMinistry.util.PrefUtils;
import com.myMinistry.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TimeEditorFragment extends ListFragment implements NumberPickerDialogHandler {
	public static final String ARG_TIME_ID = "time_id";
	public static final String ARG_PUBLISHER_ID = "publisher_id";
	
	private boolean is_dual_pane = false;
	
	private MinistryService database = null;
	private int publisherId, originalPublisherId, entryTypeId, timeId = 0;
	private Spinner publishers, entryTypes = null;
	private TextView dateStart, dateEnd, timeStart, timeEnd;
	private ImageView addListItem;
	private Cursor qEntryTypes = null;
	private Calendar selectedDateStart = Calendar.getInstance();
	private Calendar selectedDateEnd = Calendar.getInstance();
	private Calendar originalSelectedDateStart = Calendar.getInstance();
	private Cursor qPublishers = null;
	private String publisherName = null;
	private ArrayList<HouseholderForTime> householderList = new ArrayList<HouseholderForTime>();
	private int selectedHHLoc, selectedLitLoc = 0;
	private HouseholderForTime householderForTime;
	private QuickLiterature quickLit;
	private TimeEditorEntryAdapter adapter;
	private boolean allowedToEdit = true;
	private boolean publisherExists = false;
	private boolean showFlow = true;
	private NavDrawerMenuItemAdapter typesAdapter;
	private NavDrawerMenuItemAdapter pubsAdapter;
	private Animation anim;
	private FragmentManager fm;
	
	private final int REF_DATE_START = 3;
	private final int REF_DATE_END = 4;
	
	private final int REF_NOTES = 0;
	private final int REF_ADD_LITERATURE = 1;
	private final int REF_DELETE = 2;
	private final int REF_TOGGLE_RETURN_VISIT = 3;
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
	SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aaa", Locale.getDefault());
	
	SimpleDateFormat saveTimeFormat	= new SimpleDateFormat("HH:mm", Locale.getDefault());
	
	public TimeEditorFragment newInstance() {
		TimeEditorFragment f = new TimeEditorFragment();
        return f;
    }
	
	public TimeEditorFragment newInstance(int _timeID) {
		TimeEditorFragment f = new TimeEditorFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_TIME_ID, _timeID);
		f.setArguments(args);
		return f;
    }
	
	public TimeEditorFragment newInstance(int timeId, int publisherId) {
		TimeEditorFragment f = new TimeEditorFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_TIME_ID, timeId);
		args.putInt(ARG_PUBLISHER_ID, publisherId);
		f.setArguments(args);
		return f;
    }
	
	public TimeEditorFragment newInstanceForPublisher(int publisherId) {
		TimeEditorFragment f = new TimeEditorFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_PUBLISHER_ID, publisherId);
		f.setArguments(args);
		return f;
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if(allowedToEdit) {
			if(timeId == 0)
				inflater.inflate(R.menu.save_cancel, menu);
			else
				inflater.inflate(R.menu.save_cancel_discard, menu);
		}
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.time_editor, container, false);
		int _timeID = 0;
		
		Bundle args = getArguments();
		
		if(args != null) {
			if(args.containsKey(ARG_TIME_ID))
				_timeID = args.getInt(ARG_TIME_ID);
			if(args.containsKey(ARG_PUBLISHER_ID))
				publisherId = args.getInt(ARG_PUBLISHER_ID);
		}
		
		setHasOptionsMenu(true);
		
		fm = getActivity().getSupportFragmentManager();
		
		anim = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fade_anim);
	    
		publishers = (Spinner) root.findViewById(R.id.publishers);
		entryTypes = (Spinner) root.findViewById(R.id.entryTypes);
		dateStart = (TextView) root.findViewById(R.id.dateStart);
		dateEnd = (TextView) root.findViewById(R.id.dateEnd);
		timeStart = (TextView) root.findViewById(R.id.timeStart);
		timeEnd = (TextView) root.findViewById(R.id.timeEnd);
		addListItem = (ImageView) root.findViewById(R.id.addListItem);
		
		selectedDateStart.set(Calendar.MILLISECOND, 0);
		selectedDateEnd.set(Calendar.MILLISECOND, 0);
		
		database = new MinistryService(getActivity());
		database.openWritable();
		
		if(_timeID == 0) {
			qEntryTypes = database.fetchActiveEntryTypes();
			qPublishers = database.fetchActivePublishers();
		} else { 
			qEntryTypes = database.fetchActiveEntryTypes();
			qPublishers = database.fetchAllPublishers();
		}
		
		
		typesAdapter = new NavDrawerMenuItemAdapter(getActivity().getApplicationContext());
		while(qEntryTypes.moveToNext())
			typesAdapter.addItem(new NavDrawerMenuItem(qEntryTypes.getString(qEntryTypes.getColumnIndex(EntryType.NAME)), R.drawable.ic_drawer_entry_types, qEntryTypes.getInt(qEntryTypes.getColumnIndex(EntryType._ID))));
		
		pubsAdapter = new NavDrawerMenuItemAdapter(getActivity().getApplicationContext());
		while(qPublishers.moveToNext())
			pubsAdapter.addItem(new NavDrawerMenuItem(qPublishers.getString(qPublishers.getColumnIndex(Publisher.NAME)), R.drawable.ic_drawer_publisher, qPublishers.getInt(qPublishers.getColumnIndex(Publisher._ID))));
		
		entryTypes.setAdapter(typesAdapter);
		entryTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				entryTypeId = typesAdapter.getItem(position).getID();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
		
		publishers.setAdapter(pubsAdapter);
		publishers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				publisherId = pubsAdapter.getItem(position).getID();
				//qPublishers.moveToPosition(position);
				publisherName = pubsAdapter.getItem(position).toString();
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
		timeStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerDialogFragment frag = TimePickerDialogFragment.newInstance(selectedDateStart);
				frag.setTimePickerDialogFragmentListener(new TimePickerDialogFragmentListener() {
					@Override
					public void TimePickerDialogFragmentListenerSet(int hourOfDay, int minute) {
						selectedDateStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
						selectedDateStart.set(Calendar.MINUTE, minute);
						updateDisplayTimes();
						
					}
				});
				frag.show(fm, "TimePickerDialogFragment");
			}
		});
		
		timeEnd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerDialogFragment frag = TimePickerDialogFragment.newInstance(selectedDateEnd);
				frag.setTimePickerDialogFragmentListener(new TimePickerDialogFragmentListener() {
					@Override
					public void TimePickerDialogFragmentListenerSet(int hourOfDay, int minute) {
						selectedDateEnd.set(Calendar.HOUR_OF_DAY, hourOfDay);
						selectedDateEnd.set(Calendar.MINUTE, minute);
						updateDisplayTimes();
						
					}
				});
				frag.show(fm, "TimePickerDialogFragment");
			}
		});
		
		dateStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerDialogFragment frag = DatePickerDialogFragment.newInstance(selectedDateStart);
				frag.setDatePickerDialogFragmentListener(new DatePickerDialogFragmentListener() {
					@Override
					public void DatePickerDialogFragmentListenerSet(int selectedYear, int selectedMonth, int selectedDay) {
						selectedDateStart.set(selectedYear, selectedMonth, selectedDay);
						updateDisplayTimes();
						
					}
				});
				frag.show(fm, "DatePickerDialogFragment");
			}
		});
		
		dateEnd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerDialogFragment frag = DatePickerDialogFragment.newInstance(selectedDateEnd);
				frag.setDatePickerDialogFragmentListener(new DatePickerDialogFragmentListener() {
					@Override
					public void DatePickerDialogFragmentListenerSet(int selectedYear, int selectedMonth, int selectedDay) {
						selectedDateEnd.set(selectedYear, selectedMonth, selectedDay);
						updateDisplayTimes();
						
					}
				});
				frag.show(fm, "DatePickerDialogFragment");
			}
		});
		
		addListItem.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showFlow = true;
				showHouseholdersDialog();
			}
		});
		
		adapter = new TimeEditorEntryAdapter(getActivity().getApplicationContext(), householderList);
		adapter.setNotifyOnChange(true);
		setListAdapter(adapter);
		
		database.close();
		setTimeEntry(_timeID);
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		
		is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;
		
		getActivity().setTitle(R.string.navdrawer_item_time_entry);
	}
	
	public void setTimeEntry(int _timeID) {
		timeId = _timeID;
		
		if(timeId > 0) {
    		database.openWritable();
    		Cursor record = database.fetchTimeEntry(timeId);
    		if(record.moveToFirst()) {
    			String[] splitTime = record.getString(record.getColumnIndex(Time.TIME_START)).split(":");
    			Calendar todel = Calendar.getInstance(Locale.getDefault());
    			Calendar todel2 = Calendar.getInstance(Locale.getDefault());
    			
    			try {
    				todel.setTime(TimeUtils.dbDateFormat.parse(record.getString(record.getColumnIndex(Time.DATE_START))));
				} catch (Exception e) {
					todel = Calendar.getInstance(Locale.getDefault());
				}
    			
    			todel.set(Calendar.HOUR_OF_DAY,Integer.valueOf(splitTime[0]));
    			todel.set(Calendar.MINUTE,Integer.valueOf(splitTime[1]));
    			
    			setDate(todel,REF_DATE_START);
    			
    			try {
    				todel2.setTime(TimeUtils.dbDateFormat.parse(record.getString(record.getColumnIndex(Time.DATE_END))));
				} catch (Exception e) {
					todel2 = Calendar.getInstance(Locale.getDefault());
				}
    			
    			splitTime = record.getString(record.getColumnIndex(Time.TIME_END)).split(":");
    			todel2.set(Calendar.HOUR_OF_DAY,Integer.valueOf(splitTime[0]));
    			todel2.set(Calendar.MINUTE,Integer.valueOf(splitTime[1]));
    			
    			setDate(todel2,REF_DATE_END);
    			
    			// Set the publisher in the spinner 
    			for(qPublishers.moveToFirst();!qPublishers.isAfterLast();qPublishers.moveToNext()) {
    				if(qPublishers.getInt(qPublishers.getColumnIndex(Publisher._ID)) == record.getInt(record.getColumnIndex(Time.PUBLISHER_ID))) {
    					publishers.setSelection(qPublishers.getPosition());
    					/** Set the default publisher ID to be checked on update */
    					originalPublisherId = record.getInt(record.getColumnIndex(Time.PUBLISHER_ID));
    					publisherExists = true;
    					break;
    				}
    			}
    			
    			// The publisher doesn't exist in the cursor so we need to grab ALL the publishers.
    			if(!publisherExists) {
    				qPublishers = database.fetchAllPublishers();
    				
    				pubsAdapter.clear();
    				while(qPublishers.moveToNext()) {
    					if(qPublishers.getInt(qPublishers.getColumnIndex(UnionsNameAsRef.ACTIVE)) == MinistryService.ACTIVE || qPublishers.getInt(qPublishers.getColumnIndex(Publisher._ID)) == record.getInt(record.getColumnIndex(Time.PUBLISHER_ID))) {
    						pubsAdapter.addItem(new NavDrawerMenuItem(qPublishers.getString(qPublishers.getColumnIndex(Publisher.NAME)), R.drawable.ic_drawer_publisher, qPublishers.getInt(qPublishers.getColumnIndex(Publisher._ID))));
    					}
    				}	
    				
    				pubsAdapter.notifyDataSetChanged();
    				
    				publisherExists = true;
    				
    				for(qPublishers.moveToFirst();!qPublishers.isAfterLast();qPublishers.moveToNext()) {
        				if(qPublishers.getInt(qPublishers.getColumnIndex(Publisher._ID)) == record.getInt(record.getColumnIndex(Time.PUBLISHER_ID))) {
        					publishers.setSelection(qPublishers.getPosition());
        					/** Set the default publisher ID to be checked on update */
        					originalPublisherId = record.getInt(record.getColumnIndex(Time.PUBLISHER_ID));
        					break;
        				}
        			}
    			}
    			
    			if(record.getInt(record.getColumnIndex(Time.ENTRY_TYPE_ID)) == MinistryDatabase.ID_ROLLOVER) {
    				allowedToEdit = false;
    				ActivityCompat.invalidateOptionsMenu(getActivity());
    				
    				qEntryTypes = database.fetchAllEntryTypes();
    				
    				typesAdapter.clear();
    				while(qEntryTypes.moveToNext())
    					typesAdapter.addItem(new NavDrawerMenuItem(qEntryTypes.getString(qEntryTypes.getColumnIndex(EntryType.NAME)), R.drawable.ic_drawer_entry_types, qEntryTypes.getInt(qEntryTypes.getColumnIndex(EntryType._ID))));
    				
    				typesAdapter.notifyDataSetChanged();
    			}
    			
    			for(qEntryTypes.moveToFirst();!qEntryTypes.isAfterLast();qEntryTypes.moveToNext()) {
    				if(qEntryTypes.getInt(qEntryTypes.getColumnIndex(EntryType._ID)) == record.getInt(record.getColumnIndex(Time.ENTRY_TYPE_ID))) {
    					entryTypes.setSelection(qEntryTypes.getPosition());
    					break;
    				}
    			}
    			
    			// Let's get all the return visits/studies for this entry :)
    			Cursor qTimeHouseholders = database.fetchTimeHouseholdersForTimeByID(timeId);
    			Cursor qTimeLiterature;
    			for(qTimeHouseholders.moveToFirst();!qTimeHouseholders.isAfterLast();qTimeHouseholders.moveToNext()) {
    				if(qTimeHouseholders.getString(qTimeHouseholders.getColumnIndex(TimeHouseholder.HOUSEHOLDER_ID)) != null) {
	    				householderForTime = new HouseholderForTime(qTimeHouseholders.getInt(qTimeHouseholders.getColumnIndex(TimeHouseholder.HOUSEHOLDER_ID)), qTimeHouseholders.getString(qTimeHouseholders.getColumnIndex(Householder.NAME)), qTimeHouseholders.getInt(qTimeHouseholders.getColumnIndex(TimeHouseholder._ID)));
	    				householderForTime.setCountedForReturnVisit(qTimeHouseholders.getInt(qTimeHouseholders.getColumnIndex(TimeHouseholder.RETURN_VISIT)));
	    				
	    				if(qTimeHouseholders.getString(qTimeHouseholders.getColumnIndex(UnionsNameAsRef.NOTE_ID)) != "") {
		    				householderForTime.setNotesID(qTimeHouseholders.getInt(qTimeHouseholders.getColumnIndex(UnionsNameAsRef.NOTE_ID)));
		    				householderForTime.setNotes(qTimeHouseholders.getString(qTimeHouseholders.getColumnIndex(Notes.NOTES)));
	    				}
	    				householderList.add(householderForTime);
	    				// Let's get all the literature placed for this householder :)
	    				qTimeLiterature = database.fetchPlacedLitByTimeAndHouseholderID(timeId, qTimeHouseholders.getInt(qTimeHouseholders.getColumnIndex(TimeHouseholder.HOUSEHOLDER_ID)));
	    				for(qTimeLiterature.moveToFirst();!qTimeLiterature.isAfterLast();qTimeLiterature.moveToNext()) {
	    					householderList.get(householderList.size() - 1).addLit(new QuickLiterature(qTimeLiterature.getInt(qTimeLiterature.getColumnIndex(LiteraturePlaced.LITERATURE_ID)), qTimeLiterature.getString(qTimeLiterature.getColumnIndex(Literature.NAME)), qTimeLiterature.getInt(qTimeLiterature.getColumnIndex(LiteraturePlaced.COUNT)), qTimeLiterature.getInt(qTimeLiterature.getColumnIndex(Literature.TYPE_OF_LIERATURE_ID))));
	        			}
	    				qTimeLiterature.close();
    				}
    			}
    			qTimeHouseholders.close();
    		}
    		record.close();
    		database.close();
    		adapter.notifyDataSetChanged();
    	}
		
		// Set Defaults
		else {
			selectedDateEnd = Helper.roundMinutesAndHour(selectedDateEnd, 15);
			
			selectedDateStart.set(Calendar.HOUR_OF_DAY,selectedDateEnd.get(Calendar.HOUR_OF_DAY) - 1);
			selectedDateStart.set(Calendar.MINUTE,selectedDateEnd.get(Calendar.MINUTE));
			
			setDate(selectedDateStart,REF_DATE_START);	
			setDate(selectedDateEnd,REF_DATE_END);
			
			for(qPublishers.moveToFirst();!qPublishers.isAfterLast();qPublishers.moveToNext()) {
				if(qPublishers.getInt(qPublishers.getColumnIndex(Publisher._ID)) == publisherId) {
					publishers.setSelection(qPublishers.getPosition());
					break;
				}
			}
		}
		
		updateDisplayTimes();
		
		originalSelectedDateStart.set(Calendar.YEAR,selectedDateStart.get(Calendar.YEAR));
		originalSelectedDateStart.set(Calendar.MONTH,selectedDateStart.get(Calendar.MONTH));
	}
	
	public void updateDisplayTimes() {
		dateEnd.setText(dateFormat.format(selectedDateEnd.getTime()).toString());
		dateStart.setText(dateFormat.format(selectedDateStart.getTime()).toString());
		timeStart.setText(DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(selectedDateStart.getTime()).toString());
		timeEnd.setText(DateFormat.getTimeFormat(getActivity().getApplicationContext()).format(selectedDateEnd.getTime()).toString());
	}
	
	public void setDate(Calendar _date, int _which) {
		switch(_which) {
		case REF_DATE_START:
			selectedDateStart = _date;
			break;
		case REF_DATE_END:
			selectedDateEnd = _date;
			break;
		}
		updateDisplayTimes();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_save:
				if(allowedToEdit) {
					if(saveTime()) {
						PrefUtils.setSummaryMonthAndYear(getActivity(), selectedDateStart);
						PrefUtils.setPublisherId(getActivity(), publisherId);
						// This will update the action bar with the new publisher
						MainActivity mainFrag = (MainActivity) getActivity();
						mainFrag.setPublisherId(publisherId, publisherName);
					}
					else {
						Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_saved_problem), Toast.LENGTH_SHORT).show();
					}
				}
				else
					Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_cannot_modify_rollover_time), Toast.LENGTH_SHORT).show();
				
				return true;
			case R.id.menu_cancel:
				getActivity().setTitle(R.string.navdrawer_item_summary);
				
				if(is_dual_pane) {
		        	// The fragment is the time entry one, just update the frag instead of doing a replacement.
	        		SummaryFragment fragment = (SummaryFragment) fm.findFragmentById(R.id.primary_fragment_container);
	        		if(timeId > 0) {
		        		fragment.setPublisherId(originalPublisherId);
		        		fragment.setDate(originalSelectedDateStart);
	        		}
	        		fragment.refresh(SummaryFragment.DIRECTION_NO_CHANGE);
				}
				else {
					FragmentTransaction ft = fm.beginTransaction();
		        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		        	
		        	Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
		        	
					new SummaryFragment();
					SummaryFragment f;
					if(timeId > 0) {
						f = SummaryFragment.newInstance(originalPublisherId);
						f.setDate(originalSelectedDateStart);
					}
					else {
						f = SummaryFragment.newInstance(publisherId);
						f.setDate(selectedDateStart);
					}
					
					f.setDate(originalSelectedDateStart);
					
					if(frag != null)
						ft.remove(frag);
					
					ft.add(R.id.primary_fragment_container, f);
		        	
		        	ft.commit();	
				}
				return true;
			case R.id.menu_discard:
				if(allowedToEdit) {
					
					DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					    @Override
					    public void onClick(DialogInterface dialog, int which) {
					        switch (which){
					        case DialogInterface.BUTTON_POSITIVE:
					        	getActivity().setTitle(R.string.navdrawer_item_summary);
								
								// Delete Time Entries Deep
								database.openWritable();
								database.removeTimeEntryDeep(timeId);
								database.processRolloverTime(originalPublisherId, originalSelectedDateStart);
								database.close();
								
								Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show();
								
								if(is_dual_pane) {
					        		SummaryFragment f = (SummaryFragment) fm.findFragmentById(R.id.primary_fragment_container);
					        		if(timeId > 0) {
						        		f.setPublisherId(originalPublisherId);
						        		f.setDate(originalSelectedDateStart);
					        		}
					        		f.calculateSummaryValues();
					        		f.refresh(SummaryFragment.DIRECTION_NO_CHANGE);
								}
								else {
									FragmentTransaction ft = fm.beginTransaction();
									ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
									
									Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
									
									new SummaryFragment();
									SummaryFragment f;
									if(timeId > 0) {
										f = SummaryFragment.newInstance(originalPublisherId);
										f.setDate(originalSelectedDateStart);
									}
									else {
										f = SummaryFragment.newInstance(publisherId);
										f.setDate(selectedDateStart);
									}
									
									f.setDate(originalSelectedDateStart);
									
									if(frag != null)
										ft.remove(frag);
									
									ft.add(R.id.primary_fragment_container, f);
									ft.addToBackStack(null);
									
						        	ft.commit();	
								}
					        	
					        	
					        	break;
					        }
					    }
					};

					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle(R.string.confirm_deletion)
						.setPositiveButton(R.string.menu_delete, dialogClickListener)
						.setNegativeButton(R.string.menu_cancel, dialogClickListener)
						.show();
				}
				else
					Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_cannot_modify_rollover_time), Toast.LENGTH_SHORT).show();
				
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	/** Insert values into database */
	private boolean saveTime() {
		/** Flag to know if the literature should be inserted or deleted */
		boolean isNew = (timeId == 0) ? true : false;
		
		double totalTime = Helper.getDifference(selectedDateStart, selectedDateEnd);
		
		/** No zero or negative hours allowed */
		if(totalTime < 0) {
			Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_saved_problem), Toast.LENGTH_SHORT).show();
		}
		else {
			/** Values to save */
			ContentValues values = new ContentValues();
			values.put(Time.PUBLISHER_ID, publisherId);
			values.put(Time.ENTRY_TYPE_ID, entryTypeId);
			values.put(Time.DATE_START, TimeUtils.dbDateFormat.format(selectedDateStart.getTime()));
			values.put(Time.DATE_END, TimeUtils.dbDateFormat.format(selectedDateEnd.getTime()));
			values.put(Time.TIME_START, saveTimeFormat.format(selectedDateStart.getTime()));
			values.put(Time.TIME_END, saveTimeFormat.format(selectedDateEnd.getTime()));
			
			/** Let's save the info to the times table */
			database.openWritable();
			if(timeId > 0) {
				if(database.saveTime(timeId, values) == 0) {
					Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_saved_problem), Toast.LENGTH_SHORT).show();
					return false;
				}
			}
			else {
				timeId = (int) database.createTime(values);
				if(timeId == -1) {
					Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_created_problem), Toast.LENGTH_SHORT).show();
					return false;
				}
			}
			
			/** Do we need to check for two publishers for the changing of the guard? :) */
			if(!isNew && publisherId != originalPublisherId) {
				/** Run the rollover checkup on the original publisher. */
				database.processRolloverTime(originalPublisherId, originalSelectedDateStart);
			}
			
			if(originalSelectedDateStart.after(selectedDateStart))
				database.processRolloverTime(publisherId, selectedDateStart);
			else
				database.processRolloverTime(publisherId, originalSelectedDateStart);
			
			database.close();
			
			long[] householderPKIDs = new long[householderList.size()];
			int[] householderIDs = new int[householderList.size()];
			long[] placedIDs;
			
			database.openWritable();
			
			/** Loop over our householder list to save */
			for(int i = 0;i < householderList.size(); i++) {
				householderForTime = householderList.get(i);
				values = new ContentValues();
				values.put(TimeHouseholder.TIME_ID, timeId);
				values.put(TimeHouseholder.HOUSEHOLDER_ID, householderForTime.getID());
				values.put(TimeHouseholder.RETURN_VISIT, householderForTime.isCountedForReturnVisit());
				
				/** Bible Study */
				if(entryTypeId == MinistryDatabase.ID_BIBLE_STUDY)
					values.put(TimeHouseholder.STUDY, 1);
				else
					values.put(TimeHouseholder.STUDY, 0);
				
				/** Check to see if the record exists */
				if(householderForTime.getTimeHouseholderPK() == MinistryDatabase.CREATE_ID) {
					householderForTime.setTimeHouseholderPK(database.createTimeHouseholder(values));
				}
				else {
					database.saveTimeHouseholder(householderForTime.getTimeHouseholderPK(), values);
				}
				
				if(householderForTime.getNotes() != null && householderForTime.getNotes().length() > 0) {
					values = new ContentValues();
					values.put(Notes.TIME_ID, timeId);
					values.put(Notes.HOUSEHOLDER_ID, householderForTime.getID());
					values.put(Notes.NOTES, householderForTime.getNotes());
					if(householderForTime.getNotesID() == 0)
						database.createNotes(values);
					else
						database.saveNotes(householderForTime.getNotesID(), values);
				}
				
				/** We need to delete a note that existed before but has been removed. */
				if(householderForTime.getNotesID() != 0 && (householderForTime.getNotes() == null || (householderForTime.getNotes() != null && householderForTime.getNotes().length() == 0)))
					database.deleteNoteByID(householderForTime.getNotesID());
				
				householderPKIDs[i] = householderForTime.getTimeHouseholderPK();
				householderIDs[i] = householderForTime.getID();
				
				ArrayList<QuickLiterature> litList = householderList.get(i).getLit();
				if(litList == null)
					placedIDs = null;
				else {
					placedIDs = new long[litList.size()];
					for(int j = 0;j < litList.size();j++) {
						quickLit = litList.get(j);
						values = new ContentValues();
						values.put(LiteraturePlaced.TIME_ID, timeId);
						values.put(LiteraturePlaced.HOUSEHOLDER_ID, householderForTime.getID());
						values.put(LiteraturePlaced.LITERATURE_ID, quickLit.getID());
						values.put(LiteraturePlaced.PUBLISHER_ID, publisherId);
						values.put(LiteraturePlaced.DATE, TimeUtils.dbDateFormat.format(selectedDateStart.getTime()));
						values.put(LiteraturePlaced.COUNT, quickLit.getCount());
	
						/** Check to see if the record exists */
						quickLit.setPlacedID(database.fetchPlacedLitByTimeAndHouseholderAndLitID(timeId, householderForTime.getID(), quickLit.getID()));
						
						if(quickLit.getPlacedID() == 0) {
							quickLit.setPlacedID(database.createPlacedLiterature(values));
						}
						else {
							database.savePlacedLiterature(quickLit.getPlacedID(), values);
						}
						
						placedIDs[j] = quickLit.getPlacedID();
					}
				}
				
				if(!isNew) {
					/** Delete orphaned litPlaced records */
					database.deletePlacedLiteratureOrphans(timeId, householderForTime.getID(), placedIDs);
				} 
			}
			
			if(!isNew) {
				/** Delete orphaned timeHouseholder and records */
				database.deleteTimeHouseholderOrphans(timeId, householderPKIDs);
				/** Delete orphaned litPlaced records that aren't for the householders */
				database.deleteTimeHouseholderLiteraturePlacedOrphans(timeId, householderIDs);
				/** Delete orphaned notes records */
				database.deleteTimeHouseholderNotesOrphans(timeId, householderIDs);
			}
			
			database.close();
			
			if(isNew)
				Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_created), Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(getActivity(), getActivity().getApplicationContext().getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
			
			return true;
		}
		
		return false;
	}
	
	public void setSelectedHouseholder(int _ID,String _name, Boolean _isReturnVisit) {
		boolean addHH = true;
		selectedHHLoc = 0;
		
		for(int i = 0;i < householderList.size(); i++) {
			if(householderList.get(i).getID() == _ID) {
				/** We have the householder selected - don't add a new one */
				addHH = false;
				selectedHHLoc = i;
				break;
			}
		}
		
		if(addHH) {
			HouseholderForTime entry = new HouseholderForTime(_ID,_name,MinistryDatabase.CREATE_ID);
			entry.setCountedForReturnVisit(_isReturnVisit);
			selectedHHLoc = householderList.size(); /** Since this is a zero based list, the size will be the new location of the added entry. */
			householderList.add(entry);
		}
		
		adapter.notifyDataSetChanged();
		
		showLiteratureTypesDialog();
	}
	
	private void showHouseholdersDialog() {
		HouseholderDialogFragment frag = HouseholderDialogFragment.newInstance();
		frag.setHouseholderFragmentListener(new HouseholderDialogFragmentListener() {
			@Override
			public void householderDialogFragmentSet(int _ID, String _name) {
				if(_ID == HouseholderDialogFragment.CREATE_ID)
					showHouseholderCreateNewDialog();
				else
					setSelectedHouseholder(_ID,_name,true);
			}
		});
		frag.show(fm, "HouseholderDialogFragment");
	}
	
	public void showHouseholderCreateNewDialog() {
		HouseholderNewDialogFragment frag = HouseholderNewDialogFragment.newInstance();
		frag.setPositiveButton(new HouseholderNewDialogFragmentListener() {
			@Override
			public void setPositiveButton(int _ID, String _name, Boolean _isFirstVisit) {
				setSelectedHouseholder(_ID,_name,_isFirstVisit);
			}
		});
		frag.show(fm, "HouseholderNewDialogFragment");
	}
	
	public void showLiteratureTypesDialog() {
		PublicationTypeDialogFragment frag = PublicationTypeDialogFragment.newInstance(showFlow);
		frag.setLiteratureTypeDialogFragmentListener(new LiteratureTypeDialogFragmentListener() {
			@Override
			public void LiteratureTypeDialogFragmentListenerSet(int _ID, String _name) {
				if(_ID == MinistryDatabase.CREATE_ID)
					showNotesDialog(householderList.get(selectedHHLoc).getNotes());
				else
					showLiteratureDialog(_ID, _name);
			}
		});
		frag.show(fm, "PublicationTypeDialogFragment");
	}
	
	public void showLiteratureDialog(int _typeID, String _name) {
		PublicationDialogFragment frag = PublicationDialogFragment.newInstance(_typeID, _name);
		frag.setLiteratureFragmentListener(new LiteratureDialogFragmentListener() {
			@Override
			public void literatureDialogFragmentSet(int _ID, String _name, int _typeID) {
				if(_ID == PublicationDialogFragment.CREATE_ID - 1) {
					showNotesDialog(householderList.get(selectedHHLoc).getNotes());
				}
				else if(_ID == PublicationDialogFragment.CREATE_ID) {
					showLiteratureCreateNewDialog(_typeID);
				}
				else {
					QuickLiterature quickLit = new QuickLiterature(_ID, _name, 0, _typeID);
					selectedLitLoc = householderList.get(selectedHHLoc).addLit(quickLit);
					showLiteratureCountDialog();
					adapter.notifyDataSetChanged();
				}
			}
		});
		frag.show(fm, "PublicationDialogFragment");
	}
	
	public void showLiteratureCreateNewDialog(int _typeID) {
		PublicationNewDialogFragment frag = PublicationNewDialogFragment.newInstance(_typeID);
		frag.setPositiveButton(new LiteratureNewDialogFragmentListener() {
			@Override
			public void setPositiveButton(int _ID, String _name, int _litTypeID) {
				QuickLiterature quickLit = new QuickLiterature(_ID, _name, 0, _litTypeID);
				selectedLitLoc = householderList.get(selectedHHLoc).addLit(quickLit);
				showLiteratureCountDialog();
				adapter.notifyDataSetChanged();
			}
		});
		frag.show(fm, "PublicationNewDialogFragment");
	}
	
	public void showLiteratureCountDialog() {
		NumberPickerBuilder npb = new NumberPickerBuilder()
				.setReference(selectedLitLoc)
                .setFragmentManager(fm)
                .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                .setPlusMinusVisibility(View.GONE)
                .setMinNumber(1)
                .setMaxNumber(99)
                .setDecimalVisibility(View.GONE)
                .setTargetFragment(TimeEditorFragment.this);
        npb.show();
	}
	
	private void showNotesDialog(String notes) {
		NotesDialogFragment frag = NotesDialogFragment.newInstance(notes);
		frag.setPositiveButton(new NotesDialogFragmentListener() {
			@Override
			public void setPositiveButton(String _notes) {
				householderList.get(selectedHHLoc).setNotes(_notes);
				adapter.notifyDataSetChanged();
			}
		});
		frag.show(fm, "NotesDialogFragment");
	}
	
    @Override
	public void onListItemClick(ListView l, final View v, final int position, long id) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		if(householderList.get(position).toString() != null && householderList.get(position).toString().length() > 0)
			builder.setTitle(householderList.get(position).toString());
		else
			builder.setTitle(R.string.menu_options);
		
		builder.setItems(getResources().getStringArray(householderList.get(position).isCountedForReturnVisit() ? R.array.time_editor_list_item_options_no_count : R.array.time_editor_list_item_options_count), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch(item) {
					case REF_NOTES: {
						selectedHHLoc = position;
						showNotesDialog(householderList.get(position).getNotes());
						break;
					}
					case REF_ADD_LITERATURE: {
						selectedHHLoc = position;
						showFlow = false;
						showLiteratureTypesDialog();
						break;
					}
					case REF_DELETE: {
						anim.setAnimationListener(new Animation.AnimationListener() {
							@Override
							public void onAnimationStart(Animation animation) {}
							
							@Override
							public void onAnimationRepeat(Animation animation) {}
							
							@Override
							public void onAnimationEnd(Animation animation) {
								/** Remove the item from our list */
								householderList.remove(position);
								adapter.notifyDataSetChanged();
							}
						});
						
						v.startAnimation(anim);
						break;
					}
					case REF_TOGGLE_RETURN_VISIT: {
						householderList.get(position).toggleCountedForReturnVisit();
						adapter.notifyDataSetChanged();
						break;
					}
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
    }

	@Override
	public void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative, double fullNumber) {
		householderList.get(selectedHHLoc).getLit().get(reference).setCount(number);
		if(showFlow)
			showNotesDialog(householderList.get(selectedHHLoc).getNotes());
		adapter.notifyDataSetChanged();
	}
}