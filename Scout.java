package team051;


import java.util.*;


import battlecode.common.*;
import static team051.Common.*;
import static team051.Message.*;
import static team051.Move.*;


public class Scout{
	
	
	static enum ScoutState{
		START,
		NORMAL,
		PROTECT,
		EVADE,
		BAIT,
		EXPLORE,
		FINDEXTENTS,
		SIGHT
	}
	static ScoutState sctState=ScoutState.START;
	static ScoutState oldSctState=ScoutState.NORMAL;
	
	
	

	public static void run() throws GameActionException{
		
		
		
		while(true){//int the switch block use continue to re-execute the switch block.
			debS+=" sst:"+sctState;
			switch(sctState){
				case START:
					scout(sLoc);
					
					setSctState(ScoutState.NORMAL);
					continue;
					
					
					//break;
		
				case NORMAL://normal
		
					setSctState(ScoutState.EXPLORE);
					continue;
		
					//break;
		
				case SIGHT:
					if(destination !=null && !moveToDestination())
						destination=null;
		
					spotTargets();
					
					break;
		
				case EVADE://evade
					boolean clear=true;
					for(int i=0;i<hostiles.length;i++){
						if(rc.getLocation().distanceSquaredTo(hostiles[i].location)<=hostiles[i].type.attackRadiusSquared+15){
							clear=false;
							break;
						}
					}
					
					if(clear){
						revertSctState();
					}else{
						sortOfEvade(hostiles);
					}
					break;
					
				case EXPLORE:
					for(int i=0;i<hostiles.length;i++){
						if(rc.getLocation().distanceSquaredTo(hostiles[i].location)<=hostiles[i].type.attackRadiusSquared*2){
							setSctState(ScoutState.EVADE);
							evade(hostiles[i],hostiles);
							continue;
						}
					}
					
					if(!foundAllExtents){
						setSctState(ScoutState.FINDEXTENTS);
						continue;
					}
					
					explore();
					
					break;
					
				case FINDEXTENTS:
					
					if(findExt()){
						revertSctState();
						continue;
					}
					
					break;
					
				case BAIT:
					
					break;
					
				case PROTECT:
					if(protecteeID==0)
						revertSctState();
					
					spotTargets();
					if(rc.getLocation().distanceSquaredTo(protecteeLoc)>6)
						moveIsh(rc.getLocation().directionTo(protecteeLoc),5,0,true,0);
					break;
		
			}
			
			break;//while
		}
		
		
		//always execute final statements
		
		if(!rc.getLocation().equals(sLoc)){
			scout(sLoc);
		}
		
	}

	static void setSctState(ScoutState st){
		oldSctState=sctState;
		sctState=st;
	}
	static void revertSctState(){
		sctState=oldSctState;
	}
	
	
	private static boolean findExt() throws GameActionException {
		explore();
		return foundAllExtents;
	}
	
