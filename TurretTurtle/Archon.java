package TurretTurtle;

import java.util.ArrayList;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;



public class Archon extends RobotRunner{
	private enum strat{SPAWN,TRAVELTOBASE,OBJECTIVE,KITE,RETURNTOBASE,MEANDER};
	private strat curStrat;
	private boolean isMainArchon = false;
	private MapLocation nearArchonLoc;

	private MapLocation curLoc;
	private int rounds = 0;
	private boolean moving = true;
	private MapLocation dest;
	private MapLocation lastScanNode = new MapLocation(5000,5000);
	private int nodeThresh = rc.getType().sensorRadiusSquared/2;
	private FastInfo intel;
	private int locSignalRange = RobotType.ARCHON.sensorRadiusSquared*4;
	private int buildCount = 0;
	private int broadcastRange = RobotType.ARCHON.sensorRadiusSquared*3;
	private MapLocation objLoc;
	private int objType;
	private boolean touchedBase = false;
	private int turnsTravelToBase = 0;
	private int meanderRadius = 4;
	private Direction meanderDirection;
	private MapLocation lastMeanderLoc;
	private MapLocation meanderDest;
	
	public Archon(RobotController rcin) {
		super(rcin);
		
		curLoc = rc.getLocation();
		
		//get grouping point for the swarm and find the direction to it
		MapLocation[] archLocs = rc.getInitialArchonLocations(myTeam);
		if(archLocs.length==1){
			isMainArchon = true;
		}
		int centerX=0;
		int centerY=0;
		for(int i=0;i<archLocs.length;i++){
			centerX += archLocs[i].x;
			centerY += archLocs[i].y;
		}
		MapLocation center = new MapLocation(centerX /= archLocs.length, centerY /= archLocs.length);
		int min = 99999;
		for(int i=0;i<archLocs.length;i++){
			if(center.distanceSquaredTo(archLocs[i]) < min){
				min = center.distanceSquaredTo(archLocs[i]);
				nearArchonLoc = archLocs[i];
			}
		}
		
		meanderDirection = RobotConstants.directions[randall.nextInt(8)];
		
		intel = new FastInfo(nodeThresh);
		
		if(nearArchonLoc.equals(curLoc)){
			isMainArchon = true;
			curStrat = strat.SPAWN;
		}
		else{
			moving = true;
			curStrat = strat.TRAVELTOBASE;
			dest = nearArchonLoc;
			scanNextNode();
			/*if(curStrat == strat.TRAVELTOBASE){
				System.out.println("r2b");
			}
			else if(curStrat == strat.OBJECTIVE){
				System.out.println("obj");
			}*/
		}
	}
	
	public void run() throws GameActionException{
		if (rc.isCoreReady()){
			RobotInfo[] enemies = rc.senseHostileRobots(curLoc, RobotType.ARCHON.sensorRadiusSquared/2);
			
			//if there are enemies, change to kite state
			if(enemies.length > 0){
				if(!isMainArchon){
					runawayMove(rc, enemies);
					return;
				}
				else{
					runawayMove(rc, enemies);
					return;
				}
	        }
			if(curStrat == strat.SPAWN){
				ArchonSpawn();
			}
			else if(curStrat == strat.TRAVELTOBASE){
				ArchonTravelToBase();
			}
			else if(curStrat == strat.RETURNTOBASE){
				if(enemies.length == 0){
					if(rc.getTeamParts()>200){
						curStrat = strat.TRAVELTOBASE;
					}
					else{
						setupMeander();
						curStrat = strat.MEANDER;
					}
				}
				ArchonReturnToBase();
			}
			else if(curStrat == strat.MEANDER){
				ArchonMeander();
			}
			else if(curStrat == strat.OBJECTIVE){
				ArchonObjective();
			}
		}
	}
	public void ArchonSpawn() throws GameActionException{
		if(!isMainArchon){
			if(rc.getTeamParts()<150){
				setupMeander();
				curStrat = strat.MEANDER;
				return;
			}
		}
		RobotType buildType = RobotType.TURRET;
		if(buildCount % 8 == 4){
			buildType = RobotType.SCOUT;
		}
		if(rc.hasBuildRequirements(buildType)){
			boolean builtFlag = false;
			for(int i=0;i<8;i++){
				if(rc.canBuild(RobotConstants.directions[i], buildType)){
					rc.build(RobotConstants.directions[i], buildType);
					buildCount++;
					builtFlag = true;
					break;
				}
			}
			if(builtFlag){
				return;
			}
			else{
				//wasn't able to build
				//message turrets to move?
				sendMoveSignal();
			}
		}
	}
	
	public void ArchonTravelToBase() throws GameActionException{
		
		
			turnsTravelToBase++;
			int dist = marco.bugMove(rc,dest);
			if(dist<5){
				moving = false;
				touchedBase = true;
				if(rc.getTeamParts()>200){
					curStrat = strat.SPAWN;
				}
				else{
					setupMeander();
					curStrat = strat.MEANDER;
				}
			}
			curLoc = rc.getLocation();
			if(dist==9999){
				if(curLoc.distanceSquaredTo(dest) < 17){
					curStrat = strat.SPAWN;
					touchedBase = true;
					if(rc.getTeamParts()>200){
						curStrat = strat.SPAWN;
					}
					else{
						setupMeander();
						curStrat = strat.MEANDER;
					}
				}
			}
			
			if(curLoc.distanceSquaredTo(lastScanNode) > nodeThresh){
				boolean canScan = intel.checkNode(curLoc);
				if(canScan){
					scanNextNode();
				}
			}
			if(turnsTravelToBase > 200){
				setupMeander();
				curStrat = strat.MEANDER;
				touchedBase = true;
			}
			
		
	}
	
