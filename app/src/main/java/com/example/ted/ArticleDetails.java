package com.example.ted;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ted.models.Article;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ArticleDetails extends AppCompatActivity {
    Article article;
    TextView tvTitle, tvAuthor, tvBody, tvDate;
    ImageView ivThumbnail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_details);

        tvTitle = findViewById(R.id.tvTitle);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvBody =findViewById(R.id.tvBody);
        tvDate= findViewById(R.id.tvDate);
        ivThumbnail = findViewById(R.id.ivPicture);

        article = Parcels.unwrap(getIntent().getParcelableExtra(Article.class.getSimpleName()));
        tvTitle.setText(article.getTitle());
        tvAuthor.setText(article.getAuthor());
        fillBody();
        tvDate.setText(article.getTime());
        String url = article.getImageUrl();
        Glide.with(ArticleDetails.this).load(url).thumbnail(Glide.with(ArticleDetails.this).load(R.drawable.noresponse)).into(ivThumbnail);

    }
    public void fillBody(){
        SpannableString ss = new SpannableString(article.getBody());
       List<ClickableSpan> cs= new ArrayList<>();
        final List<String> links= article.getLinks();
        List<int[]> index = article.getIndex();
        for(final String i:links) {
            cs.add(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(i));
                    startActivity(browserIntent);
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(getResources().getColor(R.color.blue));
                    ds.setUnderlineText(false);
                }
            });
        }

        for( int i = 0;i<cs.size();i++)
            ss.setSpan(cs.get(i),index.get(i)[0], index.get(i)[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvBody.setText(ss);
        tvBody.setMovementMethod(LinkMovementMethod.getInstance());

    }



}