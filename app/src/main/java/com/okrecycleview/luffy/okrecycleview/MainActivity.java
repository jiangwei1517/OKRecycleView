package com.okrecycleview.luffy.okrecycleview;

import java.util.ArrayList;

import com.okrecycleview.luffy.recycleview.MyItemDecoration;
import com.okrecycleview.luffy.recycleview.OKRecycleView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

public class MainActivity extends AppCompatActivity {
    private OKRecycleView mOKRecycleView;
    private RecyclerView.Adapter adapter;
    private ArrayList<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOKRecycleView = (OKRecycleView) findViewById(R.id.okrecycle);
//         mOKRecycleView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
//         mOKRecycleView.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));
        mOKRecycleView.setLayoutManager(new StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL));
        // mOKRecycleView.setCanRefresh(false);
        // mOKRecycleView.setCanLoadMore(false);
        list = new ArrayList<>();
        for (int i = 0; i < 100; i++)
            list.add("list" + i);
        adapter = new RecycleViewAdapter(this, list);
        mOKRecycleView.setAdapter(adapter);
        mOKRecycleView.addItemDecoration(new MyItemDecoration(this, 10));
        mOKRecycleView.setPullToRefreshListener(new OKRecycleView.PullRefreshListerer() {
            @Override
            public void onRefreshing() {
                refreshData();
            }
        });
        mOKRecycleView.setLoadMoreListener(new OKRecycleView.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMoreData();
            }
        });
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                for (int i = 0; i < 2; i++)
                    list.add(0, "更新了一条旧数据");
                mOKRecycleView.setRefreshComplete();
            }
            if (msg.what == 2) {
                for (int i = 0; i < 2; i++)
                    list.add("加载了一条新数据");
                mOKRecycleView.setLoadMoreComplete();
            }
        }
    };

    private void loadMoreData() {
        mHandler.sendEmptyMessageDelayed(2, 2000);
    }

    private void refreshData() {
        mHandler.sendEmptyMessageDelayed(1, 2000);
    }
}
