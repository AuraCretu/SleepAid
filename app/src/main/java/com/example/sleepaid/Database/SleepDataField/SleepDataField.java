package com.example.sleepaid.Database.SleepDataField;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {
        @Index(
                value = {"name"},
                unique = true
        )
})
public class SleepDataField {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "fieldId")
    public int id;

    @NonNull
    public String name;

    public SleepDataField(String name) {
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
}
