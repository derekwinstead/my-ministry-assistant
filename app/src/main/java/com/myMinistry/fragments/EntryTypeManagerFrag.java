package com.myMinistry.fragments;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.myMinistry.R;
import com.myMinistry.adapters.DialogItemAdapter;
import com.myMinistry.adapters.ItemWithIconAdapter;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryContract.EntryType;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;

public class EntryTypeManagerFrag extends ListFragment {
    private boolean is_dual_pane = false;

    private FloatingActionButton fab;

    private ItemWithIconAdapter adapter;
    private ContentValues values = null;
    private MinistryService database;
    private FragmentManager fm;

    private final int RENAME_ID = 0;
    private final int TRANSFER_ID = 1;
    private final int DELETE_ID = 2;

    public EntryTypeManagerFrag newInstance() {
        return new EntryTypeManagerFrag();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.entry_type_manager, container, false);
        fab = (FloatingActionButton) root.findViewById(R.id.fab);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;

        fm = getActivity().getSupportFragmentManager();

        if (is_dual_pane) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (is_dual_pane) {
                        populateEditor(MinistryDatabase.CREATE_ID);
                    } else {

                        openEditor(HouseholderEditorFragment.CREATE_ID);
                        /*
                        EntryTypeNewDialogFrag f = EntryTypeNewDialogFrag.newInstance();
                        f.setPositiveButton(new EntryTypeNewDialogFragListener() {
                            @Override
                            public void setPositiveButton(boolean created) {

                            }
                        });
                        f.show(fm, EntryTypeNewDialogFrag.class.getName());
                        */
                    }
                }
            });
        }

        database = new MinistryService(getActivity().getApplicationContext());
        database.openWritable();

        adapter = new ItemWithIconAdapter(getActivity().getApplicationContext(), ItemWithIconAdapter.TYPE_ENTRY_TYPE);

        loadCursor();

        setListAdapter(adapter);

        database.close();

        if (is_dual_pane) {
            Fragment frag = fm.findFragmentById(R.id.secondary_fragment_container);
            EntryTypeManagerEditorFrag f = new EntryTypeManagerEditorFrag().newInstance(MinistryDatabase.CREATE_ID);
            FragmentTransaction ft = fm.beginTransaction();

            if (frag != null)
                ft.remove(frag);

            ft.add(R.id.secondary_fragment_container, f);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.addToBackStack(null);

            ft.commit();
        }
    }

    private void populateEditor(long id) {
        EntryTypeManagerEditorFrag f = (EntryTypeManagerEditorFrag) fm.findFragmentById(R.id.secondary_fragment_container);
        f.switchForm(id);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (adapter.getItem(position).getID() > MinistryDatabase.MAX_ENTRY_TYPE_ID) {
            showListItems(adapter.getItem(position).getID(), adapter.getItem(position).toString(), adapter.getItem(position).getIsActive(), adapter.getItem(position).getIsDefault());
        } else {
            if (is_dual_pane) {
                // TODO
            } else {
                createDialog(adapter.getItem(position).getID(), adapter.getItem(position).toString(), adapter.getItem(position).getIsActive(), adapter.getItem(position).getIsDefault());
            }
        }
    }


    /*

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        createDialog(adapter.getItem(position).getID(), adapter.getItem(position).toString(), (adapter.getItem(position).getID() == MinistryDatabase.ID_ROLLOVER) ? 0 : 1);
    }
*/


    private void createDialog(final long id, String name, int isActive, int isDefault) {
        if ((int) id <= MinistryDatabase.MAX_ENTRY_TYPE_ID)
            showEditTextDialog((int) id, name, isActive, isDefault);
        else
            showTransferToDialog((int) id, name);
    }
    /*

    private void createDialog(final long id, String name, int isActive) {
        switch ((int) id) {
            case MinistryDatabase.ID_ROLLOVER:
            case MinistryDatabase.ID_BIBLE_STUDY:
            case MinistryDatabase.ID_RETURN_VISIT:
            case MinistryDatabase.ID_SERVICE:
            case MinistryDatabase.ID_RBC:
            case MinistryDatabase.CREATE_ID:
                if (is_dual_pane)
                    populateEditor(id);
                else
                    showEditTextDialog((int) id, name, isActive);

                break;
            default:
                showListItems((int) id, name, isActive);
        }
    }

*/

    @SuppressLint("InflateParams")
    private void showEditTextDialog(final int id, String name, int isActive, int isDefault) {
        View view = LayoutInflater.from(EntryTypeManagerFrag.this.getActivity()).inflate(R.layout.d_edit_text_with_two_cb, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(EntryTypeManagerFrag.this.getActivity());
        final EditText editText = (EditText) view.findViewById(R.id.text1);
        final CheckBox cb_is_active = (CheckBox) view.findViewById(R.id.cb_is_active);
        final CheckBox cb_is_default = (CheckBox) view.findViewById(R.id.cb_is_default);

        // A default - don't allow them to make it inactive
        if (id <= MinistryDatabase.MAX_ENTRY_TYPE_ID && id != MinistryDatabase.CREATE_ID) {
            cb_is_active.setVisibility(View.GONE);
        }

        editText.setText(name);
        cb_is_active.setChecked(isActive != 0);
        cb_is_default.setChecked(isDefault != 0);

        builder.setView(view);
        builder.setTitle((id == MinistryDatabase.CREATE_ID) ? R.string.form_name : R.string.edit);
        builder.setNegativeButton(R.string.menu_cancel, null); // Do nothing on cancel - this will dismiss the dialog :)
        builder.setPositiveButton((id == MinistryDatabase.CREATE_ID) ? R.string.menu_create : R.string.menu_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (values == null) {
                    values = new ContentValues();
                }

                values.put(EntryType.NAME, editText.getText().toString());
                values.put(EntryType.ACTIVE, cb_is_active.isChecked() ? MinistryService.ACTIVE : MinistryService.INACTIVE);
                //  values.put(EntryType.RBC, cb_is_active.isChecked() ? MinistryService.ACTIVE : MinistryService.INACTIVE);
                values.put(EntryType.DEFAULT, cb_is_default.isChecked() ? MinistryService.ACTIVE : MinistryService.INACTIVE);

                database.openWritable();

                if (cb_is_default.isChecked()) {
                    database.clearEntryTypeDefault();
                }

                if (id == MinistryDatabase.CREATE_ID) {
                    database.createEntryType(values);
                } else {
                    database.saveEntryType(id, values);
                }

                reloadCursor();
                database.close();
            }
        });
        builder.show();
    }

    /*

    @SuppressLint("InflateParams")
    private void showEditTextDialog(final int id, String name, int isActive) {
        EntryTypeDialogFrag frag = EntryTypeDialogFrag.newInstance(id, name, isActive);
        frag.setPositiveButton(new EntryTypeDialogFragListener() {
            @Override
            public void setPositiveButton(String _name, int _isActive) {
                if (values == null)
                    values = new ContentValues();

                values.put(EntryType.NAME, _name);
                values.put(EntryType.RBC, id != MinistryDatabase.ID_RBC ? MinistryService.INACTIVE : MinistryService.ACTIVE);

                if (id != MinistryDatabase.ID_ROLLOVER)
                    values.put(EntryType.ACTIVE, _isActive);
                else
                    values.put(EntryType.ACTIVE, MinistryService.INACTIVE);

                database.openWritable();

                if (id != MinistryDatabase.CREATE_ID)
                    database.saveEntryType(id, values);
                else
                    database.createEntryType(values);

                reloadCursor();

                database.close();
            }
        });
        frag.show(fm, "EntryTypeDialogFrag");
    }


*/


    public void showListItems(final int id, final String name, final int isActive, final int isDefault) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EntryTypeManagerFrag.this.getActivity());
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
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        database.openWritable();
                                        database.deleteEntryTypeByID(id);
                                        database.close();
                                        reloadCursor();
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(EntryTypeManagerFrag.this.getActivity());
                        builder.setTitle(R.string.confirm_deletion)
                                .setMessage(R.string.confirm_deletion_message_entry_type)
                                .setPositiveButton(R.string.menu_delete, dialogClickListener)
                                .setNegativeButton(R.string.menu_cancel, dialogClickListener)
                                .show();

                        break;
                }
            }
        });
        builder.show();
    }

    /*


    public void showListItems(final int id, final String name, final int isActive) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EntryTypeManagerFrag.this.getActivity());
        builder.setTitle(R.string.menu_options);
        builder.setItems(getResources().getStringArray(R.array.entry_type_list_item_options), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case RENAME_ID:
                        if (is_dual_pane) {
                            populateEditor(id);
                        } else {
                            showEditTextDialog(id, name, isActive);
                        }
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
                                        database.deleteEntryTypeByID(id);

                                        if (is_dual_pane)
                                            populateEditor(MinistryDatabase.CREATE_ID);

                                        database.close();

                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(R.string.confirm_deletion)
                                .setPositiveButton(R.string.menu_delete, dialogClickListener)
                                .setNegativeButton(R.string.menu_cancel, dialogClickListener)
                                .show();


                        break;
                }
            }
        });
        builder.show();
    }
*/

    /*
    public void showTransferToDialog(final int id, final String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EntryTypeManagerFrag.this.getActivity());
        database.openWritable();
        final Cursor defaults = database.fetchAllEntryTypesButID(id);
        builder.setTitle(R.string.menu_transfer_to);
        builder.setCursor(defaults, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                defaults.moveToPosition(which);
                database.reassignEntryType(id, defaults.getInt(defaults.getColumnIndex(EntryType._ID)));
            }
        }, EntryType.NAME);
        builder.show();
    }

*/


    public void showTransferToDialog(final int id, final String name) {
        database.openWritable();
        final Cursor cursor = database.fetchActiveEntryTypesButID(id);
        final DialogItemAdapter mAdapter = new DialogItemAdapter(getActivity().getApplicationContext());

        while (cursor.moveToNext()) {
            mAdapter.addItem(new NavDrawerMenuItem(cursor.getString(cursor.getColumnIndex(MinistryContract.EntryType.NAME)), R.drawable.ic_drawer_entry_types_new, cursor.getInt(cursor.getColumnIndex(MinistryContract.EntryType._ID))));
        }

        cursor.close();
        database.close();

        AlertDialog.Builder builder = new AlertDialog.Builder(EntryTypeManagerFrag.this.getActivity());
        builder.setTitle(getActivity().getApplicationContext().getString(R.string.menu_transfer_to));
        builder.setAdapter(mAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                database.openWritable();
                database.reassignEntryType(id, mAdapter.getItem(which).getID());
                database.close();
                reloadCursor();
            }
        });

        builder.show();
    }

    private void loadCursor() {
        if (!database.isOpen()) {
            database.openWritable();
        }

        adapter.loadNewData(database.fetchAllEntryTypes());
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
}