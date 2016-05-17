package com.example.newsreader;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;


import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import android.widget.ListView;
import android.widget.TextView;

import com.newsreader.bean.NewsBean;


import org.json.JSONArray;
import org.json.JSONObject;


import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;



/**
 * Created by cxy on 2016/4/20.
 */
public class Fragment2 extends Fragment implements SwipeRefreshLayout.OnRefreshListener{


    private static final int MSG_NEWS_LOADED=100;   //指示Rss新闻数据已获取
    private View layoutView;
    private ProgressDialog pd;
    private List<NewsBean> newsList=new ArrayList<NewsBean>();  //新闻条目数组
    private ListView listView1;
    private NewsAdapter adapter;
    private SwipeRefreshLayout refresh_layout = null;//刷新控件

    String httpUrl = "http://apis.baidu.com/showapi_open_bus/channel_news/search_news";
    String httpArg = "channelId=5572a108b3cdc86cf39001ce&page=1&needContent=0&needHtml=1";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layoutView=inflater.inflate(R.layout.fragment1,null);
        listView1=(ListView)layoutView.findViewById(R.id.listView);

        //创建并显示一个进度条，设定可以被用户打断
        pd=ProgressDialog.show(getActivity(),"请稍候...","正在加载数据",true,true);
        refresh_layout = (SwipeRefreshLayout) layoutView.findViewById(R.id.refresh_layout);
        // refresh_layout.setProgressBackgroundColorSchemeColor(Color.GRAY);
        refresh_layout.setColorSchemeResources(android.R.color.holo_green_light, android.R.color.holo_blue_light, android.R.color.holo_red_light);//设置跑动的颜色值

        adapter=new NewsAdapter(newsList);
        listView1.setAdapter(adapter);
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(Fragment2.this.getActivity(),NewsActivity.class);
                NewsBean news=newsList.get(position);
                intent.putExtra("news",news);
               // intent.putExtra("title","国际焦点");
                startActivity(intent);
            }
        });

        refresh_layout.setOnRefreshListener(this);//设置下拉的监听
        new Thread(new Runnable() {
            @Override
            public void run() {

                String jsonResult = request(httpUrl, httpArg);
                System.out.println(jsonResult);
                try {
                    getRssItems(jsonResult);
                } catch (Exception e) {
                    e.printStackTrace();
                }

              

                Message msg=mUIHandler.obtainMessage(MSG_NEWS_LOADED);
                //向主线程发送消息时，还可以携带数据
                //msg.obj=newsList;
                mUIHandler.sendMessage(msg);

                //销毁进度条
                pd.dismiss();

            }


        }).start();


        return layoutView;
    }


    /**
     * @param  httpUrl
     *            :请求接口
     * @param httpArg
     *            :参数
     * @return 返回结果
     */
    public static String request(String httpUrl, String httpArg) {
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();
        httpUrl = httpUrl + "?" + httpArg;

        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");
            // 填入apikey到HTTP header
            connection.setRequestProperty("apikey",  "0c3a92793aec19894d356d433b9b2622");
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }



    public void getRssItems(String jsonData) throws Exception{



        JSONObject jsonObject1=new JSONObject(jsonData);
        JSONObject jsonObject2=jsonObject1.getJSONObject("showapi_res_body");
        JSONObject jsonObject3=jsonObject2.getJSONObject("pagebean");
        JSONArray jsonArray=jsonObject3.getJSONArray("contentlist");

        newsList.clear();
        for (int i=0;i<jsonArray.length();i++){
            JSONObject jsonObjectSon= (JSONObject)jsonArray.opt(i);
            String description=jsonObjectSon.getString("desc");
            String link=jsonObjectSon.getString("link");
            String date=jsonObjectSon.getString("pubDate");
            String title=jsonObjectSon.getString("title");
            String content=jsonObjectSon.getString("html");

            NewsBean newsBean=new NewsBean();
            newsBean.description=description;
            newsBean.pubDate=date;
            newsBean.link=link;
            newsBean.title=title;
            newsBean.content=content;

            newsList.add(newsBean);

        }
    }

    private Handler mUIHandler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_NEWS_LOADED:
                    //更新ListView显示
                    adapter.notifyDataSetChanged();
                    refresh_layout.setRefreshing(false);
                    break;
            }
        }
    };

    class NewsAdapter extends BaseAdapter{
        //待显示的新闻列表
        private  List<NewsBean> newsItems;

        public NewsAdapter(List<NewsBean> newsItems){
            this.newsItems=newsItems;

        }

        @Override
        public int getCount() {
            return newsItems.size();
        }

        @Override
        public Object getItem(int position) {
            return newsItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        //ListView显示每条数据时，都要调用getView()方法
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null){
                convertView=getActivity().getLayoutInflater().inflate(R.layout.news_item,null);
            }

            //初始化行布局视图中的各个子控件
            TextView newsTitle=(TextView) convertView.findViewById(R.id.news_title);
            TextView newsDescr=(TextView) convertView.findViewById(R.id.news_description);
            TextView newsPubdate=(TextView) convertView.findViewById(R.id.news_pubDate);

            //获取第position行的数据
            NewsBean item=newsItems.get(position);
            //将第position行的数据显示到布局界面中
            newsTitle.setText(item.title);
            newsDescr.setText(item.description);
            newsPubdate.setText(item.pubDate);

            //将行布局返回给ListView组件显示
            return convertView;
        }
    }

    @Override
    public void onRefresh() {
        new Thread(new Runnable() {//下拉触发的函数，这里是谁1s然后加入一个数据，然后更新界面
            @Override
            public void run() {
                try {
                    //  System.out.println("下拉啦");
                    Thread.sleep(1000);
                    String jsonResult = request(httpUrl, httpArg);
                    getRssItems(jsonResult);
                    Message msg=mUIHandler.obtainMessage(MSG_NEWS_LOADED);
                    mUIHandler.sendMessage(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();


    }


}
