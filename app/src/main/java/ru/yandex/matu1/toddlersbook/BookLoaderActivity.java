package ru.yandex.matu1.toddlersbook;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.ThinDownloadManager;

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

    // "http://human-factors.ru/todbook/book1.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_loader);

        BookUriFromId();

        fileBookStorage = "book_"+bookId+".json";
        CheckBookFile(fileBookStorage);
        if(CheckBookStorage){
            NextActivity();
        }
        BookLoader bookLoader = new BookLoader();
        bookLoader.execute();
        ArrayList<Uri> pagesPath;
        ArrayList<Uri> soundsPath;
        pagesPath = new ArrayList<>();
        soundsPath = new ArrayList<>();
        try {
            String result = bookLoader.get();
            Gson gson = new Gson();
            Book book = gson.fromJson(result, Book.class);
//            Log.d(TAG, result);
            List<String> pages = book.getPageUrl();
            String pagesUrl = String.valueOf(pages);

            for(int i=0; i<pages.size(); i++){
                Uri uri = Uri.parse(pages.get(i));
                String fileN = uri.getLastPathSegment();
                String filenam = getApplicationContext().getFilesDir()
                        + File.separator + "book" + bookId +"_"+ fileN;
            Log.d(TAG, filenam);
                ThinDownloadManager downloadManager = new ThinDownloadManager(5); //количество потоков загрузки
                Uri destinationUri = Uri.parse(filenam);
                DownloadRequest downloadRequest = new DownloadRequest(uri).setDestinationURI(destinationUri);
                downloadManager.add(downloadRequest);
                pagesPath.add(destinationUri);
            }

//            Log.d(TAG, String.valueOf(pages));
            List<String> sounds = book.getSoundUrl();
            String soundsUrl = String.valueOf(sounds);

            for(int i=0; i<sounds.size(); i++){
                Uri uri = Uri.parse(sounds.get(i));
                String fileNS = uri.getLastPathSegment();
                String filenamS = getApplicationContext().getFilesDir()
                        + File.separator + "book_" + bookId + fileNS;
                Log.d(TAG, filenamS);
                ThinDownloadManager downloadManager = new ThinDownloadManager(5); //количество потоков загрузки
                Uri destinationUri = Uri.parse(filenamS);
                DownloadRequest downloadRequest = new DownloadRequest(uri).setDestinationURI(destinationUri);
                downloadManager.add(downloadRequest);
                soundsPath.add(destinationUri);
            }
            BookFiles bookFiles = new BookFiles();
            bookFiles.setBookID(bookId);
            bookFiles.setPagesPath(pagesPath);
            bookFiles.setSoundsPath(soundsPath);

            Gson gson11 = new Gson();
            String filesJson = gson11.toJson(bookFiles);

            MyJSON.saveData(this, filesJson, fileBookStorage);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
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

}
