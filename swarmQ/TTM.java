package swarmQ;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class TTM extends RobotRunner{
	public TTM(RobotController rcin){
		super(rcin);
	}
	public void run() throws GameActionException{
		if (rc.isCoreReady()){
			rc.unpack();
		}
	}
}
