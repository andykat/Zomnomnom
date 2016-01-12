package communicatingPlayer;

public interface MessageConstants {
	//only 31 bit per integer because largest number for an int is 1 followed by 31 zeros
	//First four bytes of the integer then is reserved, and last byte is also reserved, the middle 59 bytes are free to use
	//Message type ranges from 0 ~7, currently 0~1 is occupied
	//To create a new message type: 1) add in the distribution mapping here, add decrypting and createMessage public methods to retrieve
	
	public int[] charMesDistr= {11,7,7,15,5,7,7}; //Character message bit distribution
	public int[] mapMesDistr= {11,7,10,7,15,2,7};
}
