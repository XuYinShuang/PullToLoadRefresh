package com.zhiyuan3g.pulltoloadrefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;

/**
 * Created by Administrator on 2016/7/28.
 */
public class RefreshListView extends ListView implements AbsListView.OnScrollListener {

    private View headView;//头部布局对象
    private int headViewHeight;//头布局的高度
    private int downY;//按下时Y的偏移量
    private int firstVisibleItemPosition;//屏幕显示在第一条的索引

    private final int DOWN_PULL_REFRESH = 0;//下拉刷新状态
    private final int REFRESH_REFRESH = 1;//松开刷新
    private final int REFRESHING = 2;//刷新中
    private int currentState = DOWN_PULL_REFRESH;//头布局状态默认为下拉刷新

    private Animation upAnimation;//向上选装的动画
    private Animation downAnimation;//向下旋转的动画

    private ImageView list_head_image;//头布局的剪头
    private ProgressBar list_head_progressBar;//头布局的进度条
    private TextView head_txt_refresh;//头布局的状态
    private TextView head_txt_currentTime;//头布局的最后更新时间

    private OnRefreshListener mOnRefreshListener;//刷新监听
    private boolean isScrollToBottom;//是否滑动到底部
    private View footerView;//脚布局对象
    private int footerViewHeight;//脚布局的高度
    private boolean isLoadingMore = false;//是否加载更多功能

    public RefreshListView(Context context) {
        super(context);
        initFooterView();
        initHeadView();
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFooterView();
        initHeadView();
        this.setOnScrollListener(this);
    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFooterView();
        initHeadView();

    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mOnRefreshListener = listener;
    }

    //初始化脚布局
    private void initFooterView() {
        footerView = View.inflate(getContext(), R.layout.listview_footer, null);
        footerView.measure(0, 0);
        footerViewHeight = footerView.getMeasuredHeight();//得到脚布局视图的高度
        footerView.setPadding(0, -footerViewHeight, 0, 0);
        this.addFooterView(footerView);

    }

    //初始化头布局
    private void initHeadView() {
        headView = View.inflate(getContext(), R.layout.item_head, null);
        list_head_image = (ImageView) headView.findViewById(R.id.list_head_image);
        list_head_progressBar = (ProgressBar) headView.findViewById(R.id.list_head_progressbar);
        head_txt_currentTime = (TextView) headView.findViewById(R.id.head_txt_currentTime);
        head_txt_refresh = (TextView) headView.findViewById(R.id.head_txt_refresh);

        head_txt_currentTime.setText("最后刷新时间：" + getLastUpdateTime());

        headView.measure(0, 0);
        headViewHeight = headView.getMeasuredHeight();
        headView.setPadding(0, -headViewHeight, 0, 0);
        this.addHeaderView(headView);

        initAnimation();


    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = (int) ev.getY();//绝对位置
                break;
            case MotionEvent.ACTION_UP:
                if (currentState == REFRESH_REFRESH) {
                    headView.setPadding(0, 0, 0, 0);
                    currentState = REFRESHING;

                    refreshHeaderView();//刷新头布局

                    if (mOnRefreshListener != null) {
                        mOnRefreshListener.onDownPullRefresh();
                    }
                } else if (currentState == DOWN_PULL_REFRESH) {
                    //隐藏头布局
                    headView.setPadding(0, -headViewHeight, 0, 0);

                }
                break;
            case MotionEvent.ACTION_MOVE:
                int moveY = (int) ev.getY();//移动Y的距离
                int diff = (moveY - downY) / 2;//移动时Y-按下的Y=间距
                int paddingTop = -headViewHeight + diff;//此时头部还隐藏的高度
                //同时想要刷新一下，必须要下拉一下，即diff>0，只有当firstVisibleItemPosition=0,才能进行相应的刷新准备
                if (firstVisibleItemPosition == 0 && paddingTop > -headViewHeight) {
                    if (paddingTop > 0 && currentState == DOWN_PULL_REFRESH) {
                        currentState = REFRESH_REFRESH;//松开时刷新状态
                        refreshHeaderView();
                    }
                    headView.setPadding(0, paddingTop, 0, 0);//头布局拉伸的距离
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    //根据currentState刷新头布局的状态
    private void refreshHeaderView() {
        switch (currentState) {
            case DOWN_PULL_REFRESH://下拉刷新
                head_txt_refresh.setText("下拉刷新");
                //执行向下旋转
                list_head_image.startAnimation(downAnimation);
                break;
            case REFRESH_REFRESH://松开刷新
                head_txt_refresh.setText("松开刷新");
                list_head_image.startAnimation(upAnimation);
                break;
            case REFRESHING://刷新进行时
                list_head_image.clearAnimation();
                list_head_image.setVisibility(GONE);
                list_head_progressBar.setVisibility(VISIBLE);
                head_txt_refresh.setText("z正在刷新中....");
                break;
            default:
                break;
        }
    }

    //初始化动画
    private void initAnimation() {
        upAnimation = new RotateAnimation(0f, -180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        upAnimation.setDuration(500);
        upAnimation.setFillAfter(true);

        downAnimation = new RotateAnimation(-180f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        downAnimation.setDuration(500);
        downAnimation.setFillAfter(true);
    }

    //获得系统最新时间
    public String getLastUpdateTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-DD HH:MM:ss");
        return format.format(System.currentTimeMillis());
    }

    //掩藏头布局
    public void hideHeadView() {
        headView.setPadding(0, -headViewHeight, 0, 0);
        list_head_image.setVisibility(VISIBLE);
        list_head_progressBar.setVisibility(GONE);
        head_txt_refresh.setText("下拉刷新");
        head_txt_currentTime.setText("最后刷新时间：" + getLastUpdateTime());
        currentState = DOWN_PULL_REFRESH;
    }

    //隐藏脚布局
    public void hideFooterView() {
        footerView.setPadding(0, -footerViewHeight, 0, 0);
        isLoadingMore = false;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //当滑动停止或者惯性滑动时
        if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_FLING) {
            if (isScrollToBottom && !isLoadingMore) {
                //当前在底部
                isLoadingMore = true;
                footerView.setPadding(0, 0, 0, 0);
                this.setSelection(this.getCount());

                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onLoadingMore();
                }
            }
        }
    }

    /**
     * @param view
     * @param firstVisibleItem //当前屏幕显示在顶部的Item的position
     * @param visibleItemCount //当前屏幕显示了多个条目的总数
     * @param totalItemCount   //listView的总条目的总数
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        firstVisibleItemPosition = firstVisibleItem;
        if (getLastVisiblePosition() == (totalItemCount - 1)) {
            isScrollToBottom = true;
        } else {
            isLoadingMore = false;
        }
    }

    public interface OnRefreshListener {

        //下拉刷新
        void onDownPullRefresh();

        //上拉加载更多
        void onLoadingMore();
    }
}
