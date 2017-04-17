package com.brotherpowers.audiojournal.View;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * Created by harsh_v on 10/14/16.
 */

public class VH extends RecyclerView.ViewHolder {
    final public VhClick vhClick;

    public VH(View itemView, VhClick vhClick) {
        super(itemView);
        this.vhClick = vhClick;
        ButterKnife.bind(this, itemView);
    }

    public interface VhClick {
        /**
         * @param adapterPosition of the item clicked {@method getAdapterPosition()}
         */
        void onItemClick(View clickedView, int adapterPosition);
    }
}
