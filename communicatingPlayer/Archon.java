package communicatingPlayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Archon extends RobotRunner {
	private enum mode{CREATE_EYES};
	private mode currentMode;


	public Archon(RobotController rcin) {
		super(rcin);
		memory= new Information();
		currentMode= mode.CREATE_EYES;
	}
	
	public void run() throws GameActionException{
		if (rc.isCoreReady()){
			
		}
	}
}
