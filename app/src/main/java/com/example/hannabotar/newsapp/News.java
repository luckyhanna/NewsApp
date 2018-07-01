package com.example.hannabotar.newsapp;

import java.util.Date;

/**
 * Created by hanna on 7/1/2018.
 */

public class News {

    private String title;
    private String sectionName;
    private String author;
    private Date published;
    private String webUrl;

    public News(String title, String sectionName, String webUrl, String author, Date published) {
        this.title = title;
        this.sectionName = sectionName;
        this.webUrl = webUrl;
        this.author = author;
        this.published = published;
    }

    public String getTitle() {
        return title;
    }

    public String getSectionName() {
        return sectionName;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getAuthor() {
        return author;
    }

    public Date getPublished() {
        return published;
    }
}
