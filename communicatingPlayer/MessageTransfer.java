package communicatingPlayer;

import java.util.Arrays;
import java.util.Vector;

public class MessageTransfer implements MessageKeys{
	
	public boolean bigMapTest(){ //Takes a long time, be careful
		boolean answer= true;
		int completedTests= 0;
		outerloop:
		for (int turn= 0; turn< 2048; turn++){
			for (int rubble= 0; rubble< 99; rubble++){
				for (int parts= 0; parts< 999; parts++){
					for (int denHealth= 0; denHealth< 99; denHealth++){
						for (int nid= 0; nid< 32000; nid++){
							for (int ntype=0; ntype< 4; ntype++){
								for (int nhealth= 0; nhealth< 128; nhealth++){
									int[] message={turn,rubble,parts,denHealth,nid,ntype,nhealth};
									boolean test= test(1,message);
									if (!test){
										answer= false;
										break outerloop;
									}else{
										completedTests+= 1;
										System.out.println(Arrays.toString(message));
									}
								}
							}
						}
					}
				}
			}
			
		}
		
		return answer;
	}
	
	
	public boolean bigCharacterTest(){
		boolean answer= true;
		int completedTests= 0;
		outerloop:
		for (int turn= 0; turn< 2048; turn++){
			for (int x= 0; x< 100; x++){
				for (int y= 0; y< 100; y++){
					for(int id= 0; id< 32768; id++ ){
						for (int charType= 0; charType< 16; charType++){
							for (int healthPerc= 0; healthPerc< 128; healthPerc++){
								for (int infectionCount= 0; infectionCount< 128; infectionCount++){
									int[] message={turn,x,y,id,charType,healthPerc,infectionCount};
									boolean test= test(0,message);
									if (!test){
										answer= false;
										break outerloop;
									}else{
										completedTests+= 1;
										System.out.println(completedTests);
									}
								}
							}
						}
					}
				}
			}
		}
		
		return answer;
	}
	
	public boolean test(int messageType, int[] message){
		boolean answer= true;
		
		int[] condensedMessage= createMessage(messageType,message);
		
		Vector<Integer> check= decryptMessage(condensedMessage);
		
		if (message.length== check.size()){
			for (int n= 0; n< check.size(); n++){
				if (message[n]!= check.get(n)){
					answer= false;
					break;
				}
			}
		}
		
		return answer;
	}
	
	public Vector<Integer> decryptMessage(int[] receivedMessage){
		//Find message id
		Vector<Integer> answer= new Vector<Integer>();
		
		if (receivedMessage.length== 2){
			String firstPart= Integer.toBinaryString(receivedMessage[0]);
			firstPart= padLeft(firstPart, Integer.BYTES*8);
			
			String secondPart= Integer.toBinaryString(receivedMessage[1]);
			secondPart= padLeft(secondPart, Integer.BYTES*8);
			
			String binary= firstPart+secondPart;
			int messageType= Integer.parseInt(binary.substring(1, 4),2);
			p("binary: "+ binary);
			p("Message type: "+messageType);
			
			if (binary.charAt(binary.length()-1)== '1'){ //If the replacer indicator is 1
				char[] temp = binary.toCharArray();
				temp[32] = '1';
				binary = String.valueOf(temp);
			}
			//p("binary size: "+binary.length());
			int startNum= 4;
			
			if (messageType== 0){//========================================================================================================Character message
				for (int n= 0; n< MessageKeys.charMesDistr.length; n++){
					//p(getPart(binary,startNum, startNum+MessageKeys.charMesDistr[n]));
					System.out.println("\tcharacterType");
					answer.add(getPart(binary,startNum, startNum+MessageKeys.charMesDistr[n]));
					startNum+= MessageKeys.charMesDistr[n];
				}
			}else if (messageType== 1){
				System.out.println("\tmapType");
				for (int n= 0; n< MessageKeys.mapMesDistr.length; n++){
					//p(getPart(binary,startNum, startNum+MessageKeys.charMesDistr[n]));
					answer.add(getPart(binary,startNum, startNum+MessageKeys.mapMesDistr[n]));
					startNum+= MessageKeys.mapMesDistr[n];
				}				
			}
		}	
		
		return answer;
	}
	
	public int getPart(String binary, int startIndex, int endIndex){
		return Integer.parseInt(binary.substring(startIndex,endIndex),2);
	}
	
	public int[] createMessage(int idType, int[] message){//Returns -1 on failure
		int[] answer= new int[2];
		if (idType== 0){ //Character message
			int[] distribution= MessageKeys.charMesDistr;
			answer = createMessage(idType, distribution, message);
		}else if (idType== 1){
			int[] distibution= MessageKeys.mapMesDistr;
			answer= createMessage(idType, distibution, message);
		}
		
		return answer;
	}
	
	private int[] createMessage(int idType, int[] distribution, int[]message){
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
			p(preConvert);
		}else{
			System.out.println("Message check failed against proposed division");
		}
		return answer;
	}
	
	private String checkInputArray(int[] distribution, int[] message){//Check if length of each part is <= to the actual part, padding zeros when smaller
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
	
	public String padLeft(String s, int totalLength) {
		//http://stackoverflow.com/questions/4051887/how-to-format-a-java-string-with-leading-zero
	     if (s.length() >= totalLength) return s;
	     else return String.format("%0" + (totalLength-s.length()) + "d%s", 0, s);
	}
	
	public String padRight(String s, int totalLength, char c) {
		if (s.length()>= totalLength) return s;
		else return String.format("%-"+totalLength+"s", s).replace(' ', c);
	}
	
	public void p(String s){
		System.out.println(s);
	}
	
	public void p(int s){
		System.out.println(s);
	}
}
