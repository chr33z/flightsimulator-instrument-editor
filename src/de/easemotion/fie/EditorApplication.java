package de.easemotion.fie;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.ComponentMouseListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FillPane;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.Mouse.Button;

import de.easemotion.fie.model.graphics.GraphicSurface;
import de.easemotion.fie.view.GraphicPanel;

public class EditorApplication implements Application {
	
	private static final String TAG = EditorApplication.class.getSimpleName();
	
	private Window window = null;
	
	public GraphicSurface graphicSurface;
	
	public static void main(String[] args) {
	    DesktopApplicationContext.main(EditorApplication.class, args);
	}

	@Override
	public void startup(Display display, Map<String, String> properties)
			throws Exception {
		
		window = new Window();
		
		graphicSurface = GraphicSurface.getInstance();
		 
		BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(EditorApplication.class, "window.bxml");
        bxmlSerializer.bind(this, EditorApplication.class);
        
        FillPane graphics = (FillPane) bxmlSerializer.getNamespace().get("editor_graphic_container");
        
        GraphicPanel graphicPanel = new GraphicPanel(graphicSurface);
        graphics.add(graphicPanel);
        
        window.open(display);
		
	}
	
	@Override
	public void resume() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean shutdown(boolean arg0) throws Exception {
		if (window != null) {
			window.close();
		}
		
		return false;
	}
	
	@Override
	public void suspend() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Call when a right click on the graphic panel is performed
	 */
	private void onGraphicPanelContextMenu(){
		
	}
}
