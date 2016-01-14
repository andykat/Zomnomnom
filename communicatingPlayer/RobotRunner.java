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
						if (rc.senseRubble(rc.getLocation().add(dir)) > 0)
							rc.clearRubble(dir);
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
					MapLocation visitingSpot= new MapLocation(rc.getLocation().x+px, rc.getLocation().y+py);
					if (rc.canSenseLocation(visitingSpot) && rc.onTheMap(visitingSpot)){
						answer.add(visitingSpot);
						rc.setIndicatorDot(visitingSpot, 255, 0, 0);
					}
					else if (!rc.canSense(visitingSpot)){
						answer.add(visitingSpot); //For the scout himself to check
						rc.setIndicatorDot(visitingSpot, 255, 0, 0);
					}
					//System.out.println(n);
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
	
	public MapLocation checkLocation(MapLocation check) throws GameActionException{ //returns null on invalid map
		MapLocation answer= check;
		if (rc.canSense(check)){
			if (rc.onTheMap(check)== false){
				answer= null;
			}
		}
		return answer;
	}
	
	public void simpleMove(Direction dirToMove) throws GameActionException{
		if (rc!= null){
			if (rc.isCoreReady()){
				if (rc.canMove(dirToMove)){
					rc.move(dirToMove);
				}else{
					Direction[] nextMoves;
					if (randall.nextInt(2)== 0)
						nextMoves= nextBestRight(dirToMove);
					else
						nextMoves= nextBestLeft(dirToMove);
					for (int n= 0; n< nextMoves.length; n++){
						if (rc.canMove(nextMoves[n])){
							rc.move(nextMoves[n]);
							break;
						}
					}
				}
			}
		}else{
			System.out.println("rc is null");
		}
	}
	public static Direction[] nextBestRight(Direction bestDir){
		Direction[] runnerup= {bestDir.rotateLeft(), bestDir.rotateLeft(),bestDir.rotateRight().rotateRight(), bestDir.rotateLeft().rotateLeft()};
		return runnerup;
	}
	public static Direction[] nextBestLeft(Direction bestDir){
		Direction[] runnerup= {bestDir.rotateLeft(),bestDir.rotateLeft(), bestDir.rotateLeft().rotateLeft(), bestDir.rotateRight().rotateRight()};
		return runnerup;
	}
}
