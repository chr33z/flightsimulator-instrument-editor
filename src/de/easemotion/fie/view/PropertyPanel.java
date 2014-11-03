package de.easemotion.fie.view;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Pattern;

import javax.security.auth.callback.TextOutputCallback;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.validation.RegexTextValidator;

import sun.font.TextLabel;
import sun.org.mozilla.javascript.ast.CatchClause;
import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.model.graphics.Instrument;
import de.easemotion.fie.model.graphics.ImageLayer;
import de.easemotion.fie.model.graphics.Layer;
import de.easemotion.fie.model.graphics.TextLayer;

public class PropertyPanel extends BoxPane implements Observer {

	private static final String TAG = PropertyPanel.class.getSimpleName();

	private Instrument surface;

	public enum Attribute {
		ID
	}

	private EditorApplication editor;

	private TextInput propName;
	private TextInput propLeft;
	private TextInput propTop;
	private TextInput propPivotLeft;
	private TextInput propPivotTop;
	private TextInput propRotation;

	public PropertyPanel(EditorApplication editor, Instrument surface){
		this.editor = editor;
		this.surface = surface;

		try {
			BXMLSerializer s = new BXMLSerializer();
			Component component = (Component) s.readObject(PropertyPanel.class, "layer_properties_pane.bxml");
			this.add(component);

			propName = (TextInput) s.getNamespace().get("prop_id");
			propName.getComponentKeyListeners().add(keyListener);
			propName.setAttribute(Attribute.ID, "prop_id");

			propLeft = (TextInput) s.getNamespace().get("prop_left");
			propLeft.getComponentKeyListeners().add(keyListener);
			propLeft.setAttribute(Attribute.ID, "prop_left");

			propTop = (TextInput) s.getNamespace().get("prop_top");
			propTop.getComponentKeyListeners().add(keyListener);
			propTop.setAttribute(Attribute.ID, "prop_top");
			
			propPivotLeft = (TextInput) s.getNamespace().get("prop_pivot_left");
			propPivotLeft.getComponentKeyListeners().add(keyListener);
			propPivotLeft.setAttribute(Attribute.ID, "prop_pivot_left");
			
			propPivotTop = (TextInput) s.getNamespace().get("prop_pivot_top");
			propPivotTop.getComponentKeyListeners().add(keyListener);
			propPivotTop.setAttribute(Attribute.ID, "prop_pivot_top");
			
			propRotation = (TextInput) s.getNamespace().get("prop_direction");
			propRotation.getComponentKeyListeners().add(keyListener);
			propRotation.setAttribute(Attribute.ID, "prop_direction");
			
		} catch (IOException | SerializationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		System.out.println("Property updated");

		Layer active = surface.getActiveLayer();
		if(active != null){
			propName.setText(active.getId()+"");
			propLeft.setText(active.getLeft()+"");
			propTop.setText(active.getTop()+"");

			if(active instanceof ImageLayer){
				ImageLayer imageLayer = (ImageLayer) active;
				propRotation.setText(imageLayer.getRotation()+"");
				propPivotLeft.setText(imageLayer.getPivotX()+"");
				propPivotTop.setText(imageLayer.getPivotY()+"");
			}
			else if(active instanceof TextLayer){
				// TODO implement
			}
		} else {
			propName.setText("");
			propLeft.setText("");
			propTop.setText("");
			propPivotLeft.setText("");
			propPivotTop.setText("");
			propRotation.setText("");
		}
	}

	private void updateLayer(){
		Layer layer = surface.getActiveLayer();
		if(layer != null){
			try {
				layer.setId(propName.getText());
				layer.setLeft(Integer.parseInt(propLeft.getText()));
				layer.setTop(Integer.parseInt(propTop.getText()));

				if(layer instanceof ImageLayer){
					ImageLayer imageLayer = (ImageLayer) layer;
					imageLayer.setPivotX(Integer.parseInt(propPivotLeft.getText()));
					imageLayer.setPivotY(Integer.parseInt(propPivotTop.getText()));
					imageLayer.setRotation(Integer.parseInt(propRotation.getText()));
				}

			} catch (NumberFormatException e){

			}
			surface.updateObservers();
		}
	}

	ComponentKeyListener keyListener = new ComponentKeyListener() {

		@Override
		public boolean keyTyped(Component component, char character) {
			System.out.println("typed");
			
			if(component instanceof TextInput){
				TextInput input = (TextInput) component;

				String id = null;
				try {
					id = (String) component.getAttribute(Attribute.ID);
				} catch(NullPointerException | ClassCastException e){

				}

				if(id != null && id.equals("prop_id")){
					if(!String.valueOf(character).matches("([A-Za-z0-9\\_\b]+)")){
						String txt = input.getText();
						input.setText(txt.substring(0, txt.length()-1));
						return true;
					}
				}
				if(id != null && (id.equals("prop_left") || 
						id.equals("prop_top") || 
						id.equals("prop_pivot_left") || 
						id.equals("prop_pivot_top") ||
						id.equals("prop_direction"))){
					
					if(!String.valueOf(character).matches("([0-9\b]+)")){
						String txt = input.getText();
						input.setText(txt.substring(0, txt.length()-1));
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean keyReleased(Component component, int keyCode,
				KeyLocation keyLocation) {
			System.out.println("released");
			return false;
		}

		@Override
		public boolean keyPressed(Component component, int keyCode,
				KeyLocation keyLocation) {
			System.out.println("pressed");

			if(component instanceof TextInput){
				switch (keyCode) {
				case KeyCode.ENTER:
					updateLayer();
					break;
				case KeyCode.RIGHT:
				case KeyCode.UP:
					TextInput input1 = (TextInput) component;
					try {
						int value = Integer.parseInt(input1.getText());
						input1.setText((++value)+"");
					} catch (NumberFormatException e){
						// is not an integer
					}
					updateLayer();
					break;
				case KeyCode.LEFT:
				case KeyCode.DOWN:
					TextInput input2 = (TextInput) component;
					try {
						int value = Integer.parseInt(input2.getText());
						input2.setText((--value)+"");
					} catch (NumberFormatException e){
						// is not an integer
					}
					updateLayer();
					break;
				case KeyCode.BACKSPACE:
					return true;
				default:
					break;
				}
			}

			return false;
		}
	};
}
