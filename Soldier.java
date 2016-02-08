package team051;

import battlecode.common.*;
import static team051.Common.*;
import static team051.Message.*;
import static team051.Move.*;


public class Soldier{
	
	static int underAttack; 
	static Direction lastUnderAttackDir=null;
	
	public static void run() throws GameActionException{
		
		if(lostHealth || underAttack>0){
			if(lostHealth)
				underAttack=10;
			else
				underAttack--;
			
			debS+=" underATTACK";
			RobotInfo ri=null;
			for(int i=0;i<hostiles.length;i++){
				if(rc.getLocation().distanceSquaredTo(hostiles[i].location) <= hostiles[i].type.attackRadiusSquared){
					ri=hostiles[i];
					break;
				}
			}
			if(ri==null){//don't know where shot could have come from
				if(hostileTargets.isEmpty()){
					debS+=" dirUnknown";
					if(lastUnderAttackDir==null){
						if(lastMoveDir!=null){
							lastUnderAttackDir=lastMoveDir;
						}else
							lastUnderAttackDir=Direction.NORTH;//TODO ???
						
					}
				}else{//not empty hostileTargets
					lastUnderAttackDir=rc.getLocation().directionTo(closestLoc(hostileTargets));
					
				}
				debS+=" trytomove:"+
				moveIsh(lastUnderAttackDir.opposite(),3,0,true,0);
				
				
			}else{
				attack();
			}
		}else{
			lastUnderAttackDir=null;
		}
		
		// just attack if possible
		attack();
		
		
		if(hostileLoc!=null){
			if(rc.isWeaponReady() && rc.canAttackLocation(hostileLoc)){
				rc.attackLocation(hostileLoc);
			}
		}
		
		if(protecteeID!=0){
			if(rc.getLocation().distanceSquaredTo(protecteeLoc)>6)
				moveIsh(rc.getLocation().directionTo(protecteeLoc),5,0,true,0);
			
			
		}else{
			RobotInfo[] friends=rc.senseNearbyRobots(-1, myTeam);
			for(int i=0;i<friends.length;i++){
				if(friends[i].type==RobotType.ARCHON){
					protecteeID=friends[i].ID;
					protecteeLoc=friends[i].location;
					protecteeHB=0;
					rc.broadcastSignal(5);
					break;
				}
			}
			if(protecteeID==0){
				destination=masterLoc;
				destDist=50;
			}
		}
		
		
		
		if(protecteeDir!=Direction.NONE){
			moveIsh(protecteeDir,5,0,true,0);
		}
		
		
		if(destination!=null && !moveToDestination()){
			destination=null;
		}
		
		
	}

}
