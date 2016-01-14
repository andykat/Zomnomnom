package communicatingPlayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Archon extends RobotRunner {
	private int scoutCount= 5;
	
	public Archon(RobotController rcin) {
		super(rcin);
	}
	
	public void run() throws GameActionException{
		if (rc.isCoreReady()){
			if (rc.canBuild(Direction.NORTH, RobotType.SCOUT)){
				if (scoutCount> 0){
					rc.build(Direction.NORTH, RobotType.SCOUT);
					scoutCount--;
				}
			}
		}
	}

}
