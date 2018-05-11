package com.myMinistry.ui.householders;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.myMinistry.R;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.utils.AppConstants;
import com.myMinistry.utils.TimeUtils;
import com.squareup.phrase.Phrase;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

public class HouseholdersListFragment extends Fragment implements HouseholdersListAdapter.ItemClickListener {
    private MinistryService database;
    private FloatingActionButton fab;
    private TextView empty_view;
    private RecyclerView householders;
    private HouseholdersListAdapter householders_adapter;
    private final ArrayList<HouseholderItem> householders_arraylist = new ArrayList<>();

    public HouseholdersListFragment newInstance() {
        return new HouseholdersListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.householders, container, false);

        fab = view.findViewById(R.id.fab);

        empty_view = view.findViewById(R.id.empty_view);
        householders = view.findViewById(R.id.householders_list);

        householders.setLayoutManager(new LinearLayoutManager(getContext()));
        householders_adapter = new HouseholdersListAdapter(getActivity().getApplicationContext(), householders_arraylist);
        householders_adapter.setClickListener(this);
        householders.setAdapter(householders_adapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        database = new MinistryService(getActivity());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditor(AppConstants.CREATE_ID);
            }
        });

        loadCursor();
    }

    public void openEditor(long id) {
        HouseholderEditFragment f = new HouseholderEditFragment().newInstance(id);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.primary_fragment_container, f, "main");
        transaction.commit();
    }

    private void loadCursor() {
        Calendar date = Calendar.getInstance();
        if (!database.isOpen())
            database.openWritable();

        // Load up the array list for the adapter
        Cursor cursor = database.fetchAllHouseholdersWithActivityDates();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            HouseholderItem item = new HouseholderItem(cursor);

            if (!TextUtils.isEmpty(item.getDate())) {
                try {
                    date.setTime(TimeUtils.dbDateFormat.parse(item.getDate()));
                    item.setLastActiveString(Phrase.from(getActivity().getApplicationContext(), R.string.last_visited_on).put("date", DateUtils.formatDateTime(getActivity().getApplicationContext(), date.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY)).format().toString());
                } catch (ParseException e) {
                    item.setLastActiveString(getActivity().getApplicationContext().getResources().getString(R.string.no_activity));
                }
            } else {
                item.setLastActiveString(getActivity().getApplicationContext().getResources().getString(R.string.no_activity));
            }

            householders_arraylist.add(item);
        }
        cursor.close();
        database.close();

        householders_adapter.notifyDataSetChanged();

        if (householders_arraylist.isEmpty()) {
            householders.setVisibility(View.GONE);
            empty_view.setVisibility(View.VISIBLE);
        } else {
            householders.setVisibility(View.VISIBLE);
            empty_view.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(getActivity().getApplicationContext(), "You clicked " + householders_arraylist.get(position).getName() + " on row number " + position, Toast.LENGTH_SHORT).show();
    }
}