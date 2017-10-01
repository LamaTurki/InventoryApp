package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

import java.io.IOException;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    Uri mUri;
    int quantity = 0;
    Bitmap mBitmap = null;
    EditText nameEditText;
    EditText priceEditText;
    TextView quantityTextView;
    ImageView imageView;
    Button selectImageButton;
    Button incrementButton;
    Button decrementButton;
    private int PICK_IMAGE_REQUEST = 1;
    private boolean mDataHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mDataHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        nameEditText = (EditText) findViewById(R.id.edit_name);
        priceEditText = (EditText) findViewById(R.id.edit_price);
        quantityTextView = (TextView) findViewById(R.id.quantity_text_view);
        imageView = (ImageView) findViewById(R.id.imageview);
        selectImageButton = (Button) findViewById(R.id.select_image_button);
        incrementButton = (Button) findViewById(R.id.increment_button);
        decrementButton = (Button) findViewById(R.id.decrement_button);
        mUri = getIntent().getData();
        if (mUri == null) {
            setTitle(getString(R.string.detail_activity_title_add));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.detail_activity_title_edit));
            getSupportLoaderManager().initLoader(0, null, this);
        }
        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantity++;
                quantityTextView.setText(Integer.toString(quantity));
            }
        });
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity == 0)
                    return;
                quantity--;
                quantityTextView.setText(Integer.toString(quantity));
            }
        });
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //resource: http://codetheory.in/android-pick-select-image-from-gallery-with-intents/
                Intent intent = new Intent();
// Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
// Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });
        nameEditText.setOnTouchListener(mTouchListener);
        priceEditText.setOnTouchListener(mTouchListener);
        incrementButton.setOnTouchListener(mTouchListener);
        decrementButton.setOnTouchListener(mTouchListener);
        selectImageButton.setOnTouchListener(mTouchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_item);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_delete_item:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_order_more:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setData(Uri.parse("mailto:"));
                intent.setType("text/email");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"supplier@example.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Order more " + nameEditText.getText().toString().trim());
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                return true;
            case R.id.action_save:
                saveItem();
                finish();
                return true;
            case android.R.id.home:
                if (!mDataHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveItem() {
        ContentValues values = new ContentValues();
        String name = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        if (mUri == null && TextUtils.isEmpty(name) && TextUtils.isEmpty(priceString) && quantity == 0 && mBitmap == null)
            return;
        int price = 0;
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, getString(R.string.missing_product_name),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, R.string.missing_product_price,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (mBitmap == null) {
            Toast.makeText(this, R.string.missing_product_image,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        values.put(ProductEntry.COLUMN_NAME, name);
        values.put(ProductEntry.COLUMN_PRICE, Integer.parseInt(priceString));
        values.put(ProductEntry.COLUMN_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_IMAGE, DbBitmapUtility.getBytes(mBitmap));
        if (mUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.error_with_saving_product),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.successful_insertion),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowUpdated = getContentResolver().update(mUri, values, null, null);
            if (rowUpdated == 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, R.string.failed_update,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, R.string.successful_update,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.yes, discardButtonClickListener);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void onBackPressed() {
        if (!mDataHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        int rowDeleted = getContentResolver().delete(mUri, null, null);
        if (rowDeleted == 0) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, R.string.failed_delete,
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, R.string.successful_delete,
                    Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    // resource: http://codetheory.in/android-pick-select-image-from-gallery-with-intents/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageView.setImageBitmap(mBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_NAME,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_IMAGE};
        return new CursorLoader(this, mUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            nameEditText.setText(data.getString(data.getColumnIndex(ProductEntry.COLUMN_NAME)));
            priceEditText.setText(Integer.toString(data.getInt(data.getColumnIndex(ProductEntry.COLUMN_PRICE))));
            quantity = data.getInt(data.getColumnIndex(ProductEntry.COLUMN_QUANTITY));
            quantityTextView.setText(Integer.toString(quantity));
            byte[] imageBytes = data.getBlob(data.getColumnIndex(ProductEntry.COLUMN_IMAGE));
            if (imageBytes != null)
                imageView.setImageBitmap(DbBitmapUtility.getImage(imageBytes));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.setText("");
        priceEditText.setText("");
        quantityTextView.setText("0");
        imageView.setImageResource(0);
    }
}
