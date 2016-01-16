package communicatingPlayer;

import java.util.Random;

import battlecode.common.Direction;

public class Utility {
	private static Random randall= new Random();
	public static int clamp(int val, int min, int max) {
	    return Math.max(min, Math.min(max, val));
	}
	
	public static void shuffleDirArray(Direction[] ar){
	    for (int i = ar.length - 1; i > 0; i--)
	    {
	      int index = randall.nextInt(i + 1);
	      // Simple swap
	      Direction a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	}
}
