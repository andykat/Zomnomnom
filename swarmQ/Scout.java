package swarmQ;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;

public class Scout extends RobotRunner {
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
	private int closestEnemyDist = 99999;
	private double lastHealth = RobotType.SCOUT.maxHealth;
	private int steps=0;
	private int stepLimit = 3;
	public Scout(RobotController rcin){
		super(rcin);
		curStrat = strat.MEANDER;
		nearArchonLoc = rc.getLocation();
		nearArchonLocDist = 99999;
		nearArchonID = -1;
		broadcastRange = RobotType.SCOUT.sensorRadiusSquared * 2;
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
				scoutMeander();
			}
			else if(curStrat == strat.OBJECTIVE){
				scoutObjective();
			}
			else if(curStrat == strat.FIND_ENEMY){
				scoutFindEnemy();
			}
			else if(curStrat == strat.COMBAT){
				scoutCombat();
			}
			else if(curStrat == strat.KITE){
				scoutKite();
			}
		}
	}
	
	public void scoutMeander(){
		readSignals();
		MapLocation curLoc = rc.getLocation();
		RobotInfo[] enemies = rc.senseHostileRobots(curLoc, RobotType.SCOUT.sensorRadiusSquared);
		if(enemies.length>0){
			//enemy found. must kill
			changeStrat();
			//broadcast
			
			int [] message = enigma.fastHash(0, enemies[0].location.x, enemies[0].location.y, 0, 0);
			try{
				rc.broadcastMessageSignal(message[0], message[1], broadcastRange);
				//rc.broadcastSignal(broadcastRange);
			}
			catch (GameActionException e) {
				e.printStackTrace();
			}
			
			
			if(enemies[0].location.distanceSquaredTo(curLoc)<20){
				curStrat = strat.KITE;
				changeStrat();
			}
		}
		
		
		RobotInfo[] friends = rc.senseNearbyRobots(RobotType.SCOUT.sensorRadiusSquared, myTeam);
		
		
		if(!moving){
			if(curLoc.distanceSquaredTo(archCenter)<RobotType.SCOUT.sensorRadiusSquared || friends.length>8){
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
	public void scoutObjective(){
		
	}
	public void scoutCombat(){
	}
	public void scoutFindEnemy(){
		
	}
	public void scoutKite(){
		RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), RobotType.ARCHON.sensorRadiusSquared);
		try {
			runawayMove(rc, enemies);
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		steps++;
		
		if(steps>=stepLimit){
			//no more enemies
			curStrat = strat.MEANDER;
			changeStrat();
			return;

		}
	}
	
	public void readSignals(){
		Signal[] signals = rc.emptySignalQueue();
		
	}
	public void changeStrat(){
		moving = false;
		closestEnemyDist = 99999;
	}
	public void checkForHurtFriends(){
		
	}
}
