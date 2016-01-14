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
       
        MessageHash meepo = new MessageHash();
        
        int myAttackRange = 0;
        Team myTeam = rc.getTeam();
        Team enemyTeam = myTeam.opponent();
        
        if (rc.getType() == RobotType.ARCHON) {
        	try {

                
        	} catch (Exception e) {

                System.out.println(e.getMessage());
                e.printStackTrace();
            }  
        	
            try {
                while(true){
                	if(rc.isCoreReady()){
                		Signal[] signals = rc.emptySignalQueue();
                		if(rc.getMessageSignalCount()==0){
                			///////////////////////////
                        	// Preparing the signals to send
                        	/////////////////////////
                			int[] list0 = {1,45,97,12,12}; //must be less than 28 bits
                            int[] list1 = {2,3,2,11,11,11,11}; // must be less than 32 bits
                            int[] list2 = {1,0,1};
                            int[] list3 = {0,1,0};
                            int messageType = 0;
                            int[] signalInts = new int[2];
                            signalInts = meepo.hashMessage(messageType, list0, list1);
                			rc.broadcastMessageSignal(signalInts[0],signalInts[1] , RobotType.SCOUT.sensorRadiusSquared*2);
                		}
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
                	Clock.yield();
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
	                        			int messageType = meepo.getMessageType(msg[0]);
	                        			
	                        			int [][] lists = meepo.unhashMessage(msg);
	                        			for(int j=0;j<lists[0].length;j++){
	                        				System.out.print(lists[0][j]);
	                        			}
	                        			System.out.print("\n");
	                        			//dumbass ~200 xy coordinate offset
	                        			//MapLocation dest = new MapLocation(lists[0][1] + 100,lists[0][2] + 200);
	                        			//MapLocation dest = new MapLocation(150,250);
		                        		//bugMove(rc, rc.getLocation(), dest);
		                        		break;
	                        		}
	                    		}
	                    	}
	                    	
        			}
        			Clock.yield();
        		}
        	} catch (Exception e) {
        		
                System.out.println(e.getMessage());
                e.printStackTrace();
            }  
        }
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


