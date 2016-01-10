package communicatingPlayer;
import battlecode.common.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

//MAP ITERATION: http://stackoverflow.com/questions/46898/how-to-efficiently-iterate-over-each-entry-in-a-map

//20 message signals max per turn

public class Information {
	Map<MapLocation,int[]> map = new HashMap<MapLocation,int[]>(); //Static elements, can be updated more slowly? ID: 0
		//int[rubbleCount,PartsCount,DenHealth,NeutralCharacterID,NeutralCharacterHealth]
	Map<MapLocation,RobotInfo> enemyActors = new HashMap<MapLocation,RobotInfo>(); //More urgently needs to be updated ID: 1
		//int[characterID, characterTypeID, characterHealth,characterInfectionCount]
	Map<MapLocation,RobotInfo> friendlyActors = new HashMap<MapLocation,RobotInfo>(); //More urgently needs to be updated ID: 2
	/* 
	 * Character Type IDs:
	 * 		0,Friendly Archon
	 * 		1,Friendly Scout
	 * 		2,Friendly Soldier
	 * 		3,Friendly Guard
	 * 		4,Friendly Viper
	 * 		5,Friendly Turret
	 * 		6,Friendly TTM
	 * 		7,Neutral Archon
	 * 		8,Neutral Scout
	 * 		9,Neutral Soldier
	 * 		10,Neutral Gaurd
	 * 		11,Neutral Viper
	 * 		12,Neutral Turret
	 * 		13,Neutral TTM
	 * 	 	14,Enemy Archon
	 * 		15,Enemy Scout
	 * 		16,Enemy Soldier
	 * 		17,Enemy Gaurd
	 * 		18,Enemy Viper
	 * 		19,Enemy Turret
	 * 		20,Enemy TTM
	 * 		21,Zombie Standard
	 * 		22,Zombie Ranged
	 * 		23,Zombie Fast
	 * 		24,Zombie Big		
	 */
	
	//GETTING BITS: http://stackoverflow.com/questions/9354860/how-to-get-the-value-of-a-bit-at-a-certain-position-from-a-byte
	
	public int encapsInformation(){
		 //Maybe it stores percentage instead of actual health, to truncate, i.e. xxxyyycchhiirrppdd
		return 0;
	}
	
	public void updateInformation(int inputData){ //How jam packed can an integer be?
		//TODO decrypt the message
		//Update the map based on the information
	}
	
	public int[] getInformation(MapLocation loc){ //Pinpoint
		int[] answer= null;
		if (map.containsKey(loc)){
			answer= map.get(loc);
		}
		return answer;
	}
	
	public Vector<int[]> getMap(){
		return null;
	}
	
	public Vector<int[]> getFriendly(){
		return null;
	}
	
	public Vector<int[]> getEnemy(){
		return null;
	}
	
	public boolean test(){
		boolean answer= false;
		
		//Rubble: 434,433,432 | 159
		//Parts: 435 | 157,158,159
		//People: 
		
		return answer;
	}
	
}
