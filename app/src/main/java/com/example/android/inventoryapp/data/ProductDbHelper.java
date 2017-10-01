package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lama on 9/28/2017 AD.
 */

public class ProductDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_STATMENT = "CREATE TABLE " + InventoryContract.ProductEntry.TABLE_NAME + " ( " +
                InventoryContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                InventoryContract.ProductEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                InventoryContract.ProductEntry.COLUMN_PRICE + " INTEGER DEFAULT 0 , " +
                InventoryContract.ProductEntry.COLUMN_QUANTITY + " INTEGER DEFAULT 0 , " +
                InventoryContract.ProductEntry.COLUMN_IMAGE + " BLOB ); ";
        sqLiteDatabase.execSQL(CREATE_STATMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
