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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MainActivity extends ListActivity {



    public static final String TAG=MainActivity.class.getName();

    public static final String NEW_FILE="New";
    public static final String BROWSE_DIRECTORIES="Browse Directories";
    public static final String RECENT_DIRECTORIES="Recent Directories";
    public static final String RECENT_FILES="Recent Files";
    public static final String FAVORTIES="Favorites";

    public static final String CURRENTLIST="currentList";
    public static final String HISTORYLIST="historyList";
    public static final String ISINITIALLIST="isInitialList";
    public final static String FIRSTPOSITION="firstPosition";

    public static final int DEFAULT_TEXT_COLOR=Color.BLACK;
    public static final int DEFAULT_BACKGROUND_COLOR=Color.WHITE;
    int textColor=DEFAULT_TEXT_COLOR;
    int fontSize=24;

    //contains the absolute paths of the directories, but only display file name
    ArrayList<String> initialList;
    ArrayList<String> currentList;
    ArrayList<String> historyList;//keep the directory path it is displaying
    ArrayAdapter<String> fileListAdapter;

    boolean isInitialList=true;

//    Button backButton;

    //New, favorites, default directories, recent directories, recent files

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate()", "Calling setContentView()");
        setContentView(R.layout.activity_main);

        initialList=new ArrayList<String>();
        initialList.add(NEW_FILE);
        initialList.add(FAVORTIES);
        initialList.add(BROWSE_DIRECTORIES);
        initialList.add(RECENT_DIRECTORIES);
        initialList.add(RECENT_FILES);

        currentList=initialList;
        historyList=new ArrayList<String>();

//        backButton=(Button)findViewById(R.id.backButton);

        Log.d(TAG, initialList.toString());
        try{
            //fileListAdapter=new ArrayAdapter<String>(this,R.layout.list_item, R.id.itemView,currDirListStr);
            //String[] test={"1","2"};
//            fileListAdapter=new NavigationListAdapter(this,R.layout.list_item, R.id.itemView, (String[])initialList.toArray());
            fileListAdapter=new NavigationListAdapter(this,R.layout.list_item, R.id.itemView, toStringArray(currentList));
            setListAdapter(fileListAdapter);
            setTitle(R.string.app_name);
        }catch (Exception e){
            e.printStackTrace();

        }
        Button backButton= (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backButtonPressed(v);
            }
        });

        Button homeButton= (Button) findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeButtonPressed(v);
            }
        });
        //todo read preference, set background, textColor, textSize
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(CURRENTLIST,currentList);
        outState.putStringArrayList(HISTORYLIST, historyList);
        outState.putBoolean(ISINITIALLIST, isInitialList);
        int firstVisiblePosition= this.getListView().getFirstVisiblePosition();
        outState.putInt(FIRSTPOSITION, firstVisiblePosition);
        //Log.d(TAG,"MAIN SaveInstance FirstPosition: "+firstVisiblePosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        currentList=state.getStringArrayList(CURRENTLIST);
        historyList=state.getStringArrayList(HISTORYLIST);
        isInitialList=state.getBoolean(ISINITIALLIST);

        fileListAdapter=new NavigationListAdapter(this,R.layout.list_item, R.id.itemView, toStringArray(currentList));
        setListAdapter(fileListAdapter);
        if(historyList !=null && historyList.size()>1){
            setTitle(historyList.get(historyList.size()-1));
        }else
        {
            setTitle(R.string.app_name);
        }
        int firstVisiblePosition= state.getInt(FIRSTPOSITION);
        //Log.d(TAG,"MAIN RestoreInstance FirstPosition: "+firstVisiblePosition);
        this.getListView().setSelectionFromTop(firstVisiblePosition, 0);
    }

    private void readPreferences(){
        String prefFile=getString(R.string.settings_file);
        SharedPreferences sharedPreferences = getSharedPreferences(prefFile, Context.MODE_PRIVATE);
        fontSize = sharedPreferences.getInt(getString(R.string.text_size_key),24);


    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //super.onListItemClick(l, v, position, id);
        String item=currentList.get(position);
        if(isInitialList){

            if(item.equalsIgnoreCase(NEW_FILE)){
                historyList=null;
                Intent intent2= new Intent(this, EditorActivity.class);
                startActivity(intent2);
            }else if(item.equalsIgnoreCase(FAVORTIES)){
                //Toast.makeText(this,"Favorites clicked",Toast.LENGTH_SHORT).show();
                isInitialList=false;
                historyList.add(FAVORTIES);
                setCurrentToFavorites();
                setTitle(FAVORTIES);
            }else if(item.equalsIgnoreCase(BROWSE_DIRECTORIES)){
                //Toast.makeText(this,"browse clicked",Toast.LENGTH_SHORT).show();
                isInitialList=false;
                historyList.add(BROWSE_DIRECTORIES);
                setCurrentToBrowseDirectories();
                setTitle(BROWSE_DIRECTORIES);
            }else if(item.equalsIgnoreCase(RECENT_DIRECTORIES)){
                isInitialList=false;
                historyList.add(RECENT_DIRECTORIES);
                setCurrentToRecentDirectories();
                setTitle(RECENT_DIRECTORIES);
            }else if(item.equalsIgnoreCase(RECENT_FILES)){
                isInitialList=false;
                historyList.add(RECENT_FILES);
                setCurrentToRecentFiles();
                setTitle(RECENT_FILES);
            }
        }else
        {
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
                    addRecentFileDirectory(currFile);
                    //Toast.makeText(this,Uri.fromFile(currFile).toString()+" is a "+mimeType+" file",Toast.LENGTH_LONG).show();
                    Intent intent2= new Intent(this, EditorActivity.class);
                    intent2.putExtra(EditorActivity.FILESTR, item);
                    startActivity(intent2);
                    //Toast.makeText(this,Uri.fromFile(currFile).toString()+" after startactivity",Toast.LENGTH_LONG).show();
                }
            }catch (IndexOutOfBoundsException ioobe){
                Log.e(TAG, "onChildClick IndexOutOfBoundException");
            }catch (Exception e){
                Log.e(TAG,"onChildClick Exception" );
                e.printStackTrace();
            }
        }

        fileListAdapter=new NavigationListAdapter(this,R.layout.list_item, R.id.itemView, toStringArray(currentList));
        setListAdapter(fileListAdapter);
    }

    private void processNewDirectory(String filePath){
        historyList.add(filePath);
        //historyIndex++;
        refreshNavigationList(filePath);
    }

    private void refreshNavigationList(String dirPath){

        //if(historyIndex>1){
//        if(historyList.size()>1){
//            backButton.setEnabled(true);
//        }else
//        {
//            backButton.setEnabled(false);
//        }
        try{
            File currFile=new File(dirPath);
            File[] currDirList = currFile.listFiles();
//            currDirListStr=new String[currDirList.length];
            currentList=new ArrayList<String>();
            for(int i=0;i<currDirList.length;i++){
                currentList.add(currDirList[i].getAbsolutePath());
            }
            //fileListAdapter=new ArrayAdapter<String>(this,R.layout.list_item, R.id.itemView,currDirListStr);
            fileListAdapter=new NavigationListAdapter(this,R.layout.list_item, R.id.itemView, toStringArray(currentList));
            setListAdapter(fileListAdapter);
            setTitle(currFile.getAbsolutePath());
        }catch (Exception e){
            e.printStackTrace();

        }
    }

    /**
     * Search from the currentList and their subdirectories for files with match name
     * @param view
     */
    public void searchButtonPressed(View view){
        //if it is initial list, search from root list

        //append search result to currentList

    }


    public void homeButtonPressed(View view){
        isInitialList=true;
        currentList=initialList;
        historyList=new ArrayList<String>();
        fileListAdapter=new NavigationListAdapter(this,R.layout.list_item, R.id.itemView, toStringArray(currentList));
        setListAdapter(fileListAdapter);
        setTitle(R.string.app_name);
    }

    public void backButtonPressed(View view){
        //if size of history is one and is equal to browse directory...
        //historyList size=0 currentList==initialList
        //historyList size=1 go back to initialList
        //historyList size>=3. pop2 add one back
        int historySize=historyList.size();
        if(historySize==0) {
            return;
        }else if(historySize==1){
            isInitialList=true;
            historyList.remove(0);
            currentList=initialList;
            fileListAdapter=new NavigationListAdapter(this,R.layout.list_item, R.id.itemView, toStringArray(currentList));
            setListAdapter(fileListAdapter);
            setTitle(R.string.app_name);

        }else if(historySize==2){
            historyList.remove(historySize-1);
            String item=historyList.get(historySize - 2);
            if(item.equalsIgnoreCase(FAVORTIES)){
                //Toast.makeText(this,"Favorites clicked",Toast.LENGTH_SHORT).show();
                isInitialList=false;
                setCurrentToFavorites();
                setTitle(FAVORTIES);
            }else if(item.equalsIgnoreCase(BROWSE_DIRECTORIES)){
                //Toast.makeText(this,"browse clicked",Toast.LENGTH_SHORT).show();
                isInitialList=false;
                setCurrentToBrowseDirectories();
                setTitle(BROWSE_DIRECTORIES);
            }else if(item.equalsIgnoreCase(RECENT_DIRECTORIES)){
                isInitialList=false;
                setCurrentToRecentDirectories();
                setTitle(RECENT_DIRECTORIES);
            }else if(item.equalsIgnoreCase(RECENT_FILES)){
                isInitialList=false;
                setCurrentToRecentFiles();
                setTitle(RECENT_FILES);
            }
            fileListAdapter=new NavigationListAdapter(this,R.layout.list_item, R.id.itemView, toStringArray(currentList));
            setListAdapter(fileListAdapter);
        }else{
            historyList.remove(historySize-1);
            String item=historyList.remove(historySize-2);
            processNewDirectory(item);
        }

    }


    private void setCurrentToBrowseDirectories(){
        currentList= new ArrayList<String>();
        // root list and external storage
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

    }

    private void setCurrentToFavorites(){
//        String favoritePrefFile=getString(R.string.favorites_file);
//        String favoriteNumKey=getString(R.string.num_favorites_key);
//        SharedPreferences sharedPreferences = getSharedPreferences(favoritePrefFile, Context.MODE_PRIVATE);
//        int num_favorites=sharedPreferences.getInt(favoriteNumKey, 0);
//        setCurrentToRecent(favoritePrefFile,getString(R.string.favorites),num_favorites);
        setCurrentToRecent(getString(R.string.favorites_text),getResources().getInteger(R.integer.max_favorites) );
        // sorting favorites in EditorAcitivity.addToFavorites
    }


    private void setCurrentToRecentDirectories(){
        int num_recent=getResources().getInteger(R.integer.num_recent);
//        setCurrentToRecent(getString(R.string.recent_directories_file),
//                getString(R.string.recent_directories_key), num_recent);
        setCurrentToRecent(getString(R.string.recent_directories_text), num_recent);

    }

    private void setCurrentToRecentFiles(){
        int num_recent=getResources().getInteger(R.integer.num_recent);
//        setCurrentToRecent(getString(R.string.recent_files_file),
//                getString(R.string.recent_files_key), num_recent);
        setCurrentToRecent(getString(R.string.recent_files_text), num_recent);
    }


    private void setCurrentToRecent(String prefFile, String prefKey, int num_recent){
        SharedPreferences sharedPreferences = getSharedPreferences(prefFile, Context.MODE_PRIVATE);
        String tempStr="";
        currentList=new ArrayList<String>();
        for(int i=0;i<num_recent;i++){
            tempStr = sharedPreferences.getString(prefKey+i,"");
            //Toast.makeText(this,"File "+i+" "+tempStr,Toast.LENGTH_SHORT).show();
            if(tempStr !=null && tempStr.length()>0){
                currentList.add(tempStr);
            }
        }
    }

    private void setCurrentToRecent(String prefFile, int num_recent){
        File file = new File(this.getFilesDir(), prefFile);
        currentList=new ArrayList<String>();
        try {

            if(file.exists()) {
                BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line = "";
                int i = 1;
                String tempStr = "";
                while ((line = buf.readLine()) != null && i < num_recent) {
                    Log.d(TAG, "read: " + line);
                    tempStr = line.substring(0, line.indexOf(";"));
                    if (tempStr != null && tempStr.length() > 0) {
                        currentList.add(tempStr);
                    }
                    i++;
                }
            }
        }catch (FileNotFoundException fnfe){
            //Toast.makeText(this, "Cannot read file. File does not exist.", Toast.LENGTH_SHORT).show();
            fnfe.printStackTrace();
        }catch (IOException ioe){
//            Toast.makeText(this, "IO Error reading file.", Toast.LENGTH_SHORT).show();
            ioe.printStackTrace();

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

    /**
     * add the selected file and its parent directory to recent lists
     * @param newRecentFile the recent file
     */
    private void addRecentFileDirectory(File newRecentFile){
        int num_recent=getResources().getInteger(R.integer.num_recent);
//        addRecent(newRecentFile.getParent(),getString(R.string.recent_directories_file),
//                getString(R.string.recent_directories_key),num_recent);
//
//        addRecent(newRecentFile.getAbsolutePath(), getString(R.string.recent_files_file),
//                getString(R.string.recent_files_key), num_recent);


        addRecent(newRecentFile.getParent(),getString(R.string.recent_directories_text), num_recent);
        addRecent(newRecentFile.getAbsolutePath(),getString(R.string.recent_files_text), num_recent);
    }


    /**
     *
     * @param str the file or directory
     * @param prefFile the preference file
     * @param num_recent the maximum number of items kept in recent list
     */
    private void addRecent(String str, String prefFile, int num_recent){
        File file = new File(this.getFilesDir(), prefFile);
        FileOutputStream outputStream;
        StringBuilder builder= new StringBuilder();
        String curr=str+";";
        boolean currExists=false;
        try {
            if(file.exists()) {
                BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line = "";
                int i = 1;
                while ((line = buf.readLine()) != null && i < num_recent) {
                    if(!line.contains(curr)) {
                        builder.append(line);
                        builder.append("\n");
                    }else{
                        currExists=true;
                        builder.insert(0,"\n");
                        builder.insert(0,line);
                    }
                    i++;
                }
            }
            if(!currExists){
                builder.insert(0,"\n");
                builder.insert(0,curr);
            }
            Log.d(TAG, "out: "+builder.toString());
            outputStream=openFileOutput(prefFile, Context.MODE_PRIVATE);
            outputStream.write(builder.toString().getBytes());
            outputStream.close();
        }catch (FileNotFoundException fnfe){
            //Toast.makeText(this, "Cannot read file. File does not exist.", Toast.LENGTH_SHORT).show();
            fnfe.printStackTrace();
        }catch (IOException ioe){
//            Toast.makeText(this, "IO Error reading file.", Toast.LENGTH_SHORT).show();
            ioe.printStackTrace();

        }
    }



    /**
     * //changed from preferences to files
     * @param str the file or directory
     * @param prefFile the preference file
     * @param prefKey the preference key
     * @param num_recent the maximum number of items kept in recent list
     */
    private void addRecent(String str, String prefFile, String prefKey, int num_recent){

        SharedPreferences sharedPreferences = getSharedPreferences(prefFile, Context.MODE_PRIVATE);
        String tempStr="";
        ArrayList<String> tempRecent=new ArrayList<String>(num_recent);
        for(int i=0;i<num_recent;i++){
            tempStr = sharedPreferences.getString(prefKey+i,"");
            if(tempStr != null && tempStr.length()>0){
                tempRecent.add(tempStr);
            }
        }
        if(tempRecent.indexOf(str)>=0){
            tempRecent.remove(str);
            tempRecent.add(0,str);
        }else{
            if(tempRecent.size()==num_recent) {
                tempRecent.remove(tempRecent.size() - 1);
            }
            tempRecent.add(0,str);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(int i=0;i<tempRecent.size() && i<num_recent;i++){
            editor.putString(prefKey + i, tempRecent.get(i));
        }
        editor.apply();
    }

    //todo onFinish, if this is called by Editor to choose directory, return directory, or file name
    //getParent, call function to set saveFilePath

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
        if (id == R.id.action_smallerText){

        }
        if (id == R.id.action_largerText){

        }
        //todo new directory and text size, clean favorites(remove non-existing files)
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

    private String[] toStringArray(ArrayList<String> temp){
        if(temp==null){
            return null;
        }
        String[] arr=new String[temp.size()];
        for(int i=0;i<temp.size();i++){
            arr[i]=temp.get(i);
        }
        return arr;
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
            //todo set text size
            String currentFilePath=MainActivity.this.currentList.get(position);
            if(isInitialList) {
                textview.setText(currentFilePath);
                textview.setTextColor(Color.BLACK);
            }else{
                File currentFile= new File(currentFilePath);
                textview.setTextColor(Color.BLACK);
                if(!currentFile.canWrite()){
                    if(currentFile.canRead()){
                        textview.setTypeface(null, Typeface.ITALIC);
                    }else{
                        textview.setTextColor(Color.GRAY);
                    }
                }
                if(currentFile.isDirectory()){
                    textview.setText(currentFile.getName()+"/   ");
                }else{
                    textview.setText(currentFile.getName()+"   ");
                }
            }
            return row;
        }
    }
}
