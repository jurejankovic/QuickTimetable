package hr.vsite.dipl.quicktimetable;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import hr.vsite.dipl.quicktimetable.constants.QTTconstants;

public class LocationFragment extends Fragment implements OnMapReadyCallback, MainFragment.OnRouteChangedListener {
    private static final String TAG = "LocationFragment";
    private static final String MAP_DATA_STATE = "mapData";

    MapView mapView;
    static final LatLng ZAGREB = new LatLng(45.8130204, 15.9765968);
    LatLng currentCoordinates = new LatLng(0,0);
    ArrayList<LatLng> stationsCoordinates = new ArrayList<LatLng>();
    String originName;

    public LocationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) view.findViewById(R.id.mapView);

        Bundle mapState = null;
        if(savedInstanceState != null) {
            mapState = savedInstanceState.getBundle(MAP_DATA_STATE);
        }
        mapView.onCreate(mapState);
        mapView.getMapAsync(this);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            // restore station coordinates after configuration change
            stationsCoordinates = savedInstanceState.getParcelableArrayList(QTTconstants.KEY_COORDINATES);
        }
    }

    @Override
    public void onRouteChanged(ArrayList<LatLng> coordinates) {
        if (coordinates != null) {
            stationsCoordinates = coordinates;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //map setup
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setAllGesturesEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.clear();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ZAGREB, 11.0f);
        googleMap.moveCamera(cameraUpdate);

        PolylineOptions options = new PolylineOptions();
        options.color(Color.BLUE);

        // builder for map bounds
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        // TODO add different colors for different lines
        // TODO add markers for changing lines
        for (LatLng coordinate : stationsCoordinates) {
            googleMap.addMarker(new MarkerOptions()
                    .position(coordinate)
                    // Maps Icons Collection https://mapicons.mapsmarker.com
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_tram_station_small))
//                    .anchor(0.5f, 0.5f)
            );
            options.add(coordinate);
            builder.include(coordinate);
        }
        if (!stationsCoordinates.isEmpty()) {
            LatLngBounds bounds = builder.build();
            int padding = 50;
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            googleMap.moveCamera(cameraUpdate);
            googleMap.addPolyline(options);
        }
    }

    @Override
    public void onDestroy() {
        //mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed())
        {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //mapView.invalidate();
        mapView.onResume();
        mapView.getMapAsync(this);
//        if (!getUserVisibleHint()) {
//            return;
//        }
        Intent intent = new Intent(getActivity(), LocationService.class);
//        final Bundle bundle = intent.getExtras();
        // Updates the location and zoom of the MapView
}


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        mapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save station coordinates state
        outState.putParcelableArrayList(QTTconstants.KEY_COORDINATES, stationsCoordinates);
    }


}
