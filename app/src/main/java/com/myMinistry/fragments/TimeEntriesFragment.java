package com.myMinistry.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.adapters.NavDrawerMenuItemAdapter;
import com.myMinistry.adapters.TimeEntryListAdapter;
import com.myMinistry.bean.TimeEntryItem;
import com.myMinistry.dialogfragments.PublisherNewDialogFragment;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.ui.MainActivity;
import com.myMinistry.util.PrefUtils;
import com.myMinistry.util.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TimeEntriesFragment extends Fragment {
    private final String ARG_YEAR = "year";
    private final String ARG_MONTH = "month";
    private final String ARG_PUBLISHER_ID = "publisher_id";

    private Button view_report;

    private Spinner publishers;
    private TextView month, year;
    private TextView empty_view;
    private RecyclerView monthly_entries;
    private TimeEntryListAdapter monthly_entries_adapter;
    private final ArrayList<TimeEntryItem> time_entries_arraylist = new ArrayList<>();
    private boolean calculate_rollover_time;

    private NavDrawerMenuItemAdapter pubsAdapter;

    private FragmentManager fm;

    private FloatingActionButton fab;

    private MinistryService database;
    private int publisherId = 0;
    private final Calendar monthPicked = Calendar.getInstance(Locale.getDefault());

    public TimeEntriesFragment newInstance(int publisherId, int month, int year) {
        TimeEntriesFragment f = new TimeEntriesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_PUBLISHER_ID, publisherId);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.time_entries, container, false);
        Bundle args = getArguments();

        if (args != null) {
            monthPicked.set(Calendar.YEAR, args.getInt(ARG_YEAR));
            monthPicked.set(Calendar.MONTH, args.getInt(ARG_MONTH));
            publisherId = args.getInt(ARG_PUBLISHER_ID);
        }

        //monthPicked.set(Calendar.DAY_OF_MONTH, 1);

        database = new MinistryService(getActivity().getApplicationContext());
        fm = getActivity().getSupportFragmentManager();
        calculate_rollover_time = PrefUtils.shouldCalculateRolloverTime(getActivity());

        publishers = view.findViewById(R.id.publishers);
        view_report = view.findViewById(R.id.view_entries);
        month = view.findViewById(R.id.month);
        year = view.findViewById(R.id.year);
        empty_view = view.findViewById(R.id.empty_view);
        monthly_entries = view.findViewById(R.id.monthly_entries);

        /*
        RecyclerView placement_list;
        placement_list = root.findViewById(R.id.user_placements);
        placement_list.setHasFixedSize(true);
        placement_list.setLayoutManager(new LinearLayoutManager(getContext()));
        placement_list_adapter= new ReportPublicationSummaryAdapter(getContext(), user_placements);
        placement_list.setAdapter(placement_list_adapter);
         */
        //monthly_entries.setHasFixedSize(true);
        monthly_entries.setLayoutManager(new LinearLayoutManager(getContext()));
        monthly_entries_adapter = new TimeEntryListAdapter(getActivity().getApplicationContext(), time_entries_arraylist);
        monthly_entries.setAdapter(monthly_entries_adapter);


        //adapter = new TimeEntryAdapter(getActivity().getApplicationContext(), entries);
        //setListAdapter(adapter);

        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjustMonth(1);

                //calculateValues();
                //refresh();
            }
        });

        view.findViewById(R.id.prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjustMonth(-1);

                //calculateValues();
                //refresh();
            }
        });

        view_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportFragment f = new ReportFragment().newInstance(publisherId, monthPicked.get(Calendar.MONTH), monthPicked.get(Calendar.YEAR));
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.primary_fragment_container, f, "main");
                transaction.commit();
            }
        });

        pubsAdapter = new NavDrawerMenuItemAdapter(getActivity().getApplicationContext());

        fab = view.findViewById(R.id.fab);

        return view;
    }

    private void updateDisplayList() {
        Cursor entries;
        Cursor entryItems;

        if(!database.isOpen())
            database.openWritable();

        // Get all the entries for the selected month
        if(calculate_rollover_time) {
            entries = database.fetchTimeEntriesByPublisherAndMonth(publisherId, TimeUtils.dbDateFormat.format(monthPicked.getTime()), "month");
        } else {
            entries = database.fetchTimeEntriesByPublisherAndMonthNoRollover(publisherId, TimeUtils.dbDateFormat.format(monthPicked.getTime()), "month");
        }

        // Remove all previous entries in the array list
        time_entries_arraylist.clear();

        // Load up the array list for the adapter
        for(entries.moveToFirst(); !entries.isAfterLast(); entries.moveToNext()) {
            TimeEntryItem timeEntryItem = new TimeEntryItem(entries);

            entryItems = database.fetchHouseholderAndPlacedPublicationsByTimeId(timeEntryItem.getId());

            timeEntryItem.setEntryHouseholderItems(entryItems);
            time_entries_arraylist.add(timeEntryItem);

            entryItems.close();
        }

        entries.close();
        database.close();

        monthly_entries_adapter.notifyDataSetChanged();

        if(time_entries_arraylist.isEmpty()) {
            monthly_entries.setVisibility(View.GONE);
            empty_view.setVisibility(View.VISIBLE);
        } else  {
            monthly_entries.setVisibility(View.VISIBLE);
            empty_view.setVisibility(View.GONE);
        }

         /*
        if (dataset.isEmpty()) {
    recyclerView.setVisibility(View.GONE);
    emptyView.setVisibility(View.VISIBLE);
}
else {
    recyclerView.setVisibility(View.VISIBLE);
    emptyView.setVisibility(View.GONE);
}
         */


/*
        // All user placements
        Cursor literatureTypes = database.fetchTypesOfLiteratureCountsForPublisher(publisherId, dbDateFormatted, dbTimeFrame);
        for (literatureTypes.moveToFirst(); !literatureTypes.isAfterLast(); literatureTypes.moveToNext()) {
            if(user_placements.size() <= literatureTypes.getPosition()) {
                user_placements.add(new ReportPublication(literatureTypes.getString(literatureTypes.getColumnIndex(LiteratureType.NAME)),literatureTypes.getInt(2)));
            } else {
                user_placements.set(literatureTypes.getPosition(),new ReportPublication(literatureTypes.getString(literatureTypes.getColumnIndex(LiteratureType.NAME)), literatureTypes.getInt(2)));
            }
        }
        literatureTypes.close();
        */
        // End All user placements
/*
        if (PrefUtils.shouldCalculateRolloverTime(getActivity())) {
            entries = database.fetchTimeEntriesByPublisherAndMonth(publisherId, dbDateFormatted, dbTimeFrame);
        } else {
            entries = database.fetchTimeEntriesByPublisherAndMonthNoRollover(publisherId, dbDateFormatted, dbTimeFrame);
        }
        adapter.changeCursor(entries);
        */
    }
