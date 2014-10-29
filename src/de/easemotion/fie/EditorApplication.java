package de.easemotion.fie;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FillPane;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.Window;

import de.easemotion.fie.model.graphics.GraphicSurface;
import de.easemotion.fie.view.GraphicPanel;
import de.easemotion.fie.view.GraphicPanelContainer;
import de.easemotion.fie.view.InstrumentNamePanel;
import de.easemotion.fie.view.LayerSetupPanel;
import de.easemotion.fie.view.LuaEditorPanel;
import de.easemotion.fie.view.PropertyPanel;

public class EditorApplication implements Application {
	
	private static final String TAG = EditorApplication.class.getSimpleName();
	
	public Window window = null;
	
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
        BoxPane leftContainer = (BoxPane) bxmlSerializer.getNamespace().get("editor_left_column");
        BoxPane rightContainer = (BoxPane) bxmlSerializer.getNamespace().get("editor_right_column");
        
        // add panel in left column
        LayerSetupPanel layerPanel = new LayerSetupPanel(this, graphicSurface);
        graphicSurface.addObserver(layerPanel);
        leftContainer.add(layerPanel);

        LuaEditorPanel luaPanel = new LuaEditorPanel(this, graphicSurface);
        graphicSurface.addObserver(luaPanel);
        leftContainer.add(luaPanel);

        // add panel in right column
        InstrumentNamePanel instrumentNamePanel = new InstrumentNamePanel(this, graphicSurface);
        graphicSurface.addObserver(instrumentNamePanel);
        rightContainer.add(instrumentNamePanel);
        
        PropertyPanel propertyPanel = new PropertyPanel(this, graphicSurface);
        graphicSurface.addObserver(propertyPanel);
        rightContainer.add(propertyPanel);
        
        GraphicPanelContainer graphicPanelContainer = new GraphicPanelContainer(this, graphicSurface);
        graphicSurface.addObserver(graphicPanelContainer);
        rightContainer.add(graphicPanelContainer);
        
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
}
