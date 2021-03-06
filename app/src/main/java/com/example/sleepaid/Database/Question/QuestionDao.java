package com.example.sleepaid.Database.Question;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface QuestionDao {
    @Query("SELECT * FROM Question ORDER BY questionId")
    Single<List<Question>> getAll();

    @Query("SELECT * FROM Question WHERE questionId IN (:questionIds) ORDER BY questionId")
    Single<List<Question>> loadAllByIds(int[] questionIds);

    @Query("SELECT * FROM Question WHERE questionnaireId IN (:questionnaireIds) ORDER BY questionnaireId, questionId")
    Single<List<Question>> loadAllByQuestionnaireIds(int[] questionnaireIds);

    @Insert
    Completable insert(List<Question> questions);
}