	static void scout(MapLocation oldLoc) throws GameActionException{
		debS+=" scout() ";
		Queue<MapInfo> list=new LinkedList<>();
		
		MapLocation[] parts=rc.sensePartLocations(-1);
		for(int i=0;i<parts.length;i++){
			if(map[parts[i].x-ox][parts[i].y-oy]==null){
				MapInfo temp=new MapInfo(parts[i],(int)rc.senseRubble(parts[i]),(int)rc.senseParts(parts[i]));
				map[parts[i].x-ox][parts[i].y-oy]=temp;
				list.add(temp);
			}
		}
		
		RobotInfo[] zombies=rc.senseNearbyRobots(-1, Team.ZOMBIE);
		for(int i=0;i<zombies.length;i++){
			if(zombies[i].type==RobotType.ZOMBIEDEN){
				if(map[zombies[i].location.x-ox][zombies[i].location.y-oy]==null){
					MapInfo temp=new MapInfo(zombies[i].location,0,0,false,true);
					map[zombies[i].location.x-ox][zombies[i].location.y-oy]=temp;
					list.add(temp);
				}
			}
		}
		
		RobotInfo[] neutrals=rc.senseNearbyRobots(-1, Team.NEUTRAL);
		for(int i=0;i<neutrals.length;i++){
			if(map[neutrals[i].location.x-ox][neutrals[i].location.y-oy]==null){
				MapInfo temp=new MapInfo(neutrals[i].location,0,0,true,false);
				map[neutrals[i].location.x-ox][neutrals[i].location.y-oy]=temp;
				list.add(temp);
			}
		}

		
		while(!list.isEmpty()){
			MapInfo temp=list.poll();
			
			if(symmetryType<3){//see if can find symmetry
				//try rot sym
				int candxind=((int)(2*centerx)- temp.loc.x  )-ox;
				int candyind=((int)(2*centery)- temp.loc.y  )-oy;
				MapInfo cand=map[candxind][candyind];
				if(cand!=null && (cand.den!=temp.den || cand.parts!=temp.parts || cand.neutralBot!=temp.neutralBot)){
					symmetryType=XMIRROR;
				}
				
				if(symmetryType==XMIRRORORROT){
					//try xref sym (y coord the same)
					candyind=temp.loc.y-oy;
					cand=map[candxind][candyind];
					if(cand!=null && (cand.den!=temp.den || cand.parts!=temp.parts || cand.neutralBot!=temp.neutralBot)){
						symmetryType=ROTATION;
					}
				}
				if(symmetryType==YMIRRORORROT){
					//try yref sym (x coord the same)
					candxind=temp.loc.x-ox;
					cand=map[candxind][candyind];
					if(cand!=null && (cand.den!=temp.den || cand.parts!=temp.parts || cand.neutralBot!=temp.neutralBot)){
						symmetryType=ROTATION;
					}
					
				}
				
				if(symmetryType>=3){//found
					extFromSym();
					broadcastExtentSignal(maxBroadcastDist(),true);
					debS+=" NEW SYMMETRY SENT ";
				}
			}
			
			temp.broadcast(distanceToMaster);
			debS+=" enQ:"+temp+" dist:"+distanceToMaster;
		}
		
		
		
		if(!foundAllExtents){
			if(senseExtents() && rc.getMessageSignalCount()<GameConstants.MESSAGE_SIGNALS_PER_TURN){
				broadcastExtentSignal(masterLoc.distanceSquaredTo(rc.getLocation()),false);
			}
		}
		
		checkEnemy();
	}
	
	static void checkEnemy() throws GameActionException{
		RobotInfo[] enemy=rc.senseNearbyRobots(-1, otherTeam);
		
		int numTur=0;
		int numArch=0;
		int avgTurx=0;
		int avgTury=0;
		int avgArchx=0;
		int avgArchy=0;
		for(int i=0;i<enemy.length;i++){
			if(enemy[i].type==RobotType.TURRET || enemy[i].type==RobotType.TTM){
				numTur++;
				avgTurx+=enemy[i].location.x;
				avgTury+=enemy[i].location.y;
			}else if(enemy[i].type==RobotType.ARCHON){
				numArch++;
				avgArchx+=enemy[i].location.x;
				avgArchy+=enemy[i].location.y;
			}
		}
		if(numTur>3 && numTur>lastKnownEnemyBaseTurrets){
			lastKnownEnemyBaseTurrets=numTur;
			MapLocation temp=new MapLocation(avgTurx/numTur,avgTury/numTur);
			if(enemybase.distanceSquaredTo(temp)>70){
				enemybase=temp;
				qMessage(ENEMYBASE,locToMess(enemybase),maxBroadcastDist(),true);
			}
		}else if(numArch>1){
			MapLocation temp=new MapLocation(avgArchx/numArch,avgArchy/numArch);
			if(enemybase.distanceSquaredTo(temp)>70){
				enemybase=temp;
				qMessage(ENEMYBASE,locToMess(enemybase),maxBroadcastDist(),true);
			}
		}
	}
	
