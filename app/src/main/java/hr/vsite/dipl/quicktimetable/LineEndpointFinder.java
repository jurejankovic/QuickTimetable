package hr.vsite.dipl.quicktimetable;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import hr.vsite.dipl.quicktimetable.database.DBContract;

/**
 * Created by Jure on 24.1.2017..
 */

public class LineEndpointFinder implements Callable<List<LineEndpoint>> {
    private ContentResolver contentResolver;
    private List<LineEndpoint> lineEndpoints = new ArrayList<LineEndpoint>();

    public LineEndpointFinder(Context context) {
        this.contentResolver = context.getContentResolver();
    }

    @Override
    public List<LineEndpoint> call() throws Exception {
        String[] columns = {
                DBContract.Lines.LINE_NUMBER,
                DBContract.Lines.START_STATION,
                DBContract.Lines.END_STATION
        };
        Cursor c = contentResolver.query(DBContract.Lines.CONTENT_URI, columns, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                lineEndpoints.add(new LineEndpoint(
                        c.getString(c.getColumnIndex(DBContract.Lines.LINE_NUMBER)),
                        c.getString(c.getColumnIndex(DBContract.Lines.START_STATION)),
                        c.getString(c.getColumnIndex(DBContract.Lines.END_STATION))
                        )
                );

            }
        }

        return lineEndpoints;
    }
}
