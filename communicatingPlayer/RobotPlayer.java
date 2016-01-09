package communicatingPlayer;

import battlecode.common.*;

public class RobotPlayer{
	static Direction movingDirection= Direction.EAST;
	static RobotController rc;
	static int messageDelay= 50; //Send out message every 10 turns?
	static int map[][]; //
	
	public static void run(RobotController rcin){
		rc= rcin;
		if (rc.getTeam()== Team.B)
			movingDirection= Direction.WEST;
		
		while(true){ //Will get destroyed if it ends
			try {
				repeat();
				Clock.yield(); //Ends the turn
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void sendMessage(){
		//Send out update depending on what you see
	}
	
	public static void repeat() throws GameActionException{
		
	}
}