package com.example.sleepaid;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity
public class AlarmType {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "typeId")
    public int id;

    public String type;

    public AlarmType(String type) {
        this.type = type;
    }

    public int getId() {
        return this.id;
    }

    public String getType() {
        return this.type;
    }
}