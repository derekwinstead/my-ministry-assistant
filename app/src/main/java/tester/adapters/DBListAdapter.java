package tester.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.myMinistry.R;

import java.io.File;
import java.util.ArrayList;

public class DBListAdapter extends ArrayAdapter<File> {
    private static final int LAYOUT_ID = R.layout.li_db_item;
    private ArrayList<File> list;

    public DBListAdapter(Context context, File[] list) {
        super(context, LAYOUT_ID, list);
        this.list = new ArrayList<>();
        for(File file : list) {
            this.list.add(file);
        }
    }

    public DBListAdapter(Context context) {
        super(context, 0);
    }

    public void resetList(File[] list) {
        this.list = new ArrayList<>();
        for(File file : list) {
            this.list.add(file);
        }
    }

    private class ViewHolder {
        TextView title;
        TextView date;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if(row == null) {
            //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //row = inflater.inflate(LAYOUT_ID, parent, false);


            //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = LayoutInflater.from(getContext()).inflate(LAYOUT_ID, null);
            //row = inflater.inflate(LAYOUT_ID, parent, false);

            holder = new ViewHolder();
            holder.title = (TextView)row.findViewById(R.id.title);
            holder.date = (TextView)row.findViewById(R.id.date);

            row.setTag(holder);
        }
        else {
            holder = (ViewHolder)row.getTag();
        }

        File file = list.get(position);

        holder.title.setText(file.getName());
        holder.date.setText(DateUtils.formatDateTime(getContext(), file.lastModified(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR));

        return row;
    }
}