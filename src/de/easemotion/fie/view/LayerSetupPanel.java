package de.easemotion.fie.view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.FileBrowserSheet.Mode;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.effects.ReflectionDecorator;

import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.model.graphics.GraphicSurface;
import de.easemotion.fie.model.graphics.ImageLayer;
import de.easemotion.fie.model.graphics.Layer;
import de.easemotion.fie.utils.Constants;

public class LayerSetupPanel extends BoxPane implements Observer {
	
	private static final String TAG = LayerSetupPanel.class.getSimpleName();

	private GraphicSurface surface;

	private EditorApplication editor;

	private BoxPane layerListContainer;

	private List<Component> layerItems = new ArrayList<Component>(Constants.integer.MAX_LAYER_COUNT);
	
	private enum Attribute {
		LAYER
	}

	public LayerSetupPanel(EditorApplication editor, GraphicSurface surface){
		this.editor = editor;
		this.surface = surface;

		try {
			BXMLSerializer s = new BXMLSerializer();
			Component component = (Component) s.readObject(LayerSetupPanel.class, "layer_pane.bxml");
			this.add(component);

			layerListContainer = (BoxPane) s.getNamespace().get("layer_list_container");
			
			renderLayers();

		} catch (IOException | SerializationException e) {
			e.printStackTrace();
		}
	}

