package ru.yandex.matu1.toddlersbook.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.picasso.Picasso;
import ru.yandex.matu1.toddlersbook.R;
import ru.yandex.matu1.toddlersbook.models.BookFiles;
import ru.yandex.matu1.toddlersbook.models.Cover;

public class CustomSwipeAdapter extends PagerAdapter {
    Context ctx;
    ArrayList<String> pagesFiles;

    public CustomSwipeAdapter(Context ctx, ArrayList<String> pagesFiles) {
        this.ctx = ctx;
        this.pagesFiles = pagesFiles;
    }

    @Override
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
        View item_view = layoutInflatter.inflate(R.layout.swipe_layout, container, false);
        ImageView imageView = item_view.findViewById(R.id.image_view);
        File imgFile = new File(pagesFiles.get(position));
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
            container.addView(item_view);
        }

        return item_view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((LinearLayout) object);
    }

}