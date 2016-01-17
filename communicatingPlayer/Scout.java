package communicatingPlayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Scout extends RobotRunner {
	private ArrayList<MapLocation> visitingList;
	private MapLocation targetLocation;
	private enum mode{SEARCH_FOR_NEXT_NODE, SET_UP_SEARCH, COMBAT, MOVE_TO_OBJ};
	private mode currentMode;
	private mode prevMode;
	private int visitingIndex= 0;
	private int searchLevel= 0; //An increasing with equal to with of square/2 by which the scout loops to discover all the goodies
	private RobotInfo enemies[];
	private RobotInfo friends[];
	private boolean surroundingCheckedThisRound= false;
	private boolean needsToFightThisRound= false;
	private int finalLaps= 0;

	public Scout(RobotController rcin) throws GameActionException {
		super(rcin);
		visitingList= new ArrayList<MapLocation>();
		currentMode= mode.SET_UP_SEARCH;
	}
	
	public void checkSurrounding(){
		System.out.println("Surrounding checked");
		if (!surroundingCheckedThisRound){
			enemies= rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
			friends= rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, myTeam);
			needsToFightThisRound= friends.length <= enemies.length/2; //Being cautious, if we want to be completely paranoid we can literally just fight the second we sense any enemies
		}
	}
	
	public void debugPrint(){
		System.out.println("VISITING LIST: " + visitingList.size());
		System.out.println("VISITING INDEX: " + visitingIndex);
		System.out.println("CURRENT MODE: " + currentMode);
	}
	
	public void run() throws GameActionException{ //Scout gathers information, shares location with Archon, they go grab it
		if (rc.isCoreReady()){
			for (int n= 0; n< visitingList.size(); n++){
				rc.setIndicatorDot(visitingList.get(n), 0, 0, 255);
			}
			if (targetLocation!= null){//Always update dat shit
				rc.setIndicatorString(0, String.valueOf(targetLocation.x));
				rc.setIndicatorString(1, String.valueOf(targetLocation.y));
			}
			if (currentMode== mode.SET_UP_SEARCH){
				//debugPrint();
				System.out.println("\t" + rc.getRoundNum()+":  brain size: "+memory.getBrainSize());
				visitingIndex= 0;
				
				if (finalLaps< 2){ //If you have yet to travel the whole world
					visitingList= createDividedSquarePerimNodes(searchLevel);
				}else{
					visitingList= createDividedSquareNodes(memory.getMapLongestDimension());
				}
				
				
				System.out.println("\t"+visitingList.size());
				
				targetLocation= visitingList.get(visitingIndex);
				rc.setIndicatorString(0, "Setting up new search " + visitingList.size());
				currentMode= mode.SEARCH_FOR_NEXT_NODE;
			}else if (currentMode== mode.SEARCH_FOR_NEXT_NODE){	
				rc.setIndicatorString(0, "Searching for object");
				if (targetLocation.equals(rc.getLocation())){ //If you are at the right place
					rc.setIndicatorString(0, "Gathering info");
					rc.setIndicatorDot(targetLocation, 255, 0, 0);
					gatherMapInfo();
					visitingIndex++;
					if (visitingIndex> visitingList.size()-1){
						currentMode= mode.SET_UP_SEARCH;
						visitingList.clear();
						if (memory.getNumRecordedCorners() == 4){ //If you know the size of the map
							int checkSearch= searchLevel+ (int) (robotSenseRadius* Math.sqrt(2));
							if (checkSearch> memory.getMapLongestDimension()){
								finalLaps+= 1;  //Completing the final laps
							}
							searchLevel= (int) Math.min(memory.getMapLongestDimension(), searchLevel+(robotSenseRadius* Math.sqrt(2))); //Cap search length at max dimension
						}else{
							searchLevel+= (robotSenseRadius* Math.sqrt(2));
						}
					}else{
						targetLocation= visitingList.get(visitingIndex);
					}
				}else{
					rc.setIndicatorDot(targetLocation, 255, 192, 203);
					rc.setIndicatorLine(rc.getLocation(), targetLocation, 255, 192, 203);
					
					MapLocation potentialUpdate= temperLocation(targetLocation);
					if ((rc.canSense(targetLocation) && !rc.onTheMap(targetLocation)) || !rc.canSense(targetLocation)){ //If you see it is not on the map
						if (targetLocation.equals(potentialUpdate)){ //Is not cut off by the edges of the "known" world
							senseMapEdges();
							bugMove(rc.getLocation(), targetLocation);
						}else{ //If the modified version is not the same, modify it
							targetLocation= potentialUpdate;
						}
					}else if (rc.canSense(targetLocation) && rc.onTheMap(targetLocation)){ //if you see it and it is on the map
						bugMove(rc.getLocation(), targetLocation);
					}
				}
			}
		}
	}
}
