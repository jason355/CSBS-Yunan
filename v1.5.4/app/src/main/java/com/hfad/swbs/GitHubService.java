package com.hfad.swbs;


import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GitHubService {

    public interface VersionCheckListener {
        void onVersionCheckResult(String[] data);
    }

    public static void checkUpdate(VersionCheckListener listener) {
        new VersionCheckTask(listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class VersionCheckTask extends AsyncTask<Void, Void, String[]> {
        private VersionCheckListener listener;

        public VersionCheckTask(VersionCheckListener listener) {
            this.listener = listener;
        }

        @Override
        protected String[] doInBackground(Void... voids) {
            String version = "154"; // use database to get version
            String[] data = new String[2];
            int localVersion = Integer.parseInt(version);
            String rawUrl = "https://raw.githubusercontent.com/jason355/CSBS-Yunan/main/lastest_version.txt";
            String latestVersionString = getNewVersion(rawUrl);
            int latestVersion = Integer.parseInt(latestVersionString);
            data[0] = String.valueOf(latestVersion);
            Log.d("lastestVersion", data[0]);
            Log.d("localVersion", String.valueOf(localVersion));
            // Compare version numbers
            if (latestVersion > localVersion && latestVersion != -1) {
                data[1] = "True";
            } else {
                data[1] = "False";
            }
            return data;
        }

        @Override
        public void onPostExecute(String[] data) {
//            this.data = data;
            if (listener != null) {
                listener.onVersionCheckResult(data);
            }
        }
    }

    private static String getNewVersion(String rawUrl) {
        try {
            URL url = new URL(rawUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            } else {
                // Handle request failure
                return "-1";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "-1";
    }
}
