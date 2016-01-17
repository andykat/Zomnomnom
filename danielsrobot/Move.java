package danielsrobot;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Move {
	private int prevDistanceN = 3;
	private int[] prevDistance = new int[3];
	public Move(){
		for(int i=0;i<prevDistanceN;i++){
			prevDistance[i] = -1 - i;
		}
	}

	public int bugMove(RobotController rc, MapLocation end){
		try {
			MapLocation curLoc = rc.getLocation();
			int dist = curLoc.distanceSquaredTo(end);
			Direction dir = curLoc.directionTo(end);
			
			for(int i=0;i<prevDistanceN;i++){
				if(dist == prevDistanceN){
					if(rc.isCoreReady()){
						if(rc.senseRobotAtLocation(curLoc.add(dir)) == null){
							rc.clearRubble(dir);
							for(int j=prevDistanceN-1;j>0;j--){
								prevDistance[j] = prevDistance[j-1];
							}
						}
					}
					return dist;
				}
			}
			for(int j=prevDistanceN-1;j>0;j--){
				prevDistance[j] = prevDistance[j-1];
			}
			//Direction dir = start.directionTo(end);
			
			int c=0;
			while(!rc.canMove(dir))
			{
				dir = dir.rotateRight();
				if(c==3){
					dir = dir.rotateRight();
					c++;
				}
				if(c>6){
					break;
				}
				c++;
			}
			if(c<7){
				if(rc.isCoreReady()){
					rc.move(dir);
					return dist;
				}
			}
			else{
				if(rc.isCoreReady()){
					MapLocation nextLoc = curLoc.add(dir);
					if(rc.onTheMap(nextLoc)){
						if(rc.senseRobotAtLocation(nextLoc) == null){
							rc.clearRubble(dir);
							for(int j=prevDistanceN-1;j>0;j--){
								prevDistance[j] = prevDistance[j-1];
							}
						}
					}
				}
				return dist;
			}
		} catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
		return 99999;
	}
	public void endMove(){
		for(int i=0;i<prevDistanceN;i++){
			prevDistance[i] = -1 - i;
		}
	}
	public static Direction[] bestDir(Direction dir){
		Direction[] bestDir= {dir,dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
		return bestDir;
	}
}
