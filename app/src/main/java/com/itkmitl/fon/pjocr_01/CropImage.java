package com.itkmitl.fon.pjocr_01;



import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.theartofdev.edmodo.cropper.CropImageView;


public class CropImage extends AppCompatActivity {
    CropImageView mCropImageView;
    Bitmap cropped;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ปุ่ม back แล้วโชว์ค่าเดิม
                finish();

            }
        });

        //getContentResolver().notifyChange(mImageCaptureUri, null);
        //ContentResolver cr = getContentResolver();
        //Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, mImageCaptureUri);
        final Bitmap baseBitmap = (Bitmap) Cache.getInstance().getLru().get("bitmap_image");


        mCropImageView = (CropImageView) findViewById(R.id.CropImageView);
        mCropImageView.setImageBitmap(baseBitmap);


        Button bt_save = (Button) findViewById(R.id.bt_checkIng);
        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cropped != null){
                    Cache.getInstance().getLru().put("bitmap_image", cropped);
                    Intent myIntent = new Intent(CropImage.this, MainWork.class);
                    startActivity(myIntent);
                    finish();
                    //new LoginAsyncTask().execute();
                }
                else {
                    Toast.makeText(getApplicationContext(), "กรุณาเลือก Crop ก่อนการตรวจส่วนผสม ",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    ProgressDialog progressDialog;

    private class LoginAsyncTask extends AsyncTask<Void, Void, Void> {
        //แสดง dialog ตอนประมวลผลข้อมูล (ถอดรหัสภาพมาเป็นตัวอักษร)
        @Override
        protected void onPreExecute() {
            progressDialog= new ProgressDialog(CropImage.this);
            progressDialog.setMessage("Loading, Please wait...");
            progressDialog.show();
            super.onPreExecute();
        }

        protected Void doInBackground(Void... args) {
            // Parsse response data
            Intent myIntent = new Intent(CropImage.this, MainWork.class);
            startActivity(myIntent);
            finish();
            return null;
        }

        protected void onPostExecute(Void result) {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_crop_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_rotate) {
            mCropImageView.rotateImage(90);

        }
        if (id == R.id.action_crop) {
            cropped =  mCropImageView.getCroppedImage(0,0);
            if (cropped != null)
                mCropImageView.setImageBitmap(cropped);
        }

        return super.onOptionsItemSelected(item);
    }
}
