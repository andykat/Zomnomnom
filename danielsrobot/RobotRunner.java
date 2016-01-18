package danielsrobot;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import battlecode.common.*;

public class RobotRunner {
	protected RobotController rc;
	protected RobotInfo[] startingArchons;
	protected Team enemyTeam;
	protected Team myTeam;
	protected Random randall;
	protected MapLocation eden;
	protected Information memory;
	protected int robotSenseRadius;
    protected Move move;	

    public RobotRunner(RobotController rcin){
		this.rc= rcin;
		randall= new Random();
		if (rc.getTeam().equals(Team.A)){
			enemyTeam= Team.B;
			myTeam= Team.A;
		}else{
			enemyTeam= Team.A;
			myTeam= Team.B;
		}
		eden= rc.getInitialArchonLocations(myTeam)[0];
		memory= new Information(); //Everybody has a brain
        move = new Move();
		robotSenseRadius= (int) Math.sqrt(rc.getType().sensorRadiusSquared);
	}
	

	public void run() throws GameActionException{
		//System.out.println("Override this~~");
	}
	

	public void bugMove(MapLocation start, MapLocation end){
		try {
			Direction dir = start.directionTo(end);
			int c=0;
			while(!rc.canMove(dir))
			{
				dir = dir.rotateRight();
				if(c>7){
					break;
				}
				c++;
			}
			if(c<8){
				if(rc.isCoreReady()){
					rc.move(dir);
				}
			}
			else{
				if(rc.isCoreReady()){
					if (rc.canSense(rc.getLocation().add(dir)) && rc.onTheMap(rc.getLocation().add(dir))){
					}
				}
			}
		} catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
	}

