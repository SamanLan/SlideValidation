package com.samanlan.lib_slidevalidation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.Random;

/**
 * 作者(Author)：蓝深铭(LanSaman)
 * 邮箱(E-Mail)：lansaman@163.com
 * 时间(Time)：on 2017/1/4 19:57
 */

public class SlideValidationView extends ImageView {
    public SlideValidationView(Context context) {
        super(context);
        init(context);
    }

    public SlideValidationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.SlideValidationView);
        validationSize = (int) typedArray.getDimension(R.styleable.SlideValidationView_validationSize, 0);
        typedArray.recycle();
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        // 关闭硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    Context mContext;

    /**
     * 控件宽高
     */
    int width = 0;
    int height = 0;

    /**
     * 验证区域宽高大小（正方形）
     */
    int validationSize = 0;

    /**
     * 四边突出（凹入）的半圆的直径
     */
    int circleSize = 0;

    /**
     * 验证块的path
     */
    Path validationPath;

    /**
     * 验证滑块的偏移量
     */
    float offsetX = 0;

    /**
     * 成功时的白光偏移量
     */
    int animaOffsetX = 0;

    /**
     * 验证块的起始坐标
     */
    int startX;
    int startY;

    /**
     * 是否第一次进入
     */
    boolean first = true;

    /**
     * 是否成功
     */
    boolean success = false;

    /**
     * 监听器
     */
    SlideListener mListener;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (success) {
            // 成功就闪过一条白光
            Paint baiguangPaint = new Paint();
            // 线性渐变
            Shader mShader = new LinearGradient(animaOffsetX, 0, animaOffsetX + 80, 0, new int[]{0x00ffffff, 0xffffffff, 0x00ffffff}, null, Shader.TileMode.MIRROR);
            baiguangPaint.setShader(mShader);
            canvas.drawRect(animaOffsetX, 0, animaOffsetX + 80, height, baiguangPaint);
            if (animaOffsetX < width) {
                animaOffsetX += 40;
                invalidate();
            }
        } else {
            // 验证块的阴影画笔
            Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            mPaint.setColor(0x99000000);
            // 设置画笔遮罩滤镜
            mPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.SOLID));
//            mPaint.setShadowLayer(5,2,2,0x99000000);

            // 验证滑块的画笔
            Paint mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

            // 验证滑块的阴影的画笔
            Paint mMaskShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            mMaskShadowPaint.setColor(0x99000000);
            mMaskShadowPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.SOLID));

            if (first) {
                creatValidationPath();
                first = false;
            }
