package ru.yandex.matu1.toddlersbook;

import java.util.ArrayList;

public interface CallbackCoverPaths {
    void onPreExecute();
    void onPostExecute(ArrayList<String> fileBookCover);
}
