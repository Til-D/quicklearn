package com.tilmanification.quicklearn;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by tilman on 10/07/15.
 *
 * creates the dictionary from a tab-separated word list
 * the first line (i.e. english) is the key fo the dictionary words
 */
public class LanguageDictionary {

    private static final String			TAG					= LanguageDictionary.class.getSimpleName();
    private static final String         DELIMITER           = "\t";

    private Context context;
    private String fileNameWordlist;
    private String encoding;
    private AssetManager am;

    public String[] languages;
    public Map<String, String[]> dictionary;

    public LanguageDictionary(Context context) {

        this.context = context;
        this.dictionary = new HashMap<String, String[]>();
        this.am = context.getAssets();
        this.fileNameWordlist = context.getResources().getString(R.string.file_wordlist);
        this.encoding = context.getResources().getString(R.string.wordlist_encoding);

        //read in languageDictionary
        try {
            Scanner scanner = new Scanner(context.getAssets().open(fileNameWordlist), encoding);
            String NL = System.getProperty("line.separator");

            //read languages
            if (scanner.hasNextLine()) {
                languages = scanner.nextLine().split(DELIMITER);
            }

            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(DELIMITER);
                if(line.length>1) {
                    dictionary.put(line[0], Arrays.copyOfRange(line, 0, line.length));
                }
            }

            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.d(TAG, "languageDictionary: " + dictionary.size() + " entries read (" + languages.length + " languages).");
            }

//            getTranslation("people", "spanish");

            scanner.close();
        } catch (Exception e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "could not find languageDictionary asset");
            }
            e.printStackTrace();
        }

    }

    public String getTranslation(String key, String language) {
        String translation = "";
        int index = Arrays.asList(languages).indexOf(language);
        if(index < 0) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "language not supported: " + language);
            }
        } else {
            String[] translations = dictionary.get(key);
            if(translations!=null) {
                translation = translations[index];
            }
        }
        return translation;
    }

    public ArrayList<String> getAllKeys() {

        ArrayList<String> keys = new ArrayList<String>();

        // return in the same order as source file
        try {
            Scanner scanner = new Scanner(context.getAssets().open(fileNameWordlist), encoding);
            String NL = System.getProperty("line.separator");

            //read languages
            if (scanner.hasNextLine()) {
                languages = scanner.nextLine().split(DELIMITER);
            }

            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(DELIMITER);
                if(line.length>1) {
                    keys.add(line[0]);
                }
            }
            scanner.close();

        } catch (Exception e) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "could not find languageDictionary asset");
            }
            e.printStackTrace();
        }

        return keys; // dictionary.keySet();
    }

}
