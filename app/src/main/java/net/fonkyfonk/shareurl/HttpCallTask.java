package net.fonkyfonk.shareurl;

import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jo on 15/12/2016.
 */

public class HttpCallTask extends AsyncTask<Void, Void, String> {
    String url, type;
    OnCallFinishedListener listener;
    boolean error = false;
    Exception exception;
    String data;

    public HttpCallTask(String url, String type, String data, OnCallFinishedListener listener) {
        this.url = url;
        if(!this.url.startsWith("http://") && !this.url.startsWith("https://"))
            this.url = "http://"+this.url;
        this.listener = listener;
        this.type = type;
        this.data = (data == null ? "" : data);
        this.execute();
    }

    @Override
    protected String doInBackground(Void... voids) {
        InputStream is = null;
        try {
            URL url = new URL(this.url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod(type);
            conn.setDoInput(true);
            if(type.compareTo("POST") == 0 || type.compareTo("PUT") == 0) {
                if(data != null) {
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "text/plain");
//                OutputStream os = conn.getOutputStream();
//                os.write(data.toString().getBytes("UTF-8"));
//                os.close();
                    OutputStream outputStream = new BufferedOutputStream(conn.getOutputStream());
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"));
                    writer.write(data);
                    writer.flush();
                    writer.close();
                    outputStream.close();
                }
            }
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
//            Log.d(DEBUG_TAG, "The response is: " + response);
            if (response < HttpURLConnection.HTTP_BAD_REQUEST) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }
            // Convert the InputStream into a string
            String contentAsString = readIt(is);
            if (is != null) is.close();
            return contentAsString;
        } catch(Exception e){
            e.printStackTrace();
            error = true;
            exception = e;
        }
        return null;
    }
    public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
//        Reader reader = null;
//        reader = new InputStreamReader(stream, "UTF-8");
//        char[] buffer = new char[len];
//        reader.read(buffer);
//        return new String(buffer);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }
    @Override
    protected void onPostExecute(String result) {
        if(listener != null) {
            if(error)   listener.onError(exception);
            else        listener.onCallFinished(result);
        }
    }
    public interface OnCallFinishedListener {
        void onCallFinished(String string);
        void onError(Exception e);
    }
}

