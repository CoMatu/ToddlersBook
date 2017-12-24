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
import android.widget.Toast;

import com.google.gson.Gson;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.ThinDownloadManager;

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

public class WelcomeActivity extends AppCompatActivity {

    private ArrayList<String> urlsFromServer = new ArrayList<>();
    private ArrayList<Uri> filesPath = new ArrayList<>();
    private boolean CheckFileStorage = false;
    private final int MY_PERMISSIONS_REQUEST_CODE = 1;

    private String fileNamePath = "filesPath.json";

    private String urlServerJson = "http://human-factors.ru/todbook/booklist.json";

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CODE);
    }

    private class ParseJsonServer extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJsonServer = "";

        @Override
        protected String doInBackground(Void... params) {

            // получаем данные с внешнего ресурса

            try {
                URL url = new URL(urlServerJson);

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

            } catch (Exception e) {
                e.printStackTrace();
            }

            return resultJsonServer;

        }

        @Override
        protected void onPostExecute(String strJson) {


            if (resultJsonServer != "") {
                urlsFromServer = GetListUrlCovers(resultJsonServer);

            } else {
                CheckListFile(fileNamePath);
                if (!CheckFileStorage) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            getString(R.string.NoInternetText), Toast.LENGTH_LONG);
                    toast.show();
                    NextActivity();

                } else {
                    NextActivity();
                }
            }

            CheckListFile(fileNamePath);

            if (CheckFileStorage) {

                String jsResult = MyJSON.getData(getApplicationContext(), fileNamePath);
                filesPath = getFilesPathFromFile(jsResult);

            } else {

                filesPath = new ArrayList<>();

                for (int i = 0; i < urlsFromServer.size(); i++) {
                    int d = i + 1;
                    String fileUrl = urlsFromServer.get(i);
                    String filenam = getApplicationContext().getFilesDir() + File.separator + "bookcover_" + d + ".jpg";
                    FileLoader(fileUrl, filenam);
                }
                String strJs = new Gson().toJson(filesPath);

                MyJSON.saveData(getApplicationContext(), strJs, fileNamePath);
            }

            if (filesPath.size() == urlsFromServer.size()) {

                NextActivity();

            } else {
                try {
                    deleteFile(fileNamePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < urlsFromServer.size(); i++) {
                    int d = i + 1;
                    String fileUrl = urlsFromServer.get(i);
                    String filenam = getApplicationContext().getFilesDir() + File.separator + "bookcover_" + d + ".jpg";
                    FileLoader(fileUrl, filenam);
                }
                String strJs = new Gson().toJson(filesPath);

                MyJSON.saveData(getApplicationContext(), strJs, fileNamePath);
                NextActivity();
            }
        }

    }

    private ArrayList<String> GetListUrlCovers(String strJson) {

        JSONObject dataJsonObj;

        try {
            dataJsonObj = new JSONObject(strJson);
            JSONArray books = dataJsonObj.getJSONArray("books");

            for (int i = 0; i < books.length(); i++) {
                JSONObject book = books.getJSONObject(i);
                String url_book = book.getString("coverUrl");
                urlsFromServer.add(url_book);// пишу урлы в ArrayList
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return urlsFromServer;
    }

    private void CheckListFile(String fileNameStorage) {
        File fileUrls = new File(getApplicationContext().getFilesDir().getPath() + "/" + fileNameStorage);

        if (fileUrls.exists()) {

            CheckFileStorage = true;

        }

    }

    private void FileLoader(String fileUrl, String filename) {

        ThinDownloadManager downloadManager = new ThinDownloadManager(5); //количество потоков загрузки
        Uri downloadUri = Uri.parse(fileUrl);
        Uri destinationUri = Uri.parse(filename);
        DownloadRequest downloadRequest = new DownloadRequest(downloadUri).setDestinationURI(destinationUri);
        downloadManager.add(downloadRequest);
        filesPath.add(destinationUri);
    }

    private ArrayList<Uri> getFilesPathFromFile(String jsResult) {
        ArrayList<Uri> urisImg = new ArrayList<>();
        try {
            JSONArray rootJson = new JSONArray(new JSONTokener(jsResult));
            for (int i = 0; i < rootJson.length(); i++) {
                JSONObject o = rootJson.getJSONObject(i);
                String strTo = (String) o.get("uriString");
                urisImg.add(Uri.parse(strTo));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return urisImg;
    }

    private void NextActivity() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }
    @Override
    public  void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new ParseJsonServer().execute();
                }
                else {  Toast.makeText(WelcomeActivity.this, "Без данного разрешения приложение не сможет хранить полученные данные и не будет работать, пожалуйста дайте разрешение", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CODE);

                }
            }
        }
    }

}