package com.myMinistry.fragments;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
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
import com.myMinistry.ui.MainActivity;

public class PublicationManagerFragment extends ListFragment {
    private final int RENAME_ID = 0;
    private final int TRANSFER_ID = 1;
    private final int DELETE_ID = 2;

    private ItemWithIconAdapter adapter;
    private ContentValues values = null;
    private MinistryService database;
    private FragmentManager fm;

    static final long CREATE_ID = (long) MinistryDatabase.CREATE_ID;

    public PublicationManagerFragment newInstance() {
        return new PublicationManagerFragment();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.publication_manager, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().invalidateOptionsMenu();
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.publication_manager, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fm = getActivity().getSupportFragmentManager();

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        getActivity().setTitle(R.string.form_publication_types);

        database = new MinistryService(getActivity().getApplicationContext());
        database.openWritable();

        adapter = new ItemWithIconAdapter(getActivity().getApplicationContext(), ItemWithIconAdapter.TYPE_PUBLICATION);

        setListAdapter(adapter);

        loadCursor();

        database.close();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditor(CREATE_ID);
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (adapter.getItem(position).getID() > MinistryDatabase.MAX_PUBLICATION_TYPE_ID) {
            showListItems(adapter.getItem(position).getID(), adapter.getItem(position).toString(), adapter.getItem(position).getIsActive(), adapter.getItem(position).getIsDefault());
        } else {
            createDialog(adapter.getItem(position).getID(), adapter.getItem(position).toString(), adapter.getItem(position).getIsActive(), adapter.getItem(position).getIsDefault());
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
        View view = LayoutInflater.from(PublicationManagerFragment.this.getActivity()).inflate(R.layout.d_edit_text_with_two_cb, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(PublicationManagerFragment.this.getActivity());
        final EditText editText = (EditText) view.findViewById(R.id.text1);
        final CheckBox cb_is_active = (CheckBox) view.findViewById(R.id.cb_is_active);
        final CheckBox cb_is_default = (CheckBox) view.findViewById(R.id.cb_is_default);

        // A default - don't allow them to make it inactive
        if (id <= MinistryDatabase.MAX_PUBLICATION_TYPE_ID && id != MinistryDatabase.CREATE_ID) {
            cb_is_active.setVisibility(View.GONE);
        }

        editText.setText(name);
        cb_is_active.setChecked(isActive != 0);
        cb_is_default.setChecked(isDefault != 0);

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

                if (cb_is_default.isChecked()) {
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

        AlertDialog.Builder builder = new AlertDialog.Builder(PublicationManagerFragment.this.getActivity());
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
        adapter.loadNewData(cursor);
        database.close();
    }

    public void reloadCursor() {
        loadCursor();
        adapter.notifyDataSetChanged();
    }

    public void openEditor(long id) {
        showEditTextDialog(MinistryDatabase.CREATE_ID, "", MinistryService.ACTIVE, MinistryService.INACTIVE);
    }

    public void showListItems(final int id, final String name, final int isActive, final int isDefault) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PublicationManagerFragment.this.getActivity());
        builder.setTitle(R.string.menu_options);
        builder.setItems(getResources().getStringArray(R.array.entry_type_list_item_options), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case RENAME_ID:
                        showEditTextDialog(id, name, isActive, isDefault);
                        break;
                    case TRANSFER_ID:
                        showTransferToDialog(id, name);
                        break;
                    case DELETE_ID:
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        database.openWritable();
                                        database.removePublicationType(id);
                                        database.close();
                                        reloadCursor();

                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(PublicationManagerFragment.this.getActivity());
                        builder.setTitle(R.string.confirm_deletion)
                                .setMessage(R.string.confirm_deletion_message_publication_types)
                                .setPositiveButton(R.string.menu_delete, dialogClickListener)
                                .setNegativeButton(R.string.menu_cancel, dialogClickListener)
                                .show();

                        break;
                }
            }
        });
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_publications:
                ((MainActivity) getActivity()).goToNavDrawerItem(R.id.drawer_publications);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}