package de.easemotion.fie.view;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.FocusTraversalPolicy;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.StackPane;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;

import sun.java2d.Surface;
import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.model.EditorStatus;
import de.easemotion.fie.model.Instrument;
import de.easemotion.fie.model.Layer;
import de.easemotion.fie.utils.Constants;

public class LuaEditorPanel extends BoxPane implements Observer {

	private static final String TAG = LuaEditorPanel.class.getSimpleName();

	private Instrument instrument;

	private EditorApplication editor;

	private TextArea textArea;
	
	private ImageView splashScreen;
	
	private StackPane simModeContainer;
	
	private boolean splashScreenVisible = false;

	boolean encoderEditMode = false;
	boolean encoderLeft = true;
	
	boolean simulationActive = false;

	public LuaEditorPanel(EditorApplication editor, final Instrument surface){
		this.editor = editor;
		this.instrument = surface;

		try {
			BXMLSerializer s = new BXMLSerializer();
			Component component = (Component) s.readObject(LuaEditorPanel.class, "lua_pane.bxml");
			
			splashScreen = (ImageView) s.getNamespace().get("splashscreen");
			splashScreen.setVisible(false);
			
			simModeContainer = (StackPane) s.getNamespace().get("sim_panel");
			simModeContainer.setVisible(false);

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
		System.out.println("set visible "+visible);
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
	
	public void toggleSimulation(){
		simulationActive = !simulationActive;
		
		// TODO more
		simModeContainer.setVisible(simulationActive);
	}
	
	public boolean isSimulationActive(){
		return simulationActive;
	}

	@Override
	public void update(Observable o, Object arg) {
		instrument = editor.getInstrument();
		encoderEditMode = false;
		updateScript();
	}
}
