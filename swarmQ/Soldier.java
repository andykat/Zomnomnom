package swarmQ;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.RobotController;

public class Soldier extends RobotRunner{
	private enum strat{MEANDER,OBJECTIVE,COMBAT,KITE};
	private strat curStrat;
	private int stepsN = 6;
	private boolean moving;
	private MapLocation dest;
	public Soldier(RobotController rcin){
		super(rcin);
		curStrat = strat.MEANDER;
	}
	public void run() throws GameActionException{
		if (rc.isCoreReady()){
			if(curStrat == strat.MEANDER){
				soldierMeander();
			}
			else if(curStrat == strat.OBJECTIVE){
				soldierObjective();
			}
			else if(curStrat == strat.COMBAT){
				soldierCombat();
			}
			else if(curStrat == strat.KITE){
				soldierKite();
			}
		}
	}
	
	public void soldierMeander(){
		
	}
	public void soldierObjective(){
		
	}
	public void soldierCombat(){
	
	}
	public void soldierKite(){
	
	}
	
}
