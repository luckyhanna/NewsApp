package com.example.hannabotar.newsapp;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by hanna on 7/1/2018.
 */

public class NewsUtils {

    public static final String LOG_TAG = NewsUtils.class.getSimpleName();

    private static final String GUARDIAN_BASE_URL = "https://content.guardianapis.com/search?q=technology&format=json&order-by=newest&showfields=all&show-tags=all&show-references=all&api-key=2a37502e-d1fd-4be3-a8bc-4dfb17760157&page-size=10";

    private static final SimpleDateFormat SDF = new SimpleDateFormat("MMM dd, yyyy, h:mm");
    private static final SimpleDateFormat SDF_URL = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat SDF_PUB = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final String CONTRIBUTOR_TAG = "contributor";

    public static Integer totalPages = -1;

    public static String getUrl(int page){
        StringBuilder sb = new StringBuilder();
        sb.append(GUARDIAN_BASE_URL);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -3);
        Date threeDaysAgo = cal.getTime();
        String dateString = formateUrlDate(threeDaysAgo);
        sb.append("&from-date=").append(dateString);
        sb.append("&page=").append(page);
        return sb.toString();
    }

    public static List<News> fetchNews(String urlString) {
        URL url = createUrl(urlString);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        List<News> newsList = extractNews(jsonResponse);

        return newsList;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static List<News> extractNews(String jsonResponse) {

        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        List<News> newsList = new ArrayList<>();

        try {

            JSONObject root = new JSONObject(jsonResponse);

            JSONObject response = root.getJSONObject("response");

            if (totalPages == -1) {
                totalPages = response.getInt("pages");
            }

            JSONArray results = response.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                String sectionName = result.getString("sectionName");
                String title = result.getString("webTitle");
                String webUrl = result.getString("webUrl");
                String publicationDate = result.optString("webPublicationDate");
                Date pubDate = getPublicationDate(publicationDate);

                String authorString = null;
                JSONArray tags = result.optJSONArray("tags");
                if (tags != null) {
                    List<String> authors = new ArrayList<>();
                    for (int j = 0; j < tags.length(); j++) {
                        JSONObject tag = tags.getJSONObject(j);
                        String tagType = tag.getString("type");
                        if (CONTRIBUTOR_TAG.equals(tagType)) {
                            authors.add(tag.getString("webTitle"));
                        }
                    }
                    if (!authors.isEmpty()) {
                        authorString = TextUtils.join(", ", authors);
                    }
                }

                newsList.add(new News(title, sectionName, webUrl, authorString, pubDate));
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the news JSON results", e);
        }

        return newsList;
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return SDF.format(date);
    }

    private static String formateUrlDate(Date date) {
        if (date == null) {
            return "";
        }
        return SDF_URL.format(date);
    }

    private static Date getPublicationDate(String date) {
        if (date == null) {
            return null;
        }
        try {
            Date pubDate = SDF_PUB.parse(date);
            return pubDate;
        } catch (ParseException e) {
            return null;
        }
    }
}
