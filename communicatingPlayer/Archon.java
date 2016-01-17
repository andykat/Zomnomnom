package communicatingPlayer;

import java.util.ArrayList;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;

public class Archon extends RobotRunner {
	private enum mode{CREATE_EYES};
	private mode currentMode;
	private int leaderID;
	private boolean spawned= false;

	public Archon(RobotController rcin) {
		super(rcin);
		memory= new Information();
		currentMode= mode.CREATE_EYES;
		leaderID= Integer.MAX_VALUE;
	}
	
	public void signaling() throws GameActionException{
		if (rc.getRoundNum()== 0){ //Election on first round hurray!
			Signal[] incomingMessages= rc.emptySignalQueue();
			rc.setIndicatorString(0, ""+incomingMessages.length+" messages received");
			rc.broadcastMessageSignal(0, 0, 100);
			leaderID= incomingMessages.length;
		}else{
			if (leaderID== 0){
				sendInstructions();
			}else{
				followInstructions();
			}
		}
	}
	
	public void sendInstructions(){
		
	}
	
	public void followInstructions(){
		Signal[] incomingMessages= rc.emptySignalQueue();
		for (int n= 0; n< incomingMessages.length; n++){
			if (incomingMessages[n].getTeam().equals(myTeam)){
				//Do something about it
				
			}
		}
		
	}
	
	public void run() throws GameActionException{
		if (rc.isCoreReady()){
			signaling();
			if (!spawned){
				buildRobot(RobotType.SOLDIER);
				spawned= true;
			}
		}
	}
}