	static int updateMap(Direction dir) throws GameActionException {
		MapLocation[] newLocs;
		if(dir==null){
			debS+=" updatingALLmaploc";
			newLocs=MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), rc.getType().sensorRadiusSquared);
			MapLocation[] temp=new MapLocation[200];
			int j=0,i=0;
			for(i=0;i<newLocs.length;i++){
				if(!(newLocs[i].x>xMax || newLocs[i].x<xMin || newLocs[i].y>yMax || newLocs[i].y<yMin)){
					temp[j++]=newLocs[i];
				}
			}
			if(j<i){
				System.arraycopy(temp, 0, newLocs, 0, j);
			}
		}else
			debS+=" updatingSOMEmaploc";
			newLocs=NewLocs.getNewLocs(dir, rc.getLocation());
		
		for(int i=0;i<newLocs.length;i++){
			MapInfo mi=map[newLocs[i].x-ox][newLocs[i].y-oy];
			if(mi==null){
				mi=new MapInfo(newLocs[i],(int)rc.senseRubble(newLocs[i]));
			}else{
				if(mi.rubble>=100){//check if has been reduced
					mi.rubble=(int)rc.senseRubble(newLocs[i]);
				}
			}
		}
		return newLocs.length;
	}

	static void evade(RobotInfo imdThreat, RobotInfo[] hostiles) throws GameActionException{
		debS+=" evade ";
		if(!rc.isCoreReady())
			return;
		
		
		
	}
	
	static void sortOfEvade(RobotInfo[] hostiles) throws GameActionException{
		debS+=" sortofevade ";
		if(!rc.isCoreReady())
			return;
		
		MapLocation closest=getAvgLoc(hostiles);
		
		Direction desired=rc.getLocation().directionTo(enemybase);
		if(isDirAwayFrom(desired,closest)){
			debS+=" move";
			debS+="?"+desired;
			moveIsh(desired,5,0,true,2);
		}else{
			debS+=" circle group ";
			circle(closest, rc.getLocation().distanceSquaredTo(closest)+8, circleAwayFromEdge(closest), 5, 2);
		}
	}
	
	
	static MapLocation getAvgLoc(RobotInfo[] robots) {
		int sx=0,sy=0;
		for(int i=0;i<robots.length;i++){
			if(robots[i].type != RobotType.ZOMBIEDEN){
				sx+=robots[i].location.x;
				sy+=robots[i].location.y;
			}
		}
		MapLocation avgLoc=new MapLocation(sx/robots.length, sy/robots.length);
		debS+=" avgloc: "+avgLoc;
		return avgLoc;
	}

	static boolean circleAwayFromEdge(MapLocation loc) {
		int distToEdge=distToEdge(rc.getLocation());
		MapLocation newLoc=rc.getLocation().add(loc.directionTo(rc.getLocation()).rotateRight().rotateRight());//rotate clockwise (true)
		int newDist=distToEdge(newLoc);
		if(newDist > distToEdge)
			return true;
		return false;
	}

	static boolean isDirAwayFrom(Direction dir, MapLocation loc) {
		Direction opp=dir.opposite();
		Direction toLoc=rc.getLocation().directionTo(loc);
		for(int i=0;i<5;i++){
			int j=possibleDirections[0][i];
			if((opp.ordinal()+j)%8 == toLoc.ordinal()){
				return true;
			}
		}
		return false;
	}

	static MapLocation closest(RobotInfo[] robots){
		int closestDist=9999;
		MapLocation closestLoc=null;
		for(int i=0;i<robots.length;i++){
			int d=robots[i].location.distanceSquaredTo(rc.getLocation());
			if(d < closestDist){
				closestDist=d;
				closestLoc=robots[i].location;
			}
		}
		if(closestLoc!=null)
			return closestLoc;
		return null;
	}
	
	static Direction rotateToward(Direction dir, Direction goToward) {
		if(dir.opposite()==goToward)
			return staticCw? dir.rotateRight() : dir.rotateLeft();
		if(dir==goToward)
			return dir;
		for(int i=1;i<4;i++){
			if((dir.ordinal()+i)%8 == goToward.ordinal())
				return dir.rotateRight();
		}
		return dir.rotateLeft();
	}
	

	static Direction circle(MapLocation center, int rad, boolean cw, int thresh, int histNum) throws GameActionException{
		Direction around=cw? center.directionTo(rc.getLocation()).rotateRight().rotateRight() : center.directionTo(rc.getLocation()).rotateLeft().rotateLeft();
		
		debS+=" (circle: cnt: "+center+" rad:"+rad+" cw:"+cw+" around:"+around+")";
		
		if(center.distanceSquaredTo(rc.getLocation().add(around)) > rad+thresh){
			debS+="move toward";
			around=cw? around.rotateRight() : around.rotateLeft();
		}else if(center.distanceSquaredTo(rc.getLocation().add(around)) < rad-thresh){
			debS+="move away";
			around=cw? around.rotateLeft() : around.rotateRight();
		}
		return moveIsh(around,8,cw?1:2,true,histNum);
	}
	
