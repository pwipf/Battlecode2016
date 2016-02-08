package team051;

import java.util.*;

import battlecode.common.*;

import static team051.Common.hostileTargets;
import static team051.Common.hostiles;
import static team051.Common.rc;
import static team051.Message.*;
import static team051.Move.*;


public class Common {
	
	static Team myTeam,otherTeam;
	static RobotController rc;
	
	static boolean master;
	
	static int archonN;
	static boolean staticCw=true;
	static int sInt;
	static String debS="";
	
	static final int UNKNOWN=0;
	static final int XMIRRORORROT=1;
	static final int YMIRRORORROT=2; // if < 3 partially unknown
	static final int ROTATION=3;
	static final int XMIRROR=4;
	static final int YMIRROR=5;
	static int symmetryType; 
	static boolean couldBeXMirror=true;
	static boolean couldBeYMirror=true;
	static boolean couldBeRotation=false;
	
	static TeamInfo teamInfo=new TeamInfo(0,0,0,null);
	static InitialInfo initialInfo;
	
	static ArrayList<MapLocation> hostileTargets=new ArrayList<>();
	static boolean spotter;
	
	static double centerx,centery;
	
	static final int ATTACKLOC=1;
	static final int SIGHTGUNS=5;
	static final int EXPLORE=6;
	static final int BAIT=7;
	static final int OTHERMASTERLOC=10;
	static final int XMAX=11;
	static final int XMIN=12;
	static final int YMAX=13;
	static final int YMIN=14;
	
	
	static MapLocation masterLoc;
	static MapLocation creatorLoc;
	static MapLocation centerLoc;
	static MapLocation startLoc;
	
	
	
	static MapLocation enemybase;
	static int lastKnownEnemyBaseTurrets;
	
	
	static MapLocation centerOfMap;
	static int masterID;
	static int distanceToMaster;
	
	
	static MapLocation[] otherArchonInitLocs;
	static MapLocation[]    myArchonInitLocs;
	
	//possibleDirections, 	0: try alternating, 
	//						1: try cw, 
	//						2: try ccw 
	//						3: alternating but prefer angle to forward
	static int[][] possibleDirections=new int[][]{{0,1,-1,2,-2,3,-3,4},//normal
		                                            {0,1,2,3,4,5,6,7},//clockwise
		                                            {0,-1,-2,-3,-4,-5,-6,-7},//ccw
		                                            {1,-1,0,2,-2,3,-3,4}}; //forward first but pref angles
	static int histSize=20;
	static ArrayList<MapLocation> locHistory=new ArrayList<>(histSize);
	static ArrayList<MapLocation> getOutList=new ArrayList<>(10);
	static int patience;
	static RobotInfo[] hostiles,friendlies,neutrals,others;
	static int myState=0;
	
	static MapInfo[][] map = new MapInfo[200][200];
	static MapLocation oldLoc;
	static int ox,oy;
	static MapLocation destination;
	static int destDist;
	
	static ArrayList<MapLocation> partsList = new ArrayList<>();
	//static ArrayList<MapLocation> partsIgnore = new ArrayList<>();
	static ArrayList<MapLocation> denList = new ArrayList<>();
	static ArrayList<MapLocation> neutralList = new ArrayList<>();
	
	static int xMax=-16000,xMin=16000,yMax=-16000,yMin=16000,xSize=0,ySize=0;
	
	static Direction primeDir=null;
	
	static boolean foundxMax,foundxMin,foundyMax,foundyMin,foundAllExtents;
	
	
	static Direction directions[]={Direction.NORTH,Direction.NORTH_EAST,Direction.EAST,Direction.SOUTH_EAST,
		Direction.SOUTH,Direction.SOUTH_WEST,Direction.WEST,Direction.NORTH_WEST};
	
	static Signal[] staticSigs;
	static boolean firstMove=true;
	
	static int moveOutStartDist;
	
	
	//protector
	static int protecteeID; //
	static int protecteeHB;
	static MapLocation protecteeLoc;
	static Direction protecteeDir=Direction.NONE;
	static Direction hostileDir=Direction.NONE;
	static MapLocation hostileLoc;
	//static ArrayList<Protector> protList=new ArrayList<>();
	static int numProt;
	
	static MapLocation sLoc;

	//zombie spawn info
	static int zombieStrength;
	static boolean earlySpawn;
	
