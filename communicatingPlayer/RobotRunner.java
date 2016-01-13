package communicatingPlayer;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
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
