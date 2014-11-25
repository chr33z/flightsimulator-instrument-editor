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
import org.apache.pivot.wtk.media.Image;

import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.model.ImageLayer;
import de.easemotion.fie.model.Instrument;
import de.easemotion.fie.model.Layer;
import de.easemotion.fie.model.Instrument.ImageMode;
import de.easemotion.fie.simulation.SimulationData;
import de.easemotion.fie.simulation.SimulationInstrument;
import de.easemotion.fie.utils.IconLoader;
import de.easemotion.fie.utils.IconLoader.Icon;

public class GraphicPanelContainer extends BoxPane implements Observer {
	
	private GraphicPanel graphicPanel;
	
	private Instrument instrument;

	LinkButton buttonModeDay;
	LinkButton buttonModeNight;
	LinkButton buttonAlignementGrid;
	LinkButton buttonInstrumentMask;
	
	public GraphicPanelContainer(EditorApplication editor, final Instrument instrument, 
			SimulationInstrument simInstrument, SimulationData simData){
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
			graphicPanel = new GraphicPanel(editor, instrument, simInstrument, simData);
			panel.add(graphicPanel);
			
			this.add(component);
			
			updateView();
		} catch (IOException | SerializationException e) {
			e.printStackTrace();
		}
	}
	
	private void updateView(){
		boolean dayImagesPresent = false;
		boolean nightImagesPresent = false;
		
		for (Layer layer : instrument.getLayers()) {
			if(layer != null && layer instanceof ImageLayer){
				dayImagesPresent |= (((ImageLayer) layer).getImageDay() != null);
				nightImagesPresent |= (((ImageLayer) layer).getImageNight() != null);
			}
		}
		
		/*
		 * Select Icon based on state and presents of images.
		 * If there are no night images for example the night
		 * mode is shown as deactive when not selected, otherwise
		 * as loaded
		 */
		int iconModeDay = IconLoader.DEACTIVE;
		int iconModeNight = IconLoader.DEACTIVE;
		
		if(instrument.getMode() == ImageMode.DAY){
			iconModeDay = IconLoader.ACTIVE;
			iconModeNight = nightImagesPresent ? IconLoader.LOADED : IconLoader.DEACTIVE;
		} else {
			iconModeNight = IconLoader.ACTIVE;
			iconModeDay = dayImagesPresent ? IconLoader.LOADED : IconLoader.DEACTIVE;
		}
		buttonModeDay.setButtonData(new ButtonData(IconLoader.icons.get(Icon.DAY_L)[iconModeDay]));
		buttonModeNight.setButtonData(new ButtonData(IconLoader.icons.get(Icon.NICHT_L)[iconModeNight]));
		
		if(graphicPanel.isShowGrid()){
			buttonAlignementGrid.setButtonData(new ButtonData(IconLoader.icons.get(Icon.GRID_L)[IconLoader.ACTIVE]));
		} else {
			buttonAlignementGrid.setButtonData(new ButtonData(IconLoader.icons.get(Icon.GRID_L)[IconLoader.DEACTIVE]));
		}
		
		if(graphicPanel.isShowInstrumentMask()){
			buttonInstrumentMask.setButtonData(new ButtonData(IconLoader.icons.get(Icon.MASK_L)[IconLoader.ACTIVE]));
		} else {
			buttonInstrumentMask.setButtonData(new ButtonData(IconLoader.icons.get(Icon.MASK_L)[IconLoader.DEACTIVE]));
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