//	static Direction circleIsh(Direction dir, boolean cw, boolean careHistory) throws GameActionException { // like movish but if hits wall goes the right way
//		
//		
//		if(!rc.isCoreReady())
//			return Direction.NONE;
//		
//		
//		int c=0;
//		//debS+=" "+locHistory+" ";
//		while(!rc.canMove(dir) || (careHistory && locHistory.contains(rc.getLocation().add(dir)))){
//			debS+="canmove:"+rc.canMove(dir)+" rotate ";
//			dir=cw? dir.rotateRight() : dir.rotateLeft();
//			c++;
//			if(c==8)
//				return Direction.OMNI;
//		}
//		
////		if(!willGetShot(dir))
//			rc.move(dir);
////		else
////			setState(ScoutState.EVADE);
//		
//		return dir;	
//	}
	
	
	///////////////////////////////////////////////////
	// explore()
	//
	static int exploredDist;
	static boolean[] dirIsExplored=new boolean[8];
	static Direction limitDir;
	static Direction dirLastBump;
	
	static int expState=0;
	
	public static void explore() throws GameActionException{
		
		MapLocation center =masterLoc;
		Direction awayFromCenter=center.directionTo(rc.getLocation());
		int distToCenter=center.distanceSquaredTo(rc.getLocation());
		int distToEdge=distToEdge(rc.getLocation());
		
		int rootSenseRad=(int) Math.sqrt(rc.getType().sensorRadiusSquared);
		int distShouldBe=(exploredDist+rootSenseRad)*(exploredDist+rootSenseRad);
		//distShouldBe=distShouldBe*distShouldBe;
		debS+="cw: "+staticCw+"explored: "+exploredDist*exploredDist+" distToCenter: "+distToCenter+" shouldBe: "+distShouldBe;
		
		
		while(true){
			switch(expState){
			case 0://set dist
				if(center.distanceSquaredTo(rc.getLocation()) <= rc.getType().sensorRadiusSquared){
					exploredDist=rootSenseRad;
					
				} else{
					moveIsh(awayFromCenter.opposite(),8,0,true,5);
					return;
				}
				limitDir=center.directionTo(rc.getLocation());
				expState=1;
				break;
				
			case 1://moving out to distShouldBe
				if(distToCenter< distShouldBe-2){
					moveIsh(awayFromCenter,8,staticCw?1:2,true,5);
					debS+=" moving out ";
					return;
				}			
				expState=2;
				limitDir=awayFromCenter;
				break;
				
			case 2://circling
				debS+=" circling ";
				if(distToEdge<9){
					int newDistToEdge=distToEdge(rc.getLocation().add(staticCw? awayFromCenter.rotateRight().rotateRight() 
							: awayFromCenter.rotateLeft().rotateLeft()));
					debS+=" olddisttoedge: "+distToEdge+" new: "+newDistToEdge;
					if(newDistToEdge<distToEdge){//circling toward edge, expand distance and change direction
						debS+=" increase ";
						exploredDist=(int) (Math.sqrt(distToCenter)+rootSenseRad);
						distShouldBe=(exploredDist+rootSenseRad)*(exploredDist+rootSenseRad);
						dirIsExplored=new boolean[8];
						staticCw=!staticCw;
						expState=1;//move out
						break;
					}
					
				}
				
				
				circle(startLoc,distShouldBe,staticCw,exploredDist+rootSenseRad,4);
				dirIsExplored[center.directionTo(rc.getLocation()).ordinal()]=true;
				
				if(center.directionTo(rc.getLocation())==limitDir){
					boolean finishedDist=true;
					for(int i=0;i<8;i++){
						if(!dirIsExplored[i]){
							finishedDist=false;
							break;
						}
					}
					if(finishedDist){
						exploredDist=(int) (Math.sqrt(distToCenter)+rootSenseRad);
						dirIsExplored=new boolean[8];
						expState=1;//move out
					}
				}
				return;
			
			
			}
		}
	}
	
	static int distToEdge(MapLocation loc){
		int x=loc.x;
		int y=loc.y;
		int dx=Math.min(Math.abs(x-xMax), Math.abs(x-xMin));
		int dy=Math.min(Math.abs(y-yMax), Math.abs(y-yMin));
		return Math.min(dx*dx, dy*dy);
	}
	

	static Direction dirToEdge(){
		int x=rc.getLocation().x;
		int y=rc.getLocation().y;
		int dxMax=xMax-x;
		int dxMin=x-xMin;
		int dyMax=yMax-y;
		int dyMin=y-yMin;
		if(dxMax<dxMin){
			if(dyMax<dyMin){
				if(dxMax<dyMax){
					return Direction.EAST;
				} else{
					return Direction.SOUTH;
				}
			}else{
				if(dxMax<dyMin){
					return Direction.EAST;
				}else{
					return Direction.NORTH;
				}
				
			}
		}else{
			if(dyMax<dyMin){
				if(dxMin<dyMax){
					return Direction.WEST;
				}else{
					return Direction.SOUTH;
				}
			}else{
				if(dxMin<dyMin){
					return Direction.WEST;
				}else{
					return Direction.NORTH;
				}
			}
		}
	}
	
	public static void bait() throws GameActionException{
		RobotInfo[] ri=rc.senseNearbyRobots(-1, Team.ZOMBIE);
		MapLocation loc;
		if(ri.length>0){
			Arrays.sort(ri, new MasterRangeComparator());
			for(int i=0;i<ri.length;i++){
				if(ri[i].location.distanceSquaredTo(rc.getLocation()) > ri[i].location.distanceSquaredTo(masterLoc)){
					Direction dir=rc.getLocation().directionTo(ri[i].location);
					dir=moveIsh(dir,8,0,false,5);
					loc=rc.getLocation().add(dir);
					Boolean ok=true;
					for(RobotInfo j:ri){
						if(j.location.distanceSquaredTo(loc) <= j.type.attackRadiusSquared){
								ok=false;
								break;
						}
					}
					while(!ok){
						dir=dir.rotateRight();
						loc=rc.getLocation().add(dir);
						ok=true;
						for(RobotInfo j:ri){
							if(j.location.distanceSquaredTo(loc) <= j.type.attackRadiusSquared){
									ok=false;
									break;
							}
						}
					}
					rc.setIndicatorString(0, "moving toward: "+ri[i].location);
					if(rc.canMove(dir))
						rc.move(dir);
					break;
				}
			}
			Arrays.sort(ri, new RangeComparator());
			for(RobotInfo r:ri){
				if(r.location.distanceSquaredTo(rc.getLocation()) <= r.type.attackRadiusSquared){
					rc.setIndicatorString(0, "moving away from: "+r.location);
					Direction dir=moveIsh(r.location.directionTo(rc.getLocation()),8,0,false,5);
					if(rc.canMove(dir)){
						rc.move(dir);
						break;
					}
				}
			}
		}// if zombies in range
		else{
		
			int distToMaster=rc.getLocation().distanceSquaredTo(masterLoc);
			if(distToMaster<25){
				if(rc.isCoreReady()){
					Direction dir=Direction.SOUTH_EAST;
					rc.setIndicatorString(0, "moving southeast");
					dir=moveIsh(dir,8,0,false,5);
					if(rc.canMove(dir))
						rc.move(dir);
				}
			} else if(distToMaster>30){
				if(rc.isCoreReady()){
					Direction dir=rc.getLocation().directionTo(masterLoc);
					rc.setIndicatorString(0, "moving toward master");
					dir=moveIsh(dir,8,0,false,5);
					if(rc.canMove(dir))
						rc.move(dir);
				}
			}
		}
	}
	
	static Direction towardCenterOfMap(){
		MapLocation center=new MapLocation((int)centerx,(int)centery);
		return rc.getLocation().directionTo(center);
	}
	
	
	
}
