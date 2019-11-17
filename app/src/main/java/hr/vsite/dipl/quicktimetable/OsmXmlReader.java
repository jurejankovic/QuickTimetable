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
import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import hr.vsite.dipl.quicktimetable.constants.QTTconstants;
import hr.vsite.dipl.quicktimetable.database.DBContract;

/**
 * Created by Jure on 11.12.2016..
 */

public class OsmXmlReader implements Callable<Boolean> {
    private static final String TAG = "OsmXmlReader";
    private String fileName;

    private long nodeCount = 0;
    private long relationCount = 0;

    private List<TramStation> tramStations = new ArrayList<TramStation>(500);//500
    private List<TramLine> tramLines = new ArrayList<TramLine>(500);//500

    private ArrayList<ContentProviderOperation> batchOps = new ArrayList<ContentProviderOperation>();
    private ContentResolver contentResolver;

    public OsmXmlReader(String fileName, Context context) {
        this.fileName = context.getFilesDir().getAbsolutePath()
                + "/" + fileName;
        this.contentResolver = context.getContentResolver();
    }


    @Override
    public Boolean call() throws Exception {
        VTDGen vg = new VTDGen();

        // following methods return true if their op. completed without error
        readOsmNodes(vg);

        readOsmRelations(vg);

        matchStationsToLines();

        boolean writeOK = writeToDatabase();

        if(writeOK) {
            return true;
        }
        else {
            return false;
        }
    }

