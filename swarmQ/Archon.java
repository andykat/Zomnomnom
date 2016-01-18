package swarmQ;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;


public class Archon extends RobotRunner{
	private enum strat{MEANDER,OBJECTIVE,COMBAT,KITE};
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
		
		System.out.println(rc.getID());
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
            curStrat = strat.KITE;
            changeStrat();
            return;
        }
		
		RobotInfo[] friends = rc.senseNearbyRobots(RobotType.ARCHON.sensorRadiusSquared, myTeam);
		
		//spawn soldier in best location
		if(rc.hasBuildRequirements(RobotType.SOLDIER)){
			if(rc.canBuild(spawnDir[friends.length%9], RobotType.SOLDIER)){
				rc.build(spawnDir[friends.length%9], RobotType.SOLDIER);
				return;
			}
			else{
				for(int i=0;i<bestDir.length;i++){
					if(rc.canBuild(bestDir[i], RobotType.SOLDIER)){
						rc.build(bestDir[i], RobotType.SOLDIER);
						rc.broadcastSignal(locSignalRange);
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
			rc.broadcastSignal(locSignalRange);
		}
		
		//if cannot spawn or find objectives, move with the group
		if(!moving){
			int vX=0;
			int vY=0;
			
			int friendCheckN = min(friends.length, 8);
			for(int i=0;i<friendCheckN;i++){
				vX += friends[i].location.x;
				vY += friends[i].location.y;
			}
			vX /= friendCheckN;
			vY /= friendCheckN;
			
			marco.swarmMoveStart();
			dest = new MapLocation(vX, vY);
			marco.swarmMove(rc, dest);
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
}
