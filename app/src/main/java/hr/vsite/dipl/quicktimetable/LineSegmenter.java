package hr.vsite.dipl.quicktimetable;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

import hr.vsite.dipl.quicktimetable.constants.QTTconstants;
import hr.vsite.dipl.quicktimetable.database.DBContract;

/**
 * Created by Jure on 9.12.2016..
 */

public class LineSegmenter implements Callable<AbstractMap.SimpleImmutableEntry<Long, Boolean>> {
    private static final String TAG = "LineSegmenter";
    private static final int TIMEOUT = 600 * 1000;

    // database operations
    private ContentResolver contentResolver;
    private List<TramLineSegment> tramLineSegments = new ArrayList<TramLineSegment>(200);
    private List<StationDepartureTime> departureTimes = new ArrayList<StationDepartureTime>(5000);
//    private List<TramLineSegment> tramLineSegments = new CopyOnWriteArrayList<TramLineSegment>();
//    private List<StationDepartureTime> departureTimes = new CopyOnWriteArrayList<StationDepartureTime>();
    private ArrayList<ContentProviderOperation> batchOps = new ArrayList<ContentProviderOperation>();

    private List<LineEndpoint> lineEndpoints = new ArrayList<>(40);

    private String lineLink;
    private long lineId;
    private List<Long> segmentIds;
    private List<Long> departureIds;

    public LineSegmenter(String lineLink, List<LineEndpoint> lineEndpoints, Context context) {
        this.lineLink = lineLink;
        this.lineEndpoints = lineEndpoints;
        this.contentResolver = context.getContentResolver();
        segmentIds = new ArrayList<>();
        departureIds = new ArrayList<>();
    }

    @Override
    public AbstractMap.SimpleImmutableEntry<Long, Boolean> call() throws Exception {
        // always true!?
        if(segmentLines()) {
            // return line number and result of database write operation
            return new AbstractMap.SimpleImmutableEntry<Long, Boolean>(lineId, writeToDatabase());
        }
        else {
            // return line number and false
            return new AbstractMap.SimpleImmutableEntry<Long, Boolean>(lineId, false);

        }
    }

