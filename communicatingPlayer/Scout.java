package communicatingPlayer;

import battlecode.common.*;

import java.util.ArrayList;

public class Scout extends RobotRunner {
	private RobotInfo mom;
	private ArrayList<MapLocation> visitingList;
	private MapLocation targetAttraction;
	private MapLocation homeBase;
	private enum mode{GET_SEED, LOOK_FOR_MOM,GO_TO_MOM,PATROL,SEARCH,TALK_TO_MOM};
	private mode currentMode;
	private int visitingIndex= 0;
	private int fullness= 0; //Once it exceeds the scout hunger amount, gets reset to zero and move on to next target
	private int searchLevel= 0; //Must be > 1
	private int roundSearchStart;
	private int roundSeearchStopped;
	private boolean travelTempered= false; //Whether the map has been tempered by the world edges
	
	public Scout(RobotController rcin) throws GameActionException {
		super(rcin);
		visitingList= new ArrayList<MapLocation>();
		visitingList= createDividedRadius(RobotConstants.SCOUT_SEARCH_RANGE,searchLevel);
		currentMode= mode.LOOK_FOR_MOM;
		homeBase= rc.getLocation();
	}
	
	public void run() throws GameActionException{ //Scout gathers information, shares location with Archon, they go grab it
		//280 max for sesnor radius
		if (rc.isCoreReady()){
			
			if (targetAttraction!= null){
				rc.setIndicatorString(0, String.valueOf(targetAttraction.x));
				rc.setIndicatorString(1, String.valueOf(targetAttraction.y));
			}
			
			switch (currentMode){
			case LOOK_FOR_MOM:
				RobotInfo[] fambam= rc.senseNearbyRobots(2);
				for (int n= 0; n< fambam.length; n++){
					if (fambam[n].type.equals(RobotType.ARCHON)){
						mom= fambam[n];
						currentMode= mode.SEARCH;
						roundSearchStart= rc.getRoundNum();
						break;
					}
				}
				
				break;
			
			case SEARCH://=============================================================
				if (targetAttraction== null && visitingList.size()> 0){ //If you currently aren't going any where and you want to go somewhere
					targetAttraction= visitingList.get(visitingIndex);
				}else if (targetAttraction!= null){ //If you want to go somewhere
					if (rc.getLocation().equals(targetAttraction)){//If you are at designated node
						
						//GATHERING INFORMATION====================================
						
						boolean sensed= false;
						if (rc.getLocation().distanceSquaredTo(visitingList.get((visitingIndex+1)%visitingList.size())) > rc.getType().sensorRadiusSquared/2){
							gatherMapInfo();
							sensed= true;
						}
						//SET UP FOR NEXT NODE
						visitingIndex+= 1;
						fullness= 0;
						if (visitingIndex > visitingList.size()-1){ 
							currentMode= mode.GO_TO_MOM;
						}
						
						if (visitingList.size() > 0){
							targetAttraction= visitingList.get(visitingIndex % visitingList.size());
						}else{
							currentMode= mode.GO_TO_MOM; //Happens because of tempering
						}
						
					}else{ //if you are not yet on the destination
						bugMove(rc.getLocation(), targetAttraction);
						if (rc.canSense(targetAttraction)){
							if (!rc.onTheMap(targetAttraction)){
								visitingIndex++; //Moving on
								if (visitingIndex> visitingList.size()- 1){
									currentMode= mode.GO_TO_MOM;
								}else{
									targetAttraction= visitingList.get(visitingIndex);
								}
							}
						}
						fullness+= 1;
						if (fullness > robotSenseRadius){ //After you search for a while, sometimes it is better to be content with where you are #LIFELESSON
							fullness= 0;
							//visitingList.set(visitingIndex, rc.getLocation());
							targetAttraction= rc.getLocation();
						}
					}
				}else{
					currentMode= mode.GO_TO_MOM; //Maybe defending? default case
				}
				break;
			case GO_TO_MOM:
				//simpleMove(rc.getLocation().directionTo(homeBase));
				
				bugMove(rc.getLocation(),homeBase);
				
				if (rc.getLocation().distanceSquaredTo(homeBase)<= 0){ //Go whisper into the archon's ears
					//memory.printInfo();
					System.out.println("ID: " + rc.getID()+ ": "+ rc.getRoundNum()+ ": completed with " + memory.getBrainSize());
					currentMode= mode.TALK_TO_MOM;
				}
				break;
			case TALK_TO_MOM:
				//When you're done
				targetAttraction= null;
				visitingList.clear();
				travelTempered= false;
				searchLevel+= RobotConstants.SCOUT_SEARCH_RANGE*2;
				visitingList= createDividedRadius(RobotConstants.SCOUT_SEARCH_RANGE,searchLevel);
				visitingIndex= 0;
				
				//punch time card
				roundSeearchStopped= rc.getRoundNum();
				if (searchLevel > 80){//Done searching! 
					System.out.println("Done searching!");
					currentMode= mode.PATROL;
				}else{
					roundSearchStart= rc.getRoundNum();
					if (rc.canSenseRobot(mom.ID)){
						currentMode= mode.SEARCH;
					}
				}
				break;
			default:
				//Move back towards the archon?
				break;
			}
		}
	}
	
