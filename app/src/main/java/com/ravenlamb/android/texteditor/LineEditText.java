package com.ravenlamb.android.texteditor;

import android.content.Context;
import android.graphics.Canvas;
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
        //// TODO: 8/31/2015 don't know what to do yet
    }

//http://stackoverflow.com/questions/4886858/android-edittext-deletebackspace-key-event
//    @Override
//    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
//        return new ZanyInputConnection(super.onCreateInputConnection(outAttrs),
//                true);
//    }
//
//    private class ZanyInputConnection extends InputConnectionWrapper {
//
//        public ZanyInputConnection(InputConnection target, boolean mutable) {
//            super(target, mutable);
//        }
//
//        @Override
//        public boolean sendKeyEvent(KeyEvent event) {
//            if (event.getAction() == KeyEvent.ACTION_DOWN
//                    && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
//                ZanyEditText.this.setRandomBackgroundColor();
//                // Un-comment if you wish to cancel the backspace:
//                // return false;
//            }
//            return super.sendKeyEvent(event);
//        }
//
//    }




//    @Override
//    protected void onDraw(Canvas canvas) {
//        // Get the text to print
////        final float textSize = super.getTextSize();
//        final String text = super.getText().toString();
//
////        canvas.drawText(text, getPaddingStart(), floaty, getPaint());
//        super.onDraw(canvas);
//    }
}
