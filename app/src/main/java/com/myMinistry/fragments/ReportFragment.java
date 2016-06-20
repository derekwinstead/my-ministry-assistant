package com.myMinistry.fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.adapters.NavDrawerMenuItemAdapter;
import com.myMinistry.dialogfragments.PublisherNewDialogFragment;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryContract.LiteratureType;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.ui.MainActivity;
import com.myMinistry.util.PrefUtils;
import com.myMinistry.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReportFragment extends Fragment {
    public static String ARG_PUBLISHER_ID = "publisher_id";

    private String mBSText, mRVText, mTotalHoursCount, mPlacementsCount, mVideoShowings, mRVCount, mBSCount = "";

    private FloatingActionButton fab;

    private TextView total_hours_count, return_visits_text, return_visits_count, bible_studies_text, bible_studies_count, placements_count, video_showings, view_entries;
    private LinearLayout placement_list;
    private Calendar monthPicked = Calendar.getInstance();
    private int publisherId = 0;
    private final SimpleDateFormat buttonFormat = new SimpleDateFormat("MMMM", Locale.getDefault());

    private boolean is_dual_pane = false;

    private MinistryService database;

    private String mMonth, mYear = "";
    private Spinner publishers;
    private TextView month, year;

    private NavDrawerMenuItemAdapter pubsAdapter;

    private FragmentManager fm;

    public static ReportFragment newInstance(int _publisherID) {
        ReportFragment f = new ReportFragment();
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
        View root = inflater.inflate(R.layout.report, container, false);

        Bundle args = getArguments();

        if(args != null && args.containsKey(ARG_PUBLISHER_ID))
            publisherId = args.getInt(ARG_PUBLISHER_ID);

        setPublisherId(PrefUtils.getPublisherId(getActivity().getApplicationContext()));

        setHasOptionsMenu(true);

        database = new MinistryService(getActivity().getApplicationContext());

        fm = getActivity().getSupportFragmentManager();

        publishers = (Spinner) root.findViewById(R.id.publishers);
        view_entries = (TextView) root.findViewById(R.id.view_entries);

        month = (TextView) root.findViewById(R.id.month);
        year = (TextView) root.findViewById(R.id.year);

        placements_count = (TextView) root.findViewById(R.id.placements_count);
        video_showings = (TextView) root.findViewById(R.id.video_showings);

        fab = (FloatingActionButton) root.findViewById(R.id.fab);

        return_visits_text = (TextView) root.findViewById(R.id.return_visits_text);
        return_visits_count = (TextView) root.findViewById(R.id.return_visits_count);
        bible_studies_text = (TextView) root.findViewById(R.id.bible_studies_text);
        bible_studies_count = (TextView) root.findViewById(R.id.bible_studies_count);
        total_hours_count = (TextView) root.findViewById(R.id.total_hours_count);
        placement_list = (LinearLayout) root.findViewById(R.id.placement_list);
/*
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this.getActivity().getApplicationContext(), R.layout.li_spinner_item);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.li_spinner_item_dropdown);

        for(String name : getResources().getStringArray(R.array.summary_time_span)) {
            spinnerArrayAdapter.add(name);
        }*/
/*
        ArrayAdapter<String> spinnerArrayAdapterType = new ArrayAdapter<>(this.getActivity().getApplicationContext(), R.layout.li_spinner_item);
        spinnerArrayAdapterType.setDropDownViewResource(R.layout.li_spinner_item_dropdown);

        for(String name : getResources().getStringArray(R.array.summary_nav_view_type)) {
            spinnerArrayAdapterType.add(name);
        }
*/
        root.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjustMonth(1);

                calculateSummaryValues();
                fillPublisherSummary();
                displayTimeEntries();
            }
        });

        root.findViewById(R.id.prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjustMonth(-1);

                calculateSummaryValues();
                fillPublisherSummary();
                displayTimeEntries();
            }
        });

        view_entries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar date = Calendar.getInstance(Locale.getDefault());

                Fragment frag = fm.findFragmentById(R.id.primary_fragment_container);
                FragmentTransaction ft = fm.beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                TimeEntriesFragment f = new TimeEntriesFragment().newInstance(PrefUtils.getSummaryMonth(getActivity().getApplicationContext(), date), PrefUtils.getSummaryYear(getActivity().getApplicationContext(), date), PrefUtils.getPublisherId(getActivity().getApplicationContext()));

                if(frag != null)
                    ft.remove(frag);

                ft.add(R.id.primary_fragment_container, f);
                ft.addToBackStack(null);

                ft.commit();
            }
        });

        pubsAdapter = new NavDrawerMenuItemAdapter(getActivity().getApplicationContext());

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);

        is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;

        if(is_dual_pane) {
            fab.setVisibility(View.GONE);
            view_entries.setVisibility(View.GONE);

            TimeEntriesFragment f = new TimeEntriesFragment().newInstance(monthPicked.get(Calendar.MONTH), monthPicked.get(Calendar.YEAR), publisherId);
            FragmentTransaction ft = fm.beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.replace(R.id.secondary_fragment_container, f);
            ft.commit();
        } else {
            view_entries.setText(R.string.view_month_entries);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity)getActivity()).goToNavDrawerItem(MainActivity.TIME_ENTRY_ID);
                }
            });
        }

        loadPublisherAdapter();
        calculateSummaryValues();
        fillPublisherSummary();
    }

    private void saveSharedPrefs() {
        if(getActivity() != null)
            PrefUtils.setSummaryMonthAndYear(getActivity(), monthPicked);
    }

    public void fillPublisherSummary() {
        month.setText(mMonth);
        year.setText(mYear);

        total_hours_count.setText(mTotalHoursCount);

        placements_count.setText(mPlacementsCount);
        video_showings.setText(mVideoShowings);

        bible_studies_text.setText(mBSText);
        bible_studies_count.setText(mBSCount);
        return_visits_text.setText(mRVText);
        return_visits_count.setText(mRVCount);
    }

    public void calculateSummaryValues() {
        if(!database.isOpen())
            database.openWritable();

        mMonth = buttonFormat.format(monthPicked.getTime()).toUpperCase(Locale.getDefault());
        mYear = String.valueOf(monthPicked.get(Calendar.YEAR)).toUpperCase(Locale.getDefault());

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
            tv2.setGravity(Gravity.END);

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
            switch(entryTypes.getInt(entryTypes.getColumnIndex(MinistryContract.EntryType._ID))) {
                case MinistryDatabase.ID_BIBLE_STUDY:
                    mBSText = entryTypes.getString(entryTypes.getColumnIndex(MinistryContract.EntryType.NAME));
                    mBSCount = String.valueOf(entryTypes.getInt(2));
                    break;
                case MinistryDatabase.ID_RETURN_VISIT:
                    mRVText = entryTypes.getString(entryTypes.getColumnIndex(MinistryContract.EntryType.NAME));
                    mRVCount = String.valueOf(entryTypes.getInt(2));
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
            retVal.append("\n").append(pubs.getString(pubs.getColumnIndex(MinistryContract.Publisher.NAME)));

            /** Set total hours */
            retVal.append("\n").append(getResources().getString(R.string.total_time)).append(": ");
            if(PrefUtils.shouldCalculateRolloverTime(getActivity()))
                retVal.append(TimeUtils.getTimeLength(database.fetchListOfHoursForPublisher(formattedDate, pubs.getInt(pubs.getColumnIndex(MinistryContract.Publisher._ID)), "month"), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity())));
            else
                retVal.append(TimeUtils.getTimeLength(database.fetchListOfHoursForPublisherNoRollover(formattedDate, pubs.getInt(pubs.getColumnIndex(MinistryContract.Publisher._ID)), "month"), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity())));

            /** Fill all the publication amounts */
            Cursor lit = database.fetchTypesOfLiteratureCountsForPublisher(pubs.getInt(pubs.getColumnIndex(MinistryContract.Publisher._ID)), formattedDate, "month");
            for(lit.moveToFirst();!lit.isAfterLast();lit.moveToNext()) {
                if(lit.getInt(2) > 0) {
                    retVal.append("\n").append(lit.getString(lit.getColumnIndex(MinistryContract.LiteratureType.NAME))).append(": ");
                    retVal.append(String.valueOf(lit.getInt(2)));
                }
            }
            lit.close();

            /** Now for the other entry types */
            Cursor entryTypes = database.fetchEntryTypeCountsForPublisher(pubs.getInt(pubs.getColumnIndex(MinistryContract.Publisher._ID)), formattedDate, "month");
            for(entryTypes.moveToFirst();!entryTypes.isAfterLast();entryTypes.moveToNext()) {
                if(entryTypes.getInt(2) > 0) {
                    retVal.append("\n").append(entryTypes.getString(lit.getColumnIndex(MinistryContract.EntryType.NAME))).append(": ");
                    if(entryTypes.getInt(entryTypes.getColumnIndex(MinistryContract.EntryType._ID)) == MinistryDatabase.ID_RBC)
                        retVal.append(TimeUtils.getTimeLength(database.fetchListOfRBCHoursForPublisher(formattedDate, pubs.getInt(pubs.getColumnIndex(MinistryContract.Publisher._ID)), "month"), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity())));
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

    private void loadPublisherAdapter() {
        int initialSelection = 0;
        // Add new publisher item
        pubsAdapter.addItem(new NavDrawerMenuItem(getActivity().getApplicationContext().getString(R.string.menu_add_new_publisher), R.drawable.ic_drawer_publisher_male, MinistryDatabase.CREATE_ID));

        database.openWritable();
        final Cursor cursor = database.fetchActivePublishers();
        while(cursor.moveToNext()) {
            if(cursor.getInt(cursor.getColumnIndex(MinistryContract.Publisher._ID)) == publisherId)
                initialSelection = pubsAdapter.getCount();
            pubsAdapter.addItem(new NavDrawerMenuItem(cursor.getString(cursor.getColumnIndex(MinistryContract.Publisher.NAME))
                    ,getResources().getIdentifier("ic_drawer_publisher_" + cursor.getString(cursor.getColumnIndex(MinistryContract.Publisher.GENDER)), "drawable", getActivity().getPackageName())
                    ,cursor.getInt(cursor.getColumnIndex(MinistryContract.Publisher._ID))));
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
                    f.setPositiveButton(new PublisherNewDialogFragment.PublisherNewDialogFragmentListener() {
                        @Override
                        public void setPositiveButton(int _ID, String _name) {
                            pubsAdapter.addItem(new NavDrawerMenuItem(_name, R.drawable.ic_drawer_publisher_female, _ID));
                            publishers.setSelection(pubsAdapter.getCount() - 1);
                        }
                    });
                    f.show(fm, "PublisherNewDialogFragment");
                }
                else {
                    setPublisherId(pubsAdapter.getItem(position).getID());
                    PrefUtils.setPublisherId(getActivity().getApplicationContext(), pubsAdapter.getItem(position).getID());
                    calculateSummaryValues();
                    fillPublisherSummary();
                    displayTimeEntries();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    public void displayTimeEntries() {
        if (is_dual_pane) {
            Fragment rf = fm.findFragmentById(R.id.secondary_fragment_container);

            if(rf instanceof TimeEntriesFragment) {
                TimeEntriesFragment f = (TimeEntriesFragment) fm.findFragmentById(R.id.secondary_fragment_container);

                f.setPublisherId(publisherId);

                f.switchToMonthList(monthPicked);
            } else {
                TimeEntriesFragment f = new TimeEntriesFragment().newInstance(monthPicked.get(Calendar.MONTH), monthPicked.get(Calendar.YEAR), publisherId);
                FragmentTransaction ft = fm.beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.replace(R.id.secondary_fragment_container, f);
                ft.commit();
            }
        }
    }
}