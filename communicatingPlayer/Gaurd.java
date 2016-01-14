package communicatingPlayer;

import java.util.ArrayList;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Gaurd extends RobotRunner{
	private RobotInfo mom;
	private enum mode{PATROL, LOOK_FOR_MOM, GO_TO_MOM};
	private ArrayList<MapLocation> visitingList;
	private MapLocation targetAttraction;
	private int fullness= 0; //Once it exceeds the scout hunger amount, gets reset to zero and move on to next target
	private int searchLevel;
	private MapLocation homeBase;
	private mode currentMode;
	private int visitingIndex;
	
	
	public Gaurd(RobotController rcin) throws GameActionException{
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
				//System.out.println("patrol");
				if (targetAttraction== null && visitingList.size()> 0){ //If you currently aren't going any where and you want to go somewhere
					targetAttraction= visitingList.get(visitingIndex);
				}else if (targetAttraction!= null){ //If you want to go somewhere
					if (rc.getLocation().equals(targetAttraction)){//If you are at designated node
						visitingIndex+= 1;
						fullness= 0;
						if (visitingIndex > visitingList.size()-1){ 
							visitingIndex= 0; //Loop back
							currentMode= mode.GO_TO_MOM;
						}
						targetAttraction= visitingList.get(visitingIndex);						
					}else{ //if you are not yet on the destination
						//simpleMove(rc.getLocation().directionTo(targetAttraction));
						boolean attacked= simpleAttack();
						
						if (! attacked){
							bugMove(rc.getLocation(), targetAttraction);
							fullness+= 1;
						
							MapLocation testLocation= checkLocation(targetAttraction);
							if (testLocation== null){ //If the location is not on the map or times up
								visitingList.remove(targetAttraction);
								if (visitingIndex > visitingList.size()-1){
									visitingIndex= visitingList.size()-1;
								}
						
								targetAttraction= visitingList.get(visitingIndex);
							}
							
							if (fullness > Math.sqrt(rc.getType().sensorRadiusSquared)){ //After you search for a while, sometimes it is better to be content #LIFELESSON
								fullness= 0;
								visitingList.set(visitingIndex, rc.getLocation());
								targetAttraction= rc.getLocation();
							}
						}
					}
				}else{
					currentMode= mode.GO_TO_MOM; //Maybe defending? default case
				}
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
