package de.easemotion.fie.view;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonListener;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.FillPane;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;

import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.model.graphics.Instrument;
import de.easemotion.fie.model.graphics.Layer;
import de.easemotion.fie.model.graphics.Instrument.ImageMode;

public class GraphicPanelContainer extends BoxPane implements Observer {
	
	private GraphicPanel graphicPanel;
	
	private Instrument surface;

	public GraphicPanelContainer(EditorApplication editor, final Instrument surface){
		this.surface = surface;
		
		try {
			BXMLSerializer s = new BXMLSerializer();
			Component component = (Component) s.readObject(GraphicPanelContainer.class, "graphic_panel_container.bxml");
			
			PushButton buttonModeDay = (PushButton) s.getNamespace().get("button_mode_day");
			buttonModeDay.getButtonPressListeners().add(new ButtonPressListener() {
				
				@Override
				public void buttonPressed(Button button) {
					setImageMode(true);
				}
			});
			PushButton buttonModeNight = (PushButton) s.getNamespace().get("button_mode_night");
			buttonModeNight.getButtonPressListeners().add(new ButtonPressListener() {
				
				@Override
				public void buttonPressed(Button button) {
					setImageMode(false);
				}
			});
			PushButton buttonAlignementGrid = (PushButton) s.getNamespace().get("button_alignement_grid");
			buttonAlignementGrid.getButtonPressListeners().add(new ButtonPressListener() {
				
				@Override
				public void buttonPressed(Button button) {
					setGridMode();
				}
			});
			PushButton buttonInstrumentMask = (PushButton) s.getNamespace().get("button_instrument_mask");
			buttonInstrumentMask.getButtonPressListeners().add(new ButtonPressListener() {
				
				@Override
				public void buttonPressed(Button button) {
					setMaskMode();
				}
			});
			
			FillPane panel = (FillPane) s.getNamespace().get("graphic_panel");
			graphicPanel = new GraphicPanel(editor, surface);
			panel.add(graphicPanel);
			
			this.add(component);
		} catch (IOException | SerializationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param day true if ImageMode should change to Day, otherwise its changed to
	 * Night
	 */
	private void setImageMode(boolean day){
		surface.setMode(day ? ImageMode.DAY : ImageMode.NIGHT);
	}
	
	private void setGridMode(){
		graphicPanel.setShowGrid(!graphicPanel.isShowGrid());
	}
	
	private void setMaskMode(){
		graphicPanel.setShowInstrumentMask(!graphicPanel.isShowInstrumentMask());
	}

	@Override
	public void update(Observable o, Object arg) {
		graphicPanel.update(o, arg);
	}
}
