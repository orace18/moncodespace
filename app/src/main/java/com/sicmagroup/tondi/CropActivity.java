package com.sicmagroup.tondi;

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.pixplicity.easyprefs.library.Prefs;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;

import static com.sicmagroup.tondi.Inscription.CROPPING_CODE;

public class CropActivity extends Activity {

    private CropImageView mCropImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cropping_activity);
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        mCropImageView =  findViewById(R.id.CropImageView);
        //Toast.makeText(getApplicationContext(),getIntent().getStringExtra("imageUri"),Toast.LENGTH_LONG).show();
        Uri imageUri = Uri.parse(getIntent().getStringExtra("imageUri"));
        Log.e("crop", imageUri.getPath());
        mCropImageView.setImageUriAsync(imageUri);
        Button save_crop =findViewById(R.id.save_crop);
        save_crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Bitmap cropped =  mCropImageView.getCroppedImage(500, 500);
                if (cropped != null){
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    cropped.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    Intent intent=new Intent();
                    intent.putExtra("photo_profil",byteArray);
                    intent.putExtra("imageUriProfile", imageUri);
                    setResult(CROPPING_CODE,intent);
                    finish();//finishing activity
                }else{

                    Toast.makeText(getApplicationContext(),"dhe",Toast.LENGTH_LONG).show();
                }



            }
        });
    }

    /**
     * Crop the image and set it back to the  cropping view.
     */
    public void onCropImageClick(View view) {
        Bitmap cropped =  mCropImageView.getCroppedImage(500, 500);
        if (cropped != null)
            mCropImageView.setImageBitmap(cropped);
    }


}
