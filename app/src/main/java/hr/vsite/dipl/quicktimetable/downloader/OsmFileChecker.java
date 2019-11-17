package hr.vsite.dipl.quicktimetable.downloader;

import android.content.Context;
import android.util.Log;

import java.io.File;

import hr.vsite.dipl.quicktimetable.constants.QTTconstants;

/**
 * Created by Jure on 17.7.2016..
 */
public class OsmFileChecker {

    private static final String TAG = "OsmFileChecker";

    private String osmFileName = QTTconstants.OSM_FILE_NAME;
    private Context context;

    public OsmFileChecker(Context context) {
        this.context = context;
    }

    /**
     * Checks if the OSM file exists in the location specified
     * by the constant OSM_FILE_PATH in QTTconstants.java
     * @return true if file exists, false otherwise
     */
    public boolean checkFileExists() {
        boolean fileExists = false;

        String PATH = context.getFilesDir().getAbsolutePath()
                + "/" + osmFileName;
        File osmFile = new File(PATH);

        try {
            if (osmFile.exists()) {
                fileExists = true;
            }
            else {
                fileExists = false;
            }
        } catch (SecurityException se) {
            Log.e(TAG, "SecurityException when calling method .exists() of class File.");
            se.printStackTrace();
        }
        return fileExists;
    }

}
