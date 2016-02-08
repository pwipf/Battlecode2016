package team051;

import battlecode.common.*;
import static team051.Common.*;
import static team051.Message.*;
import static team051.Move.*;



public class RobotPlayer{

	public static void run(RobotController rcin){
		//Move.debug_createMasterMask();	
		
//		for(int i=0;i<RobotType.values().length;i++){
//			System.out.println("type:"+RobotType.values()[i]+" canClear:"+RobotType.values()[i].canClearRubble());
//		}
		
		
		
		rc=rcin;
		myTeam=rc.getTeam();
		otherTeam=(myTeam==Team.A)? Team.B : Team.A;
		startLoc=rc.getLocation();
		
		//TODO these only for robots that need it
		otherArchonInitLocs=rc.getInitialArchonLocations(otherTeam);
		myArchonInitLocs=rc.getInitialArchonLocations(myTeam);
		
		int sx=0,sy=0;
		for(int i=0;i<otherArchonInitLocs.length;i++){
			sx+=otherArchonInitLocs[i].x;
			sy+=otherArchonInitLocs[i].y;
		}
		if(otherArchonInitLocs.length>0)
			enemybase=new MapLocation(sx/otherArchonInitLocs.length,sy/otherArchonInitLocs.length);
		else enemybase=rc.getLocation().add(Direction.NORTH_EAST, 50);
		
		
		if(rc.getType()==RobotType.SCOUT){
			findCenter();
			NewLocs.createMask();
		}
		
		if(rc.getType()==RobotType.ARCHON){
			findCenter();			
			if(rc.getRoundNum()==0){
			
				try{
					Archon.electMaster();
				}
				catch(GameActionException e){
					e.printStackTrace();
				}
			}
		}
	
		
		while(true){
			try{
				if(rc.getRoundNum()!=0)
					debS="dbs: H:"+rc.getHealth();
				
				lostHealth=(rc.getHealth()<lastRoundHealth);
				
				sLoc=rc.getLocation();
				hostileTargets.clear();
				
				
				if(!locHistory.contains(rc.getLocation())){
					locHistory.add(rc.getLocation());
					if(locHistory.size()>histSize)
						locHistory.remove(0);
				}
				
				
				hostiles=rc.senseHostileRobots(rc.getLocation(), -1);
				friendlies=rc.senseNearbyRobots(-1, myTeam);

				//rc.setIndicatorString(2, "made it here2"+Clock.getBytecodeNum());

				processSignals();
				
				
				if(masterLoc!=null)
					distanceToMaster=rc.getLocation().distanceSquaredTo(masterLoc);
				else
					System.out.println("did not get masterLoc!!!!!!!!!!!!!!!");
				
				moveOverIfNecessary();
				
				
				switch(rc.getType()){
					case ARCHON:
						
						healNearby();
						
						if(master)
							MasterArchon.run();
						else
							Archon.run();
						sendQdMessages();
						break;
					case SCOUT:
						Scout.run();
						sendQdMessages();
						break;
					case SOLDIER:
						Soldier.run();
						break;
					case GUARD:
						Guard.run();
						break;
					case VIPER:
						Viper.run();
						break;
					case TURRET:
						Turret.run();
						break;
					case TTM:
						Turret.runTTM();
						break;
					default:
				}
				
				debS+=" msent:"+rc.getMessageSignalCount()+" bmsent:"+rc.getBasicSignalCount();
				rc.setIndicatorString(1, "symmetryType: "+symmetryType+"  xMax: "+xMax+" xMin: "+xMin+" yMax: "+yMax+" yMin: "+yMin
						+" fxmin:"+foundxMin+" fxmax:"+foundxMax+" fymin:"+foundyMin+" fymax:"+foundyMax+" foundall: "+foundAllExtents);
				
				//rc.setIndicatorString(1, "dest:"+destination+" dist:"+destDist);
				
//				String locst="locHist:";
//				for(int i=0;i<locHistory.size();i++){
//					locst+=locHistory.get(i);
//				}
//				rc.setIndicatorString(2, locst);
				
				String getout="getoutHist:";
				for(int i=0;i<getOutList.size();i++){
					getout+=getOutList.get(i);
				}
				//rc.setIndicatorString(2, "getoutHist:"+getout);
				
				rc.setIndicatorString(2, "protectors "+Protector.pListShow());
				
				MapLocation fLoc=rc.getLocation();
				
				if(!sLoc.equals(fLoc)){
					//int bcs=Clock.getBytecodeNum();

				    if(rc.getType()==RobotType.SCOUT){
						int n=Scout.updateMap(sLoc.directionTo(fLoc));
					}
					//int bc=Clock.getBytecodeNum();
					//rc.setIndicatorString(2, "figure newlocs:"+(bc-bcs)+" n:"+n+" dir:"+sLoc.directionTo(fLoc));
					//rc.setIndicatorString(1, "map explored: "+Debug.debug_countMap());
				}

				//rc.setIndicatorString(1, "senseRobots: "+(bc-bcs));
				
				lastRoundHealth=rc.getHealth();
				debS+=" lastH:"+lastRoundHealth;
				rc.setIndicatorString(0, debS);
				firstMove=false;
				Clock.yield();
			}
			catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
