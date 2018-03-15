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
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.database_classes.PetContract;
import com.example.android.pets.database_classes.PetContract.PetEntry;
import com.example.android.pets.database_classes.PetDBHelper;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int EDITOR_LOADER_ID = 0;
    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    //Uri received from the catalog activity
    private Uri currentPetUri;

    private boolean mPetHasChanged = false;

    //The onTouchListener reusable listener for spinner
    private View.OnTouchListener mOnTouchListener= new View.OnTouchListener(){
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent fromCatalogActivity = getIntent();
        currentPetUri = fromCatalogActivity.getData();

        if(currentPetUri == null){
            setTitle(getString(R.string.add_pet_title));
            //As the pet is to be inserted therefore the options menu to
            //delete something is not required
            invalidateOptionsMenu();
        } else{
          setTitle(getString(R.string.editor_activity_title_edit_pet));
            getLoaderManager().initLoader(EDITOR_LOADER_ID,null,this);
        }
        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
        //Set the onTouchListener to recognize any change in editor activity pet details
        mNameEditText.setOnTouchListener(mOnTouchListener);
        mBreedEditText.setOnTouchListener(mOnTouchListener);
        mGenderSpinner.setOnTouchListener(mOnTouchListener);
        mWeightEditText.setOnTouchListener(mOnTouchListener);
        setupSpinner();
    }
    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }
    public void savePetRecord() {
            String petInfo[] = {
                    mNameEditText.getText().toString(),
                    mBreedEditText.getText().toString(),
                    mWeightEditText.getText().toString() };
            ContentValues contentValues = new ContentValues();
            //contentValues.put(PetEntry.COLUMN_PET_ID,1);
            contentValues.put(PetEntry.COLUMN_PET_NAME, petInfo[0]);
            contentValues.put(PetEntry.COLUMN_PET_BREED, petInfo[1]);
            contentValues.put(PetEntry.COLUMN_PET_GENDER, mGender);
            contentValues.put(PetEntry.COLUMN_PET_WEIGHT, petInfo[2]);
            int numOfRowsUpdated = 0;
          if(currentPetUri == null) {
              if(TextUtils.isEmpty(petInfo[0])){
                  Toast.makeText(this,getString(R.string.no_name_for_editor),Toast.LENGTH_LONG).show();
                  return;
              }
              if((mGenderSpinner.getSelectedItem()+"").equals(getString(R.string.gender_unknown))){
                  Toast.makeText(this,getString(R.string.no_gender_for_editor),Toast.LENGTH_LONG).show();
                  return;
              }
              if(TextUtils.isEmpty(petInfo[2])) {
                  Toast.makeText(this,getString(R.string.no_weight_for_editor),Toast.LENGTH_LONG).show();
              }
            Uri uriOfInsert = null;
            try {
                uriOfInsert = getContentResolver().insert(PetEntry.MAIN_CONTENT_URI, contentValues);
            } catch (IllegalArgumentException e) {
                Log.i("Exception in Editor", e + "");
                e.printStackTrace();
            } catch (Exception e) {
                Log.i("Some other exception", "In editor activity " + e);
                e.printStackTrace();
            }
            Log.i("Values Inserted ", " --> true");
            if (uriOfInsert == null) {
                Toast.makeText(this, getString(R.string.error_save), Toast.LENGTH_LONG).show();
            } else {
                //Correct the bug where the id of pets still increments from the previous value
                Toast.makeText(this, getString(R.string.pet_save), Toast.LENGTH_LONG).show();
            }
        } else{
            try {
                numOfRowsUpdated = getContentResolver().update(currentPetUri,contentValues,null,null);
            }
            catch (IllegalArgumentException e){
                Log.i("Exception in Editor","--> While updating");
                e.printStackTrace();
            } catch (Exception e) {
                Log.i("Some other exception","In editor activity while updated"+e);
                e.printStackTrace();
            }
            if(numOfRowsUpdated == 0){
                Toast.makeText(this,getString(R.string.pet_update_failed),Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this,getString(R.string.pet_update_successfull),Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId())
        {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                savePetRecord();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeletePetConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if(!mPetHasChanged)
                {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //User pressed "Discard" --> Navigate back to Catalog Activity
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //Deactivate the delete option menu when EditorActvity opens in add a pet mode
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(currentPetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT };
        return new CursorLoader(
                this,
                currentPetUri,
                projection,
                null,
                null,
                null );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
        mNameEditText.setText(cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME)));
        mBreedEditText.setText(cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED)));
        mGender = cursor.getInt(cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER));
        mGenderSpinner.setSelection(mGender);
        mWeightEditText.setText(cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT)));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mGenderSpinner.setSelection(0);
        mWeightEditText.setText("");
    }
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.change_discard_confirmation));
        builder.setPositiveButton(R.string.discard,discardButtonListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInterface != null)
                {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @Override
    public void onBackPressed()
    {
        if(!mPetHasChanged)
        {
            super.onBackPressed();
            return;
        }
        //Create a new discard button listener
        DialogInterface.OnClickListener discardButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        //Show and create the builder by calling this method
        showUnsavedChangesDialog(discardButtonListener);
    }
    public void showDeletePetConfirmationDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.delete_dialog_msg);
        alert.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deletePet();
            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }
    public void deletePet() {
        if(currentPetUri != null) {
            int numOfRowsDeleted = getContentResolver().delete(currentPetUri, null, null);
            if (numOfRowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_delete_pet_successful, Toast.LENGTH_SHORT).show();
            }
        }
    }
}