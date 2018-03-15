package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.android.pets.database_classes.PetContract.PetEntry;


/**
 * Created by trish on 3/10/2018.
 */

public class PetAdapter extends CursorAdapter {
    public PetAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.pet_list_item_view,parent,false);
    }
    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameOfPet = view.findViewById(R.id.pet_name_view);
        nameOfPet.setText(cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME)));
        TextView breedOfPet = view.findViewById(R.id.breed_view);
        String breedOfPetString = cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED));
        if(TextUtils.isEmpty(breedOfPetString)){
            breedOfPet.setText(R.string.breed_unknown_for_listview);
        } else {
            breedOfPet.setText(breedOfPetString);
        }
        TextView genderOfPet = view.findViewById(R.id.gender_view);
        switch (cursor.getInt(cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER))){
            case PetEntry.GENDER_UNKNOWN:
                genderOfPet.setText(context.getString(R.string.gender_unknown_for_listview));
                break;
            case PetEntry.GENDER_MALE:
                genderOfPet.setText(context.getString(R.string.gender_male));
                break;
            case PetEntry.GENDER_FEMALE:
                genderOfPet.setText(context.getString(R.string.gender_female));
                break;
            default: genderOfPet.setText(context.getString(R.string.error_getting_pet));
                break;
        }
    }
}