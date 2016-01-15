package communicatingPlayer;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
}
