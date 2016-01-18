package danielsrobot;

import java.util.ArrayList;

import battlecode.common.*;

public class Soldier extends RobotRunner{
	private RobotInfo mom;
	private enum mode{PATROL, LOOK_FOR_MOM, GO_TO_MOM, ATTACK, RUN_AWAY};
	private ArrayList<MapLocation> visitingList;
	private MapLocation targetAttraction;
	private int fullness= 0; //Once it exceeds the scout hunger amount, gets reset to zero and move on to next target
	private int searchLevel;
	private MapLocation homeBase;
	private mode currentMode;
	private int visitingIndex;
    private int KITE_THRESHOLD_HEALTH = 30; // percent
    private int moveCount = 0;
    private Move move;
	
	
	public Soldier(RobotController rcin) throws GameActionException{
		super(rcin);
		visitingList= new ArrayList<MapLocation>();
		searchLevel= (int) Math.sqrt(rc.getType().attackRadiusSquared); //Must be > 1
		visitingList= createDividedRadius(rc.getType().attackRadiusSquared/2,searchLevel); //ouskirts of soldier
		currentMode= mode.LOOK_FOR_MOM;
		homeBase= rc.getLocation();
        move = new Move();
	}

	public void run() throws GameActionException{ //Scout gathers information, shares location with Archon, they go grab it
		//280 max for sesnor radius
        RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), RobotType.SOLDIER.sensorRadiusSquared);
        if(enemies.length > 0) {
            currentMode = mode.ATTACK;
        }

		if (rc.isCoreReady()){
            switch (currentMode){
                case PATROL:
                    
                    break;

                case ATTACK:
                    if(rc.getHealth() / RobotType.SOLDIER.maxHealth <= KITE_THRESHOLD_HEALTH) {
                        currentMode = mode.RUN_AWAY;
                        break;
                    }
                    RobotInfo target = getBestTarget(RobotType.SOLDIER, enemies);
                    if(rc.isWeaponReady()) {
                        if(rc.canAttackLocation(target.location)) {
                            rc.attackLocation(target.location);
                        }
                        else {
                            if(rc.isCoreReady()) {
                                move.bugMove(rc, target.location);                            
                            }
                        }
                    }
                    break;

                case RUN_AWAY:
                    if(moveCount == 3) {
                        moveCount = 0;
                        currentMode = mode.ATTACK;
                        break;
                    }
                    runawayFrom(enemies);
                    break;

                default:
                    //Move back towards the archon?
                    break;
            }
        }
    }
}
