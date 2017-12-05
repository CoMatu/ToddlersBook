package ru.yandex.matu1.toddlersbook;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

public class MyPlayer {
    public static MediaPlayer mp;
    Context cont;
    Uri soundUri;

    public MyPlayer(Context cont, Uri soundUri) {
        this.cont = cont;
        this.soundUri = soundUri;
    }

    public static MediaPlayer getMp(Context cont, Uri soundUri) {
        mp = MediaPlayer.create(cont, soundUri);
        return mp;
    }

}
