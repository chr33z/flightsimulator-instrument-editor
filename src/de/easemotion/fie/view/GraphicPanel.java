package de.easemotion.fie.view;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.ComponentMouseListener;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.FileBrowserSheet.Mode;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.Panel;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.Mouse.Button;

import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.model.graphics.GraphicSurface;
import de.easemotion.fie.model.graphics.Layer;
import de.easemotion.fie.utils.Constants;

/**
 * Canvas in which the instrument panel is drawn and the user performs
 * manipulations on the layers
 * 
 * @author Christopher Gebhardt
 * @date Oct 21, 2014
 * @project Flightsimulator-Instrument-Editor
 *
 */
public class GraphicPanel extends Panel implements Observer {

	private static final String TAG = GraphicPanel.class.getSimpleName();

	private GraphicSurface surface;

	private EditorApplication editor;

	private Layer activeLayer = null;
	private Layer pressedLayer = null;

	private Button lastMouseButton;

	private int currentX;
	private int currentY;

	public GraphicPanel(EditorApplication editor, GraphicSurface surface){
		this.surface = surface;
		this.editor = editor;
		
		this.getComponentMouseButtonListeners().add(mouseMoveButtonListener);
		this.getComponentMouseListeners().add(mouseMovementListener);
		this.getComponentKeyListeners().add(keyListener);
		this.setMenuHandler(menuHandler);
	}

