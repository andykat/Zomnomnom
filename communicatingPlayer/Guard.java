package communicatingPlayer;

import java.util.ArrayList;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Guard extends RobotRunner{
	private RobotInfo mom;
	private enum mode{PATROL, LOOK_FOR_MOM, GO_TO_MOM};
	private ArrayList<MapLocation> visitingList;
	private MapLocation targetAttraction;
	private int fullness= 0; //Once it exceeds the scout hunger amount, gets reset to zero and move on to next target
	private int searchLevel;
	private MapLocation homeBase;
	private mode currentMode;
	private int visitingIndex;
	
	
	public Guard(RobotController rcin) throws GameActionException{
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
			case LOOK_FOR_MOM:
				RobotInfo[] fambam= rc.senseNearbyRobots(2);
				for (int n= 0; n< fambam.length; n++){
					if (fambam[n].type.equals(RobotType.ARCHON)){
						mom= fambam[n];
						currentMode= mode.PATROL;
						break;
					}
				}
				
				break;
			
			case PATROL://=============================================================
				
				break;
			case GO_TO_MOM:
				//System.out.println("Going to Mom");
				if (rc.canSenseRobot(mom.ID)){ //get update
					mom= rc.senseRobot(mom.ID);
					homeBase= mom.location;
				}
				
				bugMove(rc.getLocation(),homeBase);
				if (rc.getLocation().distanceSquaredTo(homeBase)<= searchLevel/2){ 
					currentMode= mode.PATROL;
				}
				break;
			default:
				//Move back towards the archon?
				break;
			}
		}
	}
}
