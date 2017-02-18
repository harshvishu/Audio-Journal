package com.brotherpowers.audiojournal.Records;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.brotherpowers.audiojournal.Model.Attachment;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.View.ALViewHolder;
import com.bumptech.glide.Glide;

import butterknife.BindView;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by harsh_v on 2/18/17.
 */

class AttachmentAdapter extends RealmRecyclerViewAdapter<Attachment, AttachmentAdapter.ViewHolderImage> {
    AttachmentAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<Attachment> data) {
        super(context, data, true);
    }


    @Override
    public ViewHolderImage onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recycler_view_internal_image, parent, false);
        return new ViewHolderImage(view, (click_view, position) -> {
            Toast.makeText(context, "click image: " + position, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onBindViewHolder(ViewHolderImage holder, int position) {
        Attachment attachment = getData().get(position);
        Glide.with(context)
                .load(attachment.file(context))
                .fitCenter()
                .into(holder.imageView);
    }


    static class ViewHolderImage extends ALViewHolder implements View.OnClickListener {
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