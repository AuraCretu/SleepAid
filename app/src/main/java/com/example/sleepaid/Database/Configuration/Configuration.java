package com.example.sleepaid.Database.Configuration;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {
        @Index(
                value = {"name"},
                unique = true
        )
})
public class Configuration {
    @PrimaryKey
    @NonNull
    public String name;
    @NonNull
    public String value;

    public Configuration(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }
}
