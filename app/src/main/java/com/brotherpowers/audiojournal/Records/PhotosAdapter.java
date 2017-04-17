package com.brotherpowers.audiojournal.Records;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.brotherpowers.audiojournal.Model.Attachment;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.View.VH;
import com.bumptech.glide.Glide;

import butterknife.BindView;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by harsh_v on 2/18/17.
 */

public class PhotosAdapter extends RealmRecyclerViewAdapter<Attachment, PhotosAdapter.ViewHolderImage> {
    private final Context context;

    public PhotosAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<Attachment> data) {
        super(data, true);
        this.context = context;
    }


    @Override
    public ViewHolderImage onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_internal_image, parent, false);
        return new ViewHolderImage(view, (clickedView, adapterPosition) -> {
            Toast.makeText(context, "click image: " + adapterPosition, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onBindViewHolder(ViewHolderImage holder, int position) {
        Attachment attachment = getData().get(position);
        if (attachment != null) {
            Glide.with(context)
                    .load(attachment.file(context))
                    .centerCrop()
//                .fitCenter()
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_photo);
        }
    }


    static class ViewHolderImage extends VH implements View.OnClickListener {
        @BindView(R.id.image_view_internal)
        ImageView imageView;

        ViewHolderImage(View itemView, VhClick vhClick) {
            super(itemView, vhClick);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            vhClick.onItemClick(view, getAdapterPosition());
        }
    }
}