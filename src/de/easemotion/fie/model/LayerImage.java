package de.easemotion.fie.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class LayerImage {

	/** image file name for day images with file ending */
	public File imageDay = new File("");
	
	/** image file name for night images with file ending */
	public File imageNight = new File("");
	
	/** buffered day image */
	public BufferedImage bufferedDay = null;
	
	/** buffered night image */
	public BufferedImage bufferedNight = null;
	
	private void loadImageDay(File file){
		try {
			bufferedDay = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadImageNight(File file){
		try {
			bufferedNight = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setDay(File file){
		imageDay = file;
		bufferedDay = null;
	}
	
	public BufferedImage getImageDay(){
		if(imageDay.exists() && bufferedDay == null){
			loadImageDay(imageDay);
		}
		return bufferedDay;
	}
	
	public void setNight(File file){
		imageNight = file;
		bufferedNight = null;
	}
	
	public BufferedImage getImageNight(){
		if(imageNight.exists() && bufferedNight == null){
			loadImageNight(imageNight);
		}
		return bufferedNight;
	}
	
	public LayerImage copy(){
		LayerImage image = new LayerImage();
		image.setDay(this.imageDay);
		image.setNight(this.imageNight);
		
		return image;
	}
}