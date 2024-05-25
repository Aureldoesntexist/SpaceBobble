package com.bobble.spacebobble.core.scores;

import com.bobble.spacebobble.config.Team;

import java.io.Serial;
import java.io.Serializable;

public class Score implements Serializable {
    @Serial
    private static final long serialVersionUID = 8297233202196934118L;
    private int total;
    private String name;
    private Team team = null;

    public Score() {
        this.total = 0;
    }

    public int getTotal() {
        return total;
    }

    public synchronized void setTotal(int total) {
        this.total = total;
        if (this.total < 0) {
            this.total = 0;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
