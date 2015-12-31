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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;

/**
 * have multiple ArrayAdapter, or multiple style files,
 *
 * with/without line number
 * with/without wrap
 * binary
 */

public class EditorActivity extends ListActivity
    implements LineEditText.OnInputConnectionInteraction {


    public static final String TAG = EditorActivity.class.getName();

    public final static String FILESTR = "com.ravenlamb.android.texteditor.EditorActivity.FILESTR";
    public final static String FILEPATH = "filePath";
    public final static String CURRCHARSET = "currCharSet";
    public final static String TEXTARRAYLIST = "textArrayList";
    public final static String FIRSTPOSITION = "firstPosition";


//    public final static String ZERO_SPACE="\n\n";
//    public final static String ZERO_SPACE="\u0008";
    //\u200B zero width space
    //\u200D zero width joiner

    private String filePath;
    private ArrayList<String> textArrayList;
    private ListView listView;

    //            ISO-8859-1
//            US-ASCII
//            UTF-16
//            UTF-16BE
//            UTF-16LE
//            UTF-8
    boolean displayAsBinary = false;
    String currCharset = "US-ASCII";
    String[] charsetList = {"ISO-8859-1", "US-ASCII", "UTF-16", "UTF-16BE", "UTF-16LE", "UTF-8"};
    String saveFilePath = "";//todo


    ArrayAdapter<String> fileListAdapter;

//    AdapterView.OnItemSelectedListener onItemSelectedListener=new AdapterView.OnItemSelectedListener() {
//        @Override
//        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//            Log.d(TAG,"onItemSelected "+position);//never called
//        }
//
//        @Override
//        public void onNothingSelected(AdapterView<?> parent) {
//
//        }
//    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Intent intent = getIntent();
        filePath = intent.getStringExtra(FILESTR);

        listView = getListView();

        SortedMap<String, Charset> charsetSortedMap = Charset.availableCharsets();
        Set<String> charsetNames = charsetSortedMap.keySet();
        //todo add recently used charset on top
        charsetList = charsetNames.toArray(charsetList);

        String settings_file = getString(R.string.settings_file);
        String text_binary_key = getString(R.string.text_binary_key);
        SharedPreferences sharedPreferences = getSharedPreferences(settings_file, Context.MODE_PRIVATE);
        String textBinaryDisplay = sharedPreferences.getString(text_binary_key, "text");
        displayAsBinary = (!textBinaryDisplay.equals("text"));
        if (filePath == null) {
            createNewTextFile();
        } else {
            readFile();
        }
        //getListView().setOnItemSelectedListener(onItemSelectedListener);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putCharSequence(FILEPATH, filePath);
        outState.putCharSequence(CURRCHARSET, currCharset);
        outState.putStringArrayList(TEXTARRAYLIST, textArrayList);
        int firstVisiblePosition = this.getListView().getFirstVisiblePosition();
        outState.putInt(FIRSTPOSITION, firstVisiblePosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        filePath = state.getString(FILEPATH);
        currCharset = state.getString(CURRCHARSET);
        textArrayList = state.getStringArrayList(TEXTARRAYLIST);

        fileListAdapter = new TextFileListAdapter(this, R.layout.editor_line, R.id.itemLine, toStringArray(textArrayList));
        setListAdapter(fileListAdapter);
        setTitle(filePath);

        int firstVisiblePosition = state.getInt(FIRSTPOSITION);
        this.getListView().setSelectionFromTop(firstVisiblePosition, 0);
    }

    @Override
    protected void onDestroy() {
        changeCharset();
        super.onDestroy();
    }

    private void createNewTextFile() {
        //todo
    }

    private void readFile() {

        if (displayAsBinary) {
            //todo
        } else {
            readFileAsText();
        }
    }

    private void readFileAsText() {
        //todo for large files, use LineNumberReader
        try {
            BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), currCharset));
            String line = "";
            textArrayList = new ArrayList<String>();
            while ((line = buf.readLine()) != null) {
                textArrayList.add(line);
            }

            fileListAdapter = new TextFileListAdapter(this, R.layout.editor_line, R.id.itemLine, toStringArray(textArrayList));

            setListAdapter(fileListAdapter);
            setTitle(filePath);
        } catch (FileNotFoundException fnfe) {
            //Toast.makeText(this, "Cannot read file. File does not exist.", Toast.LENGTH_SHORT).show();
            fnfe.printStackTrace();
        } catch (IOException ioe) {
//            Toast.makeText(this, "IO Error reading file.", Toast.LENGTH_SHORT).show();
            ioe.printStackTrace();

        }

    }


    @Override
    protected void onListItemClick(ListView l, View v, final int position, long id) {
        //super.onListItemClick(l, v, position, id);
        //// TODO: 8/30/2015 if edit is true, edit selected line,
        Log.d(TAG, "onListItemClick " + position);
        Toast.makeText(this, "onListItemClick " + v.getId(), Toast.LENGTH_SHORT).show();
        Log.wtf(TAG, "onListItemClick " + v.getId());
//        View view=(View) getListView().getItemAtPosition(position);
//
//        EditText editText=(EditText) view.findViewById(R.id.itemLine);
//        //editText.setTag(editText.getKeyListener());
//        editText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                String temp=s.toString();
//                if(temp.contains("\n")){
//                    int newlineIndex=temp.indexOf("\n");
//                    textArrayList.set(position, temp.substring(0,newlineIndex));
//                    textArrayList.add(position+1, temp.substring(newlineIndex+1));
//                }else {
//                    textArrayList.set(position, temp);
//                    Log.d(TAG,"onTextChanged "+temp+" "+position );
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                fileListAdapter.notifyDataSetChanged();
//                Log.d(TAG, "afterTextChanged");
//            }
//        });
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

    private String[] toStringArray(ArrayList<String> temp) {
        if (temp == null) {
            return null;
        }
        String[] arr = new String[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            arr[i] = temp.get(i);
        }
        return arr;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        //changed favorites from preferences to file
//        String favoritePrefFile = getString(R.string.favorites_file);
//        String favoriteNumKey = getString(R.string.num_favorites_key);
//        String prefKey=getString(R.string.favorites);
//        SharedPreferences sharedPreferences = getSharedPreferences(favoritePrefFile, Context.MODE_PRIVATE);
//        int num_favorites = sharedPreferences.getInt(favoriteNumKey, 0);
//        String tempStr="";
//        for(int i=0;i<num_favorites;i++){
//            tempStr = sharedPreferences.getString(prefKey + i, "");
//            //Toast.makeText(this,"File "+i+" "+tempStr,Toast.LENGTH_SHORT).show();
//            if(tempStr !=null && tempStr.equals(filePath)){
//                MenuItem favoriteMenuItem= (MenuItem) findViewById(R.id.action_favorites);
//                favoriteMenuItem.setChecked(true);
//                break;
//            }
//        }

        int max_favorites=getResources().getInteger(R.integer.max_favorites);
        File file = new File(this.getFilesDir(), getString(R.string.favorites_text));
        try {
            BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = "";
            int i = 1;
            String tempStr = "";
            while ((line = buf.readLine()) != null && i < max_favorites) {
                tempStr = line.substring(0, line.indexOf(";"));
                if (tempStr != null && tempStr.equals(filePath)) {
                    MenuItem favoriteMenuItem = (MenuItem) findViewById(R.id.action_favorites);
                    favoriteMenuItem.setChecked(true);
                    break;
                }
                i++;
            }
        } catch (FileNotFoundException fnfe) {
            //Toast.makeText(this, "Cannot read file. File does not exist.", Toast.LENGTH_SHORT).show();
            fnfe.printStackTrace();
        } catch (IOException ioe) {
//            Toast.makeText(this, "IO Error reading file.", Toast.LENGTH_SHORT).show();
            ioe.printStackTrace();

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
            Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_charsets) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Charset")
                    .setItems(charsetList, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currCharset = charsetList[which];
                            readFile();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }
        if (id == R.id.action_favorites) {
            if (item.isChecked()) {
                removeFromFavorites();
                item.setChecked(false);
            } else {
                addToFavorites();
                item.setChecked(true);
            }
        }
        //todo binary hex mode, text size
        //todo edit/view
        //todo save
        return super.onOptionsItemSelected(item);
    }

    private void changeCharset() {
        //todo change charset in recent file, directories and favorites
        //call on close
    }

    
    private void addToFavorites(){
//        String favoritePrefFile=getString(R.string.favorites_file);
//        String favoriteNumKey=getString(R.string.num_favorites_key);
////        String prefKey=getString(R.string.favorites);
//        SharedPreferences sharedPreferences = getSharedPreferences(favoritePrefFile, Context.MODE_PRIVATE);
//        int num_favorites=sharedPreferences.getInt(favoriteNumKey, 0);
//        num_favorites++;
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(favoriteNumKey, num_favorites);

        String prefFile=getString(R.string.favorites_text);
        File file = new File(this.getFilesDir(), prefFile);
        FileOutputStream outputStream;

        HashMap<String,String> favoritesPairs;
        favoritesPairs = new HashMap<String, String>();

        String curr=filePath+";";
        boolean currExists=false;

        favoritesPairs.put(filePath.substring(filePath.lastIndexOf("/")+1), curr+currCharset+"\n");
        try {
            if(file.exists()) {
                BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line = "";
                int i = 1;
                while ((line = buf.readLine()) != null) {
                    if(!line.contains(curr)) {
                        favoritesPairs.put(line.substring(line.lastIndexOf("/") + 1, line.indexOf(";")), line);
                    }
                    i++;
                }
            }

            TreeSet<String> treeset = new TreeSet<String>(favoritesPairs.keySet());
            StringBuilder builder=new StringBuilder();

            for (String treekey : treeset) {
                builder.append(favoritesPairs.get(treekey));
                builder.append("\n");
            }
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
    
    private void removeFromFavorites(){
//        String favoritePrefFile=getString(R.string.favorites_file);
//        String favoriteNumKey=getString(R.string.num_favorites_key);
////        String prefKey=getString(R.string.favorites);
//        SharedPreferences sharedPreferences = getSharedPreferences(favoritePrefFile, Context.MODE_PRIVATE);
//        int num_favorites=sharedPreferences.getInt(favoriteNumKey, 0);
//        num_favorites=(num_favorites>0)?num_favorites-1:0;
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(favoriteNumKey, num_favorites);

        String prefFile=getString(R.string.favorites_text);
        File file = new File(this.getFilesDir(), prefFile);
        FileOutputStream outputStream;
        StringBuilder builder=new StringBuilder();

        try {
            BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line="";
            int i=1;
            String search=filePath+";";
            while ((line = buf.readLine()) != null ) {
                if(line.indexOf(search)==-1){
                    builder.append(line);
                    builder.append("\n");
                }
                i++;
            }

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

    class LineTextWatcher implements TextWatcher{
        public int lineNum;

        public LineTextWatcher(int l){
            lineNum=l;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            Log.d(TAG, "beforeTextChanged "+s+" "+start+" "+count+" "+after);

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String temp=s.toString();
            if(temp.contains("\n")){
                int newlineIndex=temp.indexOf("\n");
                textArrayList.set(lineNum, temp.substring(0,newlineIndex));
                textArrayList.add(lineNum + 1, temp.substring(newlineIndex + 1));
                fileListAdapter.notifyDataSetChanged();
                //// TODO: 8/31/2015 move cursor to next line
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

    /**
     * LineEditText.OnInputConnectionInteraction interface
     * @param lineNum
     */
    @Override
    public void backspaceAtPositionZero(int lineNum) {
        //// TODO: 9/13/2015
    }

    static class ViewHolder {
        LineEditText vhEditText;
        TextView vhNumLine;
//        int vhposition;
    }

    class BinaryFileListAdapter extends ArrayAdapter<String>{

        public BinaryFileListAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
            super(context, resource, textViewResourceId, objects);
        }
    }

    class TextFileListAdapter extends ArrayAdapter<String>{//just display line numbers all the time

        ViewHolder holder;

        public TextFileListAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
            super(context, resource, textViewResourceId, objects);
        }



        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            View row;
//            row = inflater.inflate(R.layout.editor_line,listView,true);
//
//            TextView textview= (EditText) row.findViewById(R.id.itemLine);
//            TextView lineNumView= (TextView) row.findViewById(R.id.lineNum);



//            if(convertView == null){
                LayoutInflater inflater  = (LayoutInflater)getApplicationContext().getSystemService
                        (Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.editor_line, null);
                holder = new ViewHolder();
                holder.vhEditText = (LineEditText) convertView.findViewById(R.id.itemLine);
                holder.vhNumLine = (TextView) convertView.findViewById(R.id.lineNum);
                convertView.setTag(holder);
//            }else{
//                holder = (ViewHolder)convertView.getTag();
//            }

            holder.vhNumLine.setText(String.valueOf(position + 1));

            holder.vhEditText.setText(textArrayList.get(position));
            holder.vhEditText.lineNum=position;


//
//            holder.vhEditText.post(new Runnable() {
//                @Override
//                public void run() {
//                    int lineCnt = holder.vhEditText.getLineCount();
//                    // Perform any actions you want based on the line count here.
//                    holder.vhEditText.setHeight(holder.vhEditText.getLineHeight() * (lineCnt - 2));
//                    Log.d(TAG, "post "+ lineCnt +holder.vhEditText.getText());
//                }
//            });


//            You will want to look at the android:imeActionId and android:imeOptions attributes, plus the setOnEditorActionListener() method, all on TextView.
            holder.vhEditText.addTextChangedListener(new LineTextWatcher(position));
            holder.vhEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {//todo probably don't need this
                        EditText editText = (EditText) v;
                        Log.d(TAG, "onFocusChange " + editText.getSelectionStart()+editText.getText());
                        if (editText.getSelectionStart() == 0) {

                        }
                    }
                }
            });
            return convertView;




//            lineNumView.setText(String.valueOf(position + 1));
//
//            textview.setText("\n" + textArrayList.get(position) + "\n");
//            textview.setHeight(textview.getLineHeight() * 2);
//            textview.setGravity(Gravity.BOTTOM);
//            Log.d(TAG,"lineHeight "+textview.getHeight()+", "+textview.getWidth()+", "+textview.getLineHeight()+", "+textview.getLineCount());

            //textview.setHeight(textview.getHeight()/2);
//            textview.getLineHeight();
//            textview.getLineSpacingExtra()
            //textview.setHeight(textview.getHeight()-textview.getLineHeight());
            //int y = (textview.getLineCount() - 1) * textview.getLineHeight(); // the " - 1" should send it to the TOP of the last line, instead of the bottom of the last line
            //textview.scrollTo(0, y);

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

//            textview.addTextChangedListener(new LineTextWatcher(position));
//            textview.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View v, boolean hasFocus) {
//                    if (hasFocus) {
//                        EditText editText = (EditText) v;
//                        Log.d(TAG, "onFocusChange " + editText.getSelectionStart());
//                        if (editText.getSelectionStart() == 0) {
//                            editText.setSelection(1, 1);
//                        }
//                    }
//                }
//            });
//            return row;
        }
    }
}
