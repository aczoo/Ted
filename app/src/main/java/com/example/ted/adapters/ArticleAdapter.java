package com.example.ted.adapters;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.ted.ArticleDetails;
import com.example.ted.ChatActivity;
import com.example.ted.R;
import com.example.ted.models.Article;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;


import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
    private String TAG = "ArticleAdapter";
    Context context;
    List<Article> articles;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference userDB = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
    SimpleDateFormat sdf = new SimpleDateFormat("h:mm a d MMM", Locale.ENGLISH);

    public ArticleAdapter(Context context, List<Article> articles) {
        this.context = context;
        this.articles = articles;
    }
    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 1;
        else return 2;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inserts welcome card at the top of the recyclerview
        if (viewType == 1)
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

    public abstract class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        public void bind(Article article) {
        }
    }
    //Welcome View Holder
    public class welcomeViewHolder extends ViewHolder implements View.OnClickListener {
        public welcomeViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }
        //Opens chat dialogue upon click
        @Override
        public void onClick(View view) {
            Intent i = new Intent(context, ChatActivity.class);
            //specifies starting coordinates for circular reveal animation, middle of the floating action bubble
            i.putExtra("x", 948);
            i.putExtra("y", 1830);
            context.startActivity(i);
        }
    }
    //Article View Holder
    public class articleViewHolder extends ViewHolder implements View.OnClickListener {
        TextView tvTitle, tvAuthor, tvDate;
        ImageView ivThumbnail;
        LottieAnimationView heart, heartbreak;
        //Locates necessary components
        public articleViewHolder(@NonNull final View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivThumbnail = itemView.findViewById(R.id.ivPicture);
            heart = itemView.findViewById(R.id.heart);
            heartbreak = itemView.findViewById(R.id.heartbreak);
            itemView.setOnClickListener(this);
        }
        //Fills said components with the article passed in
        @Override
        public void bind(@NotNull final Article article) {
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
            //Displays correct "heart" status upon opening the app
            userDB.child("likes").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(article.getId())){
                        heart.setVisibility(View.GONE);
                        heartbreak.setVisibility(View.VISIBLE);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "onCancelled: ", error.toException());
                }
            });

            //Heart animation
            heart.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }
                /*After the animation reaches it's end state, we want to reset its progress, hide it, and reveal
                 the heartbreak animation.*/
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
            //Heartbreak animation
            heartbreak.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }
                /*After the animation reaches it's end state, we want to reset its progress, hide it, and reveal
                 the heart animation. */
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
            //Upon clicking like
            heart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    heart.playAnimation();
                    //Adds the article to the user's likes
                    HashMap<String, Object> map = new HashMap<>();
                    map.put(article.getId(), ServerValue.TIMESTAMP);
                    userDB.child("likes").updateChildren(map);
                    map = new HashMap<>();
                    //Adds a different version of the article to the user's activity, with timestamps and without unnecessary information
                    HashMap<String, Object> map2 = new HashMap<>();
                    map2.put("timestamp", ServerValue.TIMESTAMP);
                    map2.put("timeLiked",sdf.format(new Date()) );
                    map2.put("title", article.getTitle());
                    map2.put("imageUrl", article.getImageUrl());
                    map.put(article.getId().replaceAll("/","@"), map2);
                    userDB.child("activity").updateChildren(map);
                }
            });
            //Upon clicking unlike
            heartbreak.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    heartbreak.playAnimation();
                    //remove the article from the user's likes and activity
                    userDB.child("likes").child(article.getId()).removeValue();
                    userDB.child("activity").child(article.getId().replace("/","@")).removeValue();

                }
            });
        }
        //Upon clicking the article, open the details activity
        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Article article = articles.get(position);
                Intent intent = new Intent(context, ArticleDetails.class);
                intent.putExtra(Article.class.getSimpleName(), Parcels.wrap(article));
                //Shared Element Activity Transition, uses the shared element between both activities to emphasize continuity
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation((Activity)context, ivThumbnail, "thumbnail");
                 context.startActivity(intent, options.toBundle());
            }
        }
    }

}
