package com.sicmagroup.formmaster.viewholder;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import com.sicmagroup.formmaster.R;
import com.sicmagroup.formmaster.listener.ReloadListener;
import com.sicmagroup.formmaster.model.BaseFormElement;
import com.sicmagroup.formmaster.model.FormElementPickerSexe;
import com.sicmagroup.formmaster.model.FormElementPickerSingle;

public class FormElementPickerSexeViewHolder extends BaseViewHolder {
    private AppCompatTextView mTextViewTitle;
    private AppCompatEditText mEditTextValue;
    private ReloadListener mReloadListener;
    private BaseFormElement mFormElement;
    private FormElementPickerSexe mFormElementPickerSexe;
    private int mPosition;

    public FormElementPickerSexeViewHolder(View v, Context context, ReloadListener reloadListener) {
        super(v);
        mTextViewTitle = (AppCompatTextView) v.findViewById(R.id.formElementTitle);
        mEditTextValue = (AppCompatEditText) v.findViewById(R.id.formElementValue);
        mReloadListener = reloadListener;
    }

    @Override
    public void bind(final int position, BaseFormElement formElement, final Context context) {
        mFormElement = formElement;
        mPosition = position;
        mFormElementPickerSexe = (FormElementPickerSexe) mFormElement;

        mTextViewTitle.setText(formElement.getTitle());
        mEditTextValue.setText(formElement.getValue());
        mEditTextValue.setHint(formElement.getHint());
        mEditTextValue.setFocusableInTouchMode(false);

        // reformat the options in format needed
        final CharSequence[] options = new CharSequence[mFormElementPickerSexe.getOptions().size()];
        for (int i = 0; i < mFormElementPickerSexe.getOptions().size(); i++) {
            options[i] = mFormElementPickerSexe.getOptions().get(i);
        }

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(mFormElementPickerSexe.getPickerTitle())
                .setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mEditTextValue.setText(options[which]);
                        mFormElementPickerSexe.setValue(options[which].toString());
                        mReloadListener.updateValue(position, options[which].toString());
                    }
                })
                .create();

        mEditTextValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        mTextViewTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
    }
}
