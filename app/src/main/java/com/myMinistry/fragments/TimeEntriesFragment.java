package com.myMinistry.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.adapters.NavDrawerMenuItemAdapter;
import com.myMinistry.adapters.TimeEntryAdapter;
import com.myMinistry.dialogfragments.PublisherNewDialogFragment;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.ui.MainActivity;
import com.myMinistry.util.PrefUtils;
import com.myMinistry.util.TimeUtils;

import java.util.Calendar;
import java.util.Locale;

public class TimeEntriesFragment extends Fragment {
    public static String ARG_YEAR = "year";
    public static String ARG_MONTH = "month";
    public static String ARG_PUBLISHER_ID = "publisher_id";

    private Button view_report;

    private String mMonth, mYear = "";
    private Spinner publishers;
    private TextView month, year;
    private LinearLayout report_nav;
    private TextView empty_view;
    private RecyclerView monthly_entries;

    private NavDrawerMenuItemAdapter pubsAdapter;

    private FragmentManager fm;

    private FloatingActionButton fab;

    private MinistryService database;
    private Cursor entries = null;
    private TimeEntryAdapter adapter = null;
    private int publisherId = 0;
    private Calendar monthPicked = Calendar.getInstance(Locale.getDefault());

    private String dbDateFormatted = "";
    private String dbTimeFrame = "";

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

        fm = getActivity().getSupportFragmentManager();

        monthPicked.set(Calendar.DAY_OF_MONTH, 1);

        if (args != null) {
            monthPicked.set(Calendar.YEAR, args.getInt(ARG_YEAR));
            monthPicked.set(Calendar.MONTH, args.getInt(ARG_MONTH));
            publisherId = args.getInt(ARG_PUBLISHER_ID);
        }

        fm = getActivity().getSupportFragmentManager();

        publishers = view.findViewById(R.id.publishers);
        view_report = view.findViewById(R.id.view_entries);
        report_nav = view.findViewById(R.id.report_nav);

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

        database = new MinistryService(getActivity().getApplicationContext());
        //adapter = new TimeEntryAdapter(getActivity().getApplicationContext(), entries);
        //setListAdapter(adapter);

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

    public void updateList() {
        month.setText(mMonth);
        year.setText(mYear);
/*
        database.openWritable();
        if (PrefUtils.shouldCalculateRolloverTime(getActivity())) {
            entries = database.fetchTimeEntriesByPublisherAndMonth(publisherId, dbDateFormatted, dbTimeFrame);
        } else {
            entries = database.fetchTimeEntriesByPublisherAndMonthNoRollover(publisherId, dbDateFormatted, dbTimeFrame);
        }
        adapter.changeCursor(entries);
        database.close();
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

        //calculateValues();
        //updateList();

        view_report.setText(R.string.view_month_report);
        adjustMonth(0);
        updateList();
        loadPublisherAdapter();
    }

    public void calculateValues() {
        dbDateFormatted = TimeUtils.dbDateFormat.format(monthPicked.getTime());
        dbTimeFrame = "month";

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
    }

    public void adjustMonth(int addValue) {
        monthPicked.add(Calendar.MONTH, addValue);

        mMonth = TimeUtils.fullMonthFormat.format(monthPicked.getTime()).toUpperCase(Locale.getDefault());
        mYear = String.valueOf(monthPicked.get(Calendar.YEAR)).toUpperCase(Locale.getDefault());

        //saveSharedPrefs();
    }

    private void saveSharedPrefs() {
        if (getActivity() != null)
            PrefUtils.setSummaryMonthAndYear(getActivity(), monthPicked);
    }

    public void refresh() {
        updateList();
    }

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