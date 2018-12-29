
public class Command {
	private String command;
	private String outputMessage;
	private int messageLength;
	
	public Command() {}
	public Command(String c, String oM, int mL) {
		command = c;
		outputMessage = oM;
		messageLength = mL;
	}
	
	public String getCommand() { return command; }
	public String getMessage() { return outputMessage; }
	public int getMLength() { return messageLength; }
}
