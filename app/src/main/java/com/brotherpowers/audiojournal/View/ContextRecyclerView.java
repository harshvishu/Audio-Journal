package com.brotherpowers.audiojournal.View;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;

/**
 * Created by harsh_v on 12/28/16.
 * <p>
 * Base class to invoke context menu for an item
 */

public class ContextRecyclerView extends RecyclerView {
    public ContextRecyclerView(Context context) {
        super(context);
    }

    public ContextRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ContextRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    ContextMenu.ContextMenuInfo menuInfo;


    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return menuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        final int position = getChildPosition(originalView);
        if (position >= 0) {
            menuInfo = createContextMenuInfo(position);
            return super.showContextMenuForChild(originalView);
        }
        return false;
    }

    ContextMenu.ContextMenuInfo createContextMenuInfo(int position) {
        return new MenuInfo(position);
    }

    public static class MenuInfo implements ContextMenu.ContextMenuInfo {
        public final int position;

        MenuInfo(int position) {
            this.position = position;
        }

        @Override
        public String toString() {
            return "position: " + position;
        }
    }
}