	private void renderLayers(){
		layerListContainer.removeAll();
		layerItems.clear();

		try {
			for (int i = 0; i < Constants.integer.MAX_LAYER_COUNT; i++) {
				BXMLSerializer s = new BXMLSerializer();
				Component item = (Component) s.readObject(LayerSetupPanel.class, "layer_pane_row.bxml");
				
				PushButton actionDay = (PushButton)s.getNamespace().get("layer_button_day");
				PushButton actionNight = (PushButton)s.getNamespace().get("layer_button_night");
				PushButton actionUp = (PushButton)s.getNamespace().get("layer_button_up");
				PushButton actionDown = (PushButton)s.getNamespace().get("layer_button_down");
				PushButton actionEdit = (PushButton)s.getNamespace().get("layer_button_edit");
				PushButton actionHand = (PushButton)s.getNamespace().get("layer_button_hand");
				PushButton actionVisibility = (PushButton)s.getNamespace().get("layer_button_visibility");
				PushButton actionDelete = (PushButton)s.getNamespace().get("layer_button_delete");
				/*
				 * If there is a corresponding layer, read the data and fill the row
				 */
				if(i < surface.getLayers().size()){
					Layer layer = surface.getLayers().get(i);
					((Label) s.getNamespace().get("layer_label")).setText(layer.getId());
					
					actionDay.setEnabled(true);
					actionDay.setAttribute(Attribute.LAYER, layer);
					actionDay.getButtonPressListeners().add(actionSelectDayImage);
					
					actionNight.setEnabled(true);
					actionNight.setAttribute(Attribute.LAYER, layer);
					actionNight.getButtonPressListeners().add(actionSelectNightImage);
					
					actionUp.setEnabled(true);
					actionUp.setAttribute(Attribute.LAYER, layer);
					actionUp.getButtonPressListeners().add(actionUpListener);
					
					actionDown.setEnabled(true);
					actionDown.setAttribute(Attribute.LAYER, layer);
					actionDown.getButtonPressListeners().add(actionDownListener);
					
					actionEdit.setEnabled(true);
					actionEdit.setAttribute(Attribute.LAYER, layer);
					
					actionHand.setEnabled(true);
					actionHand.setAttribute(Attribute.LAYER, layer);
					
					actionVisibility.setEnabled(true);
					actionVisibility.setAttribute(Attribute.LAYER, layer);
					actionVisibility.getButtonPressListeners().add(actionToggleVisibilityListener);
					
					actionDelete.setEnabled(true);
					actionDelete.setAttribute(Attribute.LAYER, layer);
					actionDelete.getButtonPressListeners().add(actionDeleteListener);
				}
				
				layerItems.add(item);
			}
		} catch (IOException | SerializationException e) {
			e.printStackTrace();
		}

		for (Component item : layerItems) {
			layerListContainer.add(item);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		System.out.println("Layer Panel updated");
		
		renderLayers();
	}
	
	ButtonPressListener actionUpListener = new ButtonPressListener() {
		@Override
		public void buttonPressed(Button button) {
			System.out.println("Nove layer forward");
			
			Layer layer = (Layer) button.getAttribute(Attribute.LAYER);
			surface.moveLayerForward(layer);
		}
	};
	
	ButtonPressListener actionDownListener = new ButtonPressListener() {
		@Override
		public void buttonPressed(Button button) {
			System.out.println("Nove layer backwards");
			
			Layer layer = (Layer) button.getAttribute(Attribute.LAYER);
			surface.moveLayerBackwards(layer);
		}
	};
	
	ButtonPressListener actionSelectDayImage = new ButtonPressListener() {
		@Override
		public void buttonPressed(Button button) {
			System.out.println("Select day image");
			Layer layer = (Layer) button.getAttribute(Attribute.LAYER);
			
			if(layer instanceof ImageLayer){
				selectLayerImage((ImageLayer)layer, false);
			}
		}
	};
	
	ButtonPressListener actionSelectNightImage = new ButtonPressListener() {
		@Override
		public void buttonPressed(Button button) {
			System.out.println("Select night image");
			Layer layer = (Layer) button.getAttribute(Attribute.LAYER);
			
			if(layer instanceof ImageLayer){
				selectLayerImage((ImageLayer)layer, true);
			}
		}
	};
	
	ButtonPressListener actionToggleVisibilityListener = new ButtonPressListener() {
		@Override
		public void buttonPressed(Button button) {
			System.out.println("Toggle visibility");
			
			Layer layer = (Layer) button.getAttribute(Attribute.LAYER);
			surface.toggleVisibility(layer);
		}
	};
	
	ButtonPressListener actionDeleteListener = new ButtonPressListener() {
		@Override
		public void buttonPressed(Button button) {
			System.out.println("Delete layer");
			
			Layer layer = (Layer) button.getAttribute(Attribute.LAYER);
			createDeleteDialog(layer);
		}
	};

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
					break;
				case KeyCode.LEFT:
				case KeyCode.UP:
					break;
				case KeyCode.RIGHT:
				case KeyCode.DOWN:
					break;
				default:
					break;
				}
			}

			return false;
		}
	};
	
	private void selectLayerImage(final ImageLayer layer, final boolean nightImage){
		File root = EditorApplication.lastFileBrowserPath;
		
		final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();
		fileBrowserSheet.setMode(Mode.OPEN);
		if(root != null && root.exists()){
			fileBrowserSheet.setRootDirectory(root);
		}
		fileBrowserSheet.open(editor.window, new SheetCloseListener() {
			@Override
			public void sheetClosed(Sheet sheet) {
				if (sheet.getResult()) {
					Sequence<File> selectedFiles = fileBrowserSheet.getSelectedFiles();

					if(selectedFiles.getLength() > 0){
						String path = selectedFiles.get(0).getAbsolutePath();
						if(new File(path).exists()){
							if(!nightImage){
								layer.setImageDay(path);
							} else {
								layer.setImageNight(path);
							}
						}
					}
					
					EditorApplication.lastFileBrowserPath = fileBrowserSheet.getSelectedFile().getParentFile();
				}
			}
		});
	}
	
	private void createDeleteDialog(final Layer layer){
		Component body = null;
		BXMLSerializer serializer = new BXMLSerializer();
		try {
			body = (Component) serializer.readObject(GraphicPanel.class, "layer_delete_dialog.bxml");

			final Dialog dialog = new Dialog(true);
			dialog.setContent(body);
			dialog.setTitle("Neue Ebene erstellen");
			dialog.open(editor.window);

			PushButton submit = (PushButton) serializer.getNamespace().get("delete");
			submit.getButtonPressListeners().add(new ButtonPressListener() {

				@Override
				public void buttonPressed(org.apache.pivot.wtk.Button button) {
					surface.deleteLayer(layer);
					dialog.close();
				}
			});
			PushButton cancel = (PushButton) serializer.getNamespace().get("cancel");
			cancel.getButtonPressListeners().add(new ButtonPressListener() {

				@Override
				public void buttonPressed(org.apache.pivot.wtk.Button button) {
					dialog.close();
				}
			});
		} catch(Exception exception) {
			System.err.println(exception);
		}
	}
}
