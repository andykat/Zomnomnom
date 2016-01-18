package swarmQ;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;

public class Guard extends RobotRunner{
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
	private double lastHealth = RobotType.GUARD.maxHealth;
	private int steps=0;
	private int stepLimit = 3;
	public Guard(RobotController rcin){
		super(rcin);
		curStrat = strat.MEANDER;
		nearArchonLoc = rc.getLocation();
		nearArchonLocDist = 99999;
		nearArchonID = -1;
		broadcastRange = RobotType.GUARD.sensorRadiusSquared * 5;
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
				guardMeander();
			}
			else if(curStrat == strat.OBJECTIVE){
				guardObjective();
			}
			else if(curStrat == strat.FIND_ENEMY){
				guardFindEnemy();
			}
			else if(curStrat == strat.COMBAT){
				guardCombat();
			}
			else if(curStrat == strat.KITE){
				guardKite();
			}
		}
	}
	
	public void guardMeander(){
		readSignals();
		MapLocation curLoc = rc.getLocation();
		RobotInfo[] enemies = rc.senseHostileRobots(curLoc, RobotType.GUARD.sensorRadiusSquared);
		if(enemies.length>0){
			//enemy found. must kill
			changeStrat();
			//broadcast
			
			try{
				rc.broadcastSignal(broadcastRange);
			}
			catch (GameActionException e) {
				e.printStackTrace();
			}
			curStrat = strat.COMBAT;
		}
		
		
		RobotInfo[] friends = rc.senseNearbyRobots(RobotType.GUARD.sensorRadiusSquared, myTeam);
		
		/*for(int i=0;i<friends.length;i++){
			if(friends[i].maxHealth > friends[i].health){
				changeStrat();
				closestEnemyLoc = friends[i].location;
				closestEnemyID = friends[i].ID;
				curStrat = strat.FIND_ENEMY;
				return;
			}
		}*/
		
		if(!moving){
			if(curLoc.distanceSquaredTo(archCenter)<RobotType.GUARD.sensorRadiusSquared || friends.length>8){
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
	public void guardObjective(){
		
	}
	public void guardCombat(){
		MapLocation curLoc = rc.getLocation();
		if(rc.getHealth()/RobotType.GUARD.maxHealth < 0.2 && rc.getHealth() < lastHealth){
			RobotInfo[] friends = rc.senseNearbyRobots(RobotType.GUARD.sensorRadiusSquared, myTeam);
			if(friends.length > 0 && friends.length < 8){
				changeStrat();
				
				curStrat = strat.KITE;
				steps = 0;
				return;
			}
		}
		lastHealth = rc.getHealth();
		RobotInfo[] enemies = rc.senseHostileRobots(curLoc, RobotType.GUARD.sensorRadiusSquared);
        if(enemies.length > 0) {
        	double[] enemyVal = new double[enemies.length];
        	for(int i=0;i<enemies.length;i++){
        		double percentHealth = 1.0 - enemies[i].health/enemies[i].maxHealth;
        		double distValue = 1.0;
        		if(curLoc.distanceSquaredTo(enemies[i].location) > RobotType.GUARD.attackRadiusSquared){
        			double dist = (double)(curLoc.distanceSquaredTo(enemies[i].location) - RobotType.GUARD.attackRadiusSquared);
        			distValue -= 0.2;
        			distValue -= dist/((double)RobotType.GUARD.attackRadiusSquared);
        		}
        		enemyVal[i] = percentHealth * distValue;
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
        	else{
        		//move toward target
        		marco.bugMove(rc, enemies[maxIndex].location);
        	}
        	
        }
        else{
        	//meander
        	changeStrat();
        	curStrat = strat.MEANDER;
        }
        
        
	}
	public void guardFindEnemy(){
		readSignals();
		MapLocation curLoc = rc.getLocation();
		RobotInfo[] enemies = rc.senseHostileRobots(curLoc, RobotType.GUARD.sensorRadiusSquared);
		if(enemies.length>0){
			//enemy found. must kill
			changeStrat();
			curStrat = strat.COMBAT;
			return;
		}
		
		//move toward enemy
		int dist = marco.bugMove(rc, closestEnemyLoc);
		if(dist<3){
			RobotInfo[] friends = rc.senseNearbyRobots(RobotType.GUARD.sensorRadiusSquared, myTeam);
			/*for(int i=0;i<friends.length;i++){
				if(friends[i].maxHealth > friends[i].health){
					if(friends[i].ID != closestEnemyID){
						changeStrat();
						closestEnemyLoc = friends[i].location;
						closestEnemyID = friends[i].ID;
						curStrat = strat.FIND_ENEMY;
						return;
					}
				}
			}*/
			//did not find any other friends that lost health. resume meandering
			changeStrat();
			curStrat = strat.MEANDER;
		}
	}
	public void guardKite(){
		RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), RobotType.ARCHON.sensorRadiusSquared);
		try {
			runawayMove(rc, enemies);
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		steps++;
		
		if(steps>=stepLimit){
			//no more enemies
			if(enemies.length==0){
				//RobotInfo[] friends = rc.senseNearbyRobots(RobotType.GUARD.sensorRadiusSquared, myTeam);
				
				/*for(int i=0;i<friends.length;i++){
					if(friends[i].maxHealth > friends[i].health){
						changeStrat();
						closestEnemyLoc = friends[i].location;
						closestEnemyID = friends[i].ID;
						curStrat = strat.FIND_ENEMY;
						return;
					}
				}*/
				curStrat = strat.MEANDER;
				changeStrat();
				return;
			}
			else{
				changeStrat();
				curStrat = strat.COMBAT;
			}
		}
	}
	
	public void readSignals(){
		Signal[] signals = rc.emptySignalQueue();
		MapLocation curLoc = null;
		if(signals.length>0){
			curLoc = rc.getLocation();
		}
		for(int i=0;i<signals.length;i++){
    		if(signals[i].getTeam() == myTeam){
        		int[] msg = signals[i].getMessage();
        		//if message is basic message
        		if(msg==null){
        			MapLocation ml = signals[i].getLocation();
        			if(curStrat==strat.MEANDER || curStrat == strat.FIND_ENEMY){
	        			if(curLoc.distanceSquaredTo(ml) < closestEnemyDist){
							closestEnemyDist = curLoc.distanceSquaredTo(ml);
							closestEnemyLoc = ml;
							curStrat = strat.FIND_ENEMY;
							//broadcast location to other meanderers
							if(curStrat == strat.MEANDER){
								try{
									rc.broadcastSignal(broadcastRange);
								}
								catch (GameActionException e) {
									e.printStackTrace();
								}
							}
						}
        			}
        			
        		}
        		//signal has message in it
        		else{
        			//check for type
        			int type = enigma.fastHashType(msg[0]);
        			
        			//found enemy
        			if(type == 0){
        				if(curStrat==strat.MEANDER || curStrat == strat.FIND_ENEMY){
        					int[] returnMsg = enigma.fastUnHash(msg);
        					MapLocation tml = new MapLocation(returnMsg[1], returnMsg[2]);
        					if(curLoc.distanceSquaredTo(tml) < closestEnemyDist){
        						closestEnemyDist = curLoc.distanceSquaredTo(tml);
        						closestEnemyLoc = tml;
        						curStrat = strat.FIND_ENEMY;
        					}
        				}
        			}
        		}
    		}
    	}
	}
	public void changeStrat(){
		moving = false;
		closestEnemyDist = 99999;
	}
	public void checkForHurtFriends(){
		
	}
}
