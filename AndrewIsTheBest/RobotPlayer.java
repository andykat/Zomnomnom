package AndrewIsTheBest;

import java.util.Random;

import battlecode.common.*;

public class RobotPlayer {
	@SuppressWarnings("unused")
    public static void run(RobotController rc) {
        // You can instantiate variables here.
        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
        RobotType[] robotTypes = {RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
                RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET};
        boolean map[][] = new boolean [100][100];
        
        Random rand = new Random(rc.getID());
        int myAttackRange = 0;
        Team myTeam = rc.getTeam();
        Team enemyTeam = myTeam.opponent();
        int robotCount=0;
        int buildingBreak = 0;
        int buildingBreakConstant = 100;
        
        MapLocation archStart = new MapLocation(0,0);
        MapLocation archEnd = new MapLocation(0,0);
        
        int radius = 5;
        if (rc.getType() == RobotType.ARCHON) {
            try {
                // Any code here gets executed exactly once at the beginning of the game.
            	int archonCount = rc.getRobotCount();
            	if(archonCount>3){
            		buildingBreakConstant = 200;
            	}
            	archStart = rc.getLocation();
            	
            	archEnd = randomCircleLoc(archStart.x,archStart.y, radius, rand);
            	
            	robotCount = 0;
            } catch (Exception e) {
                // Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
                // Caught exceptions will result in a bytecode penalty.
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            while (true) {
                // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
                // at the end of it, the loop will iterate once per game round.
                try {
                	boolean flag = false;
                    if (rc.isCoreReady()) {
                    	if(rc.senseNearbyRobots(2, myTeam).length>5){
                    		//move
                    		MapLocation ml = rc.getLocation();
                    		if(Math.abs(ml.x - archEnd.x) + Math.abs(ml.y - archEnd.y) < 3 ){
                    			archEnd = randomCircleLoc(archStart.x,archStart.y, radius, rand);
                    		}
                    		bugMove(rc, rc.getLocation(), archEnd);
                    		
                    		
                    		flag = true;
                    	}
                    	else{
	                    	if(buildingBreak <1){
		                    	if(robotCount==1){
		                    		if(rc.hasBuildRequirements(RobotType.SCOUT)){
		                    			Direction dirToBuild = directions[rand.nextInt(8)];
		                                for (int i = 0; i < 8; i++) {
		                                    // If possible, build in this direction
		                                    if (rc.canBuild(dirToBuild, RobotType.SCOUT)) {
		                                        rc.build(dirToBuild, RobotType.SCOUT);
		                                        break;
		                                    } else {
		                                        // Rotate the direction to try
		                                        dirToBuild = dirToBuild.rotateLeft();
		                                    }
		                                }
		                                robotCount++;
		                                flag = true;
		                    		}
		                    	}
		                    	else if(robotCount==2){
		                    		if(rc.hasBuildRequirements(RobotType.SOLDIER)){
		                    			Direction dirToBuild = directions[rand.nextInt(8)];
		                                for (int i = 0; i < 8; i++) {
		                                    // If possible, build in this direction
		                                    if (rc.canBuild(dirToBuild, RobotType.SOLDIER)) {
		                                        rc.build(dirToBuild, RobotType.SOLDIER);
		                                        break;
		                                    } else {
		                                        // Rotate the direction to try
		                                        dirToBuild = dirToBuild.rotateLeft();
		                                    }
		                                }
		                    			robotCount++;
		                    			if(robotCount==5){
		                    				robotCount=0;
		                    			}
		                    			flag = true;
		                    			buildingBreak = buildingBreakConstant;
		                    		}
		                    	}
		                    	else{
		                    		if(rc.hasBuildRequirements(RobotType.TURRET)){
		                    			Direction dirToBuild = directions[rand.nextInt(8)];
		                                for (int i = 0; i < 8; i++) {
		                                    // If possible, build in this direction
		                                    if (rc.canBuild(dirToBuild, RobotType.TURRET)) {
		                                        rc.build(dirToBuild, RobotType.TURRET);
		                                        break;
		                                    } else {
		                                        // Rotate the direction to try
		                                        dirToBuild = dirToBuild.rotateLeft();
		                                    }
		                                }
		                    			robotCount++;
		                    			if(robotCount==5){
		                    				robotCount=0;
		                    			}
		                    			flag = true;
		                    			buildingBreak = buildingBreakConstant;
		                    		}
		                    	}
	                    	} 
                    	}
                    	
                    	
	                    if(!flag){
	                    	//look for parts
	                    	MapLocation ml = rc.getLocation();
                    		if(Math.abs(ml.x - archEnd.x) + Math.abs(ml.y - archEnd.y) < 3 ){
                    			archEnd = randomCircleLoc(archStart.x,archStart.y, radius, rand);
                    		}
                    		bugMove(rc, rc.getLocation(), archEnd);
	                    }
                    	
                    }
                        
                    buildingBreak --;

                    Clock.yield();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        else if (rc.getType() == RobotType.TURRET || rc.getType() == RobotType.SOLDIER) {
            try {
                myAttackRange = rc.getType().attackRadiusSquared;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            while (true) {
                // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
                // at the end of it, the loop will iterate once per game round.
                try {
                    // If this robot type can attack, check for enemies within range and attack one
                    if (rc.isWeaponReady()) {
                        RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(myAttackRange, enemyTeam);
                        RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(myAttackRange, Team.ZOMBIE);
                        if (enemiesWithinRange.length > 0) {
                            for (RobotInfo enemy : enemiesWithinRange) {
                                // Check whether the enemy is in a valid attack range (turrets have a minimum range)
                                if (rc.canAttackLocation(enemy.location)) {
                                    rc.attackLocation(enemy.location);
                                    break;
                                }
                            }
                        } else if (zombiesWithinRange.length > 0) {
                            for (RobotInfo zombie : zombiesWithinRange) {
                                if (rc.canAttackLocation(zombie.location)) {
                                    rc.attackLocation(zombie.location);
                                    break;
                                }
                            }
                        }
                    }

                    Clock.yield();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        else{
        	try {
               
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        	while (true) {
        		try {
        			Clock.yield();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
        	
        	}
        }
	}
	public static MapLocation randomCircleLoc(int x, int y, int radius, Random rand){
		double dir = rand.nextDouble()*3.14159;
		int dx = (int)(Math.cos(dir) *((double)radius));
		int dy = (int)(Math.sin(dir) *((double)radius));
		if(rand.nextInt(2) == 0){
			dy = -dy;
		}
		
		return new MapLocation(dx+x,dy+y);
	}
	public static void bugMove(RobotController rc, MapLocation start, MapLocation end){
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
				rc.move(dir);
			}
			else{
				//rc.clearRubble(dir);
			}
		} catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
	}
	
}
