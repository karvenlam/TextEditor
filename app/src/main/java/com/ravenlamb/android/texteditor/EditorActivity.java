package com.ravenlamb.android.texteditor;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.SortedMap;

/**
 * have multiple ArrayAdapter, or multiple style files,
 *
 * with/without line number
 * with/without wrap
 * binary
 */

public class EditorActivity extends ListActivity {


    public static final String TAG=EditorActivity.class.getName();

    public final static String FILESTR="com.ravenlamb.android.texteditor.EditorActivity.FILESTR";
    public final static String FILEPATH="filePath";
    public final static String CURRCHARSET="currCharSet";
    public final static String TEXTARRAYLIST="textArrayList";
    public final static String FIRSTPOSITION="firstPosition";

    private String filePath;
    private ArrayList<String> textArrayList;

//            ISO-8859-1
//            US-ASCII
//            UTF-16
//            UTF-16BE
//            UTF-16LE
//            UTF-8
    boolean displayAsBinary=false;
    String currCharset="US-ASCII";
    String[] charsetList={"ISO-8859-1", "US-ASCII","UTF-16","UTF-16BE","UTF-16LE","UTF-8"};
    String saveFilePath="";//todo


    ArrayAdapter<String> fileListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Intent intent =getIntent();
        filePath=intent.getStringExtra(FILESTR);

        SortedMap<String, Charset> charsetSortedMap= Charset.availableCharsets();
        Set<String> charsetNames=charsetSortedMap.keySet();
        charsetList= charsetNames.toArray(charsetList);

        String settings_file=getString(R.string.settings_file);
        String text_binary_key=getString(R.string.text_binary_key);
        SharedPreferences sharedPreferences = getSharedPreferences(settings_file, Context.MODE_PRIVATE);
        String textBinaryDisplay = sharedPreferences.getString(text_binary_key, "text");
        displayAsBinary= (!textBinaryDisplay.equals("text"));
        if(filePath==null){
            createNewTextFile();
        }else {
            readFile();
        }

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putCharSequence(FILEPATH, filePath);
        outState.putCharSequence(CURRCHARSET, currCharset);
        outState.putStringArrayList(TEXTARRAYLIST, textArrayList);
        int firstVisiblePosition= this.getListView().getFirstVisiblePosition();
        outState.putInt(FIRSTPOSITION,firstVisiblePosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        filePath=state.getString(FILEPATH);
        currCharset=state.getString(CURRCHARSET);
        textArrayList=state.getStringArrayList(TEXTARRAYLIST);

        fileListAdapter=new TextFileListAdapter(this,R.layout.editor_line, R.id.itemLine, toStringArray(textArrayList));
        setListAdapter(fileListAdapter);
        setTitle(filePath);

        int firstVisiblePosition= state.getInt(FIRSTPOSITION);
        this.getListView().setSelectionFromTop(firstVisiblePosition, 0);
    }

    private void createNewTextFile() {
    }

    private void readFile(){

        if(displayAsBinary){
            //todo
        }else{
            readFileAsText();
        }
    }

    private void readFileAsText(){
        //todo for large files, use LineNumberReader
        try {
            BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), currCharset));
            String line="";
            textArrayList=new ArrayList<String>();
            while ((line = buf.readLine()) != null) {
                textArrayList.add(line);
            }

            fileListAdapter=new TextFileListAdapter(this,R.layout.editor_line, R.id.itemLine, toStringArray(textArrayList));

            setListAdapter(fileListAdapter);
            setTitle(filePath);
        }catch (FileNotFoundException fnfe){
            //Toast.makeText(this, "Cannot read file. File does not exist.", Toast.LENGTH_SHORT).show();
            fnfe.printStackTrace();
        }catch (IOException ioe){
//            Toast.makeText(this, "IO Error reading file.", Toast.LENGTH_SHORT).show();
            ioe.printStackTrace();

        }

    }




    @Override
    protected void onListItemClick(ListView l, View v, final int position, long id) {
        //super.onListItemClick(l, v, position, id);
        //// TODO: 8/30/2015 if edit is true, edit selected line,
        Log.d(TAG, "onListItemClick " + position);
        Toast.makeText(this,"onListItemClick "+v.getId(),Toast.LENGTH_SHORT).show();
        Log.wtf(TAG,"onListItemClick "+v.getId());
        View view=(View) getListView().getItemAtPosition(position);

        EditText editText=(EditText) view.findViewById(R.id.itemLine);
        //editText.setTag(editText.getKeyListener());
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String temp=s.toString();
                if(temp.contains("\n")){
                    int newlineIndex=temp.indexOf("\n");
                    textArrayList.set(position, temp.substring(0,newlineIndex));
                    textArrayList.add(position+1, temp.substring(newlineIndex+1));
                }else {
                    textArrayList.set(position, temp);
                    Log.d(TAG,"onTextChanged "+temp+" "+position );
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                fileListAdapter.notifyDataSetChanged();
                Log.d(TAG, "afterTextChanged");
            }
        });
    }
