package team051;

import battlecode.common.*;
import static team051.Common.*;
import static team051.Message.*;

public class NewLocs {
	static int[][][] masterMask=new int[][][]
		//scout
		{{{-7,-3},{-6,-5},{-5,-6},{-4,-7},{-3,-7},{-2,-8},{-1,-8},{0,-8},{1,-8},{2,-8},{3,-7},{4,-7},{5,-6},{6,-5},{7,-3}},
		{{-3,-7},{-1,-8},{0,-8},{1,-8},{2,-8},{3,-8},{3,-7},{4,-7},{5,-7},{5,-6},{6,-6},{6,-5},{7,-5},{7,-4},{7,-3},{7,3},{8,-3},{8,-2},{8,-1},{8,0},{8,1}},
		{{3,-7},{3,7},{5,-6},{5,6},{6,-5},{6,5},{7,-4},{7,-3},{7,3},{7,4},{8,-2},{8,-1},{8,0},{8,1},{8,2}},
		{{-3,7},{-1,8},{0,8},{1,8},{2,8},{3,7},{3,8},{4,7},{5,6},{5,7},{6,5},{6,6},{7,-3},{7,3},{7,4},{7,5},{8,-1},{8,0},{8,1},{8,2},{8,3}},
		{{-7,3},{-6,5},{-5,6},{-4,7},{-3,7},{-2,8},{-1,8},{0,8},{1,8},{2,8},{3,7},{4,7},{5,6},{6,5},{7,3}},
		{{-8,-1},{-8,0},{-8,1},{-8,2},{-8,3},{-7,-3},{-7,3},{-7,4},{-7,5},{-6,5},{-6,6},{-5,6},{-5,7},{-4,7},{-3,7},{-3,8},{-2,8},{-1,8},{0,8},{1,8},{3,7}},
		{{-8,-2},{-8,-1},{-8,0},{-8,1},{-8,2},{-7,-4},{-7,-3},{-7,3},{-7,4},{-6,-5},{-6,5},{-5,-6},{-5,6},{-3,-7},{-3,7}},
		{{-8,-3},{-8,-2},{-8,-1},{-8,0},{-8,1},{-7,-5},{-7,-4},{-7,-3},{-7,3},{-6,-6},{-6,-5},{-5,-7},{-5,-6},{-4,-7},{-3,-8},{-3,-7},{-2,-8},{-1,-8},{0,-8},{1,-8},{3,-7}}};
		
//		static int[][][][] masterMask=new int[][][][]{
//			//archon
//			{{{-5,-4},{-4,-5},{-3,-6},{-2,-6},{-1,-6},{0,-6},{1,-6},{2,-6},{3,-6},{4,-5},{5,-4}},
//			{{-2,-6},{-1,-6},{0,-6},{1,-6},{2,-6},{3,-6},{4,-6},{4,-5},{5,-5},{5,-4},{6,-4},{6,-3},{6,-2},{6,-1},{6,0},{6,1},{6,2}},
//			{{4,-5},{4,5},{5,-4},{5,4},{6,-3},{6,-2},{6,-1},{6,0},{6,1},{6,2},{6,3}},
//			{{-2,6},{-1,6},{0,6},{1,6},{2,6},{3,6},{4,5},{4,6},{5,4},{5,5},{6,-2},{6,-1},{6,0},{6,1},{6,2},{6,3},{6,4}},
//			{{-5,4},{-4,5},{-3,6},{-2,6},{-1,6},{0,6},{1,6},{2,6},{3,6},{4,5},{5,4}},
//			{{-6,-2},{-6,-1},{-6,0},{-6,1},{-6,2},{-6,3},{-6,4},{-5,4},{-5,5},{-4,5},{-4,6},{-3,6},{-2,6},{-1,6},{0,6},{1,6},{2,6}},
//			{{-6,-3},{-6,-2},{-6,-1},{-6,0},{-6,1},{-6,2},{-6,3},{-5,-4},{-5,4},{-4,-5},{-4,5}},
//			{{-6,-4},{-6,-3},{-6,-2},{-6,-1},{-6,0},{-6,1},{-6,2},{-5,-5},{-5,-4},{-4,-6},{-4,-5},{-3,-6},{-2,-6},{-1,-6},{0,-6},{1,-6},{2,-6}}},
//			//guard
//			{{{-4,-3},{-3,-4},{-2,-5},{-1,-5},{0,-5},{1,-5},{2,-5},{3,-4},{4,-3}},
//			{{-1,-5},{0,-5},{1,-5},{2,-5},{3,-5},{3,-4},{4,-4},{4,-3},{5,-3},{5,-2},{5,-1},{5,0},{5,1}},
//			{{3,-4},{3,4},{4,-3},{4,3},{5,-2},{5,-1},{5,0},{5,1},{5,2}},
//			{{-1,5},{0,5},{1,5},{2,5},{3,4},{3,5},{4,3},{4,4},{5,-1},{5,0},{5,1},{5,2},{5,3}},
//			{{-4,3},{-3,4},{-2,5},{-1,5},{0,5},{1,5},{2,5},{3,4},{4,3}},
//			{{-5,-1},{-5,0},{-5,1},{-5,2},{-5,3},{-4,3},{-4,4},{-3,4},{-3,5},{-2,5},{-1,5},{0,5},{1,5}},
//			{{-5,-2},{-5,-1},{-5,0},{-5,1},{-5,2},{-4,-3},{-4,3},{-3,-4},{-3,4}},
//			{{-5,-3},{-5,-2},{-5,-1},{-5,0},{-5,1},{-4,-4},{-4,-3},{-3,-5},{-3,-4},{-2,-5},{-1,-5},{0,-5},{1,-5}}},
//			//scout
//			{{{-7,-3},{-6,-5},{-5,-6},{-4,-7},{-3,-7},{-2,-8},{-1,-8},{0,-8},{1,-8},{2,-8},{3,-7},{4,-7},{5,-6},{6,-5},{7,-3}},
//			{{-3,-7},{-1,-8},{0,-8},{1,-8},{2,-8},{3,-8},{3,-7},{4,-7},{5,-7},{5,-6},{6,-6},{6,-5},{7,-5},{7,-4},{7,-3},{7,3},{8,-3},{8,-2},{8,-1},{8,0},{8,1}},
//			{{3,-7},{3,7},{5,-6},{5,6},{6,-5},{6,5},{7,-4},{7,-3},{7,3},{7,4},{8,-2},{8,-1},{8,0},{8,1},{8,2}},
//			{{-3,7},{-1,8},{0,8},{1,8},{2,8},{3,7},{3,8},{4,7},{5,6},{5,7},{6,5},{6,6},{7,-3},{7,3},{7,4},{7,5},{8,-1},{8,0},{8,1},{8,2},{8,3}},
//			{{-7,3},{-6,5},{-5,6},{-4,7},{-3,7},{-2,8},{-1,8},{0,8},{1,8},{2,8},{3,7},{4,7},{5,6},{6,5},{7,3}},
//			{{-8,-1},{-8,0},{-8,1},{-8,2},{-8,3},{-7,-3},{-7,3},{-7,4},{-7,5},{-6,5},{-6,6},{-5,6},{-5,7},{-4,7},{-3,7},{-3,8},{-2,8},{-1,8},{0,8},{1,8},{3,7}},
//			{{-8,-2},{-8,-1},{-8,0},{-8,1},{-8,2},{-7,-4},{-7,-3},{-7,3},{-7,4},{-6,-5},{-6,5},{-5,-6},{-5,6},{-3,-7},{-3,7}},
//			{{-8,-3},{-8,-2},{-8,-1},{-8,0},{-8,1},{-7,-5},{-7,-4},{-7,-3},{-7,3},{-6,-6},{-6,-5},{-5,-7},{-5,-6},{-4,-7},{-3,-8},{-3,-7},{-2,-8},{-1,-8},{0,-8},{1,-8},{3,-7}}},
//			//soldier
//			{{{-4,-3},{-3,-4},{-2,-5},{-1,-5},{0,-5},{1,-5},{2,-5},{3,-4},{4,-3}},
//			{{-1,-5},{0,-5},{1,-5},{2,-5},{3,-5},{3,-4},{4,-4},{4,-3},{5,-3},{5,-2},{5,-1},{5,0},{5,1}},
//			{{3,-4},{3,4},{4,-3},{4,3},{5,-2},{5,-1},{5,0},{5,1},{5,2}},
//			{{-1,5},{0,5},{1,5},{2,5},{3,4},{3,5},{4,3},{4,4},{5,-1},{5,0},{5,1},{5,2},{5,3}},
//			{{-4,3},{-3,4},{-2,5},{-1,5},{0,5},{1,5},{2,5},{3,4},{4,3}},
//			{{-5,-1},{-5,0},{-5,1},{-5,2},{-5,3},{-4,3},{-4,4},{-3,4},{-3,5},{-2,5},{-1,5},{0,5},{1,5}},
//			{{-5,-2},{-5,-1},{-5,0},{-5,1},{-5,2},{-4,-3},{-4,3},{-3,-4},{-3,4}},
//			{{-5,-3},{-5,-2},{-5,-1},{-5,0},{-5,1},{-4,-4},{-4,-3},{-3,-5},{-3,-4},{-2,-5},{-1,-5},{0,-5},{1,-5}}},
//			//TTM
//			{{{-4,-3},{-3,-4},{-2,-5},{-1,-5},{0,-5},{1,-5},{2,-5},{3,-4},{4,-3}},
//			{{-1,-5},{0,-5},{1,-5},{2,-5},{3,-5},{3,-4},{4,-4},{4,-3},{5,-3},{5,-2},{5,-1},{5,0},{5,1}},
//			{{3,-4},{3,4},{4,-3},{4,3},{5,-2},{5,-1},{5,0},{5,1},{5,2}},
//			{{-1,5},{0,5},{1,5},{2,5},{3,4},{3,5},{4,3},{4,4},{5,-1},{5,0},{5,1},{5,2},{5,3}},
//			{{-4,3},{-3,4},{-2,5},{-1,5},{0,5},{1,5},{2,5},{3,4},{4,3}},
//			{{-5,-1},{-5,0},{-5,1},{-5,2},{-5,3},{-4,3},{-4,4},{-3,4},{-3,5},{-2,5},{-1,5},{0,5},{1,5}},
//			{{-5,-2},{-5,-1},{-5,0},{-5,1},{-5,2},{-4,-3},{-4,3},{-3,-4},{-3,4}},
//			{{-5,-3},{-5,-2},{-5,-1},{-5,0},{-5,1},{-4,-4},{-4,-3},{-3,-5},{-3,-4},{-2,-5},{-1,-5},{0,-5},{1,-5}}},
//			//viper
//			{{{-4,-3},{-3,-4},{-2,-5},{-1,-5},{0,-5},{1,-5},{2,-5},{3,-4},{4,-3}},
//			{{-1,-5},{0,-5},{1,-5},{2,-5},{3,-5},{3,-4},{4,-4},{4,-3},{5,-3},{5,-2},{5,-1},{5,0},{5,1}},
//			{{3,-4},{3,4},{4,-3},{4,3},{5,-2},{5,-1},{5,0},{5,1},{5,2}},
//			{{-1,5},{0,5},{1,5},{2,5},{3,4},{3,5},{4,3},{4,4},{5,-1},{5,0},{5,1},{5,2},{5,3}},
//			{{-4,3},{-3,4},{-2,5},{-1,5},{0,5},{1,5},{2,5},{3,4},{4,3}},
//			{{-5,-1},{-5,0},{-5,1},{-5,2},{-5,3},{-4,3},{-4,4},{-3,4},{-3,5},{-2,5},{-1,5},{0,5},{1,5}},
//			{{-5,-2},{-5,-1},{-5,0},{-5,1},{-5,2},{-4,-3},{-4,3},{-3,-4},{-3,4}},
//			{{-5,-3},{-5,-2},{-5,-1},{-5,0},{-5,1},{-4,-4},{-4,-3},{-3,-5},{-3,-4},{-2,-5},{-1,-5},{0,-5},{1,-5}}}};
		
//		static int[][][] movementMask;
//		
//		static int[] types=new int[]{RobotType.ARCHON.ordinal(),RobotType.GUARD.ordinal(),RobotType.SCOUT.ordinal(),
//				RobotType.SOLDIER.ordinal(),RobotType.TTM.ordinal(),RobotType.VIPER.ordinal()};
//		
//		static RobotType[] typeList=new RobotType[]{RobotType.ARCHON,RobotType.GUARD,RobotType.SCOUT,
//				RobotType.SOLDIER,RobotType.TTM,RobotType.VIPER};
//			
//		private static int typeIndex(){
//			switch(rc.getType()){
//			case ARCHON:return 0;
//			case GUARD:return 1;
//			case SCOUT:return 2;
//			case SOLDIER:return 3;
//			case TTM:
//			case TURRET:return 4;
//			case VIPER:return 5;
//			default:
//			}
//			return 0;
//		}
//		
//		static void createMask(){
//			movementMask=masterMask[typeIndex()];
//		}
//		
		
