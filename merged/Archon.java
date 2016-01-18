package merged;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;

public class Archon extends RobotRunner{
	private enum strat{MEANDER,OBJECTIVE,COMBAT,KITE,CREATE_EYES};
	private strat curStrat;
	private Direction toArchonCenter;
	private Direction[] spawnDir;
	private Direction[] bestDir;
	private MapLocation curLoc;
	private int stepsN = 6;
	private int rounds = 0;
	private boolean moving;
	private MapLocation dest;
	private int locSignalRange = RobotType.ARCHON.sensorRadiusSquared*4;
	private int buildCount = 0;
	private int broadcastRange = RobotType.ARCHON.sensorRadiusSquared*4;
	//private RobotType spawnOrder = {RobotType.SOLDIER, RobotType.SOLDIER, RobotType.GUARD};
	
    private int leaderID;
    private boolean spawned = false;

	public Archon(RobotController rcin) {
		super(rcin);
		
		curStrat = strat.MEANDER;
		
		//get grouping point for the swarm and find the direction to it
		MapLocation[] archLocs = rc.getInitialArchonLocations(myTeam);
		int centerX=0;
		int centerY=0;
		for(int i=0;i<archLocs.length;i++){
			centerX += archLocs[i].x;
			centerY += archLocs[i].y;
		}
		toArchonCenter = rc.getLocation().directionTo(new MapLocation(centerX/archLocs.length, centerY/archLocs.length));
		spawnDir = spawnDir(toArchonCenter);
		bestDir = bestDir(toArchonCenter);
		
		moving = false;

        memory = new Information();
        leaderID = Integer.MAX_VALUE;
	}
	public void run() throws GameActionException{
		if (rc.isCoreReady()){
			rounds++;
			readSignals();
			if(curStrat == strat.MEANDER){
				ArchonMeander();
			}
			else if(curStrat == strat.OBJECTIVE){
				
			}
			else if(curStrat == strat.COMBAT){
				
			}
			else if(curStrat == strat.KITE){
				ArchonKite();
			}
		}
	}
	public void ArchonMeander() throws GameActionException{
		curLoc = rc.getLocation();
		RobotInfo[] enemies = rc.senseHostileRobots(curLoc, RobotType.ARCHON.sensorRadiusSquared);
		
		//if there are enemies, change to kite state
		if(enemies.length > 0) {
			//alert distress!
			int [] message = enigma.fastHash(0, enemies[0].location.x, enemies[0].location.y, 0, 0);
			try{
				rc.broadcastMessageSignal(message[0], message[1], broadcastRange);
				//rc.broadcastSignal(broadcastRange);
			}
			catch (GameActionException e) {
				e.printStackTrace();
			}
            curStrat = strat.KITE;
            changeStrat();
            return;
        }
		
		RobotInfo[] friends = rc.senseNearbyRobots(RobotType.ARCHON.sensorRadiusSquared, myTeam);
		
        ///////////////////////////////////
        //*    Scouting for map info    *//
        ///////////////////////////////////
        if(!spawned) {
            if(rc.isCoreReady()) {
                signaling();
                buildRobot(RobotType.SCOUT);
                spawned = true;
            }
        }

		//spawn soldier in best location
		if(rc.hasBuildRequirements(RobotType.SOLDIER)){
			RobotType typeToBuild = RobotType.SOLDIER;
			if(buildCount%3<2){
				typeToBuild = RobotType.SOLDIER;
			}
			else{
				typeToBuild = RobotType.GUARD;
			}
			if(buildCount%15 == 4){
				typeToBuild = RobotType.SCOUT;
			}
			if(rc.canBuild(spawnDir[friends.length%9], typeToBuild)){
				rc.build(spawnDir[friends.length%9], typeToBuild);
				buildCount++;
				return;
			}
			else{
				for(int i=0;i<bestDir.length;i++){
					if(rc.canBuild(bestDir[i], typeToBuild)){
						rc.build(bestDir[i], typeToBuild);
						//rc.broadcastSignal(locSignalRange);
						buildCount++;
						return;
					}
				}
			}
		}
		
		//if cannot spawn, then look for objectives. If objective found, change to objective state
		//make sure to only look for objectives every 6 moves so that you don't
		//look at the same locations multiple times
		/////////////////////////////////////////////
		//
		// HI ALAN
		// I LEFT THIS FOR U
		//
		/////////////////////////////////////////////
		
		
		if(rounds%8==0){
			//rc.broadcastSignal(locSignalRange);
		}
		
		//if cannot spawn or find objectives, move with the group
		if(!moving){
			int friendCheckN = min(friends.length, 8);
			//check if there are units to follow
			if(friendCheckN>0){
				int vX=0;
				int vY=0;
				for(int i=0;i<friendCheckN;i++){
					vX += friends[i].location.x;
					vY += friends[i].location.y;
				}
				
				vX /= friendCheckN;
				vY /= friendCheckN;
				
				marco.swarmMoveStart();
				dest = new MapLocation(vX, vY);
				marco.swarmMove(rc, dest);
				moving = true;
			}
			else{
				//search stuff by itself.
			}
		}
		else{
			int[] moveReturn = marco.swarmMove(rc, dest);
			
			//moved enough, reached destination, or cant do anything, then
			//find new location to move to.
			if(moveReturn[0] == 99999 || moveReturn[0] < 2){
				moving = false;
			}
			if(moveReturn[1] >= stepsN){
				moving = false;
			}
		}
		
	}
	public void ArchonObjective() throws GameActionException{
			
	}
	public void ArchonCombat() throws GameActionException{
		
	}
	public void ArchonKite() throws GameActionException{
		RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), RobotType.ARCHON.sensorRadiusSquared);
		//no more enemies
		if(enemies.length==0){
			curStrat = strat.MEANDER;
			changeStrat();
			return;
		}
		runawayMove(rc, enemies);
	}
	public void changeStrat(){
		moving = false;
	}
	public void readSignals(){
		rc.emptySignalQueue();
	}

    ////////////////////////////////
    //* ALAN'S METHODS DOWN HERE *//
    ////////////////////////////////
    public void signaling() throws GameActionException{
        if (rc.getRoundNum()== 0){ //Election on first round hurray!
            Signal[] incomingMessages= rc.emptySignalQueue();
            rc.setIndicatorString(0, ""+incomingMessages.length+" messages received");
            rc.broadcastMessageSignal(0, 0, 100);
            leaderID= incomingMessages.length;
        }else{
            if (leaderID== 0){
                sendInstructions();
            }else{
                followInstructions();
            }
        }
    }

    public void followInstructions(){
		Signal[] incomingMessages= rc.emptySignalQueue();
		for (int n= 0; n< incomingMessages.length; n++){
			if (incomingMessages[n].getTeam().equals(myTeam)){
				//Do something about it
			}
		}
		
	}
}
