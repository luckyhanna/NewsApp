package com.example.hannabotar.newsapp;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Created by hanna on 7/1/2018.
 */

public class NewsLoader extends AsyncTaskLoader<List<News>> {

    private String mUrl;

    public NewsLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    public List<News> loadInBackground() {
        if (mUrl == null) {
            return null;
        }
        return NewsUtils.fetchNews(mUrl);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}
