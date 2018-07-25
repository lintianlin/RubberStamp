/*
  Copyright (C) 2017 Vinay Gaba

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.vinaygaba.rubberstamp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import static com.vinaygaba.rubberstamp.RubberStampPosition.CUSTOM;
import static com.vinaygaba.rubberstamp.RubberStampPosition.TILE;


public class RubberStamp {

    private Context mContext;
    private static final int BACKGROUND_MARGIN = 10;

    public RubberStamp(@NonNull Context context) {
        mContext = context;
    }

    /**
     * 添加水印
     *
     * @param config
     * @return
     */
    public Bitmap addStamp(@NonNull RubberStampConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("The config passed to this method should never" +
                    "be null");
        }
        //获取需要添加水印的原始图片
        Bitmap baseBitmap = getBaseBitmap(config);
        if (baseBitmap == null) {
            return baseBitmap;
        }
        //获取原始图片的宽度和高度
        int baseBitmapWidth = baseBitmap.getWidth();
        int baseBitmapHeight = baseBitmap.getHeight();

        //通过原始图片创建一个副本图片
        Bitmap result = Bitmap.createBitmap(baseBitmapWidth, baseBitmapHeight, baseBitmap.getConfig());
        //创建画布
        Canvas canvas = new Canvas(result);
        //将副本图片画到画布上
        canvas.drawBitmap(baseBitmap, 0, 0, null);

        //判断需要添加文字水印还是图片水印
        if (!TextUtils.isEmpty(config.getRubberStampString())) {
            //添加文字水印的方法
            addTextToBitmap(config, canvas, baseBitmapWidth, baseBitmapHeight);
        } else if (config.getRubberStampBitmap() != null) {
            //添加图片水印的方法
            addBitmapToBitmap(config.getRubberStampBitmap(), config, canvas,
                    baseBitmapWidth, baseBitmapHeight);
        }
        return result;
    }

    @Nullable
    private Bitmap getBaseBitmap(@NonNull RubberStampConfig config) {
        Bitmap baseBitmap = config.getBaseBitmap();
        @DrawableRes int drawable = config.getBaseDrawable();

        if (baseBitmap == null) {
            baseBitmap = BitmapFactory.decodeResource(mContext.getResources(), drawable);
            if (baseBitmap == null) return null;
        }
        return baseBitmap;
    }

    /**
     * 添加文字水印到图片上
     *
     * @param config
     * @param canvas
     * @param baseBitmapWidth
     * @param baseBitmapHeight
     */
    private void addTextToBitmap(@NonNull RubberStampConfig config,
                                 @NonNull Canvas canvas,
                                 int baseBitmapWidth,
                                 int baseBitmapHeight) {
        //创建一个书写文字的矩形区域
        Rect bounds = new Rect();

        //创建一个画笔
        Paint paint = new Paint();
        //设置抗锯齿
        paint.setAntiAlias(true);
        //设置下划线不显示
        paint.setUnderlineText(false);
        //设置文字大小
        paint.setTextSize(config.getTextSize());
        //获取字体路径
        String typeFacePath = config.getTypeFacePath();
        // 如果字体路径不为空，则设置字体路径
        if (!TextUtils.isEmpty(typeFacePath)) {
            Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), typeFacePath);
            paint.setTypeface(typeface);
        }
        //获取文字着色器
        Shader shader = config.getTextShader();
        //如果文字颜色不为空，则设置文字颜色
        if (shader != null) {
            paint.setShader(shader);
        }

        //设置文字阴影
        if (config.getTextShadowXOffset() != 0 || config.getTextShadowYOffset() != 0
                || config.getTextShadowBlurRadius() != 0) {
            //如果阴影设置不为空，则设置文字阴影
            paint.setShadowLayer(config.getTextShadowBlurRadius(),
                    config.getTextShadowXOffset(),
                    config.getTextShadowYOffset(),
                    config.getTextShadowColor());
        }
        //获取水印文字
        String rubberStampString = config.getRubberStampString();
        //将文字写到创建的矩形区域中
        paint.getTextBounds(rubberStampString, 0, rubberStampString.length(), bounds);
        //获取矩形区域的宽度
        int rubberStampWidth = bounds.width();
        //测量文字的宽度
        float rubberStampMeasuredWidth = paint.measureText(rubberStampString);
        //获取矩形区域的高度
        int rubberStampHeight = bounds.height();
        //获取设置的X轴位置
        int positionX = config.getPositionX();
        //获取设置的Y轴位置
        int positionY = config.getPositionY();
        //如果水印位置不是设置的自定义
        if (config.getRubberStampPosition() != CUSTOM) {
            // If the specified RubberStampPosition is not CUSTOM, use calculates its x & y
            // co-ordinates.
            Pair<Integer, Integer> pair = PositionCalculator
                    .getCoordinates(config.getRubberStampPosition(),
                            baseBitmapWidth, baseBitmapHeight,
                            rubberStampWidth, rubberStampHeight);
            positionX = pair.first;
            positionY = pair.second;
        }

        //设置margin值
        positionX += config.getXMargin();
        positionY += config.getYMargin();

        //获取旋转角度度
        float rotation = config.getRotation();
        //如果旋转角度存在则设置水印的旋转角度
        if (rotation != 0.0f) {
            canvas.rotate(rotation, positionX + bounds.exactCenterX(),
                    positionY - bounds.exactCenterY());
        }

        //设置文字颜色
        paint.setColor(config.getTextColor());
        //获取透明度
        int alpha = config.getAplha();
        //如果透明度设置存在，则设置水印的透明度
        if (alpha >= 0 && alpha <= 255) {
            paint.setAlpha(alpha);
        }
        //如果设置的不是全屏模式
        if (config.getRubberStampPosition() != TILE) {
            // The textBackgroundColor is only used if the specified RubberStampPosition is not TILE
            // This is because the background is actually a rectangle whose bounds are calcualted
            // below. In the case of TILE, we make use of a bitmap shader and there was no easy way
            // to draw the background rectangle for each tiled rubberstamp.
            int backgroundColor = config.getTextBackgroundColor();
            if (backgroundColor != 0) {
                Paint backgroundPaint = new Paint();
                backgroundPaint.setColor(backgroundColor);
                //将矩形画到画布上
                canvas.drawRect(positionX - BACKGROUND_MARGIN,
                        positionY - bounds.height() - paint.getFontMetrics().descent - BACKGROUND_MARGIN,
                        (positionX + rubberStampMeasuredWidth + config.getTextShadowXOffset() + BACKGROUND_MARGIN),
                        positionY + config.getTextShadowYOffset() + paint.getFontMetrics().descent + BACKGROUND_MARGIN,
                        backgroundPaint);
            }
            //将文字画到画布上
            canvas.drawText(rubberStampString, positionX, positionY, paint);
        } else {//全屏模式
            // If the specified RubberStampPosition is TILE, it tiles the rubberstamp across
            // the bitmap. In order to generate a tiled bitamp, it uses a bitmap shader.
            Bitmap textImage = Bitmap.createBitmap((int) rubberStampMeasuredWidth,
                    rubberStampHeight,
                    Bitmap.Config.ARGB_8888);
            Canvas textCanvas = new Canvas(textImage);
            textCanvas.drawText(config.getRubberStampString(), 0, rubberStampHeight, paint);
            paint.setShader(new BitmapShader(textImage,
                    Shader.TileMode.REPEAT,
                    Shader.TileMode.REPEAT));
            Rect bitmapShaderRect = canvas.getClipBounds();
            canvas.drawRect(bitmapShaderRect, paint);
        }
    }

    /**
     * Method to add a bitmap RubberStamp to a canvas based on the provided configuration
     *
     * @param rubberStampBitmap The bitmap which will be used as the RubberStamp
     * @param config            The RubberStampConfig that specifies how the RubberStamp should look
     * @param canvas            The canvas on top of which the RubberStamp needs to be drawn
     * @param baseBitmapWidth   The width of the base bitmap
     * @param baseBitmapHeight  The height of the base bitmap
     */
    private void addBitmapToBitmap(@NonNull Bitmap rubberStampBitmap,
                                   @NonNull RubberStampConfig config,
                                   @NonNull Canvas canvas,
                                   int baseBitmapWidth,
                                   int baseBitmapHeight) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setUnderlineText(false);

        int alpha = config.getAplha();
        // Add alpha to the rubberstamp if its within range or it uses the default value.
        if (alpha >= 0 && alpha <= 255) {
            paint.setAlpha(alpha);
        }

        int positionX = config.getPositionX();
        int positionY = config.getPositionY();
        RubberStampPosition rubberStampPosition = config.getRubberStampPosition();
        if (rubberStampPosition != CUSTOM) {
            // If the specified RubberStampPosition is not CUSTOM, use calculates its x & y
            // co-ordinates.
            Pair<Integer, Integer> pair =
                    PositionCalculator.getCoordinates(rubberStampPosition,
                            baseBitmapWidth, baseBitmapHeight,
                            rubberStampBitmap.getWidth(), rubberStampBitmap.getHeight());
            positionX = pair.first;
            positionY = pair.second - rubberStampBitmap.getHeight();
        }

        // Add the margin to this position if it was passed to the config.
        positionX += config.getXMargin();
        positionY += config.getYMargin();

        float rotation = config.getRotation();
        if (rotation != 0.0f) {
            // Add rotation if its present in the config.
            canvas.rotate(rotation, positionX + (rubberStampBitmap.getWidth() / 2),
                    positionY + (rubberStampBitmap.getHeight() / 2));
        }

        if (rubberStampPosition != TILE) {
            canvas.drawBitmap(rubberStampBitmap, positionX, positionY, paint);
        } else {
            // If the specified RubberStampPosition is TILE, it tiles the rubberstamp across
            // the bitmap. In order to generate a tiled bitamp, it uses a bitmap shader.
            paint.setShader(new BitmapShader(rubberStampBitmap,
                    Shader.TileMode.REPEAT,
                    Shader.TileMode.REPEAT));
            Rect bitmapShaderRect = canvas.getClipBounds();
            canvas.drawRect(bitmapShaderRect, paint);
        }
    }
}
