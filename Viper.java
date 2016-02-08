package team051;


import battlecode.common.*;
import static team051.Common.*;
import static team051.Message.*;
import static team051.Move.*;

public class Viper{
	

	public static void run() throws GameActionException{
		
		attack();
		
		if(destination!=null && !moveToDestination()){
			destination=null;
		}
		
		
	}

}