package danielsrobot;

import battlecode.common.*;

public class Rubble {
    MapLocation loc;
    double amount;

    public Rubble(MapLocation loc, double amount) {
        this.loc = loc;
        this.amount = amount;
    }

    public MapLocation getLocation() {
        return loc;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount() {
        this.amount = amount;
    }
}
