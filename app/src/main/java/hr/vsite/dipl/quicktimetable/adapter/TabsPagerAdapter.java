package hr.vsite.dipl.quicktimetable.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import hr.vsite.dipl.quicktimetable.LocationFragment;
import hr.vsite.dipl.quicktimetable.MainFragment;


public class TabsPagerAdapter extends FragmentPagerAdapter implements MainFragment.OnRouteChangedListener{
    private static final String TAG = "TabsPagerAdapter";
    private MainFragment mainFragment;
    private LocationFragment locationFragment;
    public static final int FRAGMENT_MAIN = 0;
    public static final int FRAGMENT_LOCATION = 1;

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MainFragment();
            case 1:
                return new LocationFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        switch (position) {
            case 0:
                mainFragment = (MainFragment)createdFragment;
                break;
            case 1:
                locationFragment = (LocationFragment)createdFragment;
                break;
        }
        return createdFragment;
    }

    @Override
    public void onRouteChanged(ArrayList<LatLng> coordinates) {
        if (locationFragment != null) {
            locationFragment.onRouteChanged(coordinates);
        }
        else {
            Log.d(TAG, "locationFragment was null");
        }
    }
}
