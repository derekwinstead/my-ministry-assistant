package com.myMinistry.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.myMinistry.Helper;
import com.myMinistry.R;
import com.myMinistry.model.ItemWithIcon;
import com.myMinistry.provider.MinistryContract;
import com.myMinistry.provider.MinistryDatabase;
import com.myMinistry.provider.MinistryService;

public class ItemWithIconAdapter extends ArrayAdapter<ItemWithIcon> {
    private static final int LAYOUT_CONTENT_ID = R.layout.li_item_spinner_item_3;
    private static final int LAYOUT_SEPARATOR_ID = R.layout.li_separator_item_spinner_item;

    private int separatorId = MinistryDatabase.CREATE_ID;
    private Context context;

    private static final int ITEM_VIEW_TYPE_RECORD = 0;
    private static final int ITEM_VIEW_TYPE_SEPARATOR = 1;
    private static final int ITEM_VIEW_TYPE_COUNT = 2;

    public ItemWithIconAdapter(Context context) {
        super(context, 0);
        this.context = context;
    }

    public ItemWithIconAdapter(Context context, Cursor cursor) {
        super(context, 0);
        this.context = context;
        //loadNewData(cursor);
    }

    public void addSeparatorItem(String title, int count) {
        addItem(new ItemWithIcon(separatorId, title, count));
    }


    public void addItem(String title, int icon) {
        add(new ItemWithIcon(title, icon));
    }

    public void addItem(ItemWithIcon itemModel) {
        add(itemModel);
    }

    public void setTitle(String _title, int _position) {
        ItemWithIcon thisItem = this.getItem(_position);
        thisItem.setTitle(_title);
    }

    public ItemWithIcon getItemByID(int _id) {
        for (int i = 0; i < this.getCount(); i++) {
            ItemWithIcon item = this.getItem(i);
            if (item.getID() == _id)
                return item;
        }
        return null;
    }

    public int getPositionByID(int _id) {
        for (int i = 0; i < this.getCount(); i++) {
            ItemWithIcon item = this.getItem(i);
            if (item.getID() == _id)
                return i;
        }
        return -1;
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
        public final TextView textHolder1;
        public final TextView textHolder2;
        public final ImageView imgHolder;
        public final TextView menurow_count;

        public ViewHolder(TextView text1, TextView text2, ImageView img1, TextView count) {
            this.textHolder1 = text1;
            this.textHolder2 = text2;
            this.imgHolder = img1;
            this.menurow_count = count;
        }
    }

    @SuppressWarnings("deprecation")
    public View getView(int position, View convertView, ViewGroup parent) {
        final int type = getItemViewType(position);
        ItemWithIcon item = getItem(position);
        ViewHolder holder = null;
        View view = convertView;

        if(view == null) {
            //view = LayoutInflater.from(getContext()).inflate(LAYOUT_CONTENT_ID, parent, false);
            view = LayoutInflater.from(getContext()).inflate(type == ITEM_VIEW_TYPE_SEPARATOR ? LAYOUT_SEPARATOR_ID : LAYOUT_CONTENT_ID, parent, false);
            TextView text1 = (TextView) view.findViewById(R.id.menurow_title);
            TextView text2 = (TextView) view.findViewById(R.id.menurow_subtitle);
            ImageView img1 = (ImageView) view.findViewById(R.id.menurow_img);
            TextView tvCount = (TextView) view.findViewById(R.id.menurow_count);
            view.setTag(new ViewHolder(text1, text2, img1, tvCount));
        }

        if(holder == null && view != null) {
            Object tag = view.getTag();
            if (tag instanceof ViewHolder)
                holder = (ViewHolder) tag;
        }

        holder.textHolder1.setText(item.title);
        if(type == ITEM_VIEW_TYPE_SEPARATOR) {
            holder.menurow_count.setText(getItem(position).getCount());
        } else {
            holder.textHolder2.setText((item.is_default == 1) ? getContext().getResources().getString(R.string.is_default) : "");

            Drawable img = getContext().getResources().getDrawable(item.iconRes);
            holder.imgHolder.setImageDrawable(img);
        }

        return view;
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
                if(cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType.ACTIVE)) == MinistryService.ACTIVE)
                    activeCount++;
                else
                    inactiveCount++;

                if(cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType.ACTIVE)) != activeId) {
                    if(cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType.ACTIVE)) == MinistryService.ACTIVE)
                        firstActivePosition = cursor.getPosition();
                    else
                        firstInActivePosition = cursor.getPosition();

                    activeId = cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType.ACTIVE));
                    addSeparatorItem(activeId == MinistryService.INACTIVE ? context.getResources().getString(R.string.form_is_inactive) : context.getResources().getString(R.string.form_is_active), cursor.getCount() - cursor.getPosition());
                }

                addItem(new ItemWithIcon(cursor.getString(cursor.getColumnIndex(MinistryContract.LiteratureType.NAME))
                        ,Helper.getIconResIDByLitTypeID(cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType._ID)))
                        ,cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType._ID))
                        ,cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType.ACTIVE))
                        ,cursor.getInt(cursor.getColumnIndex(MinistryContract.LiteratureType.DEFAULT)))
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
}