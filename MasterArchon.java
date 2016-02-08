package team051;

import battlecode.common.*;
import static team051.Common.*;
import static team051.Message.*;
import static team051.Move.*;

public class MasterArchon {

	static int n=1;
	static int waitMoveSig;
	static RobotType builtLast;
	static Direction getOutDir=Direction.EAST;
	static boolean startTurtle;
	static int getOutTimer;
	static int getOutDist=2;
	static int numDir;
	static Direction lastScoutDir;
	static int numSightScouts;
	static int numGuardSoldiers;
	
	static int numExplorers;
	static boolean buildExplorerNext;
	static boolean buildSightScoutNext	;
	
	static int wait;
	static int moveIn;
	
	public static void run() throws GameActionException {	
		debS+="Master, GOdist:"+getOutDist;
		

		spotTargets();
		
		wait--;
		
		if(rc.getRoundNum()>800 && wait<=0 && hostiles.length==0 && hostileTargets.size()==0){
			qMessage(MOVEOVER,Direction.SOUTH_EAST.ordinal(),aWays(),true);
			moveOver=true;
			moveOverDir=Direction.SOUTH_EAST;
			moveIn=3;
			wait=40;
		}
		
		if(moveIn>0){
			qMessage(MOVETURTLEIN,0,aWays(),true);
			moveIn--;
		}
		
		
		if(destination!=null){
			moveToDestination();
		}
		
		if(startTurtle){
			
			getOutDir=Scout.dirToEdge();
			System.out.println("dirfromedge:"+getOutDir);
			for(int i=0;i<8;i++){
				Direction candDir=Direction.values()[(possibleDirections[0][i]+getOutDir.ordinal()+8)%8];
				System.out.println("try:"+candDir);
				boolean clear=true;
				MapLocation loc=rc.getLocation().add(candDir);
				while(rc.getLocation().distanceSquaredTo(loc.add(candDir))<=rc.getType().sensorRadiusSquared){
					loc=loc.add(candDir);
					System.out.println("loc:"+loc+" rub:"+rc.senseRubble(loc));
					if(rc.senseRubble(loc)>=GameConstants.RUBBLE_OBSTRUCTION_THRESH){
						clear=false;
						break;
					}
				}
				if(clear){
					getOutDir=candDir;
					break;
				}
			}
			System.out.println("end getOutDir:"+getOutDir);
			
			lastScoutDir=getOutDir;
			
			getOutList.clear();
			getOutList.add(rc.getLocation());
			getOutList.add(rc.getLocation().add(getOutDir));
			getOutTimer=1;
			
			startTurtle=false;
		}
		
		waitMoveSig++;
		
		
		RobotType buildType=buildNext();
		Direction buildDir=lastScoutDir;
		if(buildExplorerNext){
			buildDir=getOutDir;
		}
		int bid=buildRobot(buildDir,buildType);
		if(bid != 0){
			builtLast=buildType;
			debS+=" built:"+buildType;
			
			if(buildType==RobotType.SCOUT){
				if(buildSightScoutNext){
					lastScoutDir=rc.getLocation().directionTo(rc.senseRobot(bid).location).rotateLeft().rotateLeft();
					numSightScouts++;
					sendIndMess(SIGHTFORME,bid,0,2);
					buildSightScoutNext=false;
				}else if(buildExplorerNext){
					sendIndMess(EXPLOREMAP,bid,staticCw?1:2,2);
					staticCw=!staticCw;
					buildExplorerNext=false;
					numExplorers++;
				}
			}
			
			if(buildType==RobotType.SOLDIER){
				numGuardSoldiers++;
			}
			
			sendGetOutMess(getOutDist);
			numDir=1;
			if(allBlocked()){
				sendMoveSig(findBestDirection(),numDir);
			}
			
		}else{
			if(rc.isCoreReady() && rc.hasBuildRequirements(buildType)){
				if(waitMoveSig>=20){
					numDir++;
					sendMoveSig(findBestDirection(),numDir);
					waitMoveSig=0;
				}
			}
		}
		
		clearNearby();
	}
	
	
	
	
	private static RobotType buildNext() {
		if(buildExplorerNext || buildSightScoutNext)
			return RobotType.SCOUT;
		
		if(numExplorers<2 && builtLast!=RobotType.SCOUT){
			buildExplorerNext=true;
			return RobotType.SCOUT;
		}
		
		if(numSightScouts<4 && builtLast!=RobotType.SCOUT){
			buildSightScoutNext=true;
			return RobotType.SCOUT;
			
		}
		
		if(numGuardSoldiers<1){
			return RobotType.SOLDIER;
		}
		
		return RobotType.TURRET;
	}

