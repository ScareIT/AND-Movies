package it.scareweb.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import it.scareweb.popularmovies.database.DbManager;

/**
 * Created by luca on 01/03/2018.
 */

public class MovieProvider extends ContentProvider {
    public static final String AUTHORITY = "it.scareweb.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_FAVOURITES = "favourites";

    public static final int FAVOURITES = 100;
    public static final int FAVOURITES_WITH_ID = 101;

    private SQLiteDatabase movieDb;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(AUTHORITY, PATH_FAVOURITES, FAVOURITES);
        uriMatcher.addURI(AUTHORITY, PATH_FAVOURITES + "/#", FAVOURITES_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DbManager dbHelper = new DbManager(context);
        movieDb = dbHelper.getWritableDatabase();
        return movieDb != null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DbManager.TABLE_FAVOURITE_MOVIES);

        switch (sUriMatcher.match(uri)) {
//            case FAVOURITES:
//                break;

            case FAVOURITES_WITH_ID:
                qb.appendWhere( DbManager.MOVIE_ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
        }

        if (sortOrder == null || sortOrder == ""){
            sortOrder = DbManager.MOVIE_TITLE;
        }

        Cursor c = qb.query(movieDb,	projection,	selection,
                selectionArgs,null, null, sortOrder);
        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int match = sUriMatcher.match(uri);
        Uri returnUri; // URI to be returned

        switch (match) {
            case FAVOURITES:
                // Insert new values into the database
                // Inserting values into tasks table
                long rowId = movieDb.insert(DbManager.TABLE_FAVOURITE_MOVIES, "", contentValues);
                if ( rowId > 0 ) {
                    returnUri = ContentUris.withAppendedId(BASE_CONTENT_URI, rowId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            // Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        int moviesDeleted = 0;

        switch (sUriMatcher.match(uri)) {
//            case FAVOURITES:
//                break;
            case FAVOURITES_WITH_ID:
                String idToDelete = uri.getPathSegments().get(1);
                moviesDeleted = movieDb.delete(DbManager.TABLE_FAVOURITE_MOVIES, DbManager.MOVIE_ID + "=?", new String[]{idToDelete});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return moviesDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
