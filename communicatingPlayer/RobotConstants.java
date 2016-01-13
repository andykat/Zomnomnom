package communicatingPlayer;

import battlecode.common.Direction;

public interface RobotConstants {
	public static int hunger= 5;// number after which the robot would eat its way towards the goal 
    public static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
    public static int SCOUT_MESSAGE_READ_LIMIT= 25;
    public static int SCOUT_SEARCH_RANGE= 6; //radius of the square of the scout's vision range
}
