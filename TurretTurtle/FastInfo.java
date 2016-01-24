package TurretTurtle;

import java.util.ArrayList;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class FastInfo {
	public ArrayList<MapLocation> collectedNodes = new ArrayList<MapLocation>();
	public ArrayList<MapLocation> objectives = new ArrayList<MapLocation>();
	public ArrayList<Integer> objTypes = new ArrayList<Integer>();
	private int senseRangeThresh;
	public FastInfo(int srt){
		senseRangeThresh = srt;
	}
	public boolean checkNode(MapLocation ml){
		if(objectives.size()>0){
			return false;
		}
		for(int i=0;i<collectedNodes.size();i++){
			if(ml.distanceSquaredTo(collectedNodes.get(i)) < senseRangeThresh){
				return false;
			}
		}
		return true;
	}
	public void scanNode(RobotController tRC){
		RobotInfo[] robots = tRC.senseNearbyRobots();
		for(int i=0;i<robots.length;i++){
			if(robots[i].team == Team.NEUTRAL){
				objectives.add(robots[i].location);
				objTypes.add(1);
			}
		}
		MapLocation[] partLocs = tRC.sensePartLocations(tRC.getType().sensorRadiusSquared);
		for(int i=0;i<partLocs.length;i++){
			objectives.add(partLocs[i]);
			objTypes.add(2);
		}
	}

	
}
