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

import android.util.Pair;

public class PositionCalculator {

    /**
     * 通过传入的参数计算水印坐标的公共方法
     * @param location    位置设置参数
     * @param bitmapWidth  图片宽度
     * @param bitmapHeight  图片高度
     * @param rubberstampWidth  水印宽度
     * @param rubberstampHeight  水印高度
     * @return
     */
    public static Pair<Integer, Integer> getCoordinates(RubberStampPosition location,
                                                        int bitmapWidth, int bitmapHeight,
                                                        int rubberstampWidth, int rubberstampHeight) {
        switch(location){
            case TOP_LEFT://左上方
                return new Pair<>(0, rubberstampHeight);

            case TOP_CENTER:
                return new Pair<>((bitmapWidth / 2) - (rubberstampWidth / 2),
                        rubberstampHeight);
            case TOP_RIGHT:
                return new Pair<>(bitmapWidth - rubberstampWidth, rubberstampHeight);

            case CENTER_LEFT:
                return new Pair<>(0, (bitmapHeight / 2) + (rubberstampHeight / 2));

            case CENTER:
                return new Pair<>((bitmapWidth / 2) - (rubberstampWidth / 2),
                        (bitmapHeight / 2) + (rubberstampHeight / 2));
            case CENTER_RIGHT:
                return new Pair<>(bitmapWidth - rubberstampWidth,
                        (bitmapHeight / 2) + (rubberstampHeight / 2));

            case BOTTOM_LEFT:
                return new Pair<>(0, bitmapHeight);

            case BOTTOM_CENTER:
                return new Pair<>((bitmapWidth / 2) - (rubberstampWidth / 2),
                        bitmapHeight);
            case BOTTOM_RIGHT:
                return new Pair<>(bitmapWidth - rubberstampWidth, bitmapHeight);

            default:
                return new Pair<>((bitmapWidth / 2) - (rubberstampWidth / 2),
                        (bitmapHeight / 2) + (rubberstampHeight / 2));
        }
    }
}
