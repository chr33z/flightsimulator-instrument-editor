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
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.content.ButtonData;

import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.model.graphics.Instrument;
import de.easemotion.fie.model.graphics.Layer;
import de.easemotion.fie.model.graphics.Instrument.ImageMode;
import de.easemotion.fie.utils.IconLoader;
import de.easemotion.fie.utils.IconLoader.Icon;

public class GraphicPanelContainer extends BoxPane implements Observer {
	
	private GraphicPanel graphicPanel;
	
	private Instrument instrument;

	LinkButton buttonModeDay;
	LinkButton buttonModeNight;
	LinkButton buttonAlignementGrid;
	LinkButton buttonInstrumentMask;
	
	public GraphicPanelContainer(EditorApplication editor, final Instrument instrument){
		this.instrument = instrument;
		
		try {
			BXMLSerializer s = new BXMLSerializer();
			Component component = (Component) s.readObject(GraphicPanelContainer.class, "graphic_panel_container.bxml");
			
			buttonModeDay = (LinkButton) s.getNamespace().get("button_mode_day");
			buttonModeDay.getButtonPressListeners().add(new ButtonPressListener() {
				
				@Override
				public void buttonPressed(Button button) {
					setImageMode(true);
				}
			});
			buttonModeNight = (LinkButton) s.getNamespace().get("button_mode_night");
			buttonModeNight.getButtonPressListeners().add(new ButtonPressListener() {
				
				@Override
				public void buttonPressed(Button button) {
					setImageMode(false);
				}
			});
			buttonAlignementGrid = (LinkButton) s.getNamespace().get("button_alignement_grid");
			buttonAlignementGrid.getButtonPressListeners().add(new ButtonPressListener() {
				
				@Override
				public void buttonPressed(Button button) {
					setGridMode();
				}
			});
			buttonInstrumentMask = (LinkButton) s.getNamespace().get("button_instrument_mask");
			buttonInstrumentMask.getButtonPressListeners().add(new ButtonPressListener() {
				
				@Override
				public void buttonPressed(Button button) {
					setMaskMode();
				}
			});
			
			FillPane panel = (FillPane) s.getNamespace().get("graphic_panel");
			graphicPanel = new GraphicPanel(editor, instrument);
			panel.add(graphicPanel);
			
			this.add(component);
			
			updateView();
		} catch (IOException | SerializationException e) {
			e.printStackTrace();
		}
	}
	
	private void updateView(){
		if(instrument.getMode() == ImageMode.DAY){
			buttonModeDay.setButtonData(new ButtonData(IconLoader.icons.get(Icon.DAY_L)[IconLoader.LOADED]));
			buttonModeNight.setButtonData(new ButtonData(IconLoader.icons.get(Icon.NICHT_L)[IconLoader.ACTIVE]));
		} else {
			buttonModeDay.setButtonData(new ButtonData(IconLoader.icons.get(Icon.DAY_L)[IconLoader.ACTIVE]));
			buttonModeNight.setButtonData(new ButtonData(IconLoader.icons.get(Icon.NICHT_L)[IconLoader.LOADED]));
		}
		
		if(graphicPanel.isShowGrid()){
			buttonAlignementGrid.setButtonData(new ButtonData(IconLoader.icons.get(Icon.GRID_L)[IconLoader.LOADED]));
		} else {
			buttonAlignementGrid.setButtonData(new ButtonData(IconLoader.icons.get(Icon.GRID_L)[IconLoader.ACTIVE]));
		}
		
		if(graphicPanel.isShowInstrumentMask()){
			buttonInstrumentMask.setButtonData(new ButtonData(IconLoader.icons.get(Icon.MASK_L)[IconLoader.LOADED]));
		} else {
			buttonInstrumentMask.setButtonData(new ButtonData(IconLoader.icons.get(Icon.MASK_L)[IconLoader.ACTIVE]));
		}
	}
	
	/**
	 * @param day true if ImageMode should change to Day, otherwise its changed to
	 * Night
	 */
	private void setImageMode(boolean day){
		instrument.setMode(day ? ImageMode.DAY : ImageMode.NIGHT);
		updateView();
	}
	
	private void setGridMode(){
		graphicPanel.setShowGrid(!graphicPanel.isShowGrid());
		updateView();
	}
	
	private void setMaskMode(){
		graphicPanel.setShowInstrumentMask(!graphicPanel.isShowInstrumentMask());
		updateView();
	}

	@Override
	public void update(Observable o, Object arg) {
		graphicPanel.update(o, arg);
		updateView();
	}
}
