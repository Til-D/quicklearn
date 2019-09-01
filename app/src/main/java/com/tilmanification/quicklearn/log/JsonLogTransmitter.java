package com.tilmanification.quicklearn.log;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.util.Log;

import com.tilmanification.quicklearn.BuildConfig;
import com.tilmanification.quicklearn.QuickLearnPrefs;
import com.tilmanification.quicklearn.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import static com.tilmanification.quicklearn.QuickLearnPrefs.PREF_CONSENT_DATA_SHARING;
import static com.tilmanification.quicklearn.log.QlearnKeys.QLEARN_DATE_LAST_LOG_UPLOAD;

/**
 * This logger offers a safe way of caching log messages and send them to a
 * server at a suitable time. It allows specifying the destination file on the
 * server, so log messages can be sent to different files. New messages are
 * first written into a synchronized queue. This minimizes the waiting time for
 * the logging application, and thus avoid e.g. the GUI to freeze for a while if
 * caching the message takes too long. A thread in the background empties this
 * queue as soon as new messages arrive and writes them into the specified file.
 * From time to time (and if WLAN is available) the same thread will read all
 * log files and send their content to a log server. Applications MUST call
 * onStop() when going in STOP mode, because otherwise the internal thread will
 * continue running.
 *
 * @author Martin Pielot
 */
public class JsonLogTransmitter implements Runnable {

    // ========================================================================
    // Constant Fields
    // ========================================================================

    private static final String		TAG									= JsonLogTransmitter.class.getSimpleName();
    private static final Object		ACCESS_LOG_FILE_MUTX				= new Object();
    private static final int		LINES_TO_READ						= 1000;

    private static final boolean	UPLOAD_LOGS_TO_SERVER				= true;
    private static final boolean	DELETE_LOGS_FROM_DEVICE_ON_UPLOAD	= true;

    // ========================================================================
    // Fields
    // ========================================================================

    /**
     * This value indicates how many messages should be cached before the app
     * attempts to flush the log file to the server.
     */
    public int						checkFlushFrequency					= 1000;
    public String					host;
    public boolean					allowFlushVia3G						= false;
    public boolean					includeEmptyValues					= true;
    public boolean					deleteWhenBroken					= false;

    private String					appName;
    private String					deviceUid;
    private com.tilmanification.quicklearn.log.LogMessageQueue queue;

    private Thread					runner;
    private boolean					running;

    private File					logDir;

    private ConnectivityManager		manager;

    private int						versionCode							= BuildConfig.VERSION_CODE;

    private boolean					uploadInProgress					= false;

