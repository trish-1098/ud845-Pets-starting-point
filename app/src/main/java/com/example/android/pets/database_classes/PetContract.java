package com.example.android.pets.database_classes;

import android.net.Uri;
import android.provider.BaseColumns;
import static android.content.ContentResolver.CURSOR_DIR_BASE_TYPE;
import static android.content.ContentResolver.CURSOR_ITEM_BASE_TYPE;

/**
 * Created by trishant on 4/3/18.
 */

public final class PetContract {
    public static final String CONTENT_PROVIDER = "com.example.android.pets";
    public static final String SCHEME_HEADER = "content://";
    public static final String PATH_TABLE_PETS = "pets";
    public PetContract() {
    }
    public static final class PetEntry implements BaseColumns {
        public static final String TABLE_NAME = "pets";
        public static final String COLUMN_PET_ID  = BaseColumns._ID;
        public static final String COLUMN_PET_NAME = "name";
        public static final String COLUMN_PET_BREED = "breed";
        public static final String COLUMN_PET_GENDER = "gender";
        public static final String COLUMN_PET_WEIGHT = "weight";
        /*
        Possible values of gender
         */
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;
        public static final int GENDER_UNKNOWN = 0;
        public static final Uri BASE_CONTENT_URI = Uri.parse(SCHEME_HEADER+CONTENT_PROVIDER);
        public static final Uri MAIN_CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_TABLE_PETS);

        public static final String CONTENT_WHOLE_LIST_TYPE = CURSOR_DIR_BASE_TYPE + "/" +
                CONTENT_PROVIDER + "/" + PATH_TABLE_PETS;
        public static final String CONTENT_SINGLE_ITEM_TYPE = CURSOR_ITEM_BASE_TYPE + "/" +
                CONTENT_PROVIDER + "/" + PATH_TABLE_PETS;
        public static boolean isValidGender(int gen)
        {
            if(gen == GENDER_FEMALE || gen == GENDER_MALE || gen == GENDER_UNKNOWN)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }
}