	public void ArchonObjective() throws GameActionException{
		curLoc = rc.getLocation();
		if(objType==1){
			//robot
			if(curLoc.distanceSquaredTo(objLoc) < 3){
				RobotInfo nRobot = rc.senseRobotAtLocation(objLoc);
				if(nRobot != null && nRobot.team == Team.NEUTRAL){
					rc.activate(objLoc);
					intel.objectives.remove(0);
					intel.objTypes.remove(0);
					if(intel.objectives.size()>0){
						objLoc = intel.objectives.get(0);
						objType = intel.objTypes.get(0);
						curStrat = strat.OBJECTIVE;
						moving = false;
						turnsTravelToBase = 0;
					}
					else{
						if(touchedBase){
							setupMeander();
							meanderRadius--;
							curStrat = strat.MEANDER;
						}
						else{
							dest = nearArchonLoc;
							curStrat = strat.TRAVELTOBASE;
						}
						if(curLoc.distanceSquaredTo(lastScanNode) > nodeThresh){
							boolean canScan = intel.checkNode(curLoc);
							if(canScan){
								scanNextNode();
							}
						}
					}
				}
			}
		}
			int dist = marco.bugMove(rc,objLoc);
			curLoc = rc.getLocation();
			if(curLoc.distanceSquaredTo(lastScanNode) > nodeThresh){
				boolean canScan = intel.checkNode(curLoc);
				if(canScan){
					intel.scanNode(rc);
					lastScanNode = rc.getLocation();
				}
			}
			if(objType==2){
				//parts
				if(dist<1 || dist==99999){
					intel.objectives.remove(0);
					intel.objTypes.remove(0);
					if(intel.objectives.size()>0){
						objLoc = intel.objectives.get(0);
						objType = intel.objTypes.get(0);
						curStrat = strat.OBJECTIVE;
						moving = false;
						turnsTravelToBase = 0;
					}
					else{
						if(touchedBase){
							setupMeander();
							meanderRadius--;
							curStrat = strat.MEANDER;
						}
						else{
							dest = nearArchonLoc;
							moving = true;
							curStrat = strat.TRAVELTOBASE;
						}
						if(curLoc.distanceSquaredTo(lastScanNode) > nodeThresh){
							boolean canScan = intel.checkNode(curLoc);
							if(canScan){
								scanNextNode();
							}
						}
					}
				}
			}
		
	}
	public void ArchonMeander(){
		rounds++;
		if(intel.objectives.size()>0){
			objLoc = intel.objectives.get(0);
			objType = intel.objTypes.get(0);
			curStrat = strat.OBJECTIVE;
			turnsTravelToBase = 0;
			return;
		}
		int[] ans = marco.avoidRubbleMove(rc, meanderDest);
		int dist = ans[0];
		curLoc = rc.getLocation();
		if(curLoc.distanceSquaredTo(lastScanNode) > nodeThresh){
			boolean canScan = intel.checkNode(curLoc);
			if(canScan){
				scanNextNode();
				return;
			}
		}
		if(ans[1]>0){
			System.out.println("rub:" + ans[1]);
		}
		if(dist<2 || ans[1] > 499 || rounds>35){
			setupMeander();
		}
		else if(dist==99999){
			setupMeander();
		}
	}
	
	public void ArchonReturnToBase() throws GameActionException{
		int dist = marco.bugMove(rc,nearArchonLoc);
	}
	public void ArchonCombat() throws GameActionException{
		
	}
	public void ArchonKite() throws GameActionException{
		
	}
	public void sendMoveSignal(){
		int[] msg = enigma.fastHash(0, curLoc.x, curLoc.y, 0, 0);
		try {
			rc.broadcastMessageSignal(msg[0], msg[1], broadcastRange);
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}
	public void changeStrat(){
		moving = false;
	}
	public void readSignals(){
		rc.emptySignalQueue();
	}
	public void scanNextNode(){
		intel.scanNode(rc);
		lastScanNode = rc.getLocation();
		if(intel.objectives.size()>0){
			objLoc = intel.objectives.get(0);
			objType = intel.objTypes.get(0);
			curStrat = strat.OBJECTIVE;
			//moving = false;
			turnsTravelToBase = 0;
		}
		else{
			if(touchedBase){
				setupMeander();
				curStrat = strat.MEANDER;
			}
			else{
				dest = nearArchonLoc;
				moving = true;
				curStrat = strat.TRAVELTOBASE;
			}
		}
	}
	public void setupMeander(){
		lastMeanderLoc = rc.getLocation();
		meanderDest = lastMeanderLoc.add(meanderDirection, meanderRadius);
		meanderDirection = meanderDirection.rotateLeft();
		meanderRadius+=1;
		rounds = 0;
		
	}
}
