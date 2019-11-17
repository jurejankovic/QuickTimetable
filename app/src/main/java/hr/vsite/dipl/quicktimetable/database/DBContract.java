package hr.vsite.dipl.quicktimetable.database;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;


/**
 * Created by Jure on 17.6.2015..
 */
public final class DBContract {

    public static final String CONTENT_AUTHORITY = "hr.vsite.dipl.quicktimetable.provider";
    public static final Uri BASE_CONTENT_URI = new Uri.Builder()
            .scheme("content")
            .authority(CONTENT_AUTHORITY)
            .build();

    public static final String STATIONS_PATH = "stations";
    public static final String STATIONS_ALL = "stations/all";
    public static final String STATIONS_WITH_LINE = "stations/with_line";
    public static final String STATIONS_WITH_LINE_SHORT = "stations/with_line_short";
    public static final String STATIONS_NEAREST = "stations/nearest";
    public static final String LINES_PATH = "lines";
    public static final String STATIONS_LINES_PATH = "stations-lines";
    public static final String STATIONS_LINES_TRANSFERS = "stations-lines/transfers";
    public static final String LINE_SEGMENTS_PATH = "line_segments";
    public static final String TRAINS_PATH = "trains";
    public static final String DEPARTURE_TIMES_PATH = "departure-times";

    public DBContract() {
    }

    // table Stations
    public static abstract class Stations implements BaseColumns {
        public static final String TABLE_STATIONS = "Stations";

        public static final String STATION_ID = "station_id";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String STATION_NAME = "station_name";
        public static final String STATION_NAME_SHORT = "short_name";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(STATIONS_PATH).build();
        public static final Uri CONTENT_URI_ALL = CONTENT_URI.buildUpon().appendPath("all").build();
        public static final Uri CONTENT_URI_WITH_LINE = CONTENT_URI.buildUpon().appendPath("with_line").build();
        public static final Uri CONTENT_URI_NEAREST = CONTENT_URI.buildUpon().appendPath("nearest").build();
        // in case of short name for a station is used
        public static final Uri CONTENT_URI_WITH_LINE_SHORT = CONTENT_URI.buildUpon().appendPath("with_line_short").build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + STATIONS_PATH;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + STATIONS_PATH;


        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildUri(String station) {
            Uri.Builder uri = CONTENT_URI.buildUpon();
            if(station != null) {
                uri.appendQueryParameter(STATION_NAME, station);
                // dodavanje paramentrea station?=STATION_NAME
            }
            return uri.build();
        }

        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /*
    public static abstract class TramStations implements BaseColumns {
        public static final String TABLE_TRAM_STATIONS = "Tram_Stations";

        public static final String STATION_ID = "_id";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String STATION_NAME = "station_name";
    }
    */

    // table Lines
    public static abstract class Lines implements BaseColumns {
        public static final String TABLE_LINES = "Lines";

        public static final String LINE_ID = "_id";
        public static final String LINE_NAME = "line_name";
        public static final String LINE_NUMBER = "line_number";
        public static final String START_STATION = "start_station";
        public static final String END_STATION = "end_station";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(LINES_PATH).build();

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI ,id);
        }
    }

    // table Line_Segments
    public static abstract class LineSegments implements BaseColumns {
        public static final String TABLE_LINE_SEGMENTS = "Line_Segments";

        public static final String SEGMENT_ID = "_id";
        public static final String ORIGIN_STATION_ID = "origin_id";
        public static final String DESTINATION_STATION_ID = "destination_id";
        public static final String SEGMENT_TIME = "segment_time";
        public static final String LINE_NUMBER = "line_number";
        public static final String DIRECTION = "direction";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(LINE_SEGMENTS_PATH).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + LINE_SEGMENTS_PATH;

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    // table Stations_Lines
    public static abstract class StationsLines implements BaseColumns {
        public static final String TABLE_STATIONS_LINES = "Stations_Lines";

        public static final String LINE_ID = "line_id";
        public static final String STATION_ID = "station_id";
        public static final String STATION_ORDER = "station_order";
        public static final String LINE_DIRECTION = "line_direction";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(STATIONS_LINES_PATH).build();
        public static final Uri CONTENT_URI_TRANSFERS = CONTENT_URI.buildUpon().appendPath("transfers").build();


        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    // table Trains
    public static abstract class Trains implements BaseColumns {
        public static final String TABLE_TRAINS = "Trains";

        public static final String TRAIN_ID = "_id";
        public static final String LINE_ID = "line_id";
        public static final String ORIGIN_ID = "origin_id";
        public static final String DESTINATION_ID = "destination_id";
        public static final String DEPART_ORIGIN_TIME = "depart_origin_time";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TRAINS_PATH).build();

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    // table DepartureTimes
    public static abstract class DepartureTimes implements BaseColumns {
        public static final String TABLE_DEPARTURE_TIMES = "Departure_Times";

        public static final String _ID = "_id";
        public static final String TRAIN_ID = "train_id";
        public static final String STATION_ID = "station_id";
        public static final String LINE_NUMBER = "line_number";
        public static final String DEPARTURE_TIME = "departure_time";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(DEPARTURE_TIMES_PATH).build();
        public static final Uri CONTENT_URI_HOURS = CONTENT_URI.buildUpon().appendPath("hours").build();

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
