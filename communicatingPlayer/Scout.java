package communicatingPlayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Scout extends RobotRunner {
	private ArrayList<MapLocation> visitingList;
	private MapLocation targetLocation;
	private enum mode{SEARCH_FOR_OBJ, SET_UP_SEARCH, COMBAT, MOVE_TO_OBJ, GURU};
	private mode currentMode;
	private mode prevMode;
	private int visitingIndex= 0;
	private int searchLevel= 0; //An increasing with equal to with of square/2 by which the scout loops to discover all the goodies
	private RobotInfo enemies[];
	private RobotInfo friends[];
	private boolean surroundingCheckedThisRound= false;
	private boolean needsToFightThisRound= false;

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
				visitingList= createDividedSquareNodes(searchLevel);
				System.out.println("\t"+visitingList.size());
				
				targetLocation= visitingList.get(visitingIndex);
				
				rc.setIndicatorString(0, "Setting up new search " + visitingList.size());
				currentMode= mode.SEARCH_FOR_OBJ;
			}else if (currentMode== mode.SEARCH_FOR_OBJ){	
				rc.setIndicatorString(0, "Searching for object");
				if (targetLocation.equals(rc.getLocation())){ //If you are at the right place
					rc.setIndicatorString(0, "Gathering info");
					rc.setIndicatorDot(targetLocation, 255, 0, 0);
					gatherMapInfo();
					visitingIndex++;
					if (visitingIndex> visitingList.size()-1){
						currentMode= mode.SET_UP_SEARCH;
						visitingList.clear();
						searchLevel+= (robotSenseRadius* Math.sqrt(2));
					}else{
						targetLocation= visitingList.get(visitingIndex);
					}
				}else{
					rc.setIndicatorDot(targetLocation, 255, 192, 203);
					rc.setIndicatorLine(rc.getLocation(), targetLocation, 255, 192, 203);
			
					if (rc.canSense(targetLocation) && !rc.onTheMap(targetLocation)){ //If you see it is not on the map
						senseMapEdges();
						targetLocation= temperLocation(targetLocation);	
					}else if (!rc.canSense(targetLocation)){//It might be you haven't moved to it yet, or it might be that you are too far away to ever see it
						senseMapEdges();
						MapLocation potentialUpdate= temperLocation(targetLocation);
						if (targetLocation.equals(potentialUpdate)){ //Is not cut off by the edges of the known world
							rc.setIndicatorString(0, "tempered");
							bugMove(rc.getLocation(), targetLocation);
						}else{ //If it is actually clamped
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
