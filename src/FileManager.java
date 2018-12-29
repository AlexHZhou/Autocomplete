import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileManager {
	private static final String FILENAME = "data.txt";
	
	public static ArrayList<Entry> IOData = new ArrayList<Entry>();
	public static int IODataIndex = 0;
	
	public FileManager(){}
	public FileManager(ArrayList<Entry> input){
		IOData = input;
	}
	
	public ArrayList<Entry> getData(){
		return IOData;
	}

	public void read(){
		BufferedReader br = null;
		FileReader fr = null;
		
		try{
			System.out.println("Starting reading...");
			fr = new FileReader(FILENAME);
			br = new BufferedReader(fr);
			
			String currentLine = "";
			
			while ((currentLine = br.readLine()) != null){
				String[] s = currentLine.split(":"); //: to split actualWord:WordCodes:occurance
				//String[] s = currentLine.split(".|\\;");
				//so apparently you can do some fancy splitting with multiple 
				//delimiters. In this case, I have "." and ";"
				//the "|" is "or", the "\\" is for multiple delimiters (I think?)
				//unfortunately, I don't need to do this.
				String word = s[0];
				String wordCode = "";
				String occurance = s[s.length - 1]; //the last digit stored is frequency
				
				for (int i = 0; i < s.length - 1; i++){
					wordCode += s[i];
				}
				IOData.add(new Entry(word, wordCode, Integer.parseInt(occurance)));
				IODataIndex++;
			}
			System.out.println("Finished reading.");
			System.out.println("-----------------------------------------");
			
			KeyListener.initKeyboardData(IOData);
		 
		} catch (Exception e){
			e.printStackTrace();
		}finally {
			try {
				if (br != null)
					br.close();
				if (fr != null)
					fr.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
	}
	
	public void write(ArrayList<Entry> trackingList){
		BufferedWriter bw = null;
		FileWriter fw = null;
		
		try{
			System.out.println("Printing output files:");
			fw = new FileWriter(FILENAME);
			bw = new BufferedWriter(fw);
			
			for (int i = 0; i < trackingList.size(); i++){

				bw.write(trackingList.get(i).Word() + ":");
				
				String out = trackingList.get(i).toString();
				System.out.print(out);
				bw.write(out);
			
			}
			System.out.println("Finished writing");
		} catch(Exception e){
			e.printStackTrace();
		}finally {
			try {
				if (bw != null)
					bw.close();
				if (fw != null)
					fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
	}
	

}
