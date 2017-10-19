package com.ducksonflame.worktimetracker.dto;

import javax.persistence.*;

@Entity
@Table(name = "WorktimeOut")
public class WorktimeOutDTO {

    @Id
    @GeneratedValue
    private int worktimeOutId;

    @Column(nullable = false, unique = true)
    private String day;

    @Column(nullable = false, unique = true)
    private int timeOut;

    public int getWorktimeOutId() {
        return worktimeOutId;
    }

    public void setWorktimeOutId(int worktimeOutId) {
        this.worktimeOutId = worktimeOutId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }
}
