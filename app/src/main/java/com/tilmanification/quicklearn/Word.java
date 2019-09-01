package com.tilmanification.quicklearn;

import java.util.Date;

/**
 * Created by tilman on 06/09/15.
 */
public class Word {
    public String key;

    public String original; //untranslated
    public String translation;
    public String source_language;
    public String target_language;
    public int times_shown; //how many times seen by user
    public int count_correct;
    public int count_wrong;
    public Date first_seen;
    public Date last_seen;

    public Word(String key, String original, String translation, String source_language, String target_language) {
        this.key = key;

        this.original = original;
        this.translation = translation;
        this.source_language = source_language;
        this.target_language = target_language;
        this.times_shown = 0;
        this.count_correct = 0;
        this.count_wrong = 0;
    }

    public String toString() {
        return this.key;
    }
}
