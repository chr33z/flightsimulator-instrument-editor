package de.easemotion.fie;

import java.awt.Dimension;
import java.io.File;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.Window;

import de.easemotion.fie.model.Instrument;
import de.easemotion.fie.simulation.SimulationData;
import de.easemotion.fie.simulation.SimulationInstrument;
import de.easemotion.fie.utils.IconLoader;
import de.easemotion.fie.utils.IconLoader.Icon;
import de.easemotion.fie.view.EncoderSetupPanel;
import de.easemotion.fie.view.GraphicPanelContainer;
import de.easemotion.fie.view.InstrumentNamePanel;
import de.easemotion.fie.view.LayerSetupPanel;
import de.easemotion.fie.view.LuaEditorPanel;
import de.easemotion.fie.view.MenuPanel;
import de.easemotion.fie.view.PropertyPanel;

public class EditorApplication implements Application {
	
	private static final String TAG = EditorApplication.class.getSimpleName();
	
	public Window window = null;
	
	/**
	 * Instrument used to build the view in edit mode
	 */
	public Instrument instrument;
	
	/**
	 * Instrument class used to simulate the preview.
	 * This class automatically produces the lua code
	 * that is used to maipulate the gauge
	 */
	public SimulationInstrument simulationInstrument = new SimulationInstrument();
	
	/**
	 * Global data storage for simulation data. luaPane and
	 * graphicPanelContainer listen for changes in this class
	 * to start or stop a simulation with data
	 */
	public SimulationData simulationData = new SimulationData();
	
	public static File lastFileBrowserPath = null;
	
	LayerSetupPanel layerPanel;
	LuaEditorPanel luaPanel;
	InstrumentNamePanel instrumentNamePanel;
	PropertyPanel propertyPanel;
	GraphicPanelContainer graphicPanelContainer;
	EncoderSetupPanel encoderSetupPanel;
	MenuPanel menuPanel;
	
	LinkButton simulationButton;
	
	public static void main(String[] args) {
	    DesktopApplicationContext.main(EditorApplication.class, args);
	}

