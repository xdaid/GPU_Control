package com.xdaid.gpucontrol;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

//-------------------------------------------------------------------------------
// ValuePicker: UI to select a value from an array of values.
// Author: xdaid
//
// Get notified by overriding valueChanged method from activity:
//
// public OnValueChangedListener tListener = new OnValueChangedListener()
// {
//     @Override
//     public void valueChanged(int value) { newValue = value; }
// };
//-------------------------------------------------------------------------------

public class ValuePicker extends View
{
    public interface OnValueChangedListener { void valueChanged(ValuePicker sender, int value); }

    private OnValueChangedListener mListener;
    private String[] mValues;
    private String mText;
    private int mValueIndex;
    private int mState;

    private int mPaintX;
    private int mPaintY;
    private int mPaintWidth;
    private int mPaintHeight;
    private Paint mPaint;
    private Paint mDecorPaint;
    private Paint mTextPaint;

    private Timer tm_btn;
    private TimerTask tt_dec;
    private TimerTask tt_inc;

    private int rround = dipToPix(3);
    private int tsize  = dipToPix(16);
    private int tcolor = 0xFF000000;


    // Converts dips to pixels
	public int dipToPix(int value)
	{
		float scale = getResources().getDisplayMetrics().density;
		return (int)(value * scale);
	}


	public ValuePicker(Context c, OnValueChangedListener listener, String[] values, String initialValue)
    {
    	super(c);
    	mListener = listener;
        mValues = values;
        setValue(initialValue);
        Initialize();
    }


	public ValuePicker(Context c, String[] values, String initialValue)
    {
    	super(c);
    	mListener = new OnValueChangedListener()
    	{
			@Override
			public void valueChanged(ValuePicker sender, int value) { }
		};
        mValues = values;
        setValue(initialValue);
        Initialize();
    }


	public ValuePicker(Context c, AttributeSet attr)
    {
    	super(c, attr);
    	mListener = new OnValueChangedListener()
    	{
			@Override
			public void valueChanged(ValuePicker sender, int value) { }
		};
        mValues = new String[0];
        mValueIndex = 0;
        Initialize();
    }


	public ValuePicker(Context c)
    {
    	super(c);
    	mListener = new OnValueChangedListener()
    	{
			@Override
			public void valueChanged(ValuePicker sender, int value) { }
		};
        mValues = new String[0];
        mValueIndex = 0;
        Initialize();
    }


	private void Initialize()
    {
		mState = 0;

        mPaintX = 10;
        mPaintY = 12;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);

        mDecorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDecorPaint.setStyle(Paint.Style.STROKE);
        mDecorPaint.setStrokeWidth(0);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTypeface(Typeface.DEFAULT);
        mTextPaint.setTextAlign(Align.CENTER);
    }


	public void setOnValueChangedListener(OnValueChangedListener sListener)
	{
		mListener = sListener;
	}


    public String[] getValues()
    {
    	return mValues;
    }


    public void setValues(String[] values)
    {
    	mValues = values;
    }


    public String getValue()
    {
    	return mValues[mValueIndex];
    }


	public void setValue(String value)
    {
		for (int i = 0; i < mValues.length; i++)
        {
			if (mValues[i].equals(value))
			{
				mValueIndex = i;
				invalidate();
				break;
        	}
		}
    }


    public void setTextColor(int textColor)
    {
    	tcolor = textColor;
		invalidate();
    }


    @SuppressLint("DrawAllocation")
	@Override 
    protected void onDraw(Canvas canvas)
    {
    	mPaintWidth = this.getWidth() - mPaintX * 2 - 1;
        mPaintHeight = this.getHeight() - mPaintY * 2 - 1;

        mText = mValues[mValueIndex];

        mPaint.setColor(0x80A0A0A0);
        canvas.drawRoundRect(new RectF(mPaintX, mPaintY, mPaintX + mPaintWidth, mPaintY + mPaintHeight), rround, rround, mPaint);
        if (mState == 1)
        {
            mPaint.setColor(0xFF00B0F0);
            canvas.drawRoundRect(new RectF(mPaintX, mPaintY, mPaintX + mPaintWidth/4, mPaintY + mPaintHeight), rround, rround, mPaint);
        }
        if (mState == 2)
        {
            mPaint.setColor(0xFF00B0F0);
            canvas.drawRoundRect(new RectF(mPaintX + mPaintWidth/4 * 3, mPaintY, mPaintX + mPaintWidth, mPaintY + mPaintHeight), rround, rround, mPaint);
        }
        mPaint.setColor(0x80F0F0F0);
        canvas.drawRect(new RectF(mPaintX + mPaintWidth/4, mPaintY + 1, mPaintX + mPaintWidth - mPaintWidth/4, mPaintY - 1 + mPaintHeight), mPaint);

        mTextPaint.setTextSize(tsize);
        mTextPaint.setColor(tcolor);
        canvas.drawText(mText, this.getWidth() / 2, this.getHeight() / 2 + mTextPaint.getTextSize() / 3, mTextPaint);
        canvas.drawText("-", mPaintX + mPaintWidth/8, this.getHeight() / 2 + mTextPaint.getTextSize() / 3 - 2, mTextPaint);
        canvas.drawText("+", mPaintX + mPaintWidth - mPaintWidth/8, this.getHeight() / 2 + mTextPaint.getTextSize() / 3 + 1, mTextPaint);
 
        mDecorPaint.setColor(0xFF404040);
        canvas.drawRoundRect(new RectF(mPaintX - 2, mPaintY - 2, mPaintX + mPaintWidth + 2, mPaintY + mPaintHeight + 2), rround, rround, mDecorPaint);
    }


    public void activateTimer(int whichTask)
    {
    	tt_dec = new TimerTask()
        {
        	@Override
        	public void run()
        	{
        		if (mValueIndex > 0) { mValueIndex--; mState = 1; postInvalidate(); }
        		mListener.valueChanged(ValuePicker.this, mValueIndex);
        	}
        };
        tt_inc = new TimerTask()
        {
        	@Override
        	public void run()
        	{
        		if (mValueIndex < mValues.length - 1) { mValueIndex++; mState = 2; postInvalidate(); }
        		mListener.valueChanged(ValuePicker.this, mValueIndex);
        	}
        };
        tm_btn = new Timer();
    	tm_btn.schedule(whichTask == 1 ? tt_dec : tt_inc, 0, 200);
    }


    public void deactivateTimer()
    {
    	if (tt_dec != null) { tt_dec.cancel(); }
    	if (tt_inc != null) { tt_inc.cancel(); }
    	if (tm_btn != null) { tm_btn.cancel(); tm_btn.purge(); }
    }


	@Override
    public boolean onTouchEvent(MotionEvent event)
    {
    	int  x = (int) event.getX();
        int  y = (int) event.getY();
        int  u = ValuePicker.this.getWidth() / 4;
        Rect r = new Rect();
        ValuePicker.this.getDrawingRect(r);
    	if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
         	if (r.contains(x, y))
        	{
         		if ( x >= u * 3) { activateTimer(2); return true; }
         		else if (x <= u) { activateTimer(1); return true; }
        	}
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE)
        {
         	if (r.contains(x, y))
        	{
         		if ( x >= u * 3) { return true; }
         		else if (x <= u) { return true; }
        	}
    	}
        if (event.getAction() == MotionEvent.ACTION_UP) { }
        deactivateTimer();
        mState = 0;
    	invalidate();
        return true;
    }
}