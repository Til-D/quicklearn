package com.tilmanification.quicklearn;

import android.content.Context;
import android.util.Log;

import com.tilmanification.quicklearn.log.QLearnJsonLog;
import com.tilmanification.quicklearn.log.QlearnKeys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import static com.tilmanification.quicklearn.StudyManager.NUMBER_OF_WORDS_ADDED_VOCAB_EXTENSION;
import static com.tilmanification.quicklearn.StudyManager.SHUFFLE_VOCABULARY;

/**
 * Created by tilman on 14/07/15.
 *
 * manages all study-related parameters
 */
public class StudyManager {

    private static final String			TAG					= StudyManager.class.getSimpleName();

    public static final int CONDITION_FLASHCARD = 0;
    public static final int CONDITION_MULTIPLE_CHOICE = 1;
    public static final boolean RANDOMIZE_STARTING_CONDITION = false;
    public static final int STARTING_CONDITION = CONDITION_FLASHCARD;
    public static final int WORDS_PER_SET = 3;
    public static final int DAYS_PER_CONDITION = 3;
    public static final long ONE_MINUTE_IN_MILLIS = 60000;//ms
    public static final int SURVEY_TRIGGERED_PER_APP_STARTS = 3; //every N app starts the survey will be triggered
    public static final boolean SHUFFLE_VOCABULARY = false;
    public static final int DAY_INTERVAL_FOR_VOCAB_EXTENSION= 7; //days after new words are added to the vocabulary
    public static final int NUMBER_OF_WORDS_ADDED_VOCAB_EXTENSION = 120; //120;


    private Context context;

    public int current_condition;
    public int session_word_count;
    public int session_set_count;
    public Date condition_started;
    public Vocabulary vocabulary;

    private static StudyManager instance = null;

    private StudyManager() {
        // Exists only to defeat instantiation.
    }

    public static StudyManager getInstance(Context context) {
        if(instance == null) {
            instance = new StudyManager(context);
        }
        return instance;
    }

    private StudyManager(Context context) {
        this.context = context;

        if(QuickLearnPrefs.DEBUG_MODE) {
            Util.put(context, QuickLearnPrefs.PREF_REINITIALIZE_VOCABLUARY, true);
            Util.putInt(context, QuickLearnPrefs.PREF_STUDY_CONDITION, CONDITION_FLASHCARD);
        }

        int startCondition = STARTING_CONDITION;
        if(RANDOMIZE_STARTING_CONDITION) {
            //assign start condition by chance, in case not determined yet
            ArrayList<Integer> startConditions = new ArrayList<Integer>();
            startConditions.add(StudyManager.CONDITION_FLASHCARD);
            startConditions.add(StudyManager.CONDITION_MULTIPLE_CHOICE);
            startCondition = startConditions.get(new Random().nextInt(startConditions.size()));
        }

        Date now = new Date();
        startCondition = Util.getInt(context, QuickLearnPrefs.PREF_STUDY_CONDITION, startCondition);
        long conditionStarted = Util.getLong(context, QuickLearnPrefs.PREF_DATE_STUDY_CONDITION_STARTED_MS, System.currentTimeMillis());
        long last_survey = Util.getLong(context, QuickLearnPrefs.PREF_DATE_LAST_SURVEY_MS, System.currentTimeMillis());

        if(QuickLearnPrefs.DEBUG_MODE) {
            if (startCondition == StudyManager.CONDITION_FLASHCARD) {
                Log.i(TAG, "Study Condition: Flashcard");
            } else if (startCondition == StudyManager.CONDITION_MULTIPLE_CHOICE) {
                Log.i(TAG, "Study Condition: Multiple Choice");
            } else {
                Log.e(TAG, "Study Condition could not be assigned.");
            }
        }
        this.current_condition = startCondition;
        this.condition_started = Util.getDateFromTimestamp(conditionStarted);

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "Condition: " + this.current_condition + ", condition started: " + this.condition_started.toString());
        }

        String sourceLanguage = Util.getString(context, QuickLearnPrefs.PREF_LANG_SOURCE, "unknown");
        String targetLanguage = Util.getString(context, QuickLearnPrefs.PREF_LANG_TARGET, "unknown");

        this.vocabulary = new Vocabulary(context, sourceLanguage, targetLanguage);

        //reset vocab init request and save conditions
        Util.put(context, QuickLearnPrefs.PREF_REINITIALIZE_VOCABLUARY, false);
        Util.putInt(context, QuickLearnPrefs.PREF_STUDY_CONDITION, this.current_condition);
        Util.putLong(context, QuickLearnPrefs.PREF_DATE_STUDY_CONDITION_STARTED_MS, this.condition_started.getTime());
        Util.putLong(context, QuickLearnPrefs.PREF_DATE_LAST_SURVEY_MS, last_survey);

        resetWordCount();
    }

    /**
     * checks whether study_condition should be updated, DAYS_PER_CONDITION days in each condition
     */
    public void updateStudyCondition(int condition) {
        int cond = Util.getInt(context, QuickLearnPrefs.PREF_STUDY_CONDITION, current_condition);

        if(cond!=condition) {

            Date now = new Date();
            Date condition_started = Util.getDateFromTimestamp(Util.getLong(context, QuickLearnPrefs.PREF_DATE_STUDY_CONDITION_STARTED_MS, System.currentTimeMillis()));
            long days_passed = Util.getDaysPassedSince(condition_started);

            current_condition = condition;
            condition_started = now;
            Util.putInt(context, QuickLearnPrefs.PREF_STUDY_CONDITION, current_condition);
            Util.putLong(context, QuickLearnPrefs.PREF_DATE_STUDY_CONDITION_STARTED_MS, condition_started.getTime());

            QLearnJsonLog.onQLearnConditionSwitch(cond, current_condition, days_passed);

            if (QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "updateStudyCondition(): new condition: " + current_condition + ", today: " + now + ", days in last condition: " + days_passed);
            }

        } else {
            if (QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "updateCondition(): no changes made.");
            }
        }

    }

    /**
     * deprecated function to switch condition after x days
     */
