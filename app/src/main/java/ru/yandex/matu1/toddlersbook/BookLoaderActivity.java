package ru.yandex.matu1.toddlersbook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tonyodev.fetch.Fetch;
import com.tonyodev.fetch.listener.FetchListener;
import com.tonyodev.fetch.request.Request;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
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
    private FirebaseAnalytics mFirebaseAnalytics;

    private final int MY_PERMISSIONS_REQUEST_CODE = 1;


    // "http://human-factors.ru/todbook/book1.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Type itemsListType = new TypeToken<List<String>>() {}.getType();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_loader);
        BookUriFromId();

        String folderB = "bookfiles_" + bookId;

        String fileBookStorage = "book_" + bookId + ".json";
        File fileOfBook = new File(this.getFilesDir().getPath() + File.separator + fileBookStorage);

        if (fileOfBook.exists()) {
            NextActivity();
        } else {

            BookLoader bookLoader = new BookLoader();
            bookLoader.execute();

            ArrayList<String> pagesPath;
            ArrayList<String> soundsPath;
//            pagesPath = new ArrayList<>();
            soundsPath = new ArrayList<>();

            File bookfolder = new File(String.valueOf(getExternalFilesDir(folderB)));
//            Log.d("my2", String.valueOf(bookfolder));

            if (!bookfolder.exists()) {
                bookfolder.mkdirs();
                Log.d("my", "dir. created");
            }
/*
            else {
                Log.d("my", "dir. already exists");
            }
*/

            try {
                String result = bookLoader.get();
                Gson gson = new Gson();
                Book book = gson.fromJson(result, Book.class);
                List<String> pages = book.getPageUrl();
                String[] urlsPages = pages.toArray(new String[0]);

                FileLoader fileLoader = new FileLoader();
                fileLoader.execute(urlsPages);
                String flResult = fileLoader.get();

                pagesPath = new Gson().fromJson(flResult, itemsListType);

                BookFiles bookFiles = new BookFiles();
                bookFiles.setBookID(bookId);
                bookFiles.setPagesPath(pagesPath);
                bookFiles.setSoundsPath(soundsPath);

                Gson gson11 = new Gson();
                String filesJson = gson11.toJson(bookFiles);

                if (checkPermissions()) {
                    Toast.makeText(BookLoaderActivity.this, "Разрешения уже получены", Toast.LENGTH_SHORT).show();
                } else {
                    setPermissions();
                }

                MyJSON.saveData(this, filesJson, fileBookStorage);

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            NextActivity();
        }
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
        protected void onPreExecute() {
            super.onPreExecute();
        }

    }

    private class FileLoader extends AsyncTask<String, Void, String>{
        Fetch mFetch;
        String folderB = "bookfiles_" + bookId;
        File bookfolder = new File(String.valueOf(getExternalFilesDir(folderB)));
        List<Request> requestListPages = new ArrayList<>();
        ArrayList<String> pagesPath = new ArrayList<>();
        String resultD;

        @Override
        protected String doInBackground(String... urlsFiles) {

            mFetch = Fetch.newInstance(getApplicationContext());
            mFetch.removeRequests(); //чистим базу запросов

            for(int i=0; i<urlsFiles.length; i++){
                String url = urlsFiles[i];
                String path = String.valueOf(bookfolder);
                String fileName = Uri.parse(url).getLastPathSegment();
                Log.d("my2", fileName);
                Request request = new Request(url, path, fileName);
                requestListPages.add(request);
                String pageFilePath = path + "/" + fileName;
                Log.d("my2", pageFilePath);
                pagesPath.add(pageFilePath);
            }

            mFetch.enqueue(requestListPages);
//            resultD = String.valueOf(pagesPath);
            String resultD = new Gson().toJson(pagesPath);
            return resultD;

        }

    }

    private void BookUriFromId(){
        //получаем номер ID книги, с обложки которой перешли в слайдер
        Intent intent = getIntent();
        bookId = intent.getIntExtra("bookId", 1);
        Log.d(TAG, "You read book №" + bookId);
    }

    private void NextActivity() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(BookLoaderActivity.this, SliderActivity.class);
                intent.putExtra("bookId", bookId); // передаю в слайдер номер книги

                startActivity(intent);
                finish();
            }
        }, 20);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != MY_PERMISSIONS_REQUEST_CODE) {
            return;
        }
        boolean isGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
                break;
            }
        }

        if (isGranted) {
            Toast.makeText(BookLoaderActivity.this, "Разрешения получены", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "В разрешениях отказано", Toast.LENGTH_LONG).show();

        }
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    private void setPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CODE);
    }

}
