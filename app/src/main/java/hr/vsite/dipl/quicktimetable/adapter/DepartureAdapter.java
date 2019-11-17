package hr.vsite.dipl.quicktimetable.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jure on 17.3.2017..
 */

public class DepartureAdapter<String> extends ArrayAdapter {

    public DepartureAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<java.lang.String> objects) {
        super(context, resource, objects);
    }

    public ArrayList<String> getValues() {
        ArrayList<String> objects = new ArrayList<>();
        for (int i = 0; i < this.getCount(); i++) {
            objects.add((String) this.getItem(i));
        }
        return objects;
    }
}
