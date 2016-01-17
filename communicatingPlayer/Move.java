package communicatingPlayer;

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
			rc.setIndicatorDot(end, 255, 192, 203);
			rc.setIndicatorLine(rc.getLocation(), end, 255, 192, 203);
			
			MapLocation curLoc = rc.getLocation();
			int dist = curLoc.distanceSquaredTo(end);
			Direction dir = curLoc.directionTo(end);
			
			for(int i=0;i<prevDistanceN;i++){
				if(dist == prevDistance[i]){
					if(rc.isCoreReady()){
						if(rc.senseRobotAtLocation(curLoc.add(dir)) == null){
							if(rc.senseRubble(curLoc.add(dir)) < 100.0){
								break;
							}
							else{
								rc.clearRubble(dir);
								for(int j=prevDistanceN-1;j>0;j--){
									prevDistance[j] = prevDistance[j-1];
								}
								prevDistance[0] = dist;
							}
						}
						else{
							dir.rotateRight();
							if(rc.senseRobotAtLocation(curLoc.add(dir)) == null){
								if(rc.senseRubble(curLoc.add(dir)) < 100.0){
									break;
								}
								else{
									rc.clearRubble(dir);
									for(int j=prevDistanceN-1;j>0;j--){
										prevDistance[j] = prevDistance[j-1];
									}
									prevDistance[0] = dist;
								}
							}
							else{
								//Blocked by neutral robot
								return 99999;
							}
						}
					}
					return dist;
				}
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
					for(int j=prevDistanceN-1;j>0;j--){
						prevDistance[j] = prevDistance[j-1];
					}
					prevDistance[0] = dist;
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
							prevDistance[0] = dist;
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
