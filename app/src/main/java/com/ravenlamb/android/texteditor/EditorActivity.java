package com.ravenlamb.android.texteditor;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * have multiple ArrayAdapter, or multiple style files,
 *
 * with/without line number
 * with/without wrap
 * binary
 */

public class EditorActivity extends ActionBarActivity {

    public final static String FILESTR="com.ravenlamb.android.texteditor.EditorActivity.FILESTR";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Intent intent =getIntent();
        String initFileStr=intent.getStringExtra(FILESTR);
    }

    /**
     *
     */
    private void setArrayAdapter(){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class BinaryFileListAdapter extends ArrayAdapter<String>{

        public BinaryFileListAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
            super(context, resource, textViewResourceId, objects);
        }
    }

    class TextFileListAdapter extends ArrayAdapter<String>{//just display line numbers all the time

        public TextFileListAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
            super(context, resource, textViewResourceId, objects);
        }
    }

}