//    public void switchStudyConditionAfterTimeout() {
//        int cond = Util.getInt(context, QuickLearnPrefs.PREF_STUDY_CONDITION, current_condition);
//
//        Date now = new Date();
//        Date condition_started = Util.getDateFromTimestamp(Util.getLong(context, QuickLearnPrefs.PREF_DATE_STUDY_CONDITION_STARTED_MS, System.currentTimeMillis()));
//        long days_passed = Util.getDaysPassedSince(condition_started);
//
//        boolean switch_cond = false;
//        if(days_passed >= DAYS_PER_CONDITION) { //change study condition
//            switch_cond = true;
//
//            switch(cond) {
//                case CONDITION_FLASHCARD:
//                    current_condition = CONDITION_MULTIPLE_CHOICE;
//                    break;
//                default:
//                    current_condition = CONDITION_FLASHCARD;
//                    break;
//            }
//
//            condition_started = now;
//            Util.putInt(context, QuickLearnPrefs.PREF_STUDY_CONDITION, current_condition);
//            Util.putLong(context, QuickLearnPrefs.PREF_DATE_STUDY_CONDITION_STARTED_MS, condition_started.getTime());
//            QLearnJsonLog.onQLearnConditionSwitch(cond, current_condition, days_passed);
//        }
//
//        if(QuickLearnPrefs.DEBUG_MODE) {
//            Log.i(TAG, "updateStudyCondition: condition_started + " + condition_started + ", today: " + now + ", days since start: " + days_passed + ", swith condition: " + switch_cond + ", condition: " + current_condition);
//        }
//
//    }

    public void resetWordCount() {
        session_word_count = 0;
        session_set_count = 0;
        Util.putInt(context, QuickLearnPrefs.QLEARN_SESSION_INDEX, 0);
    }

}

/**
 * holds exactly one vocabulary of words from a specific source to a specific target language
 */
class Vocabulary {

    private static final String			TAG					= Vocabulary.class.getSimpleName();

    private Context context;
    private LanguageDictionary dictionary;
    public String source_language;
    public String target_language;

    private Word[] wordlist;
    private ArrayList<Integer> leitener_indices;
    private ArrayList<String> words_seen_flashcard;
    private ArrayList<String> words_seen_multiple_choice;

    public Vocabulary(Context context, String source_language, String target_language) {

        this.context = context;
        this.source_language = source_language;
        this.target_language = target_language;

        this.dictionary = new LanguageDictionary(context);
        initWordlist();
    }

