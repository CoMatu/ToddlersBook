package ru.yandex.matu1.toddlersbook;

import android.net.Uri;

import java.util.ArrayList;

public class Book {
    ArrayList<Uri> BookPages;
    ArrayList<Uri> BookSounds;

    public Book(ArrayList<Uri> bookPages, ArrayList<Uri> bookSounds) {
        BookPages = bookPages;
        BookSounds = bookSounds;
    }

    public ArrayList<Uri> getBookPages() {
        return BookPages;
    }

    public void setBookPages(ArrayList<Uri> bookPages) {
        BookPages = bookPages;
    }

    public ArrayList<Uri> getBookSounds() {
        return BookSounds;
    }

    public void setBookSounds(ArrayList<Uri> bookSounds) {
        BookSounds = bookSounds;
    }
}
