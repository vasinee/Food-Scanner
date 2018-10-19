package com.itkmitl.fon.pjocr_01;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainUser extends AppCompatActivity {

    /* หน้าการจัดการข้อมูลผู้ใช้ ให้ผู้ใช้เลือกประเภทอาหารที่ผู้ใช้ไม่ต้องการรับประทาน
    แสดงเป็น list พร้อม checkbox ให้ผู้ใช้เลือก
    ระบบจะจัดเก็บข้อมูลลง shared preference*/

    //  ExpandableListAdapter listAdapter;
    ExpListViewAdapterWithCheckbox listAdapter;
    ExpandableListView expListView;
    ArrayList<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    SQLiteDatabase mDb;
    myDBclass mHelper;
    Cursor mCursor3, mCursor4;
    HashMap<Integer, boolean[]> mChildCheckStates;
    final String PREF_NAME = "USERPreferences";
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    Set<String> set0,set1,set2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userprofile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();

        //data add array
        mHelper = new myDBclass(this);
        mDb = mHelper.getWritableDatabase();

        mCursor3 = mDb.rawQuery("SELECT " + myDBclass.COL_THAI +
                " FROM " + myDBclass.TABLE_NAME + " WHERE " + myDBclass.COL_TYPE3 + " = 1", null);

        mCursor4 = mDb.rawQuery("SELECT " + myDBclass.COL_THAI +
                " FROM " + myDBclass.TABLE_NAME + " WHERE " + myDBclass.COL_TYPE4 + " = 1", null);


        ArrayList<String> dirArray3 = new ArrayList<String>();
        ArrayList<String> dirArray4 = new ArrayList<String>();

        mCursor3.moveToFirst();
        while (!mCursor3.isAfterLast()) {
            //String s = URLDecoder.decode(myString, "UTF-8");
            dirArray3.add(mCursor3.getString(mCursor3.getColumnIndex(myDBclass.COL_THAI)));
            mCursor3.moveToNext();
        }

        mCursor4.moveToFirst();
        while (!mCursor4.isAfterLast()) {
            dirArray4.add(mCursor4.getString(mCursor4.getColumnIndex(myDBclass.COL_THAI)));
            mCursor4.moveToNext();
        }

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        // preparing list data
        prepareListData(dirArray3, dirArray4);

        mChildCheckStates = GetmChildCheckStates();



        listAdapter = new ExpListViewAdapterWithCheckbox(this, listDataHeader, listDataChild,mChildCheckStates);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        // Listview Group click listener
        expListView.setOnGroupClickListener(new OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                // Toast.makeText(getApplicationContext(),
                // "Group Clicked " + listDataHeader.get(groupPosition),
                // Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    /*
     * Preparing the list data
     * checkbox status check
     */
    public HashMap<Integer, boolean[]> GetmChildCheckStates(){
        set0 = sp.getStringSet("set0", null);
        set1 = sp.getStringSet("set1", null);
        set2 = sp.getStringSet("set2", null);

        List<String> s0, s1, s2;
        s0 = listDataChild.get("ตามความเชื่อและศาสนา");
        s1 = listDataChild.get("ส่วนผสมที่มีสารก่อภูมิแพ้");
        s2 = listDataChild.get("รายการอื่นๆ");

        /* ตั้งค่า checkbox ถ้าเป็นตัวที่เคยเลือกอยู่แล้วให้ check ไว้
        ถ้าไม่ใช่ก็ไม่ต้อง check */
        HashMap<Integer, boolean[]> ChildCheckStates;
        ChildCheckStates = new HashMap<Integer, boolean[]>();

        boolean[] state0;
        state0 = new boolean[s0.size()];
        for (int i = 0; i < s0.size(); i++){
            if (set0.contains(s0.get(i))){
                state0[i] = true;
            }else {
                state0[i] = false;
            }
        }

        boolean[] state1;
        state1 = new boolean[s1.size()];
        for (int i = 0; i<s1.size(); i++){
            if (set1.contains(s1.get(i))){
                state1[i] = true;
            }else {
                state1[i] = false;
            }
        }

        boolean[] state2;
        state2 = new boolean[s2.size()];
        for (int i = 0; i<s2.size(); i++){
            if (set2.contains(s2.get(i))){
                state2[i] = true;
            }else {
                state2[i] = false;
            }
        }
        ChildCheckStates.put(0, state0);
        ChildCheckStates.put(1,state1);
        ChildCheckStates.put(2,state2);

        return ChildCheckStates;
    }
    private void prepareListData(ArrayList dirArray3, ArrayList dirArray4) {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        /* จัดหมวดหมู่คำศัพท์ แบบปลาก็ให้โชว์แค่ปลาคำเดียว ปลาอื่นๆซ่อนไว้
        เอาตัวที่ไม่ต้องการให้โชว์มาเก็บไว้ตรงนี้ */
        Set<String> arrayJPN = new HashSet<String>(Arrays.asList(new String[]{
                "mushbooh กรดอะมิโน (amino acid)", "mushbooh ชอทเทนนิ่ง/เนยขาว (shortening)", "mushbooh ยีสต์ (yeast)",
                "mushbooh อิมัลซิไฟเออร์ (emulsifier)", "mushbooh เยลลี่และเจลาติน (jelly & gelatin)", "mushbooh ไขมันที่ผ่านกระบวนการ additional fat",
                "กลั่นจากแอลกอฮอล์ (liquor)", "กลูเต็น/โปรตีนในแป้งสาลี (gluten)", "กุยช่าย (chinese leek)", "กุ้ง (shrimp)", "กุ้งกุลาดำ", "กุ้งก้ามกราม (lobster)",
                "ข้าวบาร์เลย์ (barley)", "ข้าวฟ่าง (millet)", "ข้าวสาลีและแป้งข้าวสาลี (wheat & wheat flour)", "ข้าวและแป้งข้าว (rice & flour)",
                "ข้าวโอ้ต (oat)", "ครีม (whipped cream)", "ชอทเทนนิ่งที่ทำจากสัตว์ animal shortening",
                "ซอสถั่วเหลือง (soy sauce)", "ซุปเนื้อ (consomme)", "ต้นหอม (welsh onion)", "ถั่วปากอ้า (broad beans & horse beans)",
                "ถั่วลิสง (peanuts)", "ถั่วเหลืองและน้ำมันถั่วเหลือง (beans oil & soya beans)", "ถั่วแดง (red beans)", "นม (cow milk)", "นมถั่วเหลือง (soy milk)",
                "นมเปรี้ยว/โยเกิร์ต (yogurt)", "นมแกะ (sheep milk)", "นมแพะ (goat milk)", "น้ำตาลนม/แลคโตส (lactose)", "น้ำมันปลา (fish oil)",
                "ชีสและเนย (cheese)", "เนยและไขมันเนย (butter)", "ครีมชีส (cream cheese)", "มอซซาเรลล่าชีส (mozzarella cheese)",
                "เชดด้าร์ชีส (cheddar cheese)", "เนยอิตาลี (ricotta)", "เนยเทียม (margarine)", "นมผง",
                "น้ำมันวัว (beef fat)", "บรั่นดี (brandy)", "บุหรี่ยาเส้น (shredded cigarette)", "ปลากะพง (bass fish)", "ปลาคอด (cod-liver oil)",
                "ปลาดาบ (marlin fish)", "ปลาทูน่า (tuna)", "ปลาหมึก (squid)", "ปลาหมึกกล้วย", "ปลาเทราท์ (trout)", "ปลาแซลมอน (salmon)", "ปลาแอนโชวี่ (anchovy)",
                "ปลาแฮร์ริ่ง (herring fish)", "ปู (crab)", "ผงซุปเนื้อ (consomme powder)", "พีแคนนัท (peacan)",
                "มันหมู (lard/pork fat)", "มันหมูมันวัว (beef and pork fat)", "มิริน (sweet sake)", "รัมและรัมเรซิน (rum & rum resin)", "รำข้าว (rice bran)",
                "วอลนัทและน้ำมันวอลนัท (walnuts)", "วิสกี้/เหล้าขาว (whiskey)", "สารไกลอาติน (glyodene)", "สาเกญี่ปุ่น (Japanese rice wine)", "ส่วนประกอบสกัดจากเนื้อวัว (beef extract)",
                "ส่วนประกอบสกัดจากเนื้อสัตว์ (meat extract)", "ส่วนประกอบสกัดจากเนื้อหมู (pork extract)", "ส่วนประกอบสกัดจากเนื้อไก่ (chicken extract)", "หอมแดง (shallot)",
                "หอย (shellfish)", "หอยนางรม (oyster)", "หอยเป๋าฮื๊อ (abalone)", "หอยแมลงภู่ (mussels)", "หัวหอมและหอมต่างๆ (onion)", "หางนม (skim milk)",
                "อัลมอนต์ (almonds)", "เคซีน/โปรตีนจากนม (caesin)", "เนยถั่ว (peanuts butter)",
                "เนื้อเป็ด (duck meat)", "เนื้อแกะ (lamb)", "เนื้อไก่ (chicken meat)", "เม็ดมะม่วงหิมพานต์ (cashew nuts)",
                "เหล้าญี่ปุ่น (โกเซเซชู) mixed refined sake", "เหล้าและสุรา (western liquor)", "เอทิลแอลกอฮอล์/เอทานอล (ethanol)", "เฮเซลนัท (hazelnut)", "แมคคาเดเมีย (macadamia)",
                "แอลกอฮอล์ (alcohol)", "แฮม (ham)", "ใบยาสูบ (cigarette)", "ไขมัน (fat)", "ไขมันสัตว์ (animal fat)", "ไข่ขาวและโปรตีนไข่ขาว (white egg)",
                "ไข่ปลาแซลมอน (salmon eggs)", "ไข่แดง (egg yolk)", "ไวน์ (wine)", "ไส้กรอก (sausage)", "ถั่วปากอ้า (broad beans/horse beans)"


        }));


        // Adding child data
        listDataHeader.add("ตามความเชื่อและศาสนา");
        listDataHeader.add("ส่วนผสมที่มีสารก่อภูมิแพ้");
        listDataHeader.add("รายการอื่นๆ");

        // Adding child data
        List<String> Array0 = new ArrayList<String>();
        Array0.add("เจและมังสวิรัติ (Vegetarian)");
        Array0.add("อิสลาม (Islam)");

        List<String> Array1 = new ArrayList<String>();
        int sizeType3 = dirArray3.size();
        for (int i = 0; i < sizeType3; i++) {
            boolean retval = arrayJPN.contains(dirArray3.get(i).toString());
            if (retval != true) {
                Array1.add((String) dirArray3.get(i));
            }
        }

        List<String> Array2 = new ArrayList<String>();
        int sizeType4 = dirArray4.size();
        for (int i = 0; i < sizeType4; i++) {
            boolean retval = arrayJPN.contains(dirArray4.get(i).toString());
            if (retval != true) {
                Array2.add((String) dirArray4.get(i));
            }
        }

        listDataChild.put(listDataHeader.get(0), Array0); // Header, Child data
        listDataChild.put(listDataHeader.get(1), Array1);
        listDataChild.put(listDataHeader.get(2), Array2);
    }

    public void onPause() {
        super.onPause();
        mHelper.close();
        mDb.close();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {

            /*เก็บค่าส่วนผสมที่ผู้ใช้เลือกลง Preference Shared
            * set0 เจและมังสวิรัติ และอิสลาม
            * set1 ภูมิแพ้
            * set2 รายการอื่นๆ */

            Set<String> set0 = new HashSet<String>();
            set0.addAll(listAdapter.getCheckedItemsInGroup().get(listDataHeader.get(0)));
            editor.putStringSet("set0", set0);

            Set<String> set1 = new HashSet<String>();
            set1.addAll(listAdapter.getCheckedItemsInGroup().get(listDataHeader.get(1)));
            editor.putStringSet("set1", set1);

            Set<String> set2 = new HashSet<String>();
            set2.addAll(listAdapter.getCheckedItemsInGroup().get(listDataHeader.get(2)));
            editor.putStringSet("set2", set2);

            Set<String> setAll = new HashSet<String>();
            setAll.addAll(listAdapter.getCheckedItemsInGroup().get(listDataHeader.get(0)));
            setAll.addAll(listAdapter.getCheckedItemsInGroup().get(listDataHeader.get(1)));
            setAll.addAll(listAdapter.getCheckedItemsInGroup().get(listDataHeader.get(2)));
            editor.putStringSet("setAll", setAll);
            editor.commit();

            Set<String> listSetAll = sp.getStringSet("setAll", null); // list ตัวเลือกที่ผู้ใช้เลือกทั้งหมด
            String[] items = listSetAll.toArray(new String[listSetAll.size()]); // ทำให้เป็น String[]

            // AlertDialog แสดงส่วนผสมทั้งหมดที่ผู้ใช้ได้เลือกไว้ ถ้าตกลงกลับไปหน้าหลัก ยกเลิกให้กลับไปเลือกใหม่
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainUser.this);
            LayoutInflater inflater = getLayoutInflater();
            View convertView = (View) inflater.inflate(R.layout.custom, null);
            alertDialog.setView(convertView);
            alertDialog.setTitle("ส่วนผสมที่คุณเลือก");
            ListView lv = (ListView) convertView.findViewById(R.id.listView1);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, items);
            lv.setAdapter(adapter);
            alertDialog.setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alertDialog.setNegativeButton("ยกเลิก", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
            alertDialog.show();

            //ตรวจสอบค่าใน logcat
            Map<String, ?> allEntries = sp.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            }
            //finish();
        }
        return super.onOptionsItemSelected(item);

    }

}
