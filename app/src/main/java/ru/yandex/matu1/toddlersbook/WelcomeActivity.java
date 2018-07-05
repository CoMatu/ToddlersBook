package ru.yandex.matu1.toddlersbook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import ru.yandex.matu1.toddlersbook.app_classes.MyJSON;

public class WelcomeActivity extends AppCompatActivity {

    private ArrayList<String> urlsFromServer = new ArrayList<>();
    private boolean checkFileStorage = false;
    private final int MY_PERMISSIONS_REQUEST_CODE = 1;
    private String fileNamePath = "filesPath.json";
    private int connectToServer = 1;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CODE);

        // Инициализация AppMetrica SDK
        String API_key = "bde07b78-22a4-4579-8dec-bab86dee0023";
        YandexMetricaConfig.Builder configBuilder = YandexMetricaConfig.newConfigBuilder(API_key);
        YandexMetrica.activate(getApplicationContext(), configBuilder.build());
        // Отслеживание активности пользователей
        YandexMetrica.enableActivityAutoTracking(getApplication());
    }

    private class ParseJsonServer extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJsonServer = "";

        @Override
        protected String doInBackground(Void... params) {

            // получаем данные с внешнего ресурса

            try {
                String urlServerJson = "http://skazkimal.ru/todbook/booklist.json";
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
                connectToServer = -1;
            }

            return resultJsonServer;

        }

        @Override
        protected void onPostExecute(String strJson) {

            if (connectToServer == -1) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        getString(R.string.NoInternetText), Toast.LENGTH_LONG);
                toast.show();
                finishAffinity(); // проверить адекватность этой команды!!!!
            }

            urlsFromServer = getListUrlCovers(resultJsonServer);
            String strJs = new Gson().toJson(urlsFromServer);
            MyJSON.saveData(WelcomeActivity.this, strJs, fileNamePath);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    nextActivity();
                }
            }, 1500);

        }

    }

    private ArrayList<String> getListUrlCovers(String strJson) {

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkListFile(fileNamePath);
                    if(checkFileStorage){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                nextActivity();
            }
        }, 1500);

                    }
                    new ParseJsonServer().execute();
                } else {
                    Toast.makeText(WelcomeActivity.this, "Без данного разрешения приложение не сможет хранить полученные данные и не будет работать, пожалуйста дайте разрешение", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CODE);

                }
            }
        }
    }

    private void nextActivity() {
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkListFile(String fileNameStorage) {
        File fileUrls = new File(getApplicationContext().getFilesDir().getPath() + "/" + fileNameStorage);

        if (fileUrls.exists()) {
            checkFileStorage = true;
        }
    }

}