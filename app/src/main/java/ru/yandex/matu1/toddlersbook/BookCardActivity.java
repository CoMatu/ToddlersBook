package ru.yandex.matu1.toddlersbook;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

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
    Fetch mFetch;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_card);

        progressBar = (ProgressBar) findViewById(R.id.progressBar3);


        ImageButton imageButton = (ImageButton) findViewById(R.id.imageButtonHome);
        View.OnClickListener clickHome = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoHome();
            }
        };

        imageButton.setOnClickListener(clickHome);

        BookUriFromId();

        String fileNamePath = "filesPath.json";
        String covers = MyJSON.getData(this, fileNamePath);
        ArrayList<String> coversPaths = getFilesPathFromFile(covers);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        int posit = bookId - 1;
        File imgFile = new File(coversPaths.get(posit));
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        imageView.setImageBitmap(myBitmap);

        final Button buttonDownload = (Button) findViewById(R.id.button);

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
                            downloadFilesBook(urlsFiles);
                        }
                    });
                    mThread.start(); // запустили поток 2

/*
                    ProgressDialog progressDialog = new ProgressDialog(BookCardActivity.this);
                    progressDialog.setMessage(getString(R.string.progressDialogText));

                    progressDialog.setCancelable(false);
                    progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    progressDialog.show();
*/

/*
                    try {
                        mThread.join(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
*/

//                    progressDialog.dismiss();

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
            String jsUrl = "http://skazkimal.ru/todbook/book" + bookId + ".json";
            URL url = new URL(jsUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
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

    private void downloadFilesBook(String[] urlsFiles) {
        mFetch = Fetch.newInstance(this);
        String folderB = "bookfiles_" + bookId;
        String fileNameForWrite = "book_" + bookId + ".json";
        int progress = 0;

        File bookfolder = new File(String.valueOf(getExternalFilesDir(folderB)));
        ArrayList<String> pagesFiles = new ArrayList<>();

        for (int i = 0; i < urlsFiles.length; i++) {
            String url = urlsFiles[i];
            String path = String.valueOf(bookfolder);
            String fileName = Uri.parse(url).getLastPathSegment();
            Log.d("my2", fileName);
            Request request = new Request(url, path, fileName);
            String pageFilePath = path + "/" + fileName;
            Log.d("my2", pageFilePath);
            pagesFiles.add(pageFilePath);
            downloadId = mFetch.enqueue(request);

            mFetch.addFetchListener(new FetchListener() {
                @Override
                public void onUpdate(long id, int status, int progress, long downloadedBytes, long fileSize, int error) {

                    Log.i("fetchDebug","id: " + id + " downloadedBytes: " + downloadedBytes + " / fileSize: " + fileSize);

                    if(status == Fetch.STATUS_DONE){
                        progressBar.setVisibility(ProgressBar.INVISIBLE);

                    }

                }
            });
        }


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

    private void GoHome() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(BookCardActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 10);
    }

}
