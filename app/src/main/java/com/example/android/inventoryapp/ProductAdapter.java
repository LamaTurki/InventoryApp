package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

/**
 * Created by lama on 9/28/2017 AD.
 */

public class ProductAdapter extends CursorAdapter {
    public ProductAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView nameTV = view.findViewById(R.id.name);
        TextView priceTV = view.findViewById(R.id.price);
        final TextView quantityTV = view.findViewById(R.id.quantity);
        Button saleButton = view.findViewById(R.id.sale_button);
        String name = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_NAME));
        int price = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRICE));
        int quantity = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY));
        String priceString = Integer.toString((price)) + " $";
        nameTV.setText(name);
        priceTV.setText(priceString);
        quantityTV.setText(Integer.toString(quantity));

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = Integer.parseInt(quantityTV.getText().toString());
                if (quantity == 0)
                    Toast.makeText(context, context.getString(R.string.out_of_stock_msg), Toast.LENGTH_SHORT).show();
                else {
                    quantity--;
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_QUANTITY, quantity);
                    long id = cursor.getLong(cursor.getColumnIndex(ProductEntry._ID));
                    context.getContentResolver().update(ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id), values, null, null);
                }
            }
        });
    }
}
