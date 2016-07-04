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
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.adapters.NavDrawerMenuItemAdapter;
import com.myMinistry.adapters.TimeEntryAdapter;
import com.myMinistry.dialogfragments.PublisherNewDialogFragment;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryContract.Time;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.ui.MainActivity;
import com.myMinistry.util.PrefUtils;
import com.myMinistry.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeEntriesFragment extends ListFragment {
	public static String ARG_YEAR = "year";
	public static String ARG_MONTH = "month";
	public static String ARG_PUBLISHER_ID = "publisher_id";

	private TextView view_report;
	private final SimpleDateFormat buttonFormat = new SimpleDateFormat("MMMM", Locale.getDefault());

	private String mMonth, mYear = "";
	private Spinner publishers;
	private TextView month, year;
	private LinearLayout report_nav;

	private NavDrawerMenuItemAdapter pubsAdapter;

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

		if (args != null) {
			if (args.containsKey(ARG_YEAR)) {
				date.set(Calendar.YEAR, args.getInt(ARG_YEAR));
			}
			if (args.containsKey(ARG_MONTH)) {
				date.set(Calendar.MONTH, args.getInt(ARG_MONTH));
			}
			if (args.containsKey(ARG_PUBLISHER_ID))
				setPublisherId(args.getInt(ARG_PUBLISHER_ID));
		}

		fm = getActivity().getSupportFragmentManager();

		publishers = (Spinner) view.findViewById(R.id.publishers);
		view_report = (TextView) view.findViewById(R.id.view_entries);
		report_nav = (LinearLayout) view.findViewById(R.id.report_nav);

		month = (TextView) view.findViewById(R.id.month);
		year = (TextView) view.findViewById(R.id.year);

		database = new MinistryService(getActivity().getApplicationContext());
		adapter = new TimeEntryAdapter(getActivity().getApplicationContext(), entries);
		setListAdapter(adapter);

		view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				adjustMonth(1);

				calculateValues();
				refresh();
			}
		});

		view.findViewById(R.id.prev).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				adjustMonth(-1);

				calculateValues();
				refresh();
			}
		});

		view_report.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
				FragmentTransaction ft = fm.beginTransaction();
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

				new ReportFragment();
				ReportFragment f = ReportFragment.newInstance(publisherId);


				if (frag != null)
					ft.remove(frag);

				// Clear out the back stack so back press doesn't go everywhere
                /*
                for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                */
				//fm.popBackStack(fm.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
				//getSupportFragmentManager().popBackStack(getSupportFragmentManager().getBackStac‌​kEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

				ft.add(R.id.primary_fragment_container, f);

				ft.commit();
			}
		});

		pubsAdapter = new NavDrawerMenuItemAdapter(getActivity().getApplicationContext());

		fab = (FloatingActionButton) view.findViewById(R.id.fab);

		return view;
	}

	public void updateList() {
		month.setText(mMonth);
		year.setText(mYear);

		database.openWritable();
		if (PrefUtils.shouldCalculateRolloverTime(getActivity())) {
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
		if (entries.getInt(entries.getColumnIndex(Time.ENTRY_TYPE_ID)) != MinistryDatabase.ID_ROLLOVER) {
			int LAYOUT_ID = (is_dual_pane) ? R.id.secondary_fragment_container : R.id.primary_fragment_container;

			FragmentTransaction ft = fm.beginTransaction();
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

			Fragment frag = fm.findFragmentById(LAYOUT_ID);
			TimeEditorFragment f = new TimeEditorFragment().newInstance((int) id, publisherId);

			if (frag != null)
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
				((MainActivity) getActivity()).goToNavDrawerItem(MainActivity.TIME_ENTRY_ID);
			}
		});

		calculateValues();
		updateList();

		if (is_dual_pane) {
			report_nav.setVisibility(View.GONE);
		} else {
			view_report.setText(R.string.view_month_report);

			adjustMonth(0);
		}
/*
		if(is_dual_pane) {
			fab.setVisibility(View.GONE);
			view_summary.setVisibility(View.GONE);

			TimeEntriesFragment f = new TimeEntriesFragment().newInstance(monthPicked.get(Calendar.MONTH), monthPicked.get(Calendar.YEAR), publisherId);
			FragmentTransaction ft = fm.beginTransaction();
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			ft.replace(R.id.secondary_fragment_container, f);
			ft.commit();
		} else {
			fab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((MainActivity)getActivity()).goToNavDrawerItem(MainActivity.TIME_ENTRY_ID);
				}
			});
		}
*/
		loadPublisherAdapter();
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

	public void adjustMonth(int addValue) {
		date.add(Calendar.MONTH, addValue);

		mMonth = buttonFormat.format(date.getTime()).toUpperCase(Locale.getDefault());
		mYear = String.valueOf(date.get(Calendar.YEAR)).toUpperCase(Locale.getDefault());

		saveSharedPrefs();
	}

	private void saveSharedPrefs() {
		if (getActivity() != null)
			PrefUtils.setSummaryMonthAndYear(getActivity(), date);
	}

	public void refresh() {
		updateList();
	}

	private void loadPublisherAdapter() {
		int initialSelection = 0;
		// Add new publisher item
		pubsAdapter.addItem(new NavDrawerMenuItem(getActivity().getApplicationContext().getString(R.string.menu_add_new_publisher), R.drawable.ic_drawer_publisher_male, MinistryDatabase.CREATE_ID));

		database.openWritable();
		final Cursor cursor = database.fetchActivePublishers();
		while (cursor.moveToNext()) {
			if (cursor.getInt(cursor.getColumnIndex(MinistryContract.Publisher._ID)) == publisherId)
				initialSelection = pubsAdapter.getCount();
			pubsAdapter.addItem(new NavDrawerMenuItem(cursor.getString(cursor.getColumnIndex(MinistryContract.Publisher.NAME))
					, getResources().getIdentifier("ic_drawer_publisher_" + cursor.getString(cursor.getColumnIndex(MinistryContract.Publisher.GENDER)), "drawable", getActivity().getPackageName())
					, cursor.getInt(cursor.getColumnIndex(MinistryContract.Publisher._ID))));
		}
		cursor.close();
		database.close();

		pubsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		publishers.setAdapter(pubsAdapter);

		if (initialSelection != 0)
			publishers.setSelection(initialSelection);

		publishers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				if (pubsAdapter.getItem(position).getID() == MinistryDatabase.CREATE_ID) {
					PublisherNewDialogFragment f = PublisherNewDialogFragment.newInstance();
					f.setPositiveButton(new PublisherNewDialogFragment.PublisherNewDialogFragmentListener() {
						@Override
						public void setPositiveButton(int _ID, String _name) {
							pubsAdapter.addItem(new NavDrawerMenuItem(_name, R.drawable.ic_drawer_publisher_female, _ID));
							publishers.setSelection(pubsAdapter.getCount() - 1);
						}
					});
					f.show(fm, "PublisherNewDialogFragment");
				} else {
					setPublisherId(pubsAdapter.getItem(position).getID());
					PrefUtils.setPublisherId(getActivity().getApplicationContext(), pubsAdapter.getItem(position).getID());
					calculateValues();
					refresh();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}
}