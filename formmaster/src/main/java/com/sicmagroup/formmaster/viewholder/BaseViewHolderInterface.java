package com.sicmagroup.formmaster.viewholder;

import android.content.Context;

import com.sicmagroup.formmaster.listener.FormItemEditTextListener;
import com.sicmagroup.formmaster.model.BaseFormElement;

/**
 * Base ViewHolder method instance
 * Created by Riddhi - Rudra on 30-Jul-17.
 */

public interface BaseViewHolderInterface {
    FormItemEditTextListener getListener();
    void bind(int position, BaseFormElement formElement, Context context);
}
