package hr.vsite.dipl.quicktimetable;

import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import hr.vsite.dipl.quicktimetable.adapter.TabsPagerAdapter;
import hr.vsite.dipl.quicktimetable.constants.QTTconstants;

//import android.app.ActionBar;


public class MainActivity extends AppCompatActivity implements ActionBar.TabListener,  MainFragment.OnRouteChangedListener {
    private static final String TAG ="MainActivity";

    private ViewPager viewPager;
    private TabsPagerAdapter tabsPagerAdapter;
    private Uri CONTENT_URI;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private double latitude;
    private double longitude;
    private String nearestStationName;
    private long nearestStationID;

    private String currentTime;
    private boolean hasLocation;
    private List<Long> truePath;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // starting location service to get coordinates as needed
        startService(new Intent(this, LocationService.class));

        //region TabView
        viewPager = (ViewPager) findViewById(R.id.pager);
        final ActionBar actionBar = getSupportActionBar();
        tabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        getSupportActionBar().setSelectedNavigationItem(position);
                    }
                }
        );
        viewPager.setAdapter(tabsPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        String[] tabNames = new String[]{getString(R.string.main_fragment), getString(R.string.location_fragment)};
        for(String tabName : tabNames){
            actionBar.addTab(actionBar.newTab().setText(tabName).setTabListener(this));
        }
        //endregion

//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        locationListener = new QttLocationListener();

        CONTENT_URI = Uri.parse("content" + "://" + getPackageName() + "provider");

    }

    @Override
    protected void onResume(){
        super.onResume();
//        loadTramData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }


    protected void startLocationUpdates() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                Toast.makeText(getBaseContext(), LocationManager.GPS_PROVIDER + " enabled", Toast.LENGTH_LONG).show();
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//                Toast.makeText(getBaseContext(), LocationManager.NETWORK_PROVIDER + " enabled", Toast.LENGTH_LONG).show();
//                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            } else {
                // open Location Services
                ActivatePositioningDialogFragment activatePositioning = new ActivatePositioningDialogFragment();
                activatePositioning.show(this.getSupportFragmentManager(), "positioning");
            }

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            criteria.setPowerRequirement(Criteria.POWER_LOW);

            String bestProvider = locationManager.getBestProvider(criteria, true);
            locationManager.requestLocationUpdates(bestProvider, 0, 0, locationListener);
        }
        catch(IllegalArgumentException iae){
            Log.e("IAE", iae.getMessage());
            iae.printStackTrace();

        }
        catch(SecurityException se){
            Log.e("SE", se.getMessage());
            se.printStackTrace();
        }
        catch(Exception e){
            Log.e("E", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onRouteChanged(ArrayList<LatLng> name) {
        tabsPagerAdapter.onRouteChanged(name);
    }


}
