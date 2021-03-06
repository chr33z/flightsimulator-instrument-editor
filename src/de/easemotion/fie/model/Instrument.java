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

	protected int width = Constants.integer.INSTRUMENT_WIDTH;

	protected int height = Constants.integer.INSTRUMENT_HEIGHT;

	protected float scale = 1.0f;

	protected ImageMode mode = ImageMode.DAY;

	protected List<Layer> layers = new ArrayList<Layer>(Constants.integer.MAX_LAYER_COUNT);

	/** Code for left encoder */
	protected String codeEncoderLeft = "";

	/** Code for right encoder */
	protected String codeEncoderRight = "";

	protected String instrumentName = "";

	public Instrument(){
		for (int i = 0; i < Constants.integer.MAX_LAYER_COUNT; i++) {
			layers.add(null);
		}
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
			if(layer != null && layer.id.equals(id)){
				return layer;
			}
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
		if(layer != null){
			layer.setParent(this);
		}
		layers.set(index, layer);
		updateObservers();
	}

	/*
	 * Delete a layer
	 */
	public void deleteLayer(Layer layer){
		if(layer == null){
			return;
		}

		for (int i = 0; i < layers.size(); i++) {
			Layer l = layers.get(i);

			if(l != null && l.getSerial() == layer.getSerial()){
				layers.set(i, null);
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
			Layer next = iterator.next();
			if(next != null && layer.getSerial() == next.getSerial()){
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
			Layer next = iterator.next();
			if(next != null && layer.getSerial() == next.getSerial()){
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
				Layer next = iterator.next();
				if(layer != null && next != null && layer.getSerial() == next.getSerial()){
					break;
				}
				index++;
			}
			try {
				if(index < layers.size()-1){
					Layer next = layers.get(index + 1);
					Layer current = layers.get(index);

					layers.set(index, next);
					layers.set(index+1, current);
				}
			} catch (IndexOutOfBoundsException e){
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
				Layer next = iterator.next();
				if(layer != null && next != null && layer.getSerial() == next.getSerial()){
					break;
				}
				index++;
			}
			try {
				if(index > 0){
					Layer next = layers.get(index - 1);
					Layer current = layers.get(index);

					layers.set(index, next);
					layers.set(index-1, current);
				}
			} catch (IndexOutOfBoundsException e){
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
			if(l != null && l.getSerial() == layer.getSerial()){
				l.setActive(true);
			} else if(l != null) {
				l.setActive(false);
			}
		}
		updateObservers();
	}

	public void setLayersInactive(){
		for (Layer layer : layers) {
			if(layer != null){
				layer.setActive(false);
			}
		}
		updateObservers();
	}

	public Layer getActiveLayer(){
		for (Layer layer : layers) {
			if(layer != null && layer.isActive()){
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
		for (int i = 0; i < layers.size(); i++) {
			layers.set(i, null);
		}
		codeEncoderLeft = "";
		codeEncoderRight = "";
		mode = ImageMode.DAY;
		instrumentName = "";

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

		for (int i = 0; i < this.layers.size(); i++) {
			Layer copy = layers.get(i);

			if(copy != null){
				instrument.addLayer(i, copy.copy(this));
			} else {
				instrument.addLayer(i, null);
			}

		}

		return instrument;
	}

	/************************************
	 * Functions used for simulation api
	 ************************************/

	/**
	 * Set absolute rotation of a layer in degree
	 * 
	 * @param id
	 * @param degree
	 */
	public void rotateLayerAbs(String id, double degree, double delay){
		Layer layer = getLayer(id);

		if(layer != null && layer instanceof ImageLayer){
			ImageLayer imageLayer = (ImageLayer) layer;
			imageLayer.setRotation((int)degree);
			imageLayer.setDelay(delay);
		}
	}

	/**
	 * Add rotation to current rotation of layer in regree
	 * 
	 * @param id
	 * @param degree
	 */
	public void rotateLayerRel(String id, double degree, double delay){
		Layer layer = getLayer(id);

		if(layer != null && layer instanceof ImageLayer){
			ImageLayer imageLayer = (ImageLayer) layer;
			imageLayer.setRotation(imageLayer.getRotation() + (int)degree);
			imageLayer.setDelay(delay);
		}
	}

	public void translateLayerAbs(String id, double x, double y, double delay){
		Layer layer = getLayer(id);

		if(layer != null && layer instanceof ImageLayer){
			ImageLayer imageLayer = (ImageLayer) layer;
			imageLayer.setLeft((int)x + imageLayer.getPivotX());
			imageLayer.setTop((int)y + imageLayer.getPivotY());
			imageLayer.setDelay(delay);
		}
	}

	public void translateLayerRel(String id, double x, double y, double delay){
		Layer layer = getLayer(id);

		if(layer != null && layer instanceof ImageLayer){
			ImageLayer imageLayer = (ImageLayer) layer;
			imageLayer.setLeft(layer.getLeft() + (int)x);
			imageLayer.setTop(layer.getTop() + (int)y);
			imageLayer.setDelay(delay);
		}
	}
	
	public void setVisible(String id, boolean visible){
		Layer layer = getLayer(id);
		
		if(layer != null){
			layer.visible = visible;
		}
	}
	
	public boolean isVisible(String id){
		Layer layer = getLayer(id);
		
		if(layer != null){
			return layer.visible;
		} else {
			return false;
		}
	}
}
