package communicatingPlayer;
import battlecode.common.*;
import communicatingPlayer.RobotConstants.mapTypes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

//MAP ITERATION: http://stackoverflow.com/questions/46898/how-to-efficiently-iterate-over-each-entry-in-a-map
//20 message signals max per turn
//Map location -16000 to 16000; 0~ 


//getInitialArchonLocations().get(0) for seed point, locations sent as difference to that starting value


public class Information {
	//Map borders
	private int minX= Integer.MIN_VALUE;
	private int minY= Integer.MIN_VALUE;
	private int maxX= Integer.MAX_VALUE;
	private int maxY= Integer.MAX_VALUE;
	private int[] edgeCheckY= {-1,-1,0,1,1,1,0,-1};
	private int[] edgeCheckX= {0,1,1,1,0,-1,-1,-1};
	
	private Map<MapLocation,int[]> map = new HashMap<MapLocation,int[]>(); 
		//int[rubbleCount,PartsCount,DenHealth,NeutralCharacterType]
	private Queue<int[]> downloadQueue= new LinkedList<int[]>();
	
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
	
	public int[] getCorners(){ //Return null if quest incomplete
		if (getNumRecordedCorners()==4){
			int[] answer= {minX,minY,maxX,maxY};
			return answer;
		}else{
			return null;
		}
	}
	
	public void addCornerValueCandidate(Direction dir, MapLocation loc){
		for (int n= 0; n< RobotConstants.directions.length; n++){
			if (RobotConstants.directions[n].equals(dir)){
				int testX= loc.x + edgeCheckX[n];
				int testY= loc.y + edgeCheckY[n];
				if (n== 0){
					if (minY < testY){
						minY= testY;
					}
				}else if (n== 1){
					if (minY < testY){
						minY= testY;
					}
					if (maxX > testX){
						maxX= testX;
					}
				}else if (n== 2){
					if (maxX > testX){
						maxX= testX;
					}
				}else if (n== 3){
					if (maxY > testY){
						maxY= testY;
					}
					if (maxX > testX){
						maxX= testX;
					}
				}else if (n== 4){
					if (maxY > testY){
						maxY= testY;
					}
				}else if (n== 5){
					if (maxY > testY){
						maxY= testY;
					}
					if (minX < testX){
						maxX= testX;
					}
				}else if (n== 6){
					if (minX < testX){
						minX= testX;
					}
				}else if (n== 7){
					if (minY < testY){
						minY= testY;
					}
					if (minX < testX){
						minX= testX;
					}
				}
			}
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
	
	public void addMapInfo(MapLocation loc, RobotInfo ri){
		addInfo(loc, 3, getRobotIntType(ri.type));
	}
	
	public void addMapInfo(MapLocation loc, int value, RobotConstants.mapTypes mapType){
		if (mapType.equals(RobotConstants.mapTypes.RUBBLE)){
			addInfo(loc,0, value);
		}else if (mapType.equals(RobotConstants.mapTypes.PARTS)){
			addInfo(loc,1, value);
		}else if (mapType.equals(RobotConstants.mapTypes.ZOMBIE_DEN)){
			addInfo(loc,2,value);
		}
	}
	
	private void addInfo(MapLocation key, int index, int value){
		if (map.containsKey(key)){
			map.get(key)[index]= value;
		}else{
			int[] info= new int[4];
			info[index]= value;
			map.put(key, info);
		}
	}
	
	public void addMapInfo(MapLocation loc, int rubbleCount, int partsCount, int denHealth,RobotInfo ri){
		int[] info= {rubbleCount, partsCount, denHealth, getRobotIntType(ri.type)};
		map.put(loc, info); //Old value will get replaced
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
	
	public void createQueue(){
		for (Entry<MapLocation, int[]> entry : map.entrySet()){
			//entry.getKey() 
			//entry.getValue()
			//Create message
		}
	}
	
}