    /**
     *
     * reads in words from the dictionary, creates a (shuffled) array of words and the corresponding array of indices
     * @returns an array of words with fixed positions
     */
    private void initWordlist() {
        ArrayList<String> keys = dictionary.getAllKeys();
        ArrayList<Word> words = new ArrayList<Word>();

        boolean constructNewIndices = false;
        String indices_serialized = Util.getString(context, QuickLearnPrefs.PREF_LEITENER_INDICES, "");
        if (indices_serialized.equals("") || Util.getBool(context, QuickLearnPrefs.PREF_REINITIALIZE_VOCABLUARY, false)) {
            Util.putString(context, QuickLearnPrefs.PREF_WORDS_SEEN_FLASHCARD, "");
            Util.putString(context, QuickLearnPrefs.PREF_WORDS_SEEN_MULTIPLE_CHOICE, "");
            constructNewIndices = true;
        } else {
            //parse leitener_indices
            String[] elements = indices_serialized.substring(1, indices_serialized.length() - 1).split(", ");
            leitener_indices = new ArrayList<Integer>(elements.length);
            for (String item : elements) {
                leitener_indices.add(Integer.valueOf(item));
            }
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.i(TAG, "leitener_indices restored: " + leitener_indices.toString());
            }
        }

        if (constructNewIndices) {
            leitener_indices = new ArrayList<Integer>();
        }

        Iterator<String> iter = keys.iterator();
//        for (int i = 0; i <= keys.size(); i++) {
        int index = 0;
//        for(String key : keys) {
        while(iter.hasNext() && index < NUMBER_OF_WORDS_ADDED_VOCAB_EXTENSION) {
            String key = iter.next();
            String original = dictionary.getTranslation(key, source_language);
            String translation = dictionary.getTranslation(key, target_language);

            Word word = new Word(key, original, translation, source_language, target_language);
            words.add(word);
            if (constructNewIndices) {
                leitener_indices.add(index);
                Util.putLong(context, QlearnKeys.QLEARN_DATE_LAST_VOCAB_EXTENSION, System.currentTimeMillis());
            }
            index++;
        }
        if(SHUFFLE_VOCABULARY) {
            Collections.shuffle(words);
        }

