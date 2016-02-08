package team051;

import java.util.LinkedList;
import java.util.Queue;

import battlecode.common.*;

import static team051.Common.*;
import static team051.Move.*;

public class Message {
	
	///////////////////////////////////////////////////////////////////
	// newest common functions
	//
	
	////////////////////////////////////////////////////////////////////
	// signaling stuff
	static final int RUBBLELOC    	=0x0200_0000;
	static final int PARTSLOC     	=0x0300_0000;
	static final int MASTERLOC   	 	=0x0400_0000;
	static final int CREATORLOC   	=0x0500_0000;
	static final int TEAMINFO     	=0x0600_0000;
	static final int HOSTILETARGET	=0x0700_0000;
	static final int INITIALINFO  	=0x0800_0000;
	static final int MAPINFOMESS   	=0x0900_0000;
	static final int MOVETURTLEOUT 	=0x0A00_0000;
	static final int GETOUTLOC    	=0x0B00_0000;
	static final int EXTENTSMESS  	=0x0C00_0000;
	static final int EXTENTSMESSNSYM	=0x0D00_0000;
	static final int INTENDDIR		=0x0E00_0000;
	static final int HOSTILEDIR		=0x0F00_0000;
	static final int ENEMYBASE		=0x1000_0000;
	static final int MOVEOVER			=0x1100_0000;
	static final int MOVETURTLEIN		=0x1200_0000;
	
	
	//directed messages (>=0x3000_0000)
	static final int YOURDESTINATION	=0x3000_0000;
	static final int PROTECTME		=0x3100_0000;
	static final int MOVEYOURBUT		=0x3200_0000;
	static final int SIGHTFORME		=0x3300_0000;
	static final int EXPLOREMAP		=0x3400_0000;
	

	// dest and dist are optional to send an initial destination to the newly created robot
	static int sendInitialSignals(Direction dir, MapLocation dest, int dist) throws GameActionException{
		int newID=rc.senseRobotAtLocation(rc.getLocation().add(dir)).ID;
		
		if(dest!=null){
			sendDestinationMess(dest,dist,newID,2);
			debS+="sent dest:"+dest+" dist:"+dist;
		}
		
		if(rc.getMessageSignalCount()<GameConstants.MESSAGE_SIGNALS_PER_TURN)
			qMessage(MASTERLOC, locToMess(masterLoc), 2,true);
		
		if(rc.getMessageSignalCount()<GameConstants.MESSAGE_SIGNALS_PER_TURN)
			qMessage(CREATORLOC, locToMess(rc.getLocation()), 2,true);
		
		broadcastExtentSignal(2,false);
		
		initialInfo.broadcast(2);
		teamInfo.broadcast(aWays());
		return newID;
	}
	
