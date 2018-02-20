package it.scareweb.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.scareweb.popularmovies.models.Movie;

/**
 * Created by luca on 20/02/2018.
 */

public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieListAdapterViewHolder>{

    private String[] movieTitles;
    private List<Movie> movies;

    public MovieListAdapter() {}

    @Override
    public MovieListAdapter.MovieListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.movie_list_item, parent, false);
        MovieListAdapterViewHolder viewHolder = new MovieListAdapterViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MovieListAdapter.MovieListAdapterViewHolder holder, int position) {
        holder.movieTitle.setText(movies.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        if(movies != null) {
            return movies.size();
        }
        return 0;
    }

    public void setMovieTitles(List<Movie> movieList) {
        movies = movieList;
        notifyDataSetChanged();
    }

    public class MovieListAdapterViewHolder extends RecyclerView.ViewHolder {
        public final TextView movieTitle;

        public MovieListAdapterViewHolder(View itemView) {
            super(itemView);
            movieTitle = itemView.findViewById(R.id.movie_title_tv);
        }
    }
}