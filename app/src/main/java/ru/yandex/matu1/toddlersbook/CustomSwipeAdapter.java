package ru.yandex.matu1.toddlersbook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.picasso.Picasso;
import ru.yandex.matu1.toddlersbook.models.BookFiles;
import ru.yandex.matu1.toddlersbook.models.Cover;

public class CustomSwipeAdapter extends PagerAdapter {

/*
    private int[] image_resourses = {
            R.drawable.cat1, R.drawable.cat2, R.drawable.cat3,
            R.drawable.cat4, R.drawable.cat5, R.drawable.cat6,
            R.drawable.cat7};
*/
    private Context ctx;
    ArrayList<String> pagesFiles;
    ArrayList<String> soundsFiles;
    /*
    CustomSwipeAdapter(Context ctx){
        this.ctx = ctx;
    }
    */

    public CustomSwipeAdapter(Context ctx, ArrayList<String> pagesFiles, ArrayList<String> soundsFiles) {
        this.ctx = ctx;
        this.pagesFiles = pagesFiles;
        this.soundsFiles = soundsFiles;
    }

    /*
        CustomSwipeAdapter(List<BookFiles> pagesPath, Context ctx) {
            this.pagesPath = pagesPath;
            this.ctx = ctx;
        }
    */
    @Override
/*
    public int getCount() {
        return image_resourses.length;
    }
*/
    public int getCount() {
        return pagesFiles.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater layoutInflatter = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflatter != null;
        View item_view = layoutInflatter.inflate(R.layout.swipe_layout,container,false);
        ImageView imageView = (ImageView)item_view.findViewById(R.id.image_view);
//        String uri = pagesFiles.get(position);
        File imgFile = new File(pagesFiles.get(position));
        if(imgFile.exists()){
/*
                Picasso
                        .with(item_view.getContext())
                        .load(uri)
                        .into(imageView);
*/
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
            container.addView(item_view);

        }
        return item_view;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((LinearLayout)object);
    }


}

