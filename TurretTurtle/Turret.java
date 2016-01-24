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

public class Turret extends RobotRunner{
	private enum strat{MEANDER,OBJECTIVE,COMBAT,KITE,FIND_ENEMY};
	public static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	private strat curStrat;
	private RobotInfo[] enemies;
	private MapLocation dest;
	private MapLocation curLoc;
	private MapLocation nearArchonLoc;
	private Map<RobotType, Double> tasty = new EnumMap<RobotType, Double>(RobotType.class);
	public Turret(RobotController rcin){
		super(rcin);
		curStrat = strat.COMBAT;
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
			if(curStrat == strat.COMBAT){
				TurretCombat();
			}
			else if(curStrat == strat.MEANDER){
				TurretMeander();
			}
		}
	}
	public void TurretMeander() throws GameActionException{
		int dist = marco.bugMove(rc, dest);
		if(dist<1 || dist==99999){
			rc.unpack();
			curStrat = strat.COMBAT;
		}
	}
	public void TurretCombat() throws GameActionException{
		/*boolean attacked = readSignals();
		if(attacked){
			return;
		}*/
		enemies = rc.senseHostileRobots(rc.getLocation(), RobotType.TURRET.sensorRadiusSquared);
		if(enemies.length>0){
			turretCombat();
			return;
		}
		readSignals();
		
	}

	public void turretCombat(){
		MapLocation curLoc = rc.getLocation();

		
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
	
	
	
	public boolean readSignals() throws GameActionException{
		Signal[] signals = rc.emptySignalQueue();
		for(int i=signals.length-1;i>-1;i--){
			if(signals[i].getTeam() == myTeam){
				int[] msg = signals[i].getMessage();
				if(msg==null){
					
				}
				else{
					int type = enigma.fastHashType(msg[0]);
					if(type==0){
						if(rc.senseNearbyRobots(2).length>3){
							curStrat = strat.MEANDER;
							curLoc = rc.getLocation();
							dest = curLoc.add(nearArchonLoc.directionTo(curLoc), 1); 
							rc.pack();							
							return true;
						}
					}
					else if(type == 1){
        				
    					int[] returnMsg = enigma.fastUnHash(msg);
    					MapLocation tml = new MapLocation(returnMsg[1], returnMsg[2]);
    					
    						if (rc.canAttackLocation(tml) && rc.isWeaponReady()) {
    							rc.attackLocation(tml);
    							return true;
    						}
    					
        			}
				}
			}
		}
		return false;
	}
	public void checkForHurtFriends(){
		
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
}
