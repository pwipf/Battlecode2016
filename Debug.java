package team051;

import battlecode.common.*;
import static team051.Common.*;
import static team051.Message.*;

public class Debug {
	
	static void debug_dbs(String s){
		debS+=s;
	}

	static int debug_countMap(){
		int count=0;
		for(int x=0;x<200;x++){
			for(int j=0;j<200;j++){
				if(map[x][j]!=null){
					count++;
				}
			}
		}
		return count;
	}
}
