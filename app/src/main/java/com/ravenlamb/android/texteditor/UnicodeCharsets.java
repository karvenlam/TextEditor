package com.ravenlamb.android.texteditor;

/**
 * Created by kl on 3/9/2016.
 * unicode planes to language
 */
public class UnicodeCharsets {
    /**
     * HashMap<String, List<Integer>> map = new HashMap<String, List<Integer>>();
     * HashMap<String, int[]> map = new HashMap<String, int[]>();
     *
     * hashmap
     * unicode planes, plane names, plane ranges
     *
     * common non unicode charset names, corresponding language name
     *
     * unicode plane names, language name,
     *
     *
     private static final Map<Integer, String> myMap;
     static {
     Map<Integer, String> aMap = ....;
     aMap.put(1, "one");
     aMap.put(2, "two");
     myMap = Collections.unmodifiableMap(aMap);
     }
     */
    public static final String[] PLANE_NAMES=new String[]{"Basic Latin"};

    public static final int BASIC_LATIN=0;

    //plane ranges


    /**
     *
     * @param input string with any charset
     * @return list of most probable charsets
     */
    public static String[] returnStringCharCodes(String input, String language){
        return null;
    }

    public static String returnCharLanguage(char c){//override with string
        return "";
    }
}
