package ru.yandex.matu1.toddlersbook;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class SliderActivity extends AppCompatActivity {
    static final String TAG = "myLogs";
    ViewPager viewPager;
    CustomSwipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider);

        viewPager = (ViewPager)findViewById(R.id.view_pager);
        adapter = new CustomSwipeAdapter(this);
        viewPager.setAdapter(adapter);

        //слушаем номер слайда при перелистывании
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int page_number = position+1;
                Log.d(TAG, "onPageSelected, position = " + page_number);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //получаем номер ID книги, с обложки которой перешли в слайдер
        Intent intent = getIntent();
        int bookId = intent.getIntExtra("bookId", 1);
        Log.d(TAG, "You read book №" + bookId);
    }

}
