package com.lena.tj.net;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.lena.tj.MapsActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostTask extends AsyncTask<String, Void, String> {
    private MyAsyncResponse delegate = null;

    private Context mContext;
    private String fullResponseMessage;
    private String serverJson;

    public PostTask(Context context) {
        mContext = context;
    }

    public PostTask(Context context, MyAsyncResponse delegate) {
        mContext = context;
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0];

        URL url;
        OutputStreamWriter out;
        HttpURLConnection httpConnection = null;

        try {
            url = new URL(urlString);

            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            httpConnection.setUseCaches(false);

            out = new OutputStreamWriter(httpConnection.getOutputStream());
            out.write(params[1]);
            out.close();

            int responseCode = httpConnection.getResponseCode();
            fullResponseMessage = "" + responseCode + ": " + httpConnection.getResponseMessage();
            Log.d(MapsActivity.LOG_TAG, "result: \n" + fullResponseMessage + "\n\n");

            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(httpConnection.getInputStream()), 65728);
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            catch (IOException e) { e.printStackTrace(); }
            catch (Exception e) { e.printStackTrace(); }

            serverJson = sb.toString();
            Log.d(MapsActivity.LOG_TAG, "finalResult: " + sb.toString());
            return fullResponseMessage;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return fullResponseMessage;
    }


    @Override
    protected void onPostExecute(String responseMessage) {
        if (fullResponseMessage.equals("200: OK")) {
            Toast.makeText(mContext, "post ok", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "post NO ok", Toast.LENGTH_SHORT).show();
        }

        delegate.processFinish(serverJson);
    }

    public interface MyAsyncResponse {
        void processFinish(String serverJson);
    }
}
