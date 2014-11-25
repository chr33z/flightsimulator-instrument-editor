package de.easemotion.fie.view;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.FocusTraversalPolicy;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.StackPane;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.TextInput;

import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.model.EditorStatus;
import de.easemotion.fie.model.Instrument;
import de.easemotion.fie.model.Layer;
import de.easemotion.fie.simulation.SimulationData;
import de.easemotion.fie.simulation.SimulationData.Data;
import de.easemotion.fie.utils.Constants;
import de.easemotion.fie.utils.IconLoader;
import de.easemotion.fie.utils.IconLoader.Icon;

public class LuaEditorPanel extends BoxPane implements Observer {

	private static final String TAG = LuaEditorPanel.class.getSimpleName();
	
	private enum Attribute {
		INDEX, ID;
	}

	private Instrument instrument;
	
	private SimulationData simulationData;

	private EditorApplication editor;

	private TextArea textArea;
	
	private ImageView splashScreen;
	
	private StackPane simModeContainer;
	
	private BoxPane simModeRows;
	
	private boolean splashScreenVisible = false;

	boolean encoderEditMode = false;
	boolean encoderLeft = true;
	
	public LuaEditorPanel(EditorApplication editor, final Instrument surface, SimulationData simulationData){
		this.editor = editor;
		this.instrument = surface;
		this.simulationData = simulationData;

		try {
			BXMLSerializer s = new BXMLSerializer();
			Component component = (Component) s.readObject(LuaEditorPanel.class, "lua_pane.bxml");
			
			// init simulation mode components
			simModeContainer = (StackPane) s.getNamespace().get("sim_panel");
			simModeContainer.setVisible(false);
			
			// add 5 simulation rows
			simModeRows = (BoxPane) s.getNamespace().get("sim_panel_rows");
			for (int i = 0; i < 5; i++) {
				simModeRows.add(getSimulationRow(i));
			}
			
			// init splashscreen
			splashScreen = (ImageView) s.getNamespace().get("splashscreen");
			splashScreen.setVisible(false);

			// init lua editor
			textArea = (TextArea) s.getNamespace().get("lua_text_area");
			textArea.getStyles().put("color", Constants.color.TEXT_PRIMARY);
			textArea.getStyles().put("font", Constants.font.FONT_CODE);
			textArea.setEditable(false);
			textArea.setEnabled(false);
			
			/*
			 * Keep focus in text area when pressing tab
			 */
			this.setFocusTraversalPolicy(new FocusTraversalPolicy() {
				
				@Override
				public Component getNextComponent(Container container, Component component,
						FocusTraversalDirection direction) {
					// TODO Auto-generated method stub
					return textArea;
				}
			});

			textArea.getComponentKeyListeners().add(new ComponentKeyListener() {

				@Override
				public boolean keyTyped(Component component, char character) {

					if(encoderEditMode){
						// save content from textArea to encoder
						if(encoderLeft){
							instrument.setCodeEncoderLeft(textArea.getText());
						} else {
							instrument.setCodeEncoderRight(textArea.getText());
						}
					} else {
						// save content from textArea to layer
						Layer layer = surface.getActiveLayer();
						if(layer != null){
							layer.setLuaScript(textArea.getText());
						}
					}
					return false;
				}

				@Override
				public boolean keyReleased(Component component, int keyCode,
						KeyLocation keyLocation) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public boolean keyPressed(Component component, int keyCode,
						KeyLocation keyLocation) {

					switch (keyCode) {
					case KeyCode.TAB:
						textArea.setText(textArea.getText() + "    ");
						break;
					default:
						break;
					}

					return true;
				}
			});

			this.add(component);
		} catch (IOException | SerializationException e) {
			e.printStackTrace();
		}
	}
	
	public void enableEncoderEdit(boolean encoderLeft){
		this.encoderLeft = encoderLeft;
		encoderEditMode = true;

		textArea.setEditable(true);
		textArea.setEnabled(true);

		if(encoderLeft){
			EditorStatus.setEncoderEditLeft(true);
			textArea.setText(instrument.getCodeEncoderLeft());
		} else {
			EditorStatus.setEncoderEditRight(true);
			textArea.setText(instrument.getCodeEncoderRight());
		}
	}

	private void updateScript(){
		Layer layer = instrument.getActiveLayer();

		if(layer != null){
			textArea.setEditable(true);
			textArea.setEnabled(true);
			textArea.setText(layer.getLuaScript());
		} else {
			textArea.setEditable(false);
			textArea.setEnabled(false);
			textArea.setText("");
		}
	}
	
