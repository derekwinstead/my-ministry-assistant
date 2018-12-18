package com.myMinistry.ui.householders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myMinistry.R;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class HouseholdersListAdapter extends RecyclerView.Adapter<HouseholdersListAdapter.ViewHolder> {
    private ArrayList<HouseholderItem> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    // data is passed into the constructor
    public HouseholdersListAdapter(Context context, ArrayList<HouseholderItem> data) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.li_householder_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.householder_name.setText(mData.get(position).getName());
        holder.householder_activity_date.setText(mData.get(position).getLastActiveString());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView householder_name;
        TextView householder_activity_date;

        ViewHolder(View itemView) {
            super(itemView);
            householder_name = itemView.findViewById(R.id.householder_name);
            householder_activity_date = itemView.findViewById(R.id.householder_activity_date);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    // convenience method for getting data at click position
    HouseholderItem getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}