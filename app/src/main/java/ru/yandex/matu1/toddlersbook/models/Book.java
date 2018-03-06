package ru.yandex.matu1.toddlersbook.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Book {
    @SerializedName("bookID")
    @Expose
    private Integer bookID;
    @SerializedName("pageUrl")
    @Expose
    private List<String> pageUrl = null;
    @SerializedName("soundUrl")
    @Expose
    private List<String> soundUrl = null;
    @SerializedName("price")
    @Expose
    private Integer price;

    public Integer getBookID() {
        return bookID;
    }

    public void setBookID(Integer bookID) {
        this.bookID = bookID;
    }

    public List<String> getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(List<String> pageUrl) {
        this.pageUrl = pageUrl;
    }

    public List<String> getSoundUrl() {
        return soundUrl;
    }

    public void setSoundUrl(List<String> soundUrl) {
        this.soundUrl = soundUrl;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

}
