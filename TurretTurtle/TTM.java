package TurretTurtle;

import java.util.EnumMap;
import java.util.Map;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;

public class TTM extends RobotRunner{
	private enum strat{MOVE, UNPACK};
	public static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	private strat curStrat = strat.MOVE;
	private MapLocation curLoc;
	private MapLocation nearArchonLoc;
	private MapLocation dest;
	public TTM(RobotController rcin){
		super(rcin);
		curLoc = rc.getLocation();
		//get grouping point for the swarm and find the direction to it
		MapLocation[] archLocs = rc.getInitialArchonLocations(myTeam);

		int centerX=0;
		int centerY=0;
		for(int i=0;i<archLocs.length;i++){
			centerX += archLocs[i].x;
			centerY += archLocs[i].y;
		}
		MapLocation center = new MapLocation(centerX /= archLocs.length, centerY /= archLocs.length);
		int min = 99999;
		for(int i=0;i<archLocs.length;i++){
			if(center.distanceSquaredTo(archLocs[i]) < min){
				min = center.distanceSquaredTo(archLocs[i]);
				nearArchonLoc = archLocs[i];
			}
		}
		dest = curLoc.add(nearArchonLoc.directionTo(curLoc), 2); 
	}
	public void run() throws GameActionException{
		if (rc.isCoreReady()){
			rc.unpack();
			/*if(curStrat == strat.MOVE){
				TTMMove();
			}
			else if(curStrat == strat.UNPACK){
				TTMUnpack();
			}*/
		}
	}
	public void TTMMove() throws GameActionException{
		int dist = marco.bugMove(rc, dest);
		if(dist<1 || dist==99999){
			curStrat = strat.UNPACK;
		}
	}
	public void TTMUnpack() throws GameActionException{
		rc.unpack();
	}

}
