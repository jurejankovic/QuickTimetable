package hr.vsite.dipl.quicktimetable.constants;

/**
 * Created by Jure on 16.7.2016..
 */
public final class QTTconstants {
    // path to save osm file on sdcard
    public static final String OSM_FILE_PATH = "/storage/emulated/0/QTT/map.osm";
    public static final String OSM_FILE_PATH_Andy= "/storage/sdcard0/Shared/Andy/map.osm";
    public static final String OSM_FILE_NAME = "map.osm";
    public static final String OSM_FILE_DIRECTORY = "QTT";
    // link to Google Drive where the downloadable osm file is stored
    public static final String GOOGLE_DRIVE_LINK = "https://docs.google.com/uc?export=download&id=0B89c5BqMPL_lSDRieDA4UzViVW8";

    public static final String DATABASE_NAME = "QTT_database.db";

    // Bundle keys
    public static final String KEY_ORIGIN_ID =  "originId";
    public static final String KEY_DIRECTION_ID =  "directionId";
    public static final String KEY_LINE = "line";
    public static final String KEY_HOUR = "hour";
    public static final String KEY_HOUR_NEXT = "hourNext";
    public static final String KEY_COORDINATES = "coordinates";
    public static final String KEY_ORIGIN_NAME = "originName";
    public static final String KEY_PROGRESS_VALUE = "progressValue" ;
    public static final String KEY_PROGRESS_TEXT = "progressText";
    public static final String KEY_PROGRESS_IS_SHOWING = "progressIsShowing";
    public static final String KEY_ET_ORIGIN_ENABLED = "etOriginEnabled";
    public static final String KEY_LIST_VIEW_STATE = "listViewState";
    public static final String KEY_LIST_VIEW_VALUES = "listViewValues";

    public static final String LINK_FOR_LINE_N = "http://zet.hr/default.aspx?id=330&route_id=";
    public static final String DAY_TRAM_LINES_URL = "http://www.zet.hr/default.aspx?id=291";

    // day lines start stations
    public static final String LINE_1_START_STATION = "Borongaj";
    public static final String LINE_2_START_STATION = "Črnomerec";
    public static final String LINE_3_START_STATION = "Ljubljanica";
    public static final String LINE_4_START_STATION = "Savski most";
    public static final String LINE_5_START_STATION = "Prečko";
    public static final String LINE_6_START_STATION = "Črnomerec";
    public static final String LINE_7_START_STATION = "Savski most";
    public static final String LINE_8_START_STATION = "Mihaljevac";
    public static final String LINE_9_START_STATION = "Ljubljanica";
    public static final String LINE_11_START_STATION = "Črnomerec";
    public static final String LINE_12_START_STATION = "Ljubljanica";
    public static final String LINE_13_START_STATION = "Žitnjak";
    public static final String LINE_14_START_STATION = "Mihaljevac";
    public static final String LINE_15_START_STATION = "Mihaljevac";
    public static final String LINE_17_START_STATION = "Prečko";
    // night lines start stations
    public static final String LINE_31_START_STATION = "Črnomerec";
    public static final String LINE_32_START_STATION = "Prečko";
    public static final String LINE_33_START_STATION = "Dolje";
    public static final String LINE_34_START_STATION = "Ljubljanica";

    public static final int DEPARTURES_LIST_LIMIT = 8;
    public static final String LINE_COLUMN = "line";
    public static final String TIME_COLUMN = "time";
}