	static boolean inDanger;
	
	static boolean allExplored;
	
	static double lastRoundHealth;
	static boolean lostHealth;
	
	
	static int buildRobot(Direction dir, RobotType type) throws GameActionException{
		return buildRobot(dir,type,null,0);
	}
	
	static int buildRobot(Direction dir, RobotType type, MapLocation init, int initDist) throws GameActionException{
		if(type==RobotType.TTM)
			type=RobotType.TURRET;
		if(!rc.isCoreReady() || !rc.hasBuildRequirements(type))
			return 0;
		
		Direction startdir=dir;
		if(startdir==null)
			startdir=Direction.values()[(int) (Math.random() * 8)];
		
		for(int i=0;i<8;i++){
			Direction cand=Direction.values()[(possibleDirections[0][i]+startdir.ordinal()+8)%8];
			if((startdir==null || !getOutList.contains(rc.getLocation().add(cand))) && rc.canBuild(cand, type)){
				
				//build robot and update information
				rc.build(cand, type);
				
				if(type==RobotType.SCOUT)
					teamInfo.numScouts++;
				
				return sendInitialSignals(cand,init,initDist); //TODO can add destination for created robots
				
			}
		}
		
		//TODO should probably signal or do something if all directions are blocked (here)
		
		return 0;
	}
	
	
	static void spotTargets() throws GameActionException{
		if(hostiles.length>0){
			Arrays.sort(hostiles, new MasterRangeComparator());
			debS+="sightHostile:";
			
			// first signal any biggies
			for(int i=0;i<hostiles.length;i++){
				if(hostiles[i].type==RobotType.BIGZOMBIE && rc.getMessageSignalCount()<GameConstants.MESSAGE_SIGNALS_PER_TURN){
					rc.broadcastMessageSignal(HOSTILETARGET, locToMess(hostiles[i].location), aWays());
					debS+="B"+hostiles[i].location+" ";
					hostiles[i]=null;
				}
			}
			
			// then the rest
			for(int i=0;i<hostiles.length;i++){
				if(hostiles[i]!=null && rc.getMessageSignalCount()<GameConstants.MESSAGE_SIGNALS_PER_TURN){
					rc.broadcastMessageSignal(HOSTILETARGET, locToMess(hostiles[i].location), aWays());
					debS+=hostiles[i].location+" ";
				}
			}
		}
	}
	
	
	static void attack() throws GameActionException{
		if(rc.isWeaponReady())
			if(!attackTarget())
				attackClosestHostile();
	}
	
	static boolean attackClosestHostile() throws GameActionException{
		if(hostiles.length==0)
			return false;
		
		
		int closestDist=9999;
		MapLocation closestLoc=null;
		for(int i=0;i<hostiles.length;i++){
			if(hostiles[i].type!=RobotType.BIGZOMBIE)
				continue;
			MapLocation hLoc=hostiles[i].location;
			int d=rc.getLocation().distanceSquaredTo(hLoc);
			if(rc.canAttackLocation(hLoc) && d<closestDist){
				closestLoc=hLoc;
				closestDist=d;
			}
		}
		if(closestLoc!=null){
			rc.attackLocation(closestLoc);
			return true;
		}
		
		
		closestDist=9999;
		closestLoc=null;
		for(int i=0;i<hostiles.length;i++){
			MapLocation hLoc=hostiles[i].location;
			int d=rc.getLocation().distanceSquaredTo(hLoc);
			if(rc.canAttackLocation(hLoc) && d<closestDist){
				closestLoc=hLoc;
				closestDist=d;
			}
		}
		if(closestLoc!=null){
			rc.attackLocation(closestLoc);
			return true;
		}
		return false;
	}
	
	static boolean attackTarget() throws GameActionException{
		
		for(MapLocation hLoc:hostileTargets){
			if(rc.canAttackLocation(hLoc)){
				if(rc.canSense(hLoc) && !rc.isLocationOccupied(hLoc)){
					//find enemy within radius 2 of that location
					for(int j=0;j<hostiles.length;j++){
						if(hLoc.distanceSquaredTo(hostiles[j].location)<=2 && rc.canAttackLocation(hostiles[j].location)){
							rc.attackLocation(hostiles[j].location);
							return true;
						}
					}
					break;
				}
				else{
					rc.attackLocation(hLoc);
					return true;
				}
			}
		}
		
		return false;
	}
	
