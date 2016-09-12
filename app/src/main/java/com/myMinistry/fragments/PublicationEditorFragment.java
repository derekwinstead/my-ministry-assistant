package com.myMinistry.fragments;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.adapters.NavDrawerMenuItemAdapter;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.provider.MinistryContract.Literature;
import com.myMinistry.provider.MinistryContract.LiteratureType;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.squareup.phrase.Phrase;

public class PublicationEditorFragment extends Fragment {
    public static String ARG_PUBLICATION_ID = "publication_id";

    private boolean is_dual_pane = false;

    private Button view_activity;
    private Spinner s_publicationTypes;
    private CheckBox cb_is_active;
    private CheckBox cb_is_pair;
    private TextInputLayout nameWrapper;

    private FloatingActionButton fab;

    private FragmentManager fm;

    static final long CREATE_ID = (long) MinistryDatabase.CREATE_ID;
    private long publicationId = CREATE_ID;

    private long publicationTypeId = 0;
    private MinistryService database;
    private Cursor cursor;
    private NavDrawerMenuItemAdapter sadapter;

    public PublicationEditorFragment newInstance() {
        return new PublicationEditorFragment();
    }

    public PublicationEditorFragment newInstance(long _literatureID) {
        PublicationEditorFragment f = new PublicationEditorFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PUBLICATION_ID, _literatureID);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (publicationId != CREATE_ID)
            inflater.inflate(R.menu.discard, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.publication_editor, container, false);
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_PUBLICATION_ID))
            setLiterature(args.getLong(ARG_PUBLICATION_ID));

        setHasOptionsMenu(true);

        fm = getActivity().getSupportFragmentManager();

        fab = (FloatingActionButton) root.findViewById(R.id.fab);

        sadapter = new NavDrawerMenuItemAdapter(getActivity().getApplicationContext());

        nameWrapper = (TextInputLayout) root.findViewById(R.id.nameWrapper);
        nameWrapper.setHint(getActivity().getString(R.string.form_name));

        s_publicationTypes = (Spinner) root.findViewById(R.id.literatureTypes);
        cb_is_active = (CheckBox) root.findViewById(R.id.cb_is_active);
        cb_is_pair = (CheckBox) root.findViewById(R.id.cb_is_pair);
        view_activity = (Button) root.findViewById(R.id.view_activity);
        Button save = (Button) root.findViewById(R.id.save);
        Button cancel = (Button) root.findViewById(R.id.cancel);

        database = new MinistryService(getActivity().getApplicationContext());
        database.openWritable();

        cursor = database.fetchActiveTypesOfLiterature();
        int default_position = 0;
        while (cursor.moveToNext()) {
            sadapter.addItem(new NavDrawerMenuItem(cursor.getString(cursor.getColumnIndex(LiteratureType.NAME)), Helper.getIconResIDByLitTypeID(cursor.getInt(cursor.getColumnIndex(LiteratureType._ID))), cursor.getInt(cursor.getColumnIndex(LiteratureType._ID))));

            if (cursor.getInt(cursor.getColumnIndex(LiteratureType.DEFAULT)) == MinistryService.ACTIVE)
                default_position = cursor.getPosition();
        }

        sadapter.setDropDownViewResource(R.layout.li_spinner_item_dropdown);
        s_publicationTypes.setAdapter(sadapter);
        s_publicationTypes.setSelection(default_position);
        s_publicationTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                publicationTypeId = sadapter.getItem(position).getID();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        database.close();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchForm(CREATE_ID);
            }
        });

        view_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PublicationActivityFragment f = new PublicationActivityFragment().newInstance(publicationId);
                FragmentTransaction transaction = fm.beginTransaction();
                if (is_dual_pane) {
                    transaction.replace(R.id.secondary_fragment_container, f, "secondary");
                } else {
                    transaction.replace(R.id.primary_fragment_container, f, "main");
                }
                transaction.commit();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameWrapper.getEditText().getText().toString().trim().length() > 0) {
                    nameWrapper.setErrorEnabled(false);

                    ContentValues values = new ContentValues();
                    values.put(Literature.NAME, nameWrapper.getEditText().getText().toString().trim());
                    values.put(Literature.ACTIVE, (cb_is_active.isChecked()) ? 1 : 0);
                    values.put(Literature.TYPE_OF_LIERATURE_ID, publicationTypeId);
                    values.put(Literature.WEIGHT, (cb_is_pair.isChecked()) ? 2 : 1);

                    database.openWritable();
                    if (publicationId > 0) {
                        if (database.saveLiterature(publicationId, values) == 0) {
                            Toast.makeText(getActivity()
                                    , Phrase.from(getActivity().getApplicationContext(), R.string.toast_saved_problem_with_space)
                                            .put("name", nameWrapper.getEditText().getText().toString().trim())
                                            .format()
                                    , Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (database.createLiterature(values) == -1) {
                            Toast.makeText(getActivity()
                                    , Phrase.from(getActivity().getApplicationContext(), R.string.toast_created_problem_with_space)
                                            .put("name", nameWrapper.getEditText().getText().toString().trim())
                                            .format()
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }
                    database.close();

                    if (is_dual_pane) {
                        PublicationFragment f = (PublicationFragment) fm.findFragmentById(R.id.primary_fragment_container);
                        f.updateLiteratureList((int) publicationTypeId);
                    } else {
                        PublicationFragment f = new PublicationFragment().newInstance();
                        FragmentTransaction transaction = fm.beginTransaction();
                        transaction.replace(R.id.primary_fragment_container, f, "main");
                        transaction.commit();
                    }
                } else {
                    nameWrapper.setError(getActivity().getApplicationContext().getString(R.string.toast_provide_name));
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_dual_pane) {
                    switchForm(CREATE_ID);
                } else {
                    PublicationFragment f = new PublicationFragment().newInstance();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.primary_fragment_container, f, "main");
                    transaction.commit();
                }
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;

        if (!is_dual_pane) {
            getActivity().setTitle(R.string.title_publication_edit);
            fab.setVisibility(View.GONE);
        }

        fillForm();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_discard:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                database.openWritable();
                                database.deleteLiteratureByID((int) publicationId);
                                database.close();

                                if (is_dual_pane) {
                                    PublicationFragment f = (PublicationFragment) fm.findFragmentById(R.id.primary_fragment_container);
                                    f.updateLiteratureList((int) publicationTypeId);
                                    switchForm(CREATE_ID);
                                } else {
                                    PublicationFragment f = new PublicationFragment().newInstance((int) s_publicationTypes.getSelectedItemId());
                                    FragmentTransaction transaction = fm.beginTransaction();
                                    transaction.replace(R.id.primary_fragment_container, f, "main");
                                    transaction.commit();
                                }

                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(PublicationEditorFragment.this.getActivity());
                builder.setTitle(R.string.confirm_deletion)
                        .setPositiveButton(R.string.menu_delete, dialogClickListener)
                        .setNegativeButton(R.string.menu_cancel, dialogClickListener)
                        .show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setLiterature(long _id) {
        publicationId = _id;
    }

    public void switchForm(long _id) {
        ActivityCompat.invalidateOptionsMenu(getActivity());
        setLiterature(_id);
        fillForm();
    }

    public void fillForm() {
        nameWrapper.setError(null);
        if (publicationId == CREATE_ID) {
            nameWrapper.getEditText().setText("");
            cb_is_active.setChecked(true);
            cb_is_pair.setChecked(false);
            view_activity.setVisibility(View.GONE);
        } else {
            view_activity.setVisibility(View.VISIBLE);
            database.openWritable();
            Cursor literature = database.fetchLiteratureByID((int) publicationId);
            if (literature.moveToFirst()) {
                nameWrapper.getEditText().setText(literature.getString(literature.getColumnIndex(Literature.NAME)));
                cb_is_active.setChecked(literature.getInt(literature.getColumnIndex(Literature.ACTIVE)) == MinistryService.ACTIVE);
                cb_is_pair.setChecked(literature.getInt(literature.getColumnIndex(Literature.WEIGHT)) != 1);

                if (cursor.moveToFirst()) {
                    int position = -1;
                    do {
                        position++;
                        if (cursor.getInt(cursor.getColumnIndex(LiteratureType._ID)) == literature.getInt(literature.getColumnIndex(Literature.TYPE_OF_LIERATURE_ID))) {
                            s_publicationTypes.setSelection(position);
                            break;
                        }
                    } while (cursor.moveToNext());
                }
            } else {
                nameWrapper.getEditText().setText("");
                s_publicationTypes.setSelection(0);
                cb_is_active.setChecked(true);
                cb_is_pair.setChecked(false);
            }

            literature.close();
            database.close();
        }
    }
}