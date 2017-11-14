package fi.jamk.l3329.stockpile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 7.11.2017.
 */

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    //Databases are stored in the /data/data/<package name>/databases folder.

    private static final String DATABASE_NAME = "database.db";
    private final String DATABASE_TABLE = "shopping_list";
    private final String NAME = "name";
    private final String AMOUNT = "count";
    private final String PRICE = "price";
    private final String UNIT = "unit";
    private final String CURRENCY = "currency";
    private final String BOUGHT = "bought"; //not boolean but integer >> 0- false,1- true

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null , 1);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        //create a new table
        db.execSQL("CREATE TABLE " + DATABASE_TABLE
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME + " TEXT, "
                + AMOUNT + " REAL, "
                + PRICE + " REAL, "
                + UNIT + " TEXT, "
                + CURRENCY + " TEXT, "
                + BOUGHT + " INTEGER"+");");

        ContentValues values = new ContentValues();
        values.put(NAME, "water");
        values.put(AMOUNT, "1");
        values.put(UNIT, "l");
        values.put(PRICE, "3.5");
        values.put(CURRENCY, "€");
        values.put(BOUGHT, "0");

        db.insert(DATABASE_TABLE, null, values);

        values.put(NAME, "bread");
        values.put(AMOUNT, "2");
        values.put(PRICE, "2.0");
        values.put(UNIT, "pcs");
        values.put(CURRENCY, "€");
        values.put(BOUGHT, "0");

        db.insert(DATABASE_TABLE, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }

    // Adding new item
    public void addItem(Item item) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NAME, item.getName());
        values.put(AMOUNT, item.getAmount());
        values.put(PRICE,item.getPrice());
        values.put(UNIT, item.getUnits());
        values.put(CURRENCY, item.getCurrency());
        values.put(BOUGHT, item.isBought());

        // Inserting Row
        db.insert(DATABASE_TABLE, null, values);
        db.close(); // Closing database connection

    }

    // Getting single item
    public Item getItem(int id) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db
                .query(DATABASE_TABLE,
                        new String[] { "_id", NAME, AMOUNT, UNIT,PRICE,CURRENCY,BOUGHT }, "_id" + "=?",
                        new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        int i = 0;
        Item item = new Item.ItemBuilder()
                .id(cursor.getInt(i++))
                .name(cursor.getString(i++))
                .amount(cursor.getFloat(i++))
                .price(cursor.getFloat(i++))
                .units(cursor.getString(i++))
                .currency(cursor.getString(i++))
                .isBought(cursor.getInt(i++) != 0)
                .build();
        // return contact
        return item;
    }

    // Getting All items
    public List<Item> getAllItems() {

        List<Item> itemList = new ArrayList<Item>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + DATABASE_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                int i =0;
                Item item = new Item.ItemBuilder()
                        .id(cursor.getInt(i++))
                        .name(cursor.getString(i++))
                        .amount(cursor.getFloat(i++))
                        .price(cursor.getFloat(i++))
                        .units(cursor.getString(i++))
                        .currency(cursor.getString(i++))
                        .isBought(cursor.getInt(i++) != 0)
                        .build();
                // Adding item to list
                itemList.add(item);
            } while (cursor.moveToNext());
        }

        // return contact list
        return itemList;
    }

    // Getting items Count
    public int getItemsCount() {
        String countQuery = "SELECT  * FROM " + DATABASE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    // Updating single item
    public int updateItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NAME, item.getName());
        values.put(AMOUNT, item.getAmount());
        values.put(PRICE,item.getPrice());
        values.put(UNIT, item.getUnits());
        values.put(CURRENCY, item.getCurrency());
        values.put(BOUGHT, item.isBought());

        // updating row
        return db.update(DATABASE_TABLE, values, "_id" + " = ?",
                new String[] { String.valueOf(item.getId()) });
    }

    // Deleting single item
    public void deleteItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(DATABASE_TABLE, "_id" + " = ?",
                new String[] { String.valueOf(item.getId()) });


        db.close();
    }

}