import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import org.jnativehook.mouse.NativeMouseMotionListener;
import org.jnativehook.mouse.NativeMouseWheelEvent;
import org.jnativehook.mouse.NativeMouseWheelListener;

public class MouseListener implements NativeMouseListener, NativeMouseMotionListener, NativeMouseWheelListener{

	@Override
	public void nativeMouseClicked(NativeMouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nativeMousePressed(NativeMouseEvent e) {
		// TODO Auto-generated method stub
		//System.out.println("MOUSE CLICKED: " + e.getX() + ", " + e.getY());
	}

	@Override
	public void nativeMouseReleased(NativeMouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nativeMouseDragged(NativeMouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nativeMouseMoved(NativeMouseEvent e) {
		//System.out.println("Mouse loc: " + e.getX() + ", " + e.getY());
		
	}

	@Override
	public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
		// TODO Auto-generated method stub
		
	}

}
