package com.myMinistry.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ResourceCursorAdapter;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.provider.MinistryContract.Householder;
import com.myMinistry.provider.MinistryContract.Literature;
import com.myMinistry.provider.MinistryContract.LiteraturePlaced;
import com.myMinistry.provider.MinistryContract.Notes;
import com.myMinistry.provider.MinistryContract.Time;
import com.myMinistry.provider.MinistryContract.TimeHouseholder;
import com.myMinistry.provider.MinistryContract.UnionsNameAsRef;
import com.myMinistry.provider.MinistryService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TimeEntryAdapter extends ResourceCursorAdapter {
    public static final int LAYOUT_ID = R.layout.li_time_entries;
    private Calendar displayDateStart = Calendar.getInstance(Locale.getDefault());
    private Calendar displayDateEnd = Calendar.getInstance(Locale.getDefault());
    private MinistryService database;
    private int padding;
    private LayoutParams lp1, lp2, lp_v;

    @SuppressWarnings("deprecation")
    public TimeEntryAdapter(Context context, Cursor cursor) {
        super(context, LAYOUT_ID, cursor, ResourceCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        database = new MinistryService(context);
        padding = Helper.dipsToPix(context, 5);

        lp1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp_v = new LayoutParams(LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics()));
        lp2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

    }

    private class ViewHolder {
        LinearLayout linlay;
        TextView title;
        TextView date;
        TextView timeStart;
        TextView timeEnd;
    }

    @Override
    public View newView(Context context, Cursor cur, ViewGroup parent) {
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(LAYOUT_ID, parent, false);
        ViewHolder holder = new ViewHolder();

        holder.linlay = (LinearLayout) view.findViewById(R.id.linlay);
        holder.title = (TextView) view.findViewById(R.id.title);
        holder.date = (TextView) view.findViewById(R.id.date);
        holder.timeStart = (TextView) view.findViewById(R.id.timeStart);
        holder.timeEnd = (TextView) view.findViewById(R.id.timeEnd);

        view.setTag(holder);
        return view;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        ArrayList<Entry> entries = new ArrayList<>();
        Entry entry;

        /** Set the date for the view */
        String[] splits = cursor.getString(cursor.getColumnIndex(Time.DATE_START)).split("-");
        /** We have the three numbers to make the date. */
        displayDateStart.set(Calendar.YEAR, Integer.valueOf(splits[0]));
        /** Subtract 1 for zero based months. */
        displayDateStart.set(Calendar.MONTH, Integer.valueOf(splits[1]) - 1);
        displayDateStart.set(Calendar.DAY_OF_MONTH, Integer.valueOf(splits[2]));

        try {
            splits = cursor.getString(cursor.getColumnIndex(Time.DATE_END)).split("-");
        } catch (Exception e) {
            splits = null;
        }
        if (splits != null && splits.length == 3) {
            /** We have the three numbers to make the date. */
            displayDateEnd.set(Calendar.YEAR, Integer.valueOf(splits[0]));
            /** Subtract 1 for zero based months. */
            displayDateEnd.set(Calendar.MONTH, Integer.valueOf(splits[1]) - 1);
            displayDateEnd.set(Calendar.DAY_OF_MONTH, Integer.valueOf(splits[2]));
        } else {
            displayDateEnd.set(Calendar.YEAR, displayDateStart.get(Calendar.YEAR));
            displayDateEnd.set(Calendar.MONTH, displayDateStart.get(Calendar.MONTH));
            displayDateEnd.set(Calendar.DAY_OF_MONTH, displayDateStart.get(Calendar.DAY_OF_MONTH));
        }

        /** Set the time of the entry */
        splits = cursor.getString(cursor.getColumnIndex(Time.TIME_START)).split(":");
        displayDateStart.set(Calendar.HOUR_OF_DAY, Integer.valueOf(splits[0]));
        displayDateStart.set(Calendar.MINUTE, Integer.valueOf(splits[1]));
        displayDateStart.set(Calendar.MILLISECOND, 0);

        splits = cursor.getString(cursor.getColumnIndex(Time.TIME_END)).split(":");
        displayDateEnd.set(Calendar.HOUR_OF_DAY, Integer.valueOf(splits[0]));
        displayDateEnd.set(Calendar.MINUTE, Integer.valueOf(splits[1]));
        displayDateEnd.set(Calendar.MILLISECOND, 0);

        if (!database.isOpen())
            database.openWritable();

        Cursor timeHouseholders = database.fetchTimeHouseholdersForTimeByID(cursor.getInt(cursor.getColumnIndex(Time._ID)));
        Cursor publications;

        /** Loop over the householders */
        for (timeHouseholders.moveToFirst(); !timeHouseholders.isAfterLast(); timeHouseholders.moveToNext()) {
            entry = new Entry();
            entry.setHouseholder(timeHouseholders.getString(timeHouseholders.getColumnIndex(Householder.NAME)));
            entry.setNotes(timeHouseholders.getString(timeHouseholders.getColumnIndex(Notes.NOTES)));
            entry.setIsReturnVisit(timeHouseholders.getInt(timeHouseholders.getColumnIndex(TimeHouseholder.RETURN_VISIT)));

            publications = database.fetchPlacedLitByTimeAndHouseholderID(cursor.getInt(cursor.getColumnIndex(Time._ID)), timeHouseholders.getInt(timeHouseholders.getColumnIndex(TimeHouseholder.HOUSEHOLDER_ID)));
            for (publications.moveToFirst(); !publications.isAfterLast(); publications.moveToNext()) {
                entry.addPublication(new PublicationItem(publications.getString(publications.getColumnIndex(Literature.NAME)), publications.getInt(publications.getColumnIndex(Literature.TYPE_OF_LIERATURE_ID)), publications.getInt(publications.getColumnIndex(LiteraturePlaced.COUNT))));
            }
            publications.close();

            entries.add(entry);
        }

        timeHouseholders.close();
        database.close();

        /** Set the display in the view as  Ddd, Mmm dd, H:MMTT - H:MMTT */
        holder.date.setText(DateUtils.formatDateTime(context, displayDateStart.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR));
        holder.timeStart.setText(DateUtils.formatDateTime(context, displayDateStart.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
        holder.timeEnd.setText(DateUtils.formatDateTime(context, displayDateEnd.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));

        /** Set the display in the view as Xh Ym */
        //holder.hours.setText(TimeUtils.getTimeLength(displayDateStart, displayDateEnd, context.getString(R.string.hours_label), context.getString(R.string.minutes_label)));

        /** Set the display in the view for the header */
        holder.title.setText(cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.TITLE)));

        if (entries.isEmpty()) {
            //holder.div.setVisibility(View.GONE);
            holder.linlay.setVisibility(View.GONE);
        } else {
            //holder.div.setVisibility(View.VISIBLE);
            holder.linlay.setVisibility(View.VISIBLE);
            /** Clean out the old views for the entries */
            holder.linlay.removeAllViews();

            int counter = 0;
            boolean emptyItem;
            for (Entry entryitem : entries) {
                counter++;
                emptyItem = true;
                LinearLayout ll = new LinearLayout(context);
                ll.setLayoutParams(lp1);
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.setPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics())
                        , (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (counter > 1) ? 5 : 0, context.getResources().getDisplayMetrics())
                        , (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics())
                        , (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics()));

                if (counter != 1) {
                    View vnew = new View(context);
                    vnew.setLayoutParams(lp_v);
                    vnew.setBackgroundResource(R.color.primary_dark);

                    holder.linlay.addView(vnew);
                }

                if (!TextUtils.isEmpty(entryitem.getHouseholder()) || !TextUtils.isEmpty(entryitem.getNotes()) || entryitem.pubs.size() > 0) {
                    View v = new View(context);
                    LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams((Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) ? LayoutParams.MATCH_PARENT : LayoutParams.FILL_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics()));
                    v.setLayoutParams(lp3);
                    v.setBackgroundColor(ContextCompat.getColor(context, R.color.divider));

                    TextView tv;

                    /** Show if NOT a return visit */
                    if (!entryitem.isReturnVisit()) {
                        emptyItem = false;
                        tv = new TextView(context);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                            tv.setBackground(ContextCompat.getDrawable(context, R.drawable.alert_bg));
                        else
                            tv.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.alert_bg));

                        tv.setText(R.string.menu_do_not_count_as_return_visit);
                        tv.setTextAppearance(context, android.R.attr.textAppearanceMedium);
                        tv.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                        tv.setGravity(Gravity.CENTER_VERTICAL);
                        tv.setLayoutParams(lp1);
                        tv.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_action_warning), null, null, null);
                        tv.setCompoundDrawablePadding(padding);

                        ll.addView(tv);
                    }

                    /** Show Householder if exists */
                    if (!TextUtils.isEmpty(entryitem.getHouseholder())) {
                        emptyItem = false;
                        tv = new TextView(context);
                        tv.setText(entryitem.getHouseholder());
                        tv.setTextAppearance(context, android.R.attr.textAppearanceLarge);
                        tv.setTextColor(ContextCompat.getColor(context, R.color.bg_card_title_text_holo_light));
                        tv.setTypeface(null, Typeface.BOLD);
                        tv.setPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics())
                                , 0
                                , (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics())
                                , 0);
                        tv.setGravity(Gravity.CENTER_VERTICAL);
                        tv.setLayoutParams(lp1);

                        ll.addView(tv);
                    }

                    /** Show Notes if exists */
                    if (!TextUtils.isEmpty(entryitem.getNotes())) {
                        ImageView iv = new ImageView(context);
                        iv.setPadding(0, 0, padding, 0);
                        //iv.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_chat));
                        iv.setLayoutParams(lp2);
                        iv.setContentDescription(context.getResources().getString(R.string.form_notes));

                        tv = new TextView(context);
                        tv.setText(entryitem.getNotes());
                        tv.setTextAppearance(context, android.R.attr.textAppearanceMedium);
                        tv.setTextColor(ContextCompat.getColor(context, R.color.default_text));
                        tv.setGravity(Gravity.CENTER_VERTICAL);
                        tv.setLayoutParams(lp2);
                        tv.setPadding(0, (emptyItem) ? 0 : padding, 0, 0);

                        LinearLayout linlay = new LinearLayout(context);
                        linlay.setLayoutParams(lp1);
                        linlay.setOrientation(LinearLayout.HORIZONTAL);
                        linlay.addView(iv);
                        linlay.addView(tv);

                        ll.addView(linlay);
                    }

                    /** Load the publications for the entry */
                    for (PublicationItem item : entryitem.pubs) {
                        tv = new TextView(context);
                        tv.setTextAppearance(context, android.R.attr.textAppearanceMedium);
                        tv.setTextColor(ContextCompat.getColor(context, R.color.default_text));
                        tv.setGravity(Gravity.CENTER_VERTICAL);
                        tv.setText(item.toString());
                        tv.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, item.iconRes), null, null, null);
                        tv.setCompoundDrawablePadding(padding);
                        tv.setLayoutParams(lp1);

                        ll.addView(tv);
                    }
                }

                holder.linlay.addView(ll);
            }
        }
    }

    public class PublicationItem {
        public String title;
        public int iconRes;
        public Drawable icon;
        public int count;
        public int litTypeID;

        public PublicationItem(String _title, int _litTypeID, int _count) {
            title = _title;
            litTypeID = _litTypeID;
            count = _count;
            iconRes = Helper.getIconResIDByLitTypeID(_litTypeID);
        }

        public String toString() {
            return "(" + count + ") " + title;
        }
    }

    public class Entry {
        public ArrayList<PublicationItem> pubs;
        public String householder;
        public String notes;
        public boolean return_visit;

        public Entry() {
            pubs = new ArrayList<>();
            householder = "";
            notes = "";
        }

        public void setHouseholder(String string) {
            householder = string;
        }

        public String getHouseholder() {
            return householder;
        }

        public void setNotes(String string) {
            notes = string;
        }

        public boolean isReturnVisit() {
            return return_visit;
        }

        public void setIsReturnVisit(int val) {
            return_visit = val != 0;
        }

        public String getNotes() {
            return notes;
        }

        public void addPublication(PublicationItem _pub) {
            pubs.add(_pub);
        }
    }
}