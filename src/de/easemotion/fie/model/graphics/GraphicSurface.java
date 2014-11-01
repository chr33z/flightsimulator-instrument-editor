package de.easemotion.fie.model.graphics;

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
public class GraphicSurface extends Observable {

	private static final String TAG = GraphicSurface.class.getSimpleName();
	
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
	
	private static GraphicSurface sInstance;
	
	private GraphicSurface(){
		// Implemented as Singleton
	}
	
	public static GraphicSurface getInstance(){
		if(sInstance == null){
			sInstance = new GraphicSurface();
		}
		return sInstance;
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
		this.layers.add(layer);
		updateObservers();
	}
	
	public void addLayer(int index, Layer layer) {
		this.layers.add(index, layer);
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
			if(l.getId().equals(layer.getId())){
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
	
	public ImageMode getMode() {
		return mode;
	}

	public void setMode(ImageMode mode) {
		this.mode = mode;
		updateObservers();
	}

	public void updateObservers(){
		setChanged();
		notifyObservers();
	}
}
