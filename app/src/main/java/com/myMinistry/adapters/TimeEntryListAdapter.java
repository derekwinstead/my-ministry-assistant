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
        //holder.date_day_of_week.setText(TimeUtils.shortDayOfWeekFormat.format(mData.get(position).getStartDateAndTime().getTime()));
        holder.date_day_of_week.setText(TimeUtils.getDayOfWeek(mData.get(position).getStartDateAndTime()));
        holder.start_and_end_times.setText(TimeUtils.getStartAndEndTimes(mData.get(position).getStartDateAndTime(), mData.get(position).getEndDateAndTime()));
        holder.entry_hours.setText(TimeUtils.getTimeLength(mData.get(position).getStartDateAndTime(),mData.get(position).getEndDateAndTime(),context.getString(R.string.hours_label),context.getString(R.string.minutes_label)));
        holder.entry_type.setText(mData.get(position).getEntryTypeName());


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
        TextView start_and_end_times;
        TextView entry_hours;
        TextView entry_type;

        ViewHolder(View itemView) {
            super(itemView);
            date_day_of_month = itemView.findViewById(R.id.date_day_of_month);
            date_day_of_week = itemView.findViewById(R.id.date_day_of_week);
            start_and_end_times = itemView.findViewById(R.id.start_and_end_times);
            entry_hours = itemView.findViewById(R.id.entry_hours);
            entry_type = itemView.findViewById(R.id.entry_type);
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