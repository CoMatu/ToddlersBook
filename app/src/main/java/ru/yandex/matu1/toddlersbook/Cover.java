package ru.yandex.matu1.toddlersbook;

import android.net.Uri;

/**
 * Created by matu1 on 28.10.17.
 */

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

