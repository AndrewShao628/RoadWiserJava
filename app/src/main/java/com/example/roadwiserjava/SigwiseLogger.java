package com.example.roadwiserjava;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class SigwiseLogger {

    private static File logFile = new File(
            Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Sigwise"
                    + File.separator + "securitywiser" + File.separator + "roadvoyager.log"
    );

    private static final String TAG = SigwiseLogger.class.getName();

    private static void logToFile(String tag, String message) {
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String strTime = f.format( new Date() );
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append(strTime)
                    .append(" ")
                    .append("[")
                    .append(tag)
                    .append("]: ")
                    .append(message)
                    .append("\r\n");
            BufferedWriter writer = new BufferedWriter( new FileWriter(logFile, true) );
            writer.write(strBuilder.toString());
            writer.close();

        } catch ( IOException e) {
            Log.e( TAG, Log.getStackTraceString(e) );
        }

    }

    synchronized
    public static void e(String tag, String message) {
        if (!Log.isLoggable(tag, Log.ERROR))
            return;
        Log.e(tag, message);
        logToFile(tag, message);

    }

    synchronized
    public static void e(String tag, String message, Throwable t) {
        if (!Log.isLoggable(tag, Log.ERROR))
            return;
        Log.e(tag, message, t);
        logToFile(tag, message + "\r\n" + Log.getStackTraceString(t));

    }

    synchronized
    public static void w(String tag, String message) {
        if (!Log.isLoggable(tag, Log.WARN))
            return;
        Log.w(tag, message);
        logToFile(tag, message);

    }

    synchronized
    public static void w(String tag, String message, Throwable t) {
 /*       if (!Log.isLoggable(tag, Log.WARN))
            return;
        Log.w(tag, message, t);
        logToFile(tag, message + "\r\n" + Log.getStackTraceString(t));
*/
    }

    synchronized
    public static void i(String tag, String message) {
        if (!Log.isLoggable(tag, Log.INFO))
            return;
        Log.i(tag, message);
        logToFile(tag, message);

    }

    synchronized
    public static void i(String tag, String message, Throwable t) {
        if (!Log.isLoggable(tag, Log.INFO))
            return;
        Log.i(tag, message, t);
        logToFile(tag, message + "\r\n" + Log.getStackTraceString(t));

    }

    synchronized
    public static void d(String tag, String message) {
/*        if ( !BuildConfig.DEBUG || !Log.isLoggable(tag, Log.DEBUG))
            return;
        Log.d(tag, message);
        logToFile(tag, message);
        */
    }

    synchronized
    public static void d(String tag, String message, Throwable t) {
/*        if ( !BuildConfig.DEBUG || !Log.isLoggable(tag, Log.DEBUG))
            return;
        Log.d(tag, message, t);
        logToFile(tag, message + "\r\n" + Log.getStackTraceString(t));
        */
    }

    synchronized
    public static void v(String tag, String message) {
/*        if ( !BuildConfig.DEBUG || !Log.isLoggable(tag, Log.VERBOSE))
            return;
        Log.v(tag, message);
        logToFile(tag, message);
        */
    }

    synchronized
    public static void v(String tag, String message, Throwable t) {
/*        if ( !BuildConfig.DEBUG || !Log.isLoggable(tag, Log.VERBOSE))
            return;
        Log.v(tag, message, t);
        logToFile(tag, message + "\r\n" + Log.getStackTraceString(t));*/
    }

}
