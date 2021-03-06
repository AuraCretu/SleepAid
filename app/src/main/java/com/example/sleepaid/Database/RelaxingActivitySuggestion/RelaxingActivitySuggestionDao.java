package com.example.sleepaid.Database.RelaxingActivitySuggestion;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface RelaxingActivitySuggestionDao {
    @Query("SELECT * FROM RelaxingActivitySuggestion ORDER BY relaxingActivityId")
    Single<List<RelaxingActivitySuggestion>> getAll();
}
