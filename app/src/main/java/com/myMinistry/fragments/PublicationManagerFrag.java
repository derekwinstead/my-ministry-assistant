package com.myMinistry.fragments;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.adapters.DialogItemAdapter;
import com.myMinistry.adapters.ItemWithIconAdapter;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.provider.MinistryContract.LiteratureType;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.util.HelpUtils;
import com.myMinistry.util.PrefUtils;

public class PublicationManagerFrag extends ListFragment {
    private boolean is_dual_pane = false;

    private final int RENAME_ID = 0;
    private final int TRANSFER_ID = 1;
    private final int DELETE_ID = 2;

    private ItemWithIconAdapter adapter;
    private ContentValues values = null;
    private MinistryService database;
    private FragmentManager fm;
    private FloatingActionButton fab;

    public PublicationManagerFrag newInstance() {
        return new PublicationManagerFrag();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.publication_manager, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sorting_only, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_alpha:
                PrefUtils.setPublicationTypeSort(getActivity(), MinistryDatabase.SORT_BY_ASC);
                HelpUtils.sortPublicationTypes(getActivity().getApplicationContext(), MinistryDatabase.SORT_BY_ASC);
                reloadCursor();
                return true;
            case R.id.sort_alpha_desc:
                PrefUtils.setPublicationTypeSort(getActivity(), MinistryDatabase.SORT_BY_DESC);
                HelpUtils.sortPublicationTypes(getActivity().getApplicationContext(), MinistryDatabase.SORT_BY_DESC);
                reloadCursor();
                return true;
            case R.id.sort_most_placed:
                PrefUtils.setPublicationTypeSort(getActivity(), MinistryDatabase.SORT_BY_POPULAR);
                HelpUtils.sortPublicationTypes(getActivity().getApplicationContext(), MinistryDatabase.SORT_BY_POPULAR);
                reloadCursor();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;

        setHasOptionsMenu(true);

        fm = getActivity().getSupportFragmentManager();

        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        getActivity().setTitle(R.string.form_publication_types);

        database = new MinistryService(getActivity().getApplicationContext());
        database.openWritable();

        adapter = new ItemWithIconAdapter(getActivity().getApplicationContext());

        setListAdapter(adapter);

        loadCursor();

        database.close();

        if (is_dual_pane) {
            fab.setVisibility(View.GONE);

            Fragment frag = fm.findFragmentById(R.id.secondary_fragment_container);
            PublicationManagerEditorFrag f = new PublicationManagerEditorFrag().newInstance(MinistryDatabase.CREATE_ID);

            FragmentTransaction ft = fm.beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

            if (frag != null)
                ft.remove(frag);

            ft.add(R.id.secondary_fragment_container, f);

            ft.commit();
        }
        if (is_dual_pane) {

        } else {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openEditor(HouseholderEditorFragment.CREATE_ID);
                }
            });
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (adapter.getItem(position).getID() > MinistryDatabase.MAX_PUBLICATION_TYPE_ID) {
            showListItems(adapter.getItem(position).getID(), adapter.getItem(position).toString(),adapter.getItem(position).getIsActive(),adapter.getItem(position).getIsDefault());
        } else {
            if (is_dual_pane) {
                PublicationManagerEditorFrag f = (PublicationManagerEditorFrag) fm.findFragmentById(R.id.secondary_fragment_container);
                f.switchForm(adapter.getItem(position).getID());
            } else {
                createDialog(adapter.getItem(position).getID(), adapter.getItem(position).toString(), adapter.getItem(position).getIsActive(), adapter.getItem(position).getIsDefault());
            }
        }
    }

    private void createDialog(final long id, String name, int isActive, int isDefault) {
        if ((int) id <= MinistryDatabase.MAX_PUBLICATION_TYPE_ID)
            showEditTextDialog((int) id, name, isActive, isDefault);
        else
            showTransferToDialog((int) id, name);
    }

    @SuppressLint("InflateParams")
    private void showEditTextDialog(final int id, String name, int isActive, int isDefault) {
        View view = LayoutInflater.from(PublicationManagerFrag.this.getActivity()).inflate(R.layout.d_edit_text_with_two_cb, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(PublicationManagerFrag.this.getActivity());
        final EditText editText = (EditText) view.findViewById(R.id.text1);
        final CheckBox cb_is_active = (CheckBox) view.findViewById(R.id.cb_is_active);
        final CheckBox cb_is_default = (CheckBox) view.findViewById(R.id.cb_is_default);

        // A default - don't allow them to make it inactive
        if(id <= MinistryDatabase.MAX_PUBLICATION_TYPE_ID && id != MinistryDatabase.CREATE_ID) {
            cb_is_active.setVisibility(View.GONE);
        }

        editText.setText(name);
        cb_is_active.setChecked((isActive != 0) ? true : false);
        cb_is_default.setChecked((isDefault != 0) ? true : false);

        builder.setView(view);
        builder.setTitle((id == MinistryDatabase.CREATE_ID) ? R.string.form_name : R.string.edit);
        builder.setNegativeButton(R.string.menu_cancel, null); // Do nothing on cancel - this will dismiss the dialog :)
        builder.setPositiveButton((id == MinistryDatabase.CREATE_ID) ? R.string.menu_create : R.string.menu_save, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (values == null)
                    values = new ContentValues();

                values.put(LiteratureType.NAME, editText.getText().toString());
                values.put(LiteratureType.ACTIVE, cb_is_active.isChecked() ? MinistryService.ACTIVE : MinistryService.INACTIVE);
                values.put(LiteratureType.DEFAULT, cb_is_default.isChecked() ? MinistryService.ACTIVE : MinistryService.INACTIVE);

                database.openWritable();

                if(cb_is_default.isChecked()) {
                    database.clearPublicationTypeDefault();
                }

                if (id == MinistryDatabase.CREATE_ID)
                    database.createPublicationType(values);
                else
                    database.savePublicationType(id, values);

                reloadCursor();
                database.close();
            }
        });
        builder.show();
    }

    public void showTransferToDialog(final int id, final String name) {
        database.openWritable();
        final Cursor cursor = database.fetchDefaultPublicationTypes();
        final DialogItemAdapter mAdapter = new DialogItemAdapter(getActivity().getApplicationContext());

        while (cursor.moveToNext())
            mAdapter.addItem(new NavDrawerMenuItem(cursor.getString(cursor.getColumnIndex(LiteratureType.NAME)), Helper.getIconResIDByLitTypeID(cursor.getInt(cursor.getColumnIndex(LiteratureType._ID))), cursor.getInt(cursor.getColumnIndex(LiteratureType._ID))));

        cursor.close();
        database.close();

        AlertDialog.Builder builder = new AlertDialog.Builder(PublicationManagerFrag.this.getActivity());
        builder.setTitle(getActivity().getApplicationContext().getString(R.string.menu_transfer_to));
        builder.setAdapter(mAdapter, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                database.openWritable();
                database.reassignPublications(id, mAdapter.getItem(which).getID());
                database.removePublicationType(id);
                database.close();
                reloadCursor();
            }
        });

        builder.show();
    }

    private void loadCursor() {
        if (!database.isOpen())
            database.openWritable();

        final Cursor cursor = database.fetchAllPublicationTypes();
        //adapter.loadNewData(database.fetchLiteratureByTypeWithActivityDates(literatureTypeId));
        /*
        adapter.clear();
        final Cursor cursor = database.fetchAllPublicationTypes();

        while (cursor.moveToNext())
            adapter.addItem(new ItemWithIcon(cursor.getString(cursor.getColumnIndex(LiteratureType.NAME)),Helper.getIconResIDByLitTypeID(cursor.getInt(cursor.getColumnIndex(LiteratureType._ID))),cursor.getInt(cursor.getColumnIndex(LiteratureType._ID)),cursor.getInt(cursor.getColumnIndex(LiteratureType.ACTIVE)),cursor.getInt(cursor.getColumnIndex(LiteratureType.DEFAULT))));

        cursor.close();
        */

        adapter.loadNewData(cursor);

        database.close();
    }

    public void reloadCursor() {
        loadCursor();
        adapter.notifyDataSetChanged();
    }

    public void openEditor(long id) {
        int LAYOUT_ID = (is_dual_pane) ? R.id.secondary_fragment_container : R.id.primary_fragment_container;

        if (is_dual_pane) {
            /*
            if(fm.findFragmentById(LAYOUT_ID) instanceof HouseholderEditorFragment) {
                HouseholderEditorFragment fragment = (HouseholderEditorFragment) fm.findFragmentById(LAYOUT_ID);
                fragment.switchForm(id);
            }
            else {
                FragmentTransaction ft = fm.beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                Fragment frag = fm.findFragmentById(LAYOUT_ID);
                HouseholderEditorFragment f = new HouseholderEditorFragment().newInstance(id);

                if(frag != null)
                    ft.remove(frag);

                ft.add(LAYOUT_ID, f);
                ft.addToBackStack(null);

                ft.commit();
            }
            */
        } else {
            showEditTextDialog(MinistryDatabase.CREATE_ID, "", MinistryService.ACTIVE, MinistryService.INACTIVE);
        }
    }

    public void showListItems(final int id, final String name, final int isActive, final int isDefault) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PublicationManagerFrag.this.getActivity());
        builder.setTitle(R.string.menu_options);
        builder.setItems(getResources().getStringArray(R.array.entry_type_list_item_options), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case RENAME_ID:
                        if (is_dual_pane) {
                            //populateEditor(id);
                        } else {
                            showEditTextDialog(id, name, isActive, isDefault);
                            //sortList(PrefUtils.getEntryTypeSort(getActivity()));
                        }
                        break;
                    case TRANSFER_ID:
                        showTransferToDialog(id, name);
                        break;
                    case DELETE_ID:
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        database.openWritable();
                                        database.removePublicationType(id);
                                        database.close();
                                        reloadCursor();

                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(PublicationManagerFrag.this.getActivity());
                        builder.setTitle(R.string.confirm_deletion)
                                .setMessage(R.string.confirm_deletaion_message_publication_types)
                                .setPositiveButton(R.string.menu_delete, dialogClickListener)
                                .setNegativeButton(R.string.menu_cancel, dialogClickListener)
                                .show();

                        break;
                }
            }
        });
        builder.show();
    }
}