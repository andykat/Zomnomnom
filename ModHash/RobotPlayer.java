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
				   {0}
				  };
		int[][] ranges1 = {{4,4,4,50,100,50,100},
				   {0}
				  };
		int[] ranges0Product = new int[ranges0.length];
		int[] ranges1Product = new int[ranges1.length];
		for(int i=0;i<ranges0.length;i++){
			ranges0Product[i] = 16;
			ranges1Product[i] = 1;
			for(int j=1;j<ranges0[i].length;j++){ //!!!!!!!! j = 1
				ranges0Product[i] *= ranges0[i][j];				
			}
			for(int j=1;j<ranges1[i].length;j++){
				ranges1Product[i] *= ranges1[i][j];
			}
		}
		
		/*returns two integers used for the signal
      * takes arrayList as an input created from the hashMessagePrep method
      * lambda is fucking weird
      */
     Function<ArrayList<Integer>,int[]> hashMessage = m -> {
     	int[] reMsg = new int[2];
     	int msgType = m.get(0);
     	reMsg[0] = msgType - 2147483647;
     	int list0Length = m.get(1);
     	int list1Length = m.get(2);
     	int multiplier = 16;
     	int curIndex = 3;
     	for(int i=list0Length-1;i>-1;i--){
     		reMsg[0] += multiplier * m.get(i+curIndex);
     		multiplier *= ranges0[msgType][i];
     	}
     	curIndex += list0Length;
     	
     	multiplier = 1;
     	reMsg[1] = -2147483647;
     	for(int i=list1Length-1;i>-1;i--){
     		reMsg[1] += multiplier * m.get(i+curIndex);
     		multiplier *= ranges1[msgType][i];
     	}
     	return reMsg;
     };
     
     /*
      * takes in an int list of length 2 (signal).
      * returns a 2d array list of size 2 containing 2 lists of ints
      */
     Function<int[], int[][]> unhashMessage = s -> {
     	long l0 = s[0];
         l0 += 2147483647;
         long l1 = s[1];
         l1 += 2147483647;
         int msgType = (int) (l0%16);
         int[] list0 = new int[ranges0[msgType].length];
     	int[] list1 = new int[ranges1[msgType].length];
     	
         int product0 = ranges0Product[msgType];
         for(int i=0;i<list0.length-1;i++){
         	list0[i] = (int)(l0/product0);
         	l0 -= list0[i] * product0;
         	product0 /= ranges0[msgType][i+1];
         }
         list0[list0.length-1] = (int)l0/product0;
         
         int product1 = ranges1Product[msgType];
         for(int i=0;i<list1.length-1;i++){
         	list1[i] = (int)(l1/product1);
         	l1 -= list1[i] * product1;
         	product1 /= ranges1[msgType][i+1];
         }
         list1[list1.length-1] = (int)l1/product1;
     	
     	int[][] rList = {list0, list1};
     	return rList;
     };
     ////
     ////
        
        
        int myAttackRange = 0;
        Team myTeam = rc.getTeam();
        Team enemyTeam = myTeam.opponent();
        int[] signalInts = new int[2];
        if (rc.getType() == RobotType.ARCHON) {
        	try {
        		///////////////////////////
        		// Preparing the signals to send
        		/////////////////////////
        		int[] list0 = {1,45,97,12,12};
                int[] list1 = {2,3,2,11,11,11,11};
                signalInts = hashMessage.apply(hashMessagePrep(0,list0,list1));
        	} catch (Exception e) {

                System.out.println(e.getMessage());
                e.printStackTrace();
            }  
        	
            try {
                while(true){
                	if(rc.isCoreReady()){
                		rc.broadcastMessageSignal(signalInts[0],signalInts[1] , 400);
                        if (rc.hasBuildRequirements(RobotType.SCOUT)) {
                            // Choose a random direction to try to build in
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
                        }
                	}
                	///////////////////////////
                	// Preparing the signals to send
                	/////////////////////////
                	
                }
            } catch (Exception e) {

                System.out.println(e.getMessage());
                e.printStackTrace();
            }              
        }
        else if(rc.getType() == RobotType.SCOUT){
        	try {
        		
        	} catch (Exception e) {

                System.out.println(e.getMessage());
                e.printStackTrace();
            }  
        	
        	try {
        		while(true){
        			if(rc.isCoreReady()){
        				Signal[] signals = rc.emptySignalQueue();
	                    
	                    	for(int i=0;i<signals.length;i++){
	                    		if(signals[i].getTeam() == myTeam){
	                        		int[] msg = signals[i].getMessage();
	                        		if(msg!=null){
	                        			///////////////////////////
	                                	// Unhash signal from 2 ints into 2 lists
	                                	/////////////////////////
	                        			int [][] lists = unhashMessage.apply(msg);
	                        			
	                        			//dumbass ~200 xy coordinate offset
	                        			MapLocation dest = new MapLocation(lists[0][1] + 100,lists[0][2] + 200);
	                        			
		                        		bugMove(rc, rc.getLocation(), dest);
		                        		break;
	                        		}
	                    		}
	                    	}
        			}
        		}
        	} catch (Exception e) {
        		
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
    public static ArrayList<Integer> hashMessagePrep(int msgType, int[] list0, int[] list1){
    	ArrayList<Integer> m = new ArrayList<Integer>();
    	m.add(msgType);
    	m.add(list0.length);
    	m.add(list1.length);
    	for(int i=0;i<list0.length;i++){
    		m.add(list0[i]);
    	}
    	for(int i=0;i<list1.length;i++){
    		m.add(list1[i]);
    	}
    	return m;
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
