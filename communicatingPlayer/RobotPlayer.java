package communicatingPlayer;

import battlecode.common.*;

public class RobotPlayer implements RobotConstants{
	static RobotController rc;
	private static RobotRunner rr;
	
	public static void run(RobotController rcin){
		rc= rcin;
		if (rc.getType().equals(RobotType.ARCHON)){
			rr= new Archon(rc);
		}
		if (rc.getType()== RobotType.SCOUT){
			rr= new Scout(rc);
		}
		
		while(true){ //Will get destroyed if it ends
			try {
				rr.run();
				Clock.yield(); //Ends the turn
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}
	}
}