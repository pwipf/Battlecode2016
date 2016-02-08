package team051;


import battlecode.common.*;
import static team051.Common.*;
import static team051.Message.*;
import static team051.Move.*;





public class Archon{
	
	
	static enum ArchState{
		START,
		NORMAL,
		PROTECT,
		EVADE,
		BAIT,
		EXPLORE,
		FINDEXTENTS,
		SIGHT,
		GATHERSTUFF
	}
	static ArchState archState=ArchState.START;
	static ArchState oldArchState=ArchState.NORMAL;
	
	static int underAttack;
	
	public static void run() throws GameActionException{
		
		makeProtWait--;
		
		//activate any adjacent neutrals
		RobotInfo[] nearNeutrals=rc.senseNearbyRobots(-1, Team.NEUTRAL);
		for(int i=0;i<nearNeutrals.length;i++){
			if(rc.isCoreReady() && nearNeutrals[i].location.distanceSquaredTo(rc.getLocation())<=2){
				rc.activate(nearNeutrals[i].location);
				sendInitialSignals(rc.getLocation().directionTo(nearNeutrals[i].location),teamInfo.base,20);
				int index=neutralList.indexOf(nearNeutrals[i].location);
				if(index!=-1){
					neutralList.remove(index);
					map[nearNeutrals[i].location.x-ox][nearNeutrals[i].location.y-oy].neutralBot=false;
					map[nearNeutrals[i].location.x-ox][nearNeutrals[i].location.y-oy].broadcast(aWays()); //TODO make distance get to all archons
				}
				return;
			}
			
		}
		
		if(lostHealth || underAttack>0){
			if(lostHealth)
				underAttack=10;
			else
				underAttack--;
			
			setArchState(ArchState.EVADE);
		}
		
		
		while(true){//int the switch block use continue to re-execute the switch block.
			debS+=" sst:"+archState;

			

			
			switch(archState){
				case START:
					archScout();
					setArchState(ArchState.NORMAL);
					continue;
					
					
					//break;
		
				case NORMAL://normal
					
					makeProtectors();
					
					if(!partsList.isEmpty() && !neutralList.isEmpty()){
						setArchState(ArchState.GATHERSTUFF);
						continue;
					}
							
					if(!allExplored && rc.getRoundNum()<1000){
						setArchState(ArchState.EXPLORE);
						continue;
					}
					
					if(rc.getLocation().distanceSquaredTo(masterLoc) <= RobotType.ARCHON.sensorRadiusSquared){
						setArchState(ArchState.SIGHT);
						continue;
					}
		
					break;
					
				case GATHERSTUFF:
					makeProtectors();
					if(!gatherStuff()){
						setArchState(ArchState.NORMAL);
						continue;
					}
					break;
		
				case SIGHT:
					if(destination !=null){
						if(!moveToDestination()){
							destination=null;
						}
					}
					
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
					
					if(clear && underAttack<=0){
						setArchState(ArchState.NORMAL);
					}else{
						//sortOfEvade(hostiles);
						if(hostiles.length>0)
							qMessage(HOSTILEDIR,rc.getLocation().directionTo(closestLoc(hostiles)).ordinal(),aWays(),true);
						
						moveToward(masterLoc);
					}
					break;
					
				case EXPLORE:
					makeProtectors();
					
					for(int i=0;i<hostiles.length;i++){
						if(rc.getLocation().distanceSquaredTo(hostiles[i].location)<=hostiles[i].type.attackRadiusSquared*2){
							setArchState(ArchState.EVADE);
							//evade(hostiles[i],hostiles);
							continue;
						}
					}
					
					
					if(!partsList.isEmpty() || !neutralList.isEmpty()){
						setArchState(ArchState.GATHERSTUFF);
						continue;
					}
					//TODO this is not right
					
					if(rc.getRoundNum()>1000){
						destination=masterLoc;
						destDist=10;
						setArchState(ArchState.SIGHT);
					}
					
					Scout.explore();
					
					
					break;
					
				case BAIT:
					
					break;
					
				case PROTECT:
					
					break;
		
			}
			
			break;//while
		}

		
		if(!rc.getLocation().equals(sLoc)){
			rc.broadcastSignal(aWays());
		}
		
		
		
		clearNearby();
		
		if(!rc.getLocation().equals(sLoc)){
			archScout();
		}
		
		
	}
		
		
		
		
		



