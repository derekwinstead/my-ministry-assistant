package com.myMinistry.ui.report;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.utils.TimeUtils;
import com.myMinistry.utils.ViewUtils;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class ReportListAdapter extends RecyclerView.Adapter<ReportListAdapter.ViewHolder> {
    private ArrayList<ReportListEntryItem> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    private final int topPaddingDP = 15;
    private final int leftRightPaddingDP = 10;
    private final int leftRightPaddingDPExtra = leftRightPaddingDP + 5;

    // data is passed into the constructor
    public ReportListAdapter(Context context, ArrayList<ReportListEntryItem> data) {
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
        holder.start_and_end_times.setText(TimeUtils.getDayInfoStartTimeEndTime(mData.get(position).getStartDateAndTime(), mData.get(position).getEndDateAndTime()));
        holder.entry_hours.setText(TimeUtils.getTimeLength(mData.get(position).getStartDateAndTime(), mData.get(position).getEndDateAndTime(), context.getString(R.string.hours_label), context.getString(R.string.minutes_label)));
        holder.entry_type.setText(mData.get(position).getEntryTypeName());
        holder.ll_entry_info.removeAllViews();

        boolean isFirst;
        for (ReportListEntryHouseholderItem householderItem : mData.get(position).getEntryHouseholderAndPlacements()) {
            boolean shouldAddTopPadding = false;
            // Create a divider for each householder entry (even empty householder)
            View v = new View(this.context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewUtils.dpToPx(1));
            params.setMargins(0, ViewUtils.dpToPx(5), 0, ViewUtils.dpToPx(5));
            v.setLayoutParams(params);
            v.setBackgroundColor(context.getResources().getColor(R.color.holo_grey));
            holder.ll_entry_info.addView(v);

            // Add householder name first
            if (householderItem.getName().length() > 0) {
                TextView tv = new TextView(this.context);
                tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tv.setText(householderItem.getName());
                tv.setTextAppearance(context, android.R.style.TextAppearance_Medium);
                tv.setPadding(ViewUtils.dpToPx(leftRightPaddingDP), 0, ViewUtils.dpToPx(leftRightPaddingDP), 0);
                holder.ll_entry_info.addView(tv);

                shouldAddTopPadding = true;
            }

            // Add notes if they exist
            if (householderItem.getNotes().length() > 0) {
                TextView tv1 = new TextView(this.context);
                tv1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tv1.setText(context.getString(R.string.form_notes));
                tv1.setTextAppearance(context, android.R.style.TextAppearance_Small);
                tv1.setPadding(ViewUtils.dpToPx(leftRightPaddingDP), (shouldAddTopPadding) ? ViewUtils.dpToPx(topPaddingDP) : 0, ViewUtils.dpToPx(leftRightPaddingDP), 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tv1.setTextColor(context.getColor(R.color.primary));
                } else {
                    tv1.setTextColor(context.getResources().getColor(R.color.primary));
                }
                holder.ll_entry_info.addView(tv1);

                TextView tv = new TextView(this.context);
                tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tv.setText(householderItem.getNotes());
                tv.setTextAppearance(context, android.R.style.TextAppearance_Medium);
                tv.setPadding(ViewUtils.dpToPx(leftRightPaddingDPExtra), 0, ViewUtils.dpToPx(leftRightPaddingDPExtra), 0);
                holder.ll_entry_info.addView(tv);

                shouldAddTopPadding = true;
            }

            // Add placed publications if they exist
            isFirst = true;
            for (ReportListEntryPlacedPublicationItem publication : householderItem.getReportListEntryPlacedPublicationItems()) {
                if (isFirst) {
                    TextView tv1 = new TextView(this.context);
                    tv1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    tv1.setText(context.getString(R.string.placements));
                    tv1.setTextAppearance(context, android.R.style.TextAppearance_Small);
                    tv1.setPadding(ViewUtils.dpToPx(leftRightPaddingDP), (shouldAddTopPadding) ? ViewUtils.dpToPx(topPaddingDP) : 0, ViewUtils.dpToPx(leftRightPaddingDP), 0);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        tv1.setTextColor(context.getColor(R.color.primary));
                    } else {
                        tv1.setTextColor(context.getResources().getColor(R.color.primary));
                    }
                    holder.ll_entry_info.addView(tv1);
                }

                TextView tv = new TextView(this.context);
                tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tv.setText(publication.getPublicationName());
                tv.setTextAppearance(context, android.R.style.TextAppearance_Medium);
                tv.setPadding(ViewUtils.dpToPx(leftRightPaddingDPExtra), 0, ViewUtils.dpToPx(leftRightPaddingDPExtra), 0);
                holder.ll_entry_info.addView(tv);

                isFirst = false;
                shouldAddTopPadding = true;
            }
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView start_and_end_times;
        TextView entry_hours;
        TextView entry_type;
        LinearLayout ll_entry_info;

        ViewHolder(View itemView) {
            super(itemView);
            start_and_end_times = itemView.findViewById(R.id.start_and_end_times);
            entry_hours = itemView.findViewById(R.id.entry_hours);
            entry_type = itemView.findViewById(R.id.entry_type);
            ll_entry_info = itemView.findViewById(R.id.ll_entry_info);
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
    ReportListEntryItem getItem(int id) {
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