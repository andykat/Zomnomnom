package communicatingPlayer;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class RobotRunner {
	protected RobotController rc;
	protected Random randall;
	public RobotRunner(RobotController rcin){
		this.rc= rcin;
		randall= new Random();
	}
	
	public void run() throws GameActionException{
		//System.out.println("Override this~~");
	}
	public static Direction[] nextBestRight(Direction bestDir){
		Direction[] runnerup= {bestDir.rotateLeft(), bestDir.rotateLeft(),bestDir.rotateRight().rotateRight(), bestDir.rotateLeft().rotateLeft()};
		return runnerup;
	}
	public static Direction[] nextBestLeft(Direction bestDir){
		Direction[] runnerup= {bestDir.rotateLeft(),bestDir.rotateLeft(), bestDir.rotateLeft().rotateLeft(), bestDir.rotateRight().rotateRight()};
		return runnerup;
	}
	
	public void avoidEnemyMove(){
		//TODO move and alternate course based on enemy repulsion
	}
	
	public ArrayList<MapLocation> createDividedRadius(int searchRange, int radius) throws GameActionException{
		ArrayList<MapLocation> answer= new ArrayList<MapLocation>();
		double angle= 360/(int)(180/Math.toDegrees(Math.asin(searchRange*1.0/(2*radius))));
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
				System.out.println(n);
			}
		}
		return answer;
	}
	
	public ArrayList<MapLocation> cleanLocationList(ArrayList<MapLocation> visitingList){
		return null;
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
}
