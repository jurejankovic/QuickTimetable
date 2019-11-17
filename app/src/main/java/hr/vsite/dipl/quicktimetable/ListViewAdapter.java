package hr.vsite.dipl.quicktimetable;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import hr.vsite.dipl.quicktimetable.constants.QTTconstants;

/**
 * Created by Jure on 3.6.2017..
 */

public class ListViewAdapter extends BaseAdapter {
    public ArrayList<LineDeparturePair> list;
    Activity activity;
    Context context;

    public ListViewAdapter(Context context, ArrayList<LineDeparturePair> list) {
        super();
        this.context = context;
        this.list = list;
    }

    static class ViewHolder {
        TextView tvLine;
        TextView tvTime;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            holder  = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_departure_row, parent, false);
            holder.tvLine = (TextView) convertView.findViewById(R.id.textLine);
            holder.tvTime = (TextView) convertView.findViewById(R.id.textTime);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        LineDeparturePair ldPair = list.get(position);
        holder.tvLine.setText(ldPair.getLineNumber());
        holder.tvTime.setText(ldPair.getDepartureTime());

        return convertView;
    }

    public ArrayList<LineDeparturePair> getValues() {
        return list;
    }
}
