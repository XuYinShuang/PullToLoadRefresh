package com.zhiyuan3g.pulltoloadrefresh;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RefreshListView listView;
    private List<String>list,tempList;
    private DataAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView= (RefreshListView) findViewById(R.id.listView);
        tempList=new ArrayList<>();
        list=getData();
        adapter=new DataAdapter(list,this);
        listView.setAdapter(adapter);
        listView.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
            @Override
            public void onDownPullRefresh() {

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {

                        SystemClock.sleep(1000);

                        tempList.add("这是下拉刷新哦");
                        tempList.addAll(list);
                        list.clear();
                        list.addAll(tempList);
                        tempList.clear();

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        adapter.notifyDataSetChanged();
                        listView.hideHeadView();
                    }
                }.execute(new Void[]{});

            }


            @Override
            public void onLoadingMore() {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        SystemClock.sleep(1000);
                        list.add("这是上拉加载哈哈");

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        adapter.notifyDataSetChanged();
                        listView.hideFooterView();
                    }
                }.execute(new Void[]{});
            }
        });
    }

    public List<String> getData() {

        List<String>list=new ArrayList<>();
        for (int i=0;i<30;i++){
            list.add("第"+(i+1)+"条数据");
        }
        return list;
    }
}
