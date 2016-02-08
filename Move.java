package team051;

import battlecode.common.*;
import static team051.Common.*;
import static team051.Message.*;

public class Move {
	
	static Direction lastMoveDir=null;
	static boolean  moveOver;
	static Direction moveOverDir;

	
	
	/////////////////////////////
	// moveIsh()
	//
	// dir: desired direction
	// numDir: number of directions to try (including dir) (1 means only try dir, 8 means try all)
	// type = 0 normal, 1 circle clockwise, 2 circle ccw
	// move: move or just return direction
	// history: number of history to avoid
	/**
	 * 
	 * @param dir
	 * @param numDir
	 * @param type
	 * @param move
	 * @param numHist
	 * @return
	 * @throws GameActionException
	 */
	
	static Direction moveIsh(Direction dir, int numDir, int type, boolean move, int numHist) throws GameActionException {
		if(!rc.isCoreReady())
			return null;
		
		main:
		for(int i=0;i<numDir;i++){
			int j=possibleDirections[type][i];
			Direction candDir=Direction.values()[(dir.ordinal()+j+8)%8];
			MapLocation candLoc=rc.getLocation().add(candDir);
			
			int lastHist=locHistory.size()-1;
			if(numHist!=0 && lastHist>=0){
				for(int k=lastHist; k>=0 && k>lastHist-numHist; k--){
					if(locHistory.get(k).equals(candLoc)){
						continue main;
					}
				}
			}
			
			if(getOutList.contains(candLoc))
				continue;
			
			if(rc.canMove(candDir)){
				
				if(move){
					if(rc.getType().canClearRubble() && rc.getType() != RobotType.SCOUT){
						int r=(int) rc.senseRubble(rc.getLocation().add(candDir));
						if(r>49&&r<=62 &&rc.getType().canClearRubble()){
							rc.clearRubble(candDir);
							return Direction.NONE;
						}
					}
					rc.move(candDir);
					lastMoveDir=candDir;
					patience=0;
					distanceToMaster=rc.getLocation().distanceSquaredTo(masterLoc);
				}
				return candDir;
			}
		}
		
		patience++;
		if(patience>=5){
			patience=0;
			locHistory.clear();
		}
		
		return Direction.OMNI;
	}
	
	
	////////////////////////////////////
	// moveToDestination
	//
	// return false if reached destination
	static int timeout;
	static boolean moveToDestination() throws GameActionException{
		
		if(destDist==0){//exact spot specified
			if(rc.getLocation().equals(destination))
				return false;
		}
		
		if(rc.getType()==RobotType.ARCHON){
			qMessage(INTENDDIR,rc.getLocation().directionTo(destination).ordinal(),aWays(),true);
		}
		
		
		int curDist=rc.getLocation().distanceSquaredTo(destination);
		
		if(rc.getLocation().add(rc.getLocation().directionTo(destination)).distanceSquaredTo(destination)>=destDist){
			if(rc.isCoreReady()){
				moveToward(destination);
				if(rc.getLocation().distanceSquaredTo(destination)<curDist){
					timeout=0;
				}else{
					timeout++;
					if(timeout>=10)
						return false;
				}
			}
			return true;
		}else if(rc.getLocation().add(destination.directionTo(rc.getLocation())).distanceSquaredTo(destination)<=destDist){
			if(rc.isCoreReady()){
				moveAwayFrom(destination);
				if(rc.getLocation().distanceSquaredTo(destination)>curDist){
					timeout=0;
				}else{
					timeout++;
					if(timeout>=10)
						return false;
				}
			}
			return true;
		}
		return false;
	}
	
	static void moveToward(MapLocation dest) throws GameActionException{
		moveIsh(rc.getLocation().directionTo(dest),8,1,true,10);
	}
	static void moveAwayFrom(MapLocation dest) throws GameActionException{
		moveIsh(dest.directionTo(rc.getLocation()),5,3,true,10);
	}

	static boolean willGetShot(Direction dir){
		MapLocation potLoc=rc.getLocation().add(dir);
		for(int i=0;i<hostiles.length;i++){
			if(potLoc.distanceSquaredTo(hostiles[i].location)<=hostiles[i].type.attackRadiusSquared && hostiles[i].weaponDelay<=2.0){
				return true;
			}
		}
		return false;
	}
	
	static void moveOverIfNecessary() throws GameActionException{
		if(!moveOver)
			return;
		if(rc.getType()==RobotType.TURRET){
			rc.pack();
			return;
		}
		Direction d=moveIsh(moveOverDir,3,0,true,5);
		if(d!=null && d!=Direction.NONE && d!=Direction.OMNI){
			moveOver=false;
			if(rc.getType()==RobotType.TTM)
				rc.unpack();
		}
		
	}

}
