package ru.yandex.matu1.toddlersbook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tonyodev.fetch.Fetch;
import com.tonyodev.fetch.request.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamCorruptedException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ru.yandex.matu1.toddlersbook.models.Book;
import ru.yandex.matu1.toddlersbook.models.BookFiles;

public class BookCardActivity extends AppCompatActivity {
    static final String TAG = "myLogs";
    private int bookId;
    private String fileNamePath = "filesPath.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_card);

        BookUriFromId();

        String covers = MyJSON.getData(this, fileNamePath);
        ArrayList<String> coversPaths = getFilesPathFromFile(covers);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        int posit = bookId - 1;
        File imgFile = new File(coversPaths.get(posit));
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        imageView.setImageBitmap(myBitmap);

        Button buttonDownload = (Button) findViewById(R.id.button);
        Button buttonRead = (Button) findViewById(R.id.button2);

        String fileListBook = "list_" + "book_" + bookId + ".json";

//        File f = new File(getApplicationContext().getFilesDir().getPath() + "/" + fileListBook);
        String filePath = getApplicationContext().getFilesDir().getPath() + "/" + fileListBook;

        /**
         * Загрузим json с url файлов книги
         */

        Thread jsDownload = new Thread(new Runnable() {
            @Override
            public void run() {
                BookLoader();
            }
        });
        jsDownload.start(); // запустили поток 1

        try {

        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * Обрабатываем нажатие кнопки "Загрузить" и грузим файлы книги
         */

        buttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String fileListB = "list_" + "book_" + bookId + ".json";
                        String jsReadFile = MyJSON.getData(getApplicationContext(), fileListB);
                        Log.d(TAG, jsReadFile);
                        Gson gson = new Gson();
                        Book book = gson.fromJson(jsReadFile, Book.class);
                        List<String> pages = book.getPageUrl();
                        String[] urlsPages = pages.toArray(new String[0]);
                        DownloadFilesBook(urlsPages);
                    }
                });
                mThread.start(); // запустили поток 2
            }
        });

        /**
         * Обрабатываем нажатие кнопки "Читать"
         */
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NextActivity();
            }
        });

    }

    private void BookUriFromId() {
        //получаем номер ID книги, с обложки которой перешли в слайдер
        Intent intent = getIntent();
        bookId = intent.getIntExtra("bookId", 1);
        Log.d(TAG, "You read book №" + bookId);
    }

    private void NextActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(BookCardActivity.this, SliderActivity.class);
                intent.putExtra("bookId", bookId); // передаю в слайдер номер книги

                startActivity(intent);
                finish();
            }
        }, 20);
    }

    private ArrayList<String> getFilesPathFromFile(String jsResult) {
        ArrayList<String> urisImg = new ArrayList<>();
        try {
            JSONArray rootJson = new JSONArray(new JSONTokener(jsResult));
            for (int i = 0; i < rootJson.length(); i++) {
                JSONObject o = rootJson.getJSONObject(i);
                String strTo = (String) o.get("uriString");
                urisImg.add(strTo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return urisImg;
    }

    private void BookLoader() {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJsonServer = "";
        String fileBook = "list_" + "book_" + bookId + ".json";

        try {
            String jsUrl = "http://human-factors.ru/todbook/book" + bookId + ".json";
            URL url = new URL(jsUrl);
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
            resultJsonServer = buffer.toString();
            MyJSON.saveData(getApplicationContext(), resultJsonServer, fileBook);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void DownloadFilesBook(String[] urlsFiles) {
        Fetch mFetch;
        String folderB = "bookfiles_" + bookId;
        String fileNameForWrite = "book_" + bookId + ".json";

        File bookfolder = new File(String.valueOf(getExternalFilesDir(folderB)));
        List<Request> requestListPages = new ArrayList<>();
        ArrayList<String> pagesPath = new ArrayList<>();
        String resultD;
        mFetch = Fetch.newInstance(getApplicationContext());
        mFetch.removeRequests(); //чистим базу запросов

        for (int i = 0; i < urlsFiles.length; i++) {
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

        BookFiles bookFiles = new BookFiles();
        bookFiles.setBookID(bookId);
        bookFiles.setPagesPath(pagesPath);
//      bookFiles.setSoundsPath(soundsPath);
        Gson gson11 = new Gson();
        String filesJson = gson11.toJson(bookFiles);

        MyJSON.saveData(getApplicationContext(), filesJson, fileNameForWrite);
    }

/*
    private class FileLoader extends AsyncTask<String, Void, String> {
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

            for (int i = 0; i < urlsFiles.length; i++) {
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
            resultD = new Gson().toJson(pagesPath);
            return resultD;

        }

    }

    private String GetJson(String fileListBook) {
        try {
            File f = new File(getApplicationContext().getFilesDir().getPath() + "/" + fileListBook);
            //check whether file exists
            if (!f.exists()) {
                return null;
            }
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer);

        } catch (IOException e) {
            Log.e("TAG", "Error in Reading: " + e.getLocalizedMessage());
            return null;
        }
    }

    public static String getStringFromFile(String filePath) throws Exception {
        String jString;
        File yourFile = new File(filePath);
        FileInputStream stream = new FileInputStream(yourFile);
        FileChannel fc = stream.getChannel();
        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        jString = Charset.defaultCharset().decode(bb).toString();
        stream.close();
        return jString;
    }
*/

}