	static void processSignals() throws GameActionException{
		
		// read messages
		Signal[] s=rc.emptySignalQueue();
		//sInt=s.length;
		
		
		if(rc.getRoundNum()==0 && rc.getType()==RobotType.ARCHON){
			s=staticSigs;
		}
		
		
		// go through signal list 
		for(int i=0;i<s.length;i++){
			
			// process my team messages
			if(s[i].getTeam()==myTeam){
				
				// update masterLoc if applicable
				if(s[i].getID()==masterID){
					masterLoc=s[i].getLocation();
					distanceToMaster=rc.getLocation().distanceSquaredTo(masterLoc);
				}

				
				if(s[i].getMessage()!=null){
					int messID=s[i].getMessage()[0] & 0xFF00_0000;
					
					//get directed messages first
					if(messID>=0x3000_0000){
						if(rc.getID()==((s[i].getMessage()[0]>>8) & 0xFFFF)){
							switch(messID){
								case YOURDESTINATION:
									destination=messToLoc(s[i].getMessage()[1]);
									destDist=(s[i].getMessage()[1] >>24) & 0xFF;
									debS+="dest recvd:"+destination+" dist: "+destDist;
									break;
								
								case PROTECTME:
									debS+=" prtME ";
									protecteeID=s[i].getID();
									protecteeLoc=s[i].getLocation();
									protecteeHB=0;
									rc.broadcastSignal(5);
									
									if(rc.getType()==RobotType.SCOUT){
										Scout.setSctState(Scout.ScoutState.PROTECT);
									}
									break;
									
								case MOVEYOURBUT:
									destination=rc.getLocation().add(Direction.values()[s[i].getMessage()[1]].opposite());
									destDist=3;
									break;
									
								case SIGHTFORME:
									Scout.setSctState(Scout.ScoutState.SIGHT);
									break;
									
								case EXPLOREMAP:
									Scout.setSctState(Scout.ScoutState.EXPLORE);
									staticCw=s[i].getMessage()[1]==1;
									break;
							}
						}
					}
					else{ // universal messages
						
						switch(messID){
							case MASTERLOC:
								masterLoc=messToLoc(s[i].getMessage()[1]);
								distanceToMaster=rc.getLocation().distanceSquaredTo(masterLoc);
								break;
							case CREATORLOC:
								creatorLoc=messToLoc(s[i].getMessage()[1]);
								break;
							case EXTENTSMESS:
								debS+=" recvd extent sig ";
								messToExtents(s[i].getMessage());
								centerOfMap=getCenterOfMap();
								if(!foundAllExtents && (rc.getType()==RobotType.SCOUT || rc.getType()==RobotType.ARCHON)){
									if(senseExtents()){
										// signal that extents were updated
										broadcastExtentSignal(distanceToMaster,false);
									}
								}
								break;
							case EXTENTSMESSNSYM:
								debS+=" NEW SYMMETRY RCVD ";
								messToExtents(s[i].getMessage());
								centerOfMap=getCenterOfMap();
								break;
							case TEAMINFO:
								teamInfo=new TeamInfo(s[i].getMessage());
								break;
							case RUBBLELOC:
								int rubble=s[i].getMessage()[0] & 0xFFFF;
								MapLocation rl=messToLoc(s[i].getMessage()[1]);
								break;
							case PARTSLOC:
								PartInfo p=new PartInfo(s[i].getMessage());
								//if(!partList.contains(p)){
								//	partList.add(p);
								//}
								break;
							
							case HOSTILETARGET:
								MapLocation hm=messToLoc(s[i].getMessage()[1]);
								if(!hostileTargets.contains(hm))
								hostileTargets.add(hm);
								break;
								
							case INITIALINFO:
								initialInfo=new InitialInfo(s[i].getMessage());
								break;
							case MAPINFOMESS:
								MapInfo m=new MapInfo(s[i].getMessage());
								map[m.loc.x-ox][m.loc.y-oy]=m;
								debS+="MAPMESS"+m.loc;
								if(rc.getType()==RobotType.ARCHON){
									if(m.den){
										if(!denList.contains(m.loc)){
											denList.add(m.loc);
										}
									}else{
										int index=denList.indexOf(m.loc);
										if(index != -1)
											denList.remove(index);
									}
									
									if(m.neutralBot){
										if(!neutralList.contains(m.loc)){
											neutralList.add(m.loc);
										}
									}else{
										int index=neutralList.indexOf(m.loc);
										if(index != -1)
											neutralList.remove(index);
									}
									
									if(m.parts>4){
										if(!partsList.contains(m.loc)){
											partsList.add(m.loc);
										}
									}else{
										int index=partsList.indexOf(m.loc);
										if(index != -1)
											partsList.remove(index);
									}
									
									
									if(master && teamInfo.numArchons>1){
										m.broadcast(aWays()); //TODO send proper distance to all archons
										debS+="resent:"+aWays();
									}
								}
								break;
								
							case MOVETURTLEOUT:
								debS+=" MOVEOUT ";
								
								Direction to=rc.getLocation().directionTo(s[i].getLocation());
								
								Direction applies=Direction.values()[s[i].getMessage()[1]];
								if(applies != Direction.OMNI && applies !=to.opposite())//only applies to direction sent (OMNI will get all)
									break;
								
								
								boolean needToMove=true;
								for(int j=0;j<(to.isDiagonal()?3:5);j++){
									Direction candDir=Direction.values()[(to.ordinal()+possibleDirections[0][j]+8)%8];
									MapLocation candLoc=rc.getLocation().add(candDir);
									debS+=" tried:"+candDir;
									if(!rc.isLocationOccupied(candLoc)&&rc.senseRubble(candLoc)<GameConstants.RUBBLE_OBSTRUCTION_THRESH &&
											!getOutList.contains(candLoc)){
										needToMove=false;
										break;
									}
								}
								if(needToMove){
									destination=s[i].getLocation();
									destDist=destination.distanceSquaredTo(rc.getLocation().add(to.opposite()));
									moveOutStartDist=rc.getLocation().distanceSquaredTo(destination);
									timeout=0;
									debS+=" needtomove ";
								}
								break;
								
							case MOVETURTLEIN:
								to=rc.getLocation().directionTo(s[i].getLocation());
								destination=s[i].getLocation();
								destDist=destination.distanceSquaredTo(rc.getLocation().add(to));
								break;
								
							case GETOUTLOC:
								if(s[i].getMessage()[1]==0){
									getOutList.clear();
								}else{
									MapLocation loc=messToLoc(s[i].getMessage()[1]);
									if(!getOutList.contains(loc))
									getOutList.add(loc);
								}
								break;
								
							case INTENDDIR:
								if(s[i].getID()==protecteeID){
									protecteeDir=Direction.values()[s[i].getMessage()[1]];
								}else{
									//moveIsh(Direction.values()[s[i].getMessage()[1]],8,1,true,0);
								}
								break;
							case HOSTILEDIR:
								if(s[i].getID()==protecteeID){
									hostileDir=Direction.values()[s[i].getMessage()[1]];
								}
								break;
								
							case ENEMYBASE:
								enemybase=messToLoc(s[i].getMessage()[1]);
								break;
								
							case MOVEOVER:
								moveOver=true;
								moveOverDir=Direction.values()[s[i].getMessage()[1]];
								break;
						}
					}
				}else{//simple messages
					debS+=" RxBs";
				
					//message for protector from protectee
					if(s[i].getID()==protecteeID){
						protecteeHB=0;
						debS+=" RxPtee";
						if(!protecteeLoc.equals(s[i].getLocation())){
							protecteeDir=protecteeLoc.directionTo(s[i].getLocation());
							protecteeLoc=s[i].getLocation();
						}else
							protecteeDir=Direction.NONE;
					}
					
					//heartbeat message for protectee from protector
					Protector.pListHeartbeat(s[i].getID());
					
				}
				
			}
		}//end for loop
		
		///other message processing stuff
		
		//for protectors (has a protecteeID)
		if(protecteeID!=0){
			if(rc.getRoundNum()%15 ==0){
				if(rc.getBasicSignalCount()<GameConstants.BASIC_SIGNALS_PER_TURN)
					rc.broadcastSignal(aWays());
			}
			protecteeHB++;
			if(protecteeHB>30){
				protecteeID=0;
				protecteeLoc=null;
				protecteeDir=Direction.NONE;
			}
		}
		
		//for protectees (anyone with non-empty protList)
		Protector.pListCheckHeartbeats();
		if(rc.getType()==RobotType.ARCHON || rc.getType()==RobotType.SCOUT){
			if(rc.getRoundNum()%15 ==2 && rc.getBasicSignalCount()<GameConstants.BASIC_SIGNALS_PER_TURN)
				rc.broadcastSignal(aWays());
		}
	}
	
