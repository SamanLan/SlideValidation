# SlideValidation
滑动验证码

#### 一大早起床就看到推送一篇文章，关于仿斗鱼web端的滑动验证码，看了一下实现，挺有趣的，便自己顺着思路撸一遍，改了一点实现和动画什么的，顺带巩固一下绘制的代码。


> 这里也贴一下原作者文章链接:http://qingmang.me/articles/-4771769944547152798

看得喜欢关系点个star。欢迎关注[我的博客](http://www.jianshu.com/users/25018a1e0b12/)

先看一下效果图
![效果图](http://p1.bpimg.com/567571/fe95bf58e635042d.gif)

效果还是不错的。


#### 用法，SlideValidationView 是继承自ImageView，所以验证码图片直接set就行。
```
SeekBar seekBar;
SlideValidationView slideValidationView;
slideValidationView = (SlideValidationView) findViewById(R.id.yzm);
// 设置监听器，判断验证成功失败时回调
slideValidationView.setListener(new SlideListener() {    
    @Override    
    public void onSuccess() {        
        Toast.makeText(MainActivity.this, "验证成功", Toast.LENGTH_SHORT).show();
        seekBar.setProgress(0);
    }
    @Override
    public void onFail() {
        Toast.makeText(MainActivity.this, "验证失败", Toast.LENGTH_SHORT).show();
        seekBar.setProgress(0);
    }
});
seekBar = (SeekBar) findViewById(R.id.seekBar);
seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // 更新验证滑块的位置
        slideValidationView.setOffsetX(progress);
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // 进行验证码的判断
        slideValidationView.deal();
    }
});
```


下面说一下实现，也可以选择去看代码，代码里面注释应该很全了。看得喜欢关系点个star。
第一步：画拼图的path，上下左右四个半圆随机凹凸
```
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
```
![绘制拼图path](http://p1.bpimg.com/567571/ebd663cff31924c5.png)



第二步：绘制阴影（设置画笔的setMaskFilter，应该要为这个view关闭硬件加速，否则阴影没作用）
```
// 单独为这个view关闭硬件加速
setLayerType(LAYER_TYPE_SOFTWARE, null);
```
阴影

```
// 验证块的阴影画笔
Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
mPaint.setColor(0x99000000);
// 设置画笔遮罩滤镜mPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.SOLID));
```
![绘制阴影](http://p1.bpimg.com/567571/c646fea1f8b27cc1.png)

第三步：绘制滑块
因为上面我们得到了拼图的path，我们就创建一个bitmap，在里面分别绘制验证码原图和这个path，通过setXfermode（不了解的可以去搜搜），取得他们的交集，即为我们的滑块，由此我们通过一个变量来控制滑块的绘制x轴就行了
```
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
```

第四步

绘制滑块的阴影，这里面有个api我也是第一次接触，bitmap.extractAlpha()拿到该bitmap的图片大小等信息，但只有透明度没有颜色，返回一张新的bitmap。我们通过设置画笔的阴影来绘制新的bitmap即可绘制出滑块的阴影
```
// extractAlpha拿到原bitmap的区域，只有透明度Bitmap
 mMaskShadowBitmap = mMaskBitmap.extractAlpha();
```
![绘制滑块和阴影](http://p1.bpimg.com/567571/940cc52260b2e731.png)

#### 一些方法

|方法名|用处|
|---|---|
|setOffsetX(float howMuch)|设置滑块移动距离(@param howMuch 0-100内数字，表示百分比)|
|restore()|重置验证区域位置（重新生成拼图path）|
|deal()|判断是否成功|
|setListener(SlideListener listener)|设置监听器|


详细请去看代码，代码里面注释应该很全了。看得喜欢关系点个star。欢迎关注[我的博客](http://www.jianshu.com/users/25018a1e0b12/)
