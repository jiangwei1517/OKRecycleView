package com.okrecycleview.luffy.recycleview;

import static com.okrecycleview.luffy.recycleview.OKRecycleView.FOOT_VIEW_TYPE;
import static com.okrecycleview.luffy.recycleview.OKRecycleView.HEAD_VIEW_TYPE;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * author: jiangwei18 on 17/4/9 15:49 email: jiangwei18@baidu.com Hi: jwill金牛
 */

public class OKRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public OKRecycleView.ViewInfo mHeaderViewInfo;
    public OKRecycleView.ViewInfo mFooterViewInfo;
    public RecyclerView.Adapter mAdapter;
    private boolean isStaggered;

    public OKRecycleViewAdapter(OKRecycleView.ViewInfo headerViewInfo, OKRecycleView.ViewInfo footerViewInfo,
            RecyclerView.Adapter adapter) {
        mAdapter = adapter;
        mHeaderViewInfo = headerViewInfo;
        mFooterViewInfo = footerViewInfo;
    }

    public int getHeadersCount() {
        return mHeaderViewInfo == null ? 0 : 1;
    }

    public int getFootersCount() {
        return mFooterViewInfo == null ? 0 : 1;
    }

    public void adjustSpanSize(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            final GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
            manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int numHeaders = getHeadersCount();
                    int adjPosition = position - numHeaders;
                    // 头布局和尾布局要占manager.getSpanCount()个位置
                    // 其余都是占1个位置
                    if (position < numHeaders || adjPosition >= mAdapter.getItemCount())
                        return manager.getSpanCount();
                    return 1;
                }
            });
        }
        // 如果是瀑布流的布局 通过viewholder设置头尾布局所占格数
        if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            isStaggered = true;
        }
    }

    public void setVisibility(View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (view.getVisibility() == View.GONE) {
            params.width = 0;
            params.height = 0;
        } else {
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            params.height = OKRecycleView.HEAD_VIEW_HEIGHT;
        }
        view.setLayoutParams(params);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEAD_VIEW_TYPE) {
            View view = mHeaderViewInfo.view;
            setVisibility(view);
            // 如果是头布局,并且是isStaggered形式,setFullSpan设置头填充满
            return viewHolder(view);
        } else if (viewType == FOOT_VIEW_TYPE) {
            View view = mFooterViewInfo.view;
            setVisibility(view);
            // 如果是尾布局,并且是isStaggered形式,setFullSpan设置尾填充满
            return viewHolder(view);
        }
        return mAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int numHeaders = getHeadersCount();
        // 头布局不进行处理
        if (position < numHeaders) {
            return;
        }
        int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (mAdapter != null) {
            adapterCount = mAdapter.getItemCount();
            // 尾布局不进行处理
            if (adjPosition < adapterCount) {
                mAdapter.onBindViewHolder(holder, adjPosition);
                return;
            }
        }

    }

    @Override
    public long getItemId(int position) {
        int numHeaders = getHeadersCount();
        if (mAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            int adapterCount = mAdapter.getItemCount();
            if (adjPosition < adapterCount) {
                return mAdapter.getItemId(adjPosition);
            }
        }
        return -1;
    }

    @Override
    public int getItemViewType(int position) {
        int numHeaders = getHeadersCount();
        // position = 0
        if (position < numHeaders) {
            return mHeaderViewInfo.viewType;
        }
        int adjPosition = position - numHeaders;
        int adapterPosition = 0;
        if (mAdapter != null) {
            adapterPosition = mAdapter.getItemCount();
            if (adjPosition < adapterPosition) {
                return mAdapter.getItemViewType(adjPosition);
            }
        }
        return mFooterViewInfo.viewType;
    }

    @Override
    public int getItemCount() {
        if (mAdapter != null) {
            return getHeadersCount() + getFootersCount() + mAdapter.getItemCount();
        } else {
            return getHeadersCount() + getFootersCount();
        }
    }

    private RecyclerView.ViewHolder viewHolder(final View itemView) {
        if (isStaggered) {
            StaggeredGridLayoutManager.LayoutParams params = new StaggeredGridLayoutManager.LayoutParams(
                    StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT, 0);
            params.setFullSpan(true);
            itemView.setLayoutParams(params);
        }
        return new RecyclerView.ViewHolder(itemView) {
        };
    }
}
