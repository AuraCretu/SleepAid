package com.example.sleepaid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.example.sleepaid.Activity.HelloScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

@SuppressLint("NewApi")
public class QuestionnaireFragment extends Fragment {
    AppDatabase db;

    private int currentQuestionId;
    private List<Answer> currentAnswers;

    private List<Question> questions;
    private List<Option> options;

    private int sizeInDp;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_questionnaire, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        db = AppDatabase.getDatabase(App.getContext());

        sizeInDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                25,
                getResources().getDisplayMetrics()
        );

        currentQuestionId = 1;
        currentAnswers = new ArrayList<Answer>();

        loadAllQuestions();
    }

//    @Override
//    public void onBackPressed() {
//        if (currentQuestionId == 1) {
//            exitQuestionnaire();
//        }
//        else {
//            loadScreen(currentQuestionId - 1);
//        }
//    }

    private void exitQuestionnaire() {
        Context context = App.getContext();

        DialogInterface.OnClickListener exitAction = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Intent mainActivity = new Intent(context, HelloScreen.class);
                startActivity(mainActivity);
            }
        };

        DialogInterface.OnClickListener cancelAction = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        };

        Modal.show(
                context,
                getString(R.string.exit_questionnaire),
                getString(R.string.yes_modal),
                exitAction,
                getString(R.string.cancel_modal),
                cancelAction
        );
    }

    private void loadAllQuestions() {
        db.questionDao()
                .getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        questionData -> {
                            questions = questionData;

                            loadAllOptions();
                        },
                        Throwable::printStackTrace
                );
    }

    private void loadAllOptions() {
        db.optionDao()
                .getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        optionData -> {
                            options = optionData;

                            loadScreen(1);
                        },
                        Throwable::printStackTrace
                );
    }

    public void loadNextScreen(View view) {
        RadioGroup radioGroup = getView().findViewById(R.id.radioGroup);
        int checkedId = radioGroup.getCheckedRadioButtonId();

        if (checkedId == -1) {
            DialogInterface.OnClickListener cancelAction = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {}
            };

            Modal.show(
                    App.getContext(),
                    getString(R.string.question_validation),
                    getString(R.string.ok_modal),
                    cancelAction,
                    null,
                    null
            );
        }
        else {
            loadScreen(currentQuestionId + 1);
        }
    }

    public void loadPreviousScreen(View view) {
//        if (currentQuestionId > questions.size()) {
//            setContentView(R.layout.activity_questionnaire_host);
//        }

        loadScreen(currentQuestionId - 1);
    }

    private void loadScreen(int questionId) {
        if (currentQuestionId <= questions.size()) {
            RadioGroup radioGroup = getView().findViewById(R.id.radioGroup);
            int checkedId = radioGroup.getCheckedRadioButtonId();

            if (checkedId != -1) {
                int existingAnswerId = -1;

                if (!currentAnswers.isEmpty()) {
                    Optional<Answer> answer = currentAnswers
                            .stream()
                            .filter(a -> a.getQuestionId() == currentQuestionId)
                            .findAny();

                    if (answer.isPresent()) {
                        existingAnswerId = currentAnswers.indexOf(answer.get());
                    }
                }

                if (existingAnswerId > -1) {
                    currentAnswers.set(existingAnswerId, new Answer(checkedId, currentQuestionId));
                }
                else {
                    currentAnswers.add(new Answer(checkedId, currentQuestionId));
                }
            }
        }

        if (questionId == 0) {
            exitQuestionnaire();
        }
        else if (questionId == questions.size() + 1) {
            currentQuestionId = questionId;

            //setContentView(R.layout.activity_questionnaire_summary);
            loadAllAnswers();
        }
        else {
            getView().findViewById(R.id.scrollView).scrollTo(0, 0);

            currentQuestionId = questionId;

            loadQuestion(questionId);
            loadOptionsForQuestion(questionId);
            loadPreviousAnswerForQuestion(questionId);

            if (questionId == 2) {
                presetWakeUpTime(questionId);
            }
        }
    }

    private void loadQuestion(int questionId) {
        TextBox questionBox = getView().findViewById(R.id.question);
        TextBox informationBox = getView().findViewById(R.id.information);

        Optional<Question> question = questions
                .stream()
                .filter(q -> q.getId() == questionId)
                .findAny();

        if (question.isPresent()) {
            questionBox.setText(question.get().getQuestion());
            informationBox.setText(question.get().getInformation());
        }
    }

    private void loadOptionsForQuestion(int questionId) {
        RadioGroup radioGroup = getView().findViewById(R.id.radioGroup);
        radioGroup.clearCheck();
        radioGroup.removeAllViews();

        List<Option> possibleOptions = options
                .stream()
                .filter(o -> o.getQuestionId() == questionId)
                .collect(Collectors.toList());

        for (Option o : possibleOptions) {
            AppCompatRadioButton optionBox = new AppCompatRadioButton(App.getContext());

            optionBox.setId(o.getId());
            optionBox.setText(o.getValue());
            optionBox.setTextSize((int) (sizeInDp / 3.5));
            optionBox.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

            RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 0, 0, sizeInDp);
            optionBox.setLayoutParams(layoutParams);

            optionBox.setPadding(
                    sizeInDp / 2,
                    sizeInDp / 2,
                    sizeInDp / 2,
                    sizeInDp / 2
            );

            radioGroup.addView(optionBox);
        }
    }

    private void loadPreviousAnswerForQuestion(int questionId) {
        if (!currentAnswers.isEmpty()) {
            Optional<Answer> answer = currentAnswers
                    .stream()
                    .filter(a -> a.getQuestionId() == questionId)
                    .findAny();

            if (answer.isPresent()) {
                AppCompatRadioButton option = getView().findViewById(answer.get().getOptionId());
                option.setChecked(true);
            }
        }
    }

    private void presetWakeUpTime(int questionId) {
        Optional<Answer> previousAnswer = currentAnswers
                .stream()
                .filter(a -> a.getQuestionId() == (questionId - 1))
                .findAny();

        Optional<Option> firstOption = options
                .stream()
                .filter(o -> o.getQuestionId() == questionId)
                .findFirst();

        if (previousAnswer.isPresent() && firstOption.isPresent()) {
            AppCompatRadioButton option = getView().findViewById(previousAnswer.get().getOptionId() + firstOption.get().getId() - 1);

            TextBox information = getView().findViewById(R.id.information);
            String currentText = information.getText().toString();
            String newText = getString(R.string.wakeup_time) + option.getText().toString().toLowerCase();

            information.setText(currentText + "\n " + newText);

            Optional<Answer> currentAnswer = currentAnswers
                    .stream()
                    .filter(a -> a.getQuestionId() == questionId)
                    .findAny();

            if(!currentAnswer.isPresent()) {
                option.setChecked(true);
            }
        }
    }

    private void loadAllAnswers() {
        LinearLayout layout = getView().findViewById(R.id.answers);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, sizeInDp / 2);

        for (Question q : questions) {
            TextBox textBox = new TextBox(App.getContext());

            textBox.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            textBox.setTextSize((int) (sizeInDp / 3.5));
            textBox.setLayoutParams(layoutParams);

            int questionId = q.getId();

            String questionText = questionId + ". " + q.getQuestion();
            String answerText;

            Optional<Answer> currentAnswer = currentAnswers
                    .stream()
                    .filter(a -> a.getQuestionId() == questionId)
                    .findAny();

            if (currentAnswer.isPresent()) {
                Optional<Option> option = options
                        .stream()
                        .filter(o -> o.getId() == currentAnswer.get().getOptionId())
                        .findAny();

                answerText = "A: " + option.get().getValue();
            }
            else {
                answerText = "No answer";
            }

            textBox.setText(questionText + "\n" + answerText);

            layout.addView(textBox);
        }
    }

    public void storeAnswers(View view) {
        db.answerDao()
                .insert(currentAnswers)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> new InitialSettingsHandler(App.getContext(), db).getSettings(),
                        Throwable::printStackTrace
                );
    }
}