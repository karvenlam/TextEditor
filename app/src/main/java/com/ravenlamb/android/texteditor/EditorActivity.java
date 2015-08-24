package com.ravenlamb.android.texteditor;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

    public final static String FILESTR="com.ravenlamb.android.texteditor.EditorActivity.FILESTR";

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


        if(filePath==null){
            createNewTextFile();
            return;
        }

        String settings_file=getString(R.string.settings_file);
        String text_binary_key=getString(R.string.text_binary_key);
        SharedPreferences sharedPreferences = getSharedPreferences(settings_file, Context.MODE_PRIVATE);
        String textBinaryDisplay = sharedPreferences.getString(text_binary_key, "text");
        displayAsBinary= (!textBinaryDisplay.equals("text"));

        refreshDisplay();
    }

    private void createNewTextFile() {
    }

    private void refreshDisplay(){

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
            setTitle(R.string.app_name);
        }catch (FileNotFoundException fnfe){
            //Toast.makeText(this, "Cannot read file. File does not exist.", Toast.LENGTH_SHORT).show();
            fnfe.printStackTrace();
        }catch (IOException ioe){
//            Toast.makeText(this, "IO Error reading file.", Toast.LENGTH_SHORT).show();
            ioe.printStackTrace();

        }

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
        if (id == R.id.action_charsets) {

            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("Select Charset")
                    .setItems(charsetList, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currCharset=charsetList[which];
                            refreshDisplay();
                        }
                    });
            AlertDialog dialog=builder.create();
            dialog.show();
            return true;
        }
        //todo binary hex mode, text size
        //todo edit/view
        //todo save
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


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            View row= inflater.inflate(R.layout.editor_line,null,false);
            TextView textview= (TextView) row.findViewById(R.id.itemLine);
            TextView lineNumView= (TextView) row.findViewById(R.id.lineNum);

            textview.setText(textArrayList.get(position));
            lineNumView.setText(String.valueOf( position+1));
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
