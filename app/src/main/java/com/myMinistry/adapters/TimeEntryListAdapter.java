package com.myMinistry.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.bean.TimeEntryItem;
import com.myMinistry.util.TimeUtils;

import java.util.ArrayList;

public class TimeEntryListAdapter extends RecyclerView.Adapter<TimeEntryListAdapter.ViewHolder> {

    private ArrayList<TimeEntryItem> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    // data is passed into the constructor
    public TimeEntryListAdapter(Context context, ArrayList<TimeEntryItem> data) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.li_monthly_time_entry_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.date_day_of_month.setText(TimeUtils.dayOfMonthFormat.format(mData.get(position).getStartDateAndTime().getTime()));
        holder.date_day_of_week.setText(TimeUtils.fullDayOfWeekFormat.format(mData.get(position).getStartDateAndTime().getTime()));
        holder.date_month_and_year.setText(TimeUtils.monthAndYearFormat.format(mData.get(position).getStartDateAndTime().getTime()));
        holder.entry_hours.setText(TimeUtils.getTimeLength(mData.get(position).getStartDateAndTime(),mData.get(position).getEndDateAndTime(),context.getString(R.string.hours_label),context.getString(R.string.minutes_label)));


        //public static String getTimeLength(Calendar start, Calendar end, String h, String m) {
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {//} implements View.OnClickListener {
        TextView date_day_of_month;
        TextView date_day_of_week;
        TextView date_month_and_year;
        TextView entry_hours;

        ViewHolder(View itemView) {
            super(itemView);
            date_day_of_month = itemView.findViewById(R.id.date_day_of_month);
            date_day_of_week = itemView.findViewById(R.id.date_day_of_week);
            date_month_and_year = itemView.findViewById(R.id.date_month_and_year);
            entry_hours = itemView.findViewById(R.id.entry_hours);
            //myTextViewCount = itemView.findViewById(R.id.count1);
            //itemView.setOnClickListener((View.OnClickListener) this);
        }
/*
        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
  */
    }

    // convenience method for getting data at click position
    TimeEntryItem getItem(int id) {
        return mData.get(id);
    }
    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}