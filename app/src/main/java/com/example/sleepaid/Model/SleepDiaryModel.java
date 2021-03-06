package com.example.sleepaid.Model;

import com.example.sleepaid.Database.Answer.Answer;
import com.example.sleepaid.Database.Option.Option;
import com.example.sleepaid.Database.Question.Question;

import java.util.List;


public class SleepDiaryModel {
    private int questionnaireId;

    private List<Question> questions;
    private boolean hasOptions;
    private List<Option> options;
    private List<Answer> answers;

    public SleepDiaryModel(int questionnaireId) {
        this.questionnaireId = questionnaireId;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void setHasOptions(boolean hasOptions) {
        this.hasOptions = hasOptions;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public int getQuestionnaireId() {
        return this.questionnaireId;
    }

    public List<Question> getQuestions() {
        return this.questions;
    }

    public boolean hasOptions() {
        return this.hasOptions;
    }

    public List<Option> getOptions() {
        return this.options;
    }

    public List<Answer> getAnswers() {
        return this.answers;
    }
}
