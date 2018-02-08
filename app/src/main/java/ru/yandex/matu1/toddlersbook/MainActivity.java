package ru.yandex.matu1.toddlersbook;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.matu1.toddlersbook.models.Cover;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    String jsResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);

        String fileNamePath = "filesPath.json";
        jsResult = MyJSON.getData(getApplicationContext(), fileNamePath);
        initializeAdapter();

    }

    private List<Cover> initData(ArrayList<Uri> urisImg) {
        ArrayList<Cover> covers = new ArrayList<>();
        for (int i=0; i<urisImg.size();i++){
            covers.add(new Cover(urisImg.get(i)));
        }
        return covers;
    }

    private void initializeAdapter() {
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(initData(getFilesPathFromFile(jsResult)));
        recyclerView.setAdapter(recyclerAdapter);
    }

    public ArrayList<Uri> getFilesPathFromFile (String jsResult){
        ArrayList<Uri> urisImg = new ArrayList<>();

        try{
            JSONArray rootJson = new JSONArray(new JSONTokener(jsResult));
            for(int i=0; i<rootJson.length(); i++){
                JSONObject o = rootJson.getJSONObject(i);
                String strTo = (String) o.get("uriString");
                urisImg.add(Uri.parse(strTo));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return urisImg;
    }

}
