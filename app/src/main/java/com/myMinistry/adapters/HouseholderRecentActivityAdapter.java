package com.myMinistry.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ResourceCursorAdapter;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryContract.Literature;
import com.myMinistry.provider.MinistryContract.LiteraturePlaced;
import com.myMinistry.provider.MinistryContract.LiteratureType;
import com.myMinistry.provider.MinistryContract.Notes;
import com.myMinistry.provider.MinistryContract.Time;
import com.myMinistry.provider.MinistryContract.UnionsNameAsRef;
import com.myMinistry.provider.MinistryService;
import com.squareup.phrase.Phrase;

import java.util.Calendar;
import java.util.Locale;

public class HouseholderRecentActivityAdapter extends ResourceCursorAdapter {
    private static final int LAYOUT_ID = R.layout.li_householder_recent_activity;
    private Calendar displayDate = Calendar.getInstance(Locale.getDefault());
    private MinistryService database;
    private Cursor cursorpubs;
    private static int householderID;
    private int padding;

    public HouseholderRecentActivityAdapter(Context context, Cursor cursor, int _householderID) {
        super(context, LAYOUT_ID, cursor, ResourceCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        database = new MinistryService(context);
        householderID = _householderID;
        padding = Helper.dipsToPix(context, 5);
    }

    public void setHouseholderID(int _householderID) {
        householderID = _householderID;
    }

    private class ViewHolder {
        TextView activity_title;
        LinearLayout activity_publications;
        LinearLayout activity_notes_layout;
        TextView activity_notes;
        TextView activity_date;
    }

    @Override
    public View newView(Context context, Cursor cur, ViewGroup parent) {
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(LAYOUT_ID, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.activity_title = (TextView) view.findViewById(R.id.activity_title);
        holder.activity_publications = (LinearLayout) view.findViewById(R.id.activity_publications);
        holder.activity_notes_layout = (LinearLayout) view.findViewById(R.id.activity_notes_layout);
        holder.activity_notes = (TextView) view.findViewById(R.id.activity_notes);
        holder.activity_date = (TextView) view.findViewById(R.id.activity_date);
        view.setTag(holder);
        return view;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        /** Title */
        holder.activity_title.setText(Phrase.from(context, R.string.activity_for_householder)
                .put("service", cursor.getString(cursor.getColumnIndex(MinistryContract.UnionsNameAsRef.ENTRY_TYPE_NAME)))
                .format().toString());

        /** Publications */
        if (cursor.getInt(cursor.getColumnIndex(UnionsNameAsRef.COUNT)) > 0) {
            holder.activity_publications.setVisibility(View.VISIBLE);
            holder.activity_publications.removeAllViews();

            database.openWritable();

            cursorpubs = database.fetchPlacedLitByTimeAndHouseholderID(cursor.getInt(cursor.getColumnIndex(Time._ID)), householderID);

            for (cursorpubs.moveToFirst(); !cursorpubs.isAfterLast(); cursorpubs.moveToNext()) {
                TextView valueTV = new TextView(context);
                valueTV.setText("(" + cursorpubs.getString(cursorpubs.getColumnIndex(LiteraturePlaced.COUNT)) + ") " + cursorpubs.getString(cursorpubs.getColumnIndex(LiteratureType.NAME)));
                valueTV.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, Helper.getIconResIDByLitTypeID(cursorpubs.getInt(cursorpubs.getColumnIndex(Literature.TYPE_OF_LIERATURE_ID)))), null, null, null);
                valueTV.setCompoundDrawablePadding(padding);
                valueTV.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                //valueTV.setTextAppearance(context, android.R.attr.textAppearanceMedium);
                valueTV.setTextColor(ContextCompat.getColor(context, R.color.bg_card_title_text_holo_light));
                valueTV.setGravity(Gravity.CENTER_VERTICAL);

                holder.activity_publications.addView(valueTV);
            }

            cursorpubs.close();
            database.close();
        } else {
            holder.activity_publications.setVisibility(View.GONE);
        }

        /** Notes */
        if (TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(Notes.NOTES)))) {
            holder.activity_notes_layout.setVisibility(View.GONE);
        } else {
            holder.activity_notes_layout.setVisibility(View.VISIBLE);
            holder.activity_notes.setText(cursor.getString(cursor.getColumnIndex(Notes.NOTES)));
        }

        /** Date */
        if (cursor.getString(cursor.getColumnIndex(Time.DATE_START)) != null && cursor.getString(cursor.getColumnIndex(Time.DATE_START)).length() > 0) {
            String[] thedate = cursor.getString(cursor.getColumnIndex(Time.DATE_START)).split("-");
            if (thedate.length == 3) {
                /** We have the three numbers to make the date. Subtract 1 for zero based months. */
                displayDate.set(Integer.valueOf(thedate[0]), Integer.valueOf(thedate[1]) - 1, Integer.valueOf(thedate[2]));
                String date = DateUtils.formatDateTime(context, displayDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY);

                holder.activity_date.setText(Phrase.from(context, R.string.activity_date_by_publisher)
                        .put("date", date)
                        .put("publisher", cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.PUBLISHER_NAME)))
                        .format().toString());
            } else
                holder.activity_date.setText(R.string.no_activity);
        } else
            holder.activity_date.setText(R.string.no_activity);
    }
}