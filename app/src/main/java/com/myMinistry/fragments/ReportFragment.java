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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.adapters.NavDrawerMenuItemAdapter;
import com.myMinistry.adapters.ReportPublicationSummaryAdapter;
import com.myMinistry.bean.ReportPublication;
import com.myMinistry.dialogfragments.PublisherNewDialogFragment;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryContract.LiteratureType;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.ui.MainActivity;
import com.myMinistry.utils.AppConstants;
import com.myMinistry.utils.PrefUtils;
import com.myMinistry.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ReportFragment extends Fragment {
    private final String ARG_YEAR = "year";
    private final String ARG_MONTH = "month";
    private final String ARG_PUBLISHER_ID = "publisher_id";

    private String mBSText, mRVText, mTotalHoursCount, mPlacementsCount, mVideoShowings, mRVCount, mBSCount = "";

    private FloatingActionButton fab;

    private TextView total_hours_count, return_visits_text, return_visits_count, bible_studies_text, bible_studies_count, placements_count, video_showings;
    private Button view_entries;
    private final Calendar monthPicked = Calendar.getInstance();
    private int publisherId = 0;
    private boolean calculate_rollover_time;

    private MinistryService database;

    private String mMonth, mYear = "";
    private Spinner publishers;
    private TextView month, year;

    private NavDrawerMenuItemAdapter pubsAdapter;

    private FragmentManager fm;

    private ReportPublicationSummaryAdapter placement_list_adapter;
    private final ArrayList<ReportPublication> user_placements = new ArrayList<>();

    public ReportFragment newInstance(int publisherId) {
        ReportFragment f = new ReportFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PUBLISHER_ID, publisherId);
        f.setArguments(args);
        return f;
    }

    public ReportFragment newInstance(int publisherId, int month, int year) {
        ReportFragment f = new ReportFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_PUBLISHER_ID, publisherId);
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

        if (args != null && args.containsKey(ARG_PUBLISHER_ID)) {
            if (args.containsKey(ARG_YEAR))
                monthPicked.set(Calendar.YEAR, args.getInt(ARG_YEAR));

            if (args.containsKey(ARG_MONTH))
                monthPicked.set(Calendar.MONTH, args.getInt(ARG_MONTH));

            publisherId = args.getInt(ARG_PUBLISHER_ID);
        }

        if (publisherId != 0)
            setPublisherId(publisherId);
        else
            setPublisherId(PrefUtils.getPublisherId(getActivity().getApplicationContext()));

        setHasOptionsMenu(true);

        database = new MinistryService(getActivity().getApplicationContext());

        fm = getActivity().getSupportFragmentManager();
        calculate_rollover_time = PrefUtils.shouldCalculateRolloverTime(getActivity());
        publishers = root.findViewById(R.id.publishers);
        view_entries = root.findViewById(R.id.view_entries);

        month = root.findViewById(R.id.month);
        year = root.findViewById(R.id.year);

        placements_count = root.findViewById(R.id.placements_count);
        video_showings = root.findViewById(R.id.video_showings);

        fab = root.findViewById(R.id.fab);

        return_visits_text = root.findViewById(R.id.return_visits_text);
        return_visits_count = root.findViewById(R.id.return_visits_count);
        bible_studies_text = root.findViewById(R.id.bible_studies_text);
        bible_studies_count = root.findViewById(R.id.bible_studies_count);
        total_hours_count = root.findViewById(R.id.total_hours_count);

        RecyclerView placement_list;
        placement_list = root.findViewById(R.id.user_placements);
        placement_list.setHasFixedSize(true);
        placement_list.setLayoutManager(new LinearLayoutManager(getContext()));
        placement_list_adapter = new ReportPublicationSummaryAdapter(getContext(), user_placements);
        placement_list.setAdapter(placement_list_adapter);

        root.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monthPicked.add(Calendar.MONTH, 1);

                calculateReportValues();
                fillPublisherReport();
            }
        });

        root.findViewById(R.id.prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monthPicked.add(Calendar.MONTH, -1);

                calculateReportValues();
                fillPublisherReport();
            }
        });

        view_entries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeEntriesFragment f = new TimeEntriesFragment().newInstance(publisherId, monthPicked.get(Calendar.MONTH), monthPicked.get(Calendar.YEAR));
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.primary_fragment_container, f, "main");
                transaction.commit();
            }
        });

        pubsAdapter = new NavDrawerMenuItemAdapter(getActivity().getApplicationContext());

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);

        view_entries.setText(R.string.view_month_entries);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).goToNavDrawerItem(MainActivity.TIME_ENTRY_ID);
            }
        });

        loadPublisherAdapter();
        fillPublisherReport();
    }

    private void fillPublisherReport() {
        month.setText(mMonth);
        year.setText(mYear);

        total_hours_count.setText(mTotalHoursCount);

        placements_count.setText(mPlacementsCount);
        video_showings.setText(mVideoShowings);

        bible_studies_text.setText(mBSText);
        bible_studies_count.setText(mBSCount);
        return_visits_text.setText(mRVText);
        return_visits_count.setText(mRVCount);

        placement_list_adapter.notifyDataSetChanged();
    }

    private void calculateReportValues() {
        mMonth = TimeUtils.fullMonthFormat.format(monthPicked.getTime()).toUpperCase(Locale.getDefault());
        mYear = String.valueOf(monthPicked.get(Calendar.YEAR)).toUpperCase(Locale.getDefault());

        String dbDateFormatted = TimeUtils.dbDateFormat.format(monthPicked.getTime());
        String dbTimeFrame = "month";

        if (!database.isOpen())
            database.openWritable();

        // Total Time
        if (PrefUtils.shouldCalculateRolloverTime(getActivity()))
            mTotalHoursCount = TimeUtils.getTimeLength(database.fetchListOfHoursForPublisher(dbDateFormatted, publisherId, dbTimeFrame), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity()));
        else
            mTotalHoursCount = TimeUtils.getTimeLength(database.fetchListOfHoursForPublisherNoRollover(dbDateFormatted, publisherId, dbTimeFrame), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity()));

        // Placements
        mPlacementsCount = String.valueOf(database.fetchPlacementsCountForPublisher(publisherId, dbDateFormatted, dbTimeFrame));
        // Video Showings
        mVideoShowings = String.valueOf(database.fetchVideoShowingsCountForPublisher(publisherId, dbDateFormatted, dbTimeFrame));

        // All user placements
        Cursor literatureTypes = database.fetchTypesOfLiteratureCountsForPublisher(publisherId, dbDateFormatted, dbTimeFrame);
        for (literatureTypes.moveToFirst(); !literatureTypes.isAfterLast(); literatureTypes.moveToNext()) {
            if (user_placements.size() <= literatureTypes.getPosition()) {
                user_placements.add(new ReportPublication(literatureTypes.getString(literatureTypes.getColumnIndex(LiteratureType.NAME)), literatureTypes.getInt(2)));
            } else {
                user_placements.set(literatureTypes.getPosition(), new ReportPublication(literatureTypes.getString(literatureTypes.getColumnIndex(LiteratureType.NAME)), literatureTypes.getInt(2)));
            }
        }
        literatureTypes.close();
        // End All user placements

        // Return visits and Bible studies
        Cursor entryTypes = database.fetchEntryTypeCountsForPublisher(publisherId, dbDateFormatted, dbTimeFrame);
        for (entryTypes.moveToFirst(); !entryTypes.isAfterLast(); entryTypes.moveToNext()) {
            switch (entryTypes.getInt(entryTypes.getColumnIndex(MinistryContract.EntryType._ID))) {
                case AppConstants.ID_ENTRY_TYPE_BIBLE_STUDY:
                    mBSText = entryTypes.getString(entryTypes.getColumnIndex(MinistryContract.EntryType.NAME));
                    mBSCount = String.valueOf(entryTypes.getInt(2));
                    break;
                case AppConstants.ID_ENTRY_TYPE_RETURN_VISIT:
                    mRVText = entryTypes.getString(entryTypes.getColumnIndex(MinistryContract.EntryType.NAME));
                    mRVCount = String.valueOf(entryTypes.getInt(2));
                    break;
            }
        }
        entryTypes.close();
        // End Return visits and Bible studies

        database.close();
    }

    // TODO populateShareString() - Make sure this is still needed and correct
    private String populateShareString() {
        StringBuilder retVal = new StringBuilder();
        String formattedDate = TimeUtils.dbDateFormat.format(monthPicked.getTime());

        if (!database.isOpen())
            database.openWritable();

        // Set the date
        retVal.append(TimeUtils.fullMonthFormat.format(monthPicked.getTime())).append(" ").append(monthPicked.get(Calendar.YEAR));

        Cursor pubs = database.fetchActivePublishers();

        // Loop over all the active publishers
        for (pubs.moveToFirst(); !pubs.isAfterLast(); pubs.moveToNext()) {
            int currentPublisherId = pubs.getInt(pubs.getColumnIndex(MinistryContract.Publisher._ID));
            int placementCount;
            int videoCount;

            if (pubs.getPosition() > 0)
                retVal.append("\n");

            // Set publisher's name
            retVal.append("\n").append(pubs.getString(pubs.getColumnIndex(MinistryContract.Publisher.NAME)));

            // Placements
            placementCount = database.fetchPlacementsCountForPublisher(currentPublisherId, formattedDate, "month");
            if (placementCount > 0)
                retVal.append("\n").append(getActivity().getApplicationContext().getString(R.string.placements)).append(": ").append(String.valueOf(placementCount));

            // Video showings
            videoCount = database.fetchVideoShowingsCountForPublisher(currentPublisherId, formattedDate, "month");
            if (videoCount > 0)
                retVal.append("\n").append(getActivity().getApplicationContext().getString(R.string.video_showings)).append(": ").append(String.valueOf(videoCount));

            // Set total time
            retVal.append("\n").append(getResources().getString(R.string.total_time)).append(": ");
            if (PrefUtils.shouldCalculateRolloverTime(getActivity()))
                retVal.append(TimeUtils.getTimeLength(database.fetchListOfHoursForPublisher(formattedDate, currentPublisherId, "month"), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity())));
            else
                retVal.append(TimeUtils.getTimeLength(database.fetchListOfHoursForPublisherNoRollover(formattedDate, currentPublisherId, "month"), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity())));

            // Now for bible studies and return visits
            Cursor entryTypes = database.fetchEntryTypeCountsForPublisher(currentPublisherId, formattedDate, "month");
            for (entryTypes.moveToFirst(); !entryTypes.isAfterLast(); entryTypes.moveToNext()) {
                if (entryTypes.getInt(2) > 0) {
                    retVal.append("\n").append(entryTypes.getString(entryTypes.getColumnIndex(MinistryContract.EntryType.NAME))).append(": ");
                    if (entryTypes.getInt(entryTypes.getColumnIndex(MinistryContract.EntryType._ID)) == AppConstants.ID_ENTRY_TYPE_RBC)
                        retVal.append(TimeUtils.getTimeLength(database.fetchListOfRBCHoursForPublisher(formattedDate, currentPublisherId, "month"), getActivity().getApplicationContext().getString(R.string.hours_label), getActivity().getApplicationContext().getString(R.string.minutes_label), PrefUtils.shouldShowMinutesInTotals(getActivity())));
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
                share.putExtra(Intent.EXTRA_SUBJECT, TimeUtils.fullMonthFormat.format(monthPicked.getTime()) + " " + monthPicked.get(Calendar.YEAR));
                startActivity(Intent.createChooser(share, getResources().getString(R.string.menu_send_report)));

                return true;
        }

        return super.onOptionsItemSelected(item);
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
        pubsAdapter.addItem(new NavDrawerMenuItem(getActivity().getApplicationContext().getString(R.string.menu_add_new_publisher), R.drawable.ic_drawer_publisher_male, AppConstants.CREATE_ID));

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
                if (pubsAdapter.getItem(position).getID() == AppConstants.CREATE_ID) {
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
                    calculateReportValues();
                    fillPublisherReport();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}