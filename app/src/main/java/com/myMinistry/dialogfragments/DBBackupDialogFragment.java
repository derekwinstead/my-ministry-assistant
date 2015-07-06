package com.myMinistry.dialogfragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.myMinistry.R;

import java.io.File;

import com.myMinistry.util.FileUtils;

import static com.myMinistry.util.LogUtils.makeLogTag;

public class DBBackupDialogFragment extends DialogFragment {
    public static final String TAG = makeLogTag(DBBackupDialogFragment.class);
    private DBBackupDialogFragmentListener sListener;

    public static DBBackupDialogFragment newInstance() {
        return new DBBackupDialogFragment();
    }

    public interface DBBackupDialogFragmentListener {
        void dbBackupDialogFragmentSet(String _name);
    }

    public void setDBBackupFragmentListener(DBBackupDialogFragmentListener listener) {
        sListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        File extDBDir = FileUtils.getExternalDBFile(getActivity(), "");
        final String[] list = extDBDir.list();
        builder.setTitle(getActivity().getApplicationContext().getString(R.string.pref_restore_dialog));

        builder.setItems(list, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                sListener.dbBackupDialogFragmentSet(list[which]);
            }
        });

        return builder.create();
    }
}