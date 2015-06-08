package com.ravenlamb.android.texteditor;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends ListActivity {


    //contains the absolute paths of the directories, but only display file name
    ArrayList<String> initialList;
    ArrayList<String> currentList;
    ArrayList<String> historyList;//keep the directory path it is displaying

    boolean isInitialList=true;

    //New, favorites, default directories, recent directories, recent files

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialList=new ArrayList<String>();
        initialList.add("New");
        initialList.add("Favorites");
        initialList.add("Browse Directories");
        initialList.add("Recent Directories");
        initialList.add("Recent Files");

        currentList=initialList;
        historyList=new ArrayList<String>();
    }

    private void onBackButtonClick(){
        //todo, if history is empty, isInitialList is true
    }



    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //super.onListItemClick(l, v, position, id);
        String item=currentList.get(position);
        if(isInitialList){

            if(item.equalsIgnoreCase("New")){
                //todo go to editor, clear historylist
            }else if(item.equalsIgnoreCase("Favorites")){

            }else if(item.equalsIgnoreCase("Browse Directories")){

                //todo root list and external storage
            }else if(item.equalsIgnoreCase("Recent Directories")){

            }else if(item.equalsIgnoreCase("Recent Files")){

            }
        }else
        {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    class NavigationListAdapter extends ArrayAdapter<String> {
        public NavigationListAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            View row= inflater.inflate(R.layout.list_item,null,false);
            TextView textview= (TextView) row.findViewById(R.id.itemView);
            String currentFileName=MainActivity.this.currentList.get(position);
            if(isInitialList) {
                textview.setText(currentFileName);
                textview.setTextColor(Color.BLACK);
            }else{
                File currentFile= new File(currentFileName);
                textview.setText(currentFile.getName());
                textview.setTextColor(Color.BLACK);
                if(!currentFile.canWrite()){
                    if(currentFile.canRead()){
                        textview.setTypeface(null, Typeface.ITALIC);
                    }else{
                        textview.setTextColor(Color.GRAY);
                    }
                }
            }
            return row;
        }
    }
}