	public boolean simpleAttack() throws GameActionException{
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
	

	public void temperingVisitingList(ArrayList<MapLocation> visitingList, int minX, int minY, int maxX, int maxY){ //make sure to temper to bound- radius!
		ArrayList<MapLocation> toDelete= new ArrayList<MapLocation>();
		//boolean skip= false; //Just dlete every other
		rc.setIndicatorString(1, "tempering");
		
		int corruption= 0;
		MapLocation corruptedPlace= new MapLocation(Integer.MIN_VALUE, Integer.MIN_VALUE);
		
		for (int n= 0; n< visitingList.size(); n++){
			MapLocation check= visitingList.get(n);
//			minX= minX + robotSenseRadius-2;
//			minY= minY + robotSenseRadius-2;
//			maxX= maxX - robotSenseRadius+2;
//			maxY= maxY - robotSenseRadius+2;
			
			int x= clamp(check.x,minX,maxX);
			int y= clamp(check.y,minY,maxY);
			
			MapLocation clampedValue= new MapLocation(x,y); //Some way to trim such that the distance between sensing points don't overlap too much
			visitingList.set(n, clampedValue);
			
			//clamping
			boolean atCorner= false; //Annoying as fk case
			MapLocation[] corners= {new MapLocation(minX,minY), new MapLocation(minX,maxY), new MapLocation(maxX, minY), new MapLocation(maxX,maxY)};
			for (int m= 0; m < corners.length; m++){
				if (clampedValue.equals(corners)){
					atCorner= true;
					break;
				}
			}
			
			if (n> 0 && visitingList.get(n-1).distanceSquaredTo(clampedValue) > rc.getType().sensorRadiusSquared/2 && !atCorner) //Humble thoughts
				visitingList.set(n, clampedValue);
			else{
				visitingList.set(n, corruptedPlace);
				corruption++;
			}
			
//			if (check.x < minX || check.x > maxX || check.y < minY || check.x > maxY){ //Humble thoughts
//				visitingList.set(n, corruptedPlace);
//				corruption++;
//			}else{
//				visitingList.set(n, clampedValue);
//			}
		}
		
		for (int n= 0; n< corruption; n++){
			visitingList.remove(corruptedPlace);
		}
		
		rc.setIndicatorString(1,"");
	}
	

	public static int clamp(int val, int min, int max) {
	    return Math.max(min, Math.min(max, val));
	}
	

	public Direction getSpawnableDir(RobotType rt){
		Direction[] alldirs= Direction.values();
		Direction answer= null;
		shuffleDirArray(alldirs);
		for (int n= 0; n< alldirs.length; n++){
			if (rc.canBuild(alldirs[n], rt)){
				answer= alldirs[n];
				break;
			}
		}
		return answer;
	}
	
	public void shuffleDirArray(Direction[] ar)
	  {
	    for (int i = ar.length - 1; i > 0; i--)
	    {
	      int index = randall.nextInt(i + 1);
	      // Simple swap
	      Direction a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	  }
	

	public void avoidEnemyMove(){
		//TODO move and alternate course based on enemy repulsion
	}
	

	public ArrayList<MapLocation> createDividedRadius(int searchRange, int radius) throws GameActionException{
		ArrayList<MapLocation> answer= new ArrayList<MapLocation>();
		int divisionNum= (int)(180/Math.toDegrees(Math.asin(searchRange*1.0/(2*radius))));
		if (divisionNum!= 0){
			double angle= 360/divisionNum;
			//System.out.println("ANGLE: "+Math.toDegrees(angle));
			if (angle!= 0){
				for (int n= 0; n< 360; n+= angle){
					int px= (int) (radius*Math.cos(Math.toRadians(n)));
					int py= (int) (radius*Math.sin(Math.toRadians(n)));
					answer.add(new MapLocation(rc.getLocation().x+px, rc.getLocation().y+py));
				}
			}
		}
		return answer;
	}


	public void buildRobot(RobotType rt) throws GameActionException{
		Direction canBuildDir= getSpawnableDir(rt);
		if (canBuildDir!= null){
			rc.build(canBuildDir, rt);
		}
	}

    public void runawayFrom(RobotInfo[] enemies) throws GameActionException{
            MapLocation myLoc = rc.getLocation();
            boolean canMove = false;
            int[] zVec = awayFromZombies(enemies); 
            int[] etVec = awayFromEnemyTeam(enemies);
            int[] eVec = new int[2];
            eVec[0] = zVec[0] + etVec[0];
            eVec[1] = zVec[1] + etVec[1];
            int[] cVec = awayFromCornersAndEnemies(eVec);

            int dx = eVec[0] + cVec[0];
            int dy = eVec[1] + cVec[1];
            System.out.println("zombie dx: " + zVec[0] + " dy: " + zVec[1]);
            System.out.println("enemy dx: " + eVec[0] + " dy: " + eVec[1]);
            System.out.println("corner dx: " + cVec[0] + " dy: " + cVec[1]);
            System.out.println("final dx: " + dx + " dy: " + dy);
            System.out.println();
            MapLocation targetLoc = myLoc.add(dx, dy);

            move.bugMove(rc, targetLoc);
    }

    public int[] awayFromCornersAndEnemies(int[] eVec) {
        MapLocation curLoc = rc.getLocation();
        MapLocation[] sensibleLocs = MapLocation.getAllMapLocationsWithinRadiusSq(curLoc, rc.getType().sensorRadiusSquared);
        int dx = 0;
        int dy = 0;
        int count = 0;
        for(MapLocation loc : sensibleLocs) {
            try{
                if(!rc.onTheMap(loc)) {
                    if(curLoc.x == loc.x || curLoc.y == loc.y) {
                        int wallX = curLoc.x - loc.x;
                        int wallY = curLoc.y - loc.y;
                            dx += wallX; 
                            dy += wallY;
                    }
                }
            } 
            catch(GameActionException e) {

            }
        }
        System.out.println("out of bounds = " + count);
        int[] vector = {dx, dy};
        return vector;
    }

    public int[] getMaxSensibleXY() {
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
    public int optimalRotation(Direction dir, RobotInfo[] enemies) {
        MapLocation curLoc = rc.getLocation();
        MapLocation[] sensibleLoc = null;
        int[] center = getMaxSensibleXY();
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

        ArrayList<Rubble> rubbles = new ArrayList<Rubble>();
        for(MapLocation loc : sensibleLoc) {
            if(!rc.canSense(loc)) {
                int dx = loc.x - curLoc.x;
                int dy = loc.y - curLoc.y;

                if(!rc.canSense(curLoc.add(dx, dy))) {
                    if(!rc.canSense(curLoc.add(dx, 0))) {
                        if((dx > 0 && dy > 0) || (dx < 0 && dy < 0)) {
                            return 1;
                        }
                        else {
                            return -1;
                        }
                    }
                    else if(!rc.canSense(curLoc.add(0, dy))){
                        if((dx > 0 && dy > 0) || (dx < 0 && dy < 0)) {
                            return -1;
                        }
                        else {
                            return 1;
                        }
                    }
                } // end of avoiding map edges

                else {
                    double amount = rc.senseRubble(loc);
                    if(amount > 0) {
                        rubbles.add(new Rubble(loc, amount));
                    }
                }
            }
        }

        int obstacleX = 0;
        int obstacleY = 0;

        for(Rubble rubble : rubbles) {
            int xLoc = rubble.getLocation().x;
            int yLoc = rubble.getLocation().y;
            if(rubble.getAmount() < 100) {
            }
        }

        return 1;
    }

    public int[] awayFromZombies(RobotInfo[] enemies) throws GameActionException {
        int dx = 0, dy = 0;
        int multiplier = 1; // repulsion force multiplier
        MapLocation myLoc = rc.getLocation();

        for(RobotInfo r : enemies) {
            MapLocation enemyLoc = r.location;
            if(r.type == RobotType.BIGZOMBIE) {
                multiplier = 1;
            }
            else {
                multiplier = 1;
            }
            dx += multiplier * (myLoc.x - enemyLoc.x);
            dy += multiplier * (myLoc.y - enemyLoc.y);
        } 
        int[] vector = {dx, dy};
        return vector;

        /*
        Direction away = myLoc.directionTo(runVector);
        if(away == Direction.NORTH) {
            if(dx < 0) {
                away = Direction.NORTH_WEST;
            } else {
                away = Direction.NORTH_EAST;
            }
        }
        else if(away == Direction.SOUTH) {
            if(dx < 0) {
                away = Direction.SOUTH_WEST;
            }
            else {
                away = Direction.SOUTH_EAST;
            }
        }
        else if(away == Direction.EAST) {
            if(dy < 0) {
                away = Direction.NORTH_EAST;
            }
            else {
                away = Direction.SOUTH_EAST;
            }
        }
        else if(away == Direction.WEST) {
            if(dy < 0) {
                away = Direction.NORTH_WEST;
            }
            else {
                away = Direction.SOUTH_WEST;
            }
        }
        return runVector;
        */
    }

    public int[] awayFromEnemyTeam(RobotInfo[] enemies) {
        int dx = 0, dy = 0;
        int multiplier = 1; // repulsion force multiplier
        MapLocation myLoc = rc.getLocation();
        Team enemyTeam = rc.getTeam().opponent();

        for(RobotInfo r : enemies) {
            if(r.team == enemyTeam) {
                MapLocation enemyLoc = r.location;
                if(willDieInTurns(enemies, 10)) {
                    multiplier = -1;  // hella move towards them
                } 
                else {
                    multiplier = 1;     // get the f away from them
                }
                dx += multiplier * (myLoc.x - enemyLoc.x);
                dy += multiplier * (myLoc.y - enemyLoc.y);
            }
        }
        int[] vector = {dx, dy};
        return vector;

    }

    public boolean willDieInTurns(RobotInfo[] enemies, int turns) {
        double totaldmg = 0.0;

        for(RobotInfo r : enemies) {
            totaldmg += (turns / r.weaponDelay * r.attackPower);
        }
        
        if(rc.getHealth() < totaldmg) {
            return true;
        }

        return false;
    }

    public RobotInfo getBestTarget(RobotType type, RobotInfo[] enemies) {
        RobotInfo target = null;
        MapLocation myLoc = rc.getLocation();

        if(type == RobotType.SOLDIER) {
            double maxBounty = 0;
            for(RobotInfo e : enemies) {
                double missingHP = 1 - (e.health / e.maxHealth);
                int dx = e.location.x - myLoc.x; 
                int dy = e.location.y - myLoc.y;
                int dSq = dx*dx + dy*dy;
                
                // bounty = (tasty) * (% missing health of enemy) * ((1-(distance of enemy / sightrange))^constant)
                double bounty = 1 * missingHP * ((1 - dSq/type.sensorRadiusSquared)^1);
                if(bounty > maxBounty) {
                    maxBounty = bounty;
                    target = e;
                }
            }
        }

        return target;
    }

}
