package com.sicmagroup.formmaster.viewholder;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;
import android.view.View;

import com.sicmagroup.formmaster.R;
import com.sicmagroup.formmaster.model.BaseFormElement;

/**
 * ViewHolder for Header
 * Created by Riddhi - Rudra on 30-Jul-17.
 */

public class FormElementHeader extends BaseViewHolder {

    public AppCompatTextView mTextViewTitle;

    public FormElementHeader(View v) {
        super(v);
        mTextViewTitle = (AppCompatTextView) v.findViewById(R.id.formElementTitle);
    }

    @Override
    public void bind(int position, BaseFormElement formElement, final Context context) {
        mTextViewTitle.setText(formElement.getTitle());
    }

}
