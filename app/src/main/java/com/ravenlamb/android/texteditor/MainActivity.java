package com.ravenlamb.android.texteditor;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;


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
    public static final String FIRSTPOSITION="firstPosition";

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

    AlertDialog randomDialog;
//    Button backButton;

    String currentSearchText="";
    String previousSearchText="";
    FilterTask filterTask;
    boolean searchPressed=false;
    boolean searchCompleted=false;
    ArrayList<String> filterDirList;



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

        currentList=(ArrayList<String>)initialList.clone();
        historyList=new ArrayList<String>();


        Log.d(TAG, initialList.toString());
        try{
            fileListAdapter=new NavigationListAdapter(this,R.layout.list_item, currentList);
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

        Button searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchButtonPressed(v);
            }
        });

        EditText searchText=(EditText) findViewById(R.id.autocompleteEditText);
        searchText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(isInitialList){
                    return;
                }
                //search button not pressed, shorter than two characters, and no previous search
                if(!searchPressed && previousSearchText.length()==0 && s.length()<2){
                    Log.d(TAG,"onTextChanged Return "+s.toString());
                    return;
                }
                //search string is longer than 2 characters, and (no previous search or search button pressed)
                if(previousSearchText.length()==0 && (s.length()>=2 || searchPressed)) {
                    Log.d(TAG,"onTextChanged Start "+s.toString());
                    MainActivity.this.getListView().setSelectionFromTop(0,0); //scroll to top
                    currentSearchText=s.toString();
                    filterDirList=new ArrayList<String>();
                    filterTask=new FilterTask();
                    filterTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,currentSearchText);
                }else{//there is a previous search
                    Log.d(TAG,"onTextChanged Continue "+s.toString());
                    currentSearchText=s.toString();
                    if(filterTask.getStatus()== AsyncTask.Status.FINISHED){
                        Log.d(TAG,"onTextChanged Continue Task FINISHED");
                        filterCheck();
                    }else{
                        Log.d(TAG,"onTextChanged Continue Task NOT FINISHED");
                        filterTask.cancel(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //todo read preference, set background, textColor, textSize
    }


    private void filterCheck(){
        if(currentSearchText.contains(previousSearchText)){
            Log.d(TAG,"filterCheck 1 "+previousSearchText+" "+currentSearchText);
            fileListAdapter.notifyDataSetChanged();
            filterTask=new FilterTask();
            filterTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentSearchText);
        }else{
            Log.d(TAG, "filterCheck 2 " + previousSearchText + " " + currentSearchText);
            resetCurrentDirectory();
            filterDirList=new ArrayList<String>();
            if(currentSearchText.length()<2){
                previousSearchText="";
                fileListAdapter.notifyDataSetChanged();
                return;
            }
            filterTask=new FilterTask();
            filterTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentSearchText);
        }

    }

    class FilterTask extends AsyncTask<String, Void, Integer>{
        public FilterTask() {
            super();
        }

        //to cancel task, call filterTask.cancel(true); then isCancelled returns true
        @Override
        protected Integer doInBackground(String... params) {
            if(params[0]==null){
                return null;
            }
            String filterString=params[0].toLowerCase();
            Log.d(TAG, "doInBackground " + filterString);
            previousSearchText = filterString;
            //// TODO: 3/13/2016
            //loop through currentList, remove items not containing filter
            //check isCancelled. break out of loop if true
            int currSize = currentList.size();
            for (int i = currSize - 1; i >= 0; i--) {
                String temp = currentList.get(i);
                File currFile=new File(temp);
                if(currFile.isDirectory()){
                    filterDirList.add(temp);
                }
                if (!temp.toLowerCase().contains(filterString)) {
                    currentList.remove(temp);
                }
                if (isCancelled()) {
                    return 1;
                }
            }
            if(searchPressed){
                //// TODO: 5/6/2016 search through filterDirLIst
                while(filterDirList.size()>0){
                    String currSearchDir=filterDirList.remove(0);
                    try{
                        File currDirFile=new File(currSearchDir);
                        File[] currDirList = currDirFile.listFiles();
                        int currentDirectorySize=getCurrentDirectory().length();
                        for (int i=0;i<currDirList.length;i++) {
                            String temp=currDirList[i].getAbsolutePath();
                            if(currDirList[i].isDirectory()){
                                filterDirList.add(temp);
                            }
                            if (temp.substring(currentDirectorySize).toLowerCase().contains(filterString)) {
                                currentList.add(temp);
                            }
                            if (isCancelled()) {
                                return 1;
                            }
                        }
                        searchCompleted=true;
                    }catch (Exception e){
                        e.printStackTrace();

                    }

                }
            }
            Log.d(TAG,"doInBackground completes");
            return 1;
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG,"onCancelled()");
            filterCheck();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            Log.d(TAG, "onPostExecute(integer)");
            loadCurrentListToAdapter();
            fileListAdapter.notifyDataSetChanged();
        }
    }


    /**
     * Search from the currentList and their subdirectories for files with match name
     * @param view
     */
    public void searchButtonPressed(View view){
        // TODO: 4/17/2016
        //if it is initial list, search from root list

        //append search result to currentList
        if(isInitialList){
            // TODO: 5/7/2016 set current directory to root or return
            return;
        }
        searchPressed=true;
        searchCompleted=false;
        filterTask=new FilterTask();
        EditText searchText=(EditText) findViewById(R.id.autocompleteEditText);
        currentSearchText=searchText.getText().toString();
        filterTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentSearchText);
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

