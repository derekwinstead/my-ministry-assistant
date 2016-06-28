package com.myMinistry.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.myMinistry.R;
import com.myMinistry.model.ItemWithDate;
import com.myMinistry.provider.MinistryContract.UnionsNameAsRef;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;
import com.myMinistry.util.TimeUtils;
import com.squareup.phrase.Phrase;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

public class TitleAndDateAdapterUpdated extends ArrayAdapter<ItemWithDate> {
    private static final int LAYOUT_CONTENT_ID = R.layout.li_item_spinner_item_simple;
    private static final int LAYOUT_SEPARATOR_ID = R.layout.li_separator_item_spinner_item;

    private int separatorId = MinistryDatabase.CREATE_ID;
    private int leadingTextResId = 0;
    private Context context;

    private static final int ITEM_VIEW_TYPE_RECORD = 0;
    private static final int ITEM_VIEW_TYPE_SEPARATOR = 1;
    private static final int ITEM_VIEW_TYPE_COUNT = 2;

    public TitleAndDateAdapterUpdated(Context context) {
        super(context, 0);
    }

    public TitleAndDateAdapterUpdated(Context context, Cursor cursor, int leadingTextResId) {
        super(context, 0);
        this.leadingTextResId = leadingTextResId;
        this.context = context;
        loadNewData(cursor);
    }

    public void loadNewData(Cursor cursor) {
        this.clear();
        int activeId = -1;
        int firstActivePosition = -1;
        int firstInActivePosition = -1;
        int activeCount = 0;
        int inactiveCount = 0;
        if(cursor != null) {
            for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
                if(cursor.getInt(cursor.getColumnIndex(UnionsNameAsRef.ACTIVE)) == MinistryService.ACTIVE)
                    activeCount++;
                else
                    inactiveCount++;

                if(cursor.getInt(cursor.getColumnIndex(UnionsNameAsRef.ACTIVE)) != activeId) {
                    if(cursor.getInt(cursor.getColumnIndex(UnionsNameAsRef.ACTIVE)) == MinistryService.ACTIVE)
                        firstActivePosition = cursor.getPosition();
                    else
                        firstInActivePosition = cursor.getPosition();

                    activeId = cursor.getInt(cursor.getColumnIndex(UnionsNameAsRef.ACTIVE));
                    addSeparatorItem(activeId == MinistryService.INACTIVE ? context.getResources().getString(R.string.inactive) : context.getResources().getString(R.string.active), cursor.getCount() - cursor.getPosition());
                }


                addItem( cursor.getInt(cursor.getColumnIndex(UnionsNameAsRef._ID))
                        ,cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.TITLE))
                        ,cursor.getString(cursor.getColumnIndex(UnionsNameAsRef.DATE))
                );
            }

            if(firstActivePosition != -1) {
                getItem(firstActivePosition).setCount(activeCount);
            }

            if(firstInActivePosition != -1) {
                getItem(firstInActivePosition).setCount(inactiveCount);
            }
        }
    }

    public void addSeparatorItem(String title, int count) {
        addItem(new ItemWithDate(separatorId, title, count));
    }

    private String createDisplayDate(Calendar date) {
        return Phrase.from(context, leadingTextResId).put("date", DateUtils.formatDateTime(context, date.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY)).format().toString();
    }

    public void addItem(int id, String title, String date) {
        Calendar convertedDate = Calendar.getInstance(Locale.getDefault());
        if(!TextUtils.isEmpty(date)) {
            try {
                convertedDate.setTime(TimeUtils.dbDateFormat.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        addItem(new ItemWithDate(id, title, !TextUtils.isEmpty(date) ? createDisplayDate(convertedDate) : context.getResources().getString(R.string.no_activity)));
    }

    private void addItem(ItemWithDate itemModel) {
        add(itemModel);
    }

    public void setTitle(String title, int position) {
        getItem(position).setTitle(title);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getID();
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return (getItem(position).getID() == separatorId) ? ITEM_VIEW_TYPE_SEPARATOR : ITEM_VIEW_TYPE_RECORD;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != ITEM_VIEW_TYPE_SEPARATOR; // A separator cannot be clicked !
    }

    public static class ViewHolder {
        public final TextView menurow_title;
        public final TextView menurow_count;
        public final TextView menurow_subtitle;

        public ViewHolder(TextView text1, TextView count, TextView activity) {
            this.menurow_title = text1;
            this.menurow_count = count;
            this.menurow_subtitle = activity;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int type = getItemViewType(position);
        ViewHolder holder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(type == ITEM_VIEW_TYPE_SEPARATOR ? LAYOUT_SEPARATOR_ID : LAYOUT_CONTENT_ID, parent, false);
            TextView text1 = (TextView) convertView.findViewById(R.id.menurow_title);
            TextView tvCount = (TextView) convertView.findViewById(R.id.menurow_count);
            TextView tvActivity = (TextView) convertView.findViewById(R.id.menurow_subtitle);
            convertView.setTag(new ViewHolder(text1, tvCount, tvActivity));
        }

        if(holder == null && convertView != null) {
            Object tag = convertView.getTag();
            if (tag instanceof ViewHolder)
                holder = (ViewHolder) tag;
        }

        holder.menurow_title.setText(getItem(position).toString());
        if(type == ITEM_VIEW_TYPE_SEPARATOR) {
            holder.menurow_count.setText(getItem(position).getCount());
        } else {
            holder.menurow_subtitle.setText(getItem(position).getDate());
        }

        return convertView;
    }
}