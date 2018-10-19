package com.itkmitl.fon.pjocr_01;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.akexorcist.cloudvision.CVRequest;
import com.akexorcist.cloudvision.CVResponse;
import com.akexorcist.cloudvision.CloudVision;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Headers;

import static com.itkmitl.fon.pjocr_01.R.id.imgHead;


public class MainWork extends AppCompatActivity implements CloudVision.Callback{
    private final static String apiKey = "AIzaSyC_sOMCJmUZQLZQ5MRCqARNU6Na964X4E8";

    ListView lvLabel;
    Button btnShowAll;
    String descriptionGetText;

    SQLiteDatabase mDb;
    myDBclass mHelper;
    ViewGroup header;
    LayoutInflater inflater;

    String[] checkSet0, checkSet1, checkSet2;
    ArrayAdapter<String> adapter;
    ArrayList<String> compareArray, cwList1;
    RelativeLayout pbLabel;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resultpage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        compareArray = new ArrayList<>();

        mHelper = new myDBclass(this);
        mDb = mHelper.getWritableDatabase();
        // array ที่มีค่าเหมือนกันกับผู้ใช้เลือก
        ArrayList<String> stringOCR = new ArrayList<String>(); // ตัวเก็บบสตริงที่ได้จากการ split
        final ArrayList<String> ocrList = new ArrayList<String>();

        // get share preference เรียกค่าที่เก็บไว้มาใช้
        SharedPreferences sp = getSharedPreferences("USERPreferences", Context.MODE_PRIVATE);
        Set<String> set0 = new HashSet<String>();
        Set<String> set1 = new HashSet<String>();
        Set<String> set2 = new HashSet<String>();
        set0 = sp.getStringSet("set0", null);
        set1 = sp.getStringSet("set1", null);
        set2 = sp.getStringSet("set2", null);

        checkSet0 = set0.toArray(new String[set0.size()]);
        checkSet1 = set1.toArray(new String[set1.size()]);
        checkSet2 = set2.toArray(new String[set2.size()]);

        //เรียกค่า จาก xml มาใช้
        lvLabel = (ListView) findViewById(R.id.lv_Label);
        //pbLabel = (ProgressBar) findViewById(R.id.pb_Label);
        btnShowAll = (Button) findViewById(R.id.btn_show_all);
        inflater = getLayoutInflater();
        header = (ViewGroup) inflater.inflate(R.layout.header, lvLabel, false);

        btnShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ปุ่มโชว์ผลลัพธ์ทั้งหมด ที่ได้จาการแสกนฉลาก
                Intent myIntent = new Intent(getApplicationContext(), pageShowAll.class);
                myIntent.putExtra("ocrList", cwList1 );
                startActivity(myIntent);
                finish();

            }
        });

        stringOCR.clear(); //reset ค่าที่อยู่ใน stringOCR ทุกครั้งเมื่อกดปุ่ม ค่าจะเป็น null
        compareArray.clear();

       // showLoading();
        startDetect();


    }

    private void splitSymbol(String text) {

        //split ค่าที่ได้จากการสแกน ตัดสัญลักษณ์ space แล้วยัดลง array
        //เอาค่าที่ได้จาก checkbox และการสแกนฉลาก มาเปรียบเทียบกัน
        //แสดงค่าผลลัพท์ที่ได้หลังจากการเปรียบเทียบแล้ว

        ArrayList<String> stringOCR = new ArrayList<String>();
        Log.v("text", text);
        char symbol[] = {'、', '`', ',', '丶', '.', ';', ':', '〝', '〈', '\'', '.', '{', '〉', 'ゝ', '|'};
        String str = "";
        for (char charJPN : text.toCharArray()) {
            if (!Character.isWhitespace(charJPN)) {
                for (int x = 0; x < symbol.length; x++) {
                    if (symbol[x] == charJPN) {
                        Log.v("str form  ", str);//เจอสัญลักษณ์จะแอดลง
                        str = str.replaceAll("\\s","");
                        char firstCharacter = str.charAt(0);
                        char lastCharacter = str.charAt(str.length()-1);
                        Log.v("first", String.valueOf(firstCharacter));
                        Log.v("last", String.valueOf(lastCharacter));
                        if (firstCharacter == '(' ){
                            str = removeCharAt(str, 0);
                            if (lastCharacter == ')'){
                                str = removeCharAt(str, str.length());
                            }
                        }else{
                            if (lastCharacter == ')'){
                                str = removeCharAt(str, str.length());
                            }
                        }
                        stringOCR.add(str);
                        str = "";
                        charJPN = ' ';
                    }
                }
            }
            str += charJPN;
        }

        str = str.replaceAll("\\s","");
        stringOCR.add(str);
        checkWord(stringOCR);
        Log.v("str form checkWord ", str);
    }

    private void checkWord (ArrayList<String> strOCR){
        //ค่าที่ได้จากฉลากภาษาญี่ปุ่น
        ArrayList<String> cwList = new ArrayList<String>();
        cwList1 = new ArrayList<String>();
        for (int i = 0; i < strOCR.size(); i++) {
            ocrDB(strOCR.get(i).toString(), cwList); //วนลูปเทียบค่าในดาต้าเบส
            ocrDB1(strOCR.get(i).toString(), cwList1);
        }
        Log.v("ค่าที่ได้จากฉลาก cwList", cwList.toString());
        Log.v("cwList1", cwList1.toString());
        compareDB(cwList); //เอาค่าเข้าไปเทียบกัน แล้วแสดงผลลัพท์ออกมา
    }
    public static String removeCharAt(String s, int pos) {
        if (pos == 0){
            return s.substring(1, s.length());
        }else{
            Log.v("subString", s.substring(0, pos-1));
            return s.substring(0, pos-1);
        }

    }

    private void startDetect() {
        Bitmap bitmap = (Bitmap) Cache.getInstance().getLru().get("bitmap_image");
        String data = CloudVision.convertBitmapToBase64String(bitmap);
        CVRequest request = createCVRequest(data);
        CloudVision.runImageDetection(apiKey, request, this);
    }

    private CVRequest createCVRequest(String data) {
        CVRequest.Image image = new CVRequest.Image(data);
        CVRequest.Feature feature = new CVRequest.Feature(CVRequest.FeatureType.TEXT_DETECTION, 5);
        List<CVRequest.Feature> featureList = new ArrayList<>();
        featureList.add(feature);
        List<CVRequest.Request> requestList = new ArrayList<>();
        requestList.add(new CVRequest.Request(image, featureList));
        return new CVRequest(requestList);
    }

    private void setCVResponse(CVResponse cvResponse) {
        if (cvResponse != null && cvResponse.isResponsesAvailable()) {
            CVResponse.Response response = cvResponse.getResponse(0);
            if (response.isTextAvailable()) {
                List<CVResponse.EntityAnnotation> responseText = response.getTexts();
                descriptionGetText = responseText.get(0).getDescription();
                Log.v("des", descriptionGetText);
                splitSymbol(descriptionGetText.replaceAll("\\s", ""));
                //hideLoading();
            }
        }
    }

    private ArrayList<String> ocrDB(String OCRtext, ArrayList ListOCR) {

         //function ocrDB
         //loop in database check thai col add to ocrArray

        Cursor mCursorOCR;
        mCursorOCR = mDb.rawQuery("SELECT " + myDBclass.COL_THAI +
                " FROM " + myDBclass.TABLE_NAME + " WHERE " + myDBclass.COL_JPN1 + " = '" + OCRtext + "'" +
                "OR " + myDBclass.COL_JPN2 + " = '" + OCRtext + "'" + "OR " + myDBclass.COL_JPN3 + " = '" + OCRtext + "'" +
                "OR " + myDBclass.COL_JPN4 + " = '" + OCRtext + "'" , null);
        //+ "OR " + myDBclass.COL_JPN5 + " = '" + OCRtext + "'"

        if (mCursorOCR.getCount() == 0) {
            ListOCR.add(OCRtext);
        } else {
            mCursorOCR.moveToFirst();
            while (!mCursorOCR.isAfterLast()) {
                ListOCR.add(mCursorOCR.getString(mCursorOCR.getColumnIndex(myDBclass.COL_THAI)));
                mCursorOCR.moveToNext();
            }
        }
        return ListOCR;
    }

    private ArrayList<String> ocrDB1(String ThaiOCRtext, ArrayList cwList1) {
        Cursor mCursorOCR;
        mCursorOCR = mDb.rawQuery("SELECT " + myDBclass.COL_THAI +
                " FROM " + myDBclass.TABLE_NAME + " WHERE " + myDBclass.COL_JPN1 + " = '" + ThaiOCRtext + "'" +
                "OR " + myDBclass.COL_JPN2 + " = '" + ThaiOCRtext + "'" + "OR " + myDBclass.COL_JPN3 + " = '" + ThaiOCRtext + "'" +
                "OR " + myDBclass.COL_JPN4 + " = '" + ThaiOCRtext + "'" , null);
        //+ "OR " + myDBclass.COL_JPN5 + " = '" + ThaiOCRtext + "'"

        if (mCursorOCR.getCount() == 0) {
            cwList1.add(ThaiOCRtext);
        } else {
            mCursorOCR.moveToFirst();
            while (!mCursorOCR.isAfterLast()) {
                cwList1.add(ThaiOCRtext + " : " + mCursorOCR.getString(mCursorOCR.getColumnIndex(myDBclass.COL_THAI)));
                mCursorOCR.moveToNext();
            }
        }
        return cwList1;
    }

    private void compareDB(ArrayList<String> ocrArrayCheck) {
        //function compareDBเทียบค่าที่ได้จากฉลากกับค่าที่ผู้ใช้เลอกไว้ในตอนแรก
        //เรียกค่าจากดาต้าเบสออกมา
        Cursor cursor1, cursor2, cursor3, cursor4;
        cursor1 = mDb.rawQuery("SELECT " + myDBclass.COL_THAI +
                " FROM " + myDBclass.TABLE_NAME + " WHERE " + myDBclass.COL_TYPE1 + " = 1", null);
        cursor2 = mDb.rawQuery("SELECT " + myDBclass.COL_THAI +
                " FROM " + myDBclass.TABLE_NAME + " WHERE " + myDBclass.COL_TYPE2 + " = 1", null);

        ArrayList<String> dirArray1 = new ArrayList<String>();
        ArrayList<String> dirArray2 = new ArrayList<String>();

        cursor1.moveToFirst();
        while (!cursor1.isAfterLast()) {
            dirArray1.add(cursor1.getString(cursor1.getColumnIndex(myDBclass.COL_THAI)));
            cursor1.moveToNext();
        }

        cursor2.moveToFirst();
        while (!cursor2.isAfterLast()) {
            dirArray2.add(cursor2.getString(cursor2.getColumnIndex(myDBclass.COL_THAI)));
            cursor2.moveToNext();
        }

        String[] shell, rice, egg, bean, milk, beef, pork, fish, shrimp;
        shell = new String[]{"ปลาหมึก (squid)", "ปลาหมึกกล้วย", "หอย (shellfish)", "หอยเป๋าฮื๊อ (abalone)",
                                "หอยแมลงภู่ (mussels)", "หอยนางรม (oyster)"};
        rice = new String[]{"ข้าวและแป้งข้าว (rice & flour)", "ข้าวสาลีและแป้งข้าวสาลี (wheat & wheat flour)", "ข้าวบาร์เลย์ (barley)",
                            "ข้าวฟ่าง (millet)", "ข้าวโอ้ต (oat)", "กลูเต็น/โปรตีนในแป้งสาลี (gluten)", "สารไกลอาติน (glyodene)","รำข้าว (rice bran)"};
        egg = new String[]{"ไข่ขาวและโปรตีนไข่ขาว (white egg)", "ไข่แดง (egg yolk)", "mushbooh อิมัลซิไฟเออร์ (emulsifier)"};
        bean = new String[]{"ถั่วแดง (red beans)", "พีแคนนัท (peacan)", "ถั่วเหลืองและน้ำมันถั่วเหลือง (beans oil & soya beans)",
                            "ถั่วปากอ้า (broad beans/horse beans)", "ถั่วลิสง (peanuts)", "อัลมอนต์ (almonds)", "เฮเซลนัท (hazelnut)",
                            "วอลนัทและน้ำมันวอลนัท (walnuts)", "เม็ดมะม่วงหิมพานต์ (cashew nuts)", "เนยถั่ว (peanuts butter)",
                            "ซอสถั่วเหลือง (soy sauce)", "แมคคาเดเมีย (macadamia)", "นมถั่วเหลือง (soy milk)"};
        milk = new String[]{"นมถั่วเหลือง (soy milk)", "นม (cow milk)", "ครีม (whipped cream)", "เคซีน/โปรตีนจากนม (caesin)", "นมแพะ (goat milk)",
                            "นมแกะ (sheep milk)", "หางนม (skim milk)", "น้ำตาลนม/แลคโตส (lactose)", "นมเปรี้ยว/โยเกิร์ต (yogurt)",
                            "ชีสและเนย (cheese)", "เนยและไขมันเนย (butter)", "ครีมชีส (cream cheese)", "มอซซาเรลล่าชีส (mozzarella cheese)",
                            "เชดด้าร์ชีส (cheddar cheese)", "เนยอิตาลี (ricotta)", "เนยเทียม (margarine)", "นมผง" };
        beef = new String[]{"ส่วนประกอบสกัดจากเนื้อวัว (beef extract)", "น้ำมันวัว (beef fat)", "ซุปเนื้อ (consomme)", "ผงซุปเนื้อ (consomme powder)",
                            "ส่วนประกอบสกัดจากเนื้อสัตว์ (meat extract)", "มันหมูมันวัว (beef and pork fat)"};
        pork = new String[]{"มันหมูมันวัว (beef and pork fat)", "มันหมู (lard/pork fat)", "ส่วนประกอบสกัดจากเนื้อหมู (pork extract)", "แฮม (ham)"};
        fish = new String[]{"น้ำมันปลา (fish oil)", "ปลาแอนโชวี่ (anchovy)", "ปลาคอด (cod-liver oil)", "ปลาแฮร์ริ่ง (herring fish)",
                            "ปลาแซลมอน (salmon)", "ปลากะพง (bass fish)", "ปลาทูน่า (tuna)", "ไข่ปลาแซลมอน (salmon eggs)",
                            "ปลาดาบ (marlin fish)", "ปลาเทราท์ (trout)"};
        shrimp = new String[]{"กุ้ง (shrimp)", "กุ้งก้ามกราม (lobster)", "กุ้งกุลาดำ", "ปู (crab)"};


        ArrayList<String[]> group = new ArrayList<>();
        group.add(shell);
        group.add(rice);
        group.add(egg);
        group.add(bean);
        group.add(milk);
        group.add(beef);
        group.add(pork);
        group.add(fish);
        group.add(shrimp);


        Map<String, String[]> map = new HashMap<String, String[]>();
        //dictionary ไว้เทียบที่ผู้ใช้เลือกภาษาไทย กับกลุ่มที่เราจัดไว้
        map.put("ถั่ว (nuts & beans)", bean);
        map.put("หอยและปลาหมึก (molluscs)", shell);
        map.put("กุ้งและปู (crustacean)", shrimp);
        map.put("ข้าวและแป้ง (wheat)", rice);
        map.put("ไข่ (egg)", egg);
        map.put("ปลา (fish)", fish);
        map.put("นม (dairy)", milk);


        //วนดูเทีบยค่าในแต่ละ set
        //จและอิสลามจะเช็คจากฐานข้อมูลของแต่ละกลุ่มเทียบกับฉลาก
         //ส่วน set1 กับ set2 จะเช็คจากส่วนผสมที่ผู้ใช้เลือกกับฉลาก
        if (checkSet0 != null) {
            for (String a : checkSet0) {
                if (a.equals("เจและมังสวิรัติ (Vegetarian)")) {
                    for (int i = 0; i < ocrArrayCheck.size(); i++) {
                        boolean retval = dirArray1.contains(ocrArrayCheck.get(i).toString());
                        if (retval == true) {
                            boolean check = compareArray.contains(ocrArrayCheck.get(i).toString());
                            if (check == false) {
                                compareArray.add(ocrArrayCheck.get(i));
                            }
                        }
                    }
                } else if (a.equals("อิสลาม (Islam)")) {
                    for (int i = 0; i < ocrArrayCheck.size(); i++) {
                        boolean retval = dirArray2.contains(ocrArrayCheck.get(i).toString());
                        if (retval == true) {
                            boolean check = compareArray.contains(ocrArrayCheck.get(i).toString());
                            if (check == false) {
                                compareArray.add(ocrArrayCheck.get(i));
                            }
                        }
                    }
                }
            }
        }
        if (checkSet1 != null) {
            for (String a : checkSet1) {
                // a: word from user select
                // checkset ค่าที่ผู้ใช้เลือกไว้ตอนแรก
                // ocrArray ค่าที่ได้จากฉลาก
           //ถ้าอยู่ก็ให้ไปเช็คว่าคำที่จะแสดงอ่ะมันมีอยู่ก่อนแล้วหรือป่าว ถ้ายังไม่มีก็แอดเพิ่มไป
            //แต่ถ้าไม่อยู่ เราก็ไปตรวจดูว่าอยู่ตามกรุ๊ปที่เราจัดไว้มั้ยถ้าใช่ก็ให้แอดลง
                boolean retval = ocrArrayCheck.contains(a);
                if (retval == true) {
                    boolean check = compareArray.contains(a);
                    if (check == false) {
                        compareArray.add(a);
                    }
                }
                //เช็คตาม group ที่แบ่งไว้ว่าตรวจเจอส่วนผสมที่อยู่ในแต่ละ group ที่ผู้ใช้อกไว้ว้หรอเปล่า
                // เช่นผู้ใช้เลือก ปลา ก็มาดูว่า ในส่วนผสมมี น้ำมันปลา, "ปลาไข่", "ไข่ปลาแซลมอน"
                // เพราะปกติจะเช็คตรงตัวตามที่ผู้ใช้ได้เลือก แต่มีส่วนผสมบางชินดที่ถูกจัดกลุ่มไว้จะไม่ถูกแจ้งเตือน
                for (int i = 0; i < group.size(); i++) {
                    if ((group.get(i)).equals(map.get(a))) { //ให้เช็คเฉพาะกลุ่มที่ตรงตามที่ผู้ใช้ได้เลือกไว้
                        for (String wordFromGroup : group.get(i)) {
                            retval = ocrArrayCheck.contains(wordFromGroup);
                            if (retval == true) {
                                boolean check = compareArray.contains(wordFromGroup);
                                if (check == false) {
                                    compareArray.add(wordFromGroup);
                                }
                            }
                        }
                    }
                }

            }
            if (checkSet2 != null) {
                for (String a : checkSet2) {
                    boolean retval = ocrArrayCheck.contains(a);
                    if (retval == true) {
                        boolean check = compareArray.contains(a);
                        if (check == false) {
                            compareArray.add(a);
                        }
                    }
                    for (String wordFromGroup : beef) {
                        retval = ocrArrayCheck.contains(wordFromGroup);
                        if (retval == true) {
                            boolean check = compareArray.contains(wordFromGroup);
                            if (check == false) {
                                compareArray.add(wordFromGroup);
                            }
                        }
                    }
                    for (String wordFromGroup : pork) {
                        retval = ocrArrayCheck.contains(wordFromGroup);
                        if (retval == true) {
                            boolean check = compareArray.contains(wordFromGroup);
                            if (check == false) {
                                compareArray.add(wordFromGroup);
                            }
                        }
                    }
                }
            }
        }
        Log.v("compareArray", compareArray.toString());
        pbLabel = (RelativeLayout) findViewById(R.id.pbLabel);
        alert(compareArray);


    }

    public void alert(ArrayList<String> compareAA){
        //ดูว่าค่าใน compareArray ว่าง?
        // ถ้าไม่ว่างแสดงว่ามีค่าจาก checkbox กับ ฉลากที่ตรงกัน จะแสดงว่า ""พบส่วนประกอบของอาหารที่คุณทานไม่ได้"
        // แล้วแสดงส่วนประกอบที่พบว่าทานไม่ได้ จะมีปุ่ม show all เพื่อให้เลือกแสดงค่าที่ได้ทั้งหมด
        //ถ้าไม่มีจะเอาค่าที่สแกนได้ทั้งหมดมาแสดง
        pbLabel.setVisibility(View.GONE);
        ImageView imageHead = (ImageView) findViewById(R.id.imgHead);
        TextView headDes = (TextView) findViewById(R.id.tvHeadDes);
        TextView listDes = (TextView) findViewById(R.id.tvDes);
        if (compareAA.isEmpty()) {
            //compareAA.addAll(ocrArray);
            adapter = new ArrayAdapter<String>(MainWork.this,
                    android.R.layout.simple_expandable_list_item_1, compareAA);
            lvLabel.setAdapter(adapter);
            imageHead.setImageResource(R.drawable.ok);
            headDes.setText("ไม่พบส่วนผสมของอาหาร\nที่คุณทานไม่ได้");


        } else {
            adapter = new ArrayAdapter<String>(MainWork.this,
                    android.R.layout.simple_expandable_list_item_1, compareAA);
            lvLabel.setAdapter(adapter);
            imageHead.setImageResource(R.drawable.danger);
            headDes.setText("พบส่วนผสมของอาหาร\nที่คุณทานไม่ได้");
            headDes.setTextColor(Color.parseColor("#c33301"));
            listDes.setText("รายการที่ตรวจพบมีดังนี้");


        }

    }

    public void onPause() {
        super.onPause();
        mHelper.close();
        mDb.close();
    }



    @Override
    public void onImageDetectionSuccess(boolean isSuccess, int statusCode, Headers headers, CVResponse cvResponse) {
        setCVResponse(cvResponse);
    }

    @Override
    public void onImageDetectionFailure(Throwable t) {

    }
}


