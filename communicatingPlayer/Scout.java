package communicatingPlayer;

import battlecode.common.*;

import java.util.PriorityQueue;

public class Scout extends RobotRunner {
	static OriginMapLocComparator mcomp;
	static PriorityQueue<MapLocation> visitingList;
	static MapLocation targetAttraction;
	static MapLocation homeBase;
	static enum mode{HOME,SEARCHXY,SEARCH,ZOMLEAD,ZOMBIELOVE,READY};
	static mode currentMode;
	private int xMax= -1;
	private int yMax= -1;
	
	public Scout(RobotController rcin) {
		super(rcin);
		mcomp= new OriginMapLocComparator(rc.getLocation()); //Closer to target the better
		visitingList= new PriorityQueue<MapLocation>(10, mcomp);
		createDividedGrid(RobotConstants.SCOUT_SEARCH_RANGE);
		currentMode= mode.SEARCH;
		homeBase= rc.getLocation();
	}

	public static void createDividedGrid(int searchRange){ //421,173?
		//http://stackoverflow.com/questions/683041/java-how-do-i-use-a-priorityqueue
		int xStart= 421;
		int yStart= 144;
		
		for (int x= xStart; x< xStart+ GameConstants.MAP_MIN_WIDTH; x+= searchRange){
			for (int y= yStart; y< yStart+ GameConstants.MAP_MIN_HEIGHT; y+= searchRange){
				//visitingList.add(new MapLocation(x,y));
				visitingList.add(new MapLocation(x,y));
			}
		}
	}
	
	public void run() throws GameActionException{ //Scout gathers information, shares location with Archon, they go grab it
		if (rc.isCoreReady()){
			switch (currentMode){
			case SEARCH:
				if (targetAttraction== null && visitingList.size()> 0){ //If you currently aren't going any where and you want to go somewhere
					targetAttraction= visitingList.poll();
				}else if (targetAttraction!= null){ //If you want to go somewhere
					//System.out.println(targetAttraction.toString());
					if (rc.getLocation().equals(targetAttraction)){//If you are at the same place
						targetAttraction= visitingList.poll();
					}else{
						simpleMove(rc.getLocation().directionTo(targetAttraction));
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
			case SEARCHXY:
				if (xMax!= -1 && yMax!= -1){
					currentMode= mode.READY;
				}else{
					
				}
				break;
			default:
				//Move back towards the archon?
				break;
			}
		}
	}
}
