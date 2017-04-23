package com.jiangwei.recycleview;

import java.util.ArrayList;

import com.okrecycleview.luffy.recycleview.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * author: jiangwei18 on 17/4/9 15:45 
 */

public class OKRecycleView extends RecyclerView {
    public static final int HEAD_VIEW_TYPE = 1;
    public static final int FOOT_VIEW_TYPE = 2;
    // 设置头部的高度
    public static final int HEAD_VIEW_HEIGHT = 89;
    private ViewInfo mHeaderViewInfo = new ViewInfo();
    private ViewInfo mFooterViewInfo = new ViewInfo();
    public ArrayList<ViewInfo> mHeaderViewInfos = new ArrayList<>();
    public ArrayList<ViewInfo> mFooterViewInfos = new ArrayList<>();
    public Adapter mAdapter, adapter;
    private int lastItem;
    private int totalCount;
    private int firstVisible;
    private boolean isLoad = false;
    private boolean isTop = true;
    private boolean isRefreshing = false;
    private int[] into;
    private int[] firstInto;
    private float startY = 0;
    private float endY;
    private float moveY = 0;
    private TextView text;
    private PullRefreshListerer mPullRefreshListener;
    private LoadMoreListener mLoadMoreListener;
    private boolean isShouldSpan;
    private boolean isFillWindow = false;
    private ValueAnimator mFinishAnimator;
    private ValueAnimator mHalfAnimator;
    private static final int TIME = 500;
    private boolean mIsCanRefresh = true;
    private boolean mIsCanLoadMore = true;
    private boolean isStaggered;

    public OKRecycleView(Context context) {
        super(context);
        initView();
    }

    public OKRecycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public OKRecycleView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    public void initView() {
        View headerView = LayoutInflater.from(getContext()).inflate(R.layout.head_layout, null);
        View footerView = LayoutInflater.from(getContext()).inflate(R.layout.footer_layout, null);
        // 将两个view保存,并不是真的添加到了GroupView当中,所以无法用setPadding
        addHeaderView(headerView);
        addFooterView(footerView);
    }

