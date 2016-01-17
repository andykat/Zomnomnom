package danielsrobot;

import battlecode.common.*;

import java.lang.Math;
import java.util.ArrayList;

public class Guard extends RobotRunner {

    public Guard(RobotController rc) {
       super(rc); 
    }

    public void run() throws GameActionException {
        RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), RobotType.GUARD.sensorRadiusSquared);
        if(enemies.length > 0) {
            RobotInfo target = null;
            double minHealth = 5000;
            for(RobotInfo r : enemies) {
                if(r.type == RobotType.BIGZOMBIE || r.type == RobotType.FASTZOMBIE) {
                    target = r;
                    break;
                }
                else if(r.health < minHealth) {
                    target = r;
                    minHealth = r.health;
                }
            }

            if(rc.isWeaponReady()) {
                if(rc.canAttackLocation(target.location)) {
                    rc.attackLocation(target.location);
                }
                else {
                    if(rc.isCoreReady()) {
                        bugMove(rc.getLocation(), target.location);
                    }
                }
            }
        }
    }
}
