package de.easemotion.fie.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import de.easemotion.fie.utils.Constants;

/**
 * Base graphic surface on which all other graphic options take place.
 * Users can add layers to this surface to build up the instrument.
 * 
 * Note:
 * This class is implemented as Singleton
 * 
 * @author Christopher Gebhardt
 * @date Oct 21, 2014
 * @project Flightsimulator-Instrument-Editor
 *
 */
public class Instrument extends Observable {

	private static final String TAG = Instrument.class.getSimpleName();
	
	/**
	 * Mode describes whether he night or day images are shown in editor
	 * 
	 * @author Christopher Gebhardt
	 * @date Nov 1, 2014
	 * @project Flightsimulator-Instrument-Editor
	 *
	 */
	public enum ImageMode {
		DAY, NIGHT
	}
	
	private int width = Constants.integer.INSTRUMENT_WIDTH;
	
	private int height = Constants.integer.INSTRUMENT_HEIGHT;
	
	private float scale = 1.0f;
	
	private ImageMode mode = ImageMode.DAY;
	
	private List<Layer> layers = new ArrayList<Layer>();
	
	/** Code for left encoder */
	private String codeEncoderLeft = "";
	
	/** Code for right encoder */
	private String codeEncoderRight = "";
	
	private String instrumentName = "";
	
	public Instrument(){
		
	}
	
	/**
	 * @return get scale factor to which all layers are scaled in the
	 * editor
	 */
	public float getScale() {
		return scale;
	}

	/**
	 * Set scale factor to which all layers are scaled
	 * in the editor
	 * @param scale
	 */
	public void setScale(float scale) {
		this.scale = scale;
		updateObservers();
	}

	/**
	 * @return all layers
	 */
	public List<Layer> getLayers() {
		return layers;
	}
	
	/**
	 * Get a layer by its id
	 * @param id
	 * @return Layer with the id or null if not found
	 */
	public Layer getLayer(String id){
		for (Layer layer : layers) {
			// FIXME implement
			return layer;
		}
		return null;
	}

	/**
	 * Add a layer to the top
	 * @param layer
	 */
	public void addLayer(Layer layer) {
		layer.setParent(this);
		layers.add(layer);
		updateObservers();
	}
	
	public void addLayer(int index, Layer layer) {
		layer.setParent(this);
		layers.add(index, layer);
		updateObservers();
	}
	
	/*
	 * Delete a layer
	 */
	public void deleteLayer(Layer layer){
		if(layer == null){
			return;
		}
		
		Iterator<Layer> iterator = layers.iterator();
		while (iterator.hasNext()) {
			if(layer.getId().equals(iterator.next().getId())){
				iterator.remove();
				break;
			}
		}
		updateObservers();
	}
	
	public void setVisibility(Layer layer, boolean visible){
		if(layer == null){
			return;
		}
		
		Iterator<Layer> iterator = layers.iterator();
		while (iterator.hasNext()) {
			if(layer.getId().equals(iterator.next().getId())){
				layer.setVisible(visible);
				break;
			}
		}
		updateObservers();
	}
	
	public void toggleVisibility(Layer layer){
		if(layer == null){
			return;
		}
		
		Iterator<Layer> iterator = layers.iterator();
		while (iterator.hasNext()) {
			if(layer.getId().equals(iterator.next().getId())){
				layer.setVisible(!layer.isVisible());
				break;
			}
		}
		updateObservers();
	}
	
	/**
	 * Move a layer forward in the drawing position
	 * @param layer
	 */
	public void moveLayerForward(Layer layer){
		if(layer != null){
			int index = 0;
			Iterator<Layer> iterator = getLayers().iterator();
			while (iterator.hasNext()) {
				// find layer
				if(layer.getId().equals(iterator.next().getId())){
					iterator.remove();
					break;
				}
				index++;
			}
			try {
				addLayer(index + 1, layer);
			} catch (IndexOutOfBoundsException e){
				addLayer(layer);
			}
		}
		updateObservers();
	}
	
	public void moveLayerBackwards(Layer layer){
		if(layer != null){
			int index = 0;
			Iterator<Layer> iterator = getLayers().iterator();
			while (iterator.hasNext()) {
				// find layer
				if(layer.getId().equals(iterator.next().getId())){
					iterator.remove();
					break;
				}
				index++;
			}
			try {
				addLayer(index - 1, layer);
			} catch (IndexOutOfBoundsException e){
				addLayer(0, layer);
			}
		}
		updateObservers();
	}
	
	public void setWidth(int width){
		this.width = width;
		updateObservers();
	}

	/**
	 * @return width of base graphic surface
	 */
	public int getWidth() {
		return width;
	}
	
	public void setHeight(int height){
		this.height = height;
		updateObservers();
	}

	/**
	 * @return height of base graphic surface
	 */
	public int getHeight() {
		return height;
	}
	
	public void setLayerActive(Layer layer){
		if(layer == null) {
			return;
		}
		
		for (Layer l : layers) {
			if(l.getSerial() == layer.getSerial()){
				l.setActive(true);
			} else {
				l.setActive(false);
			}
		}
		updateObservers();
	}
	
	public void setLayersInactive(){
		for (Layer layer : layers) {
			layer.setActive(false);
		}
		updateObservers();
	}
	
	public Layer getActiveLayer(){
		for (Layer layer : layers) {
			if(layer.isActive()){
				return layer;
			}
		}
		return null;
	}
	
	/**
	 * find a layer with its serial (get serial with getSerial())
	 * @param serial
	 * @return
	 */
	public Layer find(long serial){
		for (Layer layer : layers) {
			if(layer.getSerial() == serial){
				return layer;
			}
		}
		return null;
	}
	
	public void reset(){
		layers = new ArrayList<>();
		codeEncoderLeft = "";
		codeEncoderRight = "";
		mode = ImageMode.DAY;
		
		updateObservers();
	}
	
	public ImageMode getMode() {
		return mode;
	}

	public void setMode(ImageMode mode) {
		this.mode = mode;
		updateObservers();
	}

	public String getCodeEncoderLeft() {
		return codeEncoderLeft;
	}

	public void setCodeEncoderLeft(String codeEncoderLeft) {
		this.codeEncoderLeft = codeEncoderLeft;
	}

	public String getCodeEncoderRight() {
		return codeEncoderRight;
	}

	public void setCodeEncoderRight(String codeEncoderRight) {
		this.codeEncoderRight = codeEncoderRight;
	}

	public String getInstrumentName() {
		return instrumentName;
	}

	public void setInstrumentName(String instrumentName) {
		this.instrumentName = instrumentName;
	}

	public void updateObservers(){
		setChanged();
		notifyObservers();
	}
	
	public Instrument copy(){
		Instrument instrument = new Instrument();
		instrument.width = this.width;
		instrument.height = this.height;
		instrument.codeEncoderLeft = this.codeEncoderLeft;
		instrument.codeEncoderRight = this.codeEncoderRight;
		instrument.instrumentName = this.instrumentName;
		
		for (Layer layer : this.layers) {
			instrument.addLayer(layer.copy(this));
		}
		
		return instrument;
	}
}
