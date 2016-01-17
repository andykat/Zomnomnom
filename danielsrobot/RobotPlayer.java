package danielsrobot;

import battlecode.common.*;
import java.lang.Math;
import java.util.Random;

public class RobotPlayer {
    static RobotController rc;
    private static RobotRunner rr;

    @SuppressWarnings("unused")
        public static void run(RobotController rc) {
            Team myTeam = rc.getTeam();
            Team enemyTeam = myTeam.opponent();
            if(rc.getType() == RobotType.ARCHON) {
                rr = new Archon(rc); 
            }
            else if(rc.getType() == RobotType.SCOUT) {
            } // end of scout
            else if(rc.getType() == RobotType.SOLDIER) {

            } // end of soldier
            else if(rc.getType() == RobotType.GUARD) {
                rr = new Guard(rc);
            } // end of guard
            else if(rc.getType() == RobotType.VIPER) {

            }
            else if(rc.getType() == RobotType.TURRET) {

            }
            else if(rc.getType() == RobotType.TTM) {

            }
            else {
                rr = new RobotRunner(rc);
            }

            while(true) {
                try {
                    rr.run();
                    Clock.yield();
                } catch(GameActionException e) {
                    e.printStackTrace();
                }
            }
        } 


    public static void bugMove(RobotController rc, MapLocation start, MapLocation end){
        if(start == end) {
            return;
        }
        try {
            Direction dir = start.directionTo(end);
            int c=0;
            while(!rc.canMove(dir))
            {
                dir = dir.rotateRight();
                if(c>7){
                    break;
                }
                c++;
            }
            if(c<8){
                rc.move(dir);
            }
            else{
                rc.clearRubble(dir);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
