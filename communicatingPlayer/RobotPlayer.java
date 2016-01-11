package communicatingPlayer;
import battlecode.common.*;

public class RobotPlayer{
	static Team currentTeam= Team.A;
	static RobotController rc;
	//static int messageDelay= 50; //Send out message every 10 turns?
	static Information notebook = new Information(); //Suggests where things are
	static MapLocation randomPlace= new MapLocation(435,159);
	
	public static void run(RobotController rcin){
		rc= rcin;
		if (rc.getTeam()== Team.B)
			currentTeam= Team.B;
		
		while(true){ //Will get destroyed if it ends
			try {
				repeat();
				Clock.yield(); //Ends the turn
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void repeat() throws GameActionException{
		if (rc.getType().equals(RobotType.ARCHON))
			runArchon();
		
		if (rc.getType().equals(RobotType.SCOUT))
			runScout();
	}
	
	public static void broadCastInformation(){
		//Send out the first 20 objects in the priority list, then clear the priority list?
	}
	
	public static void runArchon() throws GameActionException{
		//SENSING PART
		RobotInfo[] sensedRobots= rc.senseNearbyRobots();
		for (int n= 0; n< sensedRobots.length; n++){
			if (sensedRobots[n].team.equals(currentTeam)){//If friendly team
				
			}else if (sensedRobots[n].team.equals(Team.NEUTRAL)){//Neutral team
				
			}else{//Enemy team
				
			}
		}
		
		//MESSAGE PART
		Signal[] radio= rc.emptySignalQueue();
		for (int n= 0; n< radio.length; n++){
			if (radio[n].getTeam().equals(currentTeam)){
				int[] message= radio[n].getMessage();
				if (message!=null){//If there is something in the message
			
				}
			}
		}
	}
	
	public static void runScout() throws GameActionException{
		/*
			The robot senses nearby terrain and characters
			When the robot receives information (i.e. message or sensing, it adds it to a priority queue using the randomness weighed with the turn as comparator, the more recent favored)
			Robot decides how much info to send (bytecode limited, and also 20 message limit)
			Each robot sends out N messages per turn, choosing the top N in the priority queue to send
			All sensed information is added into the current robot's brain, information is added to priority queue using the least recent turn as comparator, that the more recent information can override them
			If priority queue length > 100, clear it
		*/

		if (rc.isCoreReady()){
			//Move towards the center to get more information
			Direction moveDir= rc.getLocation().directionTo(randomPlace);
			if (rc.canMove(moveDir)){
				rc.move(moveDir);
				System.out.println(rc.getLocation().toString()+ " "+ rc.getRoundNum());
			}
			if (rc.getLocation().equals(randomPlace)){
				randomPlace= new MapLocation(436,170);
			}
		}
	}
	
}