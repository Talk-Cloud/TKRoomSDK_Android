package cn.talkcloud.talksdkdemo;

/**
 * Created by Administrator on 2018/1/30.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 自定义TextView，文本内容自动调整字体大小以适应TextView的大小
 * @author yzp
 */
@SuppressLint("AppCompatCustomView")
public class AutoFitTextView extends TextView {
    private Paint mTextPaint;
    private float mTextSize;

    public AutoFitTextView(Context context) {
        super(context);
        initialise();
    }

    public AutoFitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Re size the font so the specified text fits in the text box assuming the
     * text box is the specified width.
     *
     * @param text
     * @param textViewHeight
     */
//    private void refitText(String text, int textViewHeight) {
//        if (text == null || textViewHeight <= 0)
//            return;
//        mTextPaint = new Paint();
//        mTextPaint.set(this.getPaint());
//        int availableTextViewHeight = getHeight() - getPaddingBottom() - getPaddingTop();
//        float[] charsWidthArr = new float[text.length()];
//        Rect boundsRect = new Rect();
//        mTextPaint.getTextBounds(text, 0, text.length(), boundsRect);
//        int textHeight = boundsRect.height();
//        mTextSize = getTextSize();
//        while (textHeight > availableTextViewHeight) {
//            mTextSize -= 1;
//            mTextPaint.setTextSize(mTextSize);
//            textHeight = boundsRect.height();
//        }
//        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
//    }
    private float mMaxTextSize; // 获取当前所设置文字大小作为最大文字大小
    private float mMinTextSize = 4;    //最小的字体大小
    private void refitText(String textString, int height) {
        initialise();
        if (height > 0) {
            int availableHeight = height - this.getPaddingTop() - this.getPaddingBottom();   //减去边距为字体的实际高度
            float trySize = mMaxTextSize;
            mTextPaint.setTextSize(trySize);
            if (mTextPaint.descent()-mTextPaint.ascent() > availableHeight) {   //测量的字体高度过大，不断地缩放
                trySize -= 1;  //字体不断地减小来适应
                if (trySize <= mMinTextSize) {
                    trySize = mMinTextSize;  //最小为这个
//                    break;
                }
                mTextPaint.setTextSize(trySize);
            }
            setTextSize(px2sp(getContext(), trySize));
        }
    }

    private void initialise() {
        mTextPaint = new TextPaint();
        mTextPaint.set(this.getPaint());
        //默认的大小是设置的大小，如果撑不下了 就改变
        mMaxTextSize = this.getTextSize();
    }

    public static float px2sp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (pxValue / fontScale);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        refitText(this.getText().toString(), this.getHeight());
    }
    //大小改变的时候
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (h != oldh) {
            refitText(this.getText().toString(), h);
        }
    }
}
