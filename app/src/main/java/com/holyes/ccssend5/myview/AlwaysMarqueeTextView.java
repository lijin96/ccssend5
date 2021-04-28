package com.holyes.ccssend5.myview;


import android.content.Context;
import android.widget.TextView;


/**
 * @ClassName: AlwaysMarqueeTextView
 * @Description: 跑马灯
 * @Author: lijin
 * @Date: 2021/3/10 9:51
 */
public class AlwaysMarqueeTextView extends TextView {

    public AlwaysMarqueeTextView(Context context) {
        super(context);
    }

    @Override
    public boolean isFocused()
    {
        return true;
    }

}
