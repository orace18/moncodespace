package com.sicmagroup.formmaster.adapter;

import android.content.Context;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.RecyclerView;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.sicmagroup.formmaster.R;
import com.sicmagroup.formmaster.listener.FormItemEditTextListener;
import com.sicmagroup.formmaster.listener.OnFormElementValueChangedListener;
import com.sicmagroup.formmaster.model.BaseFormElement;
import com.sicmagroup.formmaster.viewholder.BaseViewHolder;
import com.sicmagroup.formmaster.viewholder.FormElementHeader;
import com.sicmagroup.formmaster.viewholder.FormElementPickerDateViewHolder;
import com.sicmagroup.formmaster.viewholder.FormElementPickerMultiViewHolder;
import com.sicmagroup.formmaster.viewholder.FormElementPickerSexeViewHolder;
import com.sicmagroup.formmaster.viewholder.FormElementPickerSingleViewHolder;
import com.sicmagroup.formmaster.viewholder.FormElementPickerTimeViewHolder;
import com.sicmagroup.formmaster.viewholder.FormElementSwitchViewHolder;
import com.sicmagroup.formmaster.viewholder.FormElementTextEmailViewHolder;
import com.sicmagroup.formmaster.viewholder.FormElementTextMultiLineViewHolder;
import com.sicmagroup.formmaster.viewholder.FormElementTextNumberViewHolder;
import com.sicmagroup.formmaster.viewholder.FormElementTextPasswordViewHolder;
import com.sicmagroup.formmaster.viewholder.FormElementTextPhoneViewHolder;
import com.sicmagroup.formmaster.viewholder.FormElementTextSingleLineViewHolder;
import com.sicmagroup.formmaster.listener.ReloadListener;
import com.sicmagroup.formmaster.viewholder.FormElementZeroViewHolder;

/**
 * The adapter the holds and displays the form objects
 * Created by Adib on 16-Apr-17.
 */

public class FormAdapter extends RecyclerView.Adapter<BaseViewHolder> implements ReloadListener {

    private Context mContext;
    private List<BaseFormElement> mDataset;
    private OnFormElementValueChangedListener mListener;

    /**
     * public constructor with context
     * @param context
     */
    public FormAdapter(Context context, OnFormElementValueChangedListener listener) {
        mContext = context;
        mListener = listener;
        mDataset = new ArrayList<>();
    }

    /**
     * adds list of elements to be shown
     * @param formObjects
     */
    public void addElements(List<BaseFormElement> formObjects) {
        this.mDataset = formObjects;
        notifyDataSetChanged();
    }

    /**
     * adds single element to be shown
     * @param formObject
     */
    public void addElement(BaseFormElement formObject) {
        this.mDataset.add(formObject);
        notifyDataSetChanged();
    }

    /**
     * set value for any unique index
     * @param position
     * @param value
     */
    public void setValueAtIndex(int position, String value) {
        BaseFormElement baseFormElement = mDataset.get(position);
        baseFormElement.setValue(value);
        notifyDataSetChanged();
    }

    /**
     * set value for any unique tag
     * @param tag
     * @param value
     */
    public void setValueAtTag(int tag, String value) {
        for (BaseFormElement f : this.mDataset) {
            if (f.getTag() == tag) {
                f.setValue(value);
                return;
            }
        }
        notifyDataSetChanged();
    }

    /**
     * get value of any element by tag
     * @param index
     * @return
     */
    public BaseFormElement getValueAtIndex(int index) {
        return (mDataset.get(index));
    }

    /**
     * get value of any element by tag
     * @param tag
     * @return
     */
    public BaseFormElement getValueAtTag(int tag) {
        for (BaseFormElement f : this.mDataset) {
            if (f.getTag() == tag) {
                return f;
            }
        }

        return null;
    }

    /**
     * get whole dataset
     * @return
     */
    public List<BaseFormElement> getDataset() {
        return mDataset;
    }

    /**
     * get value changed listener
     * @return
     */
    public OnFormElementValueChangedListener getValueChangeListener() {
        return mListener;
    }

