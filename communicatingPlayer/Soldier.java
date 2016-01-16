package communicatingPlayer;

import java.util.ArrayList;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Soldier extends RobotRunner{
	private RobotInfo mom;
	private enum mode{PATROL, LOOK_FOR_MOM, GO_TO_MOM};
	private ArrayList<MapLocation> visitingList;
	private MapLocation targetAttraction;
	private int fullness= 0; //Once it exceeds the scout hunger amount, gets reset to zero and move on to next target
	private int searchLevel;
	private MapLocation homeBase;
	private mode currentMode;
	private int visitingIndex;
	
	
	public Soldier(RobotController rcin) throws GameActionException{
		super(rcin);
		visitingList= new ArrayList<MapLocation>();
		searchLevel= (int) Math.sqrt(rc.getType().attackRadiusSquared); //Must be > 1
		visitingList= createDividedRadius(rc.getType().attackRadiusSquared/2,searchLevel); //ouskirts of soldier
		currentMode= mode.LOOK_FOR_MOM;
		homeBase= rc.getLocation();
	}
	public void run() throws GameActionException{ //Scout gathers information, shares location with Archon, they go grab it
		//280 max for sesnor radius
		if (rc.isCoreReady()){
			switch (currentMode){

			default:
				//Move back towards the archon?
				break;
			}
		}
	}
}
