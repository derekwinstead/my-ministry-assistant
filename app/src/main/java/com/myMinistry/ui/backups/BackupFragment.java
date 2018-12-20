package com.myMinistry.ui.backups;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.myMinistry.R;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.ui.backups.model.Backup;
import com.myMinistry.utils.AppConstants;
import com.myMinistry.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BackupFragment extends Fragment {
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";

    private final int REF_RESTORE = 0;
    private final int REF_EMAIL = 1;
    private final int REF_DELETE = 2;

    private enum LayoutManagerType {
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    protected RecyclerView mRecyclerView;
    protected BackupAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected TextView empty;

    protected String[] mDataset;

    public BackupFragment() {
        // Requires empty public constructor
    }

    public static BackupFragment newInstance() {
        return new BackupFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize dataset
        initDataset();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.backups_fragment, container, false);

        // Setup the list
        empty = root.findViewById(R.id.empty);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = root.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion to the way ListView would layout elements.
        // The RecyclerView.LayoutManager defines how elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager();

        BackupAdapter.RecyclerViewClickListener listener = (view, position) -> {
            onClick(position);
        };

        mAdapter = new BackupAdapter(mDataset, listener);
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);

        // Divider for RecyclerView
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        // END_INCLUDE(initializeRecyclerView)

        showHideRecyclerView();

        root.findViewById(R.id.fab).setOnClickListener(v -> createBackup());

        return root;
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     */
    public void setRecyclerViewLayoutManager() {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }

        mLayoutManager = new LinearLayoutManager(getActivity());
        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    private void showHideRecyclerView() {
        // Show/Hide the view depending on the amount of files returned
        if (mAdapter.getItemCount() > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            empty.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
        }

        //backups_adapter.notifyDataSetChanged();
    }

    private void initDataset() {
        // TODO Do a check here - might want to pull from internal SD Card or some other location which would require permissions :)
        mDataset = FileUtils.getExternalDBFile(getActivity().getApplicationContext(), "").list();
    }

    private void onClick(int position) {
        //Toast.makeText(getContext(), "Selected " + mAdapter.getItem(position).getName(), Toast.LENGTH_SHORT).show();

        Toast toast = Toast.makeText(getContext(), "Selected " + mAdapter.getItem(position).getName(), Toast.LENGTH_SHORT);
        View view = toast.getView();

//Gets the actual oval background of the Toast then sets the colour filter
        view.getBackground().setColorFilter(getResources().getColor(R.color.alert_bg), PorterDuff.Mode.SRC_IN);

//Gets the TextView from the Toast so it can be editted
        TextView text = view.findViewById(android.R.id.message);
        text.setTextColor(getContext().getResources().getColor(R.color.bpWhite));

        toast.show();








        //final File file = FileUtils.getExternalDBFile(getActivity(), fileList[position]);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mAdapter.getItem(position).getName());

        builder.setItems(getResources().getStringArray(R.array.backup_row_item_options), (dialog, item) -> {
            File file = FileUtils.getExternalDBFile(getActivity(), mAdapter.getItem(position).getName());
            if (item == REF_RESTORE) {
                MinistryService database = new MinistryService(getActivity().getApplicationContext());
                database.openWritable();

                try {
                    if (database.importDatabase(file, getActivity().getApplicationContext().getDatabasePath(AppConstants.DATABASE_NAME))) {
                        //Snackbar.make(coordinatorLayout, R.string.snackbar_import_text, Snackbar.LENGTH_SHORT).show();
                    } else {
                        //Snackbar.make(coordinatorLayout, R.string.snackbar_import_text_error, Snackbar.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    //Snackbar.make(coordinatorLayout, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            } else if (item == REF_EMAIL) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("application/image");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getActivity().getApplicationContext().getResources().getString(R.string.app_name) + ": " + getActivity().getApplicationContext().getResources().getString(R.string.pref_backup_title));
                emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
                startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.menu_share)));
            } else if (item == REF_DELETE) {
                file.delete();
                mAdapter.removeItem(position);
                //Snackbar.make(coordinatorLayout, R.string.toast_deleted, Snackbar.LENGTH_SHORT).show();
//                    reloadAdapter();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createBackup() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss-aaa", Locale.getDefault());
        Calendar now = Calendar.getInstance();
        String date = dateFormatter.format(now.getTime());
        File intDB = getActivity().getApplicationContext().getDatabasePath(AppConstants.DATABASE_NAME);
        File extDB = FileUtils.getExternalDBFile(getActivity().getApplicationContext(), date + ".db");

        try {
            if (extDB != null) {
                if (!extDB.exists())
                    extDB.createNewFile();

                FileUtils.copyFile(intDB, extDB);

                mAdapter.addItem(new Backup(extDB.getName()));

                //Snackbar.make(coordinatorLayout, R.string.snackbar_export_text, Snackbar.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            //Snackbar.make(coordinatorLayout, e.getMessage(), Snackbar.LENGTH_SHORT).show();
        }
    }
}