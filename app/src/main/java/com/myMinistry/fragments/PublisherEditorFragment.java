package com.myMinistry.fragments;

import android.content.DialogInterface;
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

import com.myMinistry.R;
import com.myMinistry.adapters.NavDrawerMenuItemAdapter;
import com.myMinistry.bean.Publisher;
import com.myMinistry.db.PublisherDAO;
import com.myMinistry.model.NavDrawerMenuItem;
import com.myMinistry.provider.MinistryDatabase;

public class PublisherEditorFragment extends Fragment {
    public static final String ARG_PUBLISHER_ID = "publisher_id";

    private boolean is_dual_pane = false;

    private CheckBox cb_is_active;
    private Button view_activity;
    private TextInputLayout nameWrapper;

    static final long CREATE_ID = (long) MinistryDatabase.CREATE_ID;
    private long publisherId = CREATE_ID;

    private FloatingActionButton fab;
    private Spinner gender_type;
    private FragmentManager fm;

    private PublisherDAO publisherDAO;
    private Publisher publisher;

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

        publisherDAO = new PublisherDAO(getActivity().getApplicationContext());

        nameWrapper = root.findViewById(R.id.nameWrapper);
        nameWrapper.setHint(getActivity().getString(R.string.form_name));

        cb_is_active = root.findViewById(R.id.cb_is_active);
        fab = root.findViewById(R.id.fab);
        gender_type = root.findViewById(R.id.gender_type);
        view_activity = root.findViewById(R.id.view_activity);
        Button save = root.findViewById(R.id.save);
        Button cancel = root.findViewById(R.id.cancel);

        genderAdapter = new NavDrawerMenuItemAdapter(getActivity().getApplicationContext());
        genderAdapter.addItem(new NavDrawerMenuItem(getActivity().getApplicationContext().getString(R.string.gender_male), R.drawable.ic_drawer_publisher_male, GENDER_MALE));
        genderAdapter.addItem(new NavDrawerMenuItem(getActivity().getApplicationContext().getString(R.string.gender_female), R.drawable.ic_drawer_publisher_female, GENDER_FEMALE));

        genderAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        gender_type.setAdapter(genderAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchForm(CREATE_ID);
            }
        });

        view_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PublisherActivityFragment f = new PublisherActivityFragment().newInstance(publisherId);
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

                    publisher.setName(nameWrapper.getEditText().getText().toString().trim());
                    publisher.setIsActive(cb_is_active.isChecked());
                    publisher.setGender(gender_type.getSelectedItemPosition() == GENDER_MALE ? "male" : "female");

                    if (publisher.getId() == CREATE_ID) {
                        publisherId = publisherDAO.create(publisher);
                        publisher.setId(publisherId);
                    } else {
                        publisherDAO.update(publisher);
                    }

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
                if (is_dual_pane) {
                    switchForm(CREATE_ID);
                } else {
                    PublishersFragment f = new PublishersFragment().newInstance();
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
            getActivity().setTitle(R.string.title_publisher_edit);
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
                                publisherDAO.deletePublisher(publisher);

                                if (is_dual_pane) {
                                    PublishersFragment f = (PublishersFragment) fm.findFragmentById(R.id.primary_fragment_container);
                                    f.updatePublisherList();
                                    switchForm(CREATE_ID);
                                } else {
                                    PublishersFragment f = new PublishersFragment().newInstance();
                                    FragmentTransaction transaction = fm.beginTransaction();
                                    transaction.replace(R.id.primary_fragment_container, f, "main");
                                    transaction.commit();
                                }

                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.confirm_deletion)
                        .setMessage(R.string.confirm_deletion_message_publisher)
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
        nameWrapper.setErrorEnabled(false);
        nameWrapper.getEditText().setError(null);

        publisher = publisherDAO.getPublisher((int) publisherId);

        nameWrapper.getEditText().setText(publisher.getName());
        cb_is_active.setChecked(publisher.isActive());

        int position = GENDER_MALE;
        if (publisher.getGender().equals("female"))
            position = GENDER_FEMALE;
        gender_type.setSelection(position);

        if (publisher.getId() == CREATE_ID) {
            view_activity.setVisibility(View.GONE);
        } else {
            view_activity.setVisibility(View.VISIBLE);
        }

        if (is_dual_pane) {
            if (publisher.getId() == CREATE_ID) {
                fab.setVisibility(View.GONE);
            } else {
                fab.setVisibility(View.VISIBLE);
            }
        }
    }
}