    private void readOsmNodes(VTDGen vg) {
        try {
            if (vg.parseFile(fileName, false)) {
                VTDNav vn = vg.getNav();
                AutoPilot osmNode = new AutoPilot(vn);
                osmNode.selectXPath("osm/node");
                osmNode.selectElement("node");

                while (osmNode.iterate()) {
                    boolean isTramStop = false;
                    String stationNameShort = null;
                    LatLng stationPosition;
                    long stationId = Long.parseLong(vn.toString(vn.getAttrVal("id")));
                    double nodeLat = Double.parseDouble(vn.toString(vn.getAttrVal("lat")));
                    double nodeLon = Double.parseDouble(vn.toString(vn.getAttrVal("lon")));

                    AutoPilot tag = new AutoPilot(vn);
                    tag.selectElement("tag");

                    String name = null;
                    String shortName = "";
                    while (tag.iterate()) {
                        if (vn.toString(vn.getAttrVal("k")).equals("name")) {
                            name = vn.toString(vn.getAttrVal("v"));
                        }
                        // short_name and alt_name are used later if there is no match for name
                        else if (vn.toString(vn.getAttrVal("k")).equals("short_name")) {
                            shortName = vn.toString(vn.getAttrVal("v"));
                        }
                        else if (vn.toString(vn.getAttrVal("k")).equals("alt_name") && shortName.equals("")) {
                            shortName = vn.toString(vn.getAttrVal("v"));
                        }
                        // checking if node is tram station
                        else if (vn.toString(vn.getAttrVal("v")).equals("tram_stop")) {
                            isTramStop = true;
                            stationNameShort = name;
                        }
                    }

                    if (isTramStop) {
                        stationPosition = new LatLng(nodeLat, nodeLon);
                        tramStations.add(new TramStation(stationId, stationNameShort, shortName, stationPosition));
                    }
                    nodeCount++;
                }
            }
        }
        catch (NavException | XPathParseException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    private void readOsmRelations(VTDGen vg) {
        try {
            if (vg.parseFile(fileName, false)) {
                VTDNav vn = vg.getNav();
                AutoPilot osmRelation = new AutoPilot(vn);
                osmRelation.selectXPath("osm/relation");
                osmRelation.selectElement("relation");

                while (osmRelation.iterate()) {
                    boolean isTramLine = false;
                    List<Long> membersList = new ArrayList<Long>(20);
                    long lineId = Long.parseLong(vn.toString(vn.getAttrVal("id")));
                    long lineNumber = 0;
                    long lineDirection;
                    String name = null;
                    String lineFrom = null;
                    String lineTo = null;

                    AutoPilot property = new AutoPilot(vn);
                    property.selectElement("*");
                    while (property.iterate()) {
                        if (vn.getAttrVal("k") != -1) {
                            if (vn.toString(vn.getAttrVal("k")).equals("name")) {
                                name = vn.toString(vn.getAttrVal("v"));
                            }
                            // checking if relation is tram route
                            else if (vn.toString(vn.getAttrVal("k")).equals("route") && vn.toString(vn.getAttrVal("v")).equals("tram")) {
                                isTramLine = true;
                            }
                            // <tag k='from' v='Borongaj' /> in OSM file
                            else if (vn.toString(vn.getAttrVal("k")).equals("from")) {
                                lineFrom = vn.toString(vn.getAttrVal("v"));
                            }
                            else if (vn.toString(vn.getAttrVal("k")).equals("to")) {
                                lineTo = vn.toString(vn.getAttrVal("v"));
                            }
                            // <tag k='ref' v='2' />
                            else if (vn.toString(vn.getAttrVal("k")).equals("ref")) {
                                lineNumber = Long.parseLong(vn.toString(vn.getAttrVal("v")));
                            }
                        }
                        // saving tram route member nodes (ref = id?)
                        else if (vn.getAttrVal("type") != -1) {
                            if (vn.toString(vn.getAttrVal("type")).equals("node")) {
                                membersList.add(Long.parseLong(vn.toString(vn.getAttrVal("ref"))));
                            }
                        }
                    }

                    if (isTramLine) {
                        // get direction of line using start station <tag k='from' v='Borongaj' />
                        switch ((int) lineNumber) {
                            // on the website zet.hr Line 1 starts on "Zapadni kolodvor"
                            // instead of "Borongaj" in OSM file, so line directions are reversed
                            case 1:
                                if (lineFrom.equals(QTTconstants.LINE_1_START_STATION)) {
                                    lineDirection = 1;
                                }
                                else
                                    lineDirection = 0;
                                break;
                            // other line directions are consistent
                            case 2:
                                if (lineFrom.equals(QTTconstants.LINE_2_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            case 3:
                                if (lineFrom.equals(QTTconstants.LINE_3_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            case 4:
                                if (lineFrom.equals(QTTconstants.LINE_4_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            case 5:
                                if (lineFrom.equals(QTTconstants.LINE_5_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            case 6:
                                if (lineFrom.equals(QTTconstants.LINE_6_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            case 7:
                                if (lineFrom.equals(QTTconstants.LINE_7_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            case 8:
                                if (lineFrom.equals(QTTconstants.LINE_8_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            case 9:
                                if (lineFrom.equals(QTTconstants.LINE_9_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            case 11:
                                if (lineFrom.equals(QTTconstants.LINE_11_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            case 12:
                                if (lineFrom.equals(QTTconstants.LINE_12_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            case 13:
                                if (lineFrom.equals(QTTconstants.LINE_13_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            case 14:
                                if (lineFrom.equals(QTTconstants.LINE_14_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            case 15:
                                if (lineFrom.equals(QTTconstants.LINE_15_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            case 17:
                                if (lineFrom.equals(QTTconstants.LINE_17_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            case 31:
                                if (lineFrom.equals(QTTconstants.LINE_31_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            case 32:
                                if (lineFrom.equals(QTTconstants.LINE_32_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            case 33:
                                if (lineFrom.equals(QTTconstants.LINE_33_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            case 34:
                                if (lineFrom.equals(QTTconstants.LINE_34_START_STATION)) {
                                    lineDirection = 0;
                                }
                                else
                                    lineDirection = 1;
                                break;
                            default:
                                lineDirection = -1;
                                Log.e(TAG, "Line direction not found");
                        }
                        tramLines.add(new TramLine(lineId, lineNumber, lineDirection, name, membersList, lineFrom, lineTo));
                        Log.i(TAG, "line " + lineNumber + " direction " + lineDirection + " added");
                    }
                    relationCount++;
                }
            }
        } catch (NavException | XPathParseException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }


    }

    /**
     * Adds list of stations(id-s) for every tram line.
     */
    private void matchStationsToLines() {
        for (TramLine tramLine : tramLines) {
            long direction = tramLine.getLineDirection();
            for (Long stationId: tramLine.getStationIds()) {
                batchOps.add(ContentProviderOperation.newInsert(DBContract.StationsLines.CONTENT_URI)
                        .withValue(DBContract.StationsLines.STATION_ID, stationId)
                        .withValue(DBContract.StationsLines.LINE_ID,tramLine.getId())
                        .withValue(DBContract.StationsLines.LINE_DIRECTION, direction)
                        .build());
            }
        }
    }

    /**
     * Method will try to update database. If update fails then it inserts data.
     * @return True if operation succeeded false if not
     * @throws ExecutionException
     */
    private boolean writeToDatabase() throws ExecutionException {
        boolean writeSuccessful = false;
        ContentProviderResult contentProviderResult[] = new ContentProviderResult[0];

        if(!tramStations.isEmpty() && !tramLines.isEmpty()) {
            // if number of tram stations read from OSM is the same as in database
            if (getCountFromDB(DBContract.Stations.CONTENT_URI) == tramStations.size()) {
                // update stations
                for (TramStation tramStation : tramStations) {
                    batchOps.add(ContentProviderOperation.newUpdate(DBContract.Stations.CONTENT_URI)
                            .withSelection(DBContract.Stations.STATION_ID + "=?", new String[]{String.valueOf(tramStation.getId())})
                            .withValue(DBContract.Stations.STATION_NAME, tramStation.getName())
                            .withValue(DBContract.Stations.STATION_NAME_SHORT, tramStation.getShortName())
                            .withValue(DBContract.Stations.LATITUDE, tramStation.getPosition().latitude)
                            .withValue(DBContract.Stations.LONGITUDE, tramStation.getPosition().longitude)
                            .build());
                }
            } else {
                // insert stations
                for (TramStation tramStation : tramStations) {
                    batchOps.add(ContentProviderOperation.newInsert(DBContract.Stations.CONTENT_URI)
                            .withValue(DBContract.Stations.STATION_ID, tramStation.getId())
                            .withValue(DBContract.Stations.STATION_NAME, tramStation.getName())
                            .withValue(DBContract.Stations.STATION_NAME_SHORT, tramStation.getShortName())
                            .withValue(DBContract.Stations.LATITUDE, tramStation.getPosition().latitude)
                            .withValue(DBContract.Stations.LONGITUDE, tramStation.getPosition().longitude)
                            .build());
                }
            }
            // if number of tram lines read from OSM is the same as in database
            if (getCountFromDB(DBContract.Lines.CONTENT_URI) == tramLines.size()) {
                // update lines
                for (TramLine tramLine : tramLines) {
                    batchOps.add(ContentProviderOperation.newUpdate(DBContract.Lines.CONTENT_URI)
                            .withSelection(DBContract.Lines.LINE_ID + "=?", new String[]{String.valueOf(tramLine.getId())})
                            .withValue(DBContract.Lines.LINE_NAME, tramLine.getName())
                            .withValue(DBContract.Lines.LINE_NUMBER, tramLine.getLineNumber())
                            .withValue(DBContract.Lines.START_STATION, tramLine.getStartStation())
                            .withValue(DBContract.Lines.END_STATION, tramLine.getEndStation())
                            .build());
                }
            } else {
                // insert lines
                for (TramLine tramLine : tramLines) {
                    batchOps.add(ContentProviderOperation.newInsert(DBContract.Lines.CONTENT_URI)
                            .withValue(DBContract.Lines.LINE_ID, tramLine.getId())
                            .withValue(DBContract.Lines.LINE_NAME, tramLine.getName())
                            .withValue(DBContract.Lines.LINE_NUMBER, tramLine.getLineNumber())
                            .withValue(DBContract.Lines.START_STATION, tramLine.getStartStation())
                            .withValue(DBContract.Lines.END_STATION, tramLine.getEndStation())
                            .build());
                }
            }
            try {
                contentProviderResult = contentResolver.applyBatch(DBContract.CONTENT_AUTHORITY, batchOps);


            } catch (RemoteException | OperationApplicationException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            } catch (SQLiteException sqle) {
//            throw new ExecutionException(sqle);
            }
        }

        if (contentProviderResult.length == batchOps.size() ){
//            if (contentProviderResult.length == (tramLines.size() + tramStations.size()) ){
            writeSuccessful = true;
        }
        // returning true or false depending on DB operations success
        return writeSuccessful;
    }

    private long getCountFromDB(Uri uri) {
        // check number of inserted
        long count = -1;
        Cursor countCursor = contentResolver.query(uri,
                new String[] {"count(*) AS count"},
                null,
                null,
                null);
        if (countCursor != null && countCursor.moveToFirst()) {
            count = countCursor.getInt(0);
            countCursor.close();
        }
        return count;
    }

}
