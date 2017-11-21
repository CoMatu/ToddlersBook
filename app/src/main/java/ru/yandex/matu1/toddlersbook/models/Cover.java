package ru.yandex.matu1.toddlersbook.models;

import android.net.Uri;

public class Cover {
    Uri fileBookCover;

    public Cover(Uri fileBookCover) {
        this.fileBookCover = fileBookCover;
    }

    public Uri getFileBookCover() {
        return fileBookCover;
    }

    public void setFileBookCover(Uri fileBookCover) {
        this.fileBookCover = fileBookCover;
    }
}

