package hr.vsite.dipl.quicktimetable;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class LocationService extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude;
    private double longitude;
    private boolean hasLocation = false;

//    private Intent intent;
//    static final String ACTION_GET_LOCATION = "hr.vsite.dipl.quicktimetable.GET_LOCATION";

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new QttLocationListener();
        Location location;

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            try {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            } catch (Exception e) {
                e.printStackTrace();
                return START_STICKY_COMPATIBILITY;
            }
        }
        else
            return START_STICKY_COMPATIBILITY;

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(locationListener);
        super.onDestroy();
    }

    public class QttLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Intent intent = new Intent("location-change");

            latitude = location.getLatitude();
            longitude = location.getLongitude();
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            //hasLocation = true;
            //sendBroadcast(intent);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(getApplicationContext(), provider + " status changed", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), provider + " enabled", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), provider + " disabled", Toast.LENGTH_LONG).show();
        }
    }
}


