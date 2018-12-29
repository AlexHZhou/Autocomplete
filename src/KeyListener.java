import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

//TODO: KNOWN PROBLEMS
//shift needs to be fixed
//decreasing occurance rates
//commands-do no accept input right after command (bc order of operations?) may be nasty to fix.
//how bout that UI.
//make special keywords better.
//add option to do most recent rather than most occured.
public class KeyListener implements NativeKeyListener{
	//replaced this with a stack or a hashmap
	static ArrayList<Entry> trackingList = new ArrayList<Entry>();
	static ArrayList<Entry> suggestions = new ArrayList<Entry>();
	FileManager fileManager = new FileManager();
	ReccoPanel reccoPanel = new ReccoPanel();
	Robot robot;
	
	int code = -1; 
	int keyNativeName = -1;
	
	boolean programPaused = false;
	boolean shiftPressed = false;
	boolean controlPressed = false;
	boolean altPressed = false;
	boolean tabPressed = true;	
	
	String encodedWord = "";
	StringBuilder wordText = new StringBuilder();
	int wordLength = 0; 
	String oldWord = "";

	boolean exiting = false;
	
	Command[] commands = new Command[] {
			new Command("acpause", " program paused", 7),
			new Command("acresume", " program resuming", 8),
			new Command("acreset", " program data reset", 7)
			
	};
	
	public KeyListener() {}
	public static void initKeyboardData(ArrayList<Entry> list) {
		trackingList = list;
	}
	
	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		if (exiting) return;
		//not sure if I actually need this. too lazy to check rn.
	
		keyNativeName = e.getKeyCode();
		code = e.getRawCode();
		System.out.println("native " + keyNativeName);
		System.out.println("code " + code);
		System.out.println("----");
		checkSpecialKeys(code);
		
		
		//for deleting. 
		if (keyNativeName == NativeKeyEvent.VC_BACKSPACE){
			wordLength--;
			if (wordText.length() > 0) wordText.deleteCharAt(wordText.length() - 1);
			encodedWord = oldWord;
			
			//note: does not cover selecting group of chars to delete,
			//or moving pointer left/right.
		}
	
		//Word is finished, so needs to be entered. Triggers on space and period.
		else if (keyNativeName == NativeKeyEvent.VC_SPACE 
				|| keyNativeName == NativeKeyEvent.VC_PERIOD){
			wordFinished();
		}
		
		
		//if not a special case (esc/space/tab), append letter to current word.
		//covers basic typing
		//NOT an 'else if...' because then you will never get out of acpause
		if (code >= 65 && code <= 90){//a = 65, z = 90. Only letters are recorded		
			wordText.append(NativeKeyEvent.getKeyText(keyNativeName));
			System.out.println("NativeKeyEvent: " + NativeKeyEvent.getKeyText(keyNativeName));
			System.out.println("KeyEvent: " + KeyEvent.getKeyText(code));
			
			oldWord = encodedWord;
			encodedWord += code + ".";
			wordLength++;
			
			updateSuggestionsList();
			Entry mostOcc = getMostOccured();
			
			if (mostOcc != null) {
				System.out.println("Debuggy " + getMostOccured().Word());
				String reccoWord = mostOcc.Word();
				reccoPanel.UpdateText(reccoWord);
			} else {
				reccoPanel.UpdateText("(No recommendations)");
			}
			//TODO: capitalization is hella wonky so is outright ignored for the time being.
		}
		System.out.println("cur: " + wordText + code);

		checkSpecialCommands();
		
