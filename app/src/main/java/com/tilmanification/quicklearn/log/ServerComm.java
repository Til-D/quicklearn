package com.tilmanification.quicklearn.log;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import com.tilmanification.quicklearn.BuildConfig;
import com.tilmanification.quicklearn.QuickLearnPrefs;
import com.tilmanification.quicklearn.R;
import com.tilmanification.quicklearn.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@SuppressLint("TrulyRandom")
public class ServerComm {

    // ========================================================================
    // Constant Fields
    // ========================================================================

    private static final String		TAG							= ServerComm.class.getSimpleName();

    public static final String		SERVER						= QuickLearnPrefs.LOG_SERVER;

    public static final String		LOG_SERVER_URL				= SERVER + "/log";

    // generic parameters
    public static String			PARAM_UID					= "uid";
    public static final String		RESP_SUCCESS				= "success";

    public static final String		REG_USER_URL				= SERVER + "/user/register";
    public static final String		UPDATE_USER_URL				= SERVER + "/user/update";
    public static final String		REG_USER_PARAM_LOCALE		= "locale";
    public static final String		REG_USER_PARAM_TIMEZONE		= "timezone";
    public static final String		REG_USER_PARAM_OS			= "os";
    public static final String		REG_USER_PARAM_DEVICE		= "device";
    public static final String		REG_USER_PARAM_SDK			= "sdk";
    public static final String		REG_USER_RESP_DATABASE_ID	= "id";
    public static final String		REG_USER_RESP_STATUS	    = "status";
    public static final String		REG_USER_PARAM_VERSION  	= "version";
    public static final String		REG_USER_PARAM_SIMCARD      = "simcard_info";
    public static final String		REG_USER_PARAM_INSTALLED_APPS  	= "installed_apps";
    public static final String		REG_USER_PARAM_ACCESSIBLITY_ENABLED  	= "accessibility_enabled";
    public static final String		REG_USER_PARAM_NOTIFICATION_ACESS  	= "notification_access";
    public static final String		REG_USER_PARAM_LOCATION_ACCESS  	= "location_access";

    public static final String		REG_USER_PARAM_SRC_LANG  	= "src_language";
    public static final String		REG_USER_PARAM_TARGET_LANG  = "target_language";
    public static final String		REG_USER_PARAM_PROFICIENCY  = "proficiency";


    public static final String		SEND_EMAIL_URL				= SERVER + "/user/email";
    public static final String		SEND_EMAIL_PARAM_EMAIL		= "email";
    public static final String		SEND_EMAIL_PARAM_AGE		= "age";
    public static final String		SEND_EMAIL_PARAM_GENDER		= "gender";
    public static final String		SEND_EMAIL_PARAM_USAGE		= "phone_usage";
    public static final String		SEND_EMAIL_RESP_SUCCESS		= "success";

    public static final String		PROGRESS_URL				= SERVER + "/user/progress";

    public static final String		COMPLETE_URL				= SERVER + "/user/complete";
    public static final String		COMPLETE_URL_RESP_SUCCESS	= "completed";

    public static final String		SETUP_COMPLETED_URL			= SERVER + "/user/setup";

    // ========================================================================
    // Methods
    // ========================================================================

