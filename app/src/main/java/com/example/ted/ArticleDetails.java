package com.example.ted;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

import com.bumptech.glide.Glide;
import com.example.ted.models.Article;

import org.parceler.Parcels;
import java.util.ArrayList;
import java.util.List;

public class ArticleDetails extends AppCompatActivity {
    Article article;
    TextView tvTitle, tvAuthor, tvBody, tvDate;
    ImageView ivThumbnail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_details);
        //Locates the necessary components of the activity
        tvTitle = findViewById(R.id.tvTitle);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvBody =findViewById(R.id.tvBody);
        tvDate= findViewById(R.id.tvDate);
        ivThumbnail = findViewById(R.id.ivPicture);
        //Takes in the article supplied in the intent
        article = Parcels.unwrap(getIntent().getParcelableExtra(Article.class.getSimpleName()));
        //Populates the components with said information
        tvTitle.setText(article.getTitle());
        tvAuthor.setText(article.getAuthor());
        fillBody();
        tvDate.setText(article.getTime());
        Glide.with(ArticleDetails.this).load(article.getImageUrl()).thumbnail(Glide.with(ArticleDetails.this).load(R.drawable.noresponse)).into(ivThumbnail);

    }
    //If the user clicks on the article image, the article is opened in a browser
    public void openArticle(View view){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(article.getArticleUrl()));
        startActivity(i);
    }
    //Fills in the article body with the supplied text while attaching links to their respective text
    public void fillBody(){
        SpannableString ss = new SpannableString(article.getBody());
        final List<String> links= article.getLinks();
        List<int[]> index = article.getIndex();
        //For each link in the article, we want to add a new clickable span to the spannable string
        for( int i = 0;i<links.size();i++) {
            final int finalI = i;
            ss.setSpan(new ClickableSpan() {
                //Upon clicking the specified text, the user is directed to a link in the web browser
                @Override
                public void onClick(View widget) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(links.get(finalI)));
                    startActivity(browserIntent);
                }
                //Differs the hyperlink text color
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(getResources().getColor(R.color.blue));
                    ds.setUnderlineText(false);
                }
            }, index.get(i)[0], index.get(i)[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tvBody.setText(ss);
        tvBody.setMovementMethod(LinkMovementMethod.getInstance());

    }
}