		static int[][][] movementMask;
	
	static void createMask(){
		movementMask=masterMask;
	}
		
	
	
	
//	static void debug_createMasterMask(){
//		MapLocation center=new MapLocation(0,0);
//		int[] temp=new int[50];
//		
//		
//		int[] types=new int[]{RobotType.ARCHON.ordinal(),RobotType.GUARD.ordinal(),RobotType.SCOUT.ordinal(),
//				RobotType.SOLDIER.ordinal(),RobotType.TTM.ordinal(),RobotType.VIPER.ordinal()};
//		for(int t=0;t<types.length;t++){
//			MapLocation[] original=MapLocation.getAllMapLocationsWithinRadiusSq(center, RobotType.values()[types[t]].sensorRadiusSquared);
//			System.out.println("\ntype: "+RobotType.values()[types[t]]);
//			for(int d=0;d<8;d++){
//				MapLocation newCenter=center.add(Direction.values()[d]);
//				MapLocation[] newLocs=MapLocation.getAllMapLocationsWithinRadiusSq(newCenter, RobotType.values()[types[t]].sensorRadiusSquared);
//				int newCount=0;
//				for(int i=0;i<newLocs.length;i++){
//					if(!contains(original,newLocs[i])){
//						temp[newCount++]=i;
//					}
//				}
//				System.out.print("direction:"+d+"\n{");
//				for(int i=0;i<newCount;i++){
//					if(i!=newCount-1)
//						System.out.print("{"+(newLocs[temp[i]].x-center.x)+","+(newLocs[temp[i]].y-center.y)+"},");
//					else
//						System.out.print("{"+(newLocs[temp[i]].x-center.x)+","+(newLocs[temp[i]].y-center.y)+"}");
//				}
//				System.out.println("},");
//			}
//		}
//		
//	}
//	
//	private static boolean contains(MapLocation[] original, MapLocation mapLocation) {
//		for(int i=0;i<original.length;i++){
//			if(original[i].equals(mapLocation))
//				return true;
//		}
//		return false;
//	}