	static void sendGetOutMess(int dist) throws GameActionException {
		for(int i=0;i<getOutList.size();i++){
			qMessage(GETOUTLOC, locToMess(getOutList.get(i)), dist,false);
		}
		
	}

	static boolean allBlocked() throws GameActionException{
		MapLocation[] m=MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), 2);
		boolean allBlocked=true;
		for(int i=0;i<m.length;i++){
			if(!rc.isLocationOccupied(m[i]) && rc.senseRubble(m[i])<GameConstants.RUBBLE_OBSTRUCTION_THRESH && rc.onTheMap(m[i])
					&& !getOutList.contains(m[i])){
				allBlocked=false;
				debS+=" notBlocked:"+m[i];
				break;
			}
		}
		return allBlocked;
	}
	
	static boolean rev=false;
	static Direction lastBest;
	static DirAndDist findBestDirection() throws GameActionException {
		if(rc.getTeamParts()>400)
			return new DirAndDist(Direction.OMNI,aWays());
		
		boolean rev=false;
		int oldRad=2;
		MapLocation testLoc=rc.getLocation().add(Direction.NORTH);
		int rad=Math.max(2,rc.getLocation().distanceSquaredTo(testLoc));
		while(rad<=rc.getType().sensorRadiusSquared && Clock.getBytecodeNum()<15000){
			MapLocation[] m=MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), rad);
			
			if(rev){
				for(int i=0;i<m.length;i++)
					if(!rc.isLocationOccupied(m[i]) && rc.senseRubble(m[i])<GameConstants.RUBBLE_OBSTRUCTION_THRESH && rc.onTheMap(m[i])
							&& !getOutList.contains(m[i])){
						if(!canGetTo(m[i])){
							debS+=" can't get to:"+m[i];
							System.out.println("can't get to "+m[i]);
							continue;
						}
						rev=!rev;
						return new DirAndDist(rc.getLocation().directionTo(m[i]), oldRad);
					}
			}else{
				for(int i=m.length-1;i>=0;i--)
					if(!rc.isLocationOccupied(m[i]) && rc.senseRubble(m[i])<GameConstants.RUBBLE_OBSTRUCTION_THRESH && rc.onTheMap(m[i])
							&& !getOutList.contains(m[i])){
						if(!canGetTo(m[i])){
							debS+=" can't get to:"+m[i];
							System.out.println("can't get to "+m[i]);
							continue;
						}
						rev=!rev;
						return new DirAndDist(rc.getLocation().directionTo(m[i]), oldRad);
					}
			
			}
			
			testLoc=testLoc.add(Direction.NORTH);
			oldRad=rad;
			rad=rc.getLocation().distanceSquaredTo(testLoc);
			
			if(rad>getOutDist){
				getOutDist=rad;
				//update getAwayList
				System.out.println("updategolist:"+getOutDist);
				MapLocation loc=getOutList.get(getOutList.size()-1);
				while(rc.getLocation().distanceSquaredTo(loc)<getOutDist){
					System.out.println("loc: "+loc);
					boolean found=false;
					for(int i=0;i<7;i++){
						Direction candDir=Direction.values()[(possibleDirections[0][i]+getOutDir.ordinal()+8)%8];
						System.out.println("tried: "+candDir);
						MapLocation candLoc=loc.add(candDir);
						if(!rc.canSense(candLoc)){
							loc=loc.add(candDir);
							getOutList.add(candLoc);
							found=true;
							break;
						}
						if(!rc.isLocationOccupied(candLoc) && rc.senseRubble(candLoc)<100 && rc.onTheMap(candLoc) && !getOutList.contains(candLoc)){
							loc=loc.add(candDir);
							getOutList.add(candLoc);
							found=true;
							break;
						}
					}
					if(!found){
						System.out.println("could not find getOutPath "+getOutDist);
						break;
					}
				}
				
			}
		}
		
		return new DirAndDist(Direction.values()[(int)(Math.random()*8)],aWays());
	}



	static boolean canGetTo(MapLocation dest) throws GameActionException {
		MapLocation m=rc.getLocation();
		while(true){
			if(m.equals(dest))
				return true;
			Direction d=m.directionTo(dest);
			boolean blocked=true;
			for(int i=0;i<3;i++){
				Direction cand=Direction.values()[(possibleDirections[0][i]+d.ordinal()+8)%8];
				MapLocation candLoc=m.add(cand);
				if(!rc.canSense(candLoc)){
					return rc.onTheMap(candLoc);
				}
				if(rc.senseRubble(candLoc) < GameConstants.RUBBLE_OBSTRUCTION_THRESH && rc.onTheMap(candLoc)){
					m=m.add(cand);
					blocked=false;
					break;
				}
			}
			if(blocked)
				return false;
		}
	}


	static void sendMoveSig(DirAndDist dd, int num) throws GameActionException {
		if(dd.dir==Direction.OMNI){
			qMessage(MOVETURTLEOUT,dd.dir.ordinal(),dd.dist,true);
		}
		for(int i=0;i<num&&i<8;i++){
			int dind=(possibleDirections[0][i]+dd.dir.ordinal()+8)%8;
			qMessage(MOVETURTLEOUT,dind,dd.dist,false);
			debS+=" sentMove:"+Direction.values()[dind]+","+dd.dist;
		}		
	}

	static void sendMoveSigInd(int id, Direction dir, int dist) throws GameActionException {
		sendIndMess(MOVEYOURBUT, id, dir.ordinal(), dist);
	}

	static void initialRun(int farthest) throws GameActionException{
		figureExtents();
		System.out.println(" sent extents sig "+farthest);
		broadcastExtentSignal(farthest,false);
		
		
		startTurtle=true;
		
		
		countZombies();
		
		// look for dens
		hostiles=rc.senseHostileRobots(rc.getLocation(), -1);
		for(int i=0;i<hostiles.length;i++){
			if(hostiles[i].type==RobotType.ZOMBIEDEN){
				denList.add(hostiles[i].location);
			}
		}
		
		if(denList.size()>0){
			
		}
	}
	
	static void countZombies() {
		ZombieSpawnSchedule zss=rc.getZombieSpawnSchedule();
		int[] zrounds=zss.getRounds();
		if(zrounds.length>0){
			if(zrounds[0]<27){//super early zombie spawn
				earlySpawn=true;
			}
			
			
			int big=0;
			int total=0;
			ZombieCount[] zc = null;
			for(int i=0;i<zrounds.length;i++){
				if(zrounds[i]>2100)
					break;
				//System.out.println("zombie round "+zrounds[i]);
				zc=zss.getScheduleForRound(zrounds[i]);
				for(int j=0;j<zc.length;j++){
					//System.out.println("\t"+zc[j].getType()+": "+zc[j].getCount());
					total+=zc[j].getCount();
					if(zc[j].getType()==RobotType.BIGZOMBIE)
						big+=zc[j].getCount();
				}
				//System.out.println();
			}
			
			
			zombieStrength=1;
			if(total>164 || big>14){
				zombieStrength=2;
				if(total>208 || big>16){
					zombieStrength=3;
				}
			}
			//if(zrounds.length>0)
				//System.out.println("                       zombies!!!! total:"+total+
			//" big:"+big+" early:"+earlySpawn+" superEarly:"+superEarlySpawn+" first:"+zrounds[0]+"\n\n");
		}

	}

	static class DirAndDist{
		Direction dir;
		int dist;
		DirAndDist(Direction d,int di){
			dir=d;dist=di;
		}
	}
}
