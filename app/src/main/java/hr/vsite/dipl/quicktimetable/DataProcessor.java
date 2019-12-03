package hr.vsite.dipl.quicktimetable;

import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import hr.vsite.dipl.quicktimetable.constants.QTTconstants;
import hr.vsite.dipl.quicktimetable.downloader.OsmFileChecker;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by Jure on 22.1.2017..
 * This class controls the execution of multiple threads that deal with downloading and processing
 * timetable information from the ZET website.
 */

public class DataProcessor {
    private static final String TAG = "DataProcessor";
    private Context context;
    // database operations
    private ContentResolver contentResolver;

    private ArrayList<ContentProviderOperation> batchOps = new ArrayList<ContentProviderOperation>();
    private TextProgressBar progressBar;
    Handler mainThreadHandler;

    int resubmitAttempt = 0;

    // threading
    private final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executor = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
    private final CompletionService<AbstractMap.SimpleImmutableEntry<Long, Boolean>> completionServiceLS =
            new ExecutorCompletionService<AbstractMap.SimpleImmutableEntry<Long, Boolean>>(executor);
    private final CompletionService<Boolean> completionServiceB =
            new ExecutorCompletionService<Boolean>(executor);
    private final CompletionService<List<LineEndpoint>> completionServiceLE = new ExecutorCompletionService<List<LineEndpoint>>(executor);

    private List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
    Collection<Callable<Boolean>> list = new ArrayList<Callable<Boolean>>();
    private List<LineEndpoint> lineEndpoints = new ArrayList<>(40);

    public DataProcessor(Context context, TextProgressBar progressBar) {
        this.context = context;
//        this.connectionStateListener = connectionStateListener;

        contentResolver = context.getContentResolver();
        mainThreadHandler = new Handler(Looper.getMainLooper());

        this.progressBar = progressBar;
    }

    public interface ConnectionState {
        public void isConnected(boolean result);
    }
    private DataProcessor.ConnectionState connectionStateListener;

