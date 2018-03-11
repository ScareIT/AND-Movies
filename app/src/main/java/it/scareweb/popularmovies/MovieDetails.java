package it.scareweb.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.scareweb.popularmovies.data.MovieProvider;
import it.scareweb.popularmovies.database.DbManager;
import it.scareweb.popularmovies.models.Movie;
import it.scareweb.popularmovies.models.MovieTrailer;
import it.scareweb.popularmovies.models.SettingsAPI;

public class MovieDetails extends AppCompatActivity implements MovieDetailsExtra.OnTaskCompleted {
    Intent intent;

    @BindView(R.id.details_big_title)
    TextView bigTitle;

    @BindView(R.id.details_movie_vote)
    TextView vote;

    @BindView(R.id.details_movie_plot)
    TextView plot;

    @BindView(R.id.details_movie_release)
    TextView releaseDate;

    @BindView(R.id.details_movie_poster)
    ImageView poster;

    @BindView(R.id.add_to_favourites)
    ImageButton addToFavouritesIcon;

    @BindView(R.id.details_reviews)
    TextView reviewLists;

    @BindView(R.id.details_trailers)
    ListView advancedTrailerList;

    private int movieId;

    private boolean movieSaved;

    private Uri movies;

    private List<MovieTrailer> trailerList;

    private Movie selectedMovie;

    MovieDetails() {
        movieSaved = false;
    }

    MovieDetailsExtra detailsExtra;

    private byte[] movieRawPicture;

    private CustomMovieAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        intent = getIntent();
        selectedMovie = (Movie)intent.getSerializableExtra("MOVIE");
        movieId = selectedMovie.getId();
        bigTitle.setText(selectedMovie.getTitle());
        vote.setText(selectedMovie.getMovieVote());
        plot.setText(selectedMovie.getMoviePlot());
        releaseDate.setText(selectedMovie.getMovieReleaseDate());
        poster.setContentDescription(selectedMovie.getTitle() + " poster");
        if(selectedMovie.getMovieRawPicture() != null) {
            this.movieRawPicture = selectedMovie.getMovieRawPicture();
            poster.setImageBitmap(BitmapFactory.decodeByteArray( this.movieRawPicture,
                    0, selectedMovie.getMovieRawPicture().length));
        } else {
            Picasso.with(this)
                    .load(SettingsAPI.IMAGE_URL + SettingsAPI.IMAGE_SIZE_NORMAL + selectedMovie.getPicture())
                    .into(poster);
        }

        movies = MovieProvider.BASE_CONTENT_URI;
        movies = movies.buildUpon().appendPath(MovieProvider.PATH_FAVOURITES).build();

        setupAddToFavouritesIcon();

