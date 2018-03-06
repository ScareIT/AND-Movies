package it.scareweb.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.scareweb.popularmovies.models.Movie;

/**
 * Created by luca on 20/02/2018.
 */

public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieListAdapterViewHolder> {
    private List<Movie> movies;
    public MovieListAdapter() {}

    // viewType: progressivo vista creata adesso
    @Override
    public MovieListAdapter.MovieListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.movie_list_item, parent, false);
        MovieListAdapterViewHolder viewHolder = new MovieListAdapterViewHolder(view);
        return viewHolder;
    }

    // position: progressivo vista creata in tutto
    @Override
    public void onBindViewHolder(MovieListAdapter.MovieListAdapterViewHolder holder, int position) {
        // holder.movieTitle.setText(movies.get(position).getTitle());
        if(movies.get(position).getMovieRawPicture() != null) {
            holder.moviePicture.setImageBitmap(BitmapFactory.decodeByteArray( movies.get(position).getMovieRawPicture(),
                    0, movies.get(position).getMovieRawPicture().length));
            return;
        }
        Picasso.with(holder.moviePicture.getContext())
                .load("http://image.tmdb.org/t/p/w185" + movies.get(position).getPicture())
                .into(holder.moviePicture);
    }

    @Override
    public int getItemCount() {
        if (movies != null) {
            return movies.size();
        }
        return 0;
    }

    public void setMovieTitles(List<Movie> movieList) {
        this.movies = movieList;
        notifyDataSetChanged();
    }

    public class MovieListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Context context;

        @BindView(R.id.movie_picture_iv)
        ImageView moviePicture;

        public MovieListAdapterViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            ButterKnife.bind(this, itemView);
            moviePicture.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int selectedMovie = getAdapterPosition();
            Intent intent = new Intent(this.context, MovieDetails.class);
            intent.putExtra("MOVIE", movies.get(selectedMovie));
            this.context.startActivity(intent);
        }
    }
}