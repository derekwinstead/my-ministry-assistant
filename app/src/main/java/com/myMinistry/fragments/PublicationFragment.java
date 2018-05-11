package com.myMinistry.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.adapters.NavDrawerMenuItemAdapter;
import com.myMinistry.adapters.TitleAndDateAdapterUpdated;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.provider.MinistryContract.LiteratureType;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.ui.MainActivity;
import com.myMinistry.utils.AppConstants;

public class PublicationFragment extends ListFragment {
    private MinistryService database;
    private Spinner myspinner;
    private TitleAndDateAdapterUpdated adapter;
    private Cursor cursor;
    private int literatureTypeId = 0;
    private FragmentManager fm;

    private FloatingActionButton fab;

    private NavDrawerMenuItemAdapter sadapter;

    public PublicationFragment newInstance() {
        return new PublicationFragment();
    }

    public PublicationFragment newInstance(int literatureTypeId) {
        PublicationFragment f = new PublicationFragment();
        Bundle args = new Bundle();
        args.putInt(AppConstants.ARG_PUBLICATION_ID, literatureTypeId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.publication, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View root = inflater.inflate(R.layout.publications, container, false);
        Bundle args = getArguments();

        if (args != null && args.containsKey(AppConstants.ARG_PUBLICATION_ID))
            literatureTypeId = args.getInt(AppConstants.ARG_PUBLICATION_ID);

        database = new MinistryService(getActivity());
        myspinner = root.findViewById(R.id.myspinner);

        fab = root.findViewById(R.id.fab);

        fm = getActivity().getSupportFragmentManager();

        sadapter = new NavDrawerMenuItemAdapter(getActivity().getApplicationContext());

        database.openWritable();
        cursor = database.fetchActiveTypesOfLiterature();
        int default_position = 0;
        while (cursor.moveToNext()) {
            sadapter.addItem(new NavDrawerMenuItem(cursor.getString(cursor.getColumnIndex(LiteratureType.NAME)), Helper.getIconResIDByLitTypeID(cursor.getInt(cursor.getColumnIndex(LiteratureType._ID))), cursor.getInt(cursor.getColumnIndex(LiteratureType._ID))));

            if (cursor.getInt(cursor.getColumnIndex(LiteratureType.DEFAULT)) == AppConstants.ACTIVE)
                default_position = cursor.getPosition();
        }

        myspinner.setAdapter(sadapter);
        myspinner.setSelection(default_position);
        myspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                updateLiteratureList(sadapter.getItem(position).getID());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        database.close();

        if (literatureTypeId > 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                if (cursor.getInt(cursor.getColumnIndex(LiteratureType._ID)) == literatureTypeId) {
                    myspinner.setSelection(cursor.getPosition());
                    break;
                }
            }
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditor(PublicationEditorFragment.CREATE_ID);
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;

        getActivity().setTitle(R.string.navdrawer_item_publications);

        database.openWritable();

        adapter = new TitleAndDateAdapterUpdated(getActivity().getApplicationContext(), null, R.string.last_placed_on);
        setListAdapter(adapter);
        database.close();
/*
        if (is_dual_pane) {
            fab.setVisibility(View.GONE);

            PublicationEditorFragment f = new PublicationEditorFragment().newInstance();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.secondary_fragment_container, f, "secondary");
            transaction.commit();
        }
        */
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_publication_manager:
                ((MainActivity) getActivity()).goToNavDrawerItem(MainActivity.PUBLICATION_MANAGER_ID);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateLiteratureList(int typeID) {
        if (literatureTypeId != typeID) {
            literatureTypeId = typeID;
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                if (cursor.getInt(cursor.getColumnIndex(LiteratureType._ID)) == literatureTypeId) {
                    myspinner.setSelection(cursor.getPosition());
                    break;
                }
            }
        }

        database.openWritable();
        adapter.loadNewData(database.fetchLiteratureByTypeWithActivityDates(literatureTypeId));
        database.close();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        openEditor(id);
    }

    public void openEditor(long id) {
        PublicationEditorFragment f = new PublicationEditorFragment().newInstance(id);
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.primary_fragment_container, f, "main");
        transaction.commit();
    }
}