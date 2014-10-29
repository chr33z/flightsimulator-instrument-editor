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

import sun.org.mozilla.javascript.ast.CatchClause;
import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.model.graphics.GraphicSurface;
import de.easemotion.fie.model.graphics.Layer;

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
			propWidth = (TextInput) s.getNamespace().get("prop_width");
			propWidth.getComponentKeyListeners().add(keyListener);
			propHeight = (TextInput) s.getNamespace().get("prop_height");
			propHeight.getComponentKeyListeners().add(keyListener);
			propLeft = (TextInput) s.getNamespace().get("prop_left");
			propLeft.getComponentKeyListeners().add(keyListener);
			propTop = (TextInput) s.getNamespace().get("prop_top");
			propTop.getComponentKeyListeners().add(keyListener);

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
			propImage.setText(active.getImageDay()+"");
			propWidth.setText(active.getWidth()+"");
			propHeight.setText(active.getHeight()+"");
			propLeft.setText(active.getLeft()+"");
			propTop.setText(active.getTop()+"");
		} else {
			propName.setText("");
			propImage.setText("");
			propWidth.setText("");
			propHeight.setText("");
			propLeft.setText("");
			propTop.setText("");
		}
	}

	private void updateLayer(){
		Layer layer = surface.getActiveLayer();
		if(layer != null){
			try {
				layer.setId(propName.getText());
				layer.setWidth(Integer.parseInt(propWidth.getText()));
				layer.setHeight(Integer.parseInt(propHeight.getText()));
				layer.setLeft(Integer.parseInt(propLeft.getText()));
				layer.setTop(Integer.parseInt(propTop.getText()));
				
				File file = new File(propImage.getText());
				if(file.exists()){
					layer.setImageDay(file.getAbsolutePath());
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
				case KeyCode.LEFT:
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
				case KeyCode.RIGHT:
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
