package swarmQ;
import java.util.ArrayList;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;



public class RobotRunner {
	protected RobotController rc;
	protected RobotInfo[] startingArchons;
	protected Team enemyTeam;
	protected Team myTeam;
	protected Random randall;
	protected MapLocation eden;
	protected Information memory;
	protected int robotSenseRadius;
	protected Move marco;
	protected MessageHash enigma;

	public RobotRunner(RobotController rcin){
		this.rc= rcin;
		randall= new Random(rc.getID());
		myTeam= rc.getTeam();
		enemyTeam= myTeam.opponent();
		eden= rc.getInitialArchonLocations(myTeam)[0];
		memory= new Information(); //Everybody has a brain
		robotSenseRadius= (int) Math.sqrt(rc.getType().sensorRadiusSquared);
		marco= new Move();
		enigma = new MessageHash();
	}
	
	public void run() throws GameActionException{
		//Can write here a general case so all non-specified class can do something
	}
	
	protected void sendInstructions(){
		
	}
	
	protected void followInstructions(){
		
	}

	
	protected boolean simpleAttack() throws GameActionException{
		boolean attacked= false;
	     if (rc.getType().canAttack()) {
             RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, enemyTeam);
             RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, Team.ZOMBIE);
             if (enemiesWithinRange.length > 0) {
                 if (rc.isWeaponReady()) {
                     rc.attackLocation(enemiesWithinRange[randall.nextInt(enemiesWithinRange.length)].location);
                     attacked= true;
                 }
             } else if (zombiesWithinRange.length > 0) {
                 if (rc.isWeaponReady()) {
                     rc.attackLocation(zombiesWithinRange[randall.nextInt(zombiesWithinRange.length)].location);
                     attacked= true;
                 }
             }
         }
	     
	     return attacked;
	}
	
	protected Direction getSpawnableDir(RobotType rt){
		Direction[] alldirs= Direction.values();
		Direction answer= null;
		Utility.shuffleDirArray(alldirs);
		for (int n= 0; n< alldirs.length; n++){
			if (rc.canBuild(alldirs[n], rt)){
				answer= alldirs[n];
				break;
			}
		}
		return answer;
	}
	
	protected void buildRobot(RobotType rt) throws GameActionException{
		Direction canBuildDir= getSpawnableDir(rt);
		if (canBuildDir!= null){
			rc.build(canBuildDir, rt);
		}
	}
	
	protected MapLocation temperLocation(MapLocation loc){
		int[] corners= memory.getCorners();		
		int tolerance= 2;
		
		int x= Utility.clamp(loc.x,corners[0]+robotSenseRadius-tolerance,corners[2]-robotSenseRadius+tolerance);
		int y= Utility.clamp(loc.y,corners[1]+robotSenseRadius-tolerance,corners[3]-robotSenseRadius+tolerance);
		
		return new MapLocation(x,y);
	}
	
	protected void senseMapEdges() throws GameActionException{ //THE CROSS IS ALWAYS MORE POWERFUL, diagonals quite useless
		//When the checked point turns from off the map to on the map, that is the value
		//rc.setIndicatorString(0, "sense map edge");
		boolean prevOffMap= false;
		
		for (int x= -robotSenseRadius; x< 0; x++){ //Check to yourself
			MapLocation check= rc.getLocation().add(x, 0);
			if (rc.canSense(check)){
				if (!rc.onTheMap(check)){
					prevOffMap= true;
				}else if (prevOffMap && rc.onTheMap(check)){
					Direction scan= rc.getLocation().directionTo(check);
					memory.addCornerValueCandidate(scan, check); //You add the direction of your check and the corresponding map location when the map first comes on grid
					//rc.setIndicatorDot(check, 0, 255, 0);
					//rc.setIndicatorLine(rc.getLocation(), check, 0, 255, 0);
					break ;
				}
			}
		}
		
		prevOffMap= false;
		for (int x= robotSenseRadius; x> 0; x--){
			MapLocation check= rc.getLocation().add(x,0);
			//rc.setIndicatorString(1, "CHECK: "+ check.toString());
			if (rc.canSense(check)){
				if (!rc.onTheMap(check)){
					prevOffMap= true;
				}else if (prevOffMap && rc.onTheMap(check)){
					//rc.setIndicatorDot(check, 0, 255, 0);
					//rc.setIndicatorLine(rc.getLocation(), check, 0, 255, 0);
					Direction scan= rc.getLocation().directionTo(check);
					memory.addCornerValueCandidate(scan, check);
					break;
				}
			}
		}
		
		prevOffMap= false;
		for (int y= -robotSenseRadius; y< 0; y++){
			MapLocation check= rc.getLocation().add(0,y);
			if (rc.canSense(check)){
				if (!rc.onTheMap(check)){ //If the checked position is not on the map
					prevOffMap= true;
				}else if (prevOffMap && rc.onTheMap(check)){ //if the previously checked position is not on the map, and the current on is
					//rc.setIndicatorDot(check, 0, 255, 0);
					//rc.setIndicatorLine(rc.getLocation(), check, 0, 255, 0);
					Direction scan= rc.getLocation().directionTo(check);
					memory.addCornerValueCandidate(scan, check);
					break;
				}
			}
		}
		
		prevOffMap= false;
		for (int y= robotSenseRadius; y> 0; y--){
			MapLocation check= rc.getLocation().add(0,y);
			if (rc.canSense(check)){
				if (!rc.onTheMap(check)){
					prevOffMap= true;
				}else if (prevOffMap && rc.onTheMap(check)){
					//rc.setIndicatorDot(check, 0, 255, 0);
					//rc.setIndicatorLine(rc.getLocation(), check, 0, 255, 0);
					Direction scan= rc.getLocation().directionTo(check);
					memory.addCornerValueCandidate(scan, check);
					break;
				}
			}
		}
	}
	
	protected void gatherMapInfo(MapLocation targetObjLoc) throws GameActionException{ //This is for a single place, used for rescue updates
		if (rc.canSense(targetObjLoc)){
			RobotInfo targetRobot= rc.senseRobotAtLocation(targetObjLoc);
			if (targetRobot!= null){
				if (targetRobot.team.equals(Team.NEUTRAL)){//IS a neutral robot
					memory.addNeutralRobotMapInfo(targetObjLoc, targetRobot);
				}else if (targetRobot.team.equals(Team.ZOMBIE)){
					memory.addMapInfo(targetObjLoc, (int)targetRobot.health, RobotConstants.mapTypes.ZOMBIE_DEN);
				}
			}
			memory.addMapInfo(targetObjLoc, (int) rc.senseRubble(targetObjLoc), RobotConstants.mapTypes.RUBBLE);
			memory.addMapInfo(targetObjLoc, (int) rc.senseParts(targetObjLoc), RobotConstants.mapTypes.PARTS);
		}
	}
	
	protected void gatherMapInfo() throws GameActionException{
		//rc.setIndicatorString(0, "Sensing");
		RobotInfo[] enemyRobotsInRange = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
		for (int n= 0; n< enemyRobotsInRange.length; n++){
			if (enemyRobotsInRange[n].type==RobotType.ZOMBIEDEN){
				memory.addMapInfo(enemyRobotsInRange[n].location, (int) enemyRobotsInRange[n].health, RobotConstants.mapTypes.ZOMBIE_DEN);	
			}
		}
		
		RobotInfo[] neutralRobotsInRange = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.NEUTRAL);
		for (int n= 0; n< neutralRobotsInRange.length; n++){
			memory.addNeutralRobotMapInfo(neutralRobotsInRange[n].location, neutralRobotsInRange[n]);
		}
		
		MapLocation[] partsLoc= rc.sensePartLocations(rc.getType().sensorRadiusSquared);
		for (int n= 0; n< partsLoc.length; n++){
			memory.addMapInfo(partsLoc[n], (int)rc.senseParts(partsLoc[n]),RobotConstants.mapTypes.PARTS);
		}
		 
		for (int x= -robotSenseRadius/(int)Math.sqrt(2)+rc.getLocation().x; x< robotSenseRadius/(int)Math.sqrt(2)+rc.getLocation().x; x++){
			for (int y= -robotSenseRadius/(int)Math.sqrt(2)+rc.getLocation().y; y< robotSenseRadius/(int)Math.sqrt(2)+rc.getLocation().y; y++){
				MapLocation underView= new MapLocation(x,y);
				if (rc.canSense(underView) && rc.onTheMap(underView)){
					int rubbles= (int) rc.senseRubble(underView);
					if (rubbles> 0){
						memory.addMapInfo(underView, rubbles,RobotConstants.mapTypes.RUBBLE);
					}
				}
			 }
		 }
	}
	public static Direction[] bestDir(Direction dir){
		Direction[] bestDir= {dir,dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
		return bestDir;
	}
	public static Direction[] spawnDir(Direction dir){
		Direction[] bestDir= {dir,dir.rotateLeft(), dir.rotateRight(), dir, dir.rotateLeft(), dir.rotateRight(), dir, dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
		return bestDir;
	}
	
	public void runawayMove(RobotController rc, RobotInfo[] enemies) throws GameActionException{
        MapLocation myLoc = rc.getLocation();
        //boolean canMove = false;
        int[] zVec = awayFromZombies(rc, enemies); 
        int[] eVec = awayFromEnemyTeam(rc, enemies);
        
        int dx = zVec[0] + eVec[0];
        int dy = zVec[1] + eVec[1];
        MapLocation targetLoc = myLoc.add(dx, dy);
        //Direction dir = myLoc.directionTo(targetLoc);

        marco.bugMove(rc, targetLoc);
        
	}

	public int[] getMaxSensibleXY(RobotController rc) {
	    int r = rc.getType().sensorRadiusSquared;
	    int x=0, y=0, max = 0;
	    
	    for(int i=0; i < Math.sqrt(r); i++) {
	        for(int j=0; j < Math.sqrt(r); j++) {
	            int a = i*i + j*j;
	            if((a <= r) && a > max) {
	                x = i;
	                y = j;
	                max = a;
	            }
	        }
	    }
	    int[] xy = {x, y};
	    return xy;
	}
	
	// 1 = right rotate
	// 0 = u fucked
	// -1 = left rotate
	public int optimalRotation(RobotController rc, Direction dir, RobotInfo[] enemies) {
	    MapLocation curLoc = rc.getLocation();
	    MapLocation[] sensibleLoc = null;
	    int[] center = getMaxSensibleXY(rc);
	    int halfRadiusSquared = (int) Math.pow(Math.floor((Math.sqrt(rc.getType().sensorRadiusSquared)/2)), 2);
	    // MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(), rc.getType().sensorRadiusSquared);
	    if(dir == Direction.NORTH_EAST) {
	        sensibleLoc = MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(center[0], -center[1]), halfRadiusSquared);
	        if(sensibleLoc.length < 4) {
	            return -1;
	        }
	    }
	    else if(dir == Direction.NORTH_WEST) {
	        sensibleLoc = MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(-center[0], -center[1]), halfRadiusSquared);
	        if(sensibleLoc.length < 4) {
	            return 1;
	        }
	    }
	    else if(dir == Direction.SOUTH_WEST) {
	        sensibleLoc = MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(-center[0], center[1]), halfRadiusSquared);
	        if(sensibleLoc.length < 4) {
	            return -1;
	        }
	    }
	    else if(dir == Direction.SOUTH_EAST) {
	        sensibleLoc = MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(center[0], center[1]), halfRadiusSquared);
	        if(sensibleLoc.length < 4) {
	            return 1;
	        }
	    }
	    else if(dir == Direction.NORTH) {
	        sensibleLoc = MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(0, -center[1]), halfRadiusSquared);
	        if(sensibleLoc.length < 4) {
	            return 1;
	        }
	    }
	    else if(dir == Direction.EAST) {
	        sensibleLoc = MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(center[0], 0), halfRadiusSquared);
	        if(sensibleLoc.length < 4) {
	            return 1;
	        }
	    }
	    else if(dir == Direction.SOUTH) {
	        sensibleLoc = MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(0, center[1]), halfRadiusSquared);
	        if(sensibleLoc.length < 4) {
	            return 1;
	        }
	    }
	    else if(dir == Direction.WEST) {
	        sensibleLoc = MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(-center[0], 0), halfRadiusSquared);
	        if(sensibleLoc.length < 4) {
	            return 1;
	        }
	    }
	    else {
	        return 1;
	    }
	    return 1;
	}
	
	public int[] awayFromZombies(RobotController rc, RobotInfo[] enemies) throws GameActionException {
	    int dx = 0, dy = 0;
	    int multiplier = 1; // repulsion force multiplier
	    MapLocation myLoc = rc.getLocation();
	
	    for(RobotInfo r : enemies) {
	        MapLocation enemyLoc = r.location;
	        if(r.type == RobotType.BIGZOMBIE) {
	            multiplier = 3;
	        }
	        else if(r.type == RobotType.RANGEDZOMBIE) {
	            multiplier = 2;
	        }
	        else {
	            multiplier = 1;
	        }
	        dx += multiplier * (myLoc.x - enemyLoc.x);
	        dy += multiplier * (myLoc.y - enemyLoc.y);
	    } 
	    int[] vector = {dx, dy};
	    //System.out.println("zombies dx: " + dx + " dy: " + dy);
	    return vector;
	}
	
	public int[] awayFromEnemyTeam(RobotController rc, RobotInfo[] enemies) {
	    int dx = 0, dy = 0;
	    int multiplier = 1; // repulsion force multiplier
	    MapLocation myLoc = rc.getLocation();
	    Team enemyTeam = rc.getTeam().opponent();
	
	    for(RobotInfo r : enemies) {
	        if(r.team == enemyTeam) {
	            MapLocation enemyLoc = r.location;
	            if(willDieInTurns(rc, enemies, 10)) {
	                multiplier = -10;  // hella move towards them
	            } 
	            else {
	                multiplier = 3;     // get the f away from them
	            }
	            dx += multiplier * (myLoc.x - enemyLoc.x);
	            dy += multiplier * (myLoc.y - enemyLoc.y);
	        }
	    }
	    //System.out.println("enemies dx: " + dx + " enemies dy: " + dy);
	    int[] vector = {dx, dy};
	    return vector;
	
	}
	
	// Very approximate. Not accurate at all. Heuristic af. But whatever.
	public boolean willDieInTurns(RobotController rc, RobotInfo[] enemies, int turns) {
	    double totaldmg = 0.0;
	    double weapondelay = 0.5;
	
	    for(RobotInfo r : enemies) {
	        totaldmg += (weapondelay * turns * r.attackPower);
	    }
	    
	    if(rc.getHealth() < totaldmg) {
	        return true;
	    }
	
	    return false;
	}
	
	public int max(int a, int b){
		if(a>b) return a;
		return b;
	}
	public int min(int a, int b){
		if(a>b) return b;
		return a;
	}
}