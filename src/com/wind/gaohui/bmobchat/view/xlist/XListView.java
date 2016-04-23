package com.wind.gaohui.bmobchat.view.xlist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.wind.gaohui.bombchat.R;

/**
 * ����ˢ��XListView
 * @author gaohui
 *
 */
public class XListView extends ListView implements OnScrollListener {
	private float mLastY = -1; // save event y
	private OnScrollListener mScrollListener; // user's scroll listener
	private Scroller mScroller;
	private Context mContext;
	private XListViewHeader mHeaderView;
	private RelativeLayout mHeaderViewContent;

	private int mHeaderViewHeight; // header view's height

	// the interface to trigger refresh and load more.
	private IXListViewListener mListViewListener;

	// -- footer view
	private XListViewFooter mFooterView = null;
	private boolean mEnablePullLoad = false;
	private boolean mPullLoading = false;

	private boolean mPullRefreshing = false; // is refreashing.
	private boolean mEnablePullRefresh = true;

	// for mScroller, scroll back from header or footer.
	private int mScrollBack;
	private final static int SCROLLBACK_HEADER = 0;
	private final static int SCROLLBACK_FOOTER = 1;

	// total list items, used to detect is at the bottom of listview.
	private int mTotalItemCount;

	private final static int SCROLL_DURATION = 400; // scroll back duration
	private static final float OFFSET_RADIO = 1.8f; // support iOS like pull
													// feature

	private final static int PULL_LOAD_MORE_DELTA = 50; // when pull up >= 50px
														// at bottom, trigger
														// load more.

	public XListView(Context context) {
		super(context);
		initWithContext(context);
	}