        wordlist = Arrays.copyOf(words.toArray(), words.size(), Word[].class);

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "length of wordlist: " + wordlist.length);
            Log.i(TAG, "leitener indices: " + leitener_indices.toString());
        }

        //OLD functions to separate review modes and the vocabulary associated with each mode
        //restore words_seen_flashcard
        String words_seen_flashcard_serialized = Util.getString(context, QuickLearnPrefs.PREF_WORDS_SEEN_FLASHCARD, "");
        words_seen_flashcard = new ArrayList<String>();
        if(!words_seen_flashcard_serialized.equals("")) {
            String[] arr_words_seen = words_seen_flashcard_serialized.substring(1, words_seen_flashcard_serialized.length() - 1).split(", ");
            for (String item : arr_words_seen) {
                words_seen_flashcard.add(item);
            }
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "words_seen_flashcard restored: " + words_seen_flashcard.toString());
            }
        }

        //restore words_seen_multiple_choice
        String words_seen_multiple_choice_serialized = Util.getString(context, QuickLearnPrefs.PREF_WORDS_SEEN_MULTIPLE_CHOICE, "");
        words_seen_multiple_choice = new ArrayList<String>();
        if(!words_seen_multiple_choice_serialized.equals("")) {
            String[] arr_words_seen = words_seen_multiple_choice_serialized.substring(1, words_seen_multiple_choice_serialized.length() - 1).split(", ");
            for (String item : arr_words_seen) {
                words_seen_multiple_choice.add(item);
            }
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "words_seen_multiple_choice restored: " + words_seen_multiple_choice.toString());
            }
        }
    }

    /**
     * can be periodically called to extend the current wordlist by NUMBER_OF_WORDS_ADDED_VOCAB_EXTENSION
     */
    public void extendWordlist() {

        if(QuickLearnPrefs.DEBUG_MODE)
            Log.i(TAG, "extendWordlist: " + wordlist.length + " by " + NUMBER_OF_WORDS_ADDED_VOCAB_EXTENSION);

        ArrayList<String> keys = dictionary.getAllKeys();
        ArrayList<Word> words = new ArrayList<Word>(Arrays.asList(wordlist));

        int previousSize = words.size();

        int keyIndex = wordlist.length;
        for(int i=0; i<NUMBER_OF_WORDS_ADDED_VOCAB_EXTENSION; i++) {
            if(keyIndex >= keys.size()) {
                if(QuickLearnPrefs.DEBUG_MODE) {
                    Log.i(TAG, "no more words to be added (" + wordlist.length + ")");
                }
                break;
            }
            String key = keys.get(keyIndex);
            String original = dictionary.getTranslation(key, source_language);
            String translation = dictionary.getTranslation(key, target_language);

            Word word = new Word(key, original, translation, source_language, target_language);
            words.add(word);

            if(QuickLearnPrefs.DEBUG_MODE)
                Log.i(TAG, "adding word: " + key);

            leitener_indices.add(i, keyIndex);
            keyIndex++;
        }

        wordlist = Arrays.copyOf(words.toArray(), words.size(), Word[].class);
        Util.putLong(context, QlearnKeys.QLEARN_DATE_LAST_VOCAB_EXTENSION, System.currentTimeMillis());
        QLearnJsonLog.onVocabularyExtensionTriggered(previousSize, wordlist.length);

        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "new wordlist with " + wordlist.length + " items.");
        }

    }

    public int getWordlistSize() {
        return wordlist.length;
    }

    /**
     *
     * @returns the next word to be re-visited
     */
    public Word nextWord() {
        //safety guard
        if(leitener_indices.size()<=0) {
            if(QuickLearnPrefs.DEBUG_MODE) {
                Log.e(TAG, "No further indices: re-init vocabulary.");
            }
            initWordlist();
            return nextWord();
        } else {
//            int condition = StudyManager.getInstance(context).current_condition;
            Word word = wordlist[leitener_indices.get(0)]; //getNextWordInCondition(condition);
            Util.putString(context, QuickLearnPrefs.PREF_CURRENT_WORD, word.key);
            return word;
        }

    }

