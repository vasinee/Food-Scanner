package com.itkmitl.fon.pjocr_01;


import android.app.Activity;

import android.content.ContentResolver;
import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;

import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends Activity {
    private static final int CAMERA_CODE = 101, GALLERY_CODE = 201;
    private Uri mImageCaptureUri;
    Set<String> set0,set1,set2;
    //Create Folder
    File folder = new File(Environment.getExternalStorageDirectory().toString()+"/Food scanner");
    String DATA_PATH = folder.toString(); //Save the path as a string value
    private File outPutFile = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage);
        folder.mkdirs();




    outPutFile = new File(DATA_PATH, "Image2.PNG"); //Create New file and name it Image2.PNG
        outPutFile.getAbsoluteFile().deleteOnExit();
        copyAssetFolder(getAssets(), "tessdata", DATA_PATH + "/tessdata");



        //checkbox != null
        SharedPreferences sp = getSharedPreferences("USERPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        set0 = sp.getStringSet("set0", null);
        set1 = sp.getStringSet("set1", null);
        set2 = sp.getStringSet("set2", null);

        //first install if user profile == null call user profile page
        if (set0 == null && set1 == null  && set2 == null ) {
            Set<String> set = new HashSet<String>();
            set.add("test");
            editor.putStringSet("set0", set);
            editor.putStringSet("set1", set);
            editor.putStringSet("set2", set);
            editor.commit();
            Intent i = new Intent(this, MainUser.class);
            startActivity(i);
        }

        /*Button Edit
        * This  select type */
        ImageButton btn_edit = (ImageButton) findViewById(R.id.btn_edit);
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MainUser.class);
                startActivity(i);
            }
        });

        //select from gallery
        ImageButton btn_gallery = (ImageButton) findViewById(R.id.btn_gallery);
        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, GALLERY_CODE);

            }
        });

        //take a photo
        ImageButton btn_camera = (ImageButton) findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File f = new File(DATA_PATH, "foodscanner.jpg");
                mImageCaptureUri = Uri.fromFile(f);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                startActivityForResult(intent, CAMERA_CODE);
            }
        });

        //คู่มือการใช้งาน
        ImageButton btn_tutor = (ImageButton) findViewById(R.id.btn_tutor);
        btn_tutor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, page4.class);
                startActivity(i);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // call gallery app from device
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK && null != data) {
            mImageCaptureUri = data.getData();
            System.out.println("Gallery Image URI : " + mImageCaptureUri);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageCaptureUri);
                Cache.getInstance().getLru().put("bitmap_image", bitmap);
                Intent i = new Intent(MainActivity.this, CropImage.class);
                startActivity(i);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();}

        }
        // call camera app from device
        else if (requestCode == CAMERA_CODE && resultCode == Activity.RESULT_OK) {
            System.out.println("Camera Image URI : " + mImageCaptureUri);
            getContentResolver().notifyChange(mImageCaptureUri, null);
            ContentResolver cr = getContentResolver();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, mImageCaptureUri);
                //imageView.setImageBitmap(bitmap);
                //Toast.makeText(getApplicationContext(), mImageCaptureUri.getPath(), Toast.LENGTH_SHORT).show();
                Cache.getInstance().getLru().put("bitmap_image", bitmap);
                Intent i = new Intent(MainActivity.this, CropImage.class);
                startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**********************************************************************************************/
    // เอาค่าในโฟลเดอร์ assets ลงเครื่อง
    private static boolean copyAssetFolder(AssetManager assetManager, String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains("."))
                    res &= copyAsset(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
                else
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyAsset(AssetManager assetManager, String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
    /**********************************************************************************************/



}