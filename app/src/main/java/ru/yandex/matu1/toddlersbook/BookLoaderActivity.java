package ru.yandex.matu1.toddlersbook;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class BookLoaderActivity extends AppCompatActivity {

    private ArrayList<Uri> BookPages;
    private ArrayList<Uri> BookSounds;
    static final String TAG = "myLogs";
    private int bookId;

    // "http://human-factors.ru/todbook/book1.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_loader);

        BookUriFromId();

        new BookLoader().execute();
    }

    private class BookLoader extends AsyncTask<Void, Void, String>{

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJsonBook = "";
        private String bookIdJson = "http://human-factors.ru/todbook/book"+bookId+".json";

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(bookIdJson);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJsonBook = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJsonBook;
        }

        @Override
        protected void onPostExecute(String jsBook) {
            super.onPostExecute(jsBook);
        }
    }

    private void BookUriFromId(){
        //получаем номер ID книги, с обложки которой перешли в слайдер
        Intent intent = getIntent();
        bookId = intent.getIntExtra("bookId", 1);
        Log.d(TAG, "You read book №" + bookId);
    }
}
