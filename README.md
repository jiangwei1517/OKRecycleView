# OKRecycleView
![MacDown logo](https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1492964750310&di=07e494ee257c1976c5ce421edfbdb2cb&imgtype=0&src=http%3A%2F%2Fimg1.ph.126.net%2FrDDks-B0UcCmMjXyDokyEA%3D%3D%2F6608234207725029013.jpg)

## 解决问题
* RecycleView的上拉加载
* RecycleView的下拉刷新
* ***LinearLayoutManager*** / ***GridLayoutManager*** / ***StaggeredGridLayoutManager***三种不同形式的数据加载方式
* 加载时动画效果优化

## 基本思想
* 根据Adapter的viewType来决定头布局跟尾布局样式
* setmargins确定头尾的隐藏效果
* 根据LayoutManager来决定头尾占的宽度

## 使用方法
### 初始化
	mOKRecycleView = (OKRecycleView) findViewById(R.id.okrecycle);
### 数据格式
	// mOKRecycleView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
	// mOKRecycleView.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));
   	// mOKRecycleView.setLayoutManager(new StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL));
### 上拉下拉开关
	  mOKRecycleView.setCanRefresh(false);
      mOKRecycleView.setCanLoadMore(false);
### 设置监听
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