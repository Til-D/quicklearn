package com.tilmanification.quicklearn;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StatisticsActivity extends Fragment {

    // ========================================================================
    // Constant Fields
    // ========================================================================

    private static final String	TAG	= StatisticsActivity.class.getSimpleName();

    // ========================================================================
    // Class Variables
    // ========================================================================

    private View statisticsView;
    private TextView textStatisticsTotal;
    private TextView textStatisticsPct;
    private TextView textVocabularySize;

    // ========================================================================
    // Methods
    // ========================================================================


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        statisticsView = inflater.inflate(R.layout.activity_statistics, container, false);

        textStatisticsTotal = (TextView) statisticsView.findViewById(R.id.text_statistics_total);
        textStatisticsPct = (TextView) statisticsView.findViewById(R.id.text_statistics_pct);
        textVocabularySize = (TextView) statisticsView.findViewById(R.id.text_statisics_vocabulary_size);
        int size = StudyManager.getInstance(getActivity()).vocabulary.getWordlistSize();
        textVocabularySize .setText(String.format(getActivity().getString(R.string.text_statisics_vocabulary_size), size));

        //TODO: retrieve statistics and update text
        textStatisticsPct.setText("");

        return statisticsView;
    }
}
