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

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.database_classes.PetContract;
import com.example.android.pets.database_classes.PetContract.PetEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private PetAdapter petAdapter;
    private static final int PET_LOADER_ID = 1;
    private Cursor cursor;
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

        ListView listView = findViewById(R.id.pet_list_view);
        //Setting the empty view
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);
        //Setting the adapter
        petAdapter = new PetAdapter(this,null);
        listView.setAdapter(petAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long item_id) {
                //Intent to go to the editor activity
                Intent editModeEditorActivity = new Intent(CatalogActivity.this,EditorActivity.class);

                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.MAIN_CONTENT_URI,item_id);
                editModeEditorActivity.setData(currentPetUri);
                startActivity(editModeEditorActivity);
            }
        });
        //Start or Initialize the loader
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(PET_LOADER_ID,null,this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }
    private void deleteAllRecords()
    {
        int deletedAllRecords = getContentResolver().delete(PetEntry.MAIN_CONTENT_URI,null,null);
        if(deletedAllRecords == 0)
        {
            return;
        }
        else{
            Toast.makeText(this,this.getString(R.string.action_delete_all_entries),Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                // Do nothing for now
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteAllPetsConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                PetContract.PetEntry.COLUMN_PET_ID,
                PetContract.PetEntry.COLUMN_PET_NAME,
                PetContract.PetEntry.COLUMN_PET_BREED,
                PetContract.PetEntry.COLUMN_PET_GENDER,
                PetContract.PetEntry.COLUMN_PET_WEIGHT
        };
        //Creates a new loader and automatically queries the provider and receives a Cursor object
        return new CursorLoader(this,PetEntry.MAIN_CONTENT_URI,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //Used to change the current reference to latest cursor object returned
        petAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Called when the data needs to be deleted
        petAdapter.swapCursor(null);
    }
    //Confirmation dialog to delete all the pets
    private void showDeleteAllPetsConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_msg);
        builder.setPositiveButton(R.string.delete_all_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteAllRecords();
            }
        });
        builder.setNegativeButton(R.string.delete_all_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