	static MapLocation closestLoc(MapLocation[] list){
		int closestDist=9999;
		MapLocation closestLoc=null;
		for(int i=0;i<list.length;i++){
			int d=list[i].distanceSquaredTo(rc.getLocation());
			if(d < closestDist){
				closestDist=d;
				closestLoc=list[i];
			}
		}
		return closestLoc;
	}
	
	static MapLocation closestLoc(RobotInfo[] list){
		int closestDist=9999;
		MapLocation closestLoc=null;
		for(int i=0;i<list.length;i++){
			int d=list[i].location.distanceSquaredTo(rc.getLocation());
			if(d < closestDist){
				closestDist=d;
				closestLoc=list[i].location;
			}
		}
		return closestLoc;
	}
	
	static MapLocation closestLoc(ArrayList<MapLocation> list){
		int closestDist=9999;
		MapLocation closestLoc=null;
		for(int i=0;i<list.size();i++){
			MapLocation loc=list.get(i);
			int d=loc.distanceSquaredTo(rc.getLocation());
			if(d < closestDist){
				closestDist=d;
				closestLoc=loc;
			}
		}
		return closestLoc;
	}
	
	static MapLocation closestLoc(ArrayList<MapLocation> parts,ArrayList<MapLocation> neutrals){
		int closestDist=9999;
		MapLocation closestLoc=null;
		for(int i=0;i<parts.size();i++){
			MapLocation loc=parts.get(i);
			if(map[loc.x-ox][loc.y-oy].rubble>=100)
				continue;
			int d=loc.distanceSquaredTo(rc.getLocation());
			if(d < closestDist){
				closestDist=d;
				closestLoc=loc;
			}
		}
		
		for(int i=0;i<neutrals.size();i++){
			MapLocation loc=neutrals.get(i);
			int d=loc.distanceSquaredTo(rc.getLocation());
			if(d < closestDist){
				closestDist=d;
				closestLoc=loc;
			}
		}
		
		
		if(closestLoc==null){
		// if nothing else found look for parts w least rubble
		// TODO find out how hard to get these parts, as in other rubble surrounding, this may include scouting a bit when getting near
		// TODO probably same for neutrals
			int bestRubble=9000000;
			for(int i=0;i<parts.size();i++){
				MapLocation loc=parts.get(i);
				int rubble=map[loc.x-ox][loc.y-oy].rubble;
				if(rubble<bestRubble){
					bestRubble=rubble;
					closestLoc=loc;
				}
			}
		}
		
		return closestLoc;
	}

	
	static int aWays(){
		return (int) (rc.getType().sensorRadiusSquared*GameConstants.BROADCAST_RANGE_MULTIPLIER);
	}
	
	///////////////////////////////////////////////////////////////////
	// initialInfo
	//
	static class InitialInfo{
		int masterIndex;
		MapLocation masterInitial;
		
		InitialInfo(MapLocation initialMasterLoc){
			MapLocation[] il=rc.getInitialArchonLocations(myTeam);
			for(int i=0;i<il.length;i++){
				if(il[i].equals(initialMasterLoc)){
					masterIndex=i;
					break;
				}
			}
			masterInitial = il[masterIndex];
			ox=masterInitial.x-100;
			oy=masterInitial.y-100;
		}
		InitialInfo(int[] mess){
			MapLocation[] il=rc.getInitialArchonLocations(myTeam);
			masterIndex=(mess[1] >> 24)& 0xF; //6th nibble
			masterInitial=il[masterIndex];
			ox=masterInitial.x-100;
			oy=masterInitial.y-100;
		}
		void broadcast(int dist) throws GameActionException{
			if(rc.getMessageSignalCount()<GameConstants.MESSAGE_SIGNALS_PER_TURN)
				rc.broadcastMessageSignal(INITIALINFO,((masterIndex & 0xF)<<24), dist);
			else
				System.out.println("someone didn't get initialInfo!!!!!!!!!!!!!!!!!!!!!");
		}
		@Override
		public String toString(){
			return "masterInitial: "+masterInitial+" ox: "+ox+" oy: "+oy;
		}
	}
	
	
	///////////////////////////////////////////////////////////////////
	// teamInfo
	//
	static class TeamInfo{
		int numArchons;
		int numScouts;
		int state;
		MapLocation base;
		
		
		TeamInfo(int st,int nArchons, int nScouts, MapLocation b){
			state=st; numArchons=nArchons; numScouts=nScouts; base=b;
		}
		TeamInfo(int[] mess){
			base= messToLoc(mess[1]);
			//other= (mess[1]>>24) & 0xFF;
			state=mess[0] & 0xFF;
			numArchons = (mess[0] >> 20) & 0xF;
			numScouts  = (mess[0] >> 8 ) & 0xFF;
		}
		
