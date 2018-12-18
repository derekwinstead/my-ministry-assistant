package com.myMinistry.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.myMinistry.R;
import com.myMinistry.adapters.TimeEntryAdapter;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.utils.AppConstants;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;

public class HouseholderActivityFragment extends ListFragment {
    static final long CREATE_ID = (long) AppConstants.CREATE_ID;
    private long householderID = CREATE_ID;

    private MinistryService database;
    private Cursor activity;

    private FragmentManager fm;

    private TimeEntryAdapter adapter;

    public HouseholderActivityFragment newInstance() {
        return new HouseholderActivityFragment();
    }

    public HouseholderActivityFragment newInstance(long _householderID) {
        HouseholderActivityFragment f = new HouseholderActivityFragment();
        Bundle args = new Bundle();
        args.putLong(AppConstants.ARG_HOUSEHOLDER_ID, _householderID);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.householder_activity, container, false);
        Bundle args = getArguments();

        if (args != null)
            setHouseholder(args.getLong(AppConstants.ARG_HOUSEHOLDER_ID));

        fm = getActivity().getSupportFragmentManager();

        adapter = new TimeEntryAdapter(getActivity().getApplicationContext(), activity);
        setListAdapter(adapter);

        database = new MinistryService(getActivity().getApplicationContext());

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(R.string.householder_activity);

        if (!database.isOpen())
            database.openWritable();

        activity = database.fetchActivityForHouseholder((int) householderID);
        adapter.changeCursor(activity);
        database.close();
    }

    public void setHouseholder(long _id) {
        householderID = _id;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        int LAYOUT_ID = R.id.primary_fragment_container;

        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        Fragment frag = fm.findFragmentById(LAYOUT_ID);
        TimeEditorFragment f = new TimeEditorFragment().newInstance((int) id);

        if (frag != null)
            ft.remove(frag);

        ft.add(LAYOUT_ID, f);
        ft.addToBackStack(null);

        ft.commit();
    }
}