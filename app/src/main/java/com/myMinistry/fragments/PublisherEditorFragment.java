package com.myMinistry.fragments;

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
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.myMinistry.R;
import com.myMinistry.adapters.NavDrawerMenuItemAdapter;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.provider.MinistryContract.Publisher;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.squareup.phrase.Phrase;

public class PublisherEditorFragment extends Fragment {
    public static final String ARG_PUBLISHER_ID = "publisher_id";

    private boolean is_dual_pane = false;

    private CheckBox cb_is_active;
    private TextInputLayout nameWrapper;
    private Button save, cancel;

    static final long CREATE_ID = (long) MinistryDatabase.CREATE_ID;
    private long publisherId = CREATE_ID;

    private MinistryService database;
    private FloatingActionButton fab;
    private Spinner gender_type;
    private FragmentManager fm;

    private NavDrawerMenuItemAdapter genderAdapter;

    private static final int GENDER_MALE = 0;
    private static final int GENDER_FEMALE = 1;

    public PublisherEditorFragment newInstance() {
        return new PublisherEditorFragment();
    }

    public PublisherEditorFragment newInstance(long _publisherID) {
        PublisherEditorFragment f = new PublisherEditorFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PUBLISHER_ID, _publisherID);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (publisherId != CREATE_ID)
            inflater.inflate(R.menu.discard, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.publisher_editor, container, false);
        Bundle args = getArguments();
        if (args != null)
            setPublisher(args.getLong(ARG_PUBLISHER_ID));

        setHasOptionsMenu(true);

        fm = getActivity().getSupportFragmentManager();

        genderAdapter = new NavDrawerMenuItemAdapter(getActivity().getApplicationContext());

        nameWrapper = (TextInputLayout) root.findViewById(R.id.nameWrapper);
        nameWrapper.setHint(getActivity().getString(R.string.form_name));

        cb_is_active = (CheckBox) root.findViewById(R.id.cb_is_active);
        fab = (FloatingActionButton) root.findViewById(R.id.fab);
        gender_type = (Spinner) root.findViewById(R.id.gender_type);
        save = (Button) root.findViewById(R.id.save);
        cancel = (Button) root.findViewById(R.id.cancel);

        database = new MinistryService(getActivity().getApplicationContext());

        root.findViewById(R.id.view_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                PublisherActivityFragment newFragment = new PublisherActivityFragment().newInstance(publisherId);
                Fragment replaceFrag = fm.findFragmentById(R.id.primary_fragment_container);
                FragmentTransaction transaction = fm.beginTransaction();

                if (replaceFrag != null)
                    transaction.remove(replaceFrag);

                transaction.add(R.id.primary_fragment_container, newFragment);
                transaction.commit();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameWrapper.getEditText().getText().toString().trim().length() > 0) {
                    nameWrapper.setErrorEnabled(false);

                    ContentValues values = new ContentValues();
                    values.put(Publisher.NAME, nameWrapper.getEditText().getText().toString().trim());
                    values.put(Publisher.ACTIVE, (cb_is_active.isChecked()) ? 1 : 0);
                    values.put(Publisher.GENDER, (gender_type.getSelectedItemPosition() == GENDER_MALE) ? "male" : "female");

                    database.openWritable();
                    if (publisherId > 0) {
                        if (database.savePublisher(publisherId, values) == 0) {
                            Toast.makeText(getActivity()
                                    , Phrase.from(getActivity().getApplicationContext(), R.string.toast_saved_problem_with_space)
                                            .put("name", nameWrapper.getEditText().getText().toString().trim())
                                            .format()
                                    , Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (database.createPublisher(values) == -1) {
                            Toast.makeText(getActivity()
                                    , Phrase.from(getActivity().getApplicationContext(), R.string.toast_created_problem_with_space)
                                            .put("name", nameWrapper.getEditText().getText().toString().trim())
                                            .format()
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }
                    database.close();

                    if (is_dual_pane) {
                        PublishersFragment f = (PublishersFragment) fm.findFragmentById(R.id.primary_fragment_container);
                        f.updatePublisherList();
                    } else {
                        PublishersFragment f = new PublishersFragment().newInstance();
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
                PublishersFragment f = new PublishersFragment().newInstance();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.primary_fragment_container, f, "main");
                transaction.commit();
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        is_dual_pane = getActivity().findViewById(R.id.secondary_fragment_container) != null;

        if (!is_dual_pane) {
            fab.setVisibility(View.GONE);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchForm(CREATE_ID);
            }
        });

        if (!is_dual_pane)
            getActivity().setTitle(R.string.title_publisher_edit);

        fillForm();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        switch (item.getItemId()) {
            case R.id.menu_discard:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                FragmentManager fm1 = getActivity().getSupportFragmentManager();
                                database.openWritable();
                                database.deletePublisherByID((int) publisherId);
                                database.close();

                                if (is_dual_pane) {
                                    PublishersFragment f = (PublishersFragment) fm1.findFragmentById(R.id.primary_fragment_container);
                                    f.updatePublisherList();
                                    switchForm(CREATE_ID);
                                } else {
                                    PublishersFragment f = new PublishersFragment().newInstance();
                                    FragmentTransaction transaction = fm1.beginTransaction();
                                    transaction.replace(R.id.primary_fragment_container, f, "main");
                                    transaction.commit();
                                }

                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.confirm_deletion)
                        .setPositiveButton(R.string.menu_delete, dialogClickListener)
                        .setNegativeButton(R.string.menu_cancel, dialogClickListener)
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setPublisher(long _id) {
        publisherId = _id;
    }

    public void switchForm(long _id) {
        ActivityCompat.invalidateOptionsMenu(getActivity());
        setPublisher(_id);
        fillForm();
    }

    public void fillForm() {
        genderAdapter.addItem(new NavDrawerMenuItem(getActivity().getApplicationContext().getString(R.string.gender_male), R.drawable.ic_drawer_publisher_male, GENDER_MALE));
        genderAdapter.addItem(new NavDrawerMenuItem(getActivity().getApplicationContext().getString(R.string.gender_female), R.drawable.ic_drawer_publisher_female, GENDER_FEMALE));

        genderAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        gender_type.setAdapter(genderAdapter);

        nameWrapper.setError(null);
        if (publisherId == CREATE_ID) {
            nameWrapper.getEditText().setText("");
            cb_is_active.setChecked(true);

            if (is_dual_pane)
                fab.setVisibility(View.GONE);
        } else {
            database.openWritable();
            Cursor publisher = database.fetchPublisher((int) publisherId);
            if (publisher.moveToFirst()) {
                nameWrapper.getEditText().setText(publisher.getString(publisher.getColumnIndex(Publisher.NAME)));
                cb_is_active.setChecked(publisher.getInt(publisher.getColumnIndex(Publisher.ACTIVE)) == 1);

                int position = GENDER_MALE;
                if (publisher.getString(publisher.getColumnIndex(Publisher.GENDER)).equals("female"))
                    position = GENDER_FEMALE;

                gender_type.setSelection(position);
            } else {
                nameWrapper.getEditText().setText("");
                cb_is_active.setChecked(true);
            }
            publisher.close();
            database.close();
            if (is_dual_pane)
                fab.setVisibility(View.VISIBLE);
        }
    }
}