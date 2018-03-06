package ru.yandex.matu1.toddlersbook.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class BookFiles {
    @SerializedName("bookID")
    @Expose
    private Integer bookID;
    @SerializedName("pagesPath")
    @Expose
    private ArrayList<String> pagesPath = null;
    @SerializedName("soundsPath")
    @Expose
    private ArrayList<String> soundsPath = null;
    @SerializedName("price")
    @Expose
    private Integer price;

    public ArrayList<String> getPagesPath() {
        return pagesPath;
    }

    public void setPagesPath(ArrayList<String> pagesPath) {
        this.pagesPath = pagesPath;
    }

    public ArrayList<String> getSoundsPath() {
        return soundsPath;
    }

    public void setSoundsPath(ArrayList<String> soundsPath) {
        this.soundsPath = soundsPath;
    }

    public Integer getBookID() {
        return bookID;
    }

    public Integer getPrice() {
        return price;
    }

    public void setBookID(Integer bookID) {
        this.bookID = bookID;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }


}
