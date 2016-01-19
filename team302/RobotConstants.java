package team302;
import battlecode.common.Direction;
import battlecode.common.RobotType;

public interface RobotConstants {
    public static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
    public static RobotType[] posNRobotTypes= {null,RobotType.ARCHON,RobotType.SCOUT, RobotType.SOLDIER, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET, RobotType.TTM, RobotType.STANDARDZOMBIE, RobotType.RANGEDZOMBIE,RobotType.FASTZOMBIE, RobotType.BIGZOMBIE};
	//12 possible robot types
	public static enum mapTypes {RUBBLE, ZOMBIE_DEN, PARTS};
	public static int[] posDirs= new int[]{0,1,-1,2,-2,3,-3,4};
	public static int patienceMultiplier= 20;
}
