package com.myMinistry.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.bean.ReportPublication;

import java.util.ArrayList;

public class ReportPublicationSummaryAdapter extends RecyclerView.Adapter<ReportPublicationSummaryAdapter.ViewHolder> {

    private ArrayList<ReportPublication> mData;
    private LayoutInflater mInflater;
    //private ItemClickListener mClickListener;

    // data is passed into the constructor
    public ReportPublicationSummaryAdapter(Context context, ArrayList<ReportPublication> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.li_report_publication_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //String animal = mData.get(position).getName();
        holder.myTextView.setText(mData.get(position).getName());
        holder.myTextViewCount.setText(String.valueOf(mData.get(position).getCount()));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {//} implements View.OnClickListener {
        TextView myTextView;
        TextView myTextViewCount;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.text1);
            myTextViewCount = itemView.findViewById(R.id.count1);
            //itemView.setOnClickListener(this);
        }
/*
        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }*/
    }
/*
    // convenience method for getting data at click position
    ReportPublication getItem(int id) {
        return mData.get(id);
    }*/
/*
    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }*/
}