	static boolean gatherStuff() throws GameActionException{

		//TODO better priority for parts/neutrals
		MapLocation closestGoal=closestLoc(partsList,neutralList);
		debS+="closestGoal:"+closestGoal;
		if(closestGoal!=null){
			moveToParts(closestGoal);
		}
		else
			return false;
		
		if(partsList.isEmpty() && neutralList.isEmpty())
			return false;
		
		return true;
		
	}


		
		
		
		
		
	static void archScout(){	
		//have a look at the area in case not explored TODO maybe at end of turn if enough bytecodes remaining, as good chance it is already reported	
		int bcs=Clock.getBytecodeNum();
		//look for parts		
		MapLocation[] nearParts=rc.sensePartLocations(-1);
		for(int i=0;i<nearParts.length;i++){
			if(map[nearParts[i].x-ox][nearParts[i].y-oy]==null){
				partsList.add(nearParts[i]);
				map[nearParts[i].x-ox][nearParts[i].y-oy]=new MapInfo(nearParts[i],(int)rc.senseRubble(nearParts[i]),(int)rc.senseParts(nearParts[i]));
			}
		}
		
		//look for neutrals
		RobotInfo[] nearNeutrals=rc.senseNearbyRobots(-1, Team.NEUTRAL);
		for(int i=0;i<nearNeutrals.length;i++){
			if(map[nearNeutrals[i].location.x-ox][nearNeutrals[i].location.y-oy]==null){
				neutralList.add(nearNeutrals[i].location);
				map[nearNeutrals[i].location.x-ox][nearNeutrals[i].location.y-oy]=
						new MapInfo(nearNeutrals[i].location,(int)rc.senseRubble(nearNeutrals[i].location),(int)rc.senseParts(nearNeutrals[i].location),true);
			}
		}
		
		
		//look for dens
		RobotInfo[] nearZombies=rc.senseNearbyRobots(-1, Team.ZOMBIE);
		for(int i=0;i<nearZombies.length;i++){
			if(nearZombies[i].type==RobotType.ZOMBIEDEN && map[nearZombies[i].location.x-ox][nearZombies[i].location.y-oy]==null){
				denList.add(nearZombies[i].location);
				map[nearZombies[i].location.x-ox][nearZombies[i].location.y-oy]=new MapInfo(nearZombies[i].location,0,0,false,true);
			}
		}
		
		rc.setIndicatorString(1, "SENSEarea: "+(Clock.getBytecodeNum()-bcs)+" location:"+rc.getLocation());
		////////////// end have a look around

		
	}
	
	
	static void setArchState(ArchState st){
		oldArchState=archState;
		archState=st;
	}
	static void revertArchState(){
		archState=oldArchState;
	}
		
	//////////////////////////////////////////////
	// electMaster()
	//
	// also does some initial (first round) archon stuff
	static void electMaster() throws GameActionException{
		staticSigs=rc.emptySignalQueue();
		MapLocation[] initial=rc.getInitialArchonLocations(myTeam);
		teamInfo.numArchons=initial.length;
		int farthest=farthest(initial,rc.getLocation());
		rc.broadcastSignal(farthest);
		
		int count=0;
		for(int i=0;i<staticSigs.length;i++){
			if(staticSigs[i].getTeam()==myTeam){
				if(masterID==0){
					masterID=staticSigs[i].getID();
					masterLoc=staticSigs[i].getLocation();
					distanceToMaster=rc.getLocation().distanceSquaredTo(masterLoc);
				}
				count++;
			}
		}
		archonN=count;
		if(count==0){
			master=true;
			masterLoc=rc.getLocation();
			MasterArchon.initialRun(farthest);
		}
		
		teamInfo.base=masterLoc;
		initialInfo=new InitialInfo(masterLoc);
		System.out.println("ox,oy "+ox+","+oy);
	}
		

	static void moveTowardMaster() throws GameActionException{
		if(rc.isCoreReady()){
			Direction d=rc.getLocation().directionTo(masterLoc);
			d=moveIsh(d,8,0,true,5);
		}		
	}
	
	static void moveAwayMaster() throws GameActionException{
		if(rc.isCoreReady()){
			Direction d=masterLoc.directionTo(rc.getLocation());
			d=moveIsh(d,8,0,true,5);
		}		
	}
	
	static void moveToParts(MapLocation loc) throws GameActionException{
		if(rc.isCoreReady()){
			Direction d=rc.getLocation().directionTo(loc);
			if(moveIsh(d,5,0,false,0)==Direction.OMNI && rc.senseRubble(rc.getLocation().add(d))>GameConstants.RUBBLE_OBSTRUCTION_THRESH){
				if(rc.isCoreReady())
					rc.clearRubble(d);
			}
			moveIsh(d,7,0,true,16);
//			d=moveIsh(d,8,0,false,5);
//			if(d==Direction.OMNI || rc.senseRubble(rc.getLocation().add(d))>=50){
//				rc.clearRubble(d);
//				return;
//			}else{
//				rc.move(d);
//			}
			if(rc.getLocation().equals(loc)){
				map[loc.x-ox][loc.y-oy].parts=0;
				map[loc.x-ox][loc.y-oy].broadcast(aWays()); //TODO make distance get to all archons
				partsList.remove(loc);
				neutralList.remove(loc);
			}
			
		}		
	}
	
	static int makeProtWait;
	static void makeProtectors() throws GameActionException{
		
		if(numProt<1 && makeProtWait<=0){
			int id=buildRobot(Direction.values()[(int)(Math.random()*8)],RobotType.SCOUT);
			if(id != 0){
				Protector.pListAdd(id);
				//protList.add(new Protector(id));
				sendIndMess(PROTECTME,id,0,9);
				makeProtWait=RobotType.SCOUT.buildTurns+5;
			}
		}
//		if(numProt<3 && makeProtWait<=0){
//			int id=buildRobot(Direction.values()[(int)(Math.random()*8)],RobotType.SOLDIER);
//			if(id != 0){
//				Protector.pListAdd(id);
//				//protList.add(new Protector(id));
//				sendIndMess(PROTECTME,id,0,9);
//				makeProtWait=RobotType.SOLDIER.buildTurns+5;
//			}
//		}
	}
	
	
}
