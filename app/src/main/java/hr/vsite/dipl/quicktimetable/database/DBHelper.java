package hr.vsite.dipl.quicktimetable.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import hr.vsite.dipl.quicktimetable.constants.QTTconstants;

/**
 * Created by Jure on 16.6.2015..
 */
public class
DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = QTTconstants.DATABASE_NAME;
    public static final int DB_VERSION = 2;

    public static final String CREATE_TABLE_STATIONS = "CREATE TABLE " + DBContract.Stations.TABLE_STATIONS +
            " ( "
            + DBContract.Stations.STATION_ID + " INTEGER PRIMARY KEY" + " ,"
            + DBContract.Stations.LATITUDE + " NUMERIC" + " ,"
            + DBContract.Stations.LONGITUDE + " NUMERIC" + " ,"
            + DBContract.Stations.STATION_NAME + " TEXT" + " ,"
            + DBContract.Stations.STATION_NAME_SHORT + " TEXT" +
            " )";

    public static final String CREATE_TABLE_LINES = "CREATE TABLE " + DBContract.Lines.TABLE_LINES +
            " ( "
            + DBContract.Lines.LINE_ID + " INTEGER PRIMARY KEY" + " ,"
            + DBContract.Lines.LINE_NAME + " TEXT" + " ,"
            + DBContract.Lines.LINE_NUMBER + " INTEGER" + " ,"
            + DBContract.Lines.START_STATION + " TEXT" + " ,"
            + DBContract.Lines.END_STATION + " TEXT" +
            ")";

    public static final String CREATE_TABLE_LINE_SEGMENTS = "CREATE TABLE " + DBContract.LineSegments.TABLE_LINE_SEGMENTS +
            " ( "
            + DBContract.LineSegments.SEGMENT_ID + " INTEGER PRIMARY KEY" + " ,"
//            + DBContract.LineSegments.SEGMENT_ID + " INTEGER NOT NULL" + " ,"
            + DBContract.LineSegments.ORIGIN_STATION_ID + " INTEGER" + " ,"
            + DBContract.LineSegments.DESTINATION_STATION_ID + " INTEGER" + " ,"
            + DBContract.LineSegments.SEGMENT_TIME + " INTEGER NOT NULL" + " ,"
            + DBContract.LineSegments.LINE_NUMBER + " INTEGER" + " ,"
            + DBContract.LineSegments.DIRECTION + " INTEGER" +
            ")";

    public static final String CREATE_TABLE_STATIONS_LINES = "CREATE TABLE " + DBContract.StationsLines.TABLE_STATIONS_LINES +
            " ( "
            + DBContract.StationsLines.LINE_ID + " INTEGER NOT NULL" + " ,"
            + DBContract.StationsLines.STATION_ID + "  INTEGER NOT NULL" + " , "
            + DBContract.StationsLines.LINE_DIRECTION + " INTEGER" + " ,"
//            + "CONSTRAINT unq UNIQUE(" + DBContract.StationsLines.LINE_NUMBER + "," +  DBContract.StationsLines.STATION_ID + ")" + ","
            + "FOREIGN KEY (" + DBContract.StationsLines.LINE_ID +")" + "REFERENCES " + DBContract.Lines.TABLE_LINES + "(" + DBContract.Lines.LINE_ID + "),"
            + "FOREIGN KEY (" + DBContract.StationsLines.STATION_ID + ") REFERENCES " + DBContract.Stations.TABLE_STATIONS + "(" + DBContract.Stations.STATION_ID + ")" +
            ")";

    public static final String CREATE_TABLE_TRAINS = "CREATE TABLE " + DBContract.Trains.TABLE_TRAINS +
            " ( "
            + DBContract.Trains.TRAIN_ID + " INTEGER PRIMARY KEY" + ","
            + DBContract.Trains.LINE_ID + " INTEGER" + ","
            + DBContract.Trains.ORIGIN_ID + " INTEGER" + ","
            + DBContract.Trains.DESTINATION_ID + " INTEGER" + ","
            + DBContract.Trains.DEPART_ORIGIN_TIME + " TIME NOT NULL" + ","
            + "FOREIGN KEY (" + DBContract.Trains.LINE_ID + ") REFERENCES " + DBContract.Lines.TABLE_LINES + "(" + DBContract.Lines.LINE_ID + ")" +
            ")";

    public static final String CREATE_TABLE_DEPARTURE_TIMES = "CREATE TABLE " + DBContract.DepartureTimes.TABLE_DEPARTURE_TIMES +
            " ( "
            + DBContract.DepartureTimes._ID + " INTEGER PRIMARY KEY" + ","
            + DBContract.DepartureTimes.STATION_ID + " INTEGER NOT NULL" + ","
            + DBContract.DepartureTimes.LINE_NUMBER + " INTEGER NOT NULL" + ","
            + DBContract.DepartureTimes.DEPARTURE_TIME + " TIME NOT NULL" + ","

//            + "FOREIGN KEY (" + DBContract.DepartureTimes.TRAIN_ID +")" + "REFERENCES " + DBContract.Trains.TABLE_TRAINS + "(" + DBContract.Trains.TRAIN_ID + "),"
            + "FOREIGN KEY (" + DBContract.DepartureTimes.STATION_ID + ") REFERENCES " + DBContract.Stations.TABLE_STATIONS + "(" + DBContract.Stations.STATION_ID + ")" +
            ")";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_STATIONS);
        db.execSQL(CREATE_TABLE_LINES);
        db.execSQL(CREATE_TABLE_LINE_SEGMENTS);
        db.execSQL(CREATE_TABLE_STATIONS_LINES);
        db.execSQL(CREATE_TABLE_DEPARTURE_TIMES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropStations = "DROP TABLE IF EXISTS " + DBContract.Stations.TABLE_STATIONS;
        String dropLines = "DROP TABLE IF EXISTS " + DBContract.Lines.TABLE_LINES;
        String dropLineSegments = "DROP TABLE IF EXISTS " + DBContract.LineSegments.TABLE_LINE_SEGMENTS;
        String dropStationsLines = "DROP TABLE IF EXISTS " + DBContract.StationsLines.TABLE_STATIONS_LINES;
        String dropDepartureTimes = "DROP TABLE IF EXISTS " + DBContract.DepartureTimes.TABLE_DEPARTURE_TIMES;

        db.execSQL(dropStations);
        db.execSQL(dropLines);
        db.execSQL(dropLineSegments);
        db.execSQL(dropStationsLines);
        db.execSQL(dropDepartureTimes);
        onCreate(db);
    }

    public void forceUpgrade(SQLiteDatabase db){
        this.onUpgrade(db, db.getVersion(), db.getVersion() + 1);
    }
}
