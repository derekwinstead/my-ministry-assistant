package com.myMinistry.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.adapters.NavDrawerMenuItemAdapter;
import com.myMinistry.bean.Publication;
import com.myMinistry.db.PublicationDAO;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.provider.MinistryContract.LiteratureType;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.utils.AppConstants;
import com.myMinistry.utils.HelpUtils;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class PublicationEditorFragment extends Fragment {
    private Button view_activity;
    private Spinner s_publicationTypes;
    private CheckBox cb_is_active;
    private CheckBox cb_is_pair;
    private TextInputLayout nameWrapper;

    private FloatingActionButton fab;
    private FragmentManager fm;

    private PublicationDAO publicationDAO;
    private Publication publication;

    static final long CREATE_ID = (long) AppConstants.CREATE_ID;

    private Cursor cursor;
    private NavDrawerMenuItemAdapter sadapter;

    public PublicationEditorFragment newInstance() {
        return new PublicationEditorFragment();
    }

    public PublicationEditorFragment newInstance(long _literatureID) {
        PublicationEditorFragment f = new PublicationEditorFragment();
        Bundle args = new Bundle();
        args.putLong(AppConstants.ARG_PUBLICATION_ID, _literatureID);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!publication.isNew()) {
            inflater.inflate(R.menu.discard, menu);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.publication_editor, container, false);

        publicationDAO = new PublicationDAO(getActivity().getApplicationContext());
        publication = new Publication();

        Bundle args = getArguments();
        if (args != null && args.containsKey(AppConstants.ARG_PUBLICATION_ID)) {
            setLiterature(args.getLong(AppConstants.ARG_PUBLICATION_ID));
        }

        setHasOptionsMenu(true);

        fm = getActivity().getSupportFragmentManager();

        fab = root.findViewById(R.id.fab);

        sadapter = new NavDrawerMenuItemAdapter(getActivity().getApplicationContext());

        nameWrapper = root.findViewById(R.id.nameWrapper);
        nameWrapper.setHint(getActivity().getString(R.string.form_name));

        s_publicationTypes = root.findViewById(R.id.literatureTypes);
        cb_is_active = root.findViewById(R.id.cb_is_active);
        cb_is_pair = root.findViewById(R.id.cb_is_pair);
        view_activity = root.findViewById(R.id.view_activity);
        Button save = root.findViewById(R.id.save);
        Button cancel = root.findViewById(R.id.cancel);

        MinistryService database = new MinistryService(getActivity().getApplicationContext());
        database.openWritable();

        cursor = database.fetchActiveTypesOfLiterature();
        int default_position = 0;
        while (cursor.moveToNext()) {
            sadapter.addItem(new NavDrawerMenuItem(cursor.getString(cursor.getColumnIndex(LiteratureType.NAME)), Helper.getIconResIDByLitTypeID(cursor.getInt(cursor.getColumnIndex(LiteratureType._ID))), cursor.getInt(cursor.getColumnIndex(LiteratureType._ID))));

            if (cursor.getInt(cursor.getColumnIndex(LiteratureType.DEFAULT)) == AppConstants.ACTIVE)
                default_position = cursor.getPosition();
        }

        sadapter.setDropDownViewResource(R.layout.li_spinner_item_dropdown);
        s_publicationTypes.setAdapter(sadapter);
        s_publicationTypes.setSelection(default_position);
        s_publicationTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                publication.setTypeId(sadapter.getItem(position).getID());
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
                PublicationActivityFragment f = new PublicationActivityFragment().newInstance(publication.getId());
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.contentFrame, f, "main");
                transaction.commit();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameWrapper.getEditText().getText().toString().trim().length() > 0) {
                    nameWrapper.setErrorEnabled(false);

                    publication.setName(nameWrapper.getEditText().getText().toString().trim());
                    publication.setIsActive(HelpUtils.booleanConversionsToInt(cb_is_active.isChecked()));
                    publication.setWeight(cb_is_pair.isChecked() ? 2 : 1);

                    if (publication.isNew()) {
                        publication.setId(publicationDAO.create(publication));
                    } else {
                        publicationDAO.update(publication);
                    }

                    PublicationFragment f = new PublicationFragment().newInstance((int) publication.getTypeId());
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.contentFrame, f, "main");
                    transaction.commit();
                } else {
                    nameWrapper.setError(getActivity().getApplicationContext().getString(R.string.toast_provide_name));
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PublicationFragment f = new PublicationFragment().newInstance();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.contentFrame, f, "main");
                transaction.commit();
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(R.string.title_publication_edit);

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
                                publicationDAO.deletePublication(publication);
                                PublicationFragment f = new PublicationFragment().newInstance((int) publication.getTypeId());
                                FragmentTransaction transaction = fm.beginTransaction();
                                transaction.replace(R.id.contentFrame, f, "main");
                                transaction.commit();

                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.confirm_deletion)
                        .setMessage(R.string.confirm_deletion_message_publication)
                        .setPositiveButton(R.string.menu_delete, dialogClickListener)
                        .setNegativeButton(R.string.menu_cancel, dialogClickListener)
                        .show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setLiterature(long _id) {
        publication = publicationDAO.getPublication((int) _id);
    }

    public void switchForm(long _id) {
        ActivityCompat.invalidateOptionsMenu(getActivity());
        setLiterature(_id);
        fillForm();
    }

    public void fillForm() {
        nameWrapper.setError(null);

        nameWrapper.getEditText().setText(publication.getName());
        cb_is_active.setChecked(publication.isActive());
        cb_is_pair.setChecked(publication.getWeight() > 1);

        int position = -1;
        cursor.moveToFirst();
        do {
            position++;
            if (cursor.getInt(cursor.getColumnIndex(LiteratureType._ID)) == publication.getTypeId()) {
                s_publicationTypes.setSelection(position);
                break;
            }
        } while (cursor.moveToNext());


        if (publication.isNew()) {
            view_activity.setVisibility(View.GONE);
        } else {
            view_activity.setVisibility(View.VISIBLE);
        }

        fab.setVisibility(View.GONE);
    }
}