package com.ravenlamb.android.texteditor;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by kl on 8/31/2015.
 */
public class LineEditText extends EditText {
    public LineEditText(Context context) {
        super(context);
    }

    public LineEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LineEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
    }


}