		int[] toMess(){
			int[] mess = new int[2];
			mess[0]=TEAMINFO | ((numArchons & 0xF)<<20) | ((numScouts & 0xFF)<<8) | (state & 0xFF);
			mess[1]=locToMess(base) | ((0 & 0xff) << 24);
			return mess;
		}
		
		void broadcast(int dist) throws GameActionException{
			int[] mess=toMess();
			if(rc.getMessageSignalCount()<GameConstants.MESSAGE_SIGNALS_PER_TURN)
				rc.broadcastMessageSignal(mess[0], mess[1], dist);
			else
				qMessage(mess[0], mess[1], dist+6,false);
		}
		@Override
		public String toString(){
			return "state: "+state+" base: "+base+" numArchons: "+numArchons+" numScouts: "+numScouts;
		}
	}
	
	
	///////////////////////////////////////////////////////////////////
	// MapInfo
	//
	static class MapInfo{
		MapLocation loc;
		int rubble;
		int parts;
		boolean neutralBot;
		boolean blocked;
		boolean den;
		MapInfo(MapLocation loc, int rubble, int parts){
			this.loc=loc; this.rubble=rubble; this.parts=parts;
			if(this.rubble>4000) this.rubble=4000;
			if(this.parts>4000) this.parts=4000;
			if(this.rubble >= GameConstants.RUBBLE_OBSTRUCTION_THRESH)
				blocked=true;
		}
		MapInfo(MapLocation loc, int rubble, int parts, boolean isNeutral){
			this(loc,rubble,parts);
			neutralBot=isNeutral;
		}
		MapInfo(MapLocation loc, int rubble, int parts, boolean isNeutral, boolean isDen){
			this(loc,rubble,parts);
			neutralBot=isNeutral;
			den=isDen;
		}
		MapInfo(MapLocation loc, int rubble){
			this.loc=loc; this.rubble=rubble;
			if(this.rubble>4000) this.rubble=4000;
			if(this.rubble >= GameConstants.RUBBLE_OBSTRUCTION_THRESH)
				blocked=true;
		}
		MapInfo(int[] mess){
			loc=messToLoc(mess[1]);
			rubble=mess[0] & 0xFFF;
			parts=(mess[0] >> 12) & 0xFFF;
			den        = (mess[1] & 0x1_000_000) != 0;
			neutralBot = (mess[1] & 0x2_000_000) != 0;
			if(rubble >= GameConstants.RUBBLE_OBSTRUCTION_THRESH)
				blocked=true;
		}
		void broadcast(int dist) throws GameActionException{
			qMessage(MAPINFOMESS | ((parts &0xFFF) << 12) | (rubble & 0xFFF), locToMess(this.loc) | (den? 0x1_000_000 : 0) | (neutralBot? 0x2_000_000 : 0), dist,false);
		}
		void setParts(int parts){
			if(parts>4000) parts=4000;
			this.parts=parts;
		}
		void setDen(){
			den=true;
			blocked=true;
		}
		@Override
		public String toString(){
			return "Minfo: "+loc+" rub: "+rubble+" pts: "+parts+" den:"+den;
		}
		
	}
	
	
	///////////////////////////////////////////////////////////////////
	// common functions
	//
	
	static int farthest(MapLocation[] list, MapLocation start){
		if(list==null)
			return 0;
		int farthest=0;
		for(int i=0;i<list.length;i++){
			int d=start.distanceSquaredTo(list[i]);
			if(d>farthest)
				farthest=d;
		}
		return farthest;
	}
	
	static int locToMess(MapLocation loc){
		return (loc.y<<12)+loc.x;
	}
	
	static MapLocation messToLoc(int val){
		return new MapLocation(val&0xFFF,(val>>12)&0xFFF);
	}
	