	@Override
	public void startup(Display display, Map<String, String> properties)
			throws Exception {
		
		display.getHostWindow().setMaximumSize(new Dimension(1000, 800));
		display.getHostWindow().setPreferredSize(new Dimension(1000, 800));
		display.getStyles().put("resizable", false);
		
		window = new Window();
		window.setMaximized(true);
		window.setSize(1000, 800);
		window.setPreferredSize(1000, 800);

		IconLoader.loadIcons();
		
		instrument = new Instrument();
		 
		BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(EditorApplication.class, "window.bxml");
        bxmlSerializer.bind(this, EditorApplication.class);
        
        // Add all components for left colum
        BoxPane editorInstrumentName = (BoxPane) bxmlSerializer.getNamespace().get("editor_instrument_name");
        TablePane.Row editorLayerSetup = (TablePane.Row) bxmlSerializer.getNamespace().get("editor_layer_setup");
        TablePane.Row editorLuaEditor = (TablePane.Row) bxmlSerializer.getNamespace().get("editor_lua_editor");

        instrumentNamePanel = new InstrumentNamePanel(this, instrument);
        instrument.addObserver(instrumentNamePanel);
        editorInstrumentName.add(instrumentNamePanel);
        
        layerPanel = new LayerSetupPanel(this, instrument);
        instrument.addObserver(layerPanel);
        simulationData.addObserver(layerPanel);
        editorLayerSetup.add(layerPanel);
        
        luaPanel = new LuaEditorPanel(this, instrument, simulationData);
        instrument.addObserver(luaPanel);
        simulationData.addObserver(luaPanel);
        editorLuaEditor.add(luaPanel);
        
        // Add all components for right colum
        TablePane.Row editorLayerProperties = (TablePane.Row) bxmlSerializer.getNamespace().get("editor_layer_properties");
        TablePane.Row editorInstrumentPreview = (TablePane.Row) bxmlSerializer.getNamespace().get("editor_instrument_preview");
        TablePane.Row editorEnoderSetup = (TablePane.Row) bxmlSerializer.getNamespace().get("editor_encoder_setup");
        TablePane.Row editorMenu = (TablePane.Row) bxmlSerializer.getNamespace().get("editor_menu");
        
        propertyPanel = new PropertyPanel(this, instrument);
        instrument.addObserver(propertyPanel);
        simulationData.addObserver(propertyPanel);
        editorLayerProperties.add(propertyPanel);
        
        graphicPanelContainer = new GraphicPanelContainer(this, instrument, 
        		simulationInstrument, simulationData);
        instrument.addObserver(graphicPanelContainer);
        simulationInstrument.addObserver(graphicPanelContainer);
        editorInstrumentPreview.add(graphicPanelContainer);
        
        encoderSetupPanel = new EncoderSetupPanel(this, instrument);
        instrument.addObserver(encoderSetupPanel);
        editorEnoderSetup.add(encoderSetupPanel);
        
        menuPanel = new MenuPanel(this, instrument);
        instrument.addObserver(menuPanel);
        editorMenu.add(menuPanel);
        
        // info and help button
        LinkButton infoButton = (LinkButton) bxmlSerializer.getNamespace().get("button_info");
        infoButton.getButtonPressListeners().add(new ButtonPressListener() {
			
			@Override
			public void buttonPressed(Button button) {
				onInfoButton();
			}
		});
        
        LinkButton helpButton = (LinkButton) bxmlSerializer.getNamespace().get("button_help");
        helpButton.getButtonPressListeners().add(new ButtonPressListener() {
			
			@Override
			public void buttonPressed(Button button) {
				onHelpButton();
			}
		});
        
        // Simulation Button
        simulationButton = (LinkButton) bxmlSerializer.getNamespace().get("button_simulation");
        simulationButton.setButtonData(IconLoader
				.icons.get(Icon.SIM)[IconLoader.DEACTIVE]);
        simulationButton.getButtonPressListeners().add(new ButtonPressListener() {
			
			@Override
			public void buttonPressed(Button button) {
				onToggleSimulation();
			}
		});
        
        /**
         * Add mouse button listener to the overall window to remove the splashscreen
         * when
         */
        window.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener() {
			
			@Override
			public boolean mouseUp(Component component,
					org.apache.pivot.wtk.Mouse.Button button, int x, int y) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean mouseDown(Component component,
					org.apache.pivot.wtk.Mouse.Button button, int x, int y) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean mouseClick(Component component,
					org.apache.pivot.wtk.Mouse.Button button, int x, int y, int count) {
				
				if(luaPanel != null && luaPanel.isSplashScreenVisible()){
					luaPanel.setSplashScreenVisible(false);
				}
				return false;
			}
		});
        
        window.open(display);
	}
	
	public Instrument getInstrument(){
		return instrument;
	}
	
	public void setInstrument(Instrument instrument){
		this.instrument = instrument;
		
		this.instrument.addObserver(instrumentNamePanel);
		this.instrument.addObserver(layerPanel);
		this.instrument.addObserver(luaPanel);
		this.instrument.addObserver(propertyPanel);
		this.instrument.addObserver(graphicPanelContainer);
		this.instrument.addObserver(encoderSetupPanel);
		this.instrument.addObserver(menuPanel);
		
		this.instrument.updateObservers();
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
	
	public void onInfoButton(){
		if(luaPanel != null){
			luaPanel.setSplashScreenVisible(true);
		}
	}
	
	public void onHelpButton(){
		System.out.println("Help button pressed");
	}
	
	public void onToggleSimulation(){
		simulationData.toggleSimulationActive();
		
		if(simulationData.isSimulationActive()){
			simulationButton.setButtonData(IconLoader
					.icons.get(Icon.SIM)[IconLoader.ACTIVE]);
		} else {
			simulationButton.setButtonData(IconLoader
					.icons.get(Icon.SIM)[IconLoader.DEACTIVE]);
		}
		
		/*
		 * If the simulation is toggled to running, update
		 * simulationInstrument with the current instrument
		 * and simulationData so that our graphic panel has the
		 * most recent configuration 
		 */
		if(simulationData.isSimulationActive()){
			updateSimulationInstrument();
		}
	}
	
	public void updateSimulationInstrument(){
		simulationInstrument.init(instrument, simulationData);
	}
	
	public void updateSimulationData(){
		simulationInstrument.update(simulationData);
		
		simulationInstrument.run();
	}
}
