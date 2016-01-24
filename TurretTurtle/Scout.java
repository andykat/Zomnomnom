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

public class Scout extends RobotRunner {
	private enum strat{SEARCH, MOVE};
	public static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	private strat curStrat = strat.SEARCH;
	private MapLocation curLoc;
	private MapLocation nearArchonLoc;
	private Map<RobotType, Double> tasty = new EnumMap<RobotType, Double>(RobotType.class);
	private int broadcastRange = RobotType.SCOUT.sensorRadiusSquared*2;
	private MapLocation dest;
	public Scout(RobotController rcin){
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
		initTasty();
	}
	public void run() throws GameActionException{
		if (rc.isCoreReady()){
			if(curStrat == strat.SEARCH){
				ScoutSearch();
			}
			else if(curStrat == strat.MOVE){
				ScoutMove();
			}
		}
	}
	public void ScoutMove() throws GameActionException{
		int dist = marco.bugMove(rc, dest);
		if(dist<1 || dist==99999){
			curStrat = strat.SEARCH;
		}
	}
	public void ScoutSearch() throws GameActionException{
		RobotInfo[] enemies = rc.senseHostileRobots(curLoc, RobotType.SCOUT.sensorRadiusSquared);
		if(enemies.length>0){
			double[] enemyVal = new double[enemies.length];
        	for(int i=0;i<enemies.length;i++){
        		double percentHealth = 1.0 - enemies[i].health/enemies[i].maxHealth;
        		double distValue = 1.0;
        		
        		enemyVal[i] = percentHealth * distValue * tasty.get(enemies[i].type);
        	}
        	int maxN = 3;
        	double[] max = new double[maxN];
        	int[] maxIndex = new int[maxN];
        	for(int i=0;i<maxN;i++){
        		max[i] = -99999;
        		maxIndex[i] = -1;
        	}
        	for(int i=0;i<enemyVal.length;i++){
        		for(int j=0;j<maxN;j++){
	        		if(enemyVal[i] > max[j]){
	        			maxIndex[j] = i;
	        			max[j] = enemyVal[i];
	        			break;
	        		}
        		}
        	}
        	//broadcast top 3 enemies to shoot
        	for(int i=0;i<maxN;i++){
        		if(maxIndex[i]!=-1){
        			MapLocation eLoc = enemies[maxIndex[i]].location;
        			int[] msg = enigma.fastHash(1, eLoc.x, eLoc.y, 0, 0);
        			rc.broadcastMessageSignal(msg[0], msg[1], broadcastRange);
        		}
        	}
		}
	}
	public void initTasty(){
		tasty.put(RobotType.ARCHON, 0.0005);
		tasty.put(RobotType.BIGZOMBIE, 8.0);
		tasty.put(RobotType.FASTZOMBIE, 2.0);
		tasty.put(RobotType.GUARD, 0.04);
		tasty.put(RobotType.RANGEDZOMBIE, 2.0);
		tasty.put(RobotType.SCOUT, 0.025);
		tasty.put(RobotType.SOLDIER, 0.05);
		tasty.put(RobotType.STANDARDZOMBIE, 2.0);
		tasty.put(RobotType.TTM, 0.05);
		tasty.put(RobotType.TURRET, 0.05);
		tasty.put(RobotType.VIPER, 0.05);
		tasty.put(RobotType.ZOMBIEDEN, 0.0001);	
	}
	
	public void readSignals() throws GameActionException{
		Signal[] signals = rc.emptySignalQueue();
		for(int i=0;i<signals.length;i++){
			if(signals[i].getTeam() == myTeam){
				int[] msg = signals[i].getMessage();
				if(msg==null){
					
				}
				else{
					int type = enigma.fastHashType(msg[0]);
					if(type==0){
						if(rc.senseNearbyRobots(2).length>2){
							dest = curLoc.add(nearArchonLoc.directionTo(curLoc), 2); 
							curStrat = strat.MOVE;
						}
					}
				}
			}
		}
	}

}
