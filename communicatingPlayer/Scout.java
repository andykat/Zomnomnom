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
	private Information memory;
	private int fullness= 0; //Once it exceeds the scout hunger amount, gets reset to zero and move on to next target
	private int searchLevel= 4; //Must be > 1
	private int roundSearchStart;
	private int roundSeearchStopped;
	private int previousRoundTime;
	private boolean travelTruncated= false; //Whether the map has been tempered by the world edges
	
	public Scout(RobotController rcin) throws GameActionException {
		super(rcin);
		visitingList= new ArrayList<MapLocation>();
		visitingList= createDividedRadius(RobotConstants.SCOUT_SEARCH_RANGE,searchLevel);
		currentMode= mode.LOOK_FOR_MOM;
		homeBase= rc.getLocation();
		memory= new Information();
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
						currentMode= mode.SEARCH;
						roundSearchStart= rc.getRoundNum();
						break;
					}
				}
				
				break;
			
			case SEARCH://=============================================================
				//System.out.println("fullness: "+ fullness);
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
						
						//GATHERING INFORMATION====================================
						
						RobotInfo[] enemyRobotsInRange = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
						for (int n= 0; n< enemyRobotsInRange.length; n++){
							if (enemyRobotsInRange[n].type==RobotType.ZOMBIEDEN){
								memory.addMapInfo(enemyRobotsInRange[n].location, (int) enemyRobotsInRange[n].health, RobotConstants.mapTypes.ZOMBIE_DEN);	
							}
						}
						
						RobotInfo[] neutralRobotsInRange = rc.senseNearbyRobots(RobotType.SCOUT.sensorRadiusSquared, Team.NEUTRAL);
						for (int n= 0; n< neutralRobotsInRange.length; n++){
							memory.addMapInfo(neutralRobotsInRange[n].location, neutralRobotsInRange[n]);
						}
						
						MapLocation[] partsLoc= rc.sensePartLocations(RobotType.SCOUT.sensorRadiusSquared);
						for (int n= 0; n< partsLoc.length; n++){
							memory.addMapInfo(partsLoc[n], (int)rc.senseParts(partsLoc[n]),RobotConstants.mapTypes.PARTS);
						}
						 
						for (int x= (int) -Math.sqrt(rc.getType().sensorRadiusSquared)+rc.getLocation().x; x< Math.sqrt(rc.getType().sensorRadiusSquared)+rc.getLocation().x; x++){
							for (int y= (int) -Math.sqrt(rc.getType().sensorRadiusSquared)+rc.getLocation().y; y< Math.sqrt(rc.getType().sensorRadiusSquared)+rc.getLocation().y; y++){
								MapLocation underView= new MapLocation(x,y);
								if (checkLocation(underView)!= null){
									int rubbles= (int) rc.senseRubble(underView);
									//System.out.println("rubble: "+parts);
									if (rubbles> 0){
										memory.addMapInfo(underView, rubbles,RobotConstants.mapTypes.RUBBLE);
									}else{
									}
								}else{//if out of range or out of map
									System.out.println("Num recoreded edges: "+ memory.getNumRecordedCorners());
									if (memory.getNumRecordedCorners()< 4){
										if (rc.canSense(underView) && !rc.onTheMap(underView)){//if you can sense it and it is out of the map, becomes candidate for max min edge value check
											Direction edgeDir= rc.getLocation().directionTo(underView);							 
											memory.addCornerValueCandidate(edgeDir, underView);
										}
									}else{
										//You got all edges!
										if (!travelTruncated){ //If the visiting list has not yet been fixed by the world edges
											int[] corners= memory.getCorners();
											truncateVisitingList(visitingList, corners[0],corners[1],corners[2],corners[3]);
										}
									}
								}
							 }
						 }
					}else{ //if you are not yet on the destination
						//simpleMove(rc.getLocation().directionTo(targetAttraction));
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
						
						if (fullness > Math.sqrt(rc.getType().sensorRadiusSquared)){ //After you search for a while, sometimes it is better to be content with where you are #LIFELESSON
							fullness= 0;
							visitingList.set(visitingIndex, rc.getLocation());
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
					System.out.println(rc.getRoundNum()+ ": completed with " + memory.getBrainSize());
					currentMode= mode.TALK_TO_MOM;
				}
				break;
			case TALK_TO_MOM:
				//When you're done
				targetAttraction= null;
				visitingList.clear();
				travelTruncated= false;
				searchLevel+= RobotConstants.SCOUT_SEARCH_RANGE*2;
				visitingList= createDividedRadius(RobotConstants.SCOUT_SEARCH_RANGE,searchLevel);
				
				//punch time card
				roundSeearchStopped= rc.getRoundNum();
				int testRoundNum= roundSeearchStopped- roundSearchStart;
				if (previousRoundTime == testRoundNum || searchLevel > 80){//Done searching! 
					System.out.println("Done searching!");
					currentMode= mode.PATROL;
				}else{
					previousRoundTime= testRoundNum;
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
}
