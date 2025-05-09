package com.sicmagroup.tondi;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpacingItemDecorator  extends RecyclerView.ItemDecoration {
    private final  int verticalSpacingHeight;

    public SpacingItemDecorator(int verticalSpacingHeight) {
        this.verticalSpacingHeight = verticalSpacingHeight;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = verticalSpacingHeight;

    }
}
