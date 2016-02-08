package team051;


import battlecode.common.*;
import static team051.Common.*;
import static team051.Message.*;
import static team051.Move.*;

public class Turret{
	
	static final int guardRad=8;
	static boolean stuck=false;
	static MapLocation masterLoc;

	/////////////////////////////////////////////////////////////
	// turret()
	//
	public static void run() throws GameActionException{
		
		
		// indicator string/////////////
		debS+="hostSensed: ";
		for(RobotInfo r:hostiles)
			debS+=r.location;
		debS+=" hostMessaged: ";
		for(MapLocation r:hostileTargets)
			debS+=r;
		////////////////////////////////
				
		
		// attack if possible
		attack();

		
		if(destination != null){
			//better see if it is safe
			if(hostiles.length==0 && hostileTargets.size()<3){
				rc.pack();
			}
			else{
				destination=null;
			}
			return;
		}
		//just in case is resting on a pile of parts
		if(rc.senseParts(rc.getLocation()) > 4){
			destination=teamInfo.base;
			destDist=20;
			return;
		}
		
		
	}
	
	
	/////////////////////////////////////////////////////////////
	// ttm()
	//
	static void runTTM() throws GameActionException{
		debS+=" moveToDestination:"+destination+" dist:"+destDist;
		
		if(destination!= null && !moveToDestination()){
			destination=null;
			rc.unpack();
		}
	}
	
	
	/////////////////////////////////////////////////////////////
	// moveAway()
	//
	private static boolean moveAway(MapLocation center) throws GameActionException{
		while(!rc.isCoreReady()){
			Clock.yield();
		}
		Direction dir=center.directionTo(rc.getLocation());
		int c=0;
		while(!rc.canMove(dir)){
			dir=dir.rotateRight();
			c++;
			if(c>=4)
				return false;
		}
		rc.move(dir);
		return true;
	}

	
	///////////////////////////////////////////////////////////
	// closestArchonLoc()
	//
	private static MapLocation closestArchonLoc() {
		RobotInfo[] ri=rc.senseNearbyRobots(rc.getLocation(), 15, myTeam);
		MapLocation loc=rc.getLocation();
		int i=0;
		int dist=0;
		int closest=99;
		while(i<ri.length){
			if(ri[i].type==RobotType.ARCHON){
				closest=i;
				break;
			}
			i++;
		}
		if(closest==99){
			return null;
		}
		//found 1
		dist=rc.getLocation().distanceSquaredTo(ri[closest].location);
		int newDist;
		for(i=i+1;i<ri.length;i++){
			if(ri[i].type==RobotType.ARCHON){
				newDist=loc.distanceSquaredTo(ri[i].location);
				if(newDist<dist){
					dist=newDist;
					closest=i;
				}
			}
		}
		return ri[closest].location;
	}
	
	///////////////////////////////////////////////////////////
	// moveClear()
	//
	private static void moveClear() throws GameActionException{
		while(true){
			MapLocation center=closestArchonLoc();
			if(rc.getLocation().distanceSquaredTo(center)>=guardRad)
				break;
			else{
				if(!moveAway(center))
					break;
			}
		}
		rc.unpack();
		Clock.yield();
	}
	
	
	
}
