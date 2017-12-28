package ru.yandex.matu1.toddlersbook;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.tonyodev.fetch.Fetch;
import com.tonyodev.fetch.listener.FetchListener;
import com.tonyodev.fetch.request.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ru.yandex.matu1.toddlersbook.models.Book;
import ru.yandex.matu1.toddlersbook.models.BookFiles;

public class BookCardActivity extends AppCompatActivity {
//    private ProgressDialog mProgressDialog;
    private long downloadId = -1;
    static final String TAG = "myLogs";
    private int bookId;
    private String fileNamePath = "filesPath.json";
    Fetch mFetch;


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

        final Button buttonDownload = (Button) findViewById(R.id.button);

        String fileListBook = "list_" + "book_" + bookId + ".json";


        /**
         * Проверим наличие файлов в папке bookfiles_1, bookfiles_2, ...
         */
        String folderBook = "bookfiles_" + bookId;
        int count = 0;
        /**
         * Проверяем необходимость загрузки файлов
         */

        File rootFile = new File(String.valueOf(getExternalFilesDir(folderBook)));
        File[] filesArray = rootFile.listFiles();
        int numbFiles = filesArray.length;
        if (numbFiles == 0) {

            buttonDownload.setText(R.string.buttonDownload);
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
                            List<String> sounds = book.getSoundUrl();
                            String[] urlsPages = pages.toArray(new String[0]);
                            String[] urlsSounds = sounds.toArray(new String[0]);
                            String[] urlsFiles = ArrayAndArrayNewArray(urlsPages, urlsSounds);
                            DownloadFilesBook(urlsFiles);
                        }
                    });
                    mThread.start(); // запустили поток 2
                    buttonDownload.setText(R.string.buttonRead);
                    buttonDownload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            NextActivity();
                        }
                    });
                }
            });
        } else {
            buttonDownload.setText(R.string.buttonRead);
            buttonDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NextActivity();
                }
            });

        }
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
//        final Fetch
                mFetch = Fetch.newInstance(this);
        String folderB = "bookfiles_" + bookId;
        String fileNameForWrite = "book_" + bookId + ".json";


        File bookfolder = new File(String.valueOf(getExternalFilesDir(folderB)));
//        List<Request> requestListPages = new ArrayList<>();
        ArrayList<String> pagesFiles = new ArrayList<>();

        for (int i = 0; i < urlsFiles.length; i++) {
            String url = urlsFiles[i];
            String path = String.valueOf(bookfolder);
            String fileName = Uri.parse(url).getLastPathSegment();
            Log.d("my2", fileName);
            Request request = new Request(url, path, fileName);
//            requestListPages.add(request);
            String pageFilePath = path + "/" + fileName;
            Log.d("my2", pageFilePath);
            pagesFiles.add(pageFilePath);
            downloadId = mFetch.enqueue(request);

        }

//        mFetch.enqueue(requestListPages);

        BookFiles bookFiles = new BookFiles();
        bookFiles.setBookID(bookId);

        ArrayList<String> pagesPath = getPagesArray(pagesFiles);
        ArrayList<String> soundsPath = getSoundsArray(pagesFiles);

        bookFiles.setPagesPath(pagesPath);
        bookFiles.setSoundsPath(soundsPath);
        Gson gson11 = new Gson();
        String filesJson = gson11.toJson(bookFiles);

        MyJSON.saveData(getApplicationContext(), filesJson, fileNameForWrite);
    }

    public static String[] ArrayAndArrayNewArray(String[] a, String[] b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        String[] r = new String[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

    public ArrayList<String> getSoundsArray(ArrayList<String> pagesFiles) {
        ArrayList<String> res = new ArrayList<>();
        for (int i = 0; i < pagesFiles.size(); i++) {
            String soun = pagesFiles.get(i);
            if (soun.contains("sound")) {
                res.add(soun);
            }
        }
        return res;
    }

    public ArrayList<String> getPagesArray(ArrayList<String> pagesFiles) {
        ArrayList<String> res = new ArrayList<>();
        for (int i = 0; i < pagesFiles.size(); i++) {
            String soun = pagesFiles.get(i);
            if (!soun.contains("sound")) {
                res.add(soun);
            }
        }
        return res;
    }
}
