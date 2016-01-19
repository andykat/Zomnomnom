package team302;
import battlecode.common.*;


public class RobotPlayer  implements RobotConstants{
	static RobotController rc;
	private static RobotRunner rr;
	
	public static void run(RobotController rcin) throws GameActionException{
		rc= rcin;
		if (rc.getType().equals(RobotType.ARCHON)){
			rr= new Archon(rc);
		}else if (rc.getType().equals(RobotType.SCOUT)){
			rr = new Scout(rc);
		}else if (rc.getType().equals(RobotType.GUARD)){
			rr= new Guard(rc);
		}else if (rc.getType().equals(RobotType.SOLDIER)){
			rr= new Soldier(rc);
		}
		else if(rc.getType().equals(RobotType.VIPER)){
			rr = new Viper(rc);
		}
		else if(rc.getType().equals(RobotType.TURRET)){
			rr = new Turret(rc);
		}
		else if(rc.getType().equals(RobotType.TTM)){
			
		}
		else{
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
