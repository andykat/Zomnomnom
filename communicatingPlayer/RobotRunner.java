package communicatingPlayer;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class RobotRunner {
	protected RobotController rc;
	protected RobotInfo[] startingArchons;
	protected Team enemyTeam;
	protected Team myTeam;
	protected Random randall;
	protected MapLocation eden;
	protected Information memory;
	protected int robotSenseRadius;
	protected Move marco;
	private RobotInfo targetRobot;

	public RobotRunner(RobotController rcin){
		this.rc= rcin;
		randall= new Random();
		myTeam= rc.getTeam();
		enemyTeam= myTeam.opponent();
		eden= rc.getInitialArchonLocations(myTeam)[0];
		memory= new Information(); //Everybody has a brain
		robotSenseRadius= (int) Math.round(Math.sqrt(rc.getType().sensorRadiusSquared)); //Account for rounding differences
		marco= new Move();
	}
	
	public void run() throws GameActionException{
		//Can write here a general case so all non-specified class can do something
	}
	
	protected void sendInstructions(){
		
	}
	
	protected void followInstructions(){
		
	}
	
	protected void bugMove(MapLocation start, MapLocation end){
		try {
			Direction dir = start.directionTo(end);
			int c=0;
			while(!rc.canMove(dir))
			{
				dir = dir.rotateRight();
				if(c>7){
					break;
				}
				c++;
			}
			if(c<8){
				if(rc.isCoreReady()){
					rc.move(dir);
				}
			}
			else{
				if(rc.isCoreReady()){
					if (rc.canSense(rc.getLocation().add(dir)) && rc.onTheMap(rc.getLocation().add(dir))){
					}
				}
			}
		} catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
	}
	
	protected boolean simpleAttack() throws GameActionException{
		boolean attacked= false;
	     if (rc.getType().canAttack()) {
             RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, enemyTeam);
             RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, Team.ZOMBIE);
             if (enemiesWithinRange.length > 0) {
                 if (rc.isWeaponReady()) {
                     rc.attackLocation(enemiesWithinRange[randall.nextInt(enemiesWithinRange.length)].location);
                     attacked= true;
                 }
             } else if (zombiesWithinRange.length > 0) {
                 if (rc.isWeaponReady()) {
                     rc.attackLocation(zombiesWithinRange[randall.nextInt(zombiesWithinRange.length)].location);
                     attacked= true;
                 }
             }
         }
	     
	     return attacked;
	}
	
	protected Direction getSpawnableDir(RobotType rt){
		Direction[] alldirs= Direction.values();
		Direction answer= null;
		Utility.shuffleDirArray(alldirs);
		for (int n= 0; n< alldirs.length; n++){
			if (rc.canBuild(alldirs[n], rt)){
				answer= alldirs[n];
				break;
			}
		}
		return answer;
	}
	
	protected ArrayList<MapLocation> createDividedSquarePerimNodes(int searchRange) throws GameActionException{
		ArrayList<MapLocation> answer= new ArrayList<MapLocation>();

		if (searchRange<= 0){
			answer.add(rc.getLocation()); //zero range is yourself
		}else{
			int additive= 2* ((int) (robotSenseRadius * Math.sqrt(2)) -1)-1;
			MapLocation prevPt = new MapLocation(Integer.MIN_VALUE, Integer.MIN_VALUE);
			for (int x = -searchRange; x<= searchRange; x+= additive){
				for (int y= -searchRange; y<= searchRange; y+= additive){
					if (x== -searchRange || y== -searchRange || x+additive>= searchRange || y+additive >= searchRange){
						MapLocation test= temperLocation(rc.getLocation().add(x,y));
						if (test.distanceSquaredTo(prevPt) > rc.getType().sensorRadiusSquared/2){ //Only add points if it is further than half away? //test.distanceSquaredTo(prevPt) > rc.getType().sensorRadiusSquared/2 && 
							answer.add(rc.getLocation().add(x,y));
						}
					}
				}
			}
		}		
		for (int n= 0; n< answer.size(); n++){ //This way the direction comparator works properly
			answer.set(n, temperLocation(answer.get(n)));
		}
		
		
		Utility.removeMapLocDuplicate(answer); //Remove duplicates
		return answer;
	}
	
	
	protected ArrayList<MapLocation> createDividedRadius(int searchRange, int radius) throws GameActionException{
		ArrayList<MapLocation> answer= new ArrayList<MapLocation>();
		int divisionNum= (int)(180/Math.toDegrees(Math.asin(searchRange*1.0/(2*radius))));
		if (divisionNum!= 0){
			double angle= 360/divisionNum;
			//System.out.println("ANGLE: "+Math.toDegrees(angle));
			if (angle!= 0){
				for (int n= 0; n< 360; n+= angle){
					int px= (int) (radius*Math.cos(Math.toRadians(n)));
					int py= (int) (radius*Math.sin(Math.toRadians(n)));
					answer.add(new MapLocation(rc.getLocation().x+px, rc.getLocation().y+py));
				}
			}
		}
		return answer;
	}
	
	protected void buildRobot(RobotType rt) throws GameActionException{
		Direction canBuildDir= getSpawnableDir(rt);
		if (canBuildDir!= null){
			rc.build(canBuildDir, rt);
		}
	}
	
	protected MapLocation temperLocation(MapLocation loc){
		int[] corners= memory.getCorners();		
		int tolerance= 2;
		
		int x= Utility.clamp(loc.x,corners[0]+robotSenseRadius-tolerance,corners[2]-robotSenseRadius+tolerance);
		int y= Utility.clamp(loc.y,corners[1]+robotSenseRadius-tolerance,corners[3]-robotSenseRadius+tolerance);
		
		return new MapLocation(x,y);
	}
	
	protected void senseMapEdges() throws GameActionException{ //THE CROSS IS ALWAYS MORE POWERFUL, diagonals quite useless
		//When the checked point turns from off the map to on the map, that is the value
		rc.setIndicatorString(0, "sense map edge");
		boolean prevOffMap= false;
		
		for (int x= -robotSenseRadius; x< 0; x++){ //Check to yourself
			MapLocation check= rc.getLocation().add(x, 0);
			if (rc.canSense(check)){
				if (!rc.onTheMap(check)){
					prevOffMap= true;
				}else if (prevOffMap && rc.onTheMap(check)){
					Direction scan= rc.getLocation().directionTo(check);
					memory.addCornerValueCandidate(scan, check); //You add the direction of your check and the corresponding map location when the map first comes on grid
					rc.setIndicatorDot(check, 0, 255, 0);
					rc.setIndicatorLine(rc.getLocation(), check, 0, 255, 0);
					break ;
				}
			}
		}
		
		prevOffMap= false;
		for (int x= robotSenseRadius; x> 0; x--){
			MapLocation check= rc.getLocation().add(x,0);
			rc.setIndicatorString(1, "CHECK: "+ check.toString());
			if (rc.canSense(check)){
				if (!rc.onTheMap(check)){
					prevOffMap= true;
				}else if (prevOffMap && rc.onTheMap(check)){
					rc.setIndicatorDot(check, 0, 255, 0);
					rc.setIndicatorLine(rc.getLocation(), check, 0, 255, 0);
					Direction scan= rc.getLocation().directionTo(check);
					memory.addCornerValueCandidate(scan, check);
					break;
				}
			}
		}
		
		prevOffMap= false;
		for (int y= -robotSenseRadius; y< 0; y++){
			MapLocation check= rc.getLocation().add(0,y);
			if (rc.canSense(check)){
				if (!rc.onTheMap(check)){ //If the checked position is not on the map
					prevOffMap= true;
				}else if (prevOffMap && rc.onTheMap(check)){ //if the previously checked position is not on the map, and the current on is
					rc.setIndicatorDot(check, 0, 255, 0);
					rc.setIndicatorLine(rc.getLocation(), check, 0, 255, 0);
					Direction scan= rc.getLocation().directionTo(check);
					memory.addCornerValueCandidate(scan, check);
					break;
				}
			}
		}
		
		prevOffMap= false;
		for (int y= robotSenseRadius; y> 0; y--){
			MapLocation check= rc.getLocation().add(0,y);
			if (rc.canSense(check)){
				if (!rc.onTheMap(check)){
					prevOffMap= true;
				}else if (prevOffMap && rc.onTheMap(check)){
					rc.setIndicatorDot(check, 0, 255, 0);
					rc.setIndicatorLine(rc.getLocation(), check, 0, 255, 0);
					Direction scan= rc.getLocation().directionTo(check);
					memory.addCornerValueCandidate(scan, check);
					break;
				}
			}
		}
	}
	
	protected void gatherPartInfo(){
		MapLocation[] partsLoc= rc.sensePartLocations(rc.getType().sensorRadiusSquared);
		for (int n= 0; n< partsLoc.length; n++){
			memory.addMapInfo(partsLoc[n], (int)rc.senseParts(partsLoc[n]),RobotConstants.mapTypes.PARTS);
		}
	}
	
	protected void gatherMapInfo(MapLocation targetObjLoc) throws GameActionException{ //This is for a single place, used for rescue updates
		if (rc.canSense(targetObjLoc)){
			targetRobot = rc.senseRobotAtLocation(targetObjLoc);
			if (targetRobot!= null){
				if (targetRobot.team.equals(Team.NEUTRAL)){//IS a neutral robot
					memory.addNeutralRobotMapInfo(targetObjLoc, targetRobot);
				}else if (targetRobot.team.equals(Team.ZOMBIE)){
					memory.addMapInfo(targetObjLoc, (int)targetRobot.health, RobotConstants.mapTypes.ZOMBIE_DEN);
				}else if (targetRobot.team.equals(myTeam) || targetRobot.team.equals(enemyTeam)){
					memory.addNeutralRobotMapInfo(targetObjLoc, null); //If converted or stolen, either way
				}
			}else{
				memory.addNeutralRobotMapInfo(targetObjLoc, null);//For when there are no longer robots there
			}
			memory.addMapInfo(targetObjLoc, (int) rc.senseRubble(targetObjLoc), RobotConstants.mapTypes.RUBBLE);
			memory.addMapInfo(targetObjLoc, (int) rc.senseParts(targetObjLoc), RobotConstants.mapTypes.PARTS);
		}
	}
	
	protected void gatherMapInfo() throws GameActionException{ //Only checks what is there; if it no longer exists it will be found out by the more stringent search up top
		rc.setIndicatorString(0, "Sensing");
		RobotInfo[] enemyRobotsInRange = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
		for (int n= 0; n< enemyRobotsInRange.length; n++){
			if (enemyRobotsInRange[n].type==RobotType.ZOMBIEDEN){
				memory.addMapInfo(enemyRobotsInRange[n].location, (int) enemyRobotsInRange[n].health, RobotConstants.mapTypes.ZOMBIE_DEN);	
			}
		}
		
		RobotInfo[] neutralRobotsInRange = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.NEUTRAL);
		for (int n= 0; n< neutralRobotsInRange.length; n++){
			memory.addNeutralRobotMapInfo(neutralRobotsInRange[n].location, neutralRobotsInRange[n]);
		}
		
		MapLocation[] partsLoc= rc.sensePartLocations(rc.getType().sensorRadiusSquared);
		for (int n= 0; n< partsLoc.length; n++){
			memory.addMapInfo(partsLoc[n], (int)rc.senseParts(partsLoc[n]),RobotConstants.mapTypes.PARTS);
		}
		 
		//If you are stopping to search, sometimes it is better to get as much as you can
		for (int x= -robotSenseRadius/(int)Math.sqrt(2)+rc.getLocation().x; x< robotSenseRadius/(int)Math.sqrt(2)+rc.getLocation().x; x++){
			for (int y= -robotSenseRadius/(int)Math.sqrt(2)+rc.getLocation().y; y< robotSenseRadius/(int)Math.sqrt(2)+rc.getLocation().y; y++){
				MapLocation underView= new MapLocation(x,y);
				if (rc.canSense(underView) && rc.onTheMap(underView)){
					int rubbles= (int) rc.senseRubble(underView);
					if (rubbles> 0){
						memory.addMapInfo(underView, rubbles,RobotConstants.mapTypes.RUBBLE);
					}
				}
			 }
		 }
		
	}
}