	static Queue<QdMessage> messQ=new LinkedList<>();
	static Queue<QdMessage> distMessQ=new LinkedList<>();
	
	static void sendQdMessages() throws GameActionException{
		debS+=" sendQdMEssages() ";
		if(!inDanger){
			while(rc.getMessageSignalCount()<GameConstants.MESSAGE_SIGNALS_PER_TURN && !distMessQ.isEmpty()){
				QdMessage mess=messQ.poll();
				debS+=" QSD:"+mess.dist;
				rc.broadcastMessageSignal(mess.m1, mess.m2, mess.dist);
			}
		}
		
		while(rc.getMessageSignalCount()<GameConstants.MESSAGE_SIGNALS_PER_TURN && !messQ.isEmpty()){
			QdMessage mess=messQ.poll();
			if(!inDanger || mess.dist<aWays()){
				debS+=" QSnD:"+mess.dist;
				rc.broadcastMessageSignal(mess.m1, mess.m2, mess.dist);
			}
			else
				distMessQ.add(mess);
		}
	}
	
	static void qMessage(int m1, int m2, int distance, boolean sendNowIfPossible) throws GameActionException{
		if(rc.getMessageSignalCount()<GameConstants.MESSAGE_SIGNALS_PER_TURN || !sendNowIfPossible)
			messQ.add(new QdMessage(m1,m2,distance));
		else
			rc.broadcastMessageSignal(m1, m2, distance);
	}
	