    public void initListener() {
        text = (TextView) getHeaderView().findViewById(R.id.header_text);
        this.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // 滑动到最后一个条目
                if (mIsCanLoadMore && isFillWindow && lastItem == adapter.getItemCount() + 1
                        && newState == SCROLL_STATE_IDLE && !isLoad) {
                    ViewGroup.LayoutParams params = getFooterView().getLayoutParams();
                    params.width = LayoutParams.MATCH_PARENT;
                    params.height = LayoutParams.WRAP_CONTENT;
                    getFooterView().setLayoutParams(params);
                    // footerView默认是隐藏的
                    getFooterView().setVisibility(View.VISIBLE);
                    // 滑动到最底
                    smoothScrollToPosition(totalCount);
                    isLoad = true;
                    mLoadMoreListener.onLoadMore();
                }
                if (firstVisible == 0) {
                    isTop = true;
                } else {
                    isTop = false;
                    LayoutParams params = (LayoutParams) getHeaderView().getLayoutParams();
                    params.width = LayoutParams.MATCH_PARENT;
                    if (isStaggered) {
                        params.height = -2;
                    } else {
                        params.height = HEAD_VIEW_HEIGHT;
                    }
                    params.setMargins(0, -getHeaderView().getHeight(), 0, 0);
                    getHeaderView().setLayoutParams(params);
                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalCount = getLayoutManager().getItemCount();
                // GridLayoutManager extends LinearLayoutManager
                if (getLayoutManager() instanceof LinearLayoutManager) {
                    lastItem = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();
                    firstVisible = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
                } else {
                    into = ((StaggeredGridLayoutManager) getLayoutManager()).findLastVisibleItemPositions(into);
                    firstInto =
                            ((StaggeredGridLayoutManager) getLayoutManager()).findFirstVisibleItemPositions(firstInto);
                    lastItem = into[0];
                    firstVisible = firstInto[0];
                }
                int visibleItemCount = getLayoutManager().getChildCount();
                if (totalCount > visibleItemCount) {
                    isFillWindow = true;
                } else {
                    isFillWindow = false;
                }
            }
        });

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 最顶端
                if (isTop && mIsCanRefresh) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startY = event.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            touchMove(event);
                            break;
                        case MotionEvent.ACTION_UP:
                            touchUp();
                            break;
                    }
                }
                return false;
            }
        });
    }

    public void touchMove(MotionEvent event) {
        endY = event.getY();
        moveY = endY - startY;
        // 防止item向上滑出
        if (moveY > 0 && !isRefreshing) {
            // 防止回退文本显示异常
            // smoothScrollToPosition(0);
            // scrollToPosition(0);
            if (getHeaderView().getVisibility() == GONE) {
                getHeaderView().setVisibility(VISIBLE);
            }
            LayoutParams params = (LayoutParams) getHeaderView().getLayoutParams();
            params.width = LayoutParams.MATCH_PARENT;
            // isStaggered会轻微抖屏
            if (isStaggered) {
                params.height = -2;
            } else {
                params.height = HEAD_VIEW_HEIGHT;
            }
            // 使header随moveY的值从顶部渐渐出现
            if (moveY >= 400) {
                moveY = 100 + moveY / 4;
            } else {
                moveY = moveY / 2;
            }
            if (isStaggered) {
                moveY = moveY - getHeaderView().getHeight();
            } else {
                moveY = moveY - HEAD_VIEW_HEIGHT;
            }
            params.setMargins(0, (int) moveY, 0, 0);
            getHeaderView().setLayoutParams(params);
            if (moveY > 80) {
                text.setText(getResources().getString(R.string.release_to_refresh));
            } else {
                text.setText(getResources().getString(R.string.pull_to_refresh));
            }
        } else {
            if (getHeaderView().getVisibility() != GONE && !isRefreshing) {
                getHeaderView().setVisibility(GONE);
            }
        }
    }

    public void touchUp() {
        if (!isRefreshing && (endY - startY) > 0) {
            if (moveY >= 80) {
                LayoutParams params1 = (LayoutParams) getHeaderView().getLayoutParams();
                params1.width = LayoutParams.MATCH_PARENT;
                if (isStaggered) {
                    params1.height = -2;
                } else {
                    params1.height = HEAD_VIEW_HEIGHT;
                }
                text.setText(getResources().getString(R.string.refreshing));
                params1.setMargins(0, 0, 0, 0);
                isRefreshing = true;
                // 刷新数据
                mPullRefreshListener.onRefreshing();
                getHeaderView().setLayoutParams(params1);
            } else {
                doRefreshHalfAnim(moveY);
            }
        }
    }

    public void addHeaderView(View view) {
        if (mHeaderViewInfos.size() >= 1) {
            throw new IllegalStateException("you have been added a headView,don't add again");
        }
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, HEAD_VIEW_HEIGHT);
        view.setLayoutParams(params);
        view.setVisibility(GONE);
        mHeaderViewInfo.view = view;
        mHeaderViewInfo.viewType = HEAD_VIEW_TYPE;
        mHeaderViewInfos.add(mHeaderViewInfo);

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public View getHeaderView() {
        if (mHeaderViewInfos.isEmpty()) {
            throw new IllegalStateException("you must add a HeaderView before!");
        }
        if (mHeaderViewInfo == null || mHeaderViewInfo.view == null || mHeaderViewInfo.viewType != HEAD_VIEW_TYPE) {
            throw new IllegalStateException("check your HeaderViewInfo");
        }
        return mHeaderViewInfo.view;
    }

    public void addFooterView(View view) {
        if (mFooterViewInfos.size() >= 1) {
            throw new IllegalStateException("you have been added a footView,don't add again");
        }
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
        mFooterViewInfo.view = view;
        mFooterViewInfo.viewType = FOOT_VIEW_TYPE;
        mFooterViewInfos.add(mFooterViewInfo);

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public View getFooterView() {
        if (mFooterViewInfos.isEmpty()) {
            throw new IllegalStateException("you must add a FooterView before!");
        }
        if (mFooterViewInfo == null || mFooterViewInfo.view == null || mFooterViewInfo.viewType != FOOT_VIEW_TYPE) {
            throw new IllegalStateException("check your FooterViewInfo");
        }
        return mFooterViewInfo.view;
    }

    @Override
    public Adapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        if (!(adapter instanceof OKRecycleViewAdapter))
            mAdapter = new OKRecycleViewAdapter(mHeaderViewInfo, mFooterViewInfo, adapter);
        super.setAdapter(mAdapter);

        if (isShouldSpan) {
            ((OKRecycleViewAdapter) mAdapter).adjustSpanSize(this);
        }
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        if (layout instanceof GridLayoutManager || layout instanceof StaggeredGridLayoutManager) {
            isShouldSpan = true;
        }
        if (layout instanceof StaggeredGridLayoutManager) {
            isStaggered = true;
        } else {
            isStaggered = false;
        }
        super.setLayoutManager(layout);
    }

    public void setPullToRefreshListener(PullRefreshListerer pullToRefresh) {
        if (mLoadMoreListener == null) {
            initListener();
        }
        this.mPullRefreshListener = pullToRefresh;
    }

    public void setLoadMoreListener(LoadMoreListener loadMoreListener) {
        if (mPullRefreshListener == null) {
            initListener();
        }
        this.mLoadMoreListener = loadMoreListener;
    }

    public void setLoadMoreComplete() {
        LayoutParams params = (LayoutParams) getFooterView().getLayoutParams();
        params.width = 0;
        params.height = 0;
        getFooterView().setLayoutParams(params);
        getFooterView().setVisibility(View.GONE);
        this.getAdapter().notifyDataSetChanged();
        isLoad = false;
    }

    public void setRefreshComplete() {
        doRefreshCompleteAnim();
        getAdapter().notifyDataSetChanged();
        isRefreshing = false;
    }

    private void doRefreshCompleteAnim() {
        mFinishAnimator = ValueAnimator.ofInt(0, -getHeaderView().getHeight());
        mFinishAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                LayoutParams params1 = (LayoutParams) getHeaderView().getLayoutParams();
                params1.width = LayoutParams.MATCH_PARENT;
                if (isStaggered) {
                    params1.height = -2;
                } else {
                    params1.height = HEAD_VIEW_HEIGHT;
                }
                params1.setMargins(0, value, 0, 0);
                getHeaderView().setLayoutParams(params1);
            }
        });
        mFinishAnimator.setDuration(TIME);
        mFinishAnimator.start();
        mFinishAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                getHeaderView().setVisibility(GONE);
            }
        });
    }

    private void doRefreshHalfAnim(float moveY) {
        int a = (int) moveY;
        mHalfAnimator = ValueAnimator.ofInt(a, -HEAD_VIEW_HEIGHT);
        mHalfAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LayoutParams params1 = (LayoutParams) getHeaderView().getLayoutParams();
                params1.width = LayoutParams.MATCH_PARENT;
                if (isStaggered) {
                    params1.height = -2;
                } else {
                    params1.height = HEAD_VIEW_HEIGHT;
                }
                params1.setMargins(0, (Integer) animation.getAnimatedValue(), 0, 0);
                getHeaderView().setLayoutParams(params1);
            }
        });
        mHalfAnimator.setDuration(TIME);
        mHalfAnimator.start();
        mHalfAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // headview 默认隐藏
                getHeaderView().setVisibility(GONE);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mHalfAnimator != null) {
            if (mHalfAnimator.isRunning()) {
                mHalfAnimator.cancel();
            }
            mHalfAnimator = null;
        }
        if (mFinishAnimator != null) {
            if (mFinishAnimator.isRunning()) {
                mFinishAnimator.cancel();
            }
            mFinishAnimator = null;
        }
    }

    public void setCanRefresh(boolean isCanRefresh) {
        mIsCanRefresh = isCanRefresh;
    }

    public void setCanLoadMore(boolean isCanLoadMore) {
        mIsCanLoadMore = isCanLoadMore;
    }

    public interface PullRefreshListerer {
        void onRefreshing();
    }

    public interface LoadMoreListener {
        void onLoadMore();
    }

    public static class ViewInfo {
        public View view;
        public int viewType;
    }
}
