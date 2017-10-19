package com.ducksonflame.worktimetracker.dto;

import javax.persistence.*;

@Entity
@Table(name = "Break")
public class BreakDTO {

    @Id
    @GeneratedValue
    private int breakId;

    @Column(nullable = false, unique = true)
    private String breakDay;

    @Column(nullable = false, unique = true)
    private int breakBegin;

    @Column(nullable = false, unique = true)
    private int breakEnd;

    public int getBreakId() {
        return breakId;
    }

    public void setBreakId(int breakId) {
        this.breakId = breakId;
    }

    public String getBreakDay() {
        return breakDay;
    }

    public void setBreakDay(String breakDay) {
        this.breakDay = breakDay;
    }

    public int getBreakBegin() {
        return breakBegin;
    }

    public void setBreakBegin(int breakBegin) {
        this.breakBegin = breakBegin;
    }

    public int getBreakEnd() {
        return breakEnd;
    }

    public void setBreakEnd(int breakEnd) {
        this.breakEnd = breakEnd;
    }
}
