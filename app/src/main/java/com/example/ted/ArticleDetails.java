package com.example.ted;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ted.models.Article;

import org.parceler.Parcels;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ArticleDetails extends AppCompatActivity {
    Article article;
    TextView tvTitle, tvAuthor, tvBody;
    ImageView ivThumbnail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_details);
        tvTitle = findViewById(R.id.tvTitle);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvBody =findViewById(R.id.tvBody);
        ivThumbnail = findViewById(R.id.ivPicture);
        article = Parcels.unwrap(getIntent().getParcelableExtra(Article.class.getSimpleName()));
        tvTitle.setText(article.getTitle());
        tvAuthor.setText(article.getAuthor());
        tvBody.setText(article.getBody());
        String url = article.getImageUrl();
        Glide.with(ArticleDetails.this).load(url).transform(new RoundedCornersTransformation(15, 15)).placeholder(R.drawable.no_result).into(ivThumbnail);

    }
}