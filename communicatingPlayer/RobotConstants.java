package communicatingPlayer;

import battlecode.common.Direction;
import battlecode.common.RobotType;

public interface RobotConstants {
	public static int SCOUT_HUNGER= 10;// number after which the robot would eat its way towards the goal 
    public static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
    public static int SCOUT_MESSAGE_READ_LIMIT= 25;
    public static int SCOUT_SEARCH_RANGE= 6; //radius of the square of the scout's vision range
	public static RobotType[] posNRobotTypes= {null,RobotType.ARCHON,RobotType.SCOUT, RobotType.SOLDIER, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET, RobotType.TTM, RobotType.STANDARDZOMBIE, RobotType.RANGEDZOMBIE,RobotType.FASTZOMBIE, RobotType.BIGZOMBIE};
	//12 possible robot types
	public static enum mapTypes {RUBBLE, ZOMBIE_DEN, PARTS};
}
