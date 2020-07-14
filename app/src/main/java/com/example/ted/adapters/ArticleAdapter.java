package com.example.ted.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ted.R;
import com.example.ted.models.Article;

import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder>{
    Context context;
    List<Article> articles;
    public ArticleAdapter(Context context, List<Article> articles) {
        this.context = context;
        this.articles = articles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View articleView = LayoutInflater.from(context).inflate(R.layout.item_article, parent, false);
        return new ViewHolder(articleView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.bind(article);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

        }

        public void bind(Article article) {
            /*mvTitle.setText(Article.getTitle());
            mvOver.setText(Article.getDescription());
            String url;
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                url = Article.getBdPath();
                Glide.with(context).load(url).transform(new RoundedCornersTransformation(15, 15)).placeholder(R.drawable.flicks_backdrop_placeholder).into(mvPoster);

            } else {
                url = Article.getPosterPath();
                Glide.with(context).load(url).transform(new RoundedCornersTransformation(15, 15)).placeholder(R.drawable.flicks_Article_placeholder).into(mvPoster);
            }*/

        }
        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Article article = articles.get(position);
                //Intent intent = new Intent(context, details.class);
                //intent.putExtra(Article.class.getSimpleName(), Parcels.wrap(Article));
                //context.startActivity(intent);
            }
        }
    }

}
