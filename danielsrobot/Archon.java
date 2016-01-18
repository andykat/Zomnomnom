package danielsrobot;

import battlecode.common.*;

import java.lang.Math;
import java.util.ArrayList;

public class Archon extends RobotRunner {
	private enum mode{MAKE_SBABY,WAIT_FOR_BABY,RUN_TO_ENEMY, RUN_AWAY, MAKE_SOLDIERS};
	private mode currentMode;
	private Information memory;
	private RobotInfo baby;
	private int numSoldiersToSummon= 5;
	private int numGaurdsToSummon= 5;

	public Archon(RobotController rcin) {
		super(rcin);
		memory= new Information();
		currentMode= mode.MAKE_SBABY;
	}
	

	public void run() throws GameActionException{
        RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), RobotType.ARCHON.sensorRadiusSquared);
        if(enemies.length > 0) {
            currentMode = mode.RUN_AWAY;
        }
		if (rc.isCoreReady()){
			switch (currentMode){
            case RUN_AWAY:
                runawayFrom(enemies);
                break;

			case MAKE_SBABY:
				Direction canBuildDir= getSpawnableDir(RobotType.SCOUT);
				if (canBuildDir!= null){
					rc.build(canBuildDir, RobotType.SCOUT);
					RobotInfo[] potentialBabies= rc.senseNearbyRobots(2);
					for (int n= 0; n< potentialBabies.length; n++){
						if (potentialBabies[n].type.equals(RobotType.SCOUT)){
							baby= potentialBabies[n];
							//System.out.println("FOUND BABY!");
							currentMode= mode.WAIT_FOR_BABY;
							break;
						}
					}
				}
				
				break;

            case MAKE_SOLDIERS:
                

                break;

			case WAIT_FOR_BABY:
//				RobotType summon= RobotConstants.posNRobotTypes[randall.nextInt(2)+3];
//				if (summon.equals(RobotType.SOLDIER)){
//					if (numSoldiersToSummon> 0){
//						numSoldiersToSummon--;
//						buildRobot(summon);
//					}
//				}else if (summon.equals(RobotType.GUARD)){
//					if (numGaurdsToSummon> 0){
//						numGaurdsToSummon--;
//						buildRobot(summon);
//					}
//				}
//				
//				if (numGaurdsToSummon<= 0 && numSoldiersToSummon<= 0){
//					bugMove(rc.getLocation(), eden);
//				}
				break;

			default:
				//Move back towards the archon?
				break;
			}
		}
	}
}
