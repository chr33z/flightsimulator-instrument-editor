package de.easemotion.fie.model;

import de.easemotion.fie.utils.Constants;

public class TextLayer extends Layer {
	
	/** text the layer is showing */
	private String text = "TEXT";

	/** font type */
	private String font = Constants.font.QUARTZ;
	
	/** font size in pixel */
	private int fontSize = 40;
	
	public String getText() {
		return text;
	}

	public TextLayer setText(String text) {
		this.text = text;
		return this;
	}

	public String getFont() {
		return font;
	}

	public TextLayer setFont(String font) {
		this.font = font;
		return this;
	}

	public int getFontSize() {
		return fontSize;
	}

	public TextLayer setFontSize(int fontSize) {
		this.fontSize = fontSize;
		return this;
	}
	
	@Override
	public Layer copy(Instrument instrument) {
		TextLayer layer = new TextLayer();
		layer.text = this.text;
		layer.font = this.font;
		layer.fontSize = this.fontSize;
		
		layer.id = this.id;
		layer.left = this.left;
		layer.top = this.top;
		layer.luaScript = this.luaScript;
		layer.parent = instrument;
		
		return layer;
	}
}