package com.example.sleepaid.Fragment.SleepDiary;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sleepaid.App;
import com.example.sleepaid.Component.SleepDiaryAnswerComponent;
import com.example.sleepaid.Component.SleepDiaryQuestionComponent;
import com.example.sleepaid.Component.SleepDiaryRadioGroupAnswerComponent;
import com.example.sleepaid.Database.AppDatabase;
import com.example.sleepaid.Database.Option.Option;
import com.example.sleepaid.Database.Question.Question;
import com.example.sleepaid.Database.SleepDiaryAnswer.SleepDiaryAnswer;
import com.example.sleepaid.Handler.ComponentHandler;
import com.example.sleepaid.Handler.DataHandler;
import com.example.sleepaid.Model.SharedViewModel;
import com.example.sleepaid.R;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SleepDiaryQuestionsFragment extends Fragment {
    private View view;

    protected int questionnaireId;

    private SharedViewModel model;
    private AppDatabase db;

    protected int[] questionComponentIds;
    protected int[][] answerComponentIds;

    protected List<Integer> questionIds;
    protected int[][] sections;
    protected List<Option> options;
    protected ArrayAdapter<String>[][] answerSuggestions;
    protected String[][] emptyErrors;

    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        this.view = view;

        this.model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        this.db = AppDatabase.getDatabase(App.getContext());

        getParentFragment().getParentFragment().getView().findViewById(R.id.scrollView).scrollTo(0, 0);

        Button saveButton = view.findViewById(R.id.saveSleepDiaryAnswersButton);
        saveButton.setOnClickListener(saveAnswers);

        this.model.setSleepDiaryHasOptions(this.questionnaireId, this.questionnaireId == 4);
        this.loadQuestions();
    }

    private void loadQuestions() {
        if (this.model.getSleepDiaryQuestions(this.questionnaireId) == null) {
            this.db.questionDao()
                    .loadAllByQuestionnaireIds(new int[]{this.questionnaireId})
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            questions -> {
                                this.model.setSleepDiaryQuestions(this.questionnaireId, questions);
                                this.setupQuestions(questions);

                                this.loadOptions();
                            },
                            Throwable::printStackTrace
                    );
        } else {
            this.setupQuestions(this.model.getSleepDiaryQuestions(this.questionnaireId));

            this.loadOptions();
        }
    }

    private void setupQuestions(List<Question> questions) {
        this.questionIds = questions
                .stream()
                .map(Question::getId)
                .collect(Collectors.toList());

        for (int i = 0; i < questions.size(); i++) {
            SleepDiaryQuestionComponent question = this.view.findViewById(this.questionComponentIds[i]);
            question.setQuestionText(questions.get(i).getQuestion());

            if (!questions.get(i).getInformation().isEmpty()) {
                question.setInformationText(questions.get(i).getInformation());
            } else {
                question.setInformationVisibility(View.GONE);
            }
        }
    }

    private void loadOptions() {
        if (model.hasOptions(this.questionnaireId) && model.getSleepDiaryOptions(this.questionnaireId) == null) {
            this.db.optionDao()
                    .loadAllByQuestionnaireIds(new int[]{this.questionnaireId})
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            options -> {
                                this.model.setSleepDiaryOptions(this.questionnaireId, options);
                                this.options = options;

                                this.loadSuggestions();
                            },
                            Throwable::printStackTrace
                    );
        } else {
            this.options = model.getSleepDiaryOptions(this.questionnaireId);

            this.loadSuggestions();
        }
    }

    private void loadSuggestions() {
        if (model.getSleepDiaryAnswers(this.questionnaireId) == null) {
            this.db.sleepDiaryAnswerDao()
                    .loadAllByQuestionIds(this.questionIds.stream().mapToInt(Integer::intValue).toArray())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            answers -> {
                                this.model.setSleepDiaryAnswers(this.questionnaireId, answers);
                                this.setupSuggestions(answers);

                                this.setupComponents();
                            },
                            Throwable::printStackTrace
                    );
        } else {
            this.setupSuggestions(this.model.getSleepDiaryAnswers(this.questionnaireId));

            this.setupComponents();
        }
    }

    private void setupSuggestions(List<SleepDiaryAnswer> answers) {
        Context context = App.getContext();
        int layout = R.layout.auto_complete_text_view_dropdown;

        for (int i = 0; i < this.questionIds.size(); i++) {
            int questionId = this.questionIds.get(i);

            for (int j = 0; j < this.sections[i].length; j++) {
                int section = this.sections[i][j];

                List<String> suggestionsForQuestionAndSection = answers.stream()
                        .filter(s -> s.getQuestionId() == questionId &&
                                s.getSection() == section)
                        .map(SleepDiaryAnswer::getValue)
                        .collect(Collectors.toList());

                if (!suggestionsForQuestionAndSection.isEmpty()) {
                    Collections.sort(suggestionsForQuestionAndSection);

                    this.answerSuggestions[i][j] = new ArrayAdapter(context, layout, suggestionsForQuestionAndSection);
                }
            }
        }
    }

    private void setupComponents() {
        boolean answerForTodayExists = this.getAnswerForToday() != null;

        if (answerForTodayExists) {
            this.view.findViewById(R.id.alreadySubmittedMessage).setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < this.answerComponentIds.length; i++) {
            for (int j = 0; j < this.answerComponentIds[i].length; j++) {
                View answer = this.view.findViewById(answerComponentIds[i][j]);

                if (answer instanceof SleepDiaryAnswerComponent) {
                    SleepDiaryAnswerComponent sleepDiaryAnswer = (SleepDiaryAnswerComponent) answer;

                    if (answerForTodayExists) {
                        sleepDiaryAnswer.setEnabled(false);
                    } else {
                        this.setupAutoCompleteSuggestions(sleepDiaryAnswer, i, j);

                        if (sleepDiaryAnswer.getInputType() == (InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME)) {
                            this.setupTimeInput(sleepDiaryAnswer, i, j);
                        } else {
                            sleepDiaryAnswer.addTextChangedListener(new TextWatcher() {
                                public void afterTextChanged(Editable s) {
                                    sleepDiaryAnswer.setError(null);
                                }

                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                                public void onTextChanged(CharSequence s, int start, int before, int count) { }
                            });
                        }
                    }
                } else if (answer instanceof SleepDiaryRadioGroupAnswerComponent) {
                    SleepDiaryRadioGroupAnswerComponent sleepDiaryRadioGroupAnswer = (SleepDiaryRadioGroupAnswerComponent) answer;

                    this.setupRadioGroup(sleepDiaryRadioGroupAnswer.getRadioGroup(), this.questionIds.get(i));

                    if (answerForTodayExists) {
                        sleepDiaryRadioGroupAnswer.setEnabled(false);
                    }
                }

                setupNextFocus(answer, i, j);
            }
        }
    }

    private void setupAutoCompleteSuggestions(SleepDiaryAnswerComponent sleepDiaryAnswer, int i, int j) {
        if (this.answerSuggestions[i][j] != null) {
            sleepDiaryAnswer.setAdapter(this.answerSuggestions[i][j]);
        }

        sleepDiaryAnswer.setOnTouchListener((v, event) -> {
            sleepDiaryAnswer.showDropDown();
            return false;
        });
    }

    private void setupNextFocus(View component, int i, int j) {
        if (component instanceof SleepDiaryAnswerComponent) {
            SleepDiaryAnswerComponent sleepDiaryAnswer = (SleepDiaryAnswerComponent) component;

            sleepDiaryAnswer.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                    goToNextComponent(i, j);
                    return true;
                }

                return false;
            });
        } else if (component instanceof SleepDiaryRadioGroupAnswerComponent) {
            SleepDiaryRadioGroupAnswerComponent sleepDiaryRadioGroupAnswer = (SleepDiaryRadioGroupAnswerComponent) component;

            sleepDiaryRadioGroupAnswer.setOnClickListener(view -> {
                sleepDiaryRadioGroupAnswer.setError(null);
                goToNextComponent(i, j);
            });
        }
    }

    private void goToNextComponent(int i, int j) {
        if (j < this.answerComponentIds[i].length - 1) {
            this.view.findViewById(this.answerComponentIds[i][j + 1]).requestFocus();
        } else if (i < this.answerComponentIds.length - 1) {
            this.view.findViewById(this.answerComponentIds[i + 1][0]).requestFocus();
        } else {
            this.view.findViewById(R.id.saveSleepDiaryAnswersButton).requestFocus();
        }
    }

    private void setupTimeInput(SleepDiaryAnswerComponent sleepDiaryAnswer, int i, int j) {
        sleepDiaryAnswer.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                sleepDiaryAnswer.setError(null);

                if (sleepDiaryAnswer.length() == 2 && !sleepDiaryAnswer.getText().toString().contains(":")) {
                    sleepDiaryAnswer.setText(sleepDiaryAnswer.getText() + ":");
                    sleepDiaryAnswer.setSelection(sleepDiaryAnswer.length());
                } else if (sleepDiaryAnswer.length() == 5) {
                    goToNextComponent(i, j);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });
    }

    private void setupRadioGroup(RadioGroup radioGroup, int questionId) {
        List<Option> possibleOptions = this.options
                .stream()
                .filter(o -> o.getQuestionId() == questionId)
                .collect(Collectors.toList());

        List<Integer> possibleOptionsIds = possibleOptions
                .stream()
                .map(Option::getId)
                .collect(Collectors.toList());

        List<String> possibleOptionsTexts = possibleOptions
                .stream()
                .map(Option::getValue)
                .collect(Collectors.toList());

        ComponentHandler.setupRadioGroup(
                radioGroup,
                R.style.RadioButton_White,
                DataHandler.getSizeInDp(22, getResources().getDisplayMetrics()),
                possibleOptionsIds,
                possibleOptionsTexts,
                null,
                null
        );
    }

    private View.OnClickListener saveAnswers = new View.OnClickListener() {
        public void onClick(View view) {
            if (getAnswerForToday() != null) {
                Toast.makeText(getActivity(), R.string.already_submitted_diary_message, Toast.LENGTH_LONG).show();
            } else {
                if (validateAnswers()) {
                    List<SleepDiaryAnswer> answers = getAnswers();

                    db.sleepDiaryAnswerDao()
                            .insert(answers)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        Toast.makeText(getActivity(), "Diary saved successfully!", Toast.LENGTH_SHORT).show();
                                        model.setSleepDiaryAnswers(questionnaireId, answers);
                                        clearAnswers();
                                    },
                                    Throwable::printStackTrace
                            );
                }
            }
        }
    };

    private boolean validateAnswers() {
        boolean hasErrors = false;

        for (int i = 0; i < this.sections.length; i++) {
            for (int j = 0; j < this.sections[i].length; j++) {
                View answerComponent = this.view.findViewById(this.answerComponentIds[i][j]);

                if (answerComponent instanceof SleepDiaryAnswerComponent) {
                    SleepDiaryAnswerComponent sleepDiaryAnswer = (SleepDiaryAnswerComponent) answerComponent;
                    SleepDiaryAnswerComponent sleepDiaryAnswerParent = this.view.findViewById(this.answerComponentIds[i][0]);

                    boolean isEmptyAndHasError = sleepDiaryAnswer.getText().toString().trim().isEmpty()
                            && this.emptyErrors[i][j] != null;

                    boolean isEmptyAndHasErrorAndParentIsNotNoneOrEmpty = sleepDiaryAnswer.getText().toString().trim().isEmpty()
                            && this.emptyErrors[i][j] != null
                            && !sleepDiaryAnswerParent.getText().toString().trim().equalsIgnoreCase("none")
                            && !sleepDiaryAnswerParent.getText().toString().trim().isEmpty();

                    if((j == 0 && isEmptyAndHasError) || (this.sections[i].length > 1 && j != 0 && isEmptyAndHasErrorAndParentIsNotNoneOrEmpty)) {
                        sleepDiaryAnswer.setError(this.emptyErrors[i][j]);

                        // If this is the first error we encounter redirect the user to it
                        if (!hasErrors) {
                            sleepDiaryAnswer.requestFocus();
                        }

                        hasErrors = true;
                    } else if (!isEmptyAndHasError && sleepDiaryAnswer.getInputType() == (InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME)) {
                            List<Integer> times = DataHandler.getIntsFromString(sleepDiaryAnswer.getText().toString());

                            boolean isHourValid = times.size() == 2 && times.get(0) >= 0 && times.get(0) <= 23;
                            boolean isMinuteValid = times.size() == 2 && times.get(1) >= 0 && times.get(1) <= 59;

                            if (!isHourValid && isMinuteValid) {
                                sleepDiaryAnswer.setError(getString(R.string.hour_validation));

                                if (!hasErrors) {
                                    sleepDiaryAnswer.requestFocus();
                                }

                                hasErrors = true;
                            } else if (isHourValid && !isMinuteValid) {
                                sleepDiaryAnswer.setError(getString(R.string.minute_validation));

                                if (!hasErrors) {
                                    sleepDiaryAnswer.requestFocus();
                                }

                                hasErrors = true;
                            } else if (!isHourValid && !isMinuteValid) {
                                sleepDiaryAnswer.setError(getString(R.string.time_validation));

                                if (!hasErrors) {
                                    sleepDiaryAnswer.requestFocus();
                                }

                                hasErrors = true;
                            }
                    }
                } else if (answerComponent instanceof SleepDiaryRadioGroupAnswerComponent) {
                    SleepDiaryRadioGroupAnswerComponent sleepDiaryRadioGroupAnswerComponent = (SleepDiaryRadioGroupAnswerComponent) answerComponent;

                    if (sleepDiaryRadioGroupAnswerComponent.getCheckedRadioButtonId() == -1) {
                        sleepDiaryRadioGroupAnswerComponent.setError(getString(R.string.radio_group_validation));

                        if (!hasErrors) {
                            sleepDiaryRadioGroupAnswerComponent.requestFocus();
                        }

                        hasErrors = true;
                    }
                }
            }
        }

        return !hasErrors;
    }

    private List<SleepDiaryAnswer> getAnswers() {
        List<SleepDiaryAnswer> answers = new ArrayList<>();

        for (int i = 0; i < this.sections.length; i++) {
            for (int j = 0; j < this.sections[i].length; j++) {
                View answerComponent = this.view.findViewById(this.answerComponentIds[i][j]);
                String answer = "";

                if (answerComponent instanceof SleepDiaryAnswerComponent) {
                    answer = ((SleepDiaryAnswerComponent) answerComponent).getText().toString();
                } else if (answerComponent instanceof SleepDiaryRadioGroupAnswerComponent) {
                    int answerId = ((SleepDiaryRadioGroupAnswerComponent) answerComponent).getCheckedRadioButtonId();
                    answer = ((RadioButton) this.view.findViewById(answerId)).getText().toString();
                }

                String date = ZonedDateTime.now().getHour() <= 3 ?
                        DataHandler.getSQLiteDate(ZonedDateTime.now().minusDays(1)) :
                        DataHandler.getSQLiteDate(ZonedDateTime.now());

                answers.add(new SleepDiaryAnswer(
                        answer,
                        this.questionIds.get(i),
                        this.sections[i][j],
                        date
                ));
            }
        }

        return answers;
    }

    private void clearAnswers() {
        for (int i = 0; i < this.answerComponentIds.length; i++) {
            for (int j = 0; j < this.answerComponentIds[i].length; j++) {
                View answerComponent = this.view.findViewById(this.answerComponentIds[i][j]);

                if (answerComponent instanceof SleepDiaryAnswerComponent) {
                    ((SleepDiaryAnswerComponent) answerComponent).clear();
                } else if (answerComponent instanceof SleepDiaryRadioGroupAnswerComponent) {
                    ((SleepDiaryRadioGroupAnswerComponent) answerComponent).clearCheck();
                }
            }
        }
    }

    private SleepDiaryAnswer getAnswerForToday() {
        List<SleepDiaryAnswer> previousAnswers = model.getSleepDiaryAnswers(questionnaireId);
        Optional<SleepDiaryAnswer> answerForToday = previousAnswers.stream()
                .filter(a -> a.getDate().equals(DataHandler.getSQLiteDate(ZonedDateTime.now())))
                .findAny();

        if (answerForToday.isPresent()) {
            return answerForToday.get();
        }

        return null;
    }
}