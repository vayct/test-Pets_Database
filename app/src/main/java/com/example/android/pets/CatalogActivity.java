/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;

import java.net.URI;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private PetCursorAdapter mPetCursorAdapter;

    private static final int PET_LOADER = 0;

    //to keep track of current number of rows
    private int currentRows;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.

        // Find the ListView which will be populated with the pet data
        ListView petListView = (ListView) findViewById(R.id.list_view);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);


        //Setup the Adapter to create a list item for each row of pet data in the Cursor
        //There is no pet yet so pass null for the cursor
        mPetCursorAdapter = new PetCursorAdapter(this,null);
        petListView.setAdapter(mPetCursorAdapter);

        //set up the click listener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {



                //Create a new intent to go to EditorActivity
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                //Form the content URI that represents the specific pet that was clicked on
                //by appending the "id" (passed as input to this method) onto the PetEntry.CONTENT_URI
                Uri petUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);

                //set the URI on the data field of the intent
                intent.setData(petUri);

                //Launch the activity to display the current pet
                startActivity(intent);
            }
        });


        //initialize the loader
        getLoaderManager().initLoader(PET_LOADER,null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                if(currentRows != 0 )
                        showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Helper method to delete all pets
     */
    private void deleteAllPets() {
       int rowsDeleted =  getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
        Toast.makeText(this, "Removed all pets from the database." , Toast.LENGTH_SHORT).show();
    }

    /**
     * Helper method to insert hardcoded pet data into the database. FOr debugging purposes
     */
    private void insertPet() {

        //Create a content values object where columns names are the keys,
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);


        // Insert a new row for Toto into the provider using the ContentResolver.
        // use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // the pets into the table
        //Receive the new content URI that will allow us to access Toto's data
        Uri newURI = getContentResolver().insert(PetEntry.CONTENT_URI, values);



    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED };

        return new CursorLoader(this,
                PetEntry.CONTENT_URI ,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPetCursorAdapter.swapCursor(data);
        currentRows = data.getCount();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPetCursorAdapter.swapCursor(null);

    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllPets();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
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
}
