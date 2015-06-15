package com.ravenlamb.android.texteditor;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
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
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


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
                currentList= new ArrayList<String>();
                isInitialList=false;
                //todo root list and external storage
                File[] rootlist= File.listRoots();
                for(int i=0;i<rootlist.length;i++){
                    currentList.add(rootlist[i].getAbsolutePath());
                }
                if(isExternalStorageReadable()){

//            Environment.DIRECTORY_ALARMS, Environment.DIRECTORY_DCIM, Environment.DIRECTORY_DOCUMENTS
//            Environment.DIRECTORY_DOWNLOADS, Environment.DIRECTORY_MOVIES,
//            Environment.DIRECTORY_MUSIC, Environment.DIRECTORY_PICTURES, Environment.DIRECTORY_PODCASTS
//            Environment.DIRECTORY_RINGTONES
                    //Toast.makeText(this, "external storage is readable", Toast.LENGTH_SHORT).show();
                    if(Build.VERSION.SDK_INT >=19){
                        currentList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
                        //externalPublic.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
                    }
                    currentList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
                    currentList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
                    currentList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
                    currentList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());
                    currentList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath());
                    currentList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES).getAbsolutePath());
                    currentList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS).getAbsolutePath());
                    currentList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS).getAbsolutePath());

                }
            }else if(item.equalsIgnoreCase("Recent Directories")){

            }else if(item.equalsIgnoreCase("Recent Files")){

            }
        }else
        {
            String currentFilePath=item;
            historyList.add(currentFilePath);
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

    /* Checks if external storage is available for read and write */
    //copied from android.com
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    //copied from android.com
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
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
            String currentFilePath=MainActivity.this.currentList.get(position);
            if(isInitialList) {
                textview.setText(currentFilePath);
                textview.setTextColor(Color.BLACK);
            }else{
                File currentFile= new File(currentFilePath);
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
