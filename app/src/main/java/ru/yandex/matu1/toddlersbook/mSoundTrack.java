package ru.yandex.matu1.toddlersbook;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;


public class mSoundTrack {
    private MediaPlayer track;
    private boolean playable;
//    private String name;

    /**
     * Конструктор
     *
     * @param cont     - контекст
     * @param soundUri - идентификатор ресурса
 //    * @param name     - имя трека
     */
    public mSoundTrack(Context cont, Uri soundUri) {
        this.playable = true;
        this.track = MediaPlayer.create(cont, soundUri);
//        this.name = name;
    }

    /**
     * запускает трек
     */
    public void start() {
        if (!track.isPlaying())
            track.start();
        this.stop();
    }

    /**
     * перезапускает звук независимо от того играется он в данный момент или нет
     */
    public void forceStart() {
        if (track.isPlaying()) {
            track.pause();
            track.seekTo(0);
            track.start();
        } else {
            track.start();
        }

    }

    /**
     * приостанавливает проигрывание звука! ВНИМАНИЕ!!! Не высвобождает память,
     * не отсоединяется от потока... просто приостанавливает!
     */
    public void stop() {
        if (track.isPlaying())
            track.pause();
    }

    /**
     * перематывает трек на начало
     */
    public void rewind() {
        this.stop();
        track.seekTo(0);
        this.start();
    }

    /**
     * @return возвращает параметр playable
     */
    public boolean isPlayable() {
        return playable;
    }

    /**
     * @param playable - истина, если трек должен играться при проигрывании ложь в
     *                 противном случае
     */
    public void setPlayable(boolean playable) {
        this.playable = playable;
    }

/*
    public String getName() {
        return name;
    }
*/

}
