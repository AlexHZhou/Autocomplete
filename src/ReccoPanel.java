import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JTextField;

public class ReccoPanel {
	static JFrame ui = new JFrame();
	static JTextField text = new JTextField();
	static Font f = text.getFont();
	static Font nF = f.deriveFont(20f);
	
	ReccoPanel(){
		ui.setUndecorated(true);
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ui.setVisible(true);
		ui.setAlwaysOnTop(true);
		ui.setAutoRequestFocus(false);
		ui.setFocusable(false);
		ui.setEnabled(false);
		//pack is autosize to component size

		//this stuff probably doesn't work for multiple screens.
		int screenW = GetScreenWorkingWidth()-60;
		int screenHAdjusted = GetScreenWorkingHeight()-60;
		ui.setLocation(20, screenHAdjusted);
		
		UpdateText("ReccoPanel init");
	}
	
	public void UpdateText(String s) {
		if (s.equals(null)) return;
		
		text.setFont(nF);
		text.setText(s);
		ui.add(text);
		ui.pack();
	}
	
	private static int GetScreenWorkingWidth() {
	    return java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
	}

	private static int GetScreenWorkingHeight() {
	    return java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
	}
}
