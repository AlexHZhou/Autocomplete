import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

public class AutoCompleter {
	public static FileManager fileManagement = new FileManager();
	static KeyListener keyboard = new KeyListener();
	static MouseListener mouse = new MouseListener();
	
	public static void main(String[] args) {
		fileManagement.read();
		
		// Get the logger for "org.jnativehook" and set the level to warning.
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.WARNING);

		// Don't forget to disable the parent handlers.
		logger.setUseParentHandlers(false);
		
		try {
			GlobalScreen.registerNativeHook();
			 // Get the Java runtime
	        Runtime runtime = Runtime.getRuntime();
	        // Run the garbage collector
	        runtime.gc();
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());

			System.exit(1);
		}
		GlobalScreen.addNativeKeyListener(keyboard);
	}	
}
