package ru.yandex.matu1.toddlersbook;

import android.media.MediaPlayer;

public class MyPlayer {
    static MediaPlayer mp;

    public static MediaPlayer getMediaPlayer()
    {
        if (mp == null)
        {
            mp = new MediaPlayer();
        }

        return mp;
    }
}