/*
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        entries.moveToPosition(position);
        if (entries.getInt(entries.getColumnIndex(Time.ENTRY_TYPE_ID)) != MinistryDatabase.ID_ROLLOVER) {
            TimeEditorFragment f = new TimeEditorFragment().newInstance((int) id, publisherId);
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.primary_fragment_container, f, "main");
            transaction.commit();
        }
    }
*/
    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).goToNavDrawerItem(MainActivity.TIME_ENTRY_ID);
            }
        });

        view_report.setText(R.string.view_month_report);
        loadPublisherAdapter();
        adjustMonth(0);
    }
/*
    public void calculateValues() {
        //dbDateFormatted = TimeUtils.dbDateFormat.format(monthPicked.getTime());
        dbTimeFrame = "month";


    }
*/
    private void adjustMonth(int addValue) {
        monthPicked.add(Calendar.MONTH, addValue);

        month.setText(TimeUtils.fullMonthFormat.format(monthPicked.getTime()).toUpperCase(Locale.getDefault()));
        year.setText(String.valueOf(monthPicked.get(Calendar.YEAR)).toUpperCase(Locale.getDefault()));

        updateDisplayList();
    }
/*
    public void refresh() {
        updateList();
    }
*/
    private void setPublisherId(int _id) {
        publisherId = _id;

        if (pubsAdapter != null) {
            for (int i = 0; i <= pubsAdapter.getCount(); i++) {
                if (publisherId == pubsAdapter.getItem(i).getID()) {
                    publishers.setSelection(i);
                    break;
                }
            }
        }
    }

    // TODO loadPublisherAdapter() - Review the function and make sure it's up to standards now
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
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}