	static int[] extentsToMess(boolean newSymmetryFound){
		int[] mess=new int[2];
		int found= 	(foundxMin? 0x100_0000 : 0) | 
						(foundxMax? 0x200_0000 : 0) | 
						(foundyMin? 0x400_0000 : 0) | 
						(foundyMax? 0x800_0000 : 0);
		if(newSymmetryFound)
			mess[0]= (xMin | (xMax<<12)) | EXTENTSMESSNSYM;
		else
			mess[0]= (xMin | (xMax<<12)) | EXTENTSMESS;
		mess[1]= ((yMin | (yMax<<12)) | found) | (symmetryType << 28);
		return mess;
	}
	static boolean messToExtents(int[] mess){ //mess[0]>>24 is extents message identifier
		int temp;
		boolean update=false;
		temp=mess[0] & 0xFFF;
		if(temp<xMin){ 
			xMin=temp;
			update=true;
		}
		temp=(mess[0] >>12) & 0xFFF;
		if(temp>xMax){ 
			xMax=temp;
			update=true;
		}
		temp=mess[1] & 0xFFF;
		if(temp<yMin){ 
			yMin=temp;
			update=true;
		}
		temp=(mess[1] >>12) & 0xFFF;
		if(temp>yMax){ 
			yMax=temp;
			update=true;
		}
		if(!foundxMin){
			if(foundxMin=(mess[1] & 0x100_0000)!=0)
				update=true;
		}
		if(!foundxMax){
			if(foundxMax=(mess[1] & 0x200_0000)!=0)
				update=true;
		}
		if(!foundyMin){
			if(foundyMin=(mess[1] & 0x400_0000)!=0)
				update=true;
		}
		if(!foundyMax){
			if(foundyMax=(mess[1] & 0x800_0000)!=0)
				update=true;
		}
		
		symmetryType=(mess[1] >> 28) & 0xF;
		
		foundAllExtents=(foundxMin && foundxMax && foundyMin && foundyMax);
		return update;
	}
	
	static void broadcastExtentSignal(int dist, boolean newSymmetryFound) throws GameActionException{
		int[] mess=extentsToMess(newSymmetryFound);
		qMessage(mess[0], mess[1], dist,true);
	}
	static void broadcastTeamInfo(int dist) throws GameActionException{
		int[] mess=teamInfo.toMess();
		qMessage(mess[0], mess[1], dist,false);
	}
	
	static int distSq(MapLocation location) {
		int dx=rc.getLocation().x-location.x;
		int dy=rc.getLocation().y-location.y;
		return dx*dx+dy*dy;
	}

	
	static class PartInfo{
		MapLocation loc;
		int numParts;
		boolean sent=false; //broadcasted
		
		PartInfo(MapLocation location, int number){
			this.loc=location;
			this.numParts=number;
		}
		PartInfo(int[] mess){
			loc=messToLoc(mess[1]);
			numParts=mess[0]&0xFFFF;
		}
		int[] toMess(){
			int[] mess=new int[2];
			mess[1]=locToMess(loc);
			if(numParts>0xFFFF){
				mess[0]=PARTSLOC | 0xFFFF;
				return mess;
			}
			mess[0]=PARTSLOC | numParts;
			return mess;
		}
		@Override
		public boolean equals(Object b){
			return loc.equals(((PartInfo)b).loc);
		}
		@Override
		public int hashCode(){
			return loc.hashCode();
		}
		@Override
		public String toString(){
			return ""+loc+numParts;
		}
		
	}
	


	
	static void senseRubble() throws GameActionException{
		/////////////////////////////////////
		// get rubble at sense-able locations
		MapLocation[] locs=MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), rc.getType().sensorRadiusSquared);
		for(int i=0;i<locs.length;i++){			
			int rub=(int)rc.senseRubble(locs[i]);
			
			//still need to store and communicate it
			
			//map[100+locs[i].x-centerLoc.x][100+locs[i].y-centerLoc.y]=new MapInfo(locs[i],rub);
		}
	}
	
	static boolean senseParts() throws GameActionException{
		
		MapLocation[] partLocs=rc.sensePartLocations(-1);
		
		if(partLocs.length==0){
			return false;
		}
		
		for(MapLocation ml:partLocs){
			PartInfo p=new PartInfo(ml,(int)rc.senseParts(ml));
//			if(!partList.contains(p)){
//				partList.add(p);
//				if(!master && messageMaster(p.toMess()))
//					p.sent=true;
//			}
		}
		return true;
	}
	
	static void senseDens(){
		RobotInfo[] ri=rc.senseNearbyRobots(-1, Team.ZOMBIE);
		for(RobotInfo r:ri){
			if(r.type==RobotType.ZOMBIEDEN){
				map[100+r.location.x-centerLoc.x][100+r.location.y-centerLoc.y].setDen();
			}
		}
	}
	
