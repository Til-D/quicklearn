package com.tilmanification.quicklearn;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.tilmanification.quicklearn.log.QLearnJsonLog;
import com.tilmanification.quicklearn.log.QlearnKeys;

import java.util.ArrayList;
import java.util.Random;

//import org.pielot.borpred.BoredomPrediction;
//import org.pielot.borpred.BoredomPredictionService;

public class MultipleChoiceActivity extends android.app.Fragment {

    private static final int NUMBER_OF_MULTIPLE_CHOICES = 3;

    private View multipleChoiceView;

    private StudyManager studyManager;
    private Word current_word;

//	UI Elements

    //task
    private CardView container_task;
//    private TextView task_heading;
    private TextView text_translation_word;
    private Button button_option_a;
    private Button button_option_b;
    private Button button_option_c;
    private Button button_correct_option;

    //solution
    private CardView container_solution;
//    private TextView solution_heading;
    private TextView text_translation_result;
    private TextView text_translation_correct_incorrect;
    private Button button_continue;

    //finishScreen
    private CardView container_finish_screen;
//    private TextView finishing_screen_heading;
    private Button button_quit;
    private Button button_more_words;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        multipleChoiceView = inflater.inflate(R.layout.activity_multiple_choice, container, false);

        this.studyManager = StudyManager.getInstance(getActivity());

        String language = Util.capitalizeWords(studyManager.vocabulary.target_language);
        String heading = String.format(getResources().getString(R.string.notif_title), language);

        //task
        container_task = (CardView) multipleChoiceView.findViewById(R.id.container_task);
//        task_heading = (TextView) multipleChoiceView.findViewById(task_heading);
//        task_heading.setText(heading);
        text_translation_word = (TextView) multipleChoiceView.findViewById(R.id.text_translation_word);
        button_option_a = (Button) multipleChoiceView.findViewById(R.id.button_option_a);
        button_option_b = (Button) multipleChoiceView.findViewById(R.id.button_option_b);
        button_option_c = (Button) multipleChoiceView.findViewById(R.id.button_option_c);

        button_option_a.setOnClickListener(options_OnClickListener);
        button_option_b.setOnClickListener(options_OnClickListener);
        button_option_c.setOnClickListener(options_OnClickListener);


        //solution
        container_solution = (CardView) multipleChoiceView.findViewById(R.id.container_solution);
//        solution_heading= (TextView) multipleChoiceView.findViewById(solution_heading);
//        solution_heading.setText(heading);
        text_translation_result = (TextView) multipleChoiceView.findViewById(R.id.text_translation_result);
        text_translation_correct_incorrect = (TextView) multipleChoiceView.findViewById(R.id.text_translation_correct_incorrect);
        button_continue = (Button) multipleChoiceView.findViewById(R.id.button_continue);

        button_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int session_index = Util.getInt(getActivity(), QuickLearnPrefs.QLEARN_SESSION_INDEX, 0);
                session_index++;
                if(session_index == StudyManager.WORDS_PER_SET) {
                    Util.putInt(getActivity(), QuickLearnPrefs.QLEARN_SESSION_INDEX, 0);
                    showFinishScreen();
                } else {
                    Util.putInt(getActivity(), QuickLearnPrefs.QLEARN_SESSION_INDEX, session_index);
                    showTask();
                }
            }
        });

        //finishScreen
        container_finish_screen = (CardView) multipleChoiceView.findViewById(R.id.container_finish_screen);
//        finishing_screen_heading = (TextView) multipleChoiceView.findViewById(finishing_screen_heading);
//        finishing_screen_heading.setText(heading);
        button_quit = (Button) multipleChoiceView.findViewById(R.id.button_quit);
        button_more_words = (Button) multipleChoiceView.findViewById(R.id.button_more_words);

        button_quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.putInt(getActivity(), QuickLearnPrefs.QLEARN_SESSION_INDEX, 0);
                getActivity().finish(); //TODO: test if this works
            }
        });

        button_more_words.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTask();
            }
        });

        showTask();

        // Close notification if there is any
//		if(!StudyManager.DEBUG) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
        notificationManager.cancel(MultipleChoiceNotification.NOTIFICATION_TAG, MultipleChoiceNotification.NOTIFICATION_ID);
//		}