    boolean segmentLines() {
        // status flag indicating if the segmentation was successful
        boolean status = false;

        Document linesDocument = null;
        // http://zet.hr/default.aspx?id=330&route_id=[lineNumber]
        long lineNumber;
        List<TramStationPair> stationPair = new ArrayList<>();
//        Map<String, Long> nameId = new HashMap<>();

        String[] array = lineLink.split("route_id=|&");
//                        lineNumber = Long.valueOf(array[2]);
        String lineIdText = array[2];
        lineNumber = Long.parseLong(lineIdText);
        this.lineId = lineNumber;
        Log.i("thread", "| " + lineNumber);
        try {
            linesDocument = Jsoup.connect(lineLink)
                    .timeout(TIMEOUT)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (linesDocument != null) {
            List<String> trainTripTimes = new ArrayList<String>();

            Elements rows = linesDocument.select("div.pageContent").select("tr tr");
            for (Element row : rows) {
                Elements e = row.select("td");
                // get the link and the text (displayed as departure time)
                String link = e.get(0).select("a").attr("abs:href");
                String time = e.get(0).text();
                // start and end station for the line
                String startStation = e.get(1).text();
                String endStation = e.get(3).text();
                // if endpoint stations match add the link to the list
                if (lineEndpoints.contains(new LineEndpoint(lineIdText, startStation, endStation))
                        && isValidTime(time)) {
                    trainTripTimes.add(link); // link ""
                }
            }

            // getting lineLinks for individual lines
/*                for (Element link : trainTripLinks) {
                if (link.attr("abs:href").startsWith("http://zet.hr/default.aspx?id=331&route_id=")
                        && isValidTime(link.text())) {
                    trainTripTimes.add(link.attr("abs:href"));
                }
            }*/
            // iterating trough individual train trips using their links to get times
            Document trainTripDocument = null;
            for (String trainTripTime : trainTripTimes) {
                // get the direction (0 or 1) from the url
                String lineDirection = trainTripTime.split("&direction_id=")[1];
                int directionId = Integer.valueOf(lineDirection);
                try {
                    trainTripDocument = Jsoup.connect(trainTripTime)
                            .timeout(TIMEOUT)
                            .get();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (trainTripDocument != null) {
                    Elements trainTrips = trainTripDocument.select("ol");
                    Elements listItems = trainTrips.select("li");

                    String previousStationTime = "0";
                    String previousStationName = "name";
                    String stationTime;
                    String stationName;


                    for (int i = 0; i < listItems.size(); i++) {
                        // iterating trough strings and checking if they are departure times
                        // departure times are lineLinks to details for individual trains on the
                        // selected line
                        String item = listItems.get(i).text();
                        String[] splitItem = item.split("-");
                        stationTime = splitItem[0].trim();
                        stationName = splitItem[1].trim();
                        long previousStationId = 0;
                        String stationAName = null;
                        String stationBName = null;

                        // when the station name change occurs
                        if (!previousStationName.equals(stationName)) {
                            //the current names are saved
                            if (!previousStationName.equals("name")) {
                                stationAName = previousStationName;
                                stationBName = stationName;
                            }
                            previousStationName = stationName;
                        }

                        int time = 0;
                        // check if station time has changed
                        if (!previousStationTime.equals(stationTime)) {
                            if (!previousStationTime.equals("0")) {
                                try {
                                    if (isValidTime(stationTime)) {
                                        time = timeDiffInSeconds(stationTime, previousStationTime);
                                        if (stationAName != null || stationBName != null) {
                                            // id for previous station
                                            long stationAId = findIdForStationName(stationAName, lineIdText, lineDirection);
                                            // id for current station
                                            long stationBId = findIdForStationName(stationBName, lineIdText, lineDirection);
                                            // TODO departure times not collected for stations that have no station before them (Dubec, Borongaj...)
//                                            nameId.put(stationBName, stationBId);
                                            previousStationId = stationBId;
                                            // avoid duplicate segments
                                            TramLineSegment segment = new TramLineSegment(stationAId, stationBId, time, lineNumber, directionId);
//                                            Log.i(TAG, lineNumber + " " + stationAId + " " + stationBId + " " + time + " " + directionId + " " + stationTime);
                                            if (!tramLineSegments.contains(segment)) {
                                                tramLineSegments.add(segment);
                                                Log.i(TAG, "Added " + lineNumber + " " + stationAId + " " + stationBId + " " + time + " " + directionId);
                                            }
                                        }
//                                        long stationId = nameId.get(stationName);
                                        long stationId = previousStationId;
                                        // adding departure times for each station that is identified by id
//                                        if (lineNumber == 1 || lineNumber == 9 || lineNumber == 17) {
                                            if (stationId == 1701766432 || stationId == 1949287459 || stationId == 2065046862) {
                                                Log.e(TAG, "Departure " + stationId + " " + lineNumber + " " + stationTime);
                                            }
//                                        }
                                        departureTimes.add(new StationDepartureTime(stationId, lineNumber, stationTime));
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            previousStationTime = stationTime;
                        }

                    }
                    status = true;

                }

            }
        }
        return status;
    }

    /**
     * Finds the station object for a given station name
     * @param name the name of the station
     * @return the unique id of the station
     */
    private long findIdForStationName(String name, String lineNumber, String lineDirection) {
        long stationId = 0;

        String[] selectionArgs = new String[]{
                name,
                lineNumber,
                lineDirection
        };

        Cursor c = contentResolver.query(DBContract.Stations.CONTENT_URI_WITH_LINE,
                null,
                null,
                selectionArgs,
                null);

        if (c != null) {
            // in case that there are no direct station name matches using station short name
            try {
                if (c.getCount() <= 0) {
                    Cursor cs = contentResolver.query(DBContract.Stations.CONTENT_URI_WITH_LINE_SHORT,
                            null,
                            null,
                            selectionArgs,
                            null);
                    if (cs != null) {
                        try {
                            if (cs.moveToFirst()) {
                                stationId = cs.getLong(cs.getColumnIndex(DBContract.Stations.STATION_ID));
                            }
                        } finally {
                            cs.close();
                        }
                    }
                } else if (c.moveToFirst()) {
                    stationId = c.getLong(c.getColumnIndex(DBContract.Stations.STATION_ID));
                }
            } finally {
                c.close();
            }
        }

        // test
/*        switch (name) {
            case "Borongaj":
                Log.e(TAG, "Borongaj " + stationId);
                break;
            case "Dubec":
                Log.e(TAG, "Dubec " + stationId);
                break;
            case "Dubrava":
                Log.e(TAG, "Dubrava " + stationId);
                break;
            case "Savišće":
                Log.e(TAG, "Savišće " + stationId);
                break;
            default:
                break;
        }*/

        return stationId;
    }

    /**
     * Returns the absolute value difference between two times in seconds
     * for time format kk:mm:ss
     * @param time1 departure time for station 1
     * @param time2 departure time for station 2
     * @return difference in seconds
     * @throws ParseException
     */
    private int timeDiffInSeconds(String time1, String time2) throws ParseException {
        int seconds = 0;
        if (isValidTime(time1) && isValidTime(time2)) {
            // using Joda time to get the number of seconds between
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("HH:mm:ss");
            DateTime t1 = DateTime.parse(time1, dateTimeFormatter);
            DateTime t2 = DateTime.parse(time2, dateTimeFormatter);
            Seconds sec = Seconds.secondsBetween(t1, t2);
            seconds = sec.getSeconds();
            // 86 400 seconds in a day
            int secondsInDay = 86400;
            // special case for time around midnight (between days)
            if ( (t1.getHourOfDay() == 0 && t2.getHourOfDay() == 23)
                    || (t1.getHourOfDay() == 23 && t2.getHourOfDay() == 0) ) {
                seconds = secondsInDay - seconds;
                Log.d(TAG, "23 -> 00 " + seconds);
            }

        }
        return Math.abs(seconds);
    }

    /**
     * Tests if the string is a time format.
     * @param testString
     * @return true if the given string matches the HH:MM:SS time format
     */
    private boolean isValidTime(String testString) {
        DateFormat df = new SimpleDateFormat( "kk':'mm':'ss", Locale.getDefault() );
        try {
            Date date = df.parse(testString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
//            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            // only if hour is 05 h , 08 h, 12 h
//            return hour == QTTconstants.INTERVAL_HOUR_5 ||
//                    hour == QTTconstants.INTERVAL_HOUR_8 ||
//                    hour == QTTconstants.INTERVAL_HOUR_12;
        }
        catch (ParseException ex) {
            return false;
        }
        // return true if no exception is thrown
        return true;
    }


    private boolean writeToDatabase() {
        boolean writeSuccessful = false;
        ContentProviderResult contentProviderResult[] = new ContentProviderResult[0];

        if(!tramLineSegments.isEmpty()) {
            // insert tram line segments
            for (TramLineSegment tramLineSegment : tramLineSegments) {
                batchOps.add(ContentProviderOperation.newInsert(DBContract.LineSegments.CONTENT_URI)
                        .withValue(DBContract.LineSegments.ORIGIN_STATION_ID, tramLineSegment.getOriginId())
                        .withValue(DBContract.LineSegments.DESTINATION_STATION_ID, tramLineSegment.getDestinationId())
                        .withValue(DBContract.LineSegments.SEGMENT_TIME, tramLineSegment.getSegmentTime())
                        .withValue(DBContract.LineSegments.LINE_NUMBER, tramLineSegment.getLineNumber())
                        .withValue(DBContract.LineSegments.DIRECTION, tramLineSegment.getDirection())
                        .build());
            }
            // insert departure times
            for (StationDepartureTime departureTime : departureTimes) {
                batchOps.add(ContentProviderOperation.newInsert(DBContract.DepartureTimes.CONTENT_URI)
                        .withValue(DBContract.DepartureTimes.STATION_ID, departureTime.getStationId())
                        .withValue(DBContract.DepartureTimes.LINE_NUMBER, departureTime.getLineNumber())
                        .withValue(DBContract.DepartureTimes.DEPARTURE_TIME, departureTime.getTime())
                        .build());
            }
        }

        try {
            if (!batchOps.isEmpty()) {
                contentProviderResult = contentResolver.applyBatch(DBContract.CONTENT_AUTHORITY, batchOps);
            }
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        catch (SQLiteException se) {
            Log.e(TAG, se.getMessage());
        }
        if (contentProviderResult.length == batchOps.size()) {
            writeSuccessful = true;
            Log.i(TAG, "Wrote " + contentProviderResult.length + " from " + lineLink);
        }
        return writeSuccessful;
    }

    private long getCountFromDB(Uri uri, String lineNumber) {
        // check number of inserted
        long count = -1;
        Cursor countCursor = contentResolver.query(uri,
                new String[] {"count(*) AS count"},
                "line_number = ?",
                new String[] {lineNumber},
                null);
        if (countCursor != null && countCursor.moveToFirst()) {
            count = countCursor.getInt(0);
            countCursor.close();
        }
        return count;
    }
}
