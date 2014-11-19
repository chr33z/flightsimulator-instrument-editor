package de.easemotion.fie.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageLayer extends Layer {
	
	/** width of the layer, typically the image width */
	private int width = 0;
	
	/** height of the layer, typically the image height */
	private int height = 0;
	
	/**
	 * X pivot at which image rotation takes place, relative to the image
	 * 0,0 coordinates
	 */
	private int pivotX = 0;
	/**
	 * Y pivot at which image rotation takes place, relative to the image
	 * 0,0 coordinates
	 */
	private int pivotY = 0;
	
	/** the image associated with this layer */
	private LayerImage image = new LayerImage();
	
	/** delay of rotation in degree per seconds */
	private int rotationDelay = 0;
	
	/** rotation */
	private int rotation = 0;
	
	public int getWidth() {
		return width;
	}

	public ImageLayer setWidth(int width) {
		this.width = width;
		resetPivot();
		return this;
	}

	public int getHeight() {
		return height;
	}

	public ImageLayer setHeight(int height) {
		this.height = height;
		resetPivot();
		return this;
	}
	
	public int getLeft() {
		return left;
	}

	public Layer setLeft(int left) {
		this.left = left;
		resetPivot();
		return this;
	}

	public int getTop() {
		return top;
	}

	public Layer setTop(int top) {
		this.top = top;
		resetPivot();
		return this;
	}
	
	public BufferedImage getImageDay() {
		return image.getImageDay();
	}

	public ImageLayer setImageDay(File imageDay) {
		image.setDay(imageDay);
		updateDimension();
		return this;
	}

	public BufferedImage getImageNight() {
		return image.getImageNight();
	}

	public ImageLayer setImageNight(File imageNight) {
		image.setNight(imageNight);
		updateDimension();
		return this;
	}

	public int getPivotX() {
		return pivotX;
	}

	public ImageLayer setPivotX(int pivotX) {
		this.pivotX = pivotX;
		return this;
	}

	public int getPivotY() {
		return pivotY;
	}

	public ImageLayer setPivotY(int pivotY) {
		this.pivotY = pivotY;
		return this;
	}
	
	public int getRotationDelay() {
		return rotationDelay;
	}

	public void setRotationDelay(int rotationDelay) {
		this.rotationDelay = rotationDelay;
	}

	public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}
	
	public LayerImage getImage(){
		return image;
	}
	
	public void resetPivot(){
		pivotX = width / 2;
		pivotY = height / 2;
	}
	
	/**
	 * Center image layer around pivot
	 */
	public void resetPosition(){
		if(parent != null){
			left = pivotX - parent.getWidth() / 2;
			top = pivotY - parent.getHeight() / 2;
		}
	}
	
	private void updateDimension(){
		BufferedImage day = image.getImageDay();
		BufferedImage night = image.getImageNight();
		
		int widthDay = 0;
		int heightDay = 0;
		
		int widthNight = 0;
		int heightNight = 0;
		
		
		
		if(day != null){
			widthDay = day.getWidth();
			heightDay = day.getHeight();
		}
		
		if(night != null){
			widthNight = night.getWidth();
			heightNight = night.getHeight();
		}
				
		if(day != null && night != null){
			if(widthDay != widthNight || heightDay != heightNight){
				System.err.println("[warning] Dimensions of day image and night image are different. Choose images with the same dimension");
			}
		}
		
		if(day != null){
			setWidth(widthDay);
			setHeight(heightDay);
		} else {
			setWidth(widthNight);
			setHeight(heightNight);
		}
	}
	
	@Override
	public Layer copy(Instrument instrument) {
		ImageLayer layer = new ImageLayer();
		layer.width = this.width;
		layer.height = this.height;
		layer.pivotX = this.pivotX;
		layer.pivotY = this.pivotY;
		layer.image = this.image.copy();
		layer.rotationDelay = this.rotationDelay;
		layer.rotation = this.rotation;
		
		layer.id = this.id;
		layer.left = this.left;
		layer.top = this.top;
		layer.luaScript = this.luaScript;
		layer.parent = instrument;
		
		return layer;
	}
}