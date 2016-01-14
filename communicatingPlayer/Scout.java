package communicatingPlayer;

import battlecode.common.*;

import java.util.ArrayList;

public class Scout extends RobotRunner {
	private RobotInfo mom;
	private ArrayList<MapLocation> visitingList;
	private MapLocation targetAttraction;
	private MapLocation homeBase;
	private enum mode{LOOK_FOR_MOM,GO_TO_MOM,PATROL,SEARCH,TALK_TO_MOM};
	private mode currentMode;
	private int visitingIndex= 0;
	private boolean needToCheckOnMap= true;
	private Information memory;
	
	public Scout(RobotController rcin) throws GameActionException {
		super(rcin);
		visitingList= new ArrayList<MapLocation>();
		visitingList= createDividedRadius(RobotConstants.SCOUT_SEARCH_RANGE,10);
		currentMode= mode.LOOK_FOR_MOM;
		homeBase= rc.getLocation();
		memory= new Information();
	}
	
	public void run() throws GameActionException{ //Scout gathers information, shares location with Archon, they go grab it
		//280 max for sesnor radius
		if (rc.isCoreReady()){
			switch (currentMode){
			case LOOK_FOR_MOM:
				RobotInfo[] fambam= rc.senseNearbyRobots(1);
				for (int n= 0; n< fambam.length; n++){
					if (fambam[n].type.equals(RobotType.ARCHON)){
						mom= fambam[n];
						currentMode= mode.SEARCH;
						break;
					}
				}
				
				break;
			
			case SEARCH://=============================================================
				if (targetAttraction== null && visitingList.size()> 0){ //If you currently aren't going any where and you want to go somewhere
					targetAttraction= visitingList.get(visitingIndex);
				}else if (targetAttraction!= null){ //If you want to go somewhere
					if (rc.getLocation().equals(targetAttraction)){//If you are at designated node
						visitingIndex+= 1;
						if (visitingIndex > visitingList.size()-1){ 
							visitingIndex= 0; //Loop back
							needToCheckOnMap= false; //You've gone through the entire loop once
							currentMode= mode.GO_TO_MOM;
						}
						targetAttraction= visitingList.get(visitingIndex);
						
						//GATHERING INFORMATION====================================
						
						RobotInfo[] enemyRobotsInRange = rc.senseNearbyRobots(RobotType.SCOUT.sensorRadiusSquared, Team.ZOMBIE);
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
						 
						for (int x= (int) -Math.sqrt(RobotType.SCOUT.sensorRadiusSquared)+rc.getLocation().x; x< Math.sqrt(RobotType.SCOUT.sensorRadiusSquared)+rc.getLocation().x; x++){
							for (int y= (int) -Math.sqrt(RobotType.SCOUT.sensorRadiusSquared)+rc.getLocation().y; y< Math.sqrt(RobotType.SCOUT.sensorRadiusSquared)+rc.getLocation().y; y++){
								MapLocation underView= new MapLocation(x,y);
								if (checkLocation(underView)!= null){
									int rubbles= (int) rc.senseRubble(underView);
									//System.out.println("rubble: "+parts);
									if (rubbles> 0){
										memory.addMapInfo(underView, rubbles,RobotConstants.mapTypes.RUBBLE);
									}
								}
							 }
						 }
					}else{ //if you are not yet on the destination
						simpleMove(rc.getLocation().directionTo(targetAttraction));
						if (needToCheckOnMap){
							MapLocation testLocation= checkLocation(targetAttraction);
							if (testLocation== null){ //If the location is not on the map
								visitingList.remove(targetAttraction);
								if (visitingIndex > visitingList.size()-1){
									visitingIndex= visitingList.size()-1;
								}
								targetAttraction= visitingList.get(visitingIndex);
							}
						}
					}
				}else{
					currentMode= mode.GO_TO_MOM; //Maybe defending? default case
				}
				break;
			case GO_TO_MOM:
				simpleMove(rc.getLocation().directionTo(homeBase));
				if (rc.getLocation().distanceSquaredTo(homeBase)<= 0){ //Go whisper into the archon's ears
					//memory.printInfo();
					System.out.println(rc.getRoundNum()+ ": completed with " + memory.getBrainSize());
					currentMode= mode.TALK_TO_MOM;
				}
				break;
			case TALK_TO_MOM:
				
				break;
			default:
				//Move back towards the archon?
				break;
			}
		}
	}
}
