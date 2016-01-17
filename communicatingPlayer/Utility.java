package communicatingPlayer;

import java.util.Random;

import battlecode.common.Direction;

public class Utility {
	private static Random randall= new Random();
	public static int clamp(int val, int min, int max) {
	    return Math.max(min, Math.min(max, val));
	}
	
//	public static void rotateDirection(Direction[] arr, int order) {
//	    if (arr == null || order < 0) {
//	        throw new IllegalArgumentException("The array must be non-null and the order must be non-negative");
//	    }
//	    int offset = arr.length - order % arr.length;
//	    if (offset > 0) {
//	    	Direction[] copy = arr.clone();
//	        for (int i = 0; i < arr.length; ++i) {
//	            int j = (i + offset) % arr.length;
//	            arr[i] = copy[j];
//	        }
//	    }
//	}
	
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
