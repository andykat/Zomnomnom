package TurretTurtle;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;

//MAP ITERATION: http://stackoverflow.com/questions/46898/how-to-efficiently-iterate-over-each-entry-in-a-map
//20 message signals max per turn
//Map location -16000 to 16000; 0~ 


//getInitialArchonLocations().get(0) for seed point, locations sent as difference to that starting value


public class Information { //http://stackoverflow.com/questions/683041/java-how-do-i-use-a-priorityqueue
	//ONLY STORES THINGS THAT ARE MAP RELATED
	//Map borders
	private int minX= Integer.MIN_VALUE;
	private int minY= Integer.MIN_VALUE;
	private int maxX= Integer.MAX_VALUE;
	private int maxY= Integer.MAX_VALUE;
	
	private Map<MapLocation,int[]> map = new HashMap<MapLocation,int[]>(); 
		//int[rubbleCount,PartsCount,DenHealth,NeutralCharacterType]
	private ArrayList<MapLocation> objectives = new ArrayList<MapLocation>(); 

	public int getNumRecordedCorners(){
		 int answer= 0;
		 if (minX!= Integer.MIN_VALUE){
			 answer++;
		 }
		 
		 if (minY!= Integer.MIN_VALUE){
			 answer++;
		 }
		 
		 if (maxX!= Integer.MAX_VALUE){
			 answer++;
		 }
		 
		 if (maxY!= Integer.MAX_VALUE){
			 answer++;
		 }
		 return answer;
	}
	
	public int getNumObjectives(){
		return objectives.size();
	}
	
	public void clearObjectives(){ //Being called in the class when it's time to give up (usually when the highest value is not > 0
		objectives.clear();
	}
	
	
	public float getMapLocValue(MapLocation objLoc, RobotController rc){ //Returns the value of a given position 
		//TODO if worth it, take time to sense around the given location for overall value
		///int[rubbleCount,PartsCount,DenHealth,NeutralCharacterType]
		
		float value= 0;
		
		if (objLoc!= null)
		if (map.containsKey(objLoc)){
				int[] info = map.get(objLoc);
				value= -info[0]/100; //Every 100 rubble is -1 valuable
				value+= info[1];
	
				//Zombie dens worth more earlier, worth a lot less later (more likely to do)
				//Lower the health of the den, more worthy the goal
				//value+= (250-rc.getRoundNum())*(GameConstants.DEN_PART_REWARD)+ ((RobotType.ZOMBIEDEN.maxHealth-info[2])/RobotType.ZOMBIEDEN.maxHealth) * GameConstants.DEN_PART_REWARD ;
				if (info[3]> 0 && RobotConstants.posNRobotTypes[info[3]]!= null){
					value+= RobotConstants.posNRobotTypes[info[3]].partCost*2;
					if (RobotConstants.posNRobotTypes[info[3]].equals(RobotType.ARCHON)){
						value+= 1000;
					}
					rc.setIndicatorString(1, "Robot int type: "+ info[3]);
					//rc.setIndicatorString(2, "Nuetral robot value added?" + rc.getRoundNum());
				}		
			value-= (Math.sqrt(rc.getLocation().distanceSquaredTo(objLoc)));
		}
		return value;
	}
	
	public void clearObjective(MapLocation loc){
		if (objectives.contains(loc)){
			objectives.remove(loc);
		}
	}
	
	public MapLocation getObjective(RobotController rc){ //Returns the next most valuable objective to get to
		MapLocation answer= null;
		if (objectives.size()> 0){
			float bestVal= getMapLocValue(objectives.get(0),rc);
			answer= objectives.get(0);
			for (int n = 1; n < objectives.size(); n++){
				float candidateVal= getMapLocValue(objectives.get(n), rc);
				if (candidateVal > bestVal){
					candidateVal= bestVal;
					answer= objectives.get(n);
				}
			}
		}
		return answer;
	}
	
	private void updateObjectives(MapLocation loc){
		if (objectives.contains(loc)){
			objectives.remove(loc);
			objectives.add(loc);
		}else{
			objectives.add(loc);
		}
	}

	public int getMapLongestDimension(){ //Returns -1 on not having all corners
		int answer= 0;	
		if (getNumRecordedCorners()== 4){//If you have all corners
			int w= maxX- minX;
			int l= maxY- minY;
			if (w> l){
				answer= w;
			}else{ //For cases of equality for if l is longer, idea the same
				answer= l;
			}
		}
		return answer;
	}
	
	public int[] getCorners(){ //Can return even though the corners have yet to be found
		int[] answer= {minX,minY,maxX,maxY};
		return answer;
	}
	
	public void addCornerValueCandidate(Direction dir, MapLocation loc){ //You add the direction of your check and the corresponding map location when the map first comes on grid
		if (dir.equals(Direction.NORTH)){
			if (loc.y > minY)
				minY= loc.y;
		}else if (dir.equals(Direction.EAST)){
			if (loc.x < maxX)
				maxX= loc.x;
		}else if (dir.equals(Direction.SOUTH)){
			if (loc.y < maxY)
				maxY= loc.y;
		}else if (dir.equals(Direction.WEST)){
			if (loc.x > minX)
				minX= loc.x;
		}
	}
	
	public int getRobotIntType(RobotType rt){
		int answer= -1;
		for (int n= 1; n< RobotConstants.posNRobotTypes.length; n++){
			if (RobotConstants.posNRobotTypes[n].equals(rt)){
				answer= n;
				break;
			}
		}
		return answer;
	}
	
	public int getBrainSize(){
		return map.size();
	}

	
	public void addNeutralRobotMapInfo(MapLocation loc, RobotInfo ri){//Adding individual values
		if (ri== null){
			addInfo(loc, 3, getRobotIntType(null));
		}else if (ri.team.equals(Team.NEUTRAL)){
			addInfo(loc, 3, getRobotIntType(ri.type));
		}
		updateObjectives(loc);
	}
	
	public void addMapInfo(MapLocation loc, int value, RobotConstants.mapTypes mapType){//Adding individual values
		if (mapType.equals(RobotConstants.mapTypes.RUBBLE)){
			addInfo(loc,0, value);
		}else if (mapType.equals(RobotConstants.mapTypes.PARTS)){
			addInfo(loc,1, value);
		}else if (mapType.equals(RobotConstants.mapTypes.ZOMBIE_DEN)){
			addInfo(loc,2,value);
		}
		updateObjectives(loc);
	}
	
	private void addInfo(MapLocation key, int index, int value){ //Adding individual values
		if (map.containsKey(key)){
			map.get(key)[index]= value;
		}else{
			int[] info= new int[4];
			info[index]= value;
			map.put(key, info);
		}
		updateObjectives(key);
	}
	
	public void addMapInfo(MapLocation loc, int rubbleCount, int partsCount, int denHealth,RobotInfo ri){
		int[] info= {rubbleCount, partsCount, denHealth, getRobotIntType(ri.type)};
		map.put(loc, info); //Old value will get replaced
		updateObjectives(loc);
	}
	
	public int[] getInformation(MapLocation loc){
		int[] answer= null;
		if (map.containsKey(loc)){
			answer= map.get(loc);
		}
		return answer;
	}
	
	public void printInfo(){
		for (Entry<MapLocation, int[]> entry : map.entrySet()){
		    System.out.println("\t"+entry.getKey() + "/" + Arrays.toString(entry.getValue()));
		}
	}
}
