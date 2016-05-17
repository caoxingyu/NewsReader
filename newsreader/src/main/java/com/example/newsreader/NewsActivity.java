package com.example.newsreader;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.newsreader.bean.NewsBean;


public class NewsActivity extends AppCompatActivity{

    protected GestureDetector mGestureDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_detail);


        //接收intent传递过来的数据
        Intent intent=this.getIntent();
        final NewsBean news=(NewsBean)intent.getSerializableExtra("news");

        //setTitle(title);

        TextView titleView=(TextView)findViewById(R.id.news_title);
        TextView pubDateView=(TextView)findViewById(R.id.news_pubDate);
        final WebView webView=(WebView)findViewById(R.id.newsDetail);

        titleView.setText(news.title);

        pubDateView.setText("(发布日期："+news.pubDate+")");


        //WebView参数设置(是否支持多窗口，是否支持缩放)
        WebSettings settings=webView.getSettings();
        settings.setSupportMultipleWindows(false);
        settings.setSupportZoom(false);
        settings.setDefaultFontSize(18);
        //加载显示新闻描述内容

        webView.loadDataWithBaseURL(null,news.content,"text/html", "utf-8",null);

        //返回动作，单击返回则结束当前NewsActivity
        ImageView back=(ImageView)findViewById(R.id.imageViewBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //单击浏览新闻URL对应的详细界面
        ImageView browser=(ImageView)findViewById(R.id.imageViewBrowser);
        browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               // webView.loadUrl(news.link);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(news.link));
                startActivity(intent);
            }
        });

//        webView.setWebViewClient(new WebViewClient(){
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                // TODO Auto-generated method stub
//                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
//                view.loadUrl(url);
//                return true;
//            }
//        });
        webView.setOnTouchListener(new View.OnTouchListener() {
                                       @Override
                                       public boolean onTouch(View v, MotionEvent event) {
                                           mGestureDetector.onTouchEvent(event);
                                           return false;
                                       }
                                   });

                mGestureDetector = new GestureDetector( new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//                if(e1.getRawX() - e2.getRawX() > 200){
//                    showNext();//向左滑动，显示图片列表
//                    return true;
//                }

                        if (e2.getRawX() - e1.getRawX() > 230) {
                            finish();//向右滑动
                            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                            return true;
                        }

                       return super.onFling(e1, e2, velocityX, velocityY);

                    }
                });



    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);

    }
}
