package ru.yandex.matu1.toddlersbook;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.tonyodev.fetch.Fetch;
import com.tonyodev.fetch.request.Request;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ru.yandex.matu1.toddlersbook.models.Book;
import ru.yandex.matu1.toddlersbook.models.BookFiles;

public class BookLoaderActivity extends AppCompatActivity {

    static final String TAG = "myLogs";
    private int bookId;
    boolean CheckBookStorage = false;
    String fileBookStorage;

    Fetch mFetch;

    // "http://human-factors.ru/todbook/book1.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_loader);

        BookUriFromId();

        fileBookStorage = "book_"+bookId+".json";
/*
        CheckBookFile(fileBookStorage);
        if(CheckBookStorage){
            NextActivity();
        }
*/
        File fileOfBook = new File(getApplicationContext().getFilesDir().getPath() + "/" + fileBookStorage);

        if (fileOfBook.exists()) {
        NextActivity();
        }

        BookLoader bookLoader = new BookLoader();
        bookLoader.execute();
        ArrayList<String> pagesPath;
        ArrayList<String> soundsPath;
        pagesPath = new ArrayList<>();
        soundsPath = new ArrayList<>();
        List<Request> requestListPages = new ArrayList<>();
        List<Request> requestListSounds = new ArrayList<>();
        String folderB = "bookfiles_"+bookId;

        File bookfolder = new File(String.valueOf(getExternalFilesDir(folderB)));
        Log.d("my2", String.valueOf(bookfolder));

        if(!bookfolder.exists()){
            bookfolder.mkdirs();
            Log.d("my", "dir. created");
            }
            else {
            Log.d("my", "dir. already exists");
        }

        try {
            String result = bookLoader.get();
            Gson gson = new Gson();
            Book book = gson.fromJson(result, Book.class);
//            Log.d(TAG, result);
            List<String> pages = book.getPageUrl();
            mFetch = Fetch.newInstance(this);
            mFetch.removeRequests(); //чистим базу запросов

//            String pagesUrl = String.valueOf(pages);

            for(int i=0; i<pages.size(); i++){
                String url = pages.get(i);
                String path = String.valueOf(bookfolder);
                String fileName = Uri.parse(url).getLastPathSegment();
            Log.d("my2", fileName);
                Request request = new Request(url, path, fileName);
                requestListPages.add(request);
                String pageFilePath = path+"/"+fileName;
                Log.d("my2", pageFilePath);
                pagesPath.add(pageFilePath);
            }
            List<Long> idsPages = mFetch.enqueue(requestListPages);

            //            Log.d(TAG, String.valueOf(pages));
            List<String> sounds = book.getSoundUrl();
//            String soundsUrl = String.valueOf(sounds);

            for(int i=0; i<sounds.size(); i++){
                String urlS = sounds.get(i);
                String path = String.valueOf(bookfolder);
                String fileNameS = Uri.parse(urlS).getLastPathSegment();
                Log.d("my2", fileNameS);
                Request requestS = new Request(urlS, path, fileNameS);
                requestListSounds.add(requestS);
                String soundFilePath = path+"/"+fileNameS;
                Log.d("my2", soundFilePath);
                soundsPath.add(soundFilePath);
            }

            List<Long> idsSound = mFetch.enqueue(requestListSounds);

            BookFiles bookFiles = new BookFiles();
            bookFiles.setBookID(bookId);
            bookFiles.setPagesPath(pagesPath);
            bookFiles.setSoundsPath(soundsPath);

            Gson gson11 = new Gson();
            String filesJson = gson11.toJson(bookFiles);

            MyJSON.saveData(this, filesJson, fileBookStorage);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        NextActivity();
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

    }

    private void BookUriFromId(){
        //получаем номер ID книги, с обложки которой перешли в слайдер
        Intent intent = getIntent();
        bookId = intent.getIntExtra("bookId", 1);
        Log.d(TAG, "You read book №" + bookId);
    }

    private void CheckBookFile(String fileBookStorage) {
        File fileOfBook = new File(getApplicationContext().getFilesDir().getPath() + "/" + fileBookStorage);

        if (fileOfBook.exists()) {

            CheckBookStorage = true;

        }

    }

    private void NextActivity() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(BookLoaderActivity.this, SliderActivity.class);
                startActivity(intent);
                finish();
            }
        }, 20);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFetch.release();
    }

}