	public XListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initWithContext(context);
	}

	public XListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initWithContext(context);
	}

	// ��ʼ��
	private void initWithContext(Context context) {
		mScroller = new Scroller(context, new DecelerateInterpolator());
		// XListView need the scroll event, and it will dispatch the event to
		// user's listener (as a proxy).
		super.setOnScrollListener(this);
		mContext = context;

		mHeaderView = new XListViewHeader(context);
		mHeaderViewContent = (RelativeLayout) mHeaderView
				.findViewById(R.id.xlistview_header_content);
		addHeaderView(mHeaderView);

		// ��ʼ��header�ĸ߶�
		mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@SuppressLint("NewApi")
					@Override
					public void onGlobalLayout() {
						mHeaderViewHeight = mHeaderView.getHeight();
						getViewTreeObserver()
								.removeOnGlobalLayoutListener(this);
					}
				});
	}

	public boolean getPullLoading() {
		return this.mPullLoading;
	}

	public boolean getPullRefreshing() {
		return this.mPullRefreshing;
	}

	/**
	 * ����ˢ��
	 */
	public void pullRefreshing() {
		if (!mEnablePullRefresh) {
			return;
		}
		// ����header view�ĸ߶�
		mHeaderView.setVisiableHeight(mHeaderViewHeight);
		mPullRefreshing = true;
		mHeaderView.setState(XListViewHeader.STATE_REFRESHING);
	}

	/**
	 * enable or disable pull down refresh feature. ��������ˢ�²�����
	 */
	public void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) { // disable, hide the content
			mHeaderViewContent.setVisibility(View.INVISIBLE);
		} else {
			mHeaderViewContent.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * enable or disable pull up load more feature. �����������ظ��಻����
	 */
	public void setPullLoadEnable(boolean enable) {
		if (mEnablePullLoad == enable)
			return;

		mEnablePullLoad = enable;
		if (!mEnablePullLoad) {
			if (mFooterView != null) {
				this.removeFooterView(mFooterView);
			}
		} else {
			// mPullLoading = false;
			if (mFooterView == null) {
				mFooterView = new XListViewFooter(mContext);
				mFooterView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// ���ظ���
						startLoadMore();
					}
				});
			}
			this.addFooterView(mFooterView);
			mFooterView.setState(XListViewFooter.STATE_NORMAL);
			// both "pull up" and "click" will invoke load more.
		}
	}

	/**
	 * ���ظ���
	 */
	private void startLoadMore() {
		mPullLoading = true;
		mFooterView.setState(XListViewFooter.STATE_LOADING);
		if (mListViewListener != null) {
			mListViewListener.onLoadMore();
		}
	}

	/**
	 * ֹͣˢ�£�����header view
	 */
	public void stopRefresh() {
		Time time = new Time();
		time.setToNow();
		mHeaderView.setRefreshTime(time.format("%Y-%m-%d %T"));
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			resetHeaderHeight();
		}
	}

	/**
	 * stop load more, reset footer view.
	 */
	public void stopLoadMore() {
		if (mPullLoading == true) {
			mPullLoading = false;
			mFooterView.setState(XListViewFooter.STATE_NORMAL);
		}
	}

	/**
	 * ����header view�ĸ߶�
	 */
	private void resetHeaderHeight() {
		int height = mHeaderView.getVisiableHeight();
		if (height == 0) { // ���ɼ�
			return;
		}
		// ����ˢ�£�header view��û����ȫ��ʾ��do nothing
		if (mPullRefreshing && height <= mHeaderViewHeight) {
			return;
		}
		int finalHeight = 0;
		// TODO ���
		if (mPullRefreshing && height > mHeaderViewHeight) {
			finalHeight = mHeaderViewHeight;
		}
		mScrollBack = SCROLLBACK_HEADER;
		mScroller.startScroll(0, height, 0, finalHeight - height,
				SCROLL_DURATION);
		// trigger computeScroll ����invalidate�������ػ�ҳ�棬���ջ����computeScroll����
		invalidate();
	}
	
	@Override
	public void computeScroll() {
		if(mScroller.computeScrollOffset()) {
			if(mScrollBack == SCROLLBACK_HEADER) {
				mHeaderView.setVisiableHeight(mScroller.getCurrY());
			} else {
				mFooterView.setBottomMargin(mScroller.getCurrY());
			}
			postInvalidate();
			invokeOnScrolling();
		}
		super.computeScroll();
	}

	private void invokeOnScrolling() {
		if (mScrollListener instanceof OnXScrollListener) {
			OnXScrollListener l = (OnXScrollListener) mScrollListener;
			l.onXScrolling(this);
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastY = ev.getRawY();
			break;
		case MotionEvent.ACTION_UP:
			final float deltaY = ev.getRawY() - mLastY;
			mLastY = ev.getRawY();
			// �ж�������ˢ�»�����������
			if (getFirstVisiblePosition() == 0
					&& (mHeaderViewHeight > 0 || deltaY > 0)) {
				// ����ˢ��
				// the first item is showing, header has shown or pull down.
				updateHeaderHeight(deltaY / OFFSET_RADIO);
				invokeOnScrolling();
			} else if (mEnablePullLoad
					&& (getLastVisiblePosition() == mTotalItemCount - 1)
					&& (mFooterView.getBottomMargin() > 0 || deltaY < 0)) {
				// ��������
				// last item, already pulled up or want to pull up.
				updateFooterHeight(-deltaY / OFFSET_RADIO);
			}
			break;
		default:
			mLastY = -1; // reset
			if (getFirstVisiblePosition() == 0) {
				// invoke refresh
				if (mEnablePullRefresh
						&& mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
					mPullRefreshing = true;
					mHeaderView.setState(XListViewHeader.STATE_REFRESHING);
					if (mListViewListener != null) {
						mListViewListener.onRefresh();
					}
				}
				resetHeaderHeight();
			} else if (getLastVisiblePosition() == mTotalItemCount - 1) {
				// invoke load more.
				if (mEnablePullLoad) {
					if (mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA) {
						startLoadMore();
					}
					resetFooterHeight();
				}
			}
			break;
		}

		return super.onTouchEvent(ev);
	}

	/**
	 * ����footer view�ĸ߶�
	 */
	private void resetFooterHeight() {
		int bottomMargin = mFooterView.getBottomMargin();
		if (bottomMargin > 0) {
			mScrollBack = SCROLLBACK_FOOTER;
			mScroller.startScroll(0, bottomMargin, 0, -bottomMargin,
					SCROLL_DURATION);
			invalidate();
		}
	}

	private void updateFooterHeight(float delta) {
		int height = mFooterView.getBottomMargin() + (int) delta;
		if (mEnablePullLoad && !mPullLoading) {
			if (height > PULL_LOAD_MORE_DELTA) { // height enough to invoke load
													// more
				mFooterView.setState(XListViewFooter.STATE_READY);
			} else {
				mFooterView.setState(XListViewFooter.STATE_NORMAL);
			}
		}
		mFooterView.setBottomMargin(height);

	}

	private void updateHeaderHeight(float delta) {
		mHeaderView.setVisiableHeight((int) delta
				+ mHeaderView.getVisiableHeight());
		if (mEnablePullRefresh && !mPullRefreshing) { // δ����ˢ��״̬�����¼�ͷ
			if (mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
				mHeaderView.setState(XListViewHeader.STATE_READY);
			} else {
				mHeaderView.setState(XListViewHeader.STATE_NORMAL);
			}
		}
		setSelection(0); // scroll to top each time

	}

	public void setListViewListener(IXListViewListener mListViewListener) {
		this.mListViewListener = mListViewListener;
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mScrollListener != null) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		mTotalItemCount = totalItemCount;
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount);
		}
	}

	/**
	 * you can listen ListView.OnScrollListener or this one. it will invoke
	 * onXScrolling when header/footer scroll back. ��header view ��footer
	 * view������ʱ������onXScrolling����
	 */
	public interface OnXScrollListener extends OnScrollListener {
		public void onXScrolling(View view);
	}

	/**
	 * ����ˢ�º��������ظ���ļ��� implements this interface to get refresh/load more event.
	 */
	public interface IXListViewListener {
		public void onRefresh();

		public void onLoadMore();
	}
}