	@Override
	public void paint(Graphics2D g) {
		g.setClip(new Rectangle(0, 0, surface.getWidth(), surface.getHeight()));
		paintGrid(g);

		for (Layer layer : surface.getLayers()) {
			BufferedImage image;
			try {
				File file = new File(layer.getImage());
				System.out.println();
				image = ImageIO.read(file);
				g.drawImage(image, layer.getLeft(), layer.getTop(), layer.getWidth(), layer.getHeight(), null);

				if(layer.isActive()){
					Paint paint = g.getPaint();
					g.setPaint(Constants.paint.LAYER_ACTIVE_BORDER);
					g.setStroke(new BasicStroke(2));
					g.drawRect(layer.getLeft(), layer.getTop(), layer.getWidth(), layer.getHeight());
					g.setPaint(paint);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		super.paint(g);
	}

	/**
	 * Paint a background grid
	 * @param g
	 */
	private void paintGrid(Graphics2D g){
		Paint paint = g.getPaint();

		int size = Constants.integer.GRID_SIZE;
		for (int i = 0; i < surface.getWidth() / size; i++) {
			for (int j = 0; j < surface.getHeight() / size; j++) {
				if((i+j) % 2 == 0){
					g.setPaint(Constants.paint.GRID_LIGHT);
				} else {
					g.setPaint(Constants.paint.GRID_DARK);
				}
				g.fillRect(size * i, size * j, size, size);
			}
		}

		g.setPaint(paint);
	}

	ComponentMouseButtonListener mouseMoveButtonListener = new ComponentMouseButtonListener() {

		@Override
		public boolean mouseUp(Component component, Button button, int x, int y) {
			System.out.println("Mouse up");
			pressedLayer = null;
			return false;
		}

		@Override
		public boolean mouseDown(Component component, Button button, int x, int y) {
			System.out.println("Mouse down");

			switch (button) {
			case RIGHT:

				break;
			case LEFT:
				break;
			default:
				break;
			}

			Layer newActive = getClickedLayer(x, y);
			if (newActive != null) {
				pressedLayer = newActive;

				// deactivate layer when clicked again
				if(activeLayer != null && newActive.getId().equals(activeLayer.getId())){
					surface.setLayersInactive();
					System.out.println("Layer "+activeLayer.getId()+" deactivated.");
					activeLayer = null;
				} else {
					activeLayer = newActive;
					surface.setLayerActive(activeLayer);
					System.out.println("Layer "+activeLayer.getId()+" is active.");
					GraphicPanel.this.setFocused(true, null);
				}
			} else {
				surface.setLayersInactive();
			}

			repaint();
			lastMouseButton = button;
			currentX = x;
			currentY = y;
			return false;
		}

		@Override
		public boolean mouseClick(Component component, Button button, int x, int y,
				int count) {
			return false;
		}
	};

	ComponentMouseListener mouseMovementListener = new ComponentMouseListener() {

		@Override
		public void mouseOver(Component component) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseOut(Component component) {
			pressedLayer = null;
		}

		@Override
		public boolean mouseMove(Component component, int x, int y) {
			if(pressedLayer != null && lastMouseButton == Button.LEFT){
				pressedLayer.setLeft(pressedLayer.getLeft() + (x - currentX));
				pressedLayer.setTop(pressedLayer.getTop() + (y - currentY));
				repaint();
			}

			currentX = x;
			currentY = y;
			return false;
		}
	};

	ComponentKeyListener keyListener = new ComponentKeyListener() {

		@Override
		public boolean keyTyped(Component component, char character) {
			System.out.println("KEY TYP");
			return false;
		}

		@Override
		public boolean keyReleased(Component component, int keyCode,
				KeyLocation keyLocation) {
			System.out.println("KEY REL");
			return false;
		}

		@Override
		public boolean keyPressed(Component component, int keyCode,
				KeyLocation keyLocation) {
			System.out.println("KEY PRE");

			switch (keyCode) {
			case KeyCode.LEFT:
				System.out.println("left");
				break;
			case KeyCode.UP:
				System.out.println("up");
				break;
			case KeyCode.RIGHT:
				System.out.println("right");
				break;
			case KeyCode.DOWN:
				System.out.println("down");
				break;

			default:
				break;
			}
			return false;
		}
	};

	private MenuHandler menuHandler = new MenuHandler.Adapter() {

		@Override
		public boolean configureContextMenu(Component component, Menu menu, int x, int y) {
			Menu.Section menuSection = new Menu.Section();
			menu.getSections().add(menuSection);

			Menu.Item addLayerItem = new Menu.Item("Add layer");
			addLayerItem.setAction(new Action() {
				@Override
				public void perform(Component source) {
					createLayerDialog();
				}
			});
			Menu.Item bringForward = new Menu.Item("Move forward");
			bringForward.setAction(new Action() {

				@Override
				public void perform(Component source) {
					moveForward(activeLayer);
				}
			});
			Menu.Item moveBackward = new Menu.Item("Move backward");
			moveBackward.setAction(new Action() {

				@Override
				public void perform(Component source) {
					moveBackward(activeLayer);
				}
			});
			Menu.Item deleteLayer = new Menu.Item("Delete layer");
			deleteLayer.setAction(new Action() {

				@Override
				public void perform(Component source) {
					deleteLayer(activeLayer);
				}
			});

			menuSection.add(addLayerItem);
			if(activeLayer != null){
				menuSection.add(bringForward);
				menuSection.add(moveBackward);
				menuSection.add(deleteLayer);
			}

			return false;
		}
	};

	private Layer getClickedLayer(int x, int y){
		Layer result = null;

		for (Layer layer : surface.getLayers()) {
			if( (x > layer.getLeft() && x < layer.getLeft() + layer.getWidth()) && 
					(y > layer.getTop() && y < layer.getTop() + layer.getHeight())){
				result = layer;
			}
		}

		return result;
	}

	private void createLayerDialog(){
		ArrayList<String> options = new ArrayList<String>();
		options.add("Ebene erstellen");
		options.add("Abbrechen");

		Component body = null;
		BXMLSerializer serializer = new BXMLSerializer();
		try {
			body = (Component) serializer.readObject(GraphicPanel.class, "layer_creation_dialog.bxml");


			final Dialog dialog = new Dialog(true);
			dialog.setContent(body);
			dialog.setTitle("Neue Ebene erstellen");
			dialog.open(editor.window);

			final TextInput layerId = (TextInput) serializer.getNamespace().get("layer_id");
			final TextInput layerImagePath = (TextInput) serializer.getNamespace().get("layer_file");
			final TextInput layerWidth = (TextInput) serializer.getNamespace().get("layer_width");
			final TextInput layerHeight = (TextInput) serializer.getNamespace().get("layer_height");
			final TextInput layerLeft = (TextInput) serializer.getNamespace().get("layer_left");
			final TextInput layerTop = (TextInput) serializer.getNamespace().get("layer_top");

			PushButton selectFile = (PushButton) serializer.getNamespace().get("select_file");
			selectFile.getButtonPressListeners().add(new ButtonPressListener() {

				@Override
				public void buttonPressed(org.apache.pivot.wtk.Button button) {
					final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();
					fileBrowserSheet.setMode(Mode.OPEN);
					fileBrowserSheet.open(editor.window, new SheetCloseListener() {
						@Override
						public void sheetClosed(Sheet sheet) {
							if (sheet.getResult()) {
								Sequence<File> selectedFiles = fileBrowserSheet.getSelectedFiles();

								if(selectedFiles.getLength() > 0){
									layerImagePath.setText(selectedFiles.get(0).getAbsolutePath());
								}
							}
						}
					});
				}
			});

			PushButton submit = (PushButton) serializer.getNamespace().get("submit");
			submit.getButtonPressListeners().add(new ButtonPressListener() {

				@Override
				public void buttonPressed(org.apache.pivot.wtk.Button button) {
					try {
						int width = Integer.parseInt(layerWidth.getText());
						int height = Integer.parseInt(layerHeight.getText());
						int left = Integer.parseInt(layerLeft.getText());
						int top = Integer.parseInt(layerTop.getText());

						createLayer(layerId.getText(), layerImagePath.getText(), width, height, left, top);
						dialog.close();
					} catch (NumberFormatException e){
						// FIXME implement errorhandling
					}

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

	private void createLayer(String id, String imagePath, int width, int height, int left, int top){
		Layer layer = new Layer()
		.setWidth(width)
		.setHeight(height)
		.setLeft(left)
		.setTop(top)
		.setId(id);

		File file = new File(imagePath);
		if(file.exists()){
			layer.setImage(imagePath);
		}
		surface.addLayer(layer);

		repaint();
	}

	private void moveForward(Layer layer){
		if(layer != null){
			int index = 0;
			Iterator<Layer> iterator = surface.getLayers().iterator();
			while (iterator.hasNext()) {
				// find layer
				if(layer.getId().equals(iterator.next().getId())){
					iterator.remove();
					break;
				}
				index++;
			}
			try {
				surface.addLayer(index + 1, layer);
			} catch (IndexOutOfBoundsException e){
				surface.addLayer(layer);
			}
			repaint();
		}
	}

	private void moveBackward(Layer layer){
		if(layer != null){
			int index = 0;
			Iterator<Layer> iterator = surface.getLayers().iterator();
			while (iterator.hasNext()) {
				// find layer
				if(layer.getId().equals(iterator.next().getId())){
					iterator.remove();
					break;
				}
				index++;
			}
			try {
				surface.addLayer(index - 1, layer);
			} catch (IndexOutOfBoundsException e){
				surface.addLayer(0, layer);
			}
			repaint();
		}
	}

	private void deleteLayer(Layer layer){
		surface.deleteLayer(layer);
	}

	@Override
	public void update(Observable o, Object arg) {
		System.out.println("Graphic updated");
		repaint();
	}
}
