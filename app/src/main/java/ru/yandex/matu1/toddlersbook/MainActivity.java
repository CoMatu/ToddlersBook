package ru.yandex.matu1.toddlersbook;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ru.yandex.matu1.toddlersbook.models.Cover;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    String fileNamePath = "filesPath.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);

        initializeAdapter();
    }

    private List<Cover> initData(ArrayList<String> urisImg) {
        ArrayList<Cover> covers = new ArrayList<>();
        for (int i = 0; i < urisImg.size(); i++) {
            covers.add(new Cover(urisImg.get(i)));
        }
        return covers;
    }

    private void initializeAdapter() {
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(initData(getFilesPathFromFile()));
        recyclerView.setAdapter(recyclerAdapter);
    }

    public ArrayList<String> getFilesPathFromFile() {
        String uriString = null;
        ArrayList<String> urlsCoverList = null;
        try {
            uriString = MyJSON.getData(getApplicationContext(), fileNamePath);
            Gson gsonCovers = new Gson();
            Type founderListType = new TypeToken<ArrayList<String>>() {
            }.getType();
            urlsCoverList = gsonCovers.fromJson(uriString, founderListType);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "Error of intent: " + e.getLocalizedMessage());
        }
        return urlsCoverList;
    }

}