    private Context context;

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * Creates a new logger
     *
     * @param context
     *            application context, used to create the UUID and to monitor
     *            WLAN connectivity.
     * @param appName
     *            the name of the application, used to define the folder where
     *            the data from this application will be stored on the server
     */
    public JsonLogTransmitter(Context context, String appName, String host) {
        this.appName = appName;
        this.host = host;
        this.deviceUid = UuidGen.getUniqueId(context);
        this.queue = new LogMessageQueue();
        this.context = context;

        // to make sure that the start time is initiated
        LogUtil.getUpTimeSec();

        logDir = getLogDir(context, appName);

        this.manager = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = pInfo.versionCode;
        } catch (Exception e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "Cannot obtain version code due to " + e, e);
            }
        }

    }

    public static File getLogDir(Context context, String appName) {

        if(QuickLearnPrefs.DEBUG_MODE) {
                    Log.i(TAG, "getLogDir()");
        }

//        File logDir = new File(Environment.getExternalStorageDirectory().toString(), appName);
        File logDir = new File(context.getFilesDir().getPath(), appName);

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "Data directory: " + logDir);
        }

        if (!logDir.exists()) {
            boolean success = logDir.mkdirs();
            if (!success) {
                if(QuickLearnPrefs.DEBUG_MODE) {
                    Log.e(TAG, "Could not create data directory: " + logDir);
                }
            }
        }

        return logDir;
    }

    // ========================================================================
    // Methods
    // ========================================================================

    // ------------------------------------------------------------------------
    // Starting & Stopping of the internal threads
    // ------------------------------------------------------------------------

    /**
     * Starts the internal thread that caches message to a file and flushes them
     * to the server.
     */
    public void onStart() {
        if (!running) {
            this.running = true;
            this.runner = new Thread(this);
            this.runner.start();
        }
    }

    /**
     * Stops the internal thread. DO NOT FORGET TO CALL THIS METHOD, otherwise
     * the thread will stay active forever and the app keeps consuming
     * resources.
     */
    public void onStop() {
        if (running) {
            this.running = false;
            this.runner.interrupt();
            this.runner = null;
        }
    }

    public boolean isRunning() {
        return running;
    }

    // ------------------------------------------------------------------------
    // Queueing messages for writing them into a log file
    // ------------------------------------------------------------------------

    public void log(Map<String, String> map) {
        updateTimeStamps(map);
        log(toString(map, includeEmptyValues));
    }

    public void log(String fileName, Map<String, String> map) {
        updateTimeStamps(map);
        log(fileName, toString(map, includeEmptyValues));
    }

    public void log(String filename, JSONObject json) {
        updateTimeStamps(json);
        log(filename, json.toString());
    }

    public void updateTimeStamps(Map<String, String> map) {
        final long now = System.currentTimeMillis();
        final String nowStr = LogUtil.getTimeStringMySql(now); // LogUtil.getTimeStringEn(now);
        final long uptimeSec = LogUtil.getUpTimeSec();

        map.put(LogKeys.UID, deviceUid);
        map.put(LogKeys.KEY_T, String.valueOf(now));
        map.put(LogKeys.KEY_UPTIME_SINCE_START_S, String.valueOf(uptimeSec));
        map.put(LogKeys.KEY_DATE, nowStr);
    }

    public void updateTimeStamps(JSONObject json) {
        final long now = System.currentTimeMillis();
        final String nowStr = LogUtil.getTimeStringMySql(now); // LogUtil.getTimeStringEn(now);
        final long uptimeSec = LogUtil.getUpTimeSec();

        try {
            // json.put(LogKeys.UID, deviceUid);
            json.put(LogKeys.KEY_T, String.valueOf(now));
            json.put(LogKeys.KEY_UPTIME_SINCE_START_S, String.valueOf(uptimeSec));
            // json.put(LogKeys.KEY_VC, String.valueOf(versionCode));
            json.put(LogKeys.KEY_DATE, nowStr);
        } catch (JSONException e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR updating timestamps in json object", e);
            }
        }
        // return json;
    }

    public static Map<String, String> clearTimeStamps(Map<String, String> map) {

        map.clear();
        map.put(LogKeys.UID, LogKeys.EMPTY);
        map.put(LogKeys.KEY_T, LogKeys.EMPTY);
        map.put(LogKeys.KEY_UPTIME_SINCE_START_S, LogKeys.EMPTY);
        // map.put(LogKeys.KEY_VC, LogKeys.EMPTY);
        map.put(LogKeys.KEY_DATE, LogKeys.EMPTY);
        return map;
    }

    /**
     * Logs a message to the default file, which equals the device ID.
     */
    public void log(String msg) {
        log(deviceUid, msg);
    }

    /**
     * Logs a message to the given file
     */
    public void log(String fileName, String msg) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "write log to: " + fileName + " (" + msg + ")");
        }
        LogMessage message = new LogMessage(fileName, msg);
        this.queue.queue(message);
    }

    // ------------------------------------------------------------------------
    // queue -> log file
    // ------------------------------------------------------------------------

    /**
     * The internal Thread, which is responsible for reading new messages from
     * the queue and writing them into the cache file, as well as reading all
     * cache files and flushing their content to the server. This Thread is not
     * a daemon, so do not forget to call onStop() when the app stops.
     */
    @Override
    public void run() {

        // clearLogDir();
        startUploadFiles(false);

        int i = 0;
        do {
            try {
                LogMessage message = this.queue.peek();

                // message might be zero if thread is interrupted
                if (message != null) {

                    // temporarily write the message to a file
                    writeToFile(message);

                    // from time to time send messages to server
                    if ((i % checkFlushFrequency == 0)) {
                        startUploadFiles(false);
                    }

                    i++;

                    this.queue.dequeue();
                }

            } catch (Exception e) {
                if(QuickLearnPrefs.DEBUG_MODE) {
                    Log.e(TAG, "unexpected exception in run()", e);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {}
            }
        } while (running);

        // We use this thread to try to send the remaining messages when the app
        // is shut down
        startUploadFiles(false);

        Log.i(TAG, "run() left");
    }

    @SuppressWarnings("unused")
    private void clearLogDir() {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.e(TAG, "clearLogDir() - REMOVE THIS");
        }
        boolean success = LogUtil.recursiveDelete(logDir);
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.e(TAG, "clearLogDir() - success == " + success);
        }
    }

    /**
     * Temporarily stores a log message in a phone on the phone's external
     * storage
     *
     * @param message
     *            the message to store
     */
    private void writeToFile(LogMessage message) {
        try {

            File logFile = new File(logDir, message.fileName);

            synchronized (ACCESS_LOG_FILE_MUTX) {

                if (!logFile.exists()) logFile.createNewFile();

                BufferedWriter out = new BufferedWriter(new FileWriter(logFile, true));
                out.append(message.message);
                out.append(QuickLearnPrefs.CRLF);
                out.close();
            }

        } catch (Exception e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "could not write message to file", e);
            }
        }

    }

    // ------------------------------------------------------------------------
    // log file -> server
    // ------------------------------------------------------------------------

    public void startUploadFiles(boolean override3G) {

        Boolean wlanAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.d(TAG, "startFlushIfWlan() -  wlan == "
                    + wlanAvailable
                    + ", allow 3G == "
                    + allowFlushVia3G
                    + ", allow override == "
                    + override3G);
        }

        boolean sharingGranted = Util.getBool(context, PREF_CONSENT_DATA_SHARING, true);
        if(sharingGranted) {

            if (wlanAvailable || allowFlushVia3G || override3G) {
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        if (UPLOAD_LOGS_TO_SERVER && !uploadInProgress) {
                            uploadInProgress = true;
                            uploadAllLogFiles();
                            uploadInProgress = false;
                        }
                    }
                });
                t.start();
            }

        } else {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "Sharing permission has been explicitly removed by user. Go to Settings to enable sharing again.");
            }
        }
    }

    /**
     * This method iterates through all temporary log files, tries to flush them
     * to the server, and deletes them if the flush was successful.
     */
    private void uploadAllLogFiles() {
        File[] logFiles = logDir.listFiles();

        // no log files? we are done!
        if (logFiles == null || logFiles.length == 0) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.d(TAG, "Nothing to flush");
            }
            return;
        }

        for (File file : logFiles) {
            try {
                boolean hasMoreContent = file.exists() && file.length() > 0;
                while (hasMoreContent) {
                    hasMoreContent = readAndUpload(file);
                }

            } catch (Exception e) {
                if(QuickLearnPrefs.DEBUG_MODE) {
                    Log.e(TAG, e + " in flush() while uploading " + file.getName(), e);
                }
            }

        }
    }

    /**
     * Reads all content from a reader, line by line. It re-adds the line
     * feed/carriage return behind each line, except for the last, so that the
     * string does not terminated by a CRLF.
     *
     * @param file
     *            the reader. will be closed after the procedure
     * @return returns the content that the reader provided
     * @throws IOException
     *             if reading the reader's content fails
     */
    private boolean readAndUpload(File file) throws IOException {

//        Log.i(TAG, "readAndUploadFile: " + file.toString());

        synchronized (ACCESS_LOG_FILE_MUTX) {

            if (!file.exists()) { return false; }

            Log.d(TAG, file.length() + ", " + file.exists() + ", " + file.getName());

            BufferedReader reader = new BufferedReader(new FileReader(file));
            // StringBuilder b = new StringBuilder();
            String line = null;
            int lineNumber = 0;
            JSONArray jArray = new JSONArray();

            while ((line = reader.readLine()) != null && lineNumber < LINES_TO_READ) {

                // write JSONArray to buffer
                try {
                    jArray.put(new JSONObject(line));
                } catch (JSONException e) {
                    if(QuickLearnPrefs.DEBUG_MODE) {
                        Log.e(TAG, "ERROR: Could not parse log to JSON: '" + line + "'", e);
                    }
                }

                // b.append(line);
                // b.append(Const.CRLF);
                lineNumber++;
            }

            // Log.d(TAG, "read " + lineNumber + " lines");

            // int length = b.length();
            int length = jArray.length();
            if (length == 0) {
                reader.close();
                // Log.i(TAG, "nothing to upload at this point.");
                return false;
            }

            // else, remove CRLF the last CRLF, because the php script will
            // always add an CRLF when writing data
            // b.delete(length - 2, length - 1);

            // boolean success = upload(file, b.toString());
            boolean success = upload(file, jArray);

            // This was not successful, try to send data later again
            if (!success) {
                reader.close();
                if(QuickLearnPrefs.DEBUG_MODE) {
                    Log.i(TAG, "upload not successful, try again later");
                }

                return false;
            }

            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.d(TAG, "successfully uploaded " + lineNumber + " lines from " + file.getName());
            }

            boolean hasMoreData = line != null;
            if (!hasMoreData) {
                reader.close();
                boolean deleted = false;

                // save last successful log upload
                Util.putLong(context, QLEARN_DATE_LAST_LOG_UPLOAD, System.currentTimeMillis());

                if (DELETE_LOGS_FROM_DEVICE_ON_UPLOAD) {
                    deleted = file.delete();
                }
                if(QuickLearnPrefs.DEBUG_MODE) {
                    Log.i(TAG, "no more data in log, log file deleted == " + deleted);
                }
                return false;
            }

            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.d(TAG, "more data in log file, will write rest back for future upload");
            }

            File tmp = new File(file.getAbsolutePath() + "tmp");
            FileWriter writer = new FileWriter(tmp);
            lineNumber = 0;

            // read remaining lines, this time do - while, because there is
            // still one line left from the last read
            // operation
            do {
                writer.write(line);
                writer.write(QuickLearnPrefs.CRLF);
                lineNumber++;
            } while ((line = reader.readLine()) != null);

            writer.close();
            reader.close();

            boolean deleted = file.delete();
            boolean renamed = tmp.renameTo(file);

            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "uploaded partially. Wrote remaining "
                        + lineNumber
                        + " lines into "
                        + tmp
                        + " and renamed to original. original deleted == "
                        + deleted
                        + ", tmp renamed == "
                        + renamed);
            }

            // true = more data
            return true;
        }
    }

    @SuppressLint("DefaultLocale")
    private boolean upload(File file, JSONArray content) {

        String filename = file.getName();

        try {

            JSONObject data = new JSONObject();
            data.put(QlearnKeys.APPLICATION_ID, this.appName);
            data.put(QlearnKeys.APPLICATION_UID, filename);
            data.put(QlearnKeys.APPLICATION_CS, getHashSum(content.toString()));
            data.put(LogKeys.KEY_VC, versionCode);
            data.put(QlearnKeys.APPLICATION_DATA, content);

            // send request
            HttpURLConnection conn = null;
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "Connecting to host: " + host);
            }
            URL url = new URL(host);
            if (url.getProtocol().toLowerCase().equals("https")) {
                ServerComm.trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(ServerComm.DO_NOT_VERIFY);
                conn = https;
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "GYUserAgentAndroid");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setUseCaches(false);

            DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
            String dataString = data.toString();
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "writing " + dataString.length() + " bytes to POST request output stream..");
            }
            //Log.v(TAG, dataString);
            outputStream.writeBytes(dataString);
            outputStream.flush();
            outputStream.close();

            // Get the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            JSONObject response = new JSONObject(ServerComm.readAllAndClose(reader));
            boolean success = response.getString("status").equals("success");

            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, filename
                        + ", "
                        + LogUtil.format(content.length() / 1024.0)
                        + "kb, response: '"
                        + response
                        + "' == "
                        + (success ? "success" : "failed"));
            }

            if (!success && deleteWhenBroken) {

                boolean deleted = file.delete();

                if(QuickLearnPrefs.DEBUG_MODE) {
                    Log.e(TAG, file.getAbsolutePath()
                            + " containing "
                            + content.length()
                            + " chars seems to be broken --> deleting it == "
                            + deleted);
                }
            }

            return success;

        } catch (Exception e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.w(TAG, e + " while sending flushing part of " + filename, e);
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    /**
     * This method turns a map (HashMap, LinkedMap, ...) into a csv line, such
     * as "time=1234,speed=1.05,acc=32".
     *
     * @param map
     *            the map to be serialized
     * @return the serialized String that represents the content of the map
     */
    public static String toString(Map<String, String> map, boolean includeEmptyValues) {
        StringBuilder b = new StringBuilder();
        Set<String> keySet = map.keySet();
        Iterator<String> iter = keySet.iterator();
        boolean first = true;

        while (iter.hasNext()) {
            String key = iter.next();
            String value = map.get(key);

            // If value is not empty
            if (includeEmptyValues || value.length() > 0) {
                // Make sure that each new entry is separated by a comma
                if (first) {
                    first = false;
                } else {
                    b.append(QuickLearnPrefs.SEPARATOR);
                }
                b.append(key);
                b.append('=');
                b.append(value);
            }

        }
        return b.toString();
    }

    /**
     * Returns a salted hash sum for the given data string.
     *
     * @param data
     *            the data to generate a hash sum for
     * @return the salted hash
     */
    public static String getHashSum(String data) {
        String retVal = null;
        String saltData = data + QuickLearnPrefs.HTTPGETSALT;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] encryptMsg = md.digest(saltData.getBytes());

            String encryptString = "";
            for (int i = 0; i < encryptMsg.length; i++) {
                encryptString += Integer.toString((encryptMsg[i] & 0xff) + 0x100, 16).substring(1);
            }
            retVal = encryptString.substring(encryptString.length() - 8, encryptString.length());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return retVal;
    }
}
