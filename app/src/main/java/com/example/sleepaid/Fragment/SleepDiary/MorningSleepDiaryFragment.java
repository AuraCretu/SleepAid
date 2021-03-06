package com.example.sleepaid.Fragment.SleepDiary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sleepaid.R;

public class MorningSleepDiaryFragment extends SleepDiaryQuestionsFragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sleep_diary_morning_questions, container, false);
    }

    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        this.questionnaireId = 4;

        this.questionComponentIds = new int[]{
                R.id.morningQuestion1,
                R.id.morningQuestion2,
                R.id.morningQuestion3,
                R.id.morningQuestion4,
                R.id.morningQuestion5,
                R.id.morningQuestion6,
                R.id.morningQuestion7,
                R.id.morningQuestion8,
                R.id.morningQuestion9,
                R.id.morningQuestion10
        };

        this.answerComponentIds = new int[][]{
                {R.id.morningAnswer1},
                {R.id.morningAnswer2},
                {R.id.morningAnswer3},
                {R.id.morningAnswer4},
                {R.id.morningAnswer5},
                {R.id.morningAnswer6},
                {R.id.morningAnswer7},
                {R.id.morningAnswer8},
                {R.id.morningAnswer9},
                {R.id.morningAnswer10}
        };

        this.sections = new int[][] {
                {1},
                {1},
                {1},
                {1},
                {1},
                {1},
                {1},
                {1},
                {1},
                {1}
        };

        this.answerSuggestions = new ArrayAdapter[][]{
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null}
        };

        this.emptyErrors = new String[][]{
                {"Please enter a time."},
                {"Please enter a time."},
                {"Please enter a duration."},
                {"Please enter a time."},
                {"Please enter a duration."},
                null,
                {"Please enter a number."},
                null,
                null,
                null
        };

        super.onViewCreated(view, savedInstanceState);
    }
}