//        fileListAdapter=new NavigationListAdapter(this,R.layout.list_item, R.id.itemView, toStringArray(currentList));
        fileListAdapter=new NavigationListAdapter(this,R.layout.list_item, currentList);
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

    // TODO: 5/6/2016 move to sharepreferences
    private void readPreferences(){
        String prefFile=getString(R.string.settings_file);
        SharedPreferences sharedPreferences = getSharedPreferences(prefFile, Context.MODE_PRIVATE);
        fontSize = sharedPreferences.getInt(getString(R.string.text_size_key),24);


    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //super.onListItemClick(l, v, position, id);

        //todo check and stop FilterTask And SearchTask

        if(searchPressed || currentSearchText.length()>0){
            if(filterTask !=null){
                filterTask.cancel(true);
            }
            searchPressed=false;
            // TODO: 5/19/2016 all directory change all need to set searchPressed to false 
        }

//        String item=currentList.get(position);
        String item=fileListAdapter.getItem(position);
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

            loadCurrentListToAdapter();
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


    }

    private void processNewDirectory(String filePath){
        historyList.add(filePath);
        //historyIndex++;
        readDirectory(filePath);
    }

    private void resetCurrentDirectory(){
        if(isInitialList){
            loadHomeDirectory();
            return;
        }
        int historySize=historyList.size();

        String item=historyList.get(historySize - 1);
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
        }else{
            readDirectory(item);
            return;
        }
        loadCurrentListToAdapter();
    }

    private void readDirectory(String dirPath){

        try{
            File currFile=new File(dirPath);
            File[] currDirList = currFile.listFiles();
//            currDirListStr=new String[currDirList.length];
            currentList=new ArrayList<String>();
            for(int i=0;i<currDirList.length;i++){
                currentList.add(currDirList[i].getAbsolutePath());
            }

            setTitle(currFile.getAbsolutePath());
            loadCurrentListToAdapter();
        }catch (Exception e){
            e.printStackTrace();

        }
    }

    private void loadCurrentListToAdapter(){
        fileListAdapter.clear();
        fileListAdapter.addAll(currentList);
        fileListAdapter.notifyDataSetChanged();
    }



    public void homeButtonPressed(View view){
        loadHomeDirectory();
    }

    public void loadHomeDirectory(){
        isInitialList=true;
        currentList=(ArrayList<String>)initialList.clone();
        historyList=new ArrayList<String>();

        setTitle(R.string.app_name);
        loadCurrentListToAdapter();

    }


    public String getCurrentDirectory(){
        // TODO: 5/7/2016 need to save currentDirectory as instance variable, too many reads required for listview
        String currentDirectory=historyList.get(historyList.size()-1)+"/";
        if(historyList.size()<2){
            currentDirectory="";
        }
        if(currentDirectory.equals("//")){
            currentDirectory="/";
        }
        return currentDirectory;
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
            loadHomeDirectory();

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
            loadCurrentListToAdapter();
        }else{
            historyList.remove(historySize-1);

            // edited on march 13
//            String item=historyList.remove(historySize-2);
//            processNewDirectory(item);
            String item=historyList.get(historySize-2);
            readDirectory(item);
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
        setCurrentToRecent(getString(R.string.favorites_text),getResources().getInteger(R.integer.max_favorites) );
        // sorting favorites in EditorAcitivity.addToFavorites
    }


    private void setCurrentToRecentDirectories(){
        int num_recent=getResources().getInteger(R.integer.num_recent);
        setCurrentToRecent(getString(R.string.recent_directories_text), num_recent);

    }

    private void setCurrentToRecentFiles(){
        int num_recent=getResources().getInteger(R.integer.num_recent);
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


    // TODO: 5/6/2016 move to sharedpreferences
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


    /**
     // TODO: 5/6/2016 move to sharedpreferences
     * add the selected file and its parent directory to recent lists
     * @param newRecentFile the recent file
     */
    private void addRecentFileDirectory(File newRecentFile){
        int num_recent=getResources().getInteger(R.integer.num_recent);

        addRecent(newRecentFile.getParent(),getString(R.string.recent_directories_text), num_recent);
        addRecent(newRecentFile.getAbsolutePath(),getString(R.string.recent_files_text), num_recent);
    }


    /**
     // TODO: 5/6/2016 move to sharedpreferences
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
        if(id == R.id.action_random){
            newRandomDialog();
//            dialog.getWindow().setLayout(dialogWidth,dialogHeight);
            return true;
        }
        //todo new directory and text size, clean favorites(remove non-existing files)
        return super.onOptionsItemSelected(item);
    }

    private void newRandomDialog(){
        final int sizeOFList=currentList.size();
        final SecureRandom random= new SecureRandom();
        int randomNum=random.nextInt(sizeOFList);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(randomNum+"/"+sizeOFList+"\n"+currentList.get(randomNum))
                .setPositiveButton("Next", null)
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        randomDialog = builder.create();
        randomDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b =  randomDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int randomNum=random.nextInt(sizeOFList);
                        randomDialog.setTitle(randomNum+"/"+sizeOFList+"\n"+currentList.get(randomNum));
                    }
                });
            }
        });
        randomDialog.show();

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

        LayoutInflater mInflator;

        //so that ArrayAdapter.remove can be used by filter and search
        public NavigationListAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            mInflator = (LayoutInflater)getApplicationContext().getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public void remove(String object) {
            super.remove(object);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textview;
            if(convertView==null) {
                convertView = mInflator.inflate(R.layout.list_item, null, false);
                textview = (TextView) convertView.findViewById(R.id.itemView);
                convertView.setTag(textview);
            }else{
                textview=(TextView)convertView.getTag();
            }
            //todo set text size

            //todo filterList
//            String currentFilePath=MainActivity.this.currentList.get(position);
            String currentFilePath=this.getItem(position);
            if(isInitialList) {
                textview.setText(currentFilePath);
                textview.setTextColor(Color.BLACK);
            }else{
                String currentDirectory=historyList.get(historyList.size()-1)+"/";
                if(historyList.size()<2){
                    currentDirectory="";
                }else if(currentFilePath.equals("/")){
                    currentDirectory="/";
                }
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
//                    textview.setText(currentFile.getName() + "/   ");
                    textview.setText(currentFilePath.substring(currentDirectory.length()) + "/   ");
                }else{
                    textview.setText(currentFilePath.substring(currentDirectory.length())+"   ");
                }
            }
            return convertView;
        }
    }
}
