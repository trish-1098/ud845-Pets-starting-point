package com.example.android.pets.database_classes;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android.pets.R;
import com.example.android.pets.database_classes.PetContract.PetEntry;

/**
 * Created by trish on 3/6/2018.
 */

public class PetProvider extends ContentProvider {
    PetDBHelper petDBHelper;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    public static final int WHOLE_PETS_TABLE = 010;
    private static final int PARTICULAR_PET = 011;
    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        petDBHelper = new PetDBHelper(getContext());
        return false;
    }
    //This is called everytime something new is run from this class
    static{
        uriMatcher.addURI(PetContract.CONTENT_PROVIDER,PetContract.PATH_TABLE_PETS,WHOLE_PETS_TABLE);
        uriMatcher.addURI(PetContract.CONTENT_PROVIDER,PetContract.PATH_TABLE_PETS+"/#",PARTICULAR_PET);
    }
    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    throws IllegalArgumentException {
        SQLiteDatabase db = petDBHelper.getReadableDatabase();

        Cursor cursor;

        switch(uriMatcher.match(uri))
        {
            case WHOLE_PETS_TABLE:
                //Here we need the whole pets table
                cursor = db.query(
                        PetEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                         sortOrder);
                break;
            case PARTICULAR_PET:
                //Here we need only one row corresponding to the id passed with the uri
                selection = PetEntry.COLUMN_PET_ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(
                        PetEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default: throw new IllegalArgumentException("Cannot query unknown Uri: "+uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }
    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) throws IllegalArgumentException{
        final int match = uriMatcher.match(uri);
        try {
            switch (match) {
                case WHOLE_PETS_TABLE:
                    return insertPet(uri, values);
                default:
                    throw new IllegalArgumentException("Cannot be inserted with invalid object of uri : " + uri);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    private Uri insertPet(Uri uri,ContentValues values) throws IllegalArgumentException
    {
        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if(name == null)
        {
            Toast.makeText(getContext(),getContext().getString(R.string.pet_req),Toast.LENGTH_LONG).show();
            throw new IllegalArgumentException("Pet requires a valid name");
        }
        //String breed = values.getAsString(PetEntry.COLUMN_PET_BREED);
        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires valid gender");
        }
        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if(weight !=  null && weight < 0)
        {
            Toast.makeText(getContext(),getContext().getString(R.string.pet_req_weight),Toast.LENGTH_LONG).show();
            throw new IllegalArgumentException("Pet has to have some name and it cannot be negative");
        }
        //If all above tests are true then get access to the database
        SQLiteDatabase db = petDBHelper.getWritableDatabase();
        long id = db.insert(PetEntry.TABLE_NAME,null,values);
        if(id == -1)
        {
            Log.i("Fail to insert row for",id+"");
            return null;
        }
        //Notify all listeners like CatalogActivity that the content at this uri has changed
        //After this the cursor loader again queries the database and fetches in the new data
        // with new cursor object
        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(uri,id);
    }
    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch(uriMatcher.match(uri)){
            case WHOLE_PETS_TABLE:
                return deletePet(uri,selection,selectionArgs);
            case PARTICULAR_PET:
                selection = PetEntry.COLUMN_PET_ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return deletePet(uri,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("Cannot use invalid uri Object --> "+uri);
        }
    }
    private int deletePet(Uri uri,String selection,String[] selectionArgs){
        SQLiteDatabase db = petDBHelper.getWritableDatabase();
        int numRowsDeleted = db.delete(PetEntry.TABLE_NAME,selection,selectionArgs);
        //Notify all listeners like CatalogActivity that the content at this uri has changed
        //After this the cursor loader again queries the database and fetches in the new data
        // with new cursor object
        if(numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsDeleted;
    }
    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs)
            throws IllegalArgumentException{
        if(values.size() == 0)
        {
            return 0;
        }
        try {
            switch (uriMatcher.match(uri)) {
                case WHOLE_PETS_TABLE:
                    return updatePet(uri, values, selection, selectionArgs);
                case PARTICULAR_PET:
                    selection = PetEntry.COLUMN_PET_ID + "=?";
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    return updatePet(uri, values, selection, selectionArgs);
                default:
                    throw new IllegalArgumentException("Cannot use invalid uri Object --> " + uri);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }
    private int updatePet(Uri uri,ContentValues values,String selection,String[] selectionArgs) throws IllegalArgumentException
    {
        if(values.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                Toast.makeText(getContext(), getContext().getString(R.string.pet_req), Toast.LENGTH_LONG).show();
                throw new IllegalArgumentException("Pet requires a valid name");
            }
        }
        if(values.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }
        if(values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                Toast.makeText(getContext(), getContext().getString(R.string.pet_req_weight), Toast.LENGTH_LONG).show();
                throw new IllegalArgumentException("Pet has to have some name and it cannot be negative");
            }
        }
        SQLiteDatabase db = petDBHelper.getWritableDatabase();
        Integer numRowsUpdated = db.update(PetEntry.TABLE_NAME,values,selection,selectionArgs);
        if(numRowsUpdated == null)
        {
            Log.i("Failed to update","check for some error as number of rows returned = null");
            return -1;
        }
        //Notify all listeners like CatalogActivity that the content at this uri has changed
        //After this the cursor loader again queries the database and fetches in the new data
        // with new cursor object
        if(numRowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsUpdated;
    }
    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri))
        {
            case WHOLE_PETS_TABLE:
                return PetEntry.CONTENT_WHOLE_LIST_TYPE;
            case PARTICULAR_PET:
                return PetEntry.CONTENT_SINGLE_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown Uri "+uri+" with match "+uriMatcher.match(uri));
        }
    }
}
