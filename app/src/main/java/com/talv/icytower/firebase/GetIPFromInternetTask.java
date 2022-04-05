package com.talv.icytower.firebase;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

class GetIPFromInternetTask extends AsyncTask<Void, Void, String> {
    private static final String IP_GETTER_URL = "https://iplist.cc/api";

    @Override
    protected String doInBackground(Void... params) {
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(IP_GETTER_URL);
            URLConnection urlConn = url.openConnection();
            bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            StringBuilder stringBuffer = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            return new JSONObject(stringBuffer.toString()).getString("countrycode");
        } catch (Exception ex) {
            return null;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }


}