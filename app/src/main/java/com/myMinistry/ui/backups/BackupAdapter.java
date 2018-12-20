package com.myMinistry.ui.backups;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.ui.backups.model.Backup;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.recyclerview.widget.RecyclerView;

public class BackupAdapter extends RecyclerView.Adapter<BackupAdapter.ViewHolder> {
    private ArrayList<Backup> mDataSet;
    private RecyclerViewClickListener mListener;

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        ViewHolder(View v) {
            super(v);

            v.setOnClickListener(v1 -> {
                // Let's the fragment handle this
                mListener.onClick(v1, getAdapterPosition());
            });

            textView = v.findViewById(R.id.textView);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    // data is passed into the constructor
    public BackupAdapter(String[] dataSet, RecyclerViewClickListener listener) {
        mListener = listener;
        Arrays.sort(dataSet);
        mDataSet = new ArrayList<>();
        for (String filename : dataSet)
            mDataSet.add(new Backup(filename));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.backups_row_item, viewGroup, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the contents of the view with that element
        viewHolder.getTextView().setText(mDataSet.get(position).getName());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void addItem(Backup item) {
        mDataSet.add(item);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mDataSet.remove(position);
        notifyDataSetChanged();
    }

    public Backup getItem(int position) {
        return mDataSet.get(position);
    }

    public interface RecyclerViewClickListener {
        void onClick(View view, int position);
    }
}