package communicatingPlayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Scout extends RobotRunner {
	private ArrayList<MapLocation> visitingList;
	private MapLocation targetNodeLoc;
	private MapLocation targetObjLoc;
	private enum mode{SEARCH_FOR_NEXT_NODE, SET_UP_SEARCH, COMBAT, SEARCH_AND_RESCUE};
	private mode currentMode;
	private mode prevMode;
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

	public void electNewTargetNodeLoc(){ //Find the closest node and set that as the target node location, if there are more than 1 in the visitingList
		if (visitingList.size()> 0){
			MapLocation closestCandidate= visitingList.get(0);
			for (int n= 1; n< visitingList.size(); n++){ //Look for the next closest place to go
				MapLocation contestant= visitingList.get(n);
				if (rc.getLocation().distanceSquaredTo(closestCandidate) > rc.getLocation().distanceSquaredTo(contestant)){
					closestCandidate= contestant;
				}
			}
			targetNodeLoc= closestCandidate;
		}
	}
	
	public void run() throws GameActionException{ //Scout gathers information, shares location with Archon, they go grab it
		if (rc.isCoreReady()){
			for (int n= 0; n< visitingList.size(); n++){
				rc.setIndicatorDot(visitingList.get(n), 0, 0, 255);
			}
			if (targetNodeLoc!= null){//Always update dat shit
				rc.setIndicatorString(0, String.valueOf(targetNodeLoc.x));
				rc.setIndicatorString(1, String.valueOf(targetNodeLoc.y));
			}
			if (currentMode== mode.SET_UP_SEARCH){
				visitingList= createDividedSquarePerimNodes(searchLevel);
				electNewTargetNodeLoc();
				rc.setIndicatorString(0, "Setting up new search " + visitingList.size());
				currentMode= mode.SEARCH_FOR_NEXT_NODE;
			}else if (currentMode== mode.SEARCH_FOR_NEXT_NODE){	//================================================================================
				rc.setIndicatorString(0, "Searching for object");
				if (temperLocation(targetNodeLoc).equals(rc.getLocation())){ //If you are at the right place
					rc.setIndicatorString(0, "Gathering info");
					rc.setIndicatorDot(targetNodeLoc, 255, 0, 0);
					gatherMapInfo(); //scan surrounding for goodies
					visitingList.remove(targetNodeLoc); //You got there, complete quest
					if (visitingList.size()<= 0){ //If there aren't any more new places to go
						currentMode= mode.SET_UP_SEARCH;
						visitingList.clear();
						if (memory.getNumRecordedCorners() == 4){ //If you know the size of the map
							searchLevel= (int) Math.min(memory.getMapLongestDimension(), searchLevel+(robotSenseRadius* Math.sqrt(2))); //Cap search length at max dimension
						}else{
							searchLevel+= (robotSenseRadius* Math.sqrt(2));
						}
					}else{ //Elect the new candidate
						electNewTargetNodeLoc();
					}
					
					if (memory.getNumObjectives()> 0){
						prevMode= currentMode;
						currentMode= mode.SEARCH_AND_RESCUE;
					}
					
				}else{//If you are not at a node	
					MapLocation potentialUpdate= temperLocation(targetNodeLoc);
					if ((rc.canSense(targetNodeLoc) && !rc.onTheMap(targetNodeLoc)) || !rc.canSense(targetNodeLoc)){ //If you see it is not on the map
						if (targetNodeLoc.equals(potentialUpdate)){ //Is not cut off by the edges of the "known" world
							senseMapEdges();
							marco.bugMove(rc, targetNodeLoc);
						}else{ //If the modified version is not the same, modify it
							targetNodeLoc= potentialUpdate;
						}
					}else if (rc.canSense(targetNodeLoc) && rc.onTheMap(targetNodeLoc)){ //if you see it and it is on the map
						marco.bugMove(rc, targetNodeLoc);
					}
				}
			}else if (currentMode== mode.SEARCH_AND_RESCUE){//===================================================================================
				if (targetObjLoc== null){ //Elect a new objective
					MapLocation objLoc= memory.getObjective(rc);
					float checkObjVal= memory.getMapLocValue(objLoc, rc);
					rc.setIndicatorString(0, "SEARCH AND RESCUE: val"+ checkObjVal);
					if (objLoc!= null && checkObjVal > 0){//if it is part of memory, and its value is greater than 0
						//TODO Tell everyone to go somewhere, once
						targetObjLoc= objLoc;
						marco.bugMove(rc, targetObjLoc);
					}else{//Wasting a turn not moving?
						//TODO Tell everyone that the objective is off
						currentMode= prevMode;
						memory.clearObjectives();
					}
				}else{ //End condition of the search
					rc.setIndicatorDot(targetObjLoc, 255, 245, 238);
					if (rc.canSense(targetObjLoc)){
						gatherMapInfo(targetObjLoc);
						if (memory.getMapLocValue(targetObjLoc, rc)<= 0){ //If value less than or equal to zero means the objective is over
							memory.clearObjective(targetObjLoc);
							targetObjLoc= null;
						}else{
							if (rc.getLocation().distanceSquaredTo(targetObjLoc) > 9){
								rc.setIndicatorString(1, "see it, only moving loser");
								marco.bugMove(rc, targetObjLoc);
							}
						}
					}else{ //If you can't sense it, keep moving towards it, this is already (hopefully) the best goal with distance considered
						rc.setIndicatorString(1, "blind moving");
						marco.bugMove(rc, targetObjLoc);
					}
				}
			}
		}
	}
}
