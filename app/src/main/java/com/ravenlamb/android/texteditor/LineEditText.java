package com.ravenlamb.android.texteditor;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by kl on 8/31/2015.
 */
public class LineEditText extends EditText {
    public static final String TAG=LineEditText.class.getName();

    public int lineNum;
    public int selectionStart;
    public int selectionEnd;
    protected OnInputConnectionInteraction onInputConnectionInteraction;

    public LineEditText(Context context) {
        super(context);
    }

    public LineEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LineEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context){
        onInputConnectionInteraction=(OnInputConnectionInteraction) context;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        //// TODO: 8/31/2015 don't know what to do yet
        selectionStart=selStart;
        selectionEnd=selEnd;
        if(selStart==0 && selEnd==0){
            Log.d(TAG,"zero zero" );
        }
    }

    private  class EditLineInputConnection extends InputConnectionWrapper {
        public EditLineInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            return super.sendKeyEvent(event);
        }

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

    public interface OnInputConnectionInteraction{
        public void backspaceAtPositionZero(int lineNum);
    }


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
