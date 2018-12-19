package com.myMinistry.ui.backups;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.adapters.ListItemAdapter;
import com.myMinistry.ui.backups.model.Backup;
import com.myMinistry.utils.FileUtils;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BackupFragment extends Fragment {
    private String[] fileList;
    private ListItemAdapter adapter;

    private final ArrayList<Backup> backups = new ArrayList<>();
    private BackupsAdapter backups_adapter;
    private RecyclerView backups_db;
    private TextView empty;

    public BackupFragment() {
        // Requires empty public constructor
    }

    public static BackupFragment newInstance() {
        return new BackupFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.backups_fragment, container, false);

        // Setup the list
        empty = root.findViewById(R.id.empty);
        backups_db = root.findViewById(R.id.backups_db);
        backups_db.setLayoutManager(new LinearLayoutManager(getContext()));
        backups_adapter = new BackupsAdapter(getContext(), backups);
        backups_db.setAdapter(backups_adapter);

        // Divider for RecyclerView
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(backups_db.getContext(), DividerItemDecoration.VERTICAL);
        backups_db.addItemDecoration(dividerItemDecoration);

        // Read all the items from the device to display
        loadAdapter();

        return root;
    }

    private void loadAdapter() {
        // TODO check for permissions to read/write the device

        fileList = loadFileList();

        if (fileList != null) {
            Arrays.sort(fileList);
        } else {
            fileList = new String[0];
        }

        for (String filename : fileList) {
            backups.add(new Backup(filename));
        }

        // Show/Hide the view depending on the amount of files returned
        if (fileList.length > 0) {
            backups_db.setVisibility(View.VISIBLE);
            empty.setVisibility(View.GONE);
        } else {
            backups_db.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
        }

        backups_adapter.notifyDataSetChanged();
    }

    private String[] loadFileList() {
        return FileUtils.getExternalDBFile(getActivity().getApplicationContext(), "").list();
    }
}


/*
private RecyclerView recyclerView;
private TextView emptyView;

// ...

recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
emptyView = (TextView) rootView.findViewById(R.id.empty_view);

// ...

if (dataset.isEmpty()) {
    recyclerView.setVisibility(View.GONE);
    emptyView.setVisibility(View.VISIBLE);
}
else {
    recyclerView.setVisibility(View.VISIBLE);
    emptyView.setVisibility(View.GONE);
}
 */