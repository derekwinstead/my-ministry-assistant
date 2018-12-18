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

public class PublisherActivityFragment extends ListFragment {
    static final long CREATE_ID = (long) AppConstants.CREATE_ID;
    private long publisherId = CREATE_ID;

    private MinistryService database;
    private Cursor activity;

    private FragmentManager fm;

    private TimeEntryAdapter adapter;

    public PublisherActivityFragment newInstance() {
        return new PublisherActivityFragment();
    }

    public PublisherActivityFragment newInstance(long publisherId) {
        PublisherActivityFragment f = new PublisherActivityFragment();
        Bundle args = new Bundle();
        args.putLong(AppConstants.ARG_PUBLISHER_ID, publisherId);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.publisher_activity, container, false);
        Bundle args = getArguments();

        if (args != null)
            setPublisher(args.getLong(AppConstants.ARG_PUBLISHER_ID));

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

        activity = database.fetchActivityForPublisher((int) publisherId);
        adapter.changeCursor(activity);
        database.close();
    }

    public void setPublisher(long id) {
        publisherId = id;
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