package de.easemotion.fie.model.graphics;

/**
 * One layer that builds up the instrument. Each layer can hold one image. Each layer has
 * a fixed order in the list that indicates drawing order. Layers at the top are drawn last
 * while layers at the bottom are drawn at first (like placing physical pictures on a stack)
 * 
 * @author Christopher Gebhardt
 * @date Oct 21, 2014
 * @project Flightsimulator-Instrument-Editor
 *
 */
public class Layer {

	private static final String TAG = Layer.class.getSimpleName();
	
	/** human readable id for this layer (like "hour-dial") */
	private String id;
	
	private int width;
	
	private int height;
	
	private int left;
	
	private int top;
	
	/** wheter this layer is active in graphicpanel */
	boolean active;
	
	/** image file name with file ending */
	private String image;
	
	public Layer(){
		// FIXME implement
	}

	public String getId() {
		return id;
	}

	public Layer setId(String id) {
		this.id = id;
		return this;
	}

	public int getWidth() {
		return width;
	}

	public Layer setWidth(int width) {
		this.width = width;
		return this;
	}

	public int getHeight() {
		return height;
	}

	public Layer setHeight(int height) {
		this.height = height;
		return this;
	}

	public int getLeft() {
		return left;
	}

	public Layer setLeft(int left) {
		this.left = left;
		return this;
	}

	public int getTop() {
		return top;
	}

	public Layer setTop(int top) {
		this.top = top;
		return this;
	}

	public String getImage() {
		return image;
	}

	public Layer setImage(String image) {
		this.image = image;
		return this;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