	static class QdMessage{
		int m1;
		int m2;
		int dist;
		QdMessage(int m1,int m2,int dist){
			this.m1=m1;this.m2=m2;this.dist=dist;
		}
	}
	
	static void sendDestinationMess(MapLocation dest, int dist, int newID, int rad) throws GameActionException{
		sendIndMess(YOURDESTINATION,newID,locToMess(dest) | ((dist &0xFF)<<24), rad);
	}
	
	static void sendIndMess(int messId, int robotID, int second, int dist) throws GameActionException{
			qMessage(messId | ((robotID & 0xFFFF)<<8), second, dist,true);
	}
	
	
	
	static final int INITPROTSIZE=5;
	static Protector[] protListA=new Protector[INITPROTSIZE];
	static int curProtSize=INITPROTSIZE;
	
	static class Protector{
		int id;
		int heartbeat;
		Protector(int i){
			id=i;
		}
		@Override
		public boolean equals(Object o){
			return ((Protector)o).id==id;
		}
		@Override
		public int hashCode(){
			return id;
		}
		@Override
		public String toString(){
			String s="P:"+id;
			try {
				s+=rc.senseRobot(id).location;
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
			}
			return s;
		}
		boolean isID(int id){
			return this.id==id;
		}
		static int pListContains(int id){
			for(int i=0;i<curProtSize;i++){
				if(protListA[i]!=null && protListA[i].id==id){
					return i;
				}
			}
			return -1;
		}
		
		static void pListAdd(int id){
			for(int i=0;i<curProtSize;i++){
				if(protListA[i]==null){
					protListA[i]=new Protector(id);
					numProt++;
					return;
				}
			}
			Protector.increaseSize(id);
		}
		
		static void increaseSize(int id){
			Protector[] temp=new Protector[curProtSize+INITPROTSIZE];
			System.arraycopy(protListA, 0, temp, 0, curProtSize);
			protListA=temp;
			protListA[curProtSize]=new Protector(id);
			numProt++;
			curProtSize+=INITPROTSIZE;
		}
		
		static void pListRemoveID(int id){
			for(int i=0;i<curProtSize;i++){
				if(protListA[i]!=null && protListA[i].id==id){
					protListA[i]=null;
					break;
				}
			}
		}
		static void pListRemoveIndex(int index){
			if(index!=-1)
				protListA[index]=null;
		}
		static void pListHeartbeat(int id){
			for(int i=0;i<curProtSize;i++){
				if(protListA[i]!=null && protListA[i].id==id){
					protListA[i].heartbeat=0;
					break;
				}
			}
		}
		static void pListCheckHeartbeats(){
			if(numProt==0)
				return;
			for(int i=0;i<curProtSize;i++){
				if(protListA[i]!=null){ 
					protListA[i].heartbeat++;
					if(protListA[i].heartbeat>31){
						pListRemoveIndex(i);
						numProt--;
					}
				}
			}
		}
		static String pListShow(){
			String s="("+curProtSize+")"+numProt;
			for(int i=0;i<curProtSize;i++){
				if(protListA[i]!=null)
					s+=protListA[i];
			}
			return s;
		}
	}
	
	static int maxBroadcastDist(){// about 9 turns for archon, 6 for scout MAX (80x80 map, much less if smaller and known)
		int xs=5184;
		int ys=5184;
		int sub=0;
		if(foundxMax&&foundxMin){
			sub=(int) Math.sqrt(rc.getType().sensorRadiusSquared);
			int xd=(xMax-xMin)-sub;
			xs=xd*xd;
		}
		if(foundyMax&&foundyMin){
			if(sub==0)
				sub=(int) Math.sqrt(rc.getType().sensorRadiusSquared);
			int yd=(yMax-yMin)-sub;
			ys=yd*yd;
		}
		return xs+ys;
	}

}
