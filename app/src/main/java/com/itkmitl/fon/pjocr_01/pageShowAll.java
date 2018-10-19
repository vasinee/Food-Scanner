package com.itkmitl.fon.pjocr_01;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/*
    แสดงค่าทัง้หมดที่สแกนได้จากฉลากอาหาร
    หลังจากที่กดปุ่ม show all ที่หน้า MainWork
 */
public class pageShowAll extends AppCompatActivity {
    ArrayAdapter<String> adapter;
    ListView lv;
    ArrayList<String> ocrList, list;
    ImageView imageView;


    File folder = new File(Environment.getExternalStorageDirectory().toString()+"/Food scanner");
    String DATA_PATH = folder.toString(); //Save the path as a string value
    //final Bitmap baseBitmap = (Bitmap) Cache.getInstance().getLru().get("bitmap_image");

    String FILENAME = "Image2.png";
    String PATH = DATA_PATH + "/" + FILENAME;
    File f = new File(PATH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.showall_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ปุ่ม back แล้วโชว์ค่าเดิม
                Intent myIntent = new Intent(getApplicationContext(), MainWork.class);
                startActivity(myIntent);
                finish();

            }
        });

        final Bitmap baseBitmap = (Bitmap) Cache.getInstance().getLru().get("bitmap_image");
        imageView = (ImageView) findViewById(R.id.imgShowAll);
        imageView.setImageBitmap(baseBitmap); // เซตค่ารูปใน tab IMAGE
        list = new ArrayList<>();
        lv = (ListView) findViewById(R.id.lvShowAll);
        Intent intent = getIntent();
        ocrList = intent.getStringArrayListExtra("ocrList");

        for (String a : ocrList) {
            if (!"".equals(a) && !" ".equals(a)) {
                list.add(a);
            }
        }
        Log.v("ส่วนผสมทั้งหมด", String.valueOf(list));

        adapter = new ArrayAdapter<String>(pageShowAll.this, android.R.layout.simple_expandable_list_item_1, list){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                /// Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                TextView tv = (TextView) view.findViewById(android.R.id.text1);

                // Set the text size 25 dip for ListView each item
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16);

                // Return the view
                return view;
            }
        };
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", adapter.getItem(position));
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getApplicationContext(), "Copy " + adapter.getItem(position) + " to clipboard",
                        Toast.LENGTH_SHORT).show();

            }
        });


        Button btnAgain = (Button) findViewById(R.id.btnAgain);
        btnAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cache.getInstance().getLru().put("bitmap_image", baseBitmap);
                Intent i = new Intent(pageShowAll.this, CropImage.class);
                startActivity(i);
                finish();

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent myIntent = new Intent(getApplicationContext(), MainWork.class);
        startActivity(myIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cancel) {
            finish();
        }return super.onOptionsItemSelected(item);
    }
}
