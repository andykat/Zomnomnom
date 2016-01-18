package swarmQ;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;

public class Soldier extends RobotRunner{
	private enum strat{MEANDER,OBJECTIVE,COMBAT,KITE};
	public static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	private strat curStrat;
	private int stepsN = 8;
	private int rounds = 0;
	private boolean moving = false;
	private MapLocation dest;
	private MapLocation nearArchonLoc;
	private MapLocation archCenter;
	private int nearArchonLocDist;
	private int nearArchonID;
	private double archonVectorWeight = 0.6;
	private double friendVectorWeight = 0.4;
	private double vectorLength = 8;
	private int randomLocationDistance = 10;
	public Soldier(RobotController rcin){
		super(rcin);
		curStrat = strat.MEANDER;
		nearArchonLoc = rc.getLocation();
		nearArchonLocDist = 99999;
		nearArchonID = -1;
		
		MapLocation[] archLocs = rc.getInitialArchonLocations(myTeam);
		int centerX=0;
		int centerY=0;
		for(int i=0;i<archLocs.length;i++){
			centerX += archLocs[i].x;
			centerY += archLocs[i].y;
		}
		archCenter = new MapLocation(centerX/archLocs.length, centerY/archLocs.length);
		dest = archCenter;
	}
	public void run() throws GameActionException{
		if (rc.isCoreReady()){
			
			if(curStrat == strat.MEANDER){
				soldierMeander();
			}
			else if(curStrat == strat.OBJECTIVE){
				soldierObjective();
			}
			else if(curStrat == strat.COMBAT){
				soldierCombat();
			}
			else if(curStrat == strat.KITE){
				soldierKite();
			}
		}
	}
	
	public void soldierMeander(){
		MapLocation curLoc = rc.getLocation();
		RobotInfo[] friends = rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadiusSquared, myTeam);
		if(!moving){
			if(curLoc.distanceSquaredTo(archCenter)<RobotType.SOLDIER.sensorRadiusSquared || friends.length>8){
				//move to random location
				dest = dest.add(directions[randall.nextInt(8)], randomLocationDistance);
			}
			else if(curLoc.distanceSquaredTo(dest) < 2){
				dest = dest.add(directions[randall.nextInt(8)], randomLocationDistance);
			}
			moving = true;
			marco.swarmMoveStart();

			marco.swarmMove(rc, dest);
		}
		else{
			int[] moveReturn = marco.swarmMove(rc, dest);
			
			//moved enough, reached destination, or cant do anything, then
			//find new location to move to.
			if(moveReturn[0] == 99999){
				moving = false;
				dest = archCenter;
			}
			else if(moveReturn[0] < 2){
				moving = false;
			}
			else if(moveReturn[1] >= stepsN){
				moving = false;
			}
		}
	}
	public void soldierObjective(){
		
	}
	public void soldierCombat(){
	
	}
	public void soldierKite(){
	
	}
	
	public void readSignals(){
		Signal[] signals = rc.emptySignalQueue();
		for(int i=0;i<signals.length;i++){
    		if(signals[i].getTeam() == myTeam){
        		int[] msg = signals[i].getMessage();
        		if(msg==null){
        			MapLocation ml = signals[i].getLocation();
        			if(signals[i].getID() == nearArchonID){
        				nearArchonLoc = ml;
        				nearArchonLocDist = rc.getLocation().distanceSquaredTo(ml);
        			}
        			else{
	        			int dist = rc.getLocation().distanceSquaredTo(ml);
	        			if(dist < nearArchonLocDist ){
	        				nearArchonLocDist = dist;
	        				nearArchonID = signals[i].getID();
	        				nearArchonLoc = ml;
	        			}
        			}
        		}
    		}
    	}
	}
	public void changeStrat(){
		moving = false;
	}
}
//old moving code
/*int friendCheckN = min(friends.length, 8);

if(friendCheckN ==0){
	dest = new MapLocation((2*curLoc.x - nearArchonLoc.x)*2, (2*curLoc.y - nearArchonLoc.y)*2);
}
else{
	int indexSave = 0;
	int maxdist = nearArchonLoc.distanceSquaredTo(friends[0].location);
	for(int i=1;i<friendCheckN;i++){
		if(nearArchonLoc.distanceSquaredTo(friends[i].location) > maxdist){
			indexSave = i;
			maxdist = nearArchonLoc.distanceSquaredTo(friends[i].location);
		}
	}
	double dist = Math.sqrt((double)maxdist);
	double fX = ((double)(curLoc.x - friends[indexSave].location.x))/dist;
	double fY = ((double)(curLoc.y - friends[indexSave].location.y))/dist;
	double adist = Math.sqrt((double)nearArchonLocDist);
	double aX = ((double)(curLoc.x - nearArchonLoc.x))/adist;
	double aY = ((double)(curLoc.y - nearArchonLoc.y))/adist;
	
	dest = new MapLocation(((int)((fX*friendVectorWeight + aX*archonVectorWeight)*vectorLength)) + curLoc.x, 
			((int)((fY*friendVectorWeight + aY*archonVectorWeight)*vectorLength)) + curLoc.y);
}


moving = true;
marco.swarmMoveStart();

marco.swarmMove(rc, dest);*/