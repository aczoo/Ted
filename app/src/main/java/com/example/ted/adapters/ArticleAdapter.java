package com.example.ted.adapters;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.ted.ArticleDetails;
import com.example.ted.ChatActivity;
import com.example.ted.R;
import com.example.ted.models.Article;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
    private String TAG = "ArticleAdapter";
    Context context;
    List<Article> articles;

    public ArticleAdapter(Context context, List<Article> articles) {
        this.context = context;
        this.articles = articles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if(viewType ==1)
                return new welcomeViewHolder(LayoutInflater.from(context).inflate(R.layout.item_welcome, parent, false));
            else
                return new articleViewHolder(LayoutInflater.from(context).inflate(R.layout.item_article, parent, false));

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
    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 1;
        else return 2;
    }
    public abstract class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
        public void bind(Article article) {
        }
    }
    public class welcomeViewHolder extends ViewHolder implements View.OnClickListener{
        public welcomeViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
            Intent i = new Intent(context, ChatActivity.class);
            i.putExtra("x", 948);
            i.putExtra("y",1830);
            context.startActivity(i);
        }
    }
    public class articleViewHolder extends ViewHolder implements View.OnClickListener {
        TextView tvTitle, tvAuthor, tvDate;
        ImageView ivThumbnail;
        LottieAnimationView heart, heartbreak;
        public articleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivThumbnail = itemView.findViewById(R.id.ivPicture);
            heart = itemView.findViewById(R.id.heart);
            heartbreak = itemView.findViewById(R.id.heartbreak);
            heart.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    heart.setVisibility(View.GONE);
                    heart.setProgress(0);
                    heartbreak.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
            heartbreak.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    heartbreak.setVisibility(View.GONE);
                    heartbreak.setProgress(0);
                    heart.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            heart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    heart.playAnimation();
                }
            });
            heartbreak.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    heartbreak.playAnimation();
                }
            });
            itemView.setOnClickListener(this);
        }
        @Override
        public void bind(@NotNull Article article) {
            tvTitle.setText(article.getTitle());
            if (article.getAuthor() == null) {
                View bar = itemView.findViewById(R.id.vBar);
                bar.setVisibility(View.GONE);
            }
            tvAuthor.setText(article.getAuthor());
            tvDate.setText(article.getTimeAgo());
            String url = article.getImageUrl();
            Glide.with(context).load(url).transform(new RoundedCornersTransformation(15, 15))
                    .thumbnail(Glide.with(itemView.getContext()).load(R.drawable.noresponse)).into(ivThumbnail);

        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Article article = articles.get(position);
                Intent intent = new Intent(context, ArticleDetails.class);
                intent.putExtra(Article.class.getSimpleName(), Parcels.wrap(article));
                context.startActivity(intent);
            }
        }
    }

}
