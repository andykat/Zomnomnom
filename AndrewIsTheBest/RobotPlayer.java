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
        int broadcastCount = 5;
        int broadcastCountConstant = 100;
        MapLocation archStart = new MapLocation(0,0);
        MapLocation archEnd = new MapLocation(0,0);
        MapLocation closestArchon = new MapLocation(10000,10000);
        int closestArchonDistance = rc.getLocation().distanceSquaredTo(closestArchon);
        int radius = 5;
        int turretMove = -2;
        int turretMoveConstant = 5;
        if (rc.getType() == RobotType.ARCHON) {
            try {
                // Any code here gets executed exactly once at the beginning of the game.
            	int archonCount = rc.getRobotCount();
            	if(archonCount>6){
            		buildingBreakConstant = 160;
            	}
            	archStart = rc.getLocation();
            	
            	archEnd = randomCircleLoc(archStart.x,archStart.y, radius, rand);
            	
            	robotCount = 0;
            	rc.broadcastSignal(250);
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
                    	if(broadcastCount<1){
                    		rc.broadcastSignal(150);
                    		broadcastCount = broadcastCountConstant;
                    		archStart = rc.getLocation();
                    		archEnd = archStart;
                    		flag = true;
                    	}
                    	else if(rc.senseNearbyRobots(2, myTeam).length>6){
                    		//move
                    		MapLocation ml = rc.getLocation();
                    		if(Math.abs(ml.x - archEnd.x) + Math.abs(ml.y - archEnd.y) < 3 ){
                    			archEnd = randomCircleLoc(archStart.x,archStart.y, radius, rand);
                    		}
                    		if(ml.distanceSquaredTo(archStart) > radius + 1){
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
		                    	else if(robotCount==3){
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
                    	Signal[] signals = rc.emptySignalQueue();
                    	for(int i=0;i<signals.length;i++){
                    		if(signals[i].getTeam() == myTeam){
                        		int[] msg = signals[i].getMessage();
                        		if(msg==null){
                        			MapLocation ml = signals[i].getLocation();
                        			int dist = rc.getLocation().distanceSquaredTo(ml);
                        			if(dist < closestArchonDistance && dist>2){
                        				closestArchon = ml;
                        				closestArchonDistance = dist;
                        			}
                        		}
                    		}
                    	}
	                    if(!flag){
	                    	if(closestArchonDistance>1000 && closestArchonDistance < 10){
		                    	//look for parts
		                    	MapLocation ml = rc.getLocation();
	                    		if(Math.abs(ml.x - archEnd.x) + Math.abs(ml.y - archEnd.y) < 3 ){
	                    			archEnd = randomCircleLoc(archStart.x,archStart.y, radius, rand);
	                    		}
	                    		if(ml.distanceSquaredTo(archStart) > radius + 1){
	                    			archEnd = randomCircleLoc(archStart.x,archStart.y, radius, rand);
	                    		}
	                    		bugMove(rc, rc.getLocation(), archEnd);
	                    	}
	                    	else{
	                    		//move toward closest Archon
	                    		bugMove(rc, rc.getLocation(), closestArchon);
	                    	}
	                    	
	                    }
                    	
                    }
                        
                    buildingBreak --;
                    broadcastCount--;
                    
                    Clock.yield();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        else if (rc.getType() == RobotType.TURRET) {
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
                	if(rc.getType()==RobotType.TURRET){
                		
                	
	                	boolean attkFlag = false;
	                    if (rc.isWeaponReady()) {
	                    	
	                        RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(myAttackRange, enemyTeam);
	                        RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(myAttackRange, Team.ZOMBIE);
	                        if (enemiesWithinRange.length > 0) {
	                            for (RobotInfo enemy : enemiesWithinRange) {
	                                // Check whether the enemy is in a valid attack range (turrets have a minimum range)
	                                if (rc.canAttackLocation(enemy.location)) {
	                                    rc.attackLocation(enemy.location);
	                                    attkFlag = true;
	                                    break;
	                                }
	                            }
	                        } else if (zombiesWithinRange.length > 0) {
	                            for (RobotInfo zombie : zombiesWithinRange) {
	                                if (rc.canAttackLocation(zombie.location)) {
	                                    rc.attackLocation(zombie.location);
	                                    attkFlag = true;
	                                    break;
	                                }
	                            }
	                        }
	                        
	                        
	                    }
	                    Signal[] signals = rc.emptySignalQueue();
	                    
	                    if(!attkFlag){
	                    	//search for enemies to attack from broadcast signals.
	                    	for(int i=0;i<signals.length;i++){
	                    		if(signals[i].getTeam() == myTeam){
	                        		int[] msg = signals[i].getMessage();
	                        		if(msg!=null){
		                        		MapLocation eloc = new MapLocation(msg[0],msg[1]);
		                        		if (rc.canAttackLocation(eloc) && rc.isWeaponReady()) {
		                                    rc.attackLocation(eloc);
		                                    attkFlag = true;
		                                    break;
		                                }
	                        		}
	                        		else{
	                        			MapLocation ml = signals[i].getLocation();
	                        			int dist = rc.getLocation().distanceSquaredTo(ml);
	                        			if(dist < closestArchonDistance && dist>2){
	                        				closestArchon = ml;
	                        				closestArchonDistance = dist;
	                        			}
	                        		}
	                    		}
	                    	}
	                    	if(!attkFlag && closestArchonDistance < 500 && closestArchonDistance > 10 && turretMove==-2){
	                    		rc.pack();
	                    		turretMove = turretMoveConstant;
	                    	}
	                    }
                	}
                	else{
            			if(rc.isCoreReady()){
                			if(turretMove>0)
                			{
                    			bugMove(rc, rc.getLocation(), closestArchon);
                    			turretMove--;
                			}
                			else{
                        		rc.unpack();
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
        else if(rc.getType() == RobotType.TTM){
        	try {
                
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        	while (true) {
                // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
                // at the end of it, the loop will iterate once per game round.
                try {
                	if(closestArchonDistance > 10 && closestArchonDistance<500){
                		if(rc.isCoreReady()){
                			bugMove(rc, rc.getLocation(), closestArchon);
                		}
                	}
                	else{
                		rc.unpack();
                	}
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
                
        }
        else if (rc.getType() == RobotType.SOLDIER) {
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
                    	boolean attkFlag = false;
                        RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(myAttackRange, enemyTeam);
                        RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(myAttackRange, Team.ZOMBIE);
                        if (enemiesWithinRange.length > 0) {
                            for (RobotInfo enemy : enemiesWithinRange) {
                                // Check whether the enemy is in a valid attack range (turrets have a minimum range)
                                if (rc.canAttackLocation(enemy.location)) {
                                    rc.attackLocation(enemy.location);
                                    attkFlag = true;
                                    break;
                                }
                            }
                        } else if (zombiesWithinRange.length > 0) {
                            for (RobotInfo zombie : zombiesWithinRange) {
                                if (rc.canAttackLocation(zombie.location)) {
                                    rc.attackLocation(zombie.location);
                                    attkFlag = true;
                                    break;
                                }
                            }
                        }
                        Signal[] signals = rc.emptySignalQueue();
                      //search for closest archon
                    	for(int i=0;i<signals.length;i++){
                    		if(signals[i].getTeam() == myTeam){
                        		int[] msg = signals[i].getMessage();
                        		
                        		if(msg==null){
                        			MapLocation ml = signals[i].getLocation();
                        			int dist = rc.getLocation().distanceSquaredTo(ml);
                        			if(dist < closestArchonDistance && dist>2){
                        				closestArchon = ml;
                        				closestArchonDistance = dist;
                        			}
                        		}
                    		}
                    	}
                        if(!attkFlag){
                        	if(closestArchonDistance > 50 && closestArchonDistance<500){
                        		if(rc.isCoreReady()){
                        			bugMove(rc, rc.getLocation(), closestArchon);
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
        else if(rc.getType() == RobotType.SCOUT){
        	try {
        		 myAttackRange = 2*rc.getType().sensorRadiusSquared;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        	while (true) {
        		try {
        			
        			RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(myAttackRange, enemyTeam);
                    RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(myAttackRange, Team.ZOMBIE);
                    if (enemiesWithinRange.length > 0) {
                        for (RobotInfo enemy : enemiesWithinRange) {
                       	 rc.broadcastMessageSignal(enemy.location.x, enemy.location.y, myAttackRange);
                        }
                    } else if (zombiesWithinRange.length > 0) {
                        for (RobotInfo zombie : zombiesWithinRange) {
                        	rc.broadcastMessageSignal(zombie.location.x, zombie.location.y, myAttackRange);
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
