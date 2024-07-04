package com.sicmagroup.formmaster.viewholder;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.sicmagroup.formmaster.listener.FormItemEditTextListener;
import com.sicmagroup.formmaster.model.BaseFormElement;

/**
 * Base ViewHolder for all other viewholders
 * Created by Riddhi - Rudra on 30-Jul-17.
 */

public class BaseViewHolder extends RecyclerView.ViewHolder implements BaseViewHolderInterface {

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public FormItemEditTextListener getListener() {
        return null;
    }

    @Override
    public void bind(int position, BaseFormElement formElement, Context context) {

    }

}
