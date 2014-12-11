package de.easemotion.fie.view;

import java.awt.EventQueue;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Keyboard.KeyLocation;

import com.sun.awt.AWTUtilities;

import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.model.ImageLayer;
import de.easemotion.fie.model.Instrument;
import de.easemotion.fie.model.Layer;
import de.easemotion.fie.model.TextLayer;
import de.easemotion.fie.simulation.SimulationData;
import de.easemotion.fie.utils.Constants;

public class PropertyPanel extends BoxPane implements Observer {

	private static final String TAG = PropertyPanel.class.getSimpleName();

	private Instrument instrument;

	public enum Attribute {
		ID, INPUT_COMPONENT_1, INPUT_COMPONENT_2
	}

	private EditorApplication editor;

	private TextInput propName;
	private TextInput propLeft;
	private TextInput propTop;
	private TextInput propPivotLeft;
	private TextInput propPivotTop;
	private TextInput propRotation;
	
	private LinkButton buttonName;
	private LinkButton buttonPosition;
	private LinkButton buttonPivot;
	private LinkButton buttonDirection;
	
	boolean interfaceActive = true;

	public PropertyPanel(EditorApplication editor, Instrument surface){
		this.editor = editor;
		this.instrument = surface;

		try {
			BXMLSerializer s = new BXMLSerializer();
			Component component = (Component) s.readObject(PropertyPanel.class, "layer_properties_pane.bxml");
			this.add(component);
			
			propName = (TextInput) s.getNamespace().get("prop_id");
			propName.getStyles().put("color", Constants.color.TEXT_PRIMARY);
			propName.getStyles().put("font", Constants.font.FONT_REGULAR);
			propName.getComponentKeyListeners().add(keyListener);
			propName.setAttribute(Attribute.ID, "prop_id");

			propLeft = (TextInput) s.getNamespace().get("prop_left");
			propLeft.getStyles().put("color", Constants.color.TEXT_PRIMARY);
			propLeft.getStyles().put("font", Constants.font.FONT_REGULAR);
			propLeft.getComponentKeyListeners().add(keyListener);
			propLeft.setAttribute(Attribute.ID, "prop_left");

			propTop = (TextInput) s.getNamespace().get("prop_top");
			propTop.getStyles().put("color", Constants.color.TEXT_PRIMARY);
			propTop.getStyles().put("font", Constants.font.FONT_REGULAR);
			propTop.getComponentKeyListeners().add(keyListener);
			propTop.setAttribute(Attribute.ID, "prop_top");
			
			propPivotLeft = (TextInput) s.getNamespace().get("prop_pivot_left");
			propPivotLeft.getStyles().put("color", Constants.color.TEXT_PRIMARY);
			propPivotLeft.getStyles().put("font", Constants.font.FONT_REGULAR);
			propPivotLeft.getComponentKeyListeners().add(keyListener);
			propPivotLeft.setAttribute(Attribute.ID, "prop_pivot_left");
			
			propPivotTop = (TextInput) s.getNamespace().get("prop_pivot_top");
			propPivotTop.getStyles().put("color", Constants.color.TEXT_PRIMARY);
			propPivotTop.getStyles().put("font", Constants.font.FONT_REGULAR);
			propPivotTop.getComponentKeyListeners().add(keyListener);
			propPivotTop.setAttribute(Attribute.ID, "prop_pivot_top");
			
			propRotation = (TextInput) s.getNamespace().get("prop_direction");
			propRotation.getStyles().put("color", Constants.color.TEXT_PRIMARY);
			propRotation.getStyles().put("font", Constants.font.FONT_REGULAR);
			propRotation.getComponentKeyListeners().add(keyListener);
			propRotation.setAttribute(Attribute.ID, "prop_direction");
			
			/*
			 * Buttons
			 */
			buttonName = (LinkButton) s.getNamespace().get("button_id_delete");
			buttonName.getButtonPressListeners().add(nameDeleteListener);
			
			buttonPosition = (LinkButton) s.getNamespace().get("button_pos_delete");
			buttonPosition.getButtonPressListeners().add(positionDeleteListener);
			
			buttonPivot = (LinkButton) s.getNamespace().get("button_pivot_delete");
			buttonPivot.getButtonPressListeners().add(pivotDeleteListener);
			
			buttonDirection = (LinkButton) s.getNamespace().get("button_direction_delete");
			buttonDirection.getButtonPressListeners().add(directionDeleteListener);
			
		} catch (IOException | SerializationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		instrument = editor.getInstrument();
		
		if(o instanceof SimulationData){
			interfaceActive = !((SimulationData) o).isSimulationActive();
			
			propName.setEditable(interfaceActive);
			propLeft.setEditable(interfaceActive);
			propTop.setEditable(interfaceActive);
			propPivotLeft.setEditable(interfaceActive);
			propPivotTop.setEditable(interfaceActive);
			propRotation.setEditable(interfaceActive);
			
			buttonName.setEnabled(interfaceActive);
			buttonPosition.setEnabled(interfaceActive);
			buttonPivot.setEnabled(interfaceActive);
			buttonDirection.setEnabled(interfaceActive);
		}

		Layer active = instrument.getActiveLayer();
		if(active != null){
			propName.setText(active.getId()+"");
			propLeft.setText(active.getLeft()+"");
			propTop.setText(active.getTop()+"");

			if(active instanceof ImageLayer){
				ImageLayer imageLayer = (ImageLayer) active;
				propRotation.setText(imageLayer.getBias()+"");
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
	
	private ButtonPressListener nameDeleteListener = new ButtonPressListener() {
		
		@Override
		public void buttonPressed(Button button) {
			propName.setText("");
			updateLayer();
		}
	};
	
	private ButtonPressListener positionDeleteListener = new ButtonPressListener() {
		
		@Override
		public void buttonPressed(Button button) {
			try {
				ImageLayer layer = (ImageLayer) instrument.getActiveLayer();
				layer.resetPosition();
				instrument.updateObservers();
			} catch(ClassCastException e){
				// not an image layer
			}
		}
	};
	
	private ButtonPressListener pivotDeleteListener = new ButtonPressListener() {
		
		@Override
		public void buttonPressed(Button button) {
			try {
				ImageLayer layer = (ImageLayer) instrument.getActiveLayer();
				layer.resetPivot();
				instrument.updateObservers();
			} catch(ClassCastException e){
				// not an image layer
			}
		}
	};
	
	private ButtonPressListener directionDeleteListener = new ButtonPressListener() {
		
		@Override
		public void buttonPressed(Button button) {
			propRotation.setText(0+"");
			updateLayer();
		}
	};

	private void updateLayer(){
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				Layer layer = instrument.getActiveLayer();
				if(layer != null){
					try {
						layer.setLeft(Integer.parseInt(propLeft.getText()));
						layer.setTop(Integer.parseInt(propTop.getText()));
						layer.setId(propName.getText());

						if(layer instanceof ImageLayer){
							ImageLayer imageLayer = (ImageLayer) layer;
							imageLayer.setPivotX(Integer.parseInt(propPivotLeft.getText()));
							imageLayer.setPivotY(Integer.parseInt(propPivotTop.getText()));
							imageLayer.setBias(Integer.parseInt(propRotation.getText()));
						}

					} catch (NumberFormatException e){

					}
					instrument.updateObservers();
				}
			}
		});
	}
	
	ComponentKeyListener keyListener = new ComponentKeyListener() {

		@Override
		public boolean keyTyped(Component component, char character) {
			if(component instanceof TextInput){
				if(!interfaceActive){
					return false;
				}
				
				TextInput input = (TextInput) component;
				Layer active = instrument.getActiveLayer();

				String id = null;
				try {
					id = (String) component.getAttribute(Attribute.ID);
				} catch(NullPointerException | ClassCastException e){

				}
				
				if(id != null && id.equals("prop_id")){
					if(!String.valueOf(character).matches("([A-Za-z0-9\\_\b\n\r\\x00\\x08\\x0B\\x0C\\x0E-\\x1F]+)")){
						System.out.println("Character is:"+character);
						
						String txt = input.getText();
						input.setText(txt.substring(0, txt.length()-1));
						return true;
					} else {
						if(active != null){
							active.setId(input.getText());
							updateLayer();
						}
					}
				}
//				if(id != null && (id.equals("prop_left") || 
//						id.equals("prop_top") || 
//						id.equals("prop_pivot_left") || 
//						id.equals("prop_pivot_top") ||
//						id.equals("prop_direction"))){
//					
//					if(String.valueOf(character).matches("([0-9\b\n\r\\x00\\x08\\x0B\\x0C\\x0E-\\x1F]+)")){
//						System.out.println("Character is:"+character);
//						
//						String txt = input.getText();
//						input.setText(txt.substring(0, txt.length()-1));
//						return true;
//					}
//				}
				if(id != null && (id.equals("prop_left") || 
						id.equals("prop_top") || 
						id.equals("prop_pivot_left") || 
						id.equals("prop_pivot_top") ||
						id.equals("prop_direction"))){
					
					String txt = input.getText();
					if(!String.valueOf(character).matches("([0-9\t\b\n\r\\x00\\x08\\x0B\\x0C\\x0E-\\x1F]+)")){
						/*
						 * Filter any unwanted characters out of the typed keys. 
						 */
						input.setText(txt.substring(0, txt.length()-1));
						return true;
					}
					else if (String.valueOf(character).matches("([\t]+)")){
						/*
						 * Save all input fields on Tab key
						 */
						updateLayer();
					}
				}
			}
			return false;
		}

		@Override
		public boolean keyReleased(Component component, int keyCode,
				KeyLocation keyLocation) {
			return false;
		}

		@Override
		public boolean keyPressed(Component component, int keyCode,
				KeyLocation keyLocation) {
			if(!interfaceActive){
				return false;
			}
			
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