//        showInfoDialog();

        return multipleChoiceView;

    }

    @Override
    public void onResume() {
        super.onResume();

        //check whether this is opened as first activity in notification
        boolean firstInteraction = Util.getBool(getActivity(), QuickLearnPrefs.NOTIF_POSTED, false);
        if(firstInteraction) {
            int condition = studyManager.current_condition;
            long notifPostedMillis = Util.getLong(getActivity(), QuickLearnPrefs.NOTIF_POSTED_MILLIS, 0);
//            String classifier = Util.getString(getActivity(), QuickLearnPrefs.BOREDOM_PRED, "");
//            boolean prediction = Util.getBool(getActivity(), QuickLearnPrefs.BOREDOM_PRED_CLASSIFIER, false);
//            String probability = Util.getString(getActivity(), QuickLearnPrefs.BOREDOM_PRED_PROBABILITY, "");
            QLearnJsonLog.onNotificationInteraction(condition, notifPostedMillis);
            Util.put(getActivity(), QuickLearnPrefs.NOTIF_POSTED, false);
        }

        String classifier = "";
        boolean bored = false;
        String probability = "";
//        BoredomPrediction prediction = BoredomPredictionService.latest_prediction;
//        if(prediction != null) {
//            classifier = prediction.classifierName;
//            bored = prediction.bored;
//            probability = String.valueOf(prediction.probability);
//        }
        int mode = Util.getInt(getActivity(), QlearnKeys.QLEARN_MODE, QuickLearnPrefs.QLEARN_MODE_APP);
//        QLearnJsonLog.onQLearnOpened(studyManager.current_condition, mode, classifier, bored, probability);
        QLearnJsonLog.onQLearnOpened(studyManager.current_condition, mode);
        Util.putInt(getActivity(), QlearnKeys.QLEARN_MODE, QuickLearnPrefs.QLEARN_MODE_APP);
    }

    @Override
    public void onPause() {

        QLearnJsonLog.onSessionFinished(QuickLearnPrefs.QLEARN_MODE_APP, StudyManager.getInstance(getActivity()).current_condition, studyManager.session_set_count, studyManager.session_word_count);
        studyManager.resetWordCount();

        //reset notification_posted to avoid triggering notifications too close to app usage
//		Date now = new Date();
//		Util.putString(getApplicationContext(), QuickLearnPrefs.PREF_LAST_NOTIFICATION_POSTED, now.toString());
//		Util.putLong(getApplicationContext(), QuickLearnPrefs.NOTIF_POSTED_MILLIS, System.currentTimeMillis());

        super.onPause();
    }

    private void showTask() {
        nextWord();
        container_solution.setVisibility(View.GONE);
        container_finish_screen.setVisibility(View.GONE);
        container_task.setVisibility(View.VISIBLE);

        text_translation_word.setText(current_word.translation);

        Random rand = new Random(System.currentTimeMillis());
        int option = 1 + rand.nextInt(NUMBER_OF_MULTIPLE_CHOICES);

        ArrayList<String> blacklist = new ArrayList<String>();
        blacklist.add(current_word.key);
        ArrayList<Word> deflectors = studyManager.vocabulary.getRandomWords(NUMBER_OF_MULTIPLE_CHOICES-1, blacklist);
        switch(option) {
            case 1:
                button_correct_option = button_option_a;
                button_option_a.setText(current_word.original);
                button_option_b.setText(deflectors.get(0).original);
                button_option_c.setText(deflectors.get(1).original);
                break;
            case 2:
                button_correct_option = button_option_b;
                button_option_a.setText(deflectors.get(0).original);
                button_option_b.setText(current_word.original);
                button_option_c.setText(deflectors.get(1).original);
                break;
            case 3:
                button_correct_option = button_option_c;
                button_option_a.setText(deflectors.get(0).original);
                button_option_b.setText(deflectors.get(1).original);
                button_option_c.setText(current_word.original);
                break;
        }
    }

    private void showSolution() {
        studyManager.session_word_count++;
        container_finish_screen.setVisibility(View.GONE);
        container_task.setVisibility(View.GONE);
        container_solution.setVisibility(View.VISIBLE);

        text_translation_result.setText(current_word.translation + " : " + current_word.original);
    }

    private void showFinishScreen() {
        studyManager.session_set_count++;
        container_solution.setVisibility(View.GONE);
        container_task.setVisibility(View.GONE);
        container_finish_screen.setVisibility(View.VISIBLE);
    }

    private void nextWord() {
        current_word = studyManager.vocabulary.nextWord();
    }

    private void updateWordList(boolean guessedCorrectly) {
        boolean word_seen = studyManager.vocabulary.wordSeenBefore(current_word.key);
        QLearnJsonLog.onWordReviewed(current_word.key, current_word.source_language, current_word.target_language, QuickLearnPrefs.QLEARN_MODE_APP, studyManager.current_condition, guessedCorrectly, word_seen);
        studyManager.vocabulary.updateWordList(guessedCorrectly);
    }

    private final View.OnClickListener options_OnClickListener = new View.OnClickListener() {
        public void onClick(final View v) {
            if(v == button_correct_option) {
                updateWordList(true);
                text_translation_correct_incorrect.setTextColor(getResources().getColor(R.color.color_correct));
                text_translation_correct_incorrect.setText(getResources().getString(R.string.text_translation_correct));
            } else {
                updateWordList(false);
                text_translation_correct_incorrect.setTextColor(getResources().getColor(R.color.color_incorrect));
                text_translation_correct_incorrect.setText(getResources().getString(R.string.text_translation_incorrect));
            }
            showSolution();
        }
    };

    private void showInfoDialog() {
        if(Util.getBool(getActivity(), QuickLearnPrefs.PREF_NOTIF_SHOWN, false) && Util.getBool(getActivity(), QuickLearnPrefs.PREF_SHOW_INFO_DIALOG, true)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.info_title);
            builder.setPositiveButton(R.string.info_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    CheckBox cb = (CheckBox) ((AlertDialog) dialogInterface).findViewById(R.id.cb_info_not_again);
                    if(cb.isChecked()) {
                        Util.put(getActivity(), QuickLearnPrefs.PREF_SHOW_INFO_DIALOG, false);
                    }
                }
            });
            builder.setView(R.layout.info_dialog);
            builder.show();
        }
    }

}
