package danielsrobot;

import battlecode.common.*;

import java.lang.Math;
import java.util.ArrayList;

public class Archon extends RobotRunner {
	private enum mode{MAKE_SBABY,WAIT_FOR_BABY,RUN_TO_ENEMY, RUN_AWAY};
	private mode currentMode;
	private Information memory;
	private RobotInfo baby;
	private int numSoldiersToSummon= 5;
	private int numGaurdsToSummon= 5;
    private Move move;	

	public Archon(RobotController rcin) {
		super(rcin);
		memory= new Information();
		currentMode= mode.MAKE_SBABY;
        move = new Move();
	}
	

	public void run() throws GameActionException{
        RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), RobotType.ARCHON.sensorRadiusSquared);
        if(enemies.length > 0) {
            currentMode = mode.RUN_AWAY;
        }
		if (rc.isCoreReady()){
			switch (currentMode){
            case RUN_AWAY:
                runawayMove(rc, enemies);
                break;

			case MAKE_SBABY:
				Direction canBuildDir= getSpawnableDir(RobotType.SCOUT);
				if (canBuildDir!= null){
					rc.build(canBuildDir, RobotType.SCOUT);
					RobotInfo[] potentialBabies= rc.senseNearbyRobots(2);
					for (int n= 0; n< potentialBabies.length; n++){
						if (potentialBabies[n].type.equals(RobotType.SCOUT)){
							baby= potentialBabies[n];
							//System.out.println("FOUND BABY!");
							currentMode= mode.WAIT_FOR_BABY;
							break;
						}
					}
				}
				
				break;

			case WAIT_FOR_BABY:
//				RobotType summon= RobotConstants.posNRobotTypes[randall.nextInt(2)+3];
//				if (summon.equals(RobotType.SOLDIER)){
//					if (numSoldiersToSummon> 0){
//						numSoldiersToSummon--;
//						buildRobot(summon);
//					}
//				}else if (summon.equals(RobotType.GUARD)){
//					if (numGaurdsToSummon> 0){
//						numGaurdsToSummon--;
//						buildRobot(summon);
//					}
//				}
//				
//				if (numGaurdsToSummon<= 0 && numSoldiersToSummon<= 0){
//					bugMove(rc.getLocation(), eden);
//				}
				break;

			default:
				//Move back towards the archon?
				break;
			}
		}
	}

    public void runawayMove(RobotController rc, RobotInfo[] enemies) throws GameActionException{
            MapLocation myLoc = rc.getLocation();
            boolean canMove = false;
            int[] zVec = awayFromZombies(rc, enemies); 
            int[] eVec = awayFromEnemyTeam(rc, enemies);
            
            int dx = zVec[0] + eVec[0];
            int dy = zVec[1] + eVec[1];
            MapLocation targetLoc = myLoc.add(dx, dy);
            Direction dir = myLoc.directionTo(targetLoc);

            move.bugMove(rc, targetLoc);
            
            /*
            int rotation = 182842;  // random number initialize

            if(dir == Direction.OMNI) {
                if(rc.isCoreReady()) {
                    
                }
            }
            else if(!rc.canMove(dir)) {
                rotation = optimalRotation(rc,dir,enemies);
            }
            int c = 0;
            Direction moveDir = dir;
            

            // only rotate 180 degrees cuz rotating further is meaningless. you're just going to walk into enemy
            while (!rc.canMove(moveDir) && c < 8) {
                if(rotation == 1) {
                    moveDir = moveDir.rotateRight();
                }
                else if(rotation == -1) {
                    moveDir = moveDir.rotateLeft();
                }
                else {
                    break;
                }

                c++;
            } // end of while
            
            Direction buildDir = dir.opposite();
            if(rotation == 0) {
                System.out.println("HERE");
                if(rc.isCoreReady()) {
                    while(!rc.canBuild(buildDir, RobotType.GUARD)) {
                        if(rotation == 1) {
                            buildDir = buildDir.rotateRight();
                        }
                        else if(rotation == -1) {
                            buildDir = buildDir.rotateLeft();
                        }
                    }
                    rc.build(buildDir, RobotType.GUARD);
                }
            }
            else {
                rc.move(moveDir);
            }*/
    }

    public int[] getMaxSensibleXY(RobotController rc) {
        int r = rc.getType().sensorRadiusSquared;
        int x=0, y=0, max = 0;
        
        for(int i=0; i < Math.sqrt(r); i++) {
            for(int j=0; j < Math.sqrt(r); j++) {
                int a = i*i + j*j;
                if((a <= r) && a > max) {
                    x = i;
                    y = j;
                    max = a;
                }
            }
        }
        int[] xy = {x, y};
        return xy;
    }

    // 1 = right rotate
    // 0 = u fucked
    // -1 = left rotate
    public int optimalRotation(RobotController rc, Direction dir, RobotInfo[] enemies) {
        MapLocation curLoc = rc.getLocation();
        MapLocation[] sensibleLoc = null;
        int[] center = getMaxSensibleXY(rc);
        int halfRadiusSquared = (int) Math.pow(Math.floor((Math.sqrt(rc.getType().sensorRadiusSquared)/2)), 2);
        // MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(), rc.getType().sensorRadiusSquared);
        if(dir == Direction.NORTH_EAST) {
            sensibleLoc = MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(center[0], -center[1]), halfRadiusSquared);
            if(sensibleLoc.length < 4) {
                return -1;
            }
        }
        else if(dir == Direction.NORTH_WEST) {
            sensibleLoc = MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(-center[0], -center[1]), halfRadiusSquared);
            if(sensibleLoc.length < 4) {
                return 1;
            }
        }
        else if(dir == Direction.SOUTH_WEST) {
            sensibleLoc = MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(-center[0], center[1]), halfRadiusSquared);
            if(sensibleLoc.length < 4) {
                return -1;
            }
        }
        else if(dir == Direction.SOUTH_EAST) {
            sensibleLoc = MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(center[0], center[1]), halfRadiusSquared);
            if(sensibleLoc.length < 4) {
                return 1;
            }
        }
        else if(dir == Direction.NORTH) {
            sensibleLoc = MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(0, -center[1]), halfRadiusSquared);
            if(sensibleLoc.length < 4) {
                return 1;
            }
        }
        else if(dir == Direction.EAST) {
            sensibleLoc = MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(center[0], 0), halfRadiusSquared);
            if(sensibleLoc.length < 4) {
                return 1;
            }
        }
        else if(dir == Direction.SOUTH) {
            sensibleLoc = MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(0, center[1]), halfRadiusSquared);
            if(sensibleLoc.length < 4) {
                return 1;
            }
        }
        else if(dir == Direction.WEST) {
            sensibleLoc = MapLocation.getAllMapLocationsWithinRadiusSq(curLoc.add(-center[0], 0), halfRadiusSquared);
            if(sensibleLoc.length < 4) {
                return 1;
            }
        }
        else {
            return 1;
        }

        ArrayList<Rubble> rubbles = new ArrayList<Rubble>();
        for(MapLocation loc : sensibleLoc) {
            if(!rc.canSense(loc)) {
                int dx = loc.x - curLoc.x;
                int dy = loc.y - curLoc.y;

                if(!rc.canSense(curLoc.add(dx, dy))) {
                    if(!rc.canSense(curLoc.add(dx, 0))) {
                        if((dx > 0 && dy > 0) || (dx < 0 && dy < 0)) {
                            return 1;
                        }
                        else {
                            return -1;
                        }
                    }
                    else if(!rc.canSense(curLoc.add(0, dy))){
                        if((dx > 0 && dy > 0) || (dx < 0 && dy < 0)) {
                            return -1;
                        }
                        else {
                            return 1;
                        }
                    }
                } // end of avoiding map edges

                else {
                    double amount = rc.senseRubble(loc);
                    if(amount > 0) {
                        rubbles.add(new Rubble(loc, amount));
                    }
                }
            }
        }

        int obstacleX = 0;
        int obstacleY = 0;

        for(Rubble rubble : rubbles) {
            int xLoc = rubble.getLocation().x;
            int yLoc = rubble.getLocation().y;
            if(rubble.getAmount() < 100) {
            }
        }

        return 1;
    }

    public int[] awayFromZombies(RobotController rc, RobotInfo[] enemies) throws GameActionException {
        int dx = 0, dy = 0;
        int multiplier = 1; // repulsion force multiplier
        MapLocation myLoc = rc.getLocation();

        for(RobotInfo r : enemies) {
            MapLocation enemyLoc = r.location;
            if(r.type == RobotType.BIGZOMBIE) {
                multiplier = 3;
            }
            else if(r.type == RobotType.RANGEDZOMBIE) {
                multiplier = 2;
            }
            else {
                multiplier = 1;
            }
            dx += multiplier * (myLoc.x - enemyLoc.x);
            dy += multiplier * (myLoc.y - enemyLoc.y);
        } 
        int[] vector = {dx, dy};
        System.out.println("zombies dx: " + dx + " dy: " + dy);
        return vector;

        /*
        Direction away = myLoc.directionTo(runVector);
        if(away == Direction.NORTH) {
            if(dx < 0) {
                away = Direction.NORTH_WEST;
            } else {
                away = Direction.NORTH_EAST;
            }
        }
        else if(away == Direction.SOUTH) {
            if(dx < 0) {
                away = Direction.SOUTH_WEST;
            }
            else {
                away = Direction.SOUTH_EAST;
            }
        }
        else if(away == Direction.EAST) {
            if(dy < 0) {
                away = Direction.NORTH_EAST;
            }
            else {
                away = Direction.SOUTH_EAST;
            }
        }
        else if(away == Direction.WEST) {
            if(dy < 0) {
                away = Direction.NORTH_WEST;
            }
            else {
                away = Direction.SOUTH_WEST;
            }
        }
        return runVector;
        */
    }

    public int[] awayFromEnemyTeam(RobotController rc, RobotInfo[] enemies) {
        int dx = 0, dy = 0;
        int multiplier = 1; // repulsion force multiplier
        MapLocation myLoc = rc.getLocation();
        Team enemyTeam = rc.getTeam().opponent();

        for(RobotInfo r : enemies) {
            if(r.team == enemyTeam) {
                MapLocation enemyLoc = r.location;
                if(willDieInTurns(rc, enemies, 10)) {
                    multiplier = -10;  // hella move towards them
                } 
                else {
                    multiplier = 3;     // get the f away from them
                }
                dx += multiplier * (myLoc.x - enemyLoc.x);
                dy += multiplier * (myLoc.y - enemyLoc.y);
            }
        }
        System.out.println("enemies dx: " + dx + " enemies dy: " + dy);
        int[] vector = {dx, dy};
        return vector;

    }

    // Very approximate. Not accurate at all. Heuristic af. But whatever.
    public boolean willDieInTurns(RobotController rc, RobotInfo[] enemies, int turns) {
        double totaldmg = 0.0;
        double weapondelay = 0.5;

        for(RobotInfo r : enemies) {
            totaldmg += (weapondelay * turns * r.attackPower);
        }
        
        if(rc.getHealth() < totaldmg) {
            return true;
        }

        return false;
    }


}