//    private Word getNextWordInCondition(int condition) {
//
//        if(QuickLearnPrefs.DEBUG_MODE) {
//            Log.i(TAG, "getNextWordInCondition: condition: " + condition);
//            Log.i(TAG, "words_seen_flashcard: " + words_seen_flashcard.toString());
//            Log.i(TAG, "words_seen_multiple_choice: " + words_seen_multiple_choice.toString());
//            Log.i(TAG, "leitener_indices: " + leitener_indices.toString());
//            Log.i(TAG, "wordlist: " + wordlist.length + "words");
//        }
//
//        Word word = null;
//        int index = 0;
//
//        switch(condition) {
//            case StudyManager.CONDITION_FLASHCARD:
//                for(int i=0; i<leitener_indices.size(); i++) {
//                    Word tmp = wordlist[leitener_indices.get(i)];
//                    if(!words_seen_multiple_choice.contains(tmp.key)) {
//                        break;
//                    }
//                    index++;
//                }
//                break;
//            default:
//                for(int i=0; i<leitener_indices.size(); i++) {
//                    Word tmp = wordlist[leitener_indices.get(i)];
//                    if(!words_seen_flashcard.contains(tmp.key)) {
//                        break;
//                    }
//                    index++;
//                }
//                break;
//        }
//
////        Log.i(TAG, "leitener_indices size: " + leitener_indices.size());
////        Log.i(TAG, "index: " + index);
//        //edge case: all words in one single condition >> reset all word_seen lists
//        if(index>=leitener_indices.size()-1) {
////            Log.i(TAG, "edge case: reset!");
//            words_seen_flashcard = new ArrayList<String>();
//            Util.putString(context, QuickLearnPrefs.PREF_WORDS_SEEN_FLASHCARD, words_seen_flashcard.toString());
//
//            words_seen_multiple_choice = new ArrayList<String>();
//            Util.putString(context, QuickLearnPrefs.PREF_WORDS_SEEN_MULTIPLE_CHOICE, words_seen_multiple_choice.toString());
//
//            QLearnJsonLog.onQLearnConditionWordsReset(StudyManager.getInstance(context).current_condition);
//        }
//
//        //re-arrange leitener_indices
//        for(int i=0; i<index; i++) {
//            leitener_indices.add(leitener_indices.get(0));
//            leitener_indices.remove(0);
//        }
//
//        if(QuickLearnPrefs.DEBUG_MODE) {
//            Log.i(TAG, "leitener_indices: " + leitener_indices.toString());
//            Log.i(TAG, "next word: " + wordlist[leitener_indices.get(0)]);
//        }
//        return wordlist[leitener_indices.get(0)];
//    }

    /**
     *
     * @param count: specifies the number of words to be returned
     * @param blacklist: ArrayList of keys(String) that are off-limits
     *
     * @returns an ArrayList with #count number of random words from the dictionary
     */
    public ArrayList<Word> getRandomWords(int count, ArrayList<String> blacklist) {
        ArrayList<Word> resultWords = new ArrayList<Word>();
        ArrayList<String> words = dictionary.getAllKeys();
        Random rand = new Random(System.currentTimeMillis());
        while(count>0) {
            String key;
            int index = rand.nextInt(words.size());
            Iterator<String> iter = words.iterator();
            for (int i = 0; i < index; i++) {
                iter.next();
            }
            //check whether key in blacklist
            do {
                key = iter.next();
            } while (blacklist.contains(key) && iter.hasNext());

            String original = dictionary.getTranslation(key, source_language);
            String translation = dictionary.getTranslation(key, target_language);
            resultWords.add(new Word(key, original, translation, source_language, target_language));
            blacklist.add(key);
            count--;
        }

        return resultWords;
    }

    public void updateWordList(boolean guessedCorrectly) {
//        Log.i(TAG, "leitender_indices BEFORE: " + leitener_indices.toString());
        //get next word index, check for uniqueness and remove from index list
        int nextWordIndex = leitener_indices.get(0);

        //check if index is unique
        if(Util.isUnique(nextWordIndex, leitener_indices)) {
//            Log.i(TAG, "word_index: " + nextWordIndex + " is unique.");

            if(guessedCorrectly) {
                //append to the end
                leitener_indices.add(nextWordIndex);
            } else {
                //replicate to position 4, 10, 20, 40, ...
                leitener_indices.add(4, nextWordIndex);
                int index = 10;
                while (index <= leitener_indices.size()) {
                    leitener_indices.add(index, nextWordIndex);
                    index = index * 2;
                }
            }
        } else {
//            Log.i(TAG, "word_index: " + nextWordIndex + " is NOT unique.");
        }
        leitener_indices.remove(0);
//        Log.i(TAG, "leitender_indices AFTER: " + leitener_indices.toString());
        //save leitener_indices
        Util.putString(context, QuickLearnPrefs.PREF_LEITENER_INDICES, leitener_indices.toString());
    }

    /**
     * checks whether words was seen before, if not word is added to words_seen list
     * @param word_key
     * @return
     */
    public boolean wordSeenBefore(String word_key) {
        if(QuickLearnPrefs.DEBUG_MODE) {
            Log.i(TAG, "wordSeenBefore: " + word_key);
        }
        if(words_seen_flashcard.contains(word_key) || words_seen_multiple_choice.contains(word_key)) {
            return true;
        } else {
            switch(StudyManager.getInstance(context).current_condition) {
                case(StudyManager.CONDITION_FLASHCARD):
                    words_seen_flashcard.add(word_key);
                    Util.putString(context, QuickLearnPrefs.PREF_WORDS_SEEN_FLASHCARD, words_seen_flashcard.toString());
                    if(QuickLearnPrefs.DEBUG_MODE) {
                        Log.i(TAG, "!wordSeenBefore, added to words_seen_flashcard: " + words_seen_flashcard);
                    }
                    break;
                default:
                    words_seen_multiple_choice.add(word_key);
                    Util.putString(context, QuickLearnPrefs.PREF_WORDS_SEEN_MULTIPLE_CHOICE, words_seen_multiple_choice.toString());
                    if(QuickLearnPrefs.DEBUG_MODE) {
                        Log.i(TAG, "!wordSeenBefore, added to words_seen_multiple_choice: " + words_seen_multiple_choice);
                    }
                    break;
            }
            return false;
        }
    }
}