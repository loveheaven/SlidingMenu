package com.jeremyfeinstein.slidingmenu.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;

public class CustomViewBehind extends ViewGroup {

	private static final String TAG = "CustomViewBehind";

	private static final int MARGIN_THRESHOLD = 48; // dips
	private int mTouchMode = SlidingMenu.TOUCHMODE_MARGIN;

	private CustomViewAbove mViewAbove;

	private View mContent;
	private View mSecondaryContent;
	private int mMarginThreshold;
	private int mWidthOffset;
	private CanvasTransformer mTransformer;
	private boolean mChildrenEnabled;

	public CustomViewBehind(Context context) {
		this(context, null);
	}

	public CustomViewBehind(Context context, AttributeSet attrs) {
		super(context, attrs);
		mMarginThreshold = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
				MARGIN_THRESHOLD, getResources().getDisplayMetrics());
	}

	public void setCustomViewAbove(CustomViewAbove customViewAbove) {
		mViewAbove = customViewAbove;
	}

	public void setCanvasTransformer(CanvasTransformer t) {
		mTransformer = t;
	}

	public void setWidthOffset(int i) {
		mWidthOffset = i;
		requestLayout();
	}
	
	public void setMarginThreshold(int marginThreshold) {
		mMarginThreshold = marginThreshold;
	}
	
	public int getMarginThreshold() {
		return mMarginThreshold;
	}

	public int getBehindWidth() {
		return mContent.getWidth();
	}
	public int getBehindHeight() {
        return mContent.getWidth();
    }

	public void setContent(View v) {
		if (mContent != null)
			removeView(mContent);
		mContent = v;
		addView(mContent);
	}

	public View getContent() {
		return mContent;
	}

	/**
	 * Sets the secondary (right) menu for use when setMode is called with SlidingMenu.LEFT_RIGHT.
	 * @param v the right menu
	 */
	public void setSecondaryContent(View v) {
		if (mSecondaryContent != null)
			removeView(mSecondaryContent);
		mSecondaryContent = v;
		addView(mSecondaryContent);
	}

	public View getSecondaryContent() {
		return mSecondaryContent;
	}

	public void setChildrenEnabled(boolean enabled) {
		mChildrenEnabled = enabled;
	}

	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
		if (mTransformer != null)
			invalidate();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent e) {
		return !mChildrenEnabled;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		return !mChildrenEnabled;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (mTransformer != null) {
			canvas.save();
			mTransformer.transformCanvas(canvas, mViewAbove.getPercentOpen());
			super.dispatchDraw(canvas);
			canvas.restore();
		} else
			super.dispatchDraw(canvas);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int width = r - l;
		final int height = b - t;
		if(mMode < SlidingMenu.TOP) {
    		mContent.layout(0, 0, width-mWidthOffset, height);
    		if (mSecondaryContent != null)
    			mSecondaryContent.layout(0, 0, width-mWidthOffset, height);
		} else {
		    mContent.layout(0, 0, width, height-mWidthOffset);
            if (mSecondaryContent != null)
                mSecondaryContent.layout(0, 0, width, height-mWidthOffset);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = getDefaultSize(0, widthMeasureSpec);
		int height = getDefaultSize(0, heightMeasureSpec);
		setMeasuredDimension(width, height);
		if(mMode < SlidingMenu.TOP) {
    		final int contentWidth = getChildMeasureSpec(widthMeasureSpec, 0, width-mWidthOffset);
    		final int contentHeight = getChildMeasureSpec(heightMeasureSpec, 0, height);
    		mContent.measure(contentWidth, contentHeight);
    		if (mSecondaryContent != null)
    			mSecondaryContent.measure(contentWidth, contentHeight);
		} else {
		    final int contentWidth = getChildMeasureSpec(widthMeasureSpec, 0, width);
            final int contentHeight = getChildMeasureSpec(heightMeasureSpec, 0, height-mWidthOffset);
            mContent.measure(contentWidth, contentHeight);
            if (mSecondaryContent != null)
                mSecondaryContent.measure(contentWidth, contentHeight);
		}
	}

	private int mMode;
	private boolean mFadeEnabled;
	private final Paint mFadePaint = new Paint();
	private float mScrollScale;
	private Drawable mShadowDrawable;
	private Drawable mSecondaryShadowDrawable;
	private int mShadowWidth;
	private float mFadeDegree;

	public void setMode(int mode) {
		if (mode == SlidingMenu.LEFT || mode == SlidingMenu.RIGHT ||
		        mode == SlidingMenu.TOP || mode == SlidingMenu.BOTTOM) {
			if (mContent != null)
				mContent.setVisibility(View.VISIBLE);
			if (mSecondaryContent != null)
				mSecondaryContent.setVisibility(View.INVISIBLE);
		}
		mMode = mode;
	}

	public int getMode() {
		return mMode;
	}

	public void setScrollScale(float scrollScale) {
		mScrollScale = scrollScale;
	}

	public float getScrollScale() {
		return mScrollScale;
	}

	public void setShadowDrawable(Drawable shadow) {
		mShadowDrawable = shadow;
		invalidate();
	}

	public void setSecondaryShadowDrawable(Drawable shadow) {
		mSecondaryShadowDrawable = shadow;
		invalidate();
	}

	public void setShadowWidth(int width) {
		mShadowWidth = width;
		invalidate();
	}

	public void setFadeEnabled(boolean b) {
		mFadeEnabled = b;
	}

	public void setFadeDegree(float degree) {
		if (degree > 1.0f || degree < 0.0f)
			throw new IllegalStateException("The BehindFadeDegree must be between 0.0f and 1.0f");
		mFadeDegree = degree;
	}

	public int getMenuPage(int page) {
		page = (page > 1) ? 2 : ((page < 1) ? 0 : page);
		if (mMode == SlidingMenu.LEFT && page > 1) {
			return 0;
		} else if (mMode == SlidingMenu.RIGHT && page < 1) {
			return 2;
		} else if (mMode == SlidingMenu.TOP && page > 1) {
            return 0;
        } else if (mMode == SlidingMenu.BOTTOM && page < 1) {
            return 2;
        } else {
			return page;
		}
	}

	public void scrollBehindTo(View content, int x, int y) {
		int vis = View.VISIBLE;		
		if (mMode == SlidingMenu.LEFT) {
			if (x >= content.getLeft()) vis = View.INVISIBLE;
			scrollTo((int)((x + getBehindWidth())*mScrollScale), y);
		} else if (mMode == SlidingMenu.RIGHT) {
			if (x <= content.getLeft()) vis = View.INVISIBLE;
			scrollTo((int)(getBehindWidth() - getWidth() + 
					(x-getBehindWidth())*mScrollScale), y);
		} else if (mMode == SlidingMenu.LEFT_RIGHT) {
			mContent.setVisibility(x >= content.getLeft() ? View.INVISIBLE : View.VISIBLE);
			mSecondaryContent.setVisibility(x <= content.getLeft() ? View.INVISIBLE : View.VISIBLE);
			vis = x == 0 ? View.INVISIBLE : View.VISIBLE;
			if (x <= content.getLeft()) {
				scrollTo((int)((x + getBehindWidth())*mScrollScale), y);				
			} else {
				scrollTo((int)(getBehindWidth() - getWidth() + 
						(x-getBehindWidth())*mScrollScale), y);				
			}
		} else if (mMode == SlidingMenu.TOP) {
            if (y >= content.getTop()) vis = View.INVISIBLE;
            scrollTo(x, (int)((y + getBehindHeight())*mScrollScale));
        } else if (mMode == SlidingMenu.BOTTOM) {
            if (y <= content.getTop()) vis = View.INVISIBLE;
            scrollTo(x, (int)(getBehindHeight() - getHeight() + 
                    (y-getBehindHeight())*mScrollScale));
        } else if (mMode == SlidingMenu.TOP_BOTTOM) {
            mContent.setVisibility(y >= content.getTop() ? View.INVISIBLE : View.VISIBLE);
            mSecondaryContent.setVisibility(y <= content.getTop() ? View.INVISIBLE : View.VISIBLE);
            vis = y == 0 ? View.INVISIBLE : View.VISIBLE;
            if (y <= content.getTop()) {
                scrollTo(x, (int)((y + getBehindHeight())*mScrollScale));                
            } else {
                scrollTo(x, (int)(getBehindHeight() - getHeight() + 
                        (y-getBehindHeight())*mScrollScale));             
            }
        }
		if (vis == View.INVISIBLE)
			Log.v(TAG, "behind INVISIBLE");
		setVisibility(vis);
	}

	public int getMenuLeft(View content, int page) {
		if (mMode == SlidingMenu.LEFT) {
			switch (page) {
			case 0:
				return content.getLeft() - getBehindWidth();
			case 2:
				return content.getLeft();
			}
		} else if (mMode == SlidingMenu.RIGHT) {
			switch (page) {
			case 0:
				return content.getLeft();
			case 2:
				return content.getLeft() + getBehindWidth();	
			}
		} else if (mMode == SlidingMenu.LEFT_RIGHT) {
			switch (page) {
			case 0:
				return content.getLeft() - getBehindWidth();
			case 2:
				return content.getLeft() + getBehindWidth();
			}
		}
		return content.getLeft();
	}
	
	public int getMenuTop(View content, int page) {
        if (mMode == SlidingMenu.TOP) {
            switch (page) {
            case 0:
                return content.getTop() - getBehindHeight();
            case 2:
                return content.getTop();
            }
        } else if (mMode == SlidingMenu.BOTTOM) {
            switch (page) {
            case 0:
                return content.getTop();
            case 2:
                return content.getTop() + getBehindHeight();    
            }
        } else if (mMode == SlidingMenu.TOP_BOTTOM) {
            switch (page) {
            case 0:
                return content.getTop() - getBehindHeight();
            case 2:
                return content.getTop() + getBehindHeight();
            }
        }
        return content.getLeft();
    }

	public int getAbsLeftBound(View content) {
		if (mMode == SlidingMenu.LEFT || mMode == SlidingMenu.LEFT_RIGHT) {
			return content.getLeft() - getBehindWidth();
		} else if (mMode == SlidingMenu.RIGHT) {
			return content.getLeft();
		}
		return 0;
	}

	public int getAbsRightBound(View content) {
		if (mMode == SlidingMenu.LEFT) {
			return content.getLeft();
		} else if (mMode == SlidingMenu.RIGHT || mMode == SlidingMenu.LEFT_RIGHT) {
			return content.getLeft() + getBehindWidth();
		}
		return 0;
	}
	
	public int getAbsTopBound(View content) {
        if (mMode == SlidingMenu.TOP || mMode == SlidingMenu.TOP_BOTTOM) {
            return content.getTop() - getBehindHeight();
        } else if (mMode == SlidingMenu.BOTTOM) {
            return content.getTop();
        }
        return 0;
    }

    public int getAbsBottomBound(View content) {
        if (mMode == SlidingMenu.TOP) {
            return content.getTop();
        } else if (mMode == SlidingMenu.BOTTOM || mMode == SlidingMenu.TOP_BOTTOM) {
            return content.getTop() + getBehindHeight();
        }
        return 0;
    }

	public boolean marginTouchAllowed(View content, int pos) {
		int left = content.getLeft();
		int right = content.getRight();
		int top = content.getTop();
		int bottom = content.getBottom();
		if (mMode == SlidingMenu.LEFT) {
			return (pos >= left && pos <= mMarginThreshold + left);
		} else if (mMode == SlidingMenu.RIGHT) {
			return (pos <= right && pos >= right - mMarginThreshold);
		} else if (mMode == SlidingMenu.LEFT_RIGHT) {
			return (pos >= left && pos <= mMarginThreshold + left) || 
					(pos <= right && pos >= right - mMarginThreshold);
		} else if (mMode == SlidingMenu.TOP) {
            return (pos >= top && pos <= mMarginThreshold + top);
        } else if (mMode == SlidingMenu.BOTTOM) {
            return (pos <= bottom && pos >= bottom - mMarginThreshold);
        } else if (mMode == SlidingMenu.TOP_BOTTOM) {
            return (pos >= top && pos <= mMarginThreshold + top) || 
                    (pos <= bottom && pos >= bottom - mMarginThreshold);
        }
		return false;
	}

	public void setTouchMode(int i) {
		mTouchMode = i;
	}

	public boolean menuOpenTouchAllowed(View content, int currPage, float x) {
		switch (mTouchMode) {
		case SlidingMenu.TOUCHMODE_FULLSCREEN:
			return true;
		case SlidingMenu.TOUCHMODE_MARGIN:
			return menuTouchInQuickReturn(content, currPage, x);
		}
		return false;
	}

	public boolean menuTouchInQuickReturn(View content, int currPage, float x) {
		if (mMode == SlidingMenu.LEFT || (mMode == SlidingMenu.LEFT_RIGHT && currPage == 0)) {
			return x >= content.getLeft();
		} else if (mMode == SlidingMenu.RIGHT || (mMode == SlidingMenu.LEFT_RIGHT && currPage == 2)) {
			return x <= content.getRight();
		} else if (mMode == SlidingMenu.TOP || (mMode == SlidingMenu.TOP_BOTTOM && currPage == 0)) {
            return x >= content.getTop();
        } else if (mMode == SlidingMenu.BOTTOM || (mMode == SlidingMenu.TOP_BOTTOM && currPage == 2)) {
            return x <= content.getBottom();
        }
		return false;
	}

	public boolean menuClosedSlideAllowed(float dx) {
		if (mMode == SlidingMenu.LEFT) {
			return dx > 0;
		} else if (mMode == SlidingMenu.RIGHT) {
			return dx < 0;
		} else if (mMode == SlidingMenu.LEFT_RIGHT) {
			return true;
		} else if (mMode == SlidingMenu.TOP) {
            return dx > 0;
        } else if (mMode == SlidingMenu.BOTTOM) {
            return dx < 0;
        } else if (mMode == SlidingMenu.TOP_BOTTOM) {
            return true;
        }
		return false;
	}

	public boolean menuOpenSlideAllowed(float dx) {
		if (mMode == SlidingMenu.LEFT) {
			return dx < 0;
		} else if (mMode == SlidingMenu.RIGHT) {
			return dx > 0;
		} else if (mMode == SlidingMenu.LEFT_RIGHT) {
			return true;
		} else if (mMode == SlidingMenu.TOP) {
            return dx < 0;
        } else if (mMode == SlidingMenu.BOTTOM) {
            return dx > 0;
        } else if (mMode == SlidingMenu.TOP_BOTTOM) {
            return true;
        }
		return false;
	}

	public void drawShadow(View content, Canvas canvas) {
		if (mShadowDrawable == null || mShadowWidth <= 0) return;
		int left = 0;
		if (mMode == SlidingMenu.LEFT) {
			left = content.getLeft() - mShadowWidth;
		} else if (mMode == SlidingMenu.RIGHT) {
			left = content.getRight();
		} else if (mMode == SlidingMenu.LEFT_RIGHT) {
			if (mSecondaryShadowDrawable != null) {
				left = content.getRight();
				mSecondaryShadowDrawable.setBounds(left, 0, left + mShadowWidth, getHeight());
				mSecondaryShadowDrawable.draw(canvas);
			}
			left = content.getLeft() - mShadowWidth;
		} else if (mMode == SlidingMenu.TOP) {
            left = content.getTop() - mShadowWidth;
        } else if (mMode == SlidingMenu.BOTTOM) {
            left = content.getBottom();
        } else if (mMode == SlidingMenu.TOP_BOTTOM) {
            if (mSecondaryShadowDrawable != null) {
                left = content.getBottom();
                mSecondaryShadowDrawable.setBounds(0, left, getWidth(), left + mShadowWidth);
                mSecondaryShadowDrawable.draw(canvas);
            }
            left = content.getLeft() - mShadowWidth;
        }
		if(mMode < SlidingMenu.TOP) {
		    mShadowDrawable.setBounds(left, 0, left + mShadowWidth, getHeight());
		} else {
		    mShadowDrawable.setBounds(0, left, getWidth(), left + mShadowWidth);
		}
		mShadowDrawable.draw(canvas);
	}

	public void drawFade(View content, Canvas canvas, float openPercent) {
		if (!mFadeEnabled) return;
		final int alpha = (int) (mFadeDegree * 255 * Math.abs(1-openPercent));
		mFadePaint.setColor(Color.argb(alpha, 0, 0, 0));
		int left = 0;
		int right = 0;
		if (mMode == SlidingMenu.LEFT) {
			left = content.getLeft() - getBehindWidth();
			right = content.getLeft();
		} else if (mMode == SlidingMenu.RIGHT) {
			left = content.getRight();
			right = content.getRight() + getBehindWidth();			
		} else if (mMode == SlidingMenu.LEFT_RIGHT) {
			left = content.getLeft() - getBehindWidth();
			right = content.getLeft();
			canvas.drawRect(left, 0, right, getHeight(), mFadePaint);
			left = content.getRight();
			right = content.getRight() + getBehindWidth();			
		} else if (mMode == SlidingMenu.TOP) {
            left = content.getTop() - getBehindHeight();
            right = content.getTop();
        } else if (mMode == SlidingMenu.BOTTOM) {
            left = content.getBottom();
            right = content.getBottom() + getBehindHeight();          
        } else if (mMode == SlidingMenu.TOP_BOTTOM) {
            left = content.getTop() - getBehindHeight();
            right = content.getTop();
            canvas.drawRect(0, left, getWidth(), right, mFadePaint);
            left = content.getBottom();
            right = content.getBottom() + getBehindHeight();          
        }
		if(mMode < SlidingMenu.TOP) {
		    canvas.drawRect(left, 0, right, getHeight(), mFadePaint);
		} else {
		    canvas.drawRect(0, left, getWidth(), right, mFadePaint);
		}
	}
	
	private boolean mSelectorEnabled = true;
	private Bitmap mSelectorDrawable;
	private View mSelectedView;
	
	public void drawSelector(View content, Canvas canvas, float openPercent) {
		if (!mSelectorEnabled) return;
		if (mSelectorDrawable != null && mSelectedView != null) {
			String tag = (String) mSelectedView.getTag(R.id.selected_view);
			if (tag.equals(TAG+"SelectedView")) {
				canvas.save();
				int left, right, offset;
				offset = (int) ((mMode < SlidingMenu.TOP?mSelectorDrawable.getWidth():mSelectorDrawable.getHeight()) * openPercent);
				if (mMode == SlidingMenu.LEFT) {
					right = content.getLeft();
					left = right - offset;
					canvas.clipRect(left, 0, right, getHeight());
					canvas.drawBitmap(mSelectorDrawable, left, getSelectorTop(), null);		
				} else if (mMode == SlidingMenu.RIGHT) {
					left = content.getRight();
					right = left + offset;
					canvas.clipRect(left, 0, right, getHeight());
					canvas.drawBitmap(mSelectorDrawable, right - mSelectorDrawable.getWidth(), getSelectorTop(), null);
				} else if (mMode == SlidingMenu.TOP) {
                    right = content.getTop();
                    left = right - offset;
                    canvas.clipRect(0, left, getWidth(), right);
                    canvas.drawBitmap(mSelectorDrawable, getSelectorLeft(), left, null);     
                } else if (mMode == SlidingMenu.BOTTOM) {
                    left = content.getBottom();
                    right = left + offset;
                    canvas.clipRect(0, left, getWidth(), right);
                    canvas.drawBitmap(mSelectorDrawable, getSelectorLeft(), right - mSelectorDrawable.getHeight(), null);
                }
				canvas.restore();
			}
		}
	}
	
	public void setSelectorEnabled(boolean b) {
		mSelectorEnabled = b;
	}

	public void setSelectedView(View v) {
		if (mSelectedView != null) {
			mSelectedView.setTag(R.id.selected_view, null);
			mSelectedView = null;
		}
		if (v != null && v.getParent() != null) {
			mSelectedView = v;
			mSelectedView.setTag(R.id.selected_view, TAG+"SelectedView");
			invalidate();
		}
	}

	private int getSelectorTop() {
		int y = mSelectedView.getTop();
		y += (mSelectedView.getHeight() - mSelectorDrawable.getHeight()) / 2;
		return y;
	}
	
	private int getSelectorLeft() {
        int x = mSelectedView.getLeft();
        x += (mSelectedView.getWidth() - mSelectorDrawable.getWidth()) / 2;
        return x;
    }

	public void setSelectorBitmap(Bitmap b) {
		mSelectorDrawable = b;
		refreshDrawableState();
	}

}
