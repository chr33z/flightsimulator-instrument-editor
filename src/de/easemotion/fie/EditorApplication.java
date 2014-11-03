package de.easemotion.fie;

import java.io.File;

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

import de.easemotion.fie.model.graphics.Instrument;
import de.easemotion.fie.utils.IconLoader;
import de.easemotion.fie.view.EncoderSetupPanel;
import de.easemotion.fie.view.GraphicPanel;
import de.easemotion.fie.view.GraphicPanelContainer;
import de.easemotion.fie.view.InstrumentNamePanel;
import de.easemotion.fie.view.LayerSetupPanel;
import de.easemotion.fie.view.LuaEditorPanel;
import de.easemotion.fie.view.MenuPanel;
import de.easemotion.fie.view.PropertyPanel;

public class EditorApplication implements Application {
	
	private static final String TAG = EditorApplication.class.getSimpleName();
	
	public Window window = null;
	
	public Instrument instrument;
	
	public static File lastFileBrowserPath = null;
	
	LayerSetupPanel layerPanel;
	LuaEditorPanel luaPanel;
	InstrumentNamePanel instrumentNamePanel;
	PropertyPanel propertyPanel;
	GraphicPanelContainer graphicPanelContainer;
	EncoderSetupPanel encoderSetupPanel;
	MenuPanel menuPanel;
	
	public static void main(String[] args) {
	    DesktopApplicationContext.main(EditorApplication.class, args);
	}

	@Override
	public void startup(Display display, Map<String, String> properties)
			throws Exception {
		
		window = new Window();
		window.setPreferredSize(1000, 800);

		IconLoader.loadIcons();
		
		instrument = Instrument.getInstance();
		 
		BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(EditorApplication.class, "window.bxml");
        bxmlSerializer.bind(this, EditorApplication.class);
        
        // Add all components for left colum
        TablePane.Row editorMenu = (TablePane.Row) bxmlSerializer.getNamespace().get("editor_menu");
        TablePane.Row editorLayerSetup = (TablePane.Row) bxmlSerializer.getNamespace().get("editor_layer_setup");
        TablePane.Row editorLuaEditor = (TablePane.Row) bxmlSerializer.getNamespace().get("editor_lua_editor");
        
        menuPanel = new MenuPanel(this, instrument);
        instrument.addObserver(menuPanel);
        editorMenu.add(menuPanel);

        layerPanel = new LayerSetupPanel(this, instrument);
        instrument.addObserver(layerPanel);
        editorLayerSetup.add(layerPanel);
        
        luaPanel = new LuaEditorPanel(this, instrument);
        instrument.addObserver(luaPanel);
        editorLuaEditor.add(luaPanel);
        
        // Add all components for right colum
        TablePane.Row editorInstrumentName = (TablePane.Row) bxmlSerializer.getNamespace().get("editor_instrument_name");
        TablePane.Row editorLayerProperties = (TablePane.Row) bxmlSerializer.getNamespace().get("editor_layer_properties");
        TablePane.Row editorInstrumentPreview = (TablePane.Row) bxmlSerializer.getNamespace().get("editor_instrument_preview");
        TablePane.Row editorEnoderSetup = (TablePane.Row) bxmlSerializer.getNamespace().get("editor_encoder_setup");
        
        instrumentNamePanel = new InstrumentNamePanel(this, instrument);
        instrument.addObserver(instrumentNamePanel);
        editorInstrumentName.add(instrumentNamePanel);
        
        propertyPanel = new PropertyPanel(this, instrument);
        instrument.addObserver(propertyPanel);
        editorLayerProperties.add(propertyPanel);
        
        graphicPanelContainer = new GraphicPanelContainer(this, instrument);
        instrument.addObserver(graphicPanelContainer);
        editorInstrumentPreview.add(graphicPanelContainer);
        
        encoderSetupPanel = new EncoderSetupPanel(this, instrument);
        instrument.addObserver(encoderSetupPanel);
        editorEnoderSetup.add(encoderSetupPanel);
        
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
	 * Activate Lua Script Editor to show encoder script
	 * 
	 * @param encoderLeft
	 */
	public void enableEncoderEdit(boolean encoderLeft){
		luaPanel.enableEncoderEdit(encoderLeft);
	}
}
