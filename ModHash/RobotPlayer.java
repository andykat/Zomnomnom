package ModHash;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;

public class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) {
        // You can instantiate variables here.
        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
        RobotType[] robotTypes = {RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
                RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET};
        Random rand = new Random(rc.getID());
        
        int[][] ranges0 = {{4,50,100,50,100},
        				   {}
        				  };
        int[][] ranges1 = {{4,4,4,50,100,50,100},
        				   {}
        				  };
        Function<ArrayList<Integer>,int[]> hashMessage = m -> {
        	int[] reMsg = new int[2];
        	reMsg[0] = m.get(0);
        	int multiplier = 16;
        	for(int i=0;i<list0.length;i++){
        		reMsg[0] += multiplier + list0[i];
        		multiplier *= ranges0[msgType][i];
        		
        	}
        	return reMsg;
        };
        int myAttackRange = 0;
        Team myTeam = rc.getTeam();
        Team enemyTeam = myTeam.opponent();

        if (rc.getType() == RobotType.ARCHON) {
            try {
                // Any code here gets executed exactly once at the beginning of the game.
            } catch (Exception e) {
                // Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
                // Caught exceptions will result in a bytecode penalty.
                System.out.println(e.getMessage());
                e.printStackTrace();
            }              
        }
    }
    
    /*takes in two list of ranges. For now I am using 16 different msg types, so msgType must be 0-15.
     * Two lists of ranges must be sent along with two lists of numbers to hash
     * The ranges just contain the number of units, so if I need 0-99, the number is 100.
     * The first number only has 28 bits, meaning the product of the max of the first range
     * must be less than 268435456.
     * The second number has 32 bits, so the product is less than 4294967296
     * Two integers are returned, and to be used as the two integers in the signal 
     * */ 
    public static ArrayList<Integer> hashMessage(int msgType, int[] list0, int[] list1){
    	ArrayList<Integer> m = new ArrayList<Integer>();
    	m.add(msgType);
    	m.add(list0.length);
    	m.add(list1.length);
    	for(int i=0;i<list0.length;i++){
    		m.add(list0[i]);
    	}
    	for(int i=0;i<list0.length;i++){
    		m.add(list1[i]);
    	}
    	return m;
    }
    
}