        detailsExtra = new MovieDetailsExtra(this);
        detailsExtra.FillTrailers(selectedMovie.getId(), reviewLists);
    }

    private void setupAddToFavouritesIcon() {
        movieSaved = movieIsFavourite();
        addToFavouritesIcon.setOnClickListener(addToFavouritesListener());
        if(movieSaved) {
            addToFavouritesIcon.setColorFilter(getResources().getColor(R.color.favouriteIcon));
        }
    }

    private View.OnClickListener addToFavouritesListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton ibv = (ImageButton)v;

                if(!movieSaved) {
                    addMovieToFavourites();
                    ibv.setColorFilter(getResources().getColor(R.color.favouriteIcon));
                    movieSaved = true;
                } else {
                    removeMovieFromFavourites();
                    ibv.clearColorFilter();
                    movieSaved = false;
                }

            }
        };
    }

    private void addMovieToFavourites() {
        // Insertion
        ContentValues values = new ContentValues();

        //region Movie picture byte array get

        URLConnection pictureUrl = null;
        InputStream is = null;

        if(this.movieRawPicture == null && selectedMovie.getPicture() != null) {
            try {
                pictureUrl = new URL(SettingsAPI.IMAGE_URL + SettingsAPI.IMAGE_SIZE_NORMAL + selectedMovie.getPicture()).openConnection();
                is = pictureUrl.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            byte[] data = new byte[50];
            int current = 0;

            try {
                while((current = bis.read(data,0,data.length)) != -1){
                    buffer.write(data,0,current);
                }
                this.movieRawPicture = buffer.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        values.put(DbManager.MOVIE_ID, movieId);
        values.put(DbManager.MOVIE_TITLE,
                bigTitle.getText().toString());
        values.put(DbManager.MOVIE_VOTE,
                vote.getText().toString());
        values.put(DbManager.MOVIE_RELEASE_DATE,
                releaseDate.getText().toString());
        values.put(DbManager.MOVIE_PLOT,
                plot.getText().toString());
        values.put(DbManager.MOVIE_POSTER,
                this.movieRawPicture);

        //endregion

        Uri uri = getContentResolver().insert(
                movies, values);

        if(uri != null) {
            Toast.makeText(getBaseContext(), selectedMovie.getTitle() + " " + this.getString(R.string.added_to_favourites), Toast.LENGTH_LONG).show();
        }

        //region Picture from array bytes, experiments
//        Bitmap bitmap = ((BitmapDrawable)poster.getDrawable()).getBitmap();
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//        byte[] byteArray = stream.toByteArray();
//        int size = bitmap.getRowBytes() * bitmap.getHeight();
//        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
//        bitmap.copyPixelsToBuffer(byteBuffer);
//        byte[] byteArray1 = byteBuffer.array();
        //endregion
    }

    private void removeMovieFromFavourites() {
        // Deletion
        Uri moviesOne = movies.buildUpon()
                .appendPath(String.valueOf(movieId)).build();

        int deletions = getContentResolver().delete(
                moviesOne, null, null);
        if(deletions > 0) {
            Toast.makeText(getBaseContext(), selectedMovie.getTitle() + " " + this.getString(R.string.removed_from_favourites), Toast.LENGTH_LONG).show();
        }
    }

    private boolean movieIsFavourite() {
        Uri moviesOne = movies.buildUpon()
                .appendPath(String.valueOf(movieId)).build();

        Cursor c = getContentResolver().query(moviesOne, null, null, null, null);

        if (c!=null && c.moveToFirst()) {
            return true;
        }

        return false;
    }

    @Override
    public void onTaskCompleted(List<MovieTrailer> movieTrailers) {
        this.trailerList = movieTrailers;

        adapter = new CustomMovieAdapter(this, trailerList);
        advancedTrailerList.setAdapter(adapter);

        //region Non scrollable trailer list
        /*
        Got this idea from https://stackoverflow.com/questions/4338185/how-to-get-a-non-scrollable-listview
        was the most simple way I found to add the trailer list inside the constraint layout and making them immediately
        visible
         */

        ViewGroup vg = advancedTrailerList;
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, vg);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = advancedTrailerList.getLayoutParams();
        par.height = totalHeight + (advancedTrailerList.getDividerHeight() * (adapter.getCount() - 1));
        //advancedTrailerList.setLayoutParams(par);
        //advancedTrailerList.requestLayout();
        //endregion
    }


    class CustomMovieAdapter extends BaseAdapter {
        private Context context;
        List<MovieTrailer> data;

        public CustomMovieAdapter(Context context, List data){
            this.context = context;
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public MovieTrailer getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if(view == null) {
                holder =new ViewHolder();
                view = LayoutInflater.from(context).inflate(R.layout.list_item, null);
                holder.imgIcon = view.findViewById(R.id.imgIcon);
                holder.linkDescription = view.findViewById(R.id.trailerLink);
                view.setTag(holder);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(getItem(i).Link()));
                        startActivity(intent);
                    }
                });


            } else {
                holder = (ViewHolder)view.getTag();
            }

            holder.linkDescription.setText(getItem(i).Name);

            return view;
        }

        class ViewHolder{
            ImageView imgIcon;
            TextView linkDescription;
        }
    }
}
