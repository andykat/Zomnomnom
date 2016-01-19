package team302;

import java.util.EnumMap;
import java.util.Map;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;

public class Turret extends RobotRunner{
	private enum strat{MEANDER,OBJECTIVE,COMBAT,KITE,FIND_ENEMY};
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
	private int broadcastRange;
	private MapLocation closestEnemyLoc;
	private int closestEnemyID;
	private int closestEnemySignal = 0;
	private int closestEnemyDist = 99999;
	private double lastHealth = RobotType.TURRET.maxHealth;
	private int steps=0;
	private int stepLimit = 3;
	private Map<RobotType, Double> tasty = new EnumMap<RobotType, Double>(RobotType.class);
	public Turret(RobotController rcin){
		super(rcin);
		curStrat = strat.COMBAT;
		nearArchonLoc = rc.getLocation();
		nearArchonLocDist = 99999;
		nearArchonID = -1;
		broadcastRange = RobotType.TURRET.sensorRadiusSquared * 5;
		MapLocation[] archLocs = rc.getInitialArchonLocations(myTeam);
		int centerX=0;
		int centerY=0;
		for(int i=0;i<archLocs.length;i++){
			centerX += archLocs[i].x;
			centerY += archLocs[i].y;
		}
		archCenter = new MapLocation(centerX/archLocs.length, centerY/archLocs.length);
		dest = archCenter;
		initTasty();
	}
	public void run() throws GameActionException{
		if (rc.isCoreReady()){
			

			if(curStrat == strat.COMBAT){
				turretCombat();
			}

		}
	}
	

	public void turretCombat(){
		MapLocation curLoc = rc.getLocation();

		lastHealth = rc.getHealth();
		RobotInfo[] enemies = rc.senseHostileRobots(curLoc, RobotType.TURRET.sensorRadiusSquared);
        if(enemies.length > 0) {
        	double[] enemyVal = new double[enemies.length];
        	for(int i=0;i<enemies.length;i++){
        		double percentHealth = 1.0 - enemies[i].health/enemies[i].maxHealth;
        		double distValue = 1.0;
        		if(curLoc.distanceSquaredTo(enemies[i].location) > RobotType.TURRET.attackRadiusSquared){
        			double dist = (double)(curLoc.distanceSquaredTo(enemies[i].location) - RobotType.TURRET.attackRadiusSquared);
        			distValue -= 0.2;
        			distValue -= dist/((double)RobotType.TURRET.attackRadiusSquared);
        		}
        		enemyVal[i] = percentHealth * distValue * tasty.get(enemies[i].type);
        	}
        	double max = enemyVal[0];
        	int maxIndex = 0;
        	for(int i=1;i<enemyVal.length;i++){
        		if(enemyVal[i] > max){
        			maxIndex = i;
        			max = enemyVal[i];
        		}
        	}
        	
        	//if target is within attack range, then attack
        	if(rc.canAttackLocation(enemies[maxIndex].location)){
        		try{
        			rc.attackLocation(enemies[maxIndex].location);
        		}catch (GameActionException e) {
    				e.printStackTrace();
    			}
        	}
        	
        }
        
        
	}
	
	
	
	public void readSignals(){
		Signal[] signals = rc.emptySignalQueue();
		
	}
	public void changeStrat(){
		moving = false;
		closestEnemyDist = 99999;
		closestEnemySignal = 0;
	}
	public void checkForHurtFriends(){
		
	}
	public void initTasty(){
		tasty.put(RobotType.ARCHON, 0.05);
		tasty.put(RobotType.BIGZOMBIE, 1.0);
		tasty.put(RobotType.FASTZOMBIE, 1.0);
		tasty.put(RobotType.GUARD, 1.0);
		tasty.put(RobotType.RANGEDZOMBIE, 1.0);
		tasty.put(RobotType.SCOUT, 0.5);
		tasty.put(RobotType.SOLDIER, 1.0);
		tasty.put(RobotType.STANDARDZOMBIE, 1.0);
		tasty.put(RobotType.TTM, 0.5);
		tasty.put(RobotType.TURRET, 2.0);
		tasty.put(RobotType.VIPER, 2.0);
		tasty.put(RobotType.ZOMBIEDEN, 0.01);	
	}
}
