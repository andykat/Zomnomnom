package communicatingPlayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Archon extends RobotRunner {
	private enum mode{MAKE_SBABY,WAIT_FOR_BABY};
	private mode currentMode;
	private Information memory;
	private RobotInfo baby;
	
	public Archon(RobotController rcin) {
		super(rcin);
		memory= new Information();
		currentMode= mode.MAKE_SBABY;
	}
	
	public void run() throws GameActionException{
		if (rc.isCoreReady()){
			switch (currentMode){
			case MAKE_SBABY:
				Direction canBuildDir= getSpawnableDir(RobotType.SCOUT);
				if (canBuildDir!= null){
					rc.build(canBuildDir, RobotType.SCOUT);
					RobotInfo[] potentialBabies= rc.senseNearbyRobots(1);
					for (int n= 0; n< potentialBabies.length; n++){
						if (potentialBabies[n].type.equals(RobotType.SCOUT)){
							baby= potentialBabies[n];
							System.out.println("FOUND BABY!");
							currentMode= mode.WAIT_FOR_BABY;
							break;
						}
					}
				}
				
				break;
			case WAIT_FOR_BABY:
				
			default:
				//Move back towards the archon?
				break;
			}
		}
	}
}
