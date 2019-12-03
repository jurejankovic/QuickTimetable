package hr.vsite.dipl.quicktimetable;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hr.vsite.dipl.quicktimetable.constants.QTTconstants;
import hr.vsite.dipl.quicktimetable.database.DBContract;
import hr.vsite.dipl.quicktimetable.database.DBHelper;
import hr.vsite.dipl.quicktimetable.downloader.OsmDownloadActivity;


public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MainFragment";
    // global variables used for loaders
    static final int LOAD_NEAREST_STATION = 1;
    static final int LOAD_STATION_ID = 3;
    static final int LOAD_LINE = 2;
    static final int LOAD_DEPARTURE_TIMES = 4;
    static final int LOAD_ALL_STATIONS = 5;
    static final int LOAD_LINE_SEGMENTS = 6;
    private static final int REQUEST_CHECK_SETTINGS = 33;

    List<TramStation> tramStations = new ArrayList<>();
    List<TramLineSegment> tramLineSegments = new ArrayList<>();

    ArrayAdapter<String> adapterStations;
    ListViewAdapter adapterListView;

    private TextProgressBar progressBar;
    private LocationManager locationManager;
    private LocationListener locationListener;
    LocationRequest locationRequest;

    private double latitude;
    private double longitude;
    private String nearestStationName;
    private long nearestStationID;
    private String currentTime;
    private boolean hasLocation;
    private List<Long> truePath;
    private int directionId;
    private List<Integer> usedLines;
    OnRouteChangedListener onRouteChangedListener;
    GoogleApiClient googleApiClient;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new QttLocationListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        Button button = (Button) rootView.findViewById(R.id.buttonSearch);
        button.setEnabled(true);

        progressBar = (TextProgressBar) rootView.findViewById(R.id.progressBar);

        rootView.findViewById(R.id.radioButtonAutomatic)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onRadioButtonClicked(v);
                    }
                });
        rootView.findViewById(R.id.radioButtonManual)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onRadioButtonClicked(v);
                    }
                });
        rootView.findViewById(R.id.buttonSearch)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buttonSearchOnClick(v);
                    }
                });
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText etOrigin = (EditText) getView().findViewById(R.id.editTextOrigin);
        etOrigin.setImeOptions(EditorInfo.IME_ACTION_DONE);
        EditText etDestination = (EditText) getView().findViewById(R.id.editTextDestination);
        etDestination.setImeOptions(EditorInfo.IME_ACTION_DONE);
        // hide List view header
        LinearLayout listHeader = (LinearLayout) getView().findViewById(R.id.listViewHeader);
        listHeader.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        buildGoogleApiClient();
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(QTTconstants.KEY_PROGRESS_IS_SHOWING)) {
                // restore progress bar state
                progressBar.setProgress(savedInstanceState.getInt(QTTconstants.KEY_PROGRESS_VALUE));
                progressBar.setText(savedInstanceState.getString(QTTconstants.KEY_PROGRESS_TEXT));
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.INVISIBLE);
            }
            // restore edit text origin state
            EditText etOrigin = (EditText) getView().findViewById(R.id.editTextOrigin);
            etOrigin.setEnabled(savedInstanceState.getBoolean(QTTconstants.KEY_ET_ORIGIN_ENABLED));
            if (savedInstanceState.getStringArrayList(QTTconstants.KEY_LIST_VIEW_VALUES) != null) {
                // restore list view
                ListView listView = (ListView) getView().findViewById(R.id.listViewDepartureTimes);
                listView.onRestoreInstanceState(savedInstanceState.getParcelable(QTTconstants.KEY_LIST_VIEW_STATE));
                ArrayList<LineDeparturePair> departures = savedInstanceState.getParcelableArrayList(QTTconstants.KEY_LIST_VIEW_VALUES);
                populateDeparturesList(departures);
            }
        }

    }

    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;
        if (context instanceof Activity) {
            activity = (Activity) context;
            try {
                onRouteChangedListener = (OnRouteChangedListener) activity;
            } catch (ClassCastException cce) {
                throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
            }
        }
    }

    @Override
    public void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public interface OnRouteChangedListener {
        public void onRouteChanged(ArrayList<LatLng> coordinates);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTramData();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (progressBar.isShown()) {
            // save progress bar state
            outState.putBoolean(QTTconstants.KEY_PROGRESS_IS_SHOWING, progressBar.isShown());
            outState.putInt(QTTconstants.KEY_PROGRESS_VALUE, progressBar.getProgress());
            outState.putString(QTTconstants.KEY_PROGRESS_TEXT, progressBar.getText());
        }
        // save edit text origin enabled/disabled state
        EditText etOrigin = (EditText) getView().findViewById(R.id.editTextOrigin);
        if (etOrigin.isEnabled()) {
            outState.putBoolean(QTTconstants.KEY_ET_ORIGIN_ENABLED, true);
        } else {
            outState.putBoolean(QTTconstants.KEY_ET_ORIGIN_ENABLED, false);
        }
        // save list view containing departure times
        ListView listView = (ListView) getView().findViewById(R.id.listViewDepartureTimes);
        if (listView.isShown() && adapterListView != null) {
            Parcelable listViewState = listView.onSaveInstanceState();
            outState.putParcelable(QTTconstants.KEY_LIST_VIEW_STATE, listViewState);
            outState.putParcelableArrayList(QTTconstants.KEY_LIST_VIEW_VALUES, adapterListView.getValues());
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        RadioButton rbAutomatic = (RadioButton) getView().findViewById(R.id.radioButtonAutomatic);
        RadioButton rbManual = (RadioButton) getView().findViewById(R.id.radioButtonManual);

        if (rbAutomatic.isChecked()) {
            locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(1000);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates states = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // start updating location
//                            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
                            Toast.makeText(getActivity(), R.string.message_auto_locate_on, Toast.LENGTH_SHORT).show();
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }

            });
            if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
            progressBar.setText(getString(R.string.loading_map_data));
            Log.i(TAG, "requestLocationUpdates");
        }
        if (rbManual.isChecked()) {
            // stop updating location
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
            Toast.makeText(getActivity(), R.string.message_auto_locate_off, Toast.LENGTH_SHORT).show();
        }
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        EditText etOrigin = (EditText) getView().findViewById(R.id.editTextOrigin);

        switch (view.getId()) {
            case R.id.radioButtonAutomatic:
                if (checked)
                    etOrigin.setEnabled(false);
                //hasLocation = false;
                if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
                break;
            case R.id.radioButtonManual:
                if (checked)
                    etOrigin.setEnabled(true);
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
                Toast.makeText(getActivity(), R.string.message_auto_locate_off, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void buttonSearchOnClick(View view) {
        if (tramStations.isEmpty()) {
            Toast.makeText(getActivity(), R.string.message_empty_database, Toast.LENGTH_LONG).show();
        } else {
            EditText etDestination = (EditText) getView().findViewById(R.id.editTextDestination);
            if (etDestination.getText().toString().equals("")) {
                Toast.makeText(getActivity(), R.string.no_destination, Toast.LENGTH_LONG).show();
            }
            String destinationName = etDestination.getText().toString();
            String originName = "";

            RadioButton rbAutomatic = (RadioButton) getView().findViewById(R.id.radioButtonAutomatic);
            RadioButton rbManual = (RadioButton) getView().findViewById(R.id.radioButtonManual);

            if (rbAutomatic.isChecked()) {
//                startLocationUpdates();
//                getLoaderManager().initLoader(LOAD_NEAREST_STATION, null, MainFragment.this);
//                LocationReceiver locationReceiver = new LocationReceiver();
//                LocalBroadcastManager.getInstance(getActivity()).registerReceiver(locationReceiver, new IntentFilter("location-change"));


                //new LocationControlTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                originName = nearestStationName;
            } else if (rbManual.isChecked()) {
//                AutoCompleteTextView or = (AutoCompleteTextView) getView().findViewById(R.id.editTextOrigin);
                EditText etOrigin = (EditText) getView().findViewById(R.id.editTextOrigin);
                if (etDestination.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), R.string.no_origin, Toast.LENGTH_LONG).show();
                }
                originName = etOrigin.getText().toString();
            }

            //check directionId
            List<Long> originIds = new ArrayList<>();
            List<Long> destinationIds = new ArrayList<>();
            // find possible tram station ids using their names
            for (TramStation tramStation : tramStations) {
                if (tramStation.getName().equalsIgnoreCase(originName)) {
                    originIds.add(tramStation.getId());
                } else if (tramStation.getName().equalsIgnoreCase(destinationName)) {
                    destinationIds.add(tramStation.getId());
                }
            }

            if (originIds.isEmpty()) {
                Toast.makeText(getActivity(), R.string.message_origin_invalid, Toast.LENGTH_SHORT).show();
            } else if (destinationIds.isEmpty()) {
                Toast.makeText(getActivity(), R.string.message_destination_invalid, Toast.LENGTH_SHORT).show();
            } else {
                TramNetwork tramNetwork = new TramNetwork(tramStations, tramLineSegments);
                RoutingTask routingTask = new RoutingTask(tramNetwork);
                // check all combinations and find optimal (fastest) route
                routingTask.execute(originIds, destinationIds);
            }
        }
    }

    private void loadTramData() {
        if (tramStations.isEmpty()) {
            getLoaderManager().restartLoader(LOAD_ALL_STATIONS, null, this);
            Log.d(TAG, "restart Loader LOAD_ALL_STATIONS");
        }
        if (tramLineSegments.isEmpty()) {
            getLoaderManager().restartLoader(LOAD_LINE_SEGMENTS, null, this);
            Log.d(TAG, "restart Loader LOAD_LINE_SEGMENTS");
        }
    }

    private void useLocation() {
        TextView textView = (TextView) getView().findViewById(R.id.nearestStation);
        textView.setText(nearestStationName);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_settings:
//                displaySettings();
//                return true;

            case R.id.action_download_osm:
                downloadOsm();
                return true;

            case R.id.action_read_data:
                readOsm();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displaySettings() {
        Intent intent = new Intent(getActivity(), SettingsActivity.class);
        this.startActivity(intent);
    }

    private void downloadOsm() {
        // switch to other activity to check for
        // and if necessary download the osm file from the internet
        Intent intent = new Intent(getActivity(), OsmDownloadActivity.class);
        this.startActivity(intent);
    }

    private void readOsm() {
        DBHelper dbHelper = new DBHelper(this.getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.forceUpgrade(db);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                DataProcessor dataProcessor = new DataProcessor(getActivity(), progressBar);
                dataProcessor.start();
            }
        });
    }

    private class RoutingTask extends AsyncTask<List<Long>, Integer, List<Long>> {

        private TramNetwork mTramNetwork;

        public RoutingTask(TramNetwork tramNetwork) {
//            super();
            mTramNetwork = tramNetwork;
        }

        @Override
        protected void onPreExecute() {
            progressBar = (TextProgressBar) getView().findViewById(R.id.progressBar);
            progressBar.setMax(100);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setText(getString(R.string.progressbar_routing));
        }

        @SuppressWarnings({"unchecked"})
        @Override
        protected List<Long> doInBackground(List<Long>... params) {
            // routing using Dijkstra's algorithm
            DijkstraRouter router = new DijkstraRouter(mTramNetwork);
            publishProgress(0);
            List<Long> originIds = params[0];
            List<Long> destinationIds = params[1];
            Log.d(TAG, "router start");
            Path path = router.route(originIds, destinationIds);
            List<Long> members = path.getMembersList();
            directionId = path.getDirection();
//            usedLines = path.getUsedLines();
            Log.d(TAG, "router stop");
            publishProgress(100);
            return members;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressBar.setProgress(progress[0]);
            if (progress[0] == 20) {
                progressBar.setText(getString(R.string.progressbar_routing));
            }
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(List<Long> members) {
            truePath = members;
            if (truePath != null) {
                // the origin station is the first on the path
                Long originId = truePath.get(0);
                DepartureTimeCalculator departTimeCalc = new DepartureTimeCalculator();
                Bundle args = new Bundle();
                args.putString(QTTconstants.KEY_ORIGIN_ID, String.valueOf(originId));
                args.putString(QTTconstants.KEY_DIRECTION_ID, String.valueOf(directionId));
//                args.putString(QTTconstants.KEY_LINE, usedLines.get(0));
                args.putString(QTTconstants.KEY_HOUR, departTimeCalc.closestHour());
                args.putString(QTTconstants.KEY_HOUR_NEXT, departTimeCalc.closestHourPlusOne());

                // load departure times for origin station
                getLoaderManager().restartLoader(LOAD_DEPARTURE_TIMES, args, MainFragment.this);
                // !!! during rotation fragment is not attached so exception is thrown

                progressBar.setText(getString(R.string.loading_map_data));
                ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
                // set coordinates of path that can be viewed in LocationFragment
                for (int step = 0; step < truePath.size(); step++) {
                    int i = 0;
                    for (TramStation station : tramStations) {
                        if (station.getId() == truePath.get(step)) {
                            coordinates.add(station.getPosition());
                            i++;
                        }
                    }
                }
                // send coordinates and used lines to other fragment
//                onRouteChangedListener.onRouteChanged(coordinates, usedLines);
                onRouteChangedListener.onRouteChanged(coordinates);
            }
            progressBar.setVisibility(View.INVISIBLE);
        }

    }

    private class LocationControlTask extends AsyncTask<Context, Void, Void>
    {
        protected void onPreExecute()
        {
            progressBar.setText(getString(R.string.message_getting_location));
        }

        protected Void doInBackground(Context... params)
        {
            // stop after location is obtained or after 60 seconds since begin time
            final Long beginTime = Calendar.getInstance().getTimeInMillis();
            while(!hasLocation && Calendar.getInstance().getTimeInMillis() - beginTime < 60000) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute(final Void unused)
        {
            // stop updating location
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
            Log.i(TAG, "removeLocationUpdates");
            // load nearest station using obtained location
            getLoaderManager().initLoader(LOAD_NEAREST_STATION, null, MainFragment.this);
            // TODO run search again after location is found
            // call function to repeat serch with newly found station

        }

    }

    // com.google.android.gms.location.LocationListener
    private class QttLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                // This needs to stop getting the location data and save the battery power.
//                locationManager.removeUpdates(locationListener);
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                hasLocation = true;
                Log.i(TAG, "got location " + latitude + " " + longitude);
                new LocationControlTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        switch (id) {
            case LOAD_NEAREST_STATION:
            {
                return new CursorLoader(getActivity(), DBContract.Stations.CONTENT_URI_NEAREST, null, null, null, null) {
                    // Loader for nearest station
                    // using Manhattan distance
                    @Override
                    public Cursor loadInBackground() {

                        ContentResolver cr = this.getContext().getContentResolver();
                        String[] columns = {
                                DBContract.Stations.STATION_NAME,
                                DBContract.Stations.LATITUDE,
                                DBContract.Stations.LONGITUDE,
                                DBContract.Stations.STATION_ID
                        };
                        String[] selectionArgs = {Double.toString(latitude), Double.toString(longitude)};
                        return cr.query(DBContract.Stations.CONTENT_URI_NEAREST, columns, null, selectionArgs, null);
                    }
                };
            }

            case LOAD_DEPARTURE_TIMES:
            {
                updateProgressBar(getString(R.string.loading_departures), 50);
                return new CursorLoader(getActivity(), DBContract.DepartureTimes.CONTENT_URI_HOURS, null, null, null, null) {
                    // Loader for departure times
                    @Override
                    public Cursor loadInBackground() {
                        final String originId = args.getString(QTTconstants.KEY_ORIGIN_ID);
                        final String directionId = args.getString(QTTconstants.KEY_DIRECTION_ID);
                        final String hour = args.getString(QTTconstants.KEY_HOUR);
                        final String nextHour = args.getString(QTTconstants.KEY_HOUR_NEXT);

                        ContentResolver cr = this.getContext().getContentResolver();
                        String[] columns = {
                                DBContract.DepartureTimes.STATION_ID,
                                DBContract.DepartureTimes.DEPARTURE_TIME,
                                DBContract.DepartureTimes.TABLE_DEPARTURE_TIMES + "." + DBContract.LineSegments.LINE_NUMBER,
                        };
                        String selectionArgs[] = {
                                originId,
                                hour + "%",
                                nextHour + "%"
                        };

                        return cr.query(DBContract.DepartureTimes.CONTENT_URI_HOURS, columns, null, selectionArgs, null);
                    }
                };
            }
            case LOAD_ALL_STATIONS:
            {
                updateProgressBar(getString(R.string.loading_stations), 25);
                return new CursorLoader(getActivity(), DBContract.Stations.CONTENT_URI, null, null, null, null) {
                    // Loads all stations for autocomplete
                    @Override
                    public Cursor loadInBackground() {

                        ContentResolver cr = this.getContext().getContentResolver();
                        String[] columns = {
                                DBContract.Stations.STATION_ID,
                                DBContract.Stations.STATION_NAME,
                                DBContract.Stations.STATION_NAME_SHORT,
                                DBContract.Stations.LATITUDE,
                                DBContract.Stations.LONGITUDE
                        };

                        return cr.query(DBContract.Stations.CONTENT_URI_ALL, columns, null, null, null);
                    }
                };

            }
            case LOAD_LINE_SEGMENTS:
            {
                updateProgressBar(getString(R.string.loading_line_segments), 25);
                return new CursorLoader(getActivity(), DBContract.LineSegments.CONTENT_URI, null, null, null, null) {
                    // iterate tram lines one by one
                    // getting a cursor for each line
                    @Override
                    public Cursor loadInBackground() {

                        ContentResolver cr = this.getContext().getContentResolver();
                        return cr.query(DBContract.LineSegments.CONTENT_URI, null, null, null, null, null);
                    }
                };
            }
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case LOAD_NEAREST_STATION: {
//                if (data.moveToFirst()) {
//                    nearestStationName = data.getString(data.getColumnIndex(DBContract.Stations.STATION_NAME));
//                    while (data.moveToNext()) {
//                        stationCoordinates.add(new LatLng(data.getDouble(1), data.getDouble(2)));
//                        nearestStationID = data.getLong(3);
//                    }
//                }
                List<LatLng> stationCoordinates = new ArrayList<LatLng>();
                float results[] = new float[1];
                float distance = Float.MAX_VALUE;
                int position = 0;
                // move cursor to starting position for subsequent loading
                data.moveToPosition(-1);
                while (data.moveToNext()) {
//                    stationCoordinates.add(new LatLng(data.getDouble(1), data.getDouble(2)));
                    Location.distanceBetween(latitude, longitude, data.getDouble(1), data.getDouble(2), results);
                    if (results[0] < distance) {
                        distance = results[0];
                        position = data.getPosition();
                    }
                }
                if (data.moveToPosition(position)) {
                    nearestStationName = data.getString(data.getColumnIndex(DBContract.Stations.STATION_NAME));
                    nearestStationID = data.getLong(data.getColumnIndex(DBContract.Stations.STATION_ID));
                }
                useLocation();
            }
            break;
            case LOAD_DEPARTURE_TIMES: {
                ArrayList<LineDeparturePair> potentialDepartures = new ArrayList<LineDeparturePair>();
                // add all potential departures to list
                while (data.moveToNext()) {
                    potentialDepartures.add(new LineDeparturePair(
                                    data.getString(data.getColumnIndex(DBContract.LineSegments.LINE_NUMBER)),
                                    data.getString(data.getColumnIndex(DBContract.DepartureTimes.DEPARTURE_TIME))
                            )
                    );
                }
                DepartureTimeCalculator departTimeCalculator = new DepartureTimeCalculator();
                // method nextDepartures() returns departures that are after current time
                ArrayList<LineDeparturePair> departures = departTimeCalculator.nextDepartures(potentialDepartures);
                populateDeparturesList(departures);
                updateProgressBar(getString(R.string.loaded_departures), 50);
            }
            break;

            case LOAD_ALL_STATIONS: {
                if (tramStations.isEmpty()) {
                    String allStations[] = new String[data.getCount()];
                    while (data.moveToNext()) {
                        tramStations.add(new TramStation(
                                data.getLong(data.getColumnIndex(DBContract.Stations.STATION_ID)),
                                data.getString(data.getColumnIndex(DBContract.Stations.STATION_NAME)),
                                data.getString(data.getColumnIndex(DBContract.Stations.STATION_NAME_SHORT)),
                                new LatLng(data.getDouble(data.getColumnIndex(DBContract.Stations.LATITUDE))
                                        , data.getDouble(data.getColumnIndex(DBContract.Stations.LONGITUDE))
                                )
                        ));
//                        Log.d(TAG, "tram station " + data.getString(data.getColumnIndex(DBContract.Stations.STATION_NAME)) + " added");
                    }
                    if (adapterStations == null) {
                        Set<String> stringSet = new LinkedHashSet<>();
                        for (TramStation tramStation : tramStations) {
                            // using Set to avoid duplicate station names
                            stringSet.add(tramStation.getName());
                        }
                        allStations = stringSet.toArray(new String[0]);

                        adapterStations = new ArrayAdapter<String>(getActivity(), R.layout.item_station_dropdown, allStations);
                        AutoCompleteTextView tvOrigin = (AutoCompleteTextView) getView().findViewById(R.id.editTextOrigin);
                        tvOrigin.setAdapter(adapterStations);
                        adapterStations = new ArrayAdapter<String>(getActivity(), R.layout.item_station_dropdown, allStations);
                        AutoCompleteTextView tvDestination = (AutoCompleteTextView) getView().findViewById(R.id.editTextDestination);
                        tvDestination.setAdapter(adapterStations);
                    }
                }
                if (adapterStations.isEmpty()) {
                    updateProgressBar("Empty adapter", 0);
                }
                else {
                    updateProgressBar(getString(R.string.loaded_stations), 25);
                }
            }

            break;

            case LOAD_LINE_SEGMENTS: {
                while (data.moveToNext()) {
                    // find origin and destination station for each segment
                    tramLineSegments.add(new TramLineSegment(
                            data.getLong(data.getColumnIndex(DBContract.LineSegments.ORIGIN_STATION_ID)),
                            data.getLong(data.getColumnIndex(DBContract.LineSegments.DESTINATION_STATION_ID)),
                            data.getInt(data.getColumnIndex(DBContract.LineSegments.SEGMENT_TIME)),
                            data.getLong(data.getColumnIndex(DBContract.LineSegments.LINE_NUMBER)),
                            data.getInt(data.getColumnIndex(DBContract.LineSegments.DIRECTION))
                    ));
                }
            }
            if (tramLineSegments.isEmpty()) {
                updateProgressBar("Empty tram lines!", 0);
            }
            else {
                updateProgressBar(getString(R.string.loaded_line_segments), 25);
            }
            break;
        }
    }

    private void populateDeparturesList(ArrayList<LineDeparturePair> departures) {
        adapterListView = new ListViewAdapter(this.getActivity().getApplicationContext(), departures);
        ListView lvDepartureTimes = (ListView) getView().findViewById(R.id.listViewDepartureTimes);
        LinearLayout listHeader = (LinearLayout) getView().findViewById(R.id.listViewHeader);
        // show List view header if hidden
        if (!listHeader.isShown()) {
            listHeader.setVisibility(View.VISIBLE);
        }
        lvDepartureTimes.setAdapter(adapterListView);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
//        switch (loader.getId()) {
//            case LOAD_ALL_STATIONS: {
//                adapterStations.notifyDataSetChanged();
//            }
//            break;
//            case LOAD_LINE_SEGMENTS: {
//                adapterStations.notifyDataSetChanged();
//            }
//            break;
//        }
    }

    private void updateProgressBar(String message, int increment) {
        progressBar.setText(message);
        if (progressBar.getProgress() < progressBar.getMax()) {
            progressBar.incrementProgressBy(increment);
            progressBar.setVisibility(View.VISIBLE);
        }
        // TODO hide progress bar after completion
    }

}