//    ArrayAdapter
//    public void insert (T object, int index)
//
//    Added in API level 1
//    Inserts the specified object at the specified index in the array.
//
//            Parameters
//    object	The object to insert into the array.
//    index	The index at which the object must be inserted.
//    public void notifyDataSetChanged ()
//
//    Added in API level 1
//    Notifies the attached observers that the underlying data has been changed and any View reflecting the data set should refresh itself.
//
//    public void remove (T object)
//
//    Added in API level 1
//    Removes the specified object from the array.
//
//            Parameters
//    object	The object to remove.


//    this.getListView().setLongClickable(true);
//    this.getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
//        public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
//            //Do some
//            return true;
//        }
//    });

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);

        String favoritePrefFile=getString(R.string.favorites_file);
        String favoriteNumKey=getString(R.string.num_favorites_key);
        String prefKey=getString(R.string.favorites);
        SharedPreferences sharedPreferences = getSharedPreferences(favoritePrefFile, Context.MODE_PRIVATE);
        int num_favorites=sharedPreferences.getInt(favoriteNumKey, 0);
        String tempStr="";
        for(int i=0;i<num_favorites;i++){
            tempStr = sharedPreferences.getString(prefKey + i, "");
            //Toast.makeText(this,"File "+i+" "+tempStr,Toast.LENGTH_SHORT).show();
            if(tempStr !=null && tempStr.equals(filePath)){
                MenuItem favoriteMenuItem= (MenuItem) findViewById(R.id.action_favorites);
                favoriteMenuItem.setChecked(true);
                break;
            }
        }
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
            Toast.makeText(this,"settings",Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_charsets) {

            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("Select Charset")
                    .setItems(charsetList, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currCharset=charsetList[which];
                            readFile();
                        }
                    });
            AlertDialog dialog=builder.create();
            dialog.show();
            return true;
        }
        if (id == R.id.action_favorites) {
            if(item.isChecked()){
                removeFromFavorites();
                item.setChecked(false);
            }else{
                addToFavorites();
                item.setChecked(true);
            }
        }
        //todo binary hex mode, text size
        //todo edit/view
        //todo save
        return super.onOptionsItemSelected(item);
    }
    
    private void addToFavorites(){
        //// TODO: 8/27/2015  
    }
    
    private void removeFromFavorites(){
        //// TODO: 8/27/2015  
    }

    class LineTextWatcher implements TextWatcher{
        public int lineNum;

        public LineTextWatcher(int l){
            lineNum=l;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            Log.d(TAG, "beforeTextChanged");

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String temp=s.toString();
            if(temp.contains("\n")){
                int newlineIndex=temp.indexOf("\n");
                textArrayList.set(lineNum, temp.substring(0,newlineIndex));
                textArrayList.add(lineNum+1, temp.substring(newlineIndex+1));

            }else {
                textArrayList.set(lineNum, temp);
                Log.d(TAG,"onTextChanged "+temp+" "+lineNum );
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            //fileListAdapter.notifyDataSetChanged();
            Log.d(TAG, "afterTextChanged");
        }
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


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            View row= inflater.inflate(R.layout.editor_line,null,false);
            TextView textview= (TextView) row.findViewById(R.id.itemLine);
            TextView lineNumView= (TextView) row.findViewById(R.id.lineNum);

            textview.setText(textArrayList.get(position));
            lineNumView.setText(String.valueOf(position + 1));

            textview.addTextChangedListener(new LineTextWatcher(position));
            //todo set text size
//            String currentLine=EditorActivity.this.textArrayList.get(position);
//
//            if(isInitialList) {
//                textview.setText(currentFilePath);
//                textview.setTextColor(Color.BLACK);
//            }else{
//                File currentFile= new File(currentFilePath);
//                textview.setTextColor(Color.BLACK);
//                if(!currentFile.canWrite()){
//                    if(currentFile.canRead()){
//                        textview.setTypeface(null, Typeface.ITALIC);
//                    }else{
//                        textview.setTextColor(Color.GRAY);
//                    }
//                }
//                if(currentFile.isDirectory()){
//                    textview.setText(currentFile.getName()+"/   ");
//                }else{
//                    textview.setText(currentFile.getName()+"   ");
//                }
//            }
            return row;
        }
    }

}
