package hr.vsite.dipl.quicktimetable;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import hr.vsite.dipl.quicktimetable.database.DBContract;
import hr.vsite.dipl.quicktimetable.database.DBHelper;

public class QttContentProvider extends ContentProvider {
    public QttContentProvider() {
    }

    private static final int URI_STATIONS = 100;
    private static final int URI_STATIONS_ID = 101;
    private static final int URI_STATIONS_ALL = 102;
    private static final int URI_STATIONS_NEAREST = 103;
    private static final int URI_STATIONS_WITH_LINE = 104;
    private static final int URI_STATIONS_WITH_LINE_SHORT = 105;
    private static final int URI_LINES = 200;
    private static final int URI_LINE_SEGMENTS = 300;
    private static final int URI_STATIONS_LINES = 400;
    private static final int URI_TRAINS = 500;
    private static final int URI_DEPARTURE_TIMES = 600;
    private static final int URI_DEPARTURE_TIMES_FOR_HOUR = 601;
    private static final int URI_DEPARTURE_TIMES_FOR_HOUR_AND_LINE = 602;
    private static final int URI_TRANSFER_STATIONS = 700;



    private DBHelper dbHelper = null;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(DBContract.CONTENT_AUTHORITY, DBContract.STATIONS_PATH, URI_STATIONS);
        uriMatcher.addURI(DBContract.CONTENT_AUTHORITY, DBContract.STATIONS_PATH + "/#", URI_STATIONS_ID);
        uriMatcher.addURI(DBContract.CONTENT_AUTHORITY, DBContract.STATIONS_ALL, URI_STATIONS_ALL);
        uriMatcher.addURI(DBContract.CONTENT_AUTHORITY, DBContract.STATIONS_WITH_LINE, URI_STATIONS_WITH_LINE);
        uriMatcher.addURI(DBContract.CONTENT_AUTHORITY, DBContract.STATIONS_WITH_LINE_SHORT, URI_STATIONS_WITH_LINE_SHORT);
        uriMatcher.addURI(DBContract.CONTENT_AUTHORITY, DBContract.STATIONS_NEAREST, URI_STATIONS_NEAREST);
        uriMatcher.addURI(DBContract.CONTENT_AUTHORITY, DBContract.LINES_PATH, URI_LINES);
        uriMatcher.addURI(DBContract.CONTENT_AUTHORITY, DBContract.LINE_SEGMENTS_PATH, URI_LINE_SEGMENTS);
        uriMatcher.addURI(DBContract.CONTENT_AUTHORITY, DBContract.STATIONS_LINES_PATH, URI_STATIONS_LINES);
        uriMatcher.addURI(DBContract.CONTENT_AUTHORITY, DBContract.TRAINS_PATH, URI_TRAINS);
        uriMatcher.addURI(DBContract.CONTENT_AUTHORITY, DBContract.DEPARTURE_TIMES_PATH, URI_DEPARTURE_TIMES);
        uriMatcher.addURI(DBContract.CONTENT_AUTHORITY, DBContract.DEPARTURE_TIMES_PATH + "/hours", URI_DEPARTURE_TIMES_FOR_HOUR);
        uriMatcher.addURI(DBContract.CONTENT_AUTHORITY, DBContract.DEPARTURE_TIMES_PATH + "/hours_lines" , URI_DEPARTURE_TIMES_FOR_HOUR_AND_LINE);
        uriMatcher.addURI(DBContract.CONTENT_AUTHORITY, DBContract.STATIONS_LINES_TRANSFERS, URI_TRANSFER_STATIONS);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int numberOfRows = 0;
        switch (uriMatcher.match(uri)) {
            case URI_STATIONS:
                numberOfRows = db.delete(DBContract.Stations.TABLE_STATIONS, selection, selectionArgs);
                break;
            case URI_LINES:
                numberOfRows = db.delete(DBContract.Lines.TABLE_LINES, selection, selectionArgs);
                break;
            case URI_LINE_SEGMENTS:
                numberOfRows = db.delete(DBContract.LineSegments.TABLE_LINE_SEGMENTS, selection, selectionArgs);
                break;
            case URI_STATIONS_LINES:
                numberOfRows = db.delete(DBContract.StationsLines.TABLE_STATIONS_LINES, selection, selectionArgs);
                break;
            case URI_TRAINS:
                numberOfRows = db.delete(DBContract.Trains.TABLE_TRAINS, selection, selectionArgs);
                break;
            case URI_DEPARTURE_TIMES_FOR_HOUR:
                numberOfRows = db.delete(DBContract.DepartureTimes.TABLE_DEPARTURE_TIMES, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        return numberOfRows;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case URI_STATIONS:
                return DBContract.Stations.CONTENT_TYPE;
            case URI_LINE_SEGMENTS:
                return DBContract.LineSegments.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;
        Uri retUri;

        switch (uriMatcher.match(uri))
        {
            case URI_STATIONS:
                id = db.insert(DBContract.Stations.TABLE_STATIONS, null, values);
                if(id == -1) {
                    throw new SQLiteException("Error inserting Station");
                }
                else {
                    retUri = DBContract.Stations.buildUri(id);
                }
                break;
            case URI_LINES:
                id = db.insert(DBContract.Lines.TABLE_LINES, null, values);
                if(id == -1) {
                    throw new SQLiteException("Error inserting Line");
                }
                else {
                    retUri = DBContract.Lines.buildUri(id);
                }
                break;
            case URI_LINE_SEGMENTS:
                id = db.insert(DBContract.LineSegments.TABLE_LINE_SEGMENTS, null, values);
                if(id == -1) {
                    throw new SQLiteException("Error inserting LineSegment");
                }
                else {
                    retUri = DBContract.Lines.buildUri(id);
                }
                break;
            case URI_STATIONS_LINES:
                id = db.insert(DBContract.StationsLines.TABLE_STATIONS_LINES, null, values);
                if(id == -1) {
                    throw new SQLiteException("Error inserting");
                }
                else {
                    retUri = DBContract.StationsLines.buildUri(id);
                }
                break;
            case URI_TRAINS:
                id = db.insert(DBContract.Trains.TABLE_TRAINS, null, values);
                if(id == -1) {
                    throw new SQLiteException("Error inserting Train");
                }
                else {
                    retUri = DBContract.Trains.buildUri(id);
                }
                break;
            case URI_DEPARTURE_TIMES:
                id = db.insert(DBContract.DepartureTimes.TABLE_DEPARTURE_TIMES, null, values);
                if(id == -1) {
                    throw new SQLiteException("Error inserting DepartureTime");
                }
                else {
                    retUri = DBContract.DepartureTimes.buildUri(id);
                }
                break;
            default:
                throw new UnsupportedOperationException("Non-existing URI");
        }
        return retUri;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        switch (uriMatcher.match(uri))
        {
            case URI_STATIONS_ID:
                cursor = db.query(DBContract.Stations.TABLE_STATIONS,
                        null,
                        "_id=?",
                        new String[] {DBContract.Stations.getIdFromUri(uri)},
                        null,
                        null,
                        null);
                break;
            case URI_STATIONS:
                cursor = db.query(DBContract.Stations.TABLE_STATIONS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                break;
            case URI_STATIONS_NEAREST:
                cursor = db.query(DBContract.Stations.TABLE_STATIONS,
                        projection,
                        null,
                        selectionArgs,
                        null,
                        null,
                        "abs(" + DBContract.Stations.LATITUDE + " - (?)) + abs( " + DBContract.Stations.LONGITUDE + " - (?))",
                        "5");
                break;
            case URI_STATIONS_ALL:
                cursor = db.query(DBContract.Stations.TABLE_STATIONS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                break;
            case URI_STATIONS_WITH_LINE:
                String SQLString = "SELECT " +
                        "S." + DBContract.Stations.STATION_NAME + "," +
                        "S." + DBContract.Stations.STATION_ID + "," +
                        "L." + DBContract.Lines.LINE_NUMBER +
                        " FROM " +
                        DBContract.Stations.TABLE_STATIONS + " S" + " INNER JOIN " +
                        DBContract.StationsLines.TABLE_STATIONS_LINES + " SL ON " +
                        "S." + DBContract.Stations.STATION_ID + "=" + "SL." + DBContract.StationsLines.STATION_ID +
                        " INNER JOIN " + DBContract.Lines.TABLE_LINES + " L ON " +
                        "SL." + DBContract.StationsLines.LINE_ID +"=" + "L." + DBContract.Lines.LINE_ID +
                        " WHERE " + DBContract.Stations.STATION_NAME + "=? AND " +
                        DBContract.Lines.LINE_NUMBER + "=? AND " +
                        DBContract.StationsLines.LINE_DIRECTION + "=?";
                cursor = db.rawQuery(SQLString, selectionArgs);
                break;
            case URI_STATIONS_WITH_LINE_SHORT:
                String SQLString2 = "SELECT " +
                        "S." + DBContract.Stations.STATION_NAME_SHORT + "," +
                        "S." + DBContract.Stations.STATION_ID + "," +
                        "L." + DBContract.Lines.LINE_NUMBER +
                        " FROM " +
                        DBContract.Stations.TABLE_STATIONS + " S" + " INNER JOIN " +
                        DBContract.StationsLines.TABLE_STATIONS_LINES + " SL ON " +
                        "S." + DBContract.Stations.STATION_ID + "=" + "SL." + DBContract.StationsLines.STATION_ID +
                        " INNER JOIN " + DBContract.Lines.TABLE_LINES + " L ON " +
                        "SL." + DBContract.StationsLines.LINE_ID +"=" + "L." + DBContract.Lines.LINE_ID +
                        " WHERE " + DBContract.Stations.STATION_NAME_SHORT + "=? AND " +
                        DBContract.Lines.LINE_NUMBER + "=? AND " +
                        DBContract.StationsLines.LINE_DIRECTION + "=?";
                cursor = db.rawQuery(SQLString2, selectionArgs);
                break;
            case URI_LINES:
                cursor = db.query(DBContract.Lines.TABLE_LINES,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                break;
            case URI_LINE_SEGMENTS:
                cursor = db.query(DBContract.LineSegments.TABLE_LINE_SEGMENTS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                break;
            case URI_DEPARTURE_TIMES:
                cursor = db.query(DBContract.DepartureTimes.TABLE_DEPARTURE_TIMES,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                break;
            case URI_DEPARTURE_TIMES_FOR_HOUR:
                SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                builder.setDistinct(true);
                builder.setTables(DBContract.DepartureTimes.TABLE_DEPARTURE_TIMES +
                        " INNER JOIN " +
                        DBContract.LineSegments.TABLE_LINE_SEGMENTS +
                        " ON " +
                        DBContract.DepartureTimes.STATION_ID + "=" + DBContract.LineSegments.ORIGIN_STATION_ID);
                cursor = builder.query(db,
                        projection,
                        DBContract.DepartureTimes.STATION_ID + "= ? AND " +
                                "(" + DBContract.DepartureTimes.DEPARTURE_TIME + " LIKE ? OR " +
                                DBContract.DepartureTimes.DEPARTURE_TIME + " LIKE ? )",
                        selectionArgs,
                        null,
                        null,
                        DBContract.DepartureTimes.DEPARTURE_TIME + " ASC",
                        null);
                break;
            case URI_DEPARTURE_TIMES_FOR_HOUR_AND_LINE:
                builder = new SQLiteQueryBuilder();
                builder.setDistinct(true);
                builder.setTables(DBContract.DepartureTimes.TABLE_DEPARTURE_TIMES +
                        " INNER JOIN " +
                        DBContract.LineSegments.TABLE_LINE_SEGMENTS +
                        " ON " +
                        DBContract.DepartureTimes.STATION_ID + "=" + DBContract.LineSegments.ORIGIN_STATION_ID);
                cursor = builder.query(db,
                        projection,
                        DBContract.DepartureTimes.STATION_ID + "= ? AND " +
                                DBContract.LineSegments.LINE_NUMBER + "= ? AND " +
                                "(" + DBContract.DepartureTimes.DEPARTURE_TIME + " LIKE ? OR " +
                                DBContract.DepartureTimes.DEPARTURE_TIME + " LIKE ? )",
                        selectionArgs,
                        null,
                        null,
                        DBContract.DepartureTimes.DEPARTURE_TIME + " ASC",
                        null);
                break;
            case URI_TRANSFER_STATIONS:
                builder = new SQLiteQueryBuilder();
                builder.setTables(DBContract.StationsLines.TABLE_STATIONS_LINES);
                cursor = db.query(DBContract.StationsLines.TABLE_STATIONS_LINES,
                        new String[]{
                                DBContract.StationsLines.STATION_ID,
                                DBContract.StationsLines.LINE_ID,
                                "count(" + DBContract.StationsLines.LINE_ID + ") as c"},
                        selection,
                        selectionArgs,
                        DBContract.Stations.STATION_ID,
                        "c > 1",
                        null,
                        null);
                break;

            default:
                throw new UnsupportedOperationException("Invalid uri");
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int numberOfRows;

        switch (uriMatcher.match(uri)) {
            case URI_STATIONS:
                numberOfRows = db.update(DBContract.Stations.TABLE_STATIONS, values, selection, selectionArgs);
                break;
            case URI_LINES:
                numberOfRows = db.update(DBContract.Lines.TABLE_LINES, values, selection, selectionArgs);
                break;
            case URI_LINE_SEGMENTS:
                numberOfRows = db.update(DBContract.LineSegments.TABLE_LINE_SEGMENTS, values, selection, selectionArgs);
                break;
            case URI_STATIONS_LINES:
                numberOfRows = db.update(DBContract.StationsLines.TABLE_STATIONS_LINES, values, selection, selectionArgs);
                break;
            case URI_TRAINS:
                numberOfRows = db.update(DBContract.Trains.TABLE_TRAINS, values, selection, selectionArgs);
                break;
            case URI_DEPARTURE_TIMES:
                numberOfRows = db.update(DBContract.DepartureTimes.TABLE_DEPARTURE_TIMES, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Non-existing URI");
        }
        return numberOfRows;
    }
}
