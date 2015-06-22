package com.ravenlamb.android.texteditor;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
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


public class MainActivity extends ListActivity {


    //contains the absolute paths of the directories, but only display file name
    ArrayList<String> initialList;
    ArrayList<String> currentList;
    ArrayList<String> historyList;//keep the directory path it is displaying
    ArrayAdapter<String> fileListAdapter;

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
            try {
                File currFile=new File(item);
                if(!currFile.canRead()){
                    Toast.makeText(this,"File is not readable",Toast.LENGTH_SHORT).show();
                }else if(currFile.isDirectory()){
                    processNewDirectory(item);

                }else if(currFile.isFile()){

//                Uri returnUri = returnIntent.getData();
//                String mimeType = getContentResolver().getType(returnUri);
                    //todo: need to make sure file is not binary
                    String mimeType = getContentResolver().getType(Uri.fromFile(currFile));
                    Toast.makeText(this,Uri.fromFile(currFile).toString()+" is a "+mimeType+" file",Toast.LENGTH_LONG).show();
                    addRecentFileDirectory(currFile);
                    Intent intent2= new Intent(this, EditorActivity.class);
                    intent2.putExtra(DisplayList.FILESTR, currentFilePath);
                    startActivity(intent2);
                }
            }catch (IndexOutOfBoundsException ioobe){
                Log.e(TAG, "onChildClick IndexOutOfBoundException");
            }catch (Exception e){
                Log.e(TAG,"onChildClick Exception" );
                e.printStackTrace();
            }
        }
    }

    private void processNewDirectory(String filePath){
        historyList.add(filePath);
        //historyIndex++;
        refreshNavigationList(filePath);
    }

    private void refreshNavigationList(String dirPath){

        //if(historyIndex>1){
        if(historyList.size()>1){
            backButton.setEnabled(true);
        }else
        {
            backButton.setEnabled(false);
        }
        try{
            File currFile=new File(dirPath);
            File[] currDirList = currFile.listFiles();
//            currDirListStr=new String[currDirList.length];
            currentList=new ArrayList<String>();
            for(int i=0;i<currDirList.length;i++){
                currentList.add(currDirList[i].getAbsolutePath());
            }
            //fileListAdapter=new ArrayAdapter<String>(this,R.layout.list_item, R.id.itemView,currDirListStr);
            fileListAdapter=new NavigationListAdapter(this,R.layout.list_item, R.id.itemView, (String[])currentList.toArray());
            setListAdapter(fileListAdapter);
            setTitle(currFile.getAbsolutePath());
        }catch (Exception e){
            e.printStackTrace();

        }
    }


//    private void addRecentFileDirectory(File newRecentFile){
//
//        String newRecentDir=newRecentFile.getParent();
//        SharedPreferences sharedPrefDir = getSharedPreferences(getString(R.string.recent_directories_file), Context.MODE_PRIVATE);
//        int num_recent=getResources().getInteger(R.integer.num_recent);
//        String tempStr="";
//        ArrayList<String> tempRecent=new ArrayList<String>(num_recent);
//        for(int i=0;i<num_recent;i++){
//            tempStr = sharedPrefDir.getString(getString(R.string.recent_directories_key)+i,"");
//            if(tempStr.length()>0){
//                tempRecent.add(tempStr);
//            }
//        }
//        if(tempRecent.indexOf(newRecentDir)>=0){
//            tempRecent.remove(newRecentDir);
//            tempRecent.add(0,newRecentDir);
//        }else{
//            tempRecent.remove(tempRecent.size()-1);
//            tempRecent.add(0,newRecentDir);
//        }
//        SharedPreferences.Editor sharedPrefDirEdit = sharedPrefDir.edit();
//        for(int i=0;i<tempRecent.size() && i<num_recent;i++){
//            sharedPrefDirEdit.putString(getString(R.string.recent_directories_key)+i,tempRecent.get(i));
//        }
//        sharedPrefDirEdit.commit();
//        // add recent file
//        String newRecentFileStr=newRecentFile.getAbsolutePath();
//        SharedPreferences sharedPrefFile = getSharedPreferences(getString(R.string.recent_files_file), Context.MODE_PRIVATE);
//        tempStr="";
//        tempRecent=new ArrayList<String>(num_recent);
//        for(int i=0;i<num_recent;i++){
//            tempStr = sharedPrefFile.getString(getString(R.string.recent_files_key)+i,"");
//            if(tempStr.length()>0){
//                tempRecent.add(tempStr);
//            }
//        }
//        if(tempRecent.indexOf(newRecentFileStr)>=0){
//            tempRecent.remove(newRecentFileStr);
//            tempRecent.add(0,newRecentFileStr);
//        }else{
//            tempRecent.remove(tempRecent.size()-1);
//            tempRecent.add(0,newRecentFileStr);
//        }
//        SharedPreferences.Editor sharedPrefFileEdit = sharedPrefFile.edit();
//        for(int i=0;i<tempRecent.size() && i<num_recent;i++){
//            sharedPrefFileEdit.putString(getString(R.string.recent_files_key)+i,tempRecent.get(i));
//        }
//        sharedPrefDirEdit.commit();
//    }

    private void addRecentFileDirectory(File newRecentFile){
        int num_recent=getResources().getInteger(R.integer.num_recent);
        addRecent(newRecentFile.getParent(),getString(R.string.recent_directories_file),
                getString(R.string.recent_directories_key),num_recent);

        addRecent(newRecentFile.getAbsolutePath(),getString(R.string.recent_files_file),
                getString(R.string.recent_files_key),num_recent);
    }

    private void addRecent(String str, String prefFile, String prefKey, int num_recent){

        SharedPreferences sharedPreferences = getSharedPreferences(prefFile, Context.MODE_PRIVATE);
        String tempStr="";
        ArrayList<String> tempRecent=new ArrayList<String>(num_recent);
        for(int i=0;i<num_recent;i++){
            tempStr = sharedPreferences.getString(prefKey+i,"");
            if(tempStr.length()>0){
                tempRecent.add(tempStr);
            }
        }
        if(tempRecent.indexOf(str)>=0){
            tempRecent.remove(str);
            tempRecent.add(0,str);
        }else{
            tempRecent.remove(tempRecent.size()-1);
            tempRecent.add(0,str);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(int i=0;i<tempRecent.size() && i<num_recent;i++){
            editor.putString(prefKey + i, tempRecent.get(i));
        }
        editor.commit();
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
