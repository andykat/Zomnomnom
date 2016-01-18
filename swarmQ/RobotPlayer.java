package swarmQ;
import battlecode.common.*;


public class RobotPlayer  implements RobotConstants{
	static RobotController rc;
	private static RobotRunner rr;
	
	public static void run(RobotController rcin) throws GameActionException{
		rc= rcin;
		if (rc.getType().equals(RobotType.ARCHON)){
			rr= new Archon(rc);
		}else if (rc.getType().equals(RobotType.SCOUT)){
		}else if (rc.getType().equals(RobotType.GUARD)){
			rr= new Guard(rc);
		}else if (rc.getType().equals(RobotType.SOLDIER)){
			rr= new Soldier(rc);
		}else{
			rr= new RobotRunner(rc); //In case some one fucks up
		}
		
		while(true){ //Will get destroyed if it ends
			try {
				rr.run();
				Clock.yield(); //Ends the turn
			} catch (GameActionException e) {
				e.printStackTrace();
			}
			Clock.yield();
		}
	}
}