		for (int i = 0; i < commands.length; i++) {
			if (wordText.toString().equalsIgnoreCase(commands[i].getCommand())) resetWord();
		}
		
	}
	
	//checks for control, shift, alt, and escape.
	//only escape has a function right now.
	public void checkSpecialKeys(int c) {
		//managing states of special case keys.
		System.out.println("cur code is " + c);
		System.out.println("attempt to match " + KeyEvent.VK_CONTROL);
						
		if (c == KeyEvent.VK_CONTROL){
			controlPressed = true;
		}else if (c == KeyEvent.VK_SHIFT
				){
			System.out.println("Shift is being held");
			shiftPressed = true;
		} else if (c == KeyEvent.VK_ALT){
			altPressed = true;
		}else if (c == KeyEvent.VK_ESCAPE) {
			try {
				exiting = true;
				printMessage("exited autocomplete program ", true, 0);
				
				fileManager.write(trackingList); 
				GlobalScreen.unregisterNativeHook();

				System.exit(0);
			} catch (NativeHookException e1) {
				e1.printStackTrace();
			}  catch (IllegalArgumentException e1){
				System.err.println("You should probably fix this");
				e1.printStackTrace();
			}
		}
	}
	public void wordFinished() {
		if (encodedWord.equals("") || wordLength < 3){
			resetWord();
			System.out.println("Word too short (" + wordLength + "). Not added to trackingList.");
			suggestions = new ArrayList<Entry>(trackingList);
			return;
		}
		//adds a completed word to trackinglist, or increments if already there.
		boolean alreadyRecorded = false;
		for (int i =  0; i < trackingList.size(); i++){
			String existingWord = trackingList.get(i).wordCode();
			if (encodedWord.equals(existingWord)){
				trackingList.get(i).incrementOccurance();
				System.out.println("The word " + encodedWord + " has been recorded " + trackingList.get(i).occurance() + " times!");
				System.out.println();
				alreadyRecorded = true;
				break;
			}
		}
		if (!alreadyRecorded && trackingList.size() < 42){
			trackingList.add(new Entry(wordText.toString(), encodedWord));
			System.out.println("Entered a new word " + encodedWord + " into tracking list!");
			System.out.println("(Tracking list size: " + trackingList.size() +")");
			System.out.println();
		}
		resetWord();
		suggestions = new ArrayList<Entry>();
		System.out.println("Suggests list reset; suggestions list size now: " + suggestions.size());	
		return;
	}
	
	public Entry getMostOccured(){
		if (suggestions.size() == 0) {
			//System.out.println("Suggestions list is empty");
			return null; //cannot return null because i'll get funny nullpointer errors 
		}
		
		//System.out.println("Checking for most used word...");
		int most = -1;
		int indexOfMost = -1;
		for (int i = 0; i < suggestions.size(); i++){
			if (suggestions.get(i).occurance() > most){
				indexOfMost = i;
				most = suggestions.get(i).occurance();
			}
		}
		if (indexOfMost == -1) return null;
		else return suggestions.get(indexOfMost);
	}

	public void checkSpecialCommands() {
		
		int index = -1;
		for (int i = 0; i < commands.length; i++) {
			if (wordText.toString().equalsIgnoreCase(commands[i].getCommand())) index = i;
		}
		if (index == -1) {
			return;
		}
		else {
			System.out.println("KEYWORD YEHAW" );
			Command c = commands[index];
			printMessage(c.getMessage(), true, c.getMLength());
		}
		
		if (index == 0) { //acpause
			programPaused = true;
		}
		else if (index == 1){ //acresume
			programPaused = false; 
		}
		else if (index == 2) { //acreset
			ArrayList<Entry> emptylol = new ArrayList<>();
			AutoCompleter.fileManagement.write(emptylol);
			printMessage("ac data files reset ", true, 11); 
			
			
			System.out.println("Data files reset.");
		
		} 
		resetWord();
	}

	
	public void updateSuggestionsList(){
		//update suggestions list
		if (keyNativeName == NativeKeyEvent.VC_TAB) return;
		boolean loop = true;
		int i = 0;
		while (loop){
			if (suggestions.size() == 0 || wordLength < 1) break;
			

			if (wordLength >= suggestions.get(i).characters.size()) {
				suggestions.remove(i);
				continue;
			}
			
			System.out.println("suggestions updating: " + wordLength + " vs " + suggestions.get(i).characters.size());
			int comparator = Integer.parseInt(suggestions.get(i).characters.get(wordLength));
			
			if (comparator != code){
				//System.out.println("Suggestions- removed " + suggestions.get(i).outputEntry());
				suggestions.remove(i);
				
				//cannot use a basic foor loop, or hard to, because array size changes which
				//messes with the for (... i < array.Size() ...)
			} else{
				i++;
			}
			
			if (i == suggestions.size()) loop = false;
		}
	}
	
	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		//could this cause a problem? I hope not...
		keyNativeName = e.getKeyCode();
		code = e.getRawCode();
		
		if (keyNativeName == NativeKeyEvent.VC_ESCAPE){
			
		}else if (keyNativeName == NativeKeyEvent.VC_SHIFT){
			shiftPressed = false;
		}else if (keyNativeName == NativeKeyEvent.VC_CONTROL){
			//System.out.println("Control no longer being pressed.");
			controlPressed = false;
		} else if (keyNativeName == NativeKeyEvent.VC_ALT){
			//System.out.println("Alt no longer being pressed.");
			altPressed = false;
		}else if (keyNativeName == NativeKeyEvent.VC_TAB){
			tabPressed = false;
			//System.out.println("Tab Pressed: " + tabPressed);
		} 
		
		if (wordLength > 0
				&& keyNativeName == NativeKeyEvent.VC_TAB
				&& !programPaused){
			
			System.out.println("Attempting to autocomplete. \n"
					+ "Word length = " + wordLength
					+ "Suggestions length " + suggestions.size());
			if (wordLength != 0) autocompleteWord();
		}
		
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {
		
	}
	
	public void updateList(ArrayList<Entry> data){
		trackingList = new ArrayList<Entry> (data);
		suggestions = new ArrayList<Entry>(data);
		//IMPORTANT: do not do suggestions = data. Apparently for lists,
		//this is recorded as a pointer, so suggestions -> trackingList and they are linked
		//which is a big no go. This starts them as separate arrays.
		
//		for (int i = 0; i < data.size(); i++){
//			for (int j = 0; j < trackingList.get(i).characters.size(); j++){
//				String output = trackingList.get(i).characters.get(j);
//				if (output == null) break;
//			}
//		}
		System.out.println("-----------------------------------------");
	}
	public void autocompleteWord(){
		Entry recommended = getMostOccured();
		if (recommended == null ||
				programPaused) return;
		System.out.println("Autocompleting for " + recommended.toString());
		
		robot.keyPress(8); //8 == backspace to cancel out the tab.
		robot.keyRelease(8);
		
		//when tab is pressed the letter is was presssed on is noo included.
		oldWord = encodedWord;
		encodedWord += code + ".";
		//currentWordText.append(recommended.characters.get(wordLength -1));
		//wordLength++;
		//not sure why, but keeping the wordlegnth knocks of the below for loop.
		
		System.out.println("Running autoComplete code");
		for (int i = wordLength; i < recommended.characters.size(); i++){
			//robot.keyPress(NativeKeyEvent.getKeyText(Integer.parseInt(recommended.characters[i])));
			try{
			Integer code = Integer.parseInt(recommended.characters.get(i));
			robot.keyPress(code);
			robot.keyRelease(code); //need this, or the code will
			//not be able to write the same character twice (aaaa will not
			//be printed, because if the first 'a' will be pressed down and
			//not release
		
			//System.out.println("Robot printed " + code);
			} catch (NumberFormatException nfe){
				System.out.println("NumberFormatException Error");
			} catch (Exception f){
				System.out.println("Error with Robot printing.");
			}	
		}
		robot.keyPress(32);
		robot.keyRelease(32);
	} 	
	public void printMessage(String s, boolean delete, int extraDelete) {
		long pause = 600;
		
		//extradelete is the length of the command itself, so it also deletes
		//along with the message, (if enabled).
		int[] exitMessage = StringToKeyCodes(s);
		for (int i : exitMessage) {
			robot.keyPress(i);
			robot.keyRelease(i);
		}
		 
		if (!delete) return;
		
		robot.delay((int) pause); //waits for one second, then deletes all the stuff it just typed.
		
		for (int i = 0; i < exitMessage.length + extraDelete; i++) {//+7 is to delete the command too.
			robot.keyPress(8);
			robot.keyRelease(8);
		}
		
		//need this last step here to reset the word
		//was having problems just doing normal reset, so just
		//pressing space and deleting it fixes it up.
//		robot.keyPress(32);
//		robot.keyRelease(32);
//		robot.keyPress(8);
//		robot.keyRelease(8);
		
    	System.err.println("Word reset after ac command");
		resetWord();
		System.err.println("Cur: " + wordText);
	
		//pretty sure this is bugged because robot is on a delay and this is not.
		
		
	}
	public int[] StringToKeyCodes(String s){
		String[] sLength = s.split("");
		int[] output = new int[sLength.length];
		for (int i = 0; i < sLength.length; i++){
			output[i] = CharToRawKeyCode(sLength[i]);
		}
		
		return output;
	}
	
	public String CodesToString(String s) {
		if (s.equals(null)) return null;
		String output = "";
		String[] temp = s.split(":"); //words are split by char.char.char:occurance
		String[] split = temp[0].split(Pattern.quote("."));
		try {
			for (String i : split) {
				output += NativeKeyEvent.getKeyText(Integer.parseInt(i));
			}
		} catch (Exception e) {
			System.err.println("Error in method 'CodesToString' when converting from string to number.");
			e.printStackTrace();
		}
		
		return output;
	}
	
	public int CharToRawKeyCode(String s){
		switch (s){
		case "a":
			return 65;
		case "b":
			return 66;
		case "c":
			return 67;
		case "d": 
			return 68;
		case "e":
			return 69;
		case "f":
			return 70;
		case "g":
			return 71;
		case "h": 
			return 72;
		case "i":
			return 73;
		case "j":
			return 74;
		case "k": 
			return 75;
		case "l":
			return 76;
		case "m":
			return 77;
		case "n": 
			return 78;
		case "o":
			return 79;
		case "p": 
			return 80;
		case "q": 
			return 81;
		case "r": 
			return 82;
		case "s": 
			return 83;
		case "t": 
			return 84;
		case "u": 
			return 85;
		case "v": 
			return 86;
		case "w": 
			return 87;
		case "x": 
			return 88;
		case "y": 
			return 89;
		case "z": 
			return 90;
		case ".": //idk why, but the period has problems. tested in other program, and its
			//not a problem to do with matching the string to the .
			return 190;
		case " ":
			return 32;
		//other common punctuation require shift...which doesn't work as of 5/3/18
		default: 
			System.err.println("Something went wrong in the CharToRawKeyCode method!");
			return 32; //defaults to spacebar 
		}
			
	}

	public void resetWord() {
		//System.err.println("word tracker reset");
		encodedWord = "";
		wordText.setLength(0);
		oldWord = "";
		wordLength = 0;
	}
}
