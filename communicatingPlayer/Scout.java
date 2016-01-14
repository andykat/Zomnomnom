package communicatingPlayer;

import battlecode.common.*;

import java.util.ArrayList;

public class Scout extends RobotRunner {
	private ArrayList<MapLocation> visitingList;
	private MapLocation targetAttraction;
	private MapLocation homeBase;
	static enum mode{HOME,PATROL,SEARCH,READY};
	private mode currentMode;
	private int visitingIndex= 0;
	private boolean needToCheckOnMap= true;
	
	public Scout(RobotController rcin) throws GameActionException {
		super(rcin);
		//mcomp= new OriginMapLocComparator(rc.getLocation()); //Closer to target the better
		visitingList= new ArrayList<MapLocation>();
		visitingList= createDividedRadius(RobotConstants.SCOUT_SEARCH_RANGE,10);
		currentMode= mode.SEARCH;
		homeBase= rc.getLocation();
	}
	
	public void run() throws GameActionException{ //Scout gathers information, shares location with Archon, they go grab it
		if (rc.isCoreReady()){
			switch (currentMode){
			case SEARCH:
				if (targetAttraction== null && visitingList.size()> 0){ //If you currently aren't going any where and you want to go somewhere
					targetAttraction= visitingList.get(visitingIndex);
				}else if (targetAttraction!= null){ //If you want to go somewhere
					//System.out.println("Going somewhere" + visitingIndex);
					if (rc.getLocation().equals(targetAttraction)){//If you are at the same place, set next destination
						visitingIndex+= 1;
						if (visitingIndex > visitingList.size()-1){ //Loopback
							visitingIndex= 0;
							needToCheckOnMap= false;
						}
						targetAttraction= visitingList.get(visitingIndex);
					}else{
						simpleMove(rc.getLocation().directionTo(targetAttraction));
						if (needToCheckOnMap){
							if (rc.canSenseLocation(targetAttraction)){ 
								if (rc.onTheMap(targetAttraction)== false){//Check if the place you want to go is on the map
									visitingList.remove(targetAttraction); //If it isn't remove it
									if (visitingIndex > visitingList.size()-1){
										visitingIndex= visitingList.size()-1;
									}
									targetAttraction= visitingList.get(visitingIndex);
								}
							}
						}
					}
				}else{
					currentMode= mode.HOME; //Maybe defending? 
				}
				break;
			case HOME:
				simpleMove(rc.getLocation().directionTo(homeBase));
				if (rc.canSense(homeBase))
					currentMode= mode.READY;
				break;
			default:
				//Move back towards the archon?
				break;
			}
		}
	}
}