	public void setSplashScreenVisible(final boolean visible){
		splashScreen.setVisible(visible);
		
		/**
		 * Set visibility delayed because the mouseclick on
		 * the button triggers also that the splashscreen vanishes
		 * again
		 */
		Thread delay = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				splashScreenVisible = visible;
			}
		});
		delay.start();
		
		repaint(true);
	}
	
	public boolean isSplashScreenVisible(){
		return splashScreenVisible;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if(o instanceof Instrument){
			instrument = editor.getInstrument();
			encoderEditMode = false;
			updateScript();
		}
		else if(o instanceof SimulationData) {
			updateSimulationInterface((SimulationData) o);
		}
	}
	
	/*****************************
	 * Simulation specific methods
	 */
	
	/**
	 * Create one row of panels for simulation control
	 * @return
	 */
	private Component getSimulationRow(final int index){
		try {
			BXMLSerializer s = new BXMLSerializer();
			Component row = (Component) s.readObject(LuaEditorPanel.class, "simulation_mode_row.bxml");
			
			Data data = simulationData.getData(index);
			
			TextInput simConnectVariable = (TextInput) s.getNamespace().get("sim_connect_variable");
			simConnectVariable.setAttribute(Attribute.INDEX, index);
			simConnectVariable.setAttribute(Attribute.ID, "sim_connect_variable");
			simConnectVariable.getComponentKeyListeners().add(inputListener);
			if(data != null){
				simConnectVariable.setText(data.variable);
			}
			
			TextInput simConnectValue = (TextInput) s.getNamespace().get("sim_connect_value");
			simConnectValue.setAttribute(Attribute.INDEX, index);
			simConnectValue.setAttribute(Attribute.ID, "sim_connect_value");
			simConnectValue.getComponentKeyListeners().add(inputListener);
			if(data != null){
				simConnectValue.setText(data.value+"");
			}
			
			LinkButton buttonIncrease = (LinkButton) s.getNamespace().get("button_sim_increase");
			buttonIncrease.setButtonData(IconLoader.icons.get(Icon.PLUS)[IconLoader.DEACTIVE]);
			buttonIncrease.setAttribute(Attribute.INDEX, index);
			buttonIncrease.setAttribute(Attribute.ID, "button_sim_increase");
			buttonIncrease.getButtonPressListeners().add(buttonListener);
			
			LinkButton buttonDecrease = (LinkButton) s.getNamespace().get("button_sim_decrease");
			buttonDecrease.setButtonData(IconLoader.icons.get(Icon.MINUS)[IconLoader.DEACTIVE]);
			buttonDecrease.setAttribute(Attribute.INDEX, index);
			buttonDecrease.setAttribute(Attribute.ID, "button_sim_decrease");
			buttonDecrease.getButtonPressListeners().add(buttonListener);
			
			LinkButton buttonMultiply = (LinkButton) s.getNamespace().get("button_sim_multiply");
			buttonMultiply.setAttribute(Attribute.INDEX, index);
			buttonMultiply.setAttribute(Attribute.ID, "button_sim_multiply");
			buttonMultiply.getButtonPressListeners().add(buttonListener);
			if(data != null){
				if(data.multiply){
					buttonMultiply.setButtonData(IconLoader.icons.get(Icon.X10)[IconLoader.ACTIVE]);
				} else {
					buttonMultiply.setButtonData(IconLoader.icons.get(Icon.X10)[IconLoader.DEACTIVE]);
				}
			} else {
				buttonMultiply.setButtonData(IconLoader.icons.get(Icon.X10)[IconLoader.DEACTIVE]);
			}
			
			return row;
			
		} catch (IOException | SerializationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private ButtonPressListener buttonListener = new ButtonPressListener() {
		@Override
		public void buttonPressed(Button button) {
			int index = (int) button.getAttribute(Attribute.INDEX);
			String id = (String) button.getAttribute(Attribute.ID);
			
			switch (id) {
			case "button_sim_increase":
				simulationData.increaseValue(index);
				break;
			case "button_sim_decrease":
				simulationData.decreaseValue(index);
				break;
			case "button_sim_multiply":
				simulationData.toggleMultiply(index);
				break;
			}
			
			editor.updateSimulationData();
		}
	};
	
	private ComponentKeyListener inputListener = new ComponentKeyListener() {
		
		@Override
		public boolean keyTyped(Component component, char character) {
			int index = (int) component.getAttribute(Attribute.INDEX);
			
//			TextInput input = (TextInput) component;
//			simulationData.setSimConnectVariable(index, input.getText());
			return false;
		}
		
		@Override
		public boolean keyReleased(Component component, int keyCode,
				KeyLocation keyLocation) {
			return false;
		}
		
		@Override
		public boolean keyPressed(Component component, int keyCode,
				KeyLocation keyLocation) {
			int index = (int) component.getAttribute(Attribute.INDEX);
			String id = (String) component.getAttribute(Attribute.ID);
			TextInput input = (TextInput) component;
			
			switch (keyCode) {
			case KeyCode.ENTER:
			case KeyCode.TAB:
				if(id.equals("sim_connect_variable")){
					simulationData.setSimConnectVariable(index, input.getText());
				}
				else if(id.equals("sim_connect_value")) {
					try {
						simulationData.setSimConnectValue(index, Integer.parseInt(input.getText()));
					} catch (NumberFormatException e) {
					}
				}
				editor.updateSimulationData();
				break;
			}
			return false;
		}
	};
	
	private void updateSimulationInterface(SimulationData data){
		simModeContainer.setVisible(data.isSimulationActive());
		repaint();
		
		simModeRows.removeAll();
		for (int i = 0; i < 5; i++) {
			simModeRows.add(getSimulationRow(i));
		}
	}
}