    public void start() {
        publishProgress(-100, "");

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        // the links (URLs) leading to pages for individual lines
        List<String> linesLinks = new ArrayList<String>();
        // check if network is available
        if (networkInfo != null && networkInfo.isConnected()) {
            // check if OSM file has been downloaded
            OsmFileChecker osmFileChecker = new OsmFileChecker(context);
            if (osmFileChecker.checkFileExists()) {
                PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "DataProcessorWakelock");
                wakeLock.acquire();
                Log.i(TAG, "Wakelock acquire");

                Boolean result = false;
                // submitting OsmXmlReader that will read the downloaded OSM file
                completionServiceB.submit(new OsmXmlReader(QTTconstants.OSM_FILE_NAME, context));
                publishProgress(10, context.getString(R.string.progressbar_reading_osm));
                Log.i(TAG, "Submitting OsmXmlReader thread");
                try {
                    Future<Boolean> osmReader = completionServiceB.take();
                    result = osmReader.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.getCause();
//                displayDatabaseDeletePrompt();
                }

                // if OsmXmlReader finished successfully next stage is initiated
                if (result) {
                    completionServiceLE.submit(new LineEndpointFinder(context));
                    publishProgress(10, "Lines");
                    Log.i(TAG, "Submitting LineEndpoints thread");
                    try {
                        Future<List<LineEndpoint>> futureLineEndpoints = completionServiceLE.take();
                        lineEndpoints = futureLineEndpoints.get();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "InterruptedException");
                        Log.e(TAG, e.getCause().getMessage());
                        e.printStackTrace();
                        // InterruptedIOException ?
                    } catch (ExecutionException e) {
                        Log.e(TAG, "ExecutionException");
                        e.printStackTrace();
                    }
                    // if all line endpoints (15+4)*2 were successfully found get tram line links (URLs)
                    if (lineEndpoints.size() == 38) {
                        // submitting a LineSegmenter for each line 1-9, 11-15, 17
                        int submittedLines = 0;
                        for (int lineNumber = 1; lineNumber <= 17; lineNumber++) {
//                            if (lineNumber != 10 && lineNumber != 16) {
                            // line 15 is not operational
                            if (lineNumber != 10 && lineNumber != 16 && lineNumber != 15) {
                                // each line is given it's own endpoints
                                String date = getWorkingDay();
                                completionServiceLS.submit(new LineSegmenter(QTTconstants.LINK_FOR_LINE_N + lineNumber + date, lineEndpoints, context));
                                Log.i(TAG, "Submitted " + QTTconstants.LINK_FOR_LINE_N + lineNumber + date);
                                submittedLines++;
                            }
                        }
/*                        int submittedLines = 3;
                        String date = getWorkingDay();
                        completionServiceLS.submit(new LineSegmenter(QTTconstants.LINK_FOR_LINE_N + 1 + date, lineEndpoints, context));
                        completionServiceLS.submit(new LineSegmenter(QTTconstants.LINK_FOR_LINE_N + 9 + date, lineEndpoints, context));
                        completionServiceLS.submit(new LineSegmenter(QTTconstants.LINK_FOR_LINE_N + 17 + date, lineEndpoints, context));*/

                        publishProgress(5, context.getString(R.string.progressbar_segmenting));
                        int finished = 0;
                        while (finished < submittedLines) {
//                        for (int i = 0; i < submittedLines; i++) {
                            try {
                                Future<AbstractMap.SimpleImmutableEntry<Long, Boolean>> futureM = completionServiceLS.take();
                                result = futureM.get().getValue();

                                // limit number of attempts
                                if (result || resubmitAttempt <= 20) {
                                    finished++;
                                    publishProgress(5, context.getString(R.string.pb_finished)
                                            + finished + context.getString(R.string.pb_of)
                                            + submittedLines + context.getString(R.string.pb_lines));
                                    Log.i(TAG, "Finished " + finished + " of " + submittedLines + " lines");
                                }
                                // if result is false, the LineSegmenter should be resubmitted
                                else {
                                    long lineNumber = futureM.get().getKey();
                                    String date = getWorkingDay();
                                    completionServiceLS.submit(new LineSegmenter(QTTconstants.LINK_FOR_LINE_N + lineNumber + date, lineEndpoints, context));
                                    Log.i(TAG, "ReSubmitted " + QTTconstants.LINK_FOR_LINE_N + lineNumber + date);
                                    resubmitAttempt++;
                                }
                                // when one thread is finished, write it's data to database
                                // now each thread/line has it's own isolated segments

//                            while (finished >= submittedLines) {
//                            Log.i(TAG, "All finished");
//                            executor.shutdown();
//                            }
                            } catch (ExecutionException | InterruptedException ee) {
                                Log.e(TAG, ee.getCause().getMessage());
                            }
                        }

                    }
                }

                //submitted tasks are executed but no new tasks are accepted
                executor.shutdownNow();
                Log.i(TAG, "Executor shutdownNow");
                wakeLock.release();
                Log.i(TAG, "Wakelock release");

                // call to loadTramData()
            }
            else {
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // displays file not found dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                .setTitle(R.string.message_file_not_found_title)
                                .setMessage(R.string.message_file_not_found)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        builder.show();
                    }
                });

            }
        }
        else {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    // displays connection not available dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(context)
                            .setTitle(R.string.message_no_connection_title)
                            .setMessage(R.string.message_no_connection)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.show();
                }
            });
        }
    }


    /**
     * This method returns the encoded string for a working day or today if today is a working day.
     * @return the string in "&datum=yyyyMMdd" format
     */
    private String getWorkingDay() {
        DateTime date = DateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMdd");
        // if today is a working day
        if (date.getDayOfWeek() <= 5) {
            String dateToday = dateTimeFormatter.print(date);
            return "&datum=" + dateToday;
        }
        // today is saturday or sunday
        else {
            date = date.plusDays(3);
            String dateAdjusted = dateTimeFormatter.print(date);
            return "&datum=" + dateAdjusted;
        }
    }

    private void publishProgress(final int progress, final String message) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                progressBar.incrementProgressBy(progress);
                progressBar.setText(message);
            }
        });
    }

}
