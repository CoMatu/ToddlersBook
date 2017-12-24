package ru.yandex.matu1.toddlersbook;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ToggleButton;


import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

import ru.yandex.matu1.toddlersbook.animations.ZoomOutPageTransformer;
import ru.yandex.matu1.toddlersbook.models.BookFiles;

import static ru.yandex.matu1.toddlersbook.MyPlayer.mp;

public class SliderActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    static final String TAG = "myLogs";
    ViewPager viewPager;
    CustomSwipeAdapter adapter;
    ToggleButton toggleButton;
    ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_slider);
        imageButton = (ImageButton) findViewById(R.id.imageButtonHome);
        View.OnClickListener clickHome = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mp !=null && mp.isPlaying()){
                    mp.stop();
                }
                GoHome();
            }
        };

        imageButton.setOnClickListener(clickHome);
/*
 * Читаем json и создаем из него объект книги bookFiles
 */
        Gson gson = new Gson();
        int bookId = GetBookId();
        String fileName = "book_" + bookId + ".json";
        String gsResult = MyJSON.getData(getApplicationContext(), fileName);
        BookFiles bookFiles = gson.fromJson(gsResult, BookFiles.class);

        final String folderB = "bookfiles_" + bookId;
        /*
        Получаем из объекта bookFiles массивы путей к файлам книги
         */
        ArrayList<String> pagesFiles = bookFiles.getPagesPath();
        final ArrayList<String> soundsFiles = bookFiles.getSoundsPath();

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        adapter = new CustomSwipeAdapter(this, pagesFiles);
        viewPager.setAdapter(adapter);
        toggleButton = (ToggleButton) findViewById(R.id.imageButtonPlay);

        //слушаем номер слайда при перелистывании
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                String nameS = Uri.parse(soundsFiles.get(position)).getLastPathSegment();
                if(mp != null){
                    mp.stop();
                }
                getMedia(nameS, folderB);
                toggleButton.setOnCheckedChangeListener(SliderActivity.this);

                if (toggleButton.isChecked()){
                    mp.start();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        ViewPager.OnPageChangeListener listener = new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                String nameS = Uri.parse(soundsFiles.get(position)).getLastPathSegment();
                if(mp != null){
                    mp.stop();
                }
                getMedia(nameS, folderB);
                toggleButton.setOnCheckedChangeListener(SliderActivity.this);

                if (toggleButton.isChecked()){
                    mp.start();
                }
            }
        };

        viewPager.addOnPageChangeListener(listener);

        listener.onPageSelected(0);

    }

    public int GetBookId() {
        Intent intent = getIntent();
        int bookId = intent.getIntExtra("bookId", 1);
        return bookId;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) mp.start();
        else if (mp.isPlaying()) {
            mp.pause();
        }

    }

    public void getMedia(String nameS, String folderB) {
        try {
            final String soundPath = String.valueOf(getApplicationContext().getExternalFilesDir(folderB));
            final Uri souF = Uri.fromFile(new File(soundPath, nameS));
            final MediaPlayer mp = MyPlayer.getMp(getApplicationContext(), souF);
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void GoHome() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SliderActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 10);
    }

}