	private void gatherMapInfo() throws GameActionException{
		rc.setIndicatorString(0, "Sensing");
		RobotInfo[] enemyRobotsInRange = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
		for (int n= 0; n< enemyRobotsInRange.length; n++){
			if (enemyRobotsInRange[n].type==RobotType.ZOMBIEDEN){
				memory.addMapInfo(enemyRobotsInRange[n].location, (int) enemyRobotsInRange[n].health, RobotConstants.mapTypes.ZOMBIE_DEN);	
			}
		}
		
		RobotInfo[] neutralRobotsInRange = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.NEUTRAL);
		for (int n= 0; n< neutralRobotsInRange.length; n++){
			memory.addMapInfo(neutralRobotsInRange[n].location, neutralRobotsInRange[n]);
		}
		
		MapLocation[] partsLoc= rc.sensePartLocations(rc.getType().sensorRadiusSquared);
		for (int n= 0; n< partsLoc.length; n++){
			memory.addMapInfo(partsLoc[n], (int)rc.senseParts(partsLoc[n]),RobotConstants.mapTypes.PARTS);
		}
		 
		for (int x= -robotSenseRadius+rc.getLocation().x; x< robotSenseRadius+rc.getLocation().x; x++){
			for (int y= -robotSenseRadius+rc.getLocation().y; y< robotSenseRadius+rc.getLocation().y; y++){
				MapLocation underView= new MapLocation(x,y);
				if (rc.canSense(underView) && rc.onTheMap(underView)){
					int rubbles= (int) rc.senseRubble(underView);
					//System.out.println("rubble: "+parts);
					if (rubbles> 0){
						memory.addMapInfo(underView, rubbles,RobotConstants.mapTypes.RUBBLE);
					}
				}else{//if out of range or out of map
					if (rc.canSense(underView) && !rc.onTheMap(underView)){//if you can sense it and it is out of the map, becomes candidate for max min edge value check
						Direction edgeDir= rc.getLocation().directionTo(underView);	
						memory.addCornerValueCandidate(edgeDir, underView);
					}
					if (memory.getNumRecordedCorners() > 0){
						if (travelTempered== false){ //If the visiting list has not yet been fixed by the world edges
							int[] corners= memory.getCorners();
							//temperingVisitingList(visitingList, corners[0],corners[1],corners[2],corners[3]); //root 2 ~ 1.71 so round to 2
							travelTempered= true;
							//System.out.println("Corners: " + memory.getNumRecordedCorners());
						}
					}
				}
			 }
		 }
		rc.setIndicatorString(0, "not sensing");
	}
}
