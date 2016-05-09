package com.myMinistry.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.adapters.NavDrawerMenuItemAdapter;
import com.myMinistry.dialogfragments.PublisherNewDialogFragment;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.util.PrefUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SummaryNavigationFragment extends Fragment {
    public static String ARG_PUBLISHER_ID = "publisher_id";
    private static int SUMMARY = 0;
    private static int ENTRIES = 1;

    private boolean is_dual_pane = false;

    private String mMonth, mYear = "";
    private int view_type_position = SUMMARY;
    private Spinner publishers, view_type;
    private TextView month, year;
    private Calendar monthPicked = Calendar.getInstance();
    private int publisherId = 0;
    private final SimpleDateFormat buttonFormat = new SimpleDateFormat("MMMM", Locale.getDefault());

    private NavDrawerMenuItemAdapter pubsAdapter;

    private FragmentManager fm;

    private MinistryService database;

    public static SummaryNavigationFragment newInstance(int _publisherID) {
        SummaryNavigationFragment f = new SummaryNavigationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PUBLISHER_ID, _publisherID);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.summary_navigation, container, false);

        Bundle args = getArguments();

        if(args != null && args.containsKey(ARG_PUBLISHER_ID))
            publisherId = args.getInt(ARG_PUBLISHER_ID);

        //setHasOptionsMenu(true);

        fm = getActivity().getSupportFragmentManager();

        database = new MinistryService(getActivity().getApplicationContext());

        monthPicked.set(Calendar.MONTH, PrefUtils.getSummaryMonth(getActivity(), monthPicked));
        monthPicked.set(Calendar.YEAR, PrefUtils.getSummaryYear(getActivity(), monthPicked));

        setPublisherId(PrefUtils.getPublisherId(getActivity().getApplicationContext()));

        publishers = (Spinner) root.findViewById(R.id.publishers);
        view_type = (Spinner) root.findViewById(R.id.view_type);

        month = (TextView) root.findViewById(R.id.month);
        year = (TextView) root.findViewById(R.id.year);

        root.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjustMonth(1);

                calculateSummaryValues();
                fillPublisherSummary();
                fillNavigationContent();
                displayTimeEntries();
            }
        });

        root.findViewById(R.id.prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjustMonth(-1);

                calculateSummaryValues();
                fillPublisherSummary();
                fillNavigationContent();
                displayTimeEntries();
            }
        });

        root.findViewById(R.id.monthYear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        pubsAdapter = new NavDrawerMenuItemAdapter(getActivity().getApplicationContext());

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

        view_type.setAdapter(spinnerArrayAdapterType);
        view_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                view_type_position = position;
                if(position == SUMMARY) {
                    fillNavigationContent();
                    /*
                    //((MainActivity)getActivity()).setTitle(R.string.menu_entries);

                    FragmentTransaction ft = fm.beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                    new SummaryFragment();
                    SummaryFragment f1 = SummaryFragment.newInstance(PrefUtils.getPublisherId(getActivity().getApplicationContext()));

                    ft.replace(R.id.summary_navigation_button_content, f1);
                    ft.commit();
                    */
                }
                else if(position == ENTRIES) {
                    /*
                    Calendar date = Calendar.getInstance(Locale.getDefault());

                    // Create new transaction
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                    // Create new fragment
                    TimeEntriesFragment f = new TimeEntriesFragment().newInstance(PrefUtils.getSummaryMonth(getActivity().getApplicationContext(), date), PrefUtils.getSummaryYear(getActivity().getApplicationContext(), date), PrefUtils.getPublisherId(getActivity().getApplicationContext()));

                    // Replace whatever is in the fragment_container view with this fragment,
                    ft.replace(R.id.summary_navigation_button_content, f);

                    // Commit the transaction
                    ft.commit();
                    */
                    fillNavigationContent();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);

        is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;

        if(is_dual_pane) {
            view_type.setVisibility(View.GONE);
        }

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

    public void setDate(Calendar _date) {
        monthPicked.set(Calendar.YEAR, _date.get(Calendar.YEAR));
        monthPicked.set(Calendar.MONTH, _date.get(Calendar.MONTH));
        saveSharedPrefs();
    }

    public void refresh() {
        fillPublisherSummary();
        displayTimeEntries();
    }

    private void saveSharedPrefs() {
        if(getActivity() != null)
            PrefUtils.setSummaryMonthAndYear(getActivity(), monthPicked);
    }

    public void displayTimeEntries() {
        if (is_dual_pane || view_type_position == ENTRIES) {
            int FRAG_CONTAINER_ID;
            if (is_dual_pane) {
                FRAG_CONTAINER_ID = R.id.secondary_fragment_container;
            } else {
                FRAG_CONTAINER_ID = R.id.summary_navigation_button_content;
            }

            Fragment rf = fm.findFragmentById(FRAG_CONTAINER_ID);

            if(rf instanceof TimeEntriesFragment) {
                TimeEntriesFragment f = (TimeEntriesFragment) fm.findFragmentById(FRAG_CONTAINER_ID);

                f.setPublisherId(publisherId);

                f.switchToMonthList(monthPicked);
            } else {
                TimeEntriesFragment f = new TimeEntriesFragment().newInstance(monthPicked.get(Calendar.MONTH), monthPicked.get(Calendar.YEAR), publisherId);
                FragmentTransaction ft = fm.beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.replace(FRAG_CONTAINER_ID, f);
                ft.commit();
            }
        }
    }

    public void fillPublisherSummary() {
        month.setText(mMonth);
        year.setText(mYear);
    }

    public void calculateSummaryValues() {
        mMonth = buttonFormat.format(monthPicked.getTime()).toUpperCase(Locale.getDefault());
        mYear = String.valueOf(monthPicked.get(Calendar.YEAR)).toUpperCase(Locale.getDefault());

        //dbDateFormatted = TimeUtils.dbDateFormat.format(monthPicked.getTime());
        //dbTimeFrame = "month";
    }

    private void loadPublisherAdapter() {
        int initialSelection = 0;
        // Add new publisher item
        pubsAdapter.addItem(new NavDrawerMenuItem(getActivity().getApplicationContext().getString(R.string.menu_add_new_publisher), R.drawable.ic_drawer_publisher_female, MinistryDatabase.CREATE_ID));

        database.openWritable();
        final Cursor cursor = database.fetchActivePublishers();
        while(cursor.moveToNext()) {
            if(cursor.getInt(cursor.getColumnIndex(MinistryContract.Publisher._ID)) == publisherId)
                initialSelection = pubsAdapter.getCount();
            pubsAdapter.addItem(new NavDrawerMenuItem(cursor.getString(cursor.getColumnIndex(MinistryContract.Publisher.NAME)), R.drawable.ic_drawer_publisher_female, cursor.getInt(cursor.getColumnIndex(MinistryContract.Publisher._ID))));
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
                    //animatePage(DIRECTION_NO_CHANGE);
                    fillPublisherSummary();
                    displayTimeEntries();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void fillNavigationContent() {
        if (!is_dual_pane) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            Fragment frag = fm.findFragmentById(R.id.summary_navigation_button_content);

            if(view_type_position == SUMMARY) {
                if(!(frag instanceof SummaryFragment)) {
                    SummaryFragment f = SummaryFragment.newInstance(PrefUtils.getPublisherId(getActivity().getApplicationContext()));

                    if(frag != null)
                        ft.remove(frag);

                    ft.add(R.id.summary_navigation_button_content, f);
                    ft.commit();
                }
                else {
                    SummaryFragment f = (SummaryFragment) fm.findFragmentById(R.id.summary_navigation_button_content);
                    f.setDate(monthPicked);
                    f.calculateSummaryValues();
                    f.fillPublisherSummary();
                }
            }
            else if(view_type_position == ENTRIES) {
                if(!(frag instanceof TimeEntriesFragment)) {
                    TimeEntriesFragment f = new TimeEntriesFragment().newInstance(monthPicked.get(Calendar.MONTH), monthPicked.get(Calendar.YEAR), publisherId);

                    if(frag != null)
                        ft.remove(frag);

                    ft.replace(R.id.summary_navigation_button_content, f);
                    ft.commit();

                    //f.setPublisherId(publisherId);

                    //f.switchToMonthList(monthPicked);
                } else {
                    TimeEntriesFragment f = (TimeEntriesFragment) fm.findFragmentById(R.id.summary_navigation_button_content);
                    f.setPublisherId(publisherId);
                    f.switchToMonthList(monthPicked);
                    /*
                    TimeEntriesFragment f = new TimeEntriesFragment().newInstance(monthPicked.get(Calendar.MONTH), monthPicked.get(Calendar.YEAR), publisherId);
                    ft.replace(R.id.summary_navigation_button_content, f);
                    ft.commit();
                    */
                }
            }
        }
    }
/*
    @Override
    public void OnDestory() {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment frag = fm.findFragmentById(R.id.summary_navigation_button_content);

        if(frag != null) { ft.remove(frag); }

        ft.commit();

        super.onDestroy();
    }
    */
}