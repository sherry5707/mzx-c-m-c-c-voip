package com.kinstalk.her.cmccmode.utils;

import android.util.Log;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.apache.log4j.pattern.LogEvent;

import java.util.ArrayList;

public class PinYinTool {
    private static final String TAG = "PinYinTool";

    public static char isPinYinChar(char c) {
        Log.i(TAG, "isPinYinChar: " + c);
        try {
            //char->string->int->string
            String num = String.valueOf(Integer.parseInt(String.valueOf(c)));
            return NumberToChineseUtil.getChinesePinYin(num).charAt(0);
        } catch (NumberFormatException e) {
            Log.i(TAG, "isPinYinChar,not number:" + c);
            return c;
        }
    }

    private static String[] textForTest= new String[]{
            "仇的行",
            "解奔和"
    };

    public static void testPinYin(){
        for(int i=0;i<textForTest.length;i++){
            Log.e(TAG, "testPinYin: "+i+",result:"+getPinyinArrayFromString(textForTest[i]));
        }
    }

    /**
     * 将字符串转为拼音，因为可能是多音字，那么得出的结果可能有多个
     *
     * @param text
     * @return
     */
    public static ArrayList<String> getPinyinArrayFromString(String text) {
        //最后得到的结果
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> tempResult = new ArrayList();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        for (char c : text.toCharArray()) {
            try {
                //获得一个字符的拼音数组
                String[] charPinYinArray = PinyinHelper.toHanyuPinyinStringArray(isPinYinChar(c), format);
                //组合的结果，比如result里面有2个拼音string了，再拼接一个有n个拼音的字符，得到的结果是2n
                int resultCount = charPinYinArray.length * result.size();
                Log.i(TAG, "getPinyinArrayFromString: resultsize:" + result.size() + ",multipinyinlength:" + charPinYinArray.length);
                //为了标记下标
                int k = 0;
                //因为size会随着内部的add和set不断变化，所以必须在for循环外部初始化好
                int resultSize = result.size();
                tempResult.clear();
                tempResult = (ArrayList<String>) result.clone();
                for (int i = 0; i <= resultSize; i++) {
                    //处理i=0的情况,刚开始的时候i=0，size=0，可能get不到值就设置onePinyin是""；如果i=size了就break
                    boolean firstPinyin = false;
                    if (i == 0 && resultSize == 0) {
                        firstPinyin = true;
                    } else if (i == resultSize) {
                        break;
                    }
                    //因为拼接后会直接set，所以需要把上一次的结果记下来，防止拼接用的是set过后的
                    //比如wu拼接多音字行，会设置0的位置结果为wuhang,第二次应该也是基于wu去拼接而不是wuhang
                    String lastPinYin = firstPinyin ? "" : tempResult.get(i);
                    Log.e(TAG, "getPinyinArrayFromString: lastPinYin:" + lastPinYin);
                    for (int j = 0; j < charPinYinArray.length; j++) {
                        Log.i(TAG, "getPinyinArrayFromString: multipinyin[" + j + "]:" + charPinYinArray[j]);
                        String onePinYin;
                        onePinYin = lastPinYin + charPinYinArray[j];
                        Log.i(TAG, "getPinyinArrayFromString: onePinYIn:"+onePinYin);
                        if (k < resultSize) {
                            result.set(k, onePinYin);
                        } else {
                            result.add(onePinYin);
                        }
                        k++;
                    }
                }
            } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                badHanyuPinyinOutputFormatCombination.printStackTrace();
            }
        }
        return result;
    }
}
