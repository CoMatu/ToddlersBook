package ru.yandex.matu1.toddlersbook;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ru.yandex.matu1.toddlersbook.models.Book;
import ru.yandex.matu1.toddlersbook.models.BookFiles;

public class BookCardActivity extends AppCompatActivity {
    static final String TAG = "myLogs";
    private int bookId;
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
                goHome();
            }
        };

        imageButton.setOnClickListener(clickHome);

        bookUriFromId();

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
        /**
         * Проверяем необходимость загрузки файлов
         */

        File rootFile1 = new File(String.valueOf(getExternalFilesDir(folderBook)));
        File[] filesArray = rootFile1.listFiles();
        int numbFiles = filesArray.length;
        if (numbFiles == 0) {

            buttonDownload.setText(R.string.buttonDownload);

            Thread jsDownload = new Thread(new Runnable() {
                @Override
                public void run() {
                    bookLoader();
                }
            });
            jsDownload.start(); // запустили поток 1*/


            /**
             * Обрабатываем нажатие кнопки "Загрузить" и грузим файлы книги
             */

            buttonDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


// Составляем список url для загрузки файлов. Читаем их из ранее записанного json
                    String fileListB = "list_" + "book_" + bookId + ".json";
                    String jsReadFile = MyJSON.getData(getApplicationContext(), fileListB);
//                    Log.d(TAG, jsReadFile);
                    Gson gson = new Gson();
                    Book book = gson.fromJson(jsReadFile, Book.class);
                    List<String> pages = book.getPageUrl();
                    List<String> sounds = book.getSoundUrl();
                    String[] urlsPages = pages.toArray(new String[0]);
                    String[] urlsSounds = sounds.toArray(new String[0]);
                    String[] urlsFiles = arrayAndArrayNewArray(urlsPages, urlsSounds);
// Запускаем загрузку файлов AsyncTask
                    BookFilesLoader bookFilesLoader = new BookFilesLoader();
                    bookFilesLoader.execute(urlsFiles);

                    buttonDownload.setText(R.string.buttonRead);
                    buttonDownload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            nextActivity();
                        }
                    });
                }
            });
        } else {
            buttonDownload.setText(R.string.buttonRead);
            buttonDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextActivity();
                }
            });

        }
    }

    private void bookUriFromId() {
        //получаем номер ID книги, с обложки которой перешли в слайдер
        Intent intent = getIntent();
        bookId = intent.getIntExtra("bookId", 1);
        Log.d(TAG, "You read book №" + bookId);
    }

    private void nextActivity() {
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

    private void bookLoader() {

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

    public static String[] arrayAndArrayNewArray(String[] a, String[] b) {
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

    private void goHome() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(BookCardActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 10);
    }

    private class BookFilesLoader extends AsyncTask<String, Integer, ArrayList<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected ArrayList<String> doInBackground(String... urlsFiles) {
            ArrayList<String> pagesFiles = new ArrayList<>();

            try {

                for (int i = 0; i < urlsFiles.length; i++) {
                    String fileName = Uri.parse(urlsFiles[i]).getLastPathSegment();
                    String filePath = downloadFile(urlsFiles[i], fileName);// загрузил и записал файл
                    pagesFiles.add(filePath);
                }

                BookFiles bookFiles = new BookFiles();
                bookFiles.setBookID(bookId);
                ArrayList<String> pagesPath = getPagesArray(pagesFiles);
                ArrayList<String> soundsPath = getSoundsArray(pagesFiles);
                bookFiles.setPagesPath(pagesPath);
                bookFiles.setSoundsPath(soundsPath);
                Gson gson11 = new Gson();
                String filesJson = gson11.toJson(bookFiles);
                String fileNameForWrite = "book_" + bookId + ".json"; // имя json для записи
                MyJSON.saveData(getApplicationContext(), filesJson, fileNameForWrite);

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }



            return pagesFiles;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(ArrayList<String> pagesFiles) {
            super.onPostExecute(pagesFiles);

        }

    }

    private String downloadFile(String fileURL, String fileName) {
        String folderB = "bookfiles_" + bookId; // имя папки для записи файла
        String rootDir = String.valueOf(getExternalFilesDir(folderB));
        try {
            File rootFile = new File(String.valueOf(getExternalFilesDir(folderB)));
            if (!rootFile.exists()) {
                rootFile.mkdir();
            }
            URL url = new URL(fileURL);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            FileOutputStream f = new FileOutputStream(new File(rootFile,
                    fileName));
            InputStream in = c.getInputStream();
            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();
        } catch (IOException e) {
            Log.d("Error....", e.toString());
        }
        String filePathForWrite = rootDir+File.separator+fileName;
        return filePathForWrite;

    }
}
