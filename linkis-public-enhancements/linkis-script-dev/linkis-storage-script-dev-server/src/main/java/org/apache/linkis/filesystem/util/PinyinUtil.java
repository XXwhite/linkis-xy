/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.filesystem.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/** @Description: 拼音工具类 @Author: colourness @Date: 2023-12-18 */
public class PinyinUtil {
  /**
   * 返回拼音首字母
   *
   * @param str 汉字
   * @return 拼音全写
   */
  public static String toPinyinAll(String str) {
    String pinyinName = "";
    HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
    defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
    defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    char[] nameChar = str.toCharArray();
    for (int i = 0; i < nameChar.length; i++) {
      if (nameChar[i] > 128) {
        try {
          // 拼音全写
          pinyinName += PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0];
        } catch (BadHanyuPinyinOutputFormatCombination e) {
          e.printStackTrace();
        }
      } else {
        pinyinName += nameChar[i];
      }
    }
    return pinyinName;
  }

  /**
   * 返回拼音首字母
   *
   * @param str 汉字
   * @return 拼音首字母
   */
  public static String toPinyinSub(String str) {
    String pinyinName = "";
    HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
    defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
    defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    char[] nameChar = str.toCharArray();
    for (int i = 0; i < nameChar.length; i++) {
      if (nameChar[i] > 128) {
        try {
          // 首字母缩写
          pinyinName +=
              PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0].charAt(0);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
          e.printStackTrace();
        }
      } else {
        pinyinName += nameChar[i];
      }
    }
    return pinyinName;
  }
}
