package communicatingPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

	public RobotRunner(RobotController rcin){
		this.rc= rcin;
		randall= new Random();
		myTeam= rc.getTeam();
		enemyTeam= myTeam.opponent();
		eden= rc.getInitialArchonLocations(myTeam)[0];
		memory= new Information(); //Everybody has a brain
		robotSenseRadius= (int) Math.sqrt(rc.getType().sensorRadiusSquared);
	}
	
	public void run() throws GameActionException{
		//System.out.println("Override this~~");
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
	
	public void avoidEnemyMove(){
		//TODO move and alternate course based on enemy repulsion
	}
	
	protected ArrayList<MapLocation> createDividedSquareNodes(int searchRange) throws GameActionException{
		ArrayList<MapLocation> answer= new ArrayList<MapLocation>();

		if (searchRange<= 0){
			answer.add(rc.getLocation()); //zero range is yourself
		}else{
			int additive= 2* ((int) (robotSenseRadius * Math.sqrt(2)) -1);
			MapLocation prevPt = new MapLocation(Integer.MIN_VALUE, Integer.MIN_VALUE);
			for (int x = -searchRange; x<= searchRange; x+= additive){
				for (int y= -searchRange; y<= searchRange; y+= additive){
					if (x== -searchRange || y== -searchRange || x+additive>= searchRange || y+additive >= searchRange){
						MapLocation test= temperLocation(rc.getLocation().add(x,y));
						if (test.distanceSquaredTo(prevPt) >= rc.getType().sensorRadiusSquared && !answer.contains(rc.getLocation().add(x,y))){ //Only add points if it is further than half away?
							answer.add(rc.getLocation().add(x,y));
							prevPt= temperLocation(rc.getLocation().add(x,y));
						}
					}
				}
			}
		}
		
		Collections.sort(answer, new Comparator<MapLocation>(){
			public int compare(MapLocation a, MapLocation b) {
				int aVal= 0;
				int bVal= 0;
				for (int n= 0; n< RobotConstants.directions.length; n++){
					if (RobotConstants.directions[n].equals(rc.getLocation().directionTo(a))){
						aVal= n;
					}
					if (RobotConstants.directions[n].equals(rc.getLocation().directionTo(b))){
						bVal= n;
					}
				}
				if (aVal > bVal){
					return 1;
				}else if (aVal< bVal){
					return -1;
				}else{
					return 0;
				}
			}
			
		});
		
		for (int n= 0; n< answer.size(); n++){ //This way the direction comparator works properly
			answer.set(n, temperLocation(answer.get(n)));
		}
		
		//Sort the answers
		
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
	
//	protected void temperMapLocList(ArrayList<MapLocation> mapLocList){ //
//		int[] corners= memory.getCorners();
//		temperMacLocList(mapLocList, corners[0],corners[1],corners[2],corners[3]); //Clamps the map locations down to limit
//		System.out.println("Memory clamped");
//	}
	
	protected MapLocation temperLocation(MapLocation loc){
		int[] corners= memory.getCorners();
//		int minX= corners[0] + (int) (robotSenseRadius/Math.sqrt(2));
//		int minY= corners[1] + (int) (robotSenseRadius/Math.sqrt(2));
//		int maxX= corners[2] - (int) (robotSenseRadius/Math.sqrt(2));
//		int maxY= corners[3] - (int) (robotSenseRadius/Math.sqrt(2));
		
		//System.out.println(Arrays.toString(corners));
		
		int tolerance= 2;
		
		int x= Utility.clamp(loc.x,corners[0]+robotSenseRadius-tolerance,corners[2]-robotSenseRadius+tolerance);
		int y= Utility.clamp(loc.y,corners[1]+robotSenseRadius-tolerance,corners[3]-robotSenseRadius+tolerance);
		
		return new MapLocation(x,y);
	}
	
	private void temperMacLocList(ArrayList<MapLocation> visitingList, int minX, int minY, int maxX, int maxY){ //make sure to temper to bound- radius!
		//Check if the edge points have been detected-- if it has, trim all out of bound locations, if not, clamp them in the same fashion

		int corruption= 0;
		MapLocation corruptedPlace= new MapLocation(Integer.MIN_VALUE, Integer.MIN_VALUE);
		
		ArrayList<MapLocation> usedCorners= new ArrayList<MapLocation>(); //Each corner can be used once
		for (int n= 0; n< visitingList.size(); n++){
			MapLocation check= visitingList.get(n);
			minX= minX + robotSenseRadius/(int)Math.sqrt(2);
			minY= minY + robotSenseRadius/(int)Math.sqrt(2);
			maxX= maxX - robotSenseRadius/(int)Math.sqrt(2);
			maxY= maxY - robotSenseRadius/(int)Math.sqrt(2);
			
			int x= Utility.clamp(check.x,minX,maxX);
			int y= Utility.clamp(check.y,minY,maxY);
			
			MapLocation clampedValue= new MapLocation(x,y); //Some way to trim such that the distance between sensing points don't overlap too much
			visitingList.set(n, clampedValue);
			
			//clamping
			
			MapLocation[] corners= {new MapLocation(minX,minY), new MapLocation(minX,maxY), new MapLocation(maxX, minY), new MapLocation(maxX,maxY)};
			for (int m= 0; m < corners.length; m++){
				if (clampedValue.equals(corners[m])){
					if (!usedCorners.contains(corners[m])){
						usedCorners.add(clampedValue); //If it has yet to be used, use it
					}
					break;
				}
			}
			
			if (n== 0){
				visitingList.set(n, clampedValue);
			}else if (n> 0 && visitingList.get(n-1).distanceSquaredTo(clampedValue) > rc.getType().sensorRadiusSquared/2 && !usedCorners.contains(clampedValue)){ //If it is a not yet visiting corner
				visitingList.set(n, clampedValue);
			}else{
				visitingList.set(n, corruptedPlace);
				corruption++;
				//System.out.println("corruption count: "+ corruption);
			}
		}
		for (int n= 0; n< corruption; n++){
			visitingList.remove(corruptedPlace);
		}
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
	
	protected void gatherMapInfo() throws GameActionException{
		rc.setIndicatorString(0, "Sensing");
		RobotInfo[] enemyRobotsInRange = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
		for (int n= 0; n< enemyRobotsInRange.length; n++){
			if (enemyRobotsInRange[n].type==RobotType.ZOMBIEDEN){
				memory.addMapInfo(enemyRobotsInRange[n].location, (int) enemyRobotsInRange[n].health, RobotConstants.mapTypes.ZOMBIE_DEN);	
			}
		}
		
		RobotInfo[] neutralRobotsInRange = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.NEUTRAL);
		for (int n= 0; n< neutralRobotsInRange.length; n++){
			memory.addMapInfo(neutralRobotsInRange[n].location, neutralRobotsInRange[n]);
		}
		
		MapLocation[] partsLoc= rc.sensePartLocations(rc.getType().sensorRadiusSquared);
		for (int n= 0; n< partsLoc.length; n++){
			memory.addMapInfo(partsLoc[n], (int)rc.senseParts(partsLoc[n]),RobotConstants.mapTypes.PARTS);
		}
		 
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
