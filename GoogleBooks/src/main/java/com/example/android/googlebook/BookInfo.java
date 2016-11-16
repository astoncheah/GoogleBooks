package com.example.android.googlebook;

/**
 * Created by cheah on 19/10/16.
 */

public class BookInfo {
    String title;
    String authors;
    String infoLink;
    String publishedDate;

    public BookInfo(String title, String authors, String infoLink, String publishedDate) {
        this.title = title;
        this.authors = authors;
        this.infoLink = infoLink;
        this.publishedDate = publishedDate;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthors() {
        return authors;
    }

    public String getInfoLink() {
        return infoLink;
    }

    public String getPublishedDate() {
        return publishedDate;
    }
}