    public static void sendUserRegistrationToServerAsync(Context context,
                                                         String uid,
                                                         String os,
                                                         int sdk,
                                                         String device,
                                                         String locale,
                                                         String timezone,
                                                         int version,
                                                         String simcardInfo,
                                                         String installedApps,
                                                         boolean accessibility_enabled,
                                                         boolean notification_access,
                                                         boolean location_access,
                                                         String email,
                                                         int age,
                                                         String gender,
                                                         String phoneUsage,
                                                         String src_language,
                                                         String target_language,
                                                         String proficiency) {

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "user registration: " + uid);
        }

        JSONObject data = new JSONObject();
        try {

            data.put(ServerComm.PARAM_UID, uid);
            data.put(ServerComm.REG_USER_PARAM_OS, os);
            data.put(ServerComm.REG_USER_PARAM_SDK, sdk);
            data.put(ServerComm.REG_USER_PARAM_DEVICE, device);
            data.put(ServerComm.REG_USER_PARAM_LOCALE, locale);
            data.put(ServerComm.REG_USER_PARAM_TIMEZONE, timezone);
            data.put(ServerComm.REG_USER_PARAM_VERSION, version);
            data.put(ServerComm.REG_USER_PARAM_SIMCARD, simcardInfo);
            data.put(ServerComm.REG_USER_PARAM_INSTALLED_APPS, installedApps);
            data.put(ServerComm.REG_USER_PARAM_ACCESSIBLITY_ENABLED, accessibility_enabled);
            data.put(ServerComm.REG_USER_PARAM_NOTIFICATION_ACESS, notification_access);
            data.put(ServerComm.REG_USER_PARAM_LOCATION_ACCESS, location_access);
            data.put(ServerComm.SEND_EMAIL_PARAM_EMAIL, email);
            data.put(ServerComm.SEND_EMAIL_PARAM_GENDER, gender);
            data.put(ServerComm.SEND_EMAIL_PARAM_USAGE, phoneUsage);
            data.put(ServerComm.SEND_EMAIL_PARAM_AGE, age);
            data.put(ServerComm.REG_USER_PARAM_SRC_LANG, src_language);
            data.put(ServerComm.REG_USER_PARAM_TARGET_LANG, target_language);
            data.put(ServerComm.REG_USER_PARAM_PROFICIENCY, proficiency);

        } catch (JSONException exc) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "could not construct JSON object");
            }
            exc.printStackTrace();
        }

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, data.toString());
        }

        String host = ServerComm.REG_USER_URL;
        final Context ctxt = context;
        ServerComm.sendBackgroundPostRequestAsync(context, host, data, new ServerCommListener() {
            public void onResult(JSONObject responseObj) {
                try {

                    // responses
                    final String status = responseObj.getString(ServerComm.REG_USER_RESP_STATUS);
                    final int dbId = responseObj.getInt(ServerComm.REG_USER_RESP_DATABASE_ID);

                    if(QuickLearnPrefs.DEBUG_MODE) {
                        Log.i(TAG, "user registered (ID: "
                                + dbId
                                + "), status == "
                                + status);
                    }

                    if(status.equals("ok")) {
                        // store response in preferences
                        Util.putInt(ctxt, QuickLearnPrefs.PREF_DATABASE_ID, dbId);
                        Util.put(ctxt, QuickLearnPrefs.PREF_USER_REGISTERED, true);
                    } else {
                        if(QuickLearnPrefs.DEBUG_MODE) {
                            Log.e(TAG, "could not register user (status: " + status + ")");
                        }
                    }


                } catch (JSONException e) {
                    if(QuickLearnPrefs.DEBUG_MODE) {
                        Log.e(TAG, "Could not parse JSON response in checkEligibility()", e);
                    }
                }
            }

            @Override
            public void onPostExecute(Boolean result) {}
        });
    }

    /**
     * updates participant entry on server (called upon application update)
     * @param context
     * @param version
     */
    public static void sendUserUpdate(final Context context,
                                        int version) {

        String uid = UuidGen.getPid6(context);

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "user update: " + uid);
        }

        JSONObject data = new JSONObject();
        try {

            data.put(ServerComm.PARAM_UID, uid);
            data.put(ServerComm.REG_USER_PARAM_VERSION, version);

        } catch (JSONException exc) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "could not construct JSON object");
            }
            exc.printStackTrace();
        }

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, data.toString());
        }

        String host = ServerComm.UPDATE_USER_URL;
        final Context ctxt = context;
        ServerComm.sendBackgroundPostRequestAsync(context, host, data, new ServerCommListener() {
            public void onResult(JSONObject responseObj) {
                try {

                    // responses
                    final String status = responseObj.getString(ServerComm.REG_USER_RESP_STATUS);

                    if(QuickLearnPrefs.DEBUG_MODE) {
                        Log.i(TAG, "user updated (version: "
                                + BuildConfig.VERSION_CODE
                                + "), status == "
                                + status);
                    }

                    if(status.equals("ok")) {
                        // store response in preferences
                        Util.putInt(context, QuickLearnPrefs.CURRENT_VERSION_CODE, BuildConfig.VERSION_CODE);
                    } else {
                        if(QuickLearnPrefs.DEBUG_MODE) {
                            Log.e(TAG, "could not register user (status: " + status + ")");
                        }
                    }


                } catch (JSONException e) {
                    if(QuickLearnPrefs.DEBUG_MODE) {
                        Log.e(TAG, "Could not parse JSON response in checkEligibility()", e);
                    }
                }
            }

            @Override
            public void onPostExecute(Boolean result) {}
        });
    }

    public static void sendDemographicsToServerAsnyc(final Context context,
                                                     final String email,
                                                     final int age,
                                                     final String gender) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "sendDemographicsToServerAsnyc( " + email + ", " + age + ", " + gender + " )");
        }

        String host = ServerComm.SEND_EMAIL_URL;
        try {

            String uid = UuidGen.getPid6(context);

            JSONObject data = new JSONObject();
            data.put(ServerComm.PARAM_UID, uid);
            data.put(ServerComm.SEND_EMAIL_PARAM_EMAIL, email);
            data.put(ServerComm.SEND_EMAIL_PARAM_AGE, age);
            data.put(ServerComm.SEND_EMAIL_PARAM_GENDER, gender);

            ServerComm.sendBackgroundPostRequestAsync(context, host, data, new ServerCommListener() {
                public void onResult(JSONObject responseObj) {
                    try {
                        boolean success = responseObj.getBoolean(ServerComm.SEND_EMAIL_RESP_SUCCESS);
                        if(QuickLearnPrefs.DEBUG_MODE) {
                            Log.i(TAG, "Email update success == " + success);
                        }
                        Util.put(context, QuickLearnPrefs.PREF_USER_REGISTERED, success);

                    } catch (JSONException e) {
                        if(QuickLearnPrefs.DEBUG_MODE) {
                            Log.e(TAG, "Could not read JSON responseObj in checkEligibility()", e);
                        }
                    }
                }

                @Override
                public void onPostExecute(Boolean result) {}
            });
        } catch (Exception e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "Could not put UID into JSON Object checkEligibility()", e);
            }
        }
    }

    public static void tryInformServerSetupCompleted(Context context) {
        // If setup finished, but server not informed yet
        if (!Util.getBool(context, QuickLearnPrefs.PREF_SETUP_COMPLETE_COMMUNICATED, false)
                && Util.getBool(context, QuickLearnPrefs.PREF_SETUP_FINISHED, false)) {
            // inform server
            informServerSetupCompleted(context);
        }
    }

    public static void informServerSetupCompleted(Context context) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "informServerSetupCompleted()");
        }
        try {

            // prepare parameters
            final Context ctxt = context;
            final String uid = UuidGen.getPid6(context);
            JSONObject data = new JSONObject();
            data.put(ServerComm.PARAM_UID, uid);

            // send request
            ServerComm.sendBackgroundPostRequestAsync(
                    context,
                    ServerComm.SETUP_COMPLETED_URL,
                    data,
                    new ServerCommListener() {
                        public void onResult(JSONObject responseObj) {
                            try {
                                boolean success = responseObj.getBoolean(ServerComm.RESP_SUCCESS);
                                if(QuickLearnPrefs.DEBUG_MODE) {
                                    Log.i(TAG, "server response: " + success);
                                }
                                Util.put(ctxt, QuickLearnPrefs.PREF_SETUP_COMPLETE_COMMUNICATED, success);
                            } catch (JSONException e) {
                                if(QuickLearnPrefs.DEBUG_MODE) {
                                    Log.e(TAG, "Could not read server response in informServerSetupCompleted()", e);
                                }
                            }
                        }

                        @Override
                        public void onPostExecute(Boolean result) {}
                    });
        } catch (Exception e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.w(TAG, "Could not reach server in informServerSetupCompleted(). Will try on next onResume()", e);
            }
        }
    }

    public static void sendPostRequestAsync(final Context context,
                                            final String serviceUrl,
                                            final JSONObject data,
                                            final ServerCommListener listener) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "sendPostRequestAsync( " + serviceUrl + " )");
        }

        final Resources res = context.getResources();

        // Progress dialog
        final ProgressDialog progress = ProgressDialog.show(context, "", res.getString(R.string.wait), true);

        AsyncTask<Void, Void, Boolean> waitForCompletion = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {

                boolean success = false;

                try {
                    JSONObject responseObj = sendPostRequest(serviceUrl, data);
                    listener.onResult(responseObj);
                    success = true;

                } catch (Exception e) {
                    if(QuickLearnPrefs.DEBUG_MODE) {
                        Log.e(TAG, "Exception in sendPostRequestAsync( " + serviceUrl + " )", e);
                    }
                }
                progress.dismiss();
                return success;

            }

            @Override
            protected void onPostExecute(Boolean result) {
                listener.onPostExecute(result);
            }
        };

        waitForCompletion.execute(null, null, null);
    }

    public static void sendBackgroundPostRequestAsync(final Context context,
                                                      final String serviceUrl,
                                                      final JSONObject data,
                                                      final ServerCommListener listener) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "sendBackgroundPostRequestAsync( " + serviceUrl + " )");
        }

        AsyncTask<Void, Void, Boolean> waitForCompletion = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {

                boolean success = false;

                try {
                    JSONObject responseObj = sendPostRequest(serviceUrl, data);
                    listener.onResult(responseObj);
                    success = true;

                } catch (Exception e) {
                    if(QuickLearnPrefs.DEBUG_MODE) {
                        Log.e(TAG, "Exception in AsyncTask of sendBackgroundPostRequestAsync()", e);
                    }
                }
                return success;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                listener.onPostExecute(result);
            }
        };
        waitForCompletion.execute(null, null, null);
    }

    public static JSONObject sendPostRequest(final String host, final JSONObject data) throws Exception {
        // send request
        HttpURLConnection con = null;

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "Connecting to " + host);
        }
        URL url = new URL(host);
        if (url.getProtocol().toLowerCase(Locale.US).equals("https")) {
            ServerComm.trustAllHosts();
            HttpsURLConnection https = (HttpsURLConnection) url.openConnection();

            https.setHostnameVerifier(ServerComm.DO_NOT_VERIFY);
            https.setReadTimeout(1000 * 2);
            con = https;
        } else {
            con = (HttpURLConnection) url.openConnection();
        }

        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "GYUserAgentAndroid");
        con.setRequestProperty("Content-Type", "application/json");
        con.setUseCaches(false);

        con.setConnectTimeout(1000 * 5);
        con.setReadTimeout(1000 * 5);

        // FileNotFound
        DataOutputStream outputStream = new DataOutputStream(con.getOutputStream());
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "JSON Data: " + data.toString());
        }
        outputStream.writeBytes(data.toString());
        outputStream.flush();
        outputStream.close();

        // Get the response
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String response = ServerComm.readAllAndClose(reader);

        JSONObject responseObj = new JSONObject(response);
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "JSON Response: " + responseObj);
        }
        return responseObj;
    };

    public static JSONObject httpGet(String url){

        InputStream inputStream = null;
        try {

//            DEPRECATED IMPLEMENTATION
//            // create HttpClient
//            HttpClient httpclient = new DefaultHttpClient();
//
//            // make GET request to the given URL
//            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
//
//            // receive response as inputStream
//            inputStream = httpResponse.getEntity().getContent();


            URL urlObj = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();
            inputStream = urlConnection.getInputStream();

            // convert inputstream to string
            if(inputStream != null) {
                String result = convertInputStreamToString(inputStream);
                JSONObject json = new JSONObject(result);

                return json;

            }


        } catch (Exception e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.d("InputStream", e.getLocalizedMessage());
            }
        }
        return null;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public static JSONObject sendGetRequest0(final String fullUrl) throws Exception {
        // send request
        HttpURLConnection con = null;

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "Connecting to " + fullUrl);
        }
        URL url = new URL(fullUrl);
        if (url.getProtocol().toLowerCase(Locale.US).equals("https")) {
            ServerComm.trustAllHosts();
            HttpsURLConnection https = (HttpsURLConnection) url.openConnection();

            https.setHostnameVerifier(ServerComm.DO_NOT_VERIFY);
            https.setReadTimeout(1000 * 2);
            con = https;
        } else {
            con = (HttpURLConnection) url.openConnection();
        }

        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "GYUserAgentAndroid");
        con.setRequestProperty("Content-Type", "application/json");
        con.setUseCaches(false);

        con.setConnectTimeout(1000 * 5);
        con.setReadTimeout(1000 * 5);

        // Get the response
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String response = ServerComm.readAllAndClose(reader);

        JSONObject responseObj = new JSONObject(response);
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "JSON Response: " + responseObj);
        }
        return responseObj;
    };

    /**
     * Reads all content from a reader, line by line. It re-adds the line
     * feed/carriage return behind each line, except for the last, so that the
     * string does not terminated by a CRLF.
     *
     * @param reader
     *            the reader. will be closed after the procedure
     * @return returns the content that the reader provided
     * @throws IOException
     *             if reading the reader's content fails
     */
    public static String readAllAndClose(BufferedReader reader) throws IOException {
        StringBuilder b = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            b.append(line);
            b.append(QuickLearnPrefs.CRLF);
        }

        // we need to delete the last CRLF, because the php script will always
        // add an CRLF when writing data
        int length = b.length();
        if (length > 1) {
            b.delete(length - 2, length - 1);
        }
        reader.close();
        return b.toString();
    }

    // ------------------------------------------------------------------------
    // SSL - by
    // http://stackoverflow.com/questions/6825226/trust-anchor-not-found-for-android-ssl-connection
    // ------------------------------------------------------------------------

    /**
     * Trust every server - dont check for any certificate
     */
    @SuppressLint("TrulyRandom")
    public static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "Exception in trustAllHosts()", e);
            }
        }
    }

    // always verify the host - dont check for certificate
    public final static HostnameVerifier	DO_NOT_VERIFY	= new HostnameVerifier() {
        public boolean verify(String hostname,
                              SSLSession session) {
            return true;
        }
    };

}

interface ServerCommListener {
    void onResult(JSONObject responseObj);

    void onPostExecute(Boolean result);
}