//	static boolean messageMaster(int[] mess) throws GameActionException{		
//		qMessage(mess[0], mess[1], distanceToMaster);
//		rc.setIndicatorString(2, "quedmesstoMaster: "+mess[0]+", "+mess[1]+", "+distanceToMaster);
//		return true;
//	
//	}
	
//	static boolean messageArchons(int[] mess) throws GameActionException{		
//		qMessage(mess[0], mess[1], distanceToMaster);
//		rc.setIndicatorString(2, "qedmessToArchons: "+mess[0]+", "+mess[1]+", "+distanceToMaster);
//		return true;
//	}

	
	public static class RangeComparator implements Comparator<RobotInfo>{
		@Override
		public int compare(RobotInfo a, RobotInfo b){
			int da=rc.getLocation().distanceSquaredTo(a.location);
			int db=rc.getLocation().distanceSquaredTo(b.location);
			return da<db? -1: da==db? 0: 1;
		}
	}
	public static class MasterRangeComparator implements Comparator<RobotInfo>{
		@Override
		public int compare(RobotInfo a, RobotInfo b){
			int da=masterLoc.distanceSquaredTo(a.location);
			int db=masterLoc.distanceSquaredTo(b.location);
			return da<db? -1: da==db? 0: 1;
		}
	}
	public static class HealthComparator implements Comparator<RobotInfo>{
		@Override
		public int compare(RobotInfo a, RobotInfo b){
			double da=a.health;
			double db=b.health;
			return da<db? -1: da==db? 0: 1;
		}
	}
	
	static void healNearby() throws GameActionException{
		RobotInfo[] ri=rc.senseNearbyRobots(-1, myTeam);
		if(ri.length>0){
			double lowest=99999;
			MapLocation ml=null;
			for(RobotInfo r:ri){
				double h=r.health;
				if(h<r.maxHealth && r.type!=RobotType.ARCHON && r.location.distanceSquaredTo(rc.getLocation())<=rc.getType().attackRadiusSquared && r.health<lowest){
					lowest=r.health;
					ml=r.location;
				}
			}
			if(ml!=null)
				rc.repair(ml);
		}
	}
	
	static void clearNearby() throws GameActionException{
		if(rc.isCoreReady()){
			for(Direction d:Direction.values()){
				if(rc.senseRubble(rc.getLocation().add(d))>0){
					rc.clearRubble(d);
					break;
				}
			}
		}
	}
	
	static void setExtents(MapLocation[] list){
		for(int i=0;i<list.length;i++){
			if(list[i].x>xMax) xMax=list[i].x;
			if(list[i].x<xMin) xMin=list[i].x;
			if(list[i].y>yMax) yMax=list[i].y;
			if(list[i].y<yMin) yMin=list[i].y;
		}
	}
	
	static void figureExtents() throws GameActionException{
		if(foundAllExtents)
			return;
				
		setExtents(myArchonInitLocs);
		setExtents(otherArchonInitLocs);
		
		if(otherArchonInitLocs.length==0){//TODO only Zombie appocalypse
			return;
		}
		
		
		int len=myArchonInitLocs.length;
		
		//test if each initial has a matching otherteam initial reflected across Y
		//(has same x coordinate)
		boolean[] used=new boolean[len];
		for(int i=0;i<len;i++){
			boolean matched=false;
			for(int j=0;j<len;j++){
				if(!used[j] && otherArchonInitLocs[j].x == myArchonInitLocs[i].x){
					matched=true;
					used[j]=true;
					break;
				}
			}
			if(!matched){
				couldBeYMirror=false;
				break;
			}
		}
		
		//test if each initial has a matching otherteam initial reflected across Y
		//(has same x coordinate)
		used=new boolean[len];
		for(int i=0;i<len;i++){
			boolean matched=false;
			for(int j=0;j<len;j++){
				if(!used[j] && otherArchonInitLocs[j].y == myArchonInitLocs[i].y){
					matched=true;
					used[j]=true;
					break;
				}
			}
			if(!matched){
				couldBeXMirror=false;
				break;
			}
		}
		
		if(!couldBeXMirror && !couldBeYMirror)
			symmetryType=ROTATION;
		
		// see if can find rotation

		
		if(!couldBeRotation){
			if(couldBeXMirror){
				symmetryType=XMIRROR;
			}else{
				symmetryType=YMIRROR;
			}
		}
		
		if(symmetryType<3){//still unknown
			if(!couldBeXMirror)
				symmetryType=YMIRRORORROT;
			else{
				symmetryType=XMIRRORORROT;
			}
		}
		
		//symmetryType can't be zero anymore
		
		debS+="Initialsym:"+symmetryType;
		
		senseExtents();
	}
	
	static void findCenter(){
		int len=myArchonInitLocs.length;
		
		if(otherArchonInitLocs.length==0)//TODO only Zombie appocalypse
			return;
		
		//honestly I don't remember how this works
		//finds out if each initial archon pos has a matching ROTATION enemy archon position
		//and also figures the center of all the archon locations regardless of symmetry,
		//if rotational symmetry then this is the center of map, otherwise it may or may not be.
		int locCenterx=0,locCentery=0;
		for(int j=0;j<len;j++){
			locCenterx= myArchonInitLocs[0].x+otherArchonInitLocs[j].x;
			locCentery= myArchonInitLocs[0].y+otherArchonInitLocs[j].y;
			boolean allFound=true;
			for(int p=1;p<len;p++){
				boolean found=false;
				for(int k=0;k<len;k++){
					if(myArchonInitLocs[p].x + otherArchonInitLocs[k].x == locCenterx && myArchonInitLocs[p].y + otherArchonInitLocs[k].y == locCentery){
						found=true;
						break;
					}
				}
				if(!found){
					allFound=false;
				}
			}
			if(allFound){
				couldBeRotation=true;
				break;
			}
		}

		centerx=(double)locCenterx/2;
		centery=(double)locCentery/2;
	}
	
	static MapLocation getCenterOfMap(){
		return new MapLocation((xMin+xMax)/2,(yMax+yMin)/2);
	}
	////////////////////////////////////////////////////////////////
	// senseExtents()
	//
	//returns true if found a new extent, should send a signal to master i guess
	//
	static boolean senseExtents() throws GameActionException{
		centerOfMap=getCenterOfMap();
		if(foundAllExtents)
			return false;
		
		
		
		boolean foundNew=false;
		boolean foundLarger=false;
		boolean newSymFound=false;
		
		int d=(int) Math.sqrt(rc.getType().sensorRadiusSquared);
		MapLocation centerLoc=rc.getLocation();
		MapLocation cur=null;
		
		
		/////////////
		// check for edge of map in each direction
		if(!foundxMax && (centerLoc.x+d>xMax)){
			for(int x=1;x<=d;x++){
				cur=centerLoc.add(x,0);
				if(!rc.onTheMap(cur)){
					foundxMax=true;
					foundNew=true;
					xMax=cur.x-1;
					if(foundxMin)
						xSize=xMax-xMin+1;
					break;
				}
			}
			if(!foundxMax){
				foundLarger=true;
				xMax=cur.x;
			}
		}
		if(!foundyMax && (centerLoc.y+d>yMax)){
			for(int y=1;y<=d;y++){
				cur=centerLoc.add(0,y);
				if(!rc.onTheMap(cur)){
					foundyMax=true;
					foundNew=true;
					yMax=cur.y-1;
					if(foundyMin)
						ySize=yMax-yMin+1;
					break;
				}
			}
			if(!foundyMax){
				foundLarger=true;
				yMax=cur.y;
			}
		}
		if(!foundxMin && (centerLoc.x-d<xMin)){
			for(int x=-1;x>=-d;x--){
				cur=centerLoc.add(x,0);
				if(!rc.onTheMap(cur)){
					foundxMin=true;
					foundNew=true;
					xMin=cur.x+1;
					if(foundxMax)
						xSize=xMax-xMin+1;
					break;
				}
			}
			if(!foundxMin){
				foundLarger=true;
				xMin=cur.x;
			}
		}
		if(!foundyMin && (centerLoc.y-d<yMin)){
			for(int y=-1;y>=-d;y--){
				cur=centerLoc.add(0,y);
				if(!rc.onTheMap(cur)){
					foundyMin=true;
					foundNew=true;
					yMin=cur.y+1;
					if(foundyMax) 
						ySize=yMax-yMin+1;
					break;
				}
			}
			if(!foundyMin){
				foundLarger=true;
				yMin=cur.y;
			}
		}
		
		//////////////////////////////
		// if symmetry unknown and new extents found try to deduce symmetry
		if(symmetryType<3){
			if(symmetryType==0){//need to figure, should not happen
				System.out.println("SymmetryType 0!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			}
			
			if(!foundNew && foundLarger){
				debS+=" (new xtent found, trying to deduce symmetry)";
				if(symmetryType==YMIRRORORROT && (foundxMax || foundxMin)){
					if(foundxMax){
						double knownDist=xMax-centerx;
						double newDist=centerx-xMin;
						if(newDist>knownDist){
							symmetryType=YMIRROR;
							newSymFound=true;
						}
					}else{//foundxMin
						double knownDist=centerx-xMin;
						double newDist=xMax-centerx;
						if(newDist>knownDist){
							symmetryType=YMIRROR;
							newSymFound=true;
						}
					}
					
				}else if(foundyMax || foundyMin){//must be XMIRRORORROT
					if(foundyMax){
						double knownDist=yMax-centery;
						double newDist=centery-yMin;
						if(newDist>knownDist){
							symmetryType=XMIRROR;
							newSymFound=true;
						}
					}else{//foundxMin
						double knownDist=centery-yMin;
						double newDist=yMax-centery;
						if(newDist>knownDist){
							symmetryType=XMIRROR;
							newSymFound=true;
						}
					}
					
				}
			}else if(foundNew){
				debS+=" (new xtent limit found, trying to deduce symmetry)";
				if(symmetryType==YMIRRORORROT && (foundxMax || foundxMin)){
					if(foundxMax){
						double knownDist=xMax-centerx;
						double otherDist=centerx-xMin;
						if(otherDist>knownDist){
							symmetryType=YMIRROR;
							newSymFound=true;
						}
					}else{//foundxMin
						double knownDist=centerx-xMin;
						double otherDist=xMax-centerx;
						if(otherDist>knownDist){
							symmetryType=YMIRROR;
							newSymFound=true;
						}
						
					}
				}else if(foundyMax || foundyMin){//XMIRRORORROT
					if(foundyMax){
						double knownDist=yMax-centery;
						double otherDist=centery-yMin;
						if(otherDist>knownDist){
							symmetryType=XMIRROR;
							newSymFound=true;
						}
					}else{//foundxMin
						double knownDist=centery-yMin;
						double otherDist=yMax-centery;
						if(otherDist>knownDist){
							symmetryType=XMIRROR;
							newSymFound=true;
						}
						
					}
					
				}
			}
			
		}
		
		
		//////////////////////////////////
		// deduce unknown extents from symmetry if possible
		extFromSym();
		
		if(newSymFound){
			/////woo hoo!!!!!!!!! let everyone know
			//only want to do this if not under attack
			broadcastExtentSignal(maxBroadcastDist(),true);
			debS+=" NEW SYMMETRY SENT ";
			return false;//not needed to send anyone extent message
		}
		
		return foundNew;
	}
	
	static void extFromSym() {
		if(symmetryType==ROTATION || symmetryType==XMIRROR || symmetryType==XMIRRORORROT){
			if(!foundxMin && foundxMax){
				foundxMin=true;
				xMin=(int)(2*centerx)-xMax;
			}
			if(!foundxMax && foundxMin){
				foundxMax=true;
				xMax=(int)(2*centerx)-xMin;
			}
		}
		if(symmetryType==ROTATION || symmetryType==YMIRROR || symmetryType==YMIRRORORROT){
			if(!foundyMin && foundyMax){
				foundyMin=true;
				yMin=(int)(2*centery)-yMax;
			}
			if(!foundyMax && foundyMin){
				foundyMax=true;
				yMax=(int)(2*centery)-yMin;
			}
		}
		
		foundAllExtents=(foundxMax && foundxMin && foundyMax && foundyMin);
	}

	////////////////////////////////////////////////////////////////
	//common structures
	//


}

