package danielsrobot;

import java.util.Comparator;
import battlecode.common.MapLocation;

public class OriginMapLocComparator implements Comparator<MapLocation>{//For priority queue favoring locations closer towards 'home'
	private MapLocation base;
	OriginMapLocComparator(MapLocation base){
		this.base= base;
	}
	
	public void setBase(MapLocation newBase){ //Doubt will ever need to change, but just in case
		this.base= newBase;
	}
	
	@Override
	public int compare(MapLocation loc1, MapLocation loc2) {
		int answer= 0;
		if (base.distanceSquaredTo(loc1) > base.distanceSquaredTo(loc2))
			answer= 1;
		else if (base.distanceSquaredTo(loc1)< base.distanceSquaredTo(loc2))
			answer= -1;
		return answer;
	}

}
