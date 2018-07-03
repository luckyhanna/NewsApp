package com.example.hannabotar.newsapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>> {

    private static final int NEWS_LOADER_ID = 1;

    @BindView(R.id.list)
    ListView mListView;

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    @BindView(R.id.empty_view)
    TextView mEmptyView;

    @BindView(R.id.reload_text)
    TextView mReload;

    @BindView(R.id.current_page_text)
    TextView mPageText;

    @BindView(R.id.previous_page_btn)
    Button mPrevPage;
    @BindView(R.id.next_page_btn)
    Button mNextPage;

    @BindView(R.id.first_page_btn)
    Button mFirstPage;
    @BindView(R.id.last_page_btn)
    Button mLastPage;

    @BindView(R.id.pager_layout)
    LinearLayout mPager;

    private NewsAdapter mAdapter;

    private int mCurrentPage = 1;

    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        ButterKnife.bind(this);

        mListView.setEmptyView(mEmptyView);

        mAdapter = new NewsAdapter(this, new ArrayList<News>());

        mListView.setAdapter(mAdapter);

        mPager.setVisibility(View.GONE);
        mReload.setVisibility(View.GONE);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                News currentNews = mAdapter.getItem(position);
                Uri newsUri = Uri.parse(currentNews.getWebUrl());
                Intent webIntent = new Intent(Intent.ACTION_VIEW, newsUri);
                startActivity(webIntent);
            }
        });

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        checkNetworkInfo();

        mPrevPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentPage > 1) {
                    mCurrentPage = mCurrentPage - 1;
                }
                disableButtons();
                reloadData();
            }
        });
        mNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentPage < NewsUtils.totalPages) {
                    mCurrentPage = mCurrentPage + 1;
                    disableButtons();
                    reloadData();
                }
            }
        });

        mFirstPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPage = 1;
                disableButtons();
                reloadData();
            }
        });
        mLastPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPage = NewsUtils.totalPages;
                disableButtons();
                reloadData();
            }
        });

        mReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadData();
            }
        });
    }

    private void checkNetworkInfo() {
        if (checkInternet()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            showNoInternet();
        }
    }

    private boolean checkInternet() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void showNoInternet() {
        mProgressBar.setVisibility(View.GONE);
        mPager.setVisibility(View.GONE);
        mAdapter.clear();
        mEmptyView.setText(R.string.no_internet_connection);
        mReload.setVisibility(View.VISIBLE);
    }

    private void disableButtons() {
        mFirstPage.setEnabled(false);
        mPrevPage.setEnabled(false);
        mNextPage.setEnabled(false);
        mLastPage.setEnabled(false);
    }

    private void reloadData() {
        if (!checkInternet()) {
            showNoInternet();
            return;
        }
        mReload.setVisibility(View.GONE);
        mEmptyView.setText(R.string.empty_string);
        getLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<List<News>> onCreateLoader(int id, Bundle args) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String orderBy  = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        String pageSize  = sharedPrefs.getString(
                getString(R.string.settings_page_key),
                getString(R.string.settings_page_default)
        );

        String fromDateOption = sharedPrefs.getString(
                getString(R.string.settings_from_date_key),
                getString(R.string.settings_from_date_default)
        );

        String fromDate = NewsUtils.getDateStringForPreference(Integer.parseInt(fromDateOption));

        Uri baseUri = Uri.parse(NewsUtils.GUARDIAN_BASE_URL);

        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("api-key", "2a37502e-d1fd-4be3-a8bc-4dfb17760157");
        uriBuilder.appendQueryParameter("format", "json");
        uriBuilder.appendQueryParameter("q", "technology");
        uriBuilder.appendQueryParameter("showfields", "all");
        uriBuilder.appendQueryParameter("show-tags", "all");
        uriBuilder.appendQueryParameter("show-references", "all");
        uriBuilder.appendQueryParameter("page", String.valueOf(mCurrentPage));
        uriBuilder.appendQueryParameter("page-size", pageSize);
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("from-date", fromDate);

        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> data) {
        mProgressBar.setVisibility(View.GONE);

        mPager.setVisibility(View.VISIBLE);

        if (mCurrentPage == 1) {
            mPrevPage.setEnabled(false);
            mFirstPage.setEnabled(false);
        } else {
            mPrevPage.setEnabled(true);
            mFirstPage.setEnabled(true);
        }

        if (mCurrentPage == NewsUtils.totalPages) {
            mNextPage.setEnabled(false);
            mLastPage.setEnabled(false);
        } else {
            mNextPage.setEnabled(true);
            mLastPage.setEnabled(true);
        }

        mAdapter.clear();
        mReload.setVisibility(View.GONE);
        if (data != null && !data.isEmpty()) {
            mAdapter.addAll(data);
            mPager.setVisibility(View.VISIBLE);
            mPageText.setText(String.valueOf(mCurrentPage));
        } else {
            mPager.setVisibility(View.GONE);
            mEmptyView.setText(R.string.no_news_found);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        mAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
