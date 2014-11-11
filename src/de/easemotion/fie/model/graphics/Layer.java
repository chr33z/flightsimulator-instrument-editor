package de.easemotion.fie.model.graphics;

import java.util.Observable;


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
public abstract class Layer {
	private static final String TAG = Layer.class.getSimpleName();
	
	public enum Type {
		IMAGE, TEXT
	}
	
	/** human readable id for this layer (like "hour-dial") */
	protected String id = "";
	
	protected int left = 0;
	protected int top = 0;
	
	/** The part of the lua script, responsible for this layer */
	protected String luaScript = "";
	
	protected Instrument parent;
	
	/** whether this layer is active in graphic panel */
	boolean active = false;
	
	/** True if this layer is visible in the graphic editor */
	protected boolean visible = true;
	
	public Layer(){
		// FIXME implement
	}
	
	public void setParent(Instrument parent){
		this.parent = parent;
	}

	public String getId() {
		return id;
	}

	public Layer setId(String id) {
		this.id = id;
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
	
	public void resetPosition(){
		top = 0;
		left = 0;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getLuaScript() {
		return luaScript;
	}

	public void setLuaScript(String luaScript) {
		this.luaScript = luaScript;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public abstract Layer copy(Instrument instrument);
}
