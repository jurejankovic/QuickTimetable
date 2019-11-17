package hr.vsite.dipl.quicktimetable.downloader;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import hr.vsite.dipl.quicktimetable.R;
import hr.vsite.dipl.quicktimetable.TextProgressBar;
import hr.vsite.dipl.quicktimetable.constants.QTTconstants;

/**
 * Created by Jure on 16.7.2016..
 * This activity is used to check for and if necessary download the osm file from the internet
 *
 * Taken from http://stackoverflow.com/questions/3028306/download-a-file-with-android-and-showing-the-progress-in-a-progressdialog
 *
 */

public class OsmDownloadActivity extends AppCompatActivity {
    // declare the dialog as a member field of your activity
    TextProgressBar mProgressBar;
    private static final String DOWNLOAD_TAG = "OsmDownloadActivity";
    Button mDownloadButton;

    OsmDownloadTask mDownloadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_osm_download);
        setupActionBar();

//        mProgressBar = new TextProgressBar(OsmDownloadActivity.this);
        mProgressBar = (TextProgressBar) findViewById(R.id.progressBarDownload);
        mDownloadButton = (Button) findViewById(R.id.button_download_osm);
        mProgressBar.setVisibility(View.INVISIBLE);

        changeButtonToDownload();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // check if osm file exists
        checkForFileAndShowState();
    }

    private void checkForFileAndShowState() {
        OsmFileChecker osmFileChecker = new OsmFileChecker(OsmDownloadActivity.this);
        TextView description = (TextView) findViewById(R.id.tvOsmStatusDescription);
        Button downloadButton = (Button) findViewById(R.id.button_download_osm);

        if (osmFileChecker.checkFileExists()) {
            // in case the file does exist
            // ask user whether to re-download the osm file
            description.setText(getText(R.string.description_download_osm_action_found));
            downloadButton.setText(getString(R.string.button_download_osm));
        }
        else {
            // in case the file does not exist
            description.setText(getText(R.string.description_download_osm_action_not_found));
            downloadButton.setText(getString(R.string.button_download_osm));
        }
    }

    private void startDownload() {
        // execute this when the downloader must be fired
        mDownloadTask = new OsmDownloadTask(this);
        mDownloadTask.execute(QTTconstants.GOOGLE_DRIVE_LINK);

//        Button mDownloadButton = (Button) findViewById(R.id.button_download_osm);
        // change download button text
        // if task began execution
//        if (mDownloadTask.getStatus() == AsyncTask.Status.RUNNING) {
//            mDownloadButton.setText(getString(R.string.button_cancel_download_osm));
//        }
//        else
//        {
//            mDownloadButton.setText(getString(R.string.button_download_osm));
//        }
    }

    private void changeButtonToCancel() {
        // change download button text
        // if task began execution
        if (mDownloadTask.getStatus() == AsyncTask.Status.RUNNING) {
            mDownloadButton.setText(R.string.button_cancel_download_osm);
        }
        mDownloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(OsmDownloadActivity.this)
                            .setTitle(R.string.message_downl_cancel_prompt_title)
                            .setMessage(R.string.message_downl_cancel_prompt)
                            // setting dialog buttons
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with cancellation
                                    mDownloadTask.cancel(true);
                                    checkForFileAndShowState();
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                    checkForFileAndShowState();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        );
    }

    private void changeButtonToDownload() {
        // change download button text
        mDownloadButton.setText(getString(R.string.button_download_osm));
        mDownloadButton.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                  startDownload();
                                              }
                                          }
        );
    }

    public class OsmDownloadTask extends AsyncTask<String, Integer, String> {

        private PowerManager.WakeLock mWakeLock;
        private TextProgressBar mProgressBar;
        private Context mContext;

        public OsmDownloadTask(Context context) {
            this.mContext = context;
            mProgressBar = (TextProgressBar)findViewById(R.id.progressBarDownload);
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(DOWNLOAD_TAG, "doInBackground");


            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
//                output = new FileOutputStream(QTTconstants.OSM_FILE_PATH);
                output = openFileOutput(QTTconstants.OSM_FILE_NAME, MODE_PRIVATE);


                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressBar.setVisibility(View.VISIBLE);
            changeButtonToCancel();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressBar.setIndeterminate(false);
            mProgressBar.setMax(100);
            mProgressBar.setProgress(progress[0]);
            if (progress[0] == 20) {
                mProgressBar.setText(getString(R.string.progressbar_downloading));
            }
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressBar.setVisibility(View.INVISIBLE);
            if (result != null) {
                Toast.makeText(mContext, getString(R.string.message_download_error), Toast.LENGTH_LONG).show();
                Log.e(DOWNLOAD_TAG, result);
            }
            else {
                Toast.makeText(mContext, getString(R.string.message_download_success), Toast.LENGTH_SHORT).show();
            }
            if (mProgressBar.getProgress() == mProgressBar.getMax()) {
                changeButtonToDownload();
            }
        }

    }
}


