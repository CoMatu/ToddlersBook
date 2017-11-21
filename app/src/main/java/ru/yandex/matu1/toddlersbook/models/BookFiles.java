package ru.yandex.matu1.toddlersbook.models;

import android.net.Uri;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class BookFiles {
    @SerializedName("bookID")
    @Expose
    private Integer bookID;

    @SerializedName("pagesPath")
    @Expose
    private ArrayList<Uri> pagesPath = null;
    @SerializedName("soundsPath")
    @Expose
    private ArrayList<Uri> soundsPath = null;


    public ArrayList<Uri> getPagesPath() {
        return pagesPath;
    }

    public void setPagesPath(ArrayList<Uri> pagesPath) {
        this.pagesPath = pagesPath;
    }

    public ArrayList<Uri> getSoundsPath() {
        return soundsPath;
    }

    public void setSoundsPath(ArrayList<Uri> soundsPath) {
        this.soundsPath = soundsPath;
    }


    public Integer getBookID() {
        return bookID;
    }

    public void setBookID(Integer bookID) {
        this.bookID = bookID;
    }


}
