package de.easemotion.fie.view;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Keyboard.KeyLocation;

import sun.font.TextLabel;
import sun.org.mozilla.javascript.ast.CatchClause;
import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.model.graphics.GraphicSurface;
import de.easemotion.fie.model.graphics.ImageLayer;
import de.easemotion.fie.model.graphics.Layer;
import de.easemotion.fie.model.graphics.TextLayer;

public class PropertyPanel extends BoxPane implements Observer {

	private static final String TAG = PropertyPanel.class.getSimpleName();

	private GraphicSurface surface;

	private EditorApplication editor;

	private TextInput propName;
	private TextInput propImage;
	private TextInput propWidth;
	private TextInput propHeight;
	private TextInput propLeft;
	private TextInput propTop;
	private TextInput propRotation;

	public PropertyPanel(EditorApplication editor, GraphicSurface surface){
		this.editor = editor;
		this.surface = surface;

		try {
			BXMLSerializer s = new BXMLSerializer();
			Component component = (Component) s.readObject(PropertyPanel.class, "layer_properties_pane.bxml");
			this.add(component);

			propName = (TextInput) s.getNamespace().get("prop_id");
			propName.getComponentKeyListeners().add(keyListener);
			propImage = (TextInput) s.getNamespace().get("prop_file");
			propImage.getComponentKeyListeners().add(keyListener);
			propLeft = (TextInput) s.getNamespace().get("prop_left");
			propLeft.getComponentKeyListeners().add(keyListener);
			propTop = (TextInput) s.getNamespace().get("prop_top");
			propTop.getComponentKeyListeners().add(keyListener);
			propRotation = (TextInput) s.getNamespace().get("prop_direction");
			propRotation.getComponentKeyListeners().add(keyListener);

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
				propImage.setText(imageLayer.getImageDay()+"");
				propRotation.setText(imageLayer.getRotation()+"");
			} else if(active instanceof TextLayer){
				// TODO implement
			}
		} else {
			propName.setText("");
			propImage.setText("");
			propLeft.setText("");
			propTop.setText("");
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
					imageLayer.setRotation(Integer.parseInt(propRotation.getText()));
					
					File file = new File(propImage.getText());
					if(file.exists()){
						imageLayer.setImageDay(file.getAbsolutePath());
					}
				}

			} catch (NumberFormatException e){

			}
			surface.updateObservers();
		}
	}

	ComponentKeyListener keyListener = new ComponentKeyListener() {

		@Override
		public boolean keyTyped(Component component, char character) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean keyReleased(Component component, int keyCode,
				KeyLocation keyLocation) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean keyPressed(Component component, int keyCode,
				KeyLocation keyLocation) {

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
				default:
					break;
				}


			}

			return false;
		}
	};
}