//            canvas.drawCircle(startX, startY, 5, new Paint());
//            canvas.drawCircle(offsetX, startY, 5, new Paint());

            // 画验证块
            canvas.drawPath(validationPath, mPaint);
            // 画验证滑块和阴影
            craeteMask(canvas, mMaskPaint, mMaskShadowPaint);
        }


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        System.out.println("[onSizeChanged]方法：宽度是：" + w + "高度是：" + h);
        width = w;
        height = h;
    }

    /**
     * 创建验证区域path
     */
    private void creatValidationPath() {
        validationPath = new Path();

        if (validationSize == 0) {
            validationSize = width/6;
        }

        circleSize = validationSize / 3;
        startX = new Random().nextInt(width - validationSize * 2 - circleSize * 2 - 10) + circleSize + validationSize + 10;
        startY = new Random().nextInt(height - validationSize - circleSize * 2) + circleSize;

        // 从左上画path到右上
        validationPath.moveTo(startX, startY);
        validationPath.lineTo(startX + circleSize, startY);
        creatRandomArc(validationPath, startX + circleSize, startY, false, 0);
        validationPath.lineTo(startX + validationSize, startY);

        // 从右上画path到右下
        validationPath.lineTo(startX + validationSize, startY + circleSize);
        creatRandomArc(validationPath, startX + validationSize, startY + circleSize, true, 0);
        validationPath.lineTo(startX + validationSize, startY + validationSize);

        // 从右下画path到左下
        validationPath.lineTo(startX + circleSize * 2, startY + validationSize);
        creatRandomArc(validationPath, startX + circleSize, startY + validationSize, false, 1);
        validationPath.lineTo(startX, startY + validationSize);

        // 从左下画path到左上
        validationPath.lineTo(startX, startY + circleSize * 2);
        creatRandomArc(validationPath, startX, startY + circleSize, true, 1);
        validationPath.lineTo(startX, startY);
    }

    /**
     * 验证区域path四条边的半圆弧度
     *
     * @param validationPath 要操作的path
     * @param beginX         弧度的起始x坐标（取弧度的左边坐标，即弧度的两点，位于左边的那个坐标）
     * @param beginY         弧度的起始y坐标（取弧度的上边坐标，即弧度的两点，位于上边的那个坐标）
     * @param isleftRight    是否左右边
     * @param type           右上边为0，左下边为1
     */
    private void creatRandomArc(Path validationPath, int beginX, int beginY, boolean isleftRight, int type) {
        RectF rectF;
        // 是左右边还是上下边
        if (isleftRight) {
            rectF = new RectF(beginX - circleSize / 2, beginY, beginX + circleSize / 2, beginY + circleSize);
        } else {
            rectF = new RectF(beginX, beginY - circleSize / 2, beginX + circleSize, beginY + circleSize / 2);
        }

        // 随机得到是突出还是凹入半圆，针对角度问题，用type来解决
        if (new Random().nextInt(10) > 5) {
            // 突出半圆
            if (isleftRight) {
                validationPath.arcTo(rectF, -90 + type * 180, 180);
            } else {
                validationPath.arcTo(rectF, -180 + type * 180, 180);
            }
        } else {
            // 凹入半圆
            if (isleftRight) {
                validationPath.arcTo(rectF, -90 + type * 180, -180);
            } else {
                validationPath.arcTo(rectF, -180 + type * 180, -180);
            }
        }
    }

    /**
     * 画验证滑块
     *
     * @param canvas           画布
     * @param paint            验证滑块的bitmap的画笔
     * @param mMaskShadowPaint 验证滑块的阴影的画笔
     */
    private void craeteMask(Canvas canvas, Paint paint, Paint mMaskShadowPaint) {
        Bitmap mMaskBitmap = getMaskBitmap(((BitmapDrawable) getDrawable()).getBitmap(), validationPath, paint);
        // 滑块阴影
        // extractAlpha拿到原bitmap的区域，只有透明度
        Bitmap mMaskShadowBitmap = mMaskBitmap.extractAlpha();
        canvas.drawBitmap(mMaskShadowBitmap, offsetX - startX + circleSize / 2, 0, mMaskShadowPaint);
        canvas.drawBitmap(mMaskBitmap, offsetX - startX + circleSize / 2, 0, null);
    }

    /**
     * 生成验证滑块bitmap
     *
     * @param mBitmap    整张图片
     * @param mask       验证滑块的path
     * @param mMaskPaint 画笔
     * @return 验证滑块bitmap
     */
    private Bitmap getMaskBitmap(Bitmap mBitmap, Path mask, Paint mMaskPaint) {
        // 以控件宽高 create一块bitmap
        Bitmap tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 把创建的bitmap作为画板
        Canvas mCanvas = new Canvas(tempBitmap);
        // 抗锯齿
        mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        // 绘制用于遮罩的圆形
        mCanvas.drawPath(mask, mMaskPaint);
        // 设置遮罩模式(图像混合模式)
        mMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        // 考虑到scaleType等因素，要用Matrix对Bitmap进行缩放
        mCanvas.drawBitmap(mBitmap, getImageMatrix(), mMaskPaint);
        mMaskPaint.setXfermode(null);
        return tempBitmap;
    }

    /**
     * 设置滑块移动距离
     *
     * @param howMuch 0-100内数字，表示百分比
     */
    public void setOffsetX(float howMuch) {
        offsetX = (width - validationSize - circleSize) / 100f * howMuch;
        invalidate();
    }

    /**
     * 重置验证区域位置（重新生成）
     */
    public void restore() {
        creatValidationPath();
        offsetX = 0;
        animaOffsetX = 0;
        success = false;
        invalidate();
    }

    /**
     * 判断是否成功验证
     */
    public void deal() {
        if (offsetX + circleSize / 2 <= startX + dp2px(mContext, 5) && offsetX + circleSize / 2 >= startX - dp2px(mContext, 5)) {
            if (mListener != null) {
                mListener.onSuccess();
            }
            success = true;
            invalidate();
        } else {
            if (mListener != null) {
                mListener.onFail();
            }
            setOffsetX(0);
        }
    }

    /**
     * 设置成功失败监听器
     *
     * @param listener 监听器
     */
    public void setListener(SlideListener listener) {
        mListener = listener;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5F);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5F);
    }
}
