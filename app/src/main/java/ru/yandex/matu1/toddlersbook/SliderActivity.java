package ru.yandex.matu1.toddlersbook;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


import com.google.gson.Gson;

import java.util.ArrayList;

import ru.yandex.matu1.toddlersbook.animations.ZoomOutPageTransformer;
import ru.yandex.matu1.toddlersbook.models.BookFiles;

public class SliderActivity extends AppCompatActivity {
    static final String TAG = "myLogs";
    ViewPager viewPager;
    CustomSwipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider);
/*
 * Читаем json и создаем из него объект книги bookFiles
 */
        Gson gson = new Gson();
        int bookId = GetBookId();
        String fileName = "book_" + bookId + ".json";
        String gsResult = MyJSON.getData(getApplicationContext(), fileName);
        BookFiles bookFiles = gson.fromJson(gsResult, BookFiles.class);

        String folderB = "bookfiles_" + bookId;
        /*
        Получаем из объекта bookFiles массивы путей к файлам книги
         */
        ArrayList<String> pagesFiles = bookFiles.getPagesPath();
        ArrayList<String> soundsFiles = bookFiles.getSoundsPath();

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        adapter = new CustomSwipeAdapter(this, pagesFiles, soundsFiles, folderB);
        viewPager.setAdapter(adapter);


        //слушаем номер слайда при перелистывании
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {


            }

            @Override
            public void onPageScrollStateChanged(int state) {
                final MediaPlayer mp = MyPlayer.mp;
                if (ViewPager.SCROLL_STATE_IDLE == state) {

                    if (mp.isPlaying()) {
                        mp.pause();
                    }
                } else {

                    if (mp.isPlaying()) {
                        mp.pause();
                    }
                }
            }
        });

    }

    public int GetBookId() {
        Intent intent = getIntent();
        int bookId = intent.getIntExtra("bookId", 1);
//        Log.d(TAG, "You read book №" + bookId);
        return bookId;
    }

}
