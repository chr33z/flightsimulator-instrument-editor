package de.easemotion.fie.model.graphics;

import java.util.ArrayList;
import java.util.List;

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
public class GraphicSurface {

	private static final String TAG = GraphicSurface.class.getSimpleName();
	
	private int width = Constants.integer.INSTRUMENT_WIDTH;
	
	private int height = Constants.integer.INSTRUMENT_HEIGHT;
	
	private float scale = 1.0f;
	
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
	}
	
	/*
	 * Delete a layer
	 */
	public void deleteLayer(Layer layer){
		// FIXME implement
	}
	
	/**
	 * Move layer one position in the specified direction
	 * @param up true when the layer should move up, false when down
	 */
	public void moveLayer(boolean up){
		// FIXME implement
	}
	
	public void setWidth(int width){
		this.width = width;
	}

	/**
	 * @return width of base graphic surface
	 */
	public int getWidth() {
		return width;
	}
	
	public void setHeight(int height){
		this.height = height;
	}

	/**
	 * @return height of base graphic surface
	 */
	public int getHeight() {
		return height;
	}
}