	static MapLocation[] getNewLocs(Direction dir, MapLocation loc) throws GameActionException{
		//int bcs=Clock.getBytecodeNum();
		if(dir==Direction.NONE || dir==Direction.OMNI)
			return new MapLocation[0];
		
		MapLocation oldLoc=loc.add(dir.opposite());
		int d=dir.ordinal();
		int n=movementMask[d].length;
		MapLocation[] list=new MapLocation[n];
		
		
		//if not near edge, easy
		if(Scout.distToEdge(loc)>=rc.getType().sensorRadiusSquared){
			for(int i=0;i<n;i++){
				list[i]=new MapLocation(oldLoc.x+movementMask[d][i][0],oldLoc.y+movementMask[d][i][1]);
			}
			//int bc=Clock.getBytecodeNum();
			return list;
			
		}
		
		
		int j=0;
		
		switch(dir){
		case NORTH:
			for(int i=0;i<n;i++){
				MapLocation m=new MapLocation(oldLoc.x+movementMask[d][i][0],oldLoc.y+movementMask[d][i][1]);
				if(!(m.x>xMax || m.x<xMin || m.y<yMin))
					list[j++]=m;
			}
			break;
		case EAST:
			for(int i=0;i<n;i++){
				MapLocation m=new MapLocation(oldLoc.x+movementMask[d][i][0],oldLoc.y+movementMask[d][i][1]);
				if(!(m.x>xMax || m.y>yMax || m.y<yMin))
					list[j++]=m;
			}
			break;
		case SOUTH:
			for(int i=0;i<n;i++){
				MapLocation m=new MapLocation(oldLoc.x+movementMask[d][i][0],oldLoc.y+movementMask[d][i][1]);
				if(!(m.x>xMax || m.x<xMin || m.y>yMax))
					list[j++]=m;
			}
			break;
		case WEST:
			for(int i=0;i<n;i++){
				MapLocation m=new MapLocation(oldLoc.x+movementMask[d][i][0],oldLoc.y+movementMask[d][i][1]);
				if(!(m.x<xMin || m.y>yMax || m.y<yMin))
					list[j++]=m;
			}
			break;
		case NORTH_WEST:
		case SOUTH_WEST:
		case NORTH_EAST:
		case SOUTH_EAST:
			for(int i=0;i<n;i++){
				MapLocation m=new MapLocation(oldLoc.x+movementMask[d][i][0],oldLoc.y+movementMask[d][i][1]);
				if(!(m.x>xMax || m.x<xMin || m.y>yMax || m.y<yMin))
					list[j++]=m;
			}
			break;
			default:
		}
		
		if(j<n){
			MapLocation[] slist=new MapLocation[j];
			System.arraycopy(list, 0, slist, 0, j);
			return slist;
		}
		return list;
	}

}
