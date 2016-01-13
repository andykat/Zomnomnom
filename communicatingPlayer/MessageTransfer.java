package communicatingPlayer;

import java.util.Arrays;
import java.util.Vector;

public class MessageTransfer implements MessageConstants{
	
	public static Vector<Integer> decryptMessage(int[] receivedMessage){
		//Find message id
		Vector<Integer> answer= new Vector<Integer>();
		
		if (receivedMessage.length== 2){
			String firstPart= Integer.toBinaryString(receivedMessage[0]);
			firstPart= padLeft(firstPart, Integer.BYTES*8);
			
			String secondPart= Integer.toBinaryString(receivedMessage[1]);
			secondPart= padLeft(secondPart, Integer.BYTES*8);
			
			String binary= firstPart+secondPart;
			int messageType= Integer.parseInt(binary.substring(1, 4),2);
			//p("binary: "+ binary);
			//p("Message type: "+messageType);
			
			if (binary.charAt(binary.length()-1)== '1'){ //If the replacer indicator is 1
				char[] temp = binary.toCharArray();
				temp[32] = '1';
				binary = String.valueOf(temp);
			}
			int startNum= 4;
			
			if (messageType== 0){ //CHARACTER MESSAGE
				for (int n= 0; n< MessageConstants.charMesDistr.length; n++){
					//System.out.println("\tcharacterType");
					answer.add(getPart(binary,startNum, startNum+MessageConstants.charMesDistr[n]));
					startNum+= MessageConstants.charMesDistr[n];
				}
			}else if (messageType== 1){ //MAP MESSAGE
				//System.out.println("\tmapType");
				for (int n= 0; n< MessageConstants.mapMesDistr.length; n++){
					answer.add(getPart(binary,startNum, startNum+MessageConstants.mapMesDistr[n]));
					startNum+= MessageConstants.mapMesDistr[n];
				}				
			}
		}	
		
		return answer;
	}
	
	public static int[] createMessage(int idType, int[] message){//Returns -1 on failure
		int[] answer= new int[2];
		if (idType== 0){ //Character message
			int[] distribution= MessageConstants.charMesDistr;
			answer = createMessage(idType, distribution, message);
		}else if (idType== 1){
			int[] distibution= MessageConstants.mapMesDistr;
			answer= createMessage(idType, distibution, message);
		}
		
		return answer;
	}
	
	private static int getPart(String binary, int startIndex, int endIndex){
		return Integer.parseInt(binary.substring(startIndex,endIndex),2);
	}
	
	private static int[] createMessage(int idType, int[] distribution, int[]message){
		//Assemble the message together, appending zeros when needed
		int[] answer= new int[2];
		String preConvert= checkInputArray(distribution, message);
		if (preConvert!=""){
			preConvert= padLeft(Integer.toBinaryString(idType),4)+ preConvert; //The idType in binary padded to the left + 1 spacing for less than integer concerns
			
			boolean oneNeededAtStartOfSecondPart= preConvert.charAt(32)=='1';
			if (oneNeededAtStartOfSecondPart){
				preConvert= padRight(preConvert, Integer.BYTES*8*2, '1');
			}else
				preConvert= padRight(preConvert, Integer.BYTES*8*2, '0');
			
			//SUBSTRING START INDEX INCLUSIVE, END INDEX EXCLUSIVE http://stackoverflow.com/questions/4570037/java-substring-index-range
			
			
			int firstPart= Integer.parseInt(preConvert.substring(0,Integer.BYTES*8),2); //[0~32) first integer
			int secondPart= Integer.parseInt(preConvert.substring(Integer.BYTES*8+1).trim(),2); //[33~64) second integer ; very first is always zero, making it always smaller than an integer
			
			answer[0]= firstPart;
			answer[1]= secondPart;
			//p("second part: "+ Integer.toBinaryString(firstPart));
			//p("first part:" + Integer.toBinaryString(secondPart));
			//p(preConvert);
		}else{
			System.out.println("Message check failed against proposed division");
		}
		return answer;
	}
	
	private static String checkInputArray(int[] distribution, int[] message){//Check if length of each part is <= to the actual part, padding zeros when smaller
		String answer= "";
		
		if (distribution!= null && message!= null){
			if (distribution.length== message.length){
				for (int n= 0; n< distribution.length; n++){
					String binary= Integer.toBinaryString(message[n]);
					if (binary.length()<= distribution[n]){ //If the length is less than or equal to proposed size
						if (binary.length() == distribution[n]){
							answer+= binary;
						}else{
							answer+= padLeft(binary, distribution[n]);
						}
					}
				}
			}
		}
		return answer; //Returns a string representing the correctly padded numbers on success, else returns ""
	}
	
	private static String padLeft(String s, int totalLength) {
		//http://stackoverflow.com/questions/4051887/how-to-format-a-java-string-with-leading-zero
	     if (s.length() >= totalLength) return s;
	     else return String.format("%0" + (totalLength-s.length()) + "d%s", 0, s);
	}
	
	private static String padRight(String s, int totalLength, char c) {
		if (s.length()>= totalLength) return s;
		else return String.format("%-"+totalLength+"s", s).replace(' ', c);
	}
}

