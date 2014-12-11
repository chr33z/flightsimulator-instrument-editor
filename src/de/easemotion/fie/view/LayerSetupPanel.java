package de.easemotion.fie.view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.FileBrowserSheet.Mode;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Keyboard.KeyLocation;

import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.model.ImageLayer;
import de.easemotion.fie.model.Instrument;
import de.easemotion.fie.model.Layer;
import de.easemotion.fie.model.TextLayer;
import de.easemotion.fie.simulation.SimulationData;
import de.easemotion.fie.utils.Constants;
import de.easemotion.fie.utils.IconLoader;
import de.easemotion.fie.utils.IconLoader.Icon;

public class LayerSetupPanel extends BoxPane implements Observer {

	private static final String TAG = LayerSetupPanel.class.getSimpleName();

	private Instrument instrument;
	
	private SimulationData simData;

	private EditorApplication editor;

	private BoxPane layerListContainer;

	private List<Component> layerItems = new ArrayList<Component>(Constants.integer.MAX_LAYER_COUNT);

	private enum Attribute {
		LAYER, NEW_LAYER, LAYER_ID
	}

	public LayerSetupPanel(EditorApplication editor, Instrument surface){
		this.editor = editor;
		this.instrument = surface;

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
		
		boolean buttonIsActive = simData != null ? !simData.isSimulationActive() : true;

		try {
			List<Layer> layerList = new ArrayList<Layer>(instrument.getLayers());
			Collections.reverse(layerList);

			for (int i = 0; i < Constants.integer.MAX_LAYER_COUNT; i++) {
				BXMLSerializer s = new BXMLSerializer();
				Component item = (Component) s.readObject(LayerSetupPanel.class, "layer_pane_row.bxml");

				LinkButton actionDay = (LinkButton)s.getNamespace().get("layer_button_day");
				LinkButton actionNight = (LinkButton)s.getNamespace().get("layer_button_night");
				LinkButton actionText = (LinkButton)s.getNamespace().get("layer_button_text");
				LinkButton actionUp = (LinkButton)s.getNamespace().get("layer_button_up");
				LinkButton actionDown = (LinkButton)s.getNamespace().get("layer_button_down");
				LinkButton actionEdit = (LinkButton)s.getNamespace().get("layer_button_edit");
				LinkButton actionVisibility = (LinkButton)s.getNamespace().get("layer_button_visibility");
				LinkButton actionDelete = (LinkButton)s.getNamespace().get("layer_button_delete");

				/*
				 * If there is a corresponding layer, read the data and fill the row
				 */
				final Layer layer = layerList.get(i);
				
				if(layer != null && i < instrument.getLayers().size()){
					boolean isActive = layer.isActive();

					/*
					 * Layer name label
					 */
					Label labelName = (Label) s.getNamespace().get("layer_label");
					labelName.setAttribute(Attribute.LAYER, layer);
					labelName.getStyles().put("font", Constants.font.FONT_REGULAR);
					
					if(layer.getId().equals("")){
						labelName.setText("NO LAYER NAME");
						labelName.setTooltipText("NO LAYER NAME");
						labelName.getStyles().put("color", Constants.color.TEXT_DARK_GREY);
					}
					else {
						labelName.setText(layer.getId());
						labelName.setTooltipText(layer.getId());
						labelName.getStyles().put("color", Constants.color.TEXT_PRIMARY);
					}
					labelName.getComponentMouseButtonListeners().add(activeClickListener);
					
					/*
					 * Layer second label
					 */
					Label labelPath = (Label) s.getNamespace().get("layer_content");
					labelPath.setAttribute(Attribute.LAYER, layer);
					labelPath.getStyles().put("font", Constants.font.FONT_REGULAR);
					
					/*
					 * If this layer is an image, enable the buttons to choose another image
					 */
					if(layer instanceof ImageLayer){
						ImageLayer imageLayer = (ImageLayer) layer;

						actionDay.setEnabled(buttonIsActive);
						actionDay.setAttribute(Attribute.LAYER, layer);
						actionDay.setAttribute(Attribute.LAYER_ID, i);
						actionDay.getButtonPressListeners().add(actionSelectDayImage);
						
						actionNight.setEnabled(buttonIsActive);
						actionNight.setAttribute(Attribute.LAYER, layer);
						actionNight.setAttribute(Attribute.LAYER_ID, i);
						actionNight.getButtonPressListeners().add(actionSelectNightImage);
						
						/*
						 * Layer second label logic
						 */
						if(imageLayer.getImageDay() == null){
							labelPath.setText("NO FILE ALLOC");
							labelPath.setTooltipText("NO FILE ALLOC");
							labelPath.getStyles().put("color", Constants.color.TEXT_DARK_GREY);
						}
						else {
							File dayFile = imageLayer.getImage().imageDay;
							if(dayFile != null){
								labelPath.setText(dayFile.getAbsolutePath());
								labelPath.setTooltipText(dayFile.getAbsolutePath());
								labelPath.getStyles().put("color", Constants.color.TEXT_PRIMARY);
							} else {
								
							}
						}
						labelPath.getComponentMouseButtonListeners().add(activeClickListener);

						if(imageLayer.getImageDay() != null && imageLayer.getImageDay() != null){
							actionDay.setButtonData(IconLoader.icons.get(Icon.DAY)[IconLoader.LOADED]);
						} else if(imageLayer.getImageDay() == null){
							actionDay.setButtonData(IconLoader.icons.get(Icon.DAY)[IconLoader.MISSING]);
						}
						else if(isActive){
							actionDay.setButtonData(IconLoader.icons.get(Icon.DAY)[IconLoader.ACTIVE]);
						}

						if(imageLayer.getImageNight() != null && imageLayer.getImageNight() != null){
							actionNight.setButtonData(IconLoader.icons.get(Icon.NIGHT)[IconLoader.LOADED]);
						} else if(imageLayer.getImageNight() == null){
							actionNight.setButtonData(IconLoader.icons.get(Icon.NIGHT)[IconLoader.MISSING]);
						}
						else if(isActive){
							actionNight.setButtonData(IconLoader.icons.get(Icon.NIGHT)[IconLoader.ACTIVE]);
						}
					} else {
						actionDay.setButtonData(IconLoader.icons.get(Icon.DAY)[IconLoader.DEACTIVE]);
						actionNight.setButtonData(IconLoader.icons.get(Icon.NIGHT)[IconLoader.DEACTIVE]);
						
						actionText.setEnabled(simData.isSimulationActive());
						actionText.setButtonData(IconLoader.icons.get(Icon.TEXT)[IconLoader.LOADED]);
						actionText.setAttribute(Attribute.LAYER, layer);
						actionText.setAttribute(Attribute.LAYER_ID, i);
						actionText.getButtonPressListeners().add(actioncreateTextLayer);
						
						labelPath.setText("TEXT LAYER");
						labelPath.setTooltipText("TEXT LAYER");
						labelPath.getStyles().put("color", Constants.color.TEXT_PRIMARY);
					}

					actionUp.setEnabled(buttonIsActive);
					actionUp.setAttribute(Attribute.LAYER, layer);
					actionUp.setAttribute(Attribute.LAYER_ID, i);
					actionUp.getButtonPressListeners().add(actionUpListener);

					actionDown.setEnabled(buttonIsActive);
					actionDown.setAttribute(Attribute.LAYER, layer);
					actionDown.setAttribute(Attribute.LAYER_ID, i);
					actionDown.getButtonPressListeners().add(actionDownListener);

					actionEdit.setEnabled(buttonIsActive);
					actionEdit.setAttribute(Attribute.LAYER, layer);
					actionEdit.setAttribute(Attribute.LAYER_ID, i);
					actionEdit.getButtonPressListeners().add(actionEditListener);
					if(isActive){
						actionEdit.setButtonData(IconLoader.icons.get(Icon.EDIT)[IconLoader.ACTIVE]);
					} else {
						actionEdit.setButtonData(IconLoader.icons.get(Icon.EDIT)[IconLoader.DEACTIVE]);
					}

					actionVisibility.setEnabled(buttonIsActive);
					actionVisibility.setAttribute(Attribute.LAYER, layer);
					actionVisibility.getButtonPressListeners().add(actionToggleVisibilityListener);
					if(layer.isVisible()){
						actionVisibility.setButtonData(IconLoader.icons.get(Icon.VIEW)[IconLoader.ACTIVE]);
					} else {
						actionVisibility.setButtonData(IconLoader.icons.get(Icon.VIEW)[IconLoader.DEACTIVE]);
					}

					actionDelete.setEnabled(buttonIsActive);
					actionDelete.setAttribute(Attribute.LAYER, layer);
					actionDelete.setAttribute(Attribute.LAYER_ID, i);
					actionDelete.getButtonPressListeners().add(actionDeleteListener);
				}
//				else if(i == instrument.getLayers().size()){
				else {
					/*
					 * the first layout row after existing rows is to create a new layer
					 */
					actionDay.setEnabled(buttonIsActive);
					actionDay.setAttribute(Attribute.NEW_LAYER, true);
					actionDay.setAttribute(Attribute.LAYER_ID, i);
					actionDay.getButtonPressListeners().add(actionSelectDayImage);

					actionNight.setEnabled(buttonIsActive);
					actionNight.setAttribute(Attribute.NEW_LAYER, true);
					actionNight.setAttribute(Attribute.LAYER_ID, i);
					actionNight.getButtonPressListeners().add(actionSelectNightImage);

					actionText.setEnabled(buttonIsActive);
					actionText.setAttribute(Attribute.NEW_LAYER, true);
					actionText.setAttribute(Attribute.LAYER_ID, i);
					actionText.getButtonPressListeners().add(actioncreateTextLayer);
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
		instrument = editor.getInstrument();
		
		if(o instanceof SimulationData){
			simData = (SimulationData) o;
		}
		
		renderLayers();
	}
	
	ComponentMouseButtonListener activeClickListener = new ComponentMouseButtonListener() {

		@Override
		public boolean mouseUp(Component component,
				org.apache.pivot.wtk.Mouse.Button button, int x, int y) {
			return false;
		}

		@Override
		public boolean mouseDown(Component component,
				org.apache.pivot.wtk.Mouse.Button button, int x, int y) {
			return false;
		}

		@Override
		public boolean mouseClick(Component component,
				org.apache.pivot.wtk.Mouse.Button button, int x, int y, int count) {
			Layer layer = (Layer) component.getAttribute(Attribute.LAYER);
			instrument.setLayerActive(layer);
			return false;
		}
	};

	ButtonPressListener actionEditListener = new ButtonPressListener() {
		@Override
		public void buttonPressed(Button button) {
			System.out.println("Edit layer");
			Layer layer = (Layer) button.getAttribute(Attribute.LAYER);
			instrument.setLayerActive(layer);
		}
	};

	ButtonPressListener actionUpListener = new ButtonPressListener() {
		@Override
		public void buttonPressed(Button button) {
			System.out.println("Nove layer forward");

			Layer layer = (Layer) button.getAttribute(Attribute.LAYER);
			instrument.setLayerActive(layer);
			instrument.moveLayerForward(layer);
		}
	};

	ButtonPressListener actionDownListener = new ButtonPressListener() {
		@Override
		public void buttonPressed(Button button) {
			System.out.println("Nove layer backwards");

			Layer layer = (Layer) button.getAttribute(Attribute.LAYER);
			instrument.setLayerActive(layer);
			instrument.moveLayerBackwards(layer);
		}
	};

	ButtonPressListener actionSelectDayImage = new ButtonPressListener() {
		@Override
		public void buttonPressed(Button button) {
			System.out.println("Select day image");
			Layer layer = (Layer) button.getAttribute(Attribute.LAYER);
			int order = (int) button.getAttribute(Attribute.LAYER_ID);

			// create new layer
			if(button.getAttribute(Attribute.NEW_LAYER) != null){
				layer = new ImageLayer();
				instrument.addLayer(Constants.integer.MAX_LAYER_COUNT - (order+1), layer);
			}

			if(layer != null){
				instrument.setLayerActive(layer);

				if(layer instanceof ImageLayer){
					selectLayerImage((ImageLayer)layer, false);
				}
			}
		}
	};

	ButtonPressListener actionSelectNightImage = new ButtonPressListener() {
		@Override
		public void buttonPressed(Button button) {
			System.out.println("Select night image");
			Layer layer = (Layer) button.getAttribute(Attribute.LAYER);
			int order = (int) button.getAttribute(Attribute.LAYER_ID);

			// create new layer
			if(button.getAttribute(Attribute.NEW_LAYER) != null){
				layer = new ImageLayer();
				instrument.addLayer(Constants.integer.MAX_LAYER_COUNT - (order+1), layer);
			}

			if(layer != null){
				instrument.setLayerActive(layer);

				if(layer instanceof ImageLayer){
					selectLayerImage((ImageLayer)layer, true);
				}
			}
		}
	};

	ButtonPressListener actioncreateTextLayer = new ButtonPressListener() {
		@Override
		public void buttonPressed(Button button) {
			System.out.println("CreateTextLayer");
			Layer layer = (Layer) button.getAttribute(Attribute.LAYER);

			// create new layer
			if(button.getAttribute(Attribute.NEW_LAYER) != null){
				layer = new TextLayer().setText("TEXT");
				instrument.addLayer(0, layer);
			}

			if(layer != null){
				instrument.setLayerActive(layer);
			}
		}
	};

	ButtonPressListener actionToggleVisibilityListener = new ButtonPressListener() {
		@Override
		public void buttonPressed(Button button) {
			System.out.println("Toggle visibility");

			Layer layer = (Layer) button.getAttribute(Attribute.LAYER);
			instrument.setLayerActive(layer);
			instrument.toggleVisibility(layer);
		}
	};

	ButtonPressListener actionDeleteListener = new ButtonPressListener() {
		@Override
		public void buttonPressed(Button button) {
			System.out.println("Delete layer");

			Layer layer = (Layer) button.getAttribute(Attribute.LAYER);
			instrument.setLayerActive(layer);
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
		fileBrowserSheet.getStyles().put("hideDisabledFiles", true);
		
		if(root != null && root.exists()){
			fileBrowserSheet.setRootDirectory(root);
		}
		fileBrowserSheet.setDisabledFileFilter(new Filter<File>() {
			
			@Override
			public boolean include(File item) {
				return (item.isFile() && !item.getName().endsWith(Constants.extension.PNG));
			}
		});
		fileBrowserSheet.open(editor.window, new SheetCloseListener() {
			@Override
			public void sheetClosed(Sheet sheet) {
				if (sheet.getResult()) {
					Sequence<File> selectedFiles = fileBrowserSheet.getSelectedFiles();

					if(selectedFiles.getLength() > 0){
						File file = fileBrowserSheet.getSelectedFile();

						if(file.exists()){
							if(!nightImage){
								layer.setImageDay(file);
							} else {
								layer.setImageNight(file);
							}
							instrument.updateObservers();
						}
					}

					EditorApplication.lastFileBrowserPath = fileBrowserSheet.getSelectedFile().getParentFile();
				} else {
					if(layer instanceof ImageLayer){
						// if there are no images in the layer -> delete it
						if( ((ImageLayer)layer).getImageDay() == null &&
							((ImageLayer)layer).getImageNight() == null){
							
							instrument.deleteLayer(layer);
						}
					}
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
					instrument.deleteLayer(layer);
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
