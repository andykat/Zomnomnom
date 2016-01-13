package communicatingPlayer;

import battlecode.common.*;
import java.util.PriorityQueue;

public class Scout extends RobotRunner {
	static OriginMapLocComparator mcomp;
	static PriorityQueue<MapLocation> visitingList;
	static MapLocation targetAttraction;
	static enum mode{SEARCHXY, SEARCH,SCOUT,EVADE}; //https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html
	static mode currentMode;
	private int xMax;
	private int yMax;
	
	public Scout(RobotController rcin) {
		super(rcin);
		mcomp= new OriginMapLocComparator(rc.getLocation()); //Closer to target the better
		visitingList= new PriorityQueue<MapLocation>(10, mcomp);
		createDividedGrid(RobotConstants.SCOUT_SEARCH_RANGE);
		currentMode= mode.SEARCHXY;
	}

	public static void createDividedGrid(int searchRange){ //421,173?
		//http://stackoverflow.com/questions/683041/java-how-do-i-use-a-priorityqueue
		int xStart= 421;
		int yStart= 173;
		
		for (int x= xStart+ GameConstants.MAP_MIN_WIDTH; x< xStart+ GameConstants.MAP_MAX_WIDTH; x+= searchRange){
			for (int y= yStart+ GameConstants.MAP_MIN_HEIGHT; y< yStart+ GameConstants.MAP_MAX_HEIGHT; y+= searchRange){
				visitingList.add(new MapLocation(x,y));
			}
		}
	}
	
	public void run() throws GameActionException{ //Scout g	athers information, shares location with Archon, they go grab it
		if (rc.isCoreReady()){
			switch (currentMode){
			case SEARCH:
				if (targetAttraction== null && visitingList.size()> 0){ //If you currently aren't going any where and you want to go somewhere
					targetAttraction= visitingList.poll(); 
				}else if (targetAttraction!= null){ //If you want to go somewhere
					System.out.println("tring to go somewhere");
					System.out.println(targetAttraction.toString());
					if (rc.getLocation().equals(targetAttraction)){//If you are at the same place
						targetAttraction= visitingList.poll(); //Might be null
					}else{
						simpleMove(rc.getLocation().directionTo(targetAttraction));
					}
				}else{
					currentMode= mode.SCOUT; //Maybe defending? 
				}
				break;
				
			default:
				//Not sure yet
				break;
			}
		}
	}
}
