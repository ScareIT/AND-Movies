package it.scareweb.popularmovies.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by luccar on 01/03/2018.
 */

public class DbManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_FAVOURITE_MOVIES = "FavouriteMovies";
    public static final String MOVIE_ID = "_id";
    public static final String MOVIE_TITLE = "movieTitle";
    public static final String MOVIE_POSTER = "moviePoster";
    public static final String MOVIE_SYNOPSIS = "movieSynopsis";
    public static final String MOVIE_VOTE = "movieVote";
    public static final String MOVIE_RELEASE_DATE = "movieRelDate";

    public static final String[] ALL_COLUMNS =
            {MOVIE_ID,MOVIE_POSTER,MOVIE_SYNOPSIS,MOVIE_VOTE,MOVIE_RELEASE_DATE};

    public DbManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String CREATE_MOVIE_TABLE =
                "CREATE TABLE " + TABLE_FAVOURITE_MOVIES + " (" +
                        MOVIE_ID + " INTEGER PRIMARY KEY, " +
                        MOVIE_TITLE + " TEXT, " +
                        MOVIE_POSTER + " BLOB, " +
                        MOVIE_SYNOPSIS + " TEXT, " +
                        MOVIE_RELEASE_DATE + " TEXT default CURRENT_TIMESTAMP, " +
                        MOVIE_VOTE + " REAL " +
                        ")";

        sqLiteDatabase.execSQL(CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_FAVOURITE_MOVIES);
        onCreate(sqLiteDatabase);
    }
}