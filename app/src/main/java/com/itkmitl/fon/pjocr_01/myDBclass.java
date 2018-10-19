package com.itkmitl.fon.pjocr_01;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by FoN on 1/23/2016.
 */
public class myDBclass extends SQLiteOpenHelper {
    //database name
    private static final String DB_NAME = "dbIngredient";

    //database version
    private static final int DB_VERSION = 2;

    //table names
    public static final String TABLE_NAME = "Ingredients";
    //public static final String TABLE_TYPE = "Type";

    //TABLE_DICT COL NAMES
    public static final String COL_JPN1 = "JPN1";
    public static final String COL_JPN2 = "JPN2";
    public static final String COL_JPN3 = "JPN3";
    public static final String COL_JPN4 = "JPN4";
    //public static final String COL_JPN5 = "JPN5";
    public static final String COL_THAI = "Thai";
    public static final String COL_TYPE1 = "Type1";
    public static final String COL_TYPE2 = "Type2";
    public static final String COL_TYPE3 = "Type3";
    public static final String COL_TYPE4 = "Type4";


    Context context;
    public myDBclass(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
        context = ctx;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // CREATE TABLE DICT
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_JPN1 + " TEXT, "
                + COL_JPN2 + " TEXT, "
                + COL_JPN3 + " TEXT, "
                + COL_JPN4 + " TEXT, "
               // + COL_JPN5 + " TEXT, "
                + COL_THAI + " TEXT, "
                + COL_TYPE1 + " INTEGER, "
                + COL_TYPE2 + " INTEGER, "
                + COL_TYPE3 + " INTEGER, "
                + COL_TYPE4 + " INTEGER );");

        Log.d("CREATE TABLE", "Create TABLE_NAME Successfully.");

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(
                            "dbIngredients.csv")));
            String readLine = null;
            readLine = br.readLine();
            try {
                while ((readLine = br.readLine()) != null) {
                    Log.i("Data Input", readLine);
                    String[] str = readLine.split(",");
                    //Log.i("String Input", String.valueOf(str));
                    /*for (String a:str){
                        Log.i("String Input", a);
                    }*/
                    db.execSQL("INSERT INTO " + TABLE_NAME
                            + " (" + COL_JPN1 + ", " + COL_JPN2
                            + ", " + COL_JPN3 + ", " + COL_JPN4
                            + ", " + COL_THAI
                            + ", " + COL_TYPE1 + ", " + COL_TYPE2
                            + ", " + COL_TYPE3 + ", " + COL_TYPE4
                            + ") VALUES ('" + str[0] + "',"
                            + " '" + str[1] + "', '" + str[2] + "',"
                            + " '" + str[3] + "', '" + str[4] + "', '"
                            + str[5] + "', " + str[6] + ", "
                            + str[7] + ", " + str[8] +  " );");

                }
            } catch (IOException e) { }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }



}