    /**
     * gets total item count
     * @return
     */
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    /**
     * gets view item type based on header, or the form element type
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position).getType();
    }
    boolean pwdVisible = false;
    /**
     * creating the view holder to be shown for a position
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // get layout based on header or element type
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        final View v;
        switch (viewType) {
            case BaseFormElement.TYPE_HEADER:
                v = inflater.inflate(R.layout.form_element_header, parent, false);
                return new FormElementHeader(v);
            case BaseFormElement.TYPE_EDITTEXT_TEXT_SINGLELINE:
                v = inflater.inflate(R.layout.form_element_denomination, parent, false);
                return new FormElementTextSingleLineViewHolder(v, new FormItemEditTextListener(this));
            case BaseFormElement.TYPE_EDITTEXT_TEXT_MULTILINE:
                v = inflater.inflate(R.layout.form_element, parent, false);
                return new FormElementTextMultiLineViewHolder(v, new FormItemEditTextListener(this));
            case BaseFormElement.TYPE_EDITTEXT_NUMBER:
                v = inflater.inflate(R.layout.form_element_mise, parent, false);
                return new FormElementTextNumberViewHolder(v, new FormItemEditTextListener(this));
            case BaseFormElement.TYPE_EDITTEXT_EMAIL:
                v = inflater.inflate(R.layout.form_element, parent, false);
                return new FormElementTextEmailViewHolder(v, new FormItemEditTextListener(this));
            case BaseFormElement.TYPE_EDITTEXT_PHONE:
                v = inflater.inflate(R.layout.form_element_telephone, parent, false);
                return new FormElementTextPhoneViewHolder(v, new FormItemEditTextListener(this));
            case BaseFormElement.TYPE_EDITTEXT_PASSWORD:
                v = inflater.inflate(R.layout.form_element_password, parent, false);

                final ImageView voir_pass = v.findViewById(R.id.voir_pass);
                voir_pass.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!pwdVisible){
                            AppCompatEditText formElementValue = v.findViewById(R.id.formElementValue_pass);
//                            Toast.makeText(mContext,"encodage actif",Toast.LENGTH_LONG).show();
                            formElementValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD );
                            formElementValue.setSelection(formElementValue.length());
                            formElementValue.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            voir_pass.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_eye));
                            pwdVisible = true;
                        }else{
                            AppCompatEditText formElementValue = v.findViewById(R.id.formElementValue_pass);
                            formElementValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            formElementValue.setSelection(formElementValue.length());
                            formElementValue.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            voir_pass.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_eye_in));
                            pwdVisible = false;
                        }
                        // verifier si encodage actif
//                        if(voir_pass.getDrawable().getConstantState() == mContext.getResources().getDrawable(R.drawable.ic_eye_in).getConstantState()){
//                            Log.d("passinfos", "encodage actif, donc désactiver pour rendre visible le pass");
//                            AppCompatEditText formElementValue = v.findViewById(R.id.formElementValue_pass);
////                            Toast.makeText(mContext,"encodage actif",Toast.LENGTH_LONG).show();
//                            formElementValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD );
//                            formElementValue.setSelection(formElementValue.length());
////                            formElementValue.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                            voir_pass.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_eye));
//                        }else{
//                            //Log.d("passinfos", "encodage inactif, donc activer pour rendre invisible le pass");
////                            Toast.makeText(mContext,"encodage inactif", Toast.LENGTH_LONG).show();
//                            AppCompatEditText formElementValue = v.findViewById(R.id.formElementValue_pass);
//                            formElementValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD );
//                            formElementValue.setSelection(formElementValue.length());
////                            formElementValue.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                            voir_pass.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_eye_in));
//                        }


                        //

                    }
                });

                return new FormElementTextPasswordViewHolder(v, new FormItemEditTextListener(this));
            case BaseFormElement.TYPE_PICKER_DATE:
                v = inflater.inflate(R.layout.form_element_date_picker, parent, false);
                return new FormElementPickerDateViewHolder(v, mContext, this);
            case BaseFormElement.TYPE_PICKER_TIME:
                v = inflater.inflate(R.layout.form_element, parent, false);
                return new FormElementPickerTimeViewHolder(v, mContext, this);
            case BaseFormElement.TYPE_PICKER_SINGLE:
                v = inflater.inflate(R.layout.form_element_periode, parent, false);
                return new FormElementPickerSingleViewHolder(v, mContext, this);
            case BaseFormElement.TYPE_PICKER_MULTI:
                v = inflater.inflate(R.layout.form_element, parent, false);
                return new FormElementPickerMultiViewHolder(v, mContext, this);
            case BaseFormElement.TYPE_SWITCH:
                v = inflater.inflate(R.layout.form_element_switch_auto_tontine, parent, false);
                return new FormElementSwitchViewHolder(v, mContext, this);
            case BaseFormElement.TYPE_ZERO:
                v = inflater.inflate(R.layout.form_element_zero, parent, false);
                return new FormElementZeroViewHolder(v,new FormItemEditTextListener(this));
            case BaseFormElement.TYPE_PICKER_SEXE:
                v = inflater.inflate(R.layout.form_element_sexe, parent, false);
                return new FormElementPickerSexeViewHolder(v, mContext, this);
            default:
                v = inflater.inflate(R.layout.form_element, parent, false);
                return new FormElementTextSingleLineViewHolder(v, new FormItemEditTextListener(this));
        }
    }

    /**
     * draws the view for the position specific view holder
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(BaseViewHolder holder, final int position) {

        // updates edit text listener index
        if (holder.getListener() != null) {
            holder.getListener().updatePosition(holder.getAdapterPosition());
        }
        if (position==0){

        }
        // gets current object
        BaseFormElement currentObject = mDataset.get(position);
        holder.bind(position, currentObject, mContext);
    }

    /**
     * use the listener to update value and notify dataset changes to adapter
     * @param position
     * @param updatedValue
     */
    @Override
    public void updateValue(int position, String updatedValue) {
        mDataset.get(position).setValue(updatedValue);
        notifyDataSetChanged();
        if (mListener != null)
            mListener.onValueChanged(mDataset.get(position));
    }

}