//
//public class RobotPlayer {
//	
//    /**
//     * run() is the method that is called when a robot is instantiated in the Battlecode world.
//     * If this method returns, the robot dies!
//     **/
//    @SuppressWarnings("unused")
//    public static void run(RobotController rc) {
//        // You can instantiate variables here.
//        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
//                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
//        RobotType[] robotTypes = {RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
//                RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET};
//        Random rand = new Random(rc.getID());
//        ////////////////////////////////////
//        //Msg Type Range set
//        //Max Product of numbers in ranges0 must be less than 268435456
//        //Max Product of numbers in ranges1 must be less than 4294967296
//        ////////////////////////////////////
//        int[][] ranges0 = {{4,50,100,50,100},
//				   {2,2,2},
//				   {12,12,12}
//				  };
//        
//		int[][] ranges1 = {{4,4,4,50,100,50,100},
//				   {2,2,2},
//				   {23,23,2}
//				  };
//		
//		int[] ranges0Product = new int[ranges0.length];
//		int[] ranges1Product = new int[ranges1.length];
//		for(int i=0;i<ranges0.length;i++){
//			ranges0Product[i] = 16;
//			ranges1Product[i] = 1;
//			for(int j=1;j<ranges0[i].length;j++){ //!!!!!!!! j = 1
//				ranges0Product[i] *= ranges0[i][j];				
//			}
//			for(int j=1;j<ranges1[i].length;j++){
//				ranges1Product[i] *= ranges1[i][j];
//			}
//		}
//		
//		
//     
//        int myAttackRange = 0;
//        Team myTeam = rc.getTeam();
//        Team enemyTeam = myTeam.opponent();
//        
//        if (rc.getType() == RobotType.ARCHON) {
//        	try {
//
//                
//        	} catch (Exception e) {
//
//                System.out.println(e.getMessage());
//                e.printStackTrace();
//            }  
//        	
//            try {
//                while(true){
//                	if(rc.isCoreReady()){
//                		Signal[] signals = rc.emptySignalQueue();
//                		if(rc.getMessageSignalCount()==0){
//                			///////////////////////////
//                        	// Preparing the signals to send
//                        	/////////////////////////
//                			int[] list0 = {1,45,97,12,12}; //must be less than 28 bits
//                            int[] list1 = {2,3,2,11,11,11,11}; // must be less than 32 bits
//                            int[] list2 = {1,0,1};
//                            int[] list3 = {0,1,0};
//                            int messageType = 1;
//                            int[] signalInts = new int[2];
//                            signalInts = hashMessage(messageType, list2, list3, ranges0[messageType], ranges1[messageType]);
//                			rc.broadcastMessageSignal(signalInts[0],signalInts[1] , RobotType.SCOUT.sensorRadiusSquared*2);
//                		}
//                        if (rc.hasBuildRequirements(RobotType.SCOUT)) {
//                            // Choose a random direction to try to build in
//                            Direction dirToBuild = directions[rand.nextInt(8)];
//                            for (int i = 0; i < 8; i++) {
//                                // If possible, build in this direction
//                                if (rc.canBuild(dirToBuild, RobotType.SCOUT)) {
//                                    rc.build(dirToBuild, RobotType.SCOUT);
//                                    break;
//                                } else {
//                                    // Rotate the direction to try
//                                    dirToBuild = dirToBuild.rotateLeft();
//                                }
//                            }
//                        }
//                	}
//                	Clock.yield();
//                }
//            } catch (Exception e) {
//
//                System.out.println(e.getMessage());
//                e.printStackTrace();
//            }              
//        }
//        else if(rc.getType() == RobotType.SCOUT){
//        	try {
//        		
//        	} catch (Exception e) {
//
//                System.out.println(e.getMessage());
//                e.printStackTrace();
//            }  
//        	
//        	try {
//        		while(true){
//        			if(rc.isCoreReady()){
//        				Signal[] signals = rc.emptySignalQueue();
//	                    
//	                    	for(int i=0;i<signals.length;i++){
//	                    		if(signals[i].getTeam() == myTeam){
//	                        		int[] msg = signals[i].getMessage();
//	                        		if(msg!=null){
//	                        			///////////////////////////
//	                                	// Unhash signal from 2 ints into 2 lists
//	                                	/////////////////////////
//	                        			int messageType = getMessageType(msg[0]);
//	                        			
//	                        			int [][] lists = unhashMessage(msg, ranges0[messageType], ranges1[messageType], ranges0Product[messageType], ranges1Product[messageType]);
//	                        			/*for(int j=0;j<lists[0].length;j++){
//	                        				System.out.print(lists[0][j]);
//	                        			}
//	                        			System.out.print("\n");*/
//	                        			//dumbass ~200 xy coordinate offset
//	                        			//MapLocation dest = new MapLocation(lists[0][1] + 100,lists[0][2] + 200);
//	                        			//MapLocation dest = new MapLocation(150,250);
//		                        		//bugMove(rc, rc.getLocation(), dest);
//		                        		break;
//	                        		}
//	                    		}
//	                    	}
//	                    	
//        			}
//        			Clock.yield();
//        		}
//        	} catch (Exception e) {
//        		
//                System.out.println(e.getMessage());
//                e.printStackTrace();
//            }  
//        }
//    }
//    
//    /*takes in two list of ranges. For now I am using 16 different msg types, so msgType must be 0-15.
//     * Two lists of ranges must be sent along with two lists of numbers to hash
//     * The ranges just contain the number of units, so if I need 0-99, the number is 100.
//     * The first number only has 28 bits, meaning the product of the max of the first range
//     * must be less than 268435456.
//     * The second number has 32 bits, so the product is less than 4294967296
//     * Two integers are returned, and to be used as the two integers in the signal 
//     * */ 
//    /*returns two integers used for the signal
//     * takes arrayList as an input created from the hashMessagePrep method
//     * lambda is fucking weird
//     */
//    
//    public static int[] hashMessage(int msgType, int[] list0, int[] list1, int[] ranges0, int[] ranges1){
//    	int[] reMsg = new int[2];
//     	reMsg[0] = msgType - (Integer.MAX_VALUE - 1);
//     	
//     	int multiplier = 16;
//     	for(int i=list0.length-1;i>-1;i--){
//     		reMsg[0] += multiplier * list0[i];
//     		multiplier *= ranges0[i];
//     	}
//     	
//     	multiplier = 1;
//     	reMsg[1] = -(Integer.MAX_VALUE - 1);
//     	for(int i=list1.length-1;i>-1;i--){
//     		reMsg[1] += multiplier * list1[i];
//     		multiplier *= ranges1[i];
//     	}
//     	return reMsg;
//    }
//    public static int getMessageType(int s){
//    	long l0 = s;
//        l0 += Integer.MAX_VALUE - 1;
//        return (int) (l0%16);
//    }
//    /*
//     * takes in an int list of length 2 (signal).
//     * returns a 2d array list of size 2 containing 2 lists of ints
//     */
//    public static int[][] unhashMessage(int[] s, int[] ranges0, int[] ranges1, int product0, int product1){
//     	long l0 = s[0];
//         l0 += Integer.MAX_VALUE - 1;
//         long l1 = s[1];
//         l1 += Integer.MAX_VALUE - 1;
//         int msgType = (int) (l0%16);
//         int[] list0 = new int[ranges0.length];
//     	int[] list1 = new int[ranges1.length];
//     	
//        // int product0 = ranges0Product[msgType];
//         for(int i=0;i<list0.length-1;i++){
//         	list0[i] = (int)(l0/product0);
//         	l0 -= list0[i] * product0;
//         	product0 /= ranges0[i+1];
//         }
//         list0[list0.length-1] = (int)l0/product0;
//         
//         //int product1 = ranges1Product[msgType];
//         for(int i=0;i<list1.length-1;i++){
//         	list1[i] = (int)(l1/product1);
//         	l1 -= list1[i] * product1;
//         	product1 /= ranges1[i+1];
//         }
//         list1[list1.length-1] = (int)l1/product1;
//     	
//     	int[][] rList = {list0, list1};
//     	return rList;
//     };
//    public static void bugMove(RobotController rc, MapLocation start, MapLocation end){
//		try {
//			Direction dir = start.directionTo(end);
//			int c=0;
//			while(!rc.canMove(dir))
//			{
//				dir = dir.rotateRight();
//				if(c>7){
//					break;
//				}
//				c++;
//			}
//			if(c<8){
//				rc.move(dir);
//			}
//			else{
//				//rc.clearRubble(dir);
//			}
//		} catch (Exception e) {
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//        }
//	}
//}
