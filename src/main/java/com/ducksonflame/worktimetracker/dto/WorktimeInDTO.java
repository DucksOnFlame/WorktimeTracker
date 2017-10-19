package com.ducksonflame.worktimetracker.dto;

import javax.persistence.*;

@Entity
@Table(name = "WorktimeIn")
public class WorktimeInDTO {

    @Id
    @GeneratedValue
    private int worktimeInId;

    @Column(nullable = false, unique = true)
    private String day;

    @Column(nullable = false, unique = true)
    private int timeIn;

    public int getWorktimeInId() {
        return worktimeInId;
    }

    public void setWorktimeInId(int worktimeInId) {
        this.worktimeInId = worktimeInId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(int timeIn) {
        this.timeIn = timeIn;
    }
}
