package com.example.hannabotar.newsapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hanna on 7/1/2018.
 */

public class NewsAdapter extends ArrayAdapter<News> {

    @BindView(R.id.section_text)
    TextView mSectionText;

    @BindView(R.id.title)
    TextView mTitle;

    @BindView(R.id.date_published)
    TextView mDatePublished;

    @BindView(R.id.author)
    TextView mAuthor;

    @BindView(R.id.separator_text)
    TextView mSeparator;

    public NewsAdapter(@NonNull Context context, @NonNull List<News> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.news_item, parent, false);
        }

        ButterKnife.bind(this, convertView);

        final News currentNews = getItem(position);

        mSectionText.setText(currentNews.getSectionName());
        mTitle.setText(currentNews.getTitle());
        mDatePublished.setText(NewsUtils.formatDate(currentNews.getPublished()));
        mAuthor.setText(currentNews.getAuthor());
        if (currentNews.getPublished() != null && currentNews.getAuthor() != null) {
            mSeparator.setText(R.string.separator);
        } else {
            mSeparator.setText(R.string.empty_string);
        }

        return convertView;
    }
}
