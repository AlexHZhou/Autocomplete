import java.util.ArrayList;
import java.util.regex.Pattern;

public class Entry {
	private String wordCode = "";
	private String word = "";
	private int occurance = -1;
	public ArrayList<String> characters = new ArrayList<String>(); // i doubt that I'll go over 10.
	public Entry(){}
	
	public Entry(String w, String wC){
		System.out.print("Making new Entry, word: " + w +" code: " + wC);
		wordCode = wC;
		word = w;
		occurance = 1;
		String[] temp = wC.split(Pattern.quote("."));
		//FUN FACT: apparently splitting by periods is wonky, so s.split("." does not work.
		//and apparently, splitting by s.split("\\.") where the \\ indicates a special character,
		//only sometimes works (works for me, but Pattern.quote is safest method).
		
		System.out.print("Character List: " );
		for (int i = 0; i < temp.length; i++){
			characters.add(temp[i]);// characters really are just the key codes
		}
		System.out.println();
	}
	//same as above, but for multiple occurances. Mostly used in IO
	public Entry(String w, String wC, int o){
		System.out.print("Making new Entry, word: " + w +" code: " + wC);
		wordCode = wC;
		word = w;
		occurance = o;
		String[] temp = wC.split(Pattern.quote("."));
		//FUN FACT: apparently splitting by periods is wonky, so s.split("." does not work.
		//and apparently, splitting by s.split("\\.") where the \\ indicates a special character,
		//only sometimes works (works for me, but Pattern.quote is safest method).
		
		System.out.print("Character List: " );
		for (int i = 0; i < temp.length; i++){
			characters.add(temp[i]);// characters really are just the key codes
			System.out.print("added " + characters.get(i) + ", ");
		}
		System.out.println();
	}
	
	public String wordCode(){
		return this.wordCode;
	}
	public void setWordCode(String value){
		this.wordCode = value;
	}

	public String Word() {
		return this.word;
	}
	public void setWord(String s) {
		this.word = s;
	}
	
	public int occurance(){
		return this.occurance;
	}
	public void setOccurance(int value){
		this.occurance = value;
	}
	public void incrementOccurance(){
		this.occurance++;
	}
	
//	public String[] Characters(){
//		return characters;
//	}
	@Override
	public String toString(){
		String s =  wordCode + ":" + occurance + "\n";
		if (s != ":-1\n") return s; //blank entry
		else return null;
	}
	
}
