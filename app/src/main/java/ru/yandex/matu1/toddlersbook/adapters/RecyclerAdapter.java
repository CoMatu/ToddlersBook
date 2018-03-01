package ru.yandex.matu1.toddlersbook.adapters;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import it.sephiroth.android.library.picasso.Picasso;
import ru.yandex.matu1.toddlersbook.BookCardActivity;
import ru.yandex.matu1.toddlersbook.R;
import ru.yandex.matu1.toddlersbook.models.Cover;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ImageViewHolder> {

//    private static final String LOG_TAG = "my_log";

    private List<Cover> covers;

    public RecyclerAdapter(List<Cover> covers) {
        this.covers = covers;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        ViewHolder vh = new ImageViewHolder(v);
        return (ImageViewHolder) vh;
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder holder, final int position) {
        Cover cover = covers.get(position);
        String uri = cover.getFileBookCover();
        holder.bind(uri);

        holder.imgObl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.imgObl.getContext(), BookCardActivity.class);
                intent.putExtra("bookId", position+1); // передаю в слайдер номер книги
                holder.imgObl.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return covers.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private ImageView imgObl;

        ImageViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            imgObl = itemView.findViewById(R.id.iv_recycler_item);
        }

        public void bind(String uri) {
            Picasso
                    .with(itemView.getContext())
                    .load(uri)
                    .into(imgObl);
        }
    }
}
