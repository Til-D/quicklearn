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

import static android.R.attr.mode;
//import org.pielot.borpred.BoredomPrediction;
//import org.pielot.borpred.BoredomPredictionService;

public class FlashcardActivity extends android.app.Fragment {

    private StudyManager studyManager;
    private Word current_word;
    private View flashcardView;

//	UI Elements

    //flashcard
    private CardView container_flashcard;
//    private TextView task_heading;
    private Button button_translate;
    private TextView text_translation_word;

    //solution
    private CardView container_solution;
    private TextView solution_heading;
    private Button button_knew_it;
    private Button button_did_not_know;
    private TextView text_translation_result;


    //finishScreen
    private CardView container_finish_screen;
//    private TextView finish_screen_heading;
    private Button button_quit;
    private Button button_more_words;

    //Debug
    private CardView container_debug_info;
    private TextView debug_info;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        flashcardView = inflater.inflate(R.layout.activity_flashcard, container, false);

        this.studyManager = StudyManager.getInstance(getActivity());

        String language = Util.capitalizeWords(studyManager.vocabulary.target_language);
        String heading = String.format(getResources().getString(R.string.notif_title), language);

        //Debug
//        container_debug_info = (CardView) flashcardView.findViewById(R.id.container_debug_info);
//        debug_info = (TextView) flashcardView.findViewById(R.id.debug_info);

        //flashcard
        container_flashcard = (CardView) flashcardView.findViewById(R.id.container_flashcard);
//        task_heading = (TextView) flashcardView.findViewById(task_heading);
//        task_heading.setText(heading);
        button_translate = (Button) flashcardView.findViewById(R.id.button_translate);
        text_translation_word = (TextView) flashcardView.findViewById(R.id.text_translation_word);

        button_translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSolution();
            }
        });

        //solution
        container_solution = (CardView) flashcardView.findViewById(R.id.container_solution);
        solution_heading = (TextView) flashcardView.findViewById(R.id.solution_heading);
//        solution_heading.setText(heading);
        button_knew_it = (Button) flashcardView.findViewById(R.id.button_knew_it);
        button_did_not_know = (Button) flashcardView.findViewById(R.id.button_did_not_know);
        text_translation_result = (TextView) flashcardView.findViewById(R.id.text_translation_result);

        button_knew_it.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateWordList(true);
                int session_index = Util.getInt(getActivity(), QuickLearnPrefs.QLEARN_SESSION_INDEX, 0);
                session_index++;
                if(session_index == StudyManager.WORDS_PER_SET) {
                    Util.putInt(getActivity(), QuickLearnPrefs.QLEARN_SESSION_INDEX, 0);
                    showFinishScreen();
                } else {
                    Util.putInt(getActivity(), QuickLearnPrefs.QLEARN_SESSION_INDEX, session_index);
                    showFlashcard();
                }
            }
        });

        button_did_not_know.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateWordList(false);
                int session_index = Util.getInt(getActivity(), QuickLearnPrefs.QLEARN_SESSION_INDEX, 0);
                session_index++;
                if(session_index == StudyManager.WORDS_PER_SET) {
                    Util.putInt(getActivity(), QuickLearnPrefs.QLEARN_SESSION_INDEX, 0);
                    showFinishScreen();
                } else {
                    Util.putInt(getActivity(), QuickLearnPrefs.QLEARN_SESSION_INDEX, session_index);
                    showFlashcard();
                }
            }
        });

        //finishScreen
        container_finish_screen = (CardView) flashcardView.findViewById(R.id.container_finish_screen);
//        finish_screen_heading = (TextView) flashcardView.findViewById(finish_screen_heading );
//        finish_screen_heading .setText(heading);
        button_quit = (Button) flashcardView.findViewById(R.id.button_quit);
        button_more_words = (Button) flashcardView.findViewById(R.id.button_more_words);

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
                showFlashcard();
            }
        });

        showFlashcard();

        // Close notification if there is any
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity()); //this, getActivity or getContext?
        notificationManager.cancel(FlashcardNotification.NOTIFICATION_TAG, FlashcardNotification.NOTIFICATION_ID);

//		}

//		QLearnJsonLog.startLogger(getApplicationContext());
//        showInfoDialog();

        return flashcardView;
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_flashcard);


//    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

//        //DEBUG output
//        if(StudyManager.DEBUG && (BoredomPredictionService.latest_prediction != null)) {
//            debug_info.setText("Boredom prediction: " + BoredomPredictionService.latest_prediction.toString());
//        } else {
//            container_debug_info.setVisibility(View.GONE);
//        }

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

//        String classifier = "";
//        boolean bored = false;
//        String probability = "";
//        BoredomPrediction prediction = BoredomPredictionService.latest_prediction;
//        if(prediction != null) {
//            classifier = prediction.classifierName;
//            bored = prediction.bored;
//            probability = String.valueOf(prediction.probability);
//        }
//        int mode = Util.getInt(getContext(), QlearnKeys.QLEARN_MODE, QuickLearnPrefs.QLEARN_MODE_APP);
//        QLearnJsonLog.onQLearnOpened(studyManager.current_condition, mode, classifier, bored, probability);
        QLearnJsonLog.onQLearnOpened(studyManager.current_condition, mode);
//        Util.putInt(getContext(), QlearnKeys.QLEARN_MODE, QuickLearnPrefs.QLEARN_MODE_APP);

        //TODO: log app opening

    }

    @Override
    public void onPause() {

        QLearnJsonLog.onSessionFinished(QuickLearnPrefs.QLEARN_MODE_APP, StudyManager.getInstance(getActivity()).current_condition, studyManager.session_set_count, studyManager.session_word_count);
        studyManager.resetWordCount();

        //reset notification_posted to avoid triggering notifications too close to app usage
//        Date now = new Date();
//        Util.putString(getApplicationContext(), QuickLearnPrefs.PREF_LAST_NOTIFICATION_POSTED, now.toString());
//        Util.putLong(getApplicationContext(), QuickLearnPrefs.NOTIF_POSTED_MILLIS, System.currentTimeMillis());

        super.onPause();
    }

    private void showFlashcard() {
        nextWord();
        container_solution.setVisibility(View.GONE);
        container_finish_screen.setVisibility(View.GONE);
        container_flashcard.setVisibility(View.VISIBLE);

        text_translation_word.setText(current_word.translation);
    }

    private void showSolution() {
        studyManager.session_word_count++;
        container_finish_screen.setVisibility(View.GONE);
        container_flashcard.setVisibility(View.GONE);
        container_solution.setVisibility(View.VISIBLE);

        solution_heading.setText(current_word.translation + " : ");
        text_translation_result.setText(current_word.original);
    }

    private void showFinishScreen() {
        studyManager.session_set_count++;
        container_solution.setVisibility(View.GONE);
        container_flashcard.setVisibility(View.GONE);
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
