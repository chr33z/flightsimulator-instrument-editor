package de.easemotion.fie.view;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ButtonGroup;
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

import com.sun.org.apache.bcel.internal.generic.Type;

import de.easemotion.fie.EditorApplication;
import de.easemotion.fie.data.LuaScriptParser;
import de.easemotion.fie.model.ImageLayer;
import de.easemotion.fie.model.Instrument;
import de.easemotion.fie.model.Layer;
import de.easemotion.fie.model.TextLayer;
import de.easemotion.fie.model.Instrument.ImageMode;
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

	private static final boolean DRAW_ACTIVE_BOARDER = false;

	private static final boolean LAYER_CLICKABLE = true;

	private Instrument instrument;

	private EditorApplication editor;

	private Layer activeLayer = null;
	private Layer pressedLayer = null;

	private boolean showInstrumentMask = true;

	private boolean showGrid = true;

	private BufferedImage instrumentMask = null;

	private Button lastMouseButton;

	private int currentX;
	private int currentY;

	public GraphicPanel(EditorApplication editor, Instrument instrument){
		this.instrument = instrument;
		this.editor = editor;

		this.setSize(Constants.integer.INSTRUMENT_WIDTH, Constants.integer.INSTRUMENT_HEIGHT);
		this.setPreferredSize(Constants.integer.INSTRUMENT_WIDTH, Constants.integer.INSTRUMENT_HEIGHT);

		this.getComponentMouseButtonListeners().add(mouseMoveButtonListener);
		this.getComponentMouseListeners().add(mouseMovementListener);
		this.getComponentKeyListeners().add(keyListener);
		this.setMenuHandler(menuHandler);
	}

	@Override
	public void paint(Graphics2D g) {
		g.setClip(new Rectangle(0, 0, instrument.getWidth(), instrument.getHeight()));

		// print a background grid
		paintBackgroundGrid(g);

		for (Layer layer : instrument.getLayers()) {
			if(!layer.isVisible()){
				continue;
			}
			if(layer instanceof ImageLayer){
				AffineTransform transform = g.getTransform();

				ImageLayer imageLayer = (ImageLayer) layer;
				BufferedImage image =  instrument.getMode() == 
						ImageMode.DAY ? imageLayer.getImageDay():imageLayer.getImageNight();

				if(image != null){
					int pivotX = imageLayer.getLeft() + imageLayer.getPivotX();
					int pivotY = imageLayer.getTop() + imageLayer.getPivotY();
					g.rotate(Math.toRadians(((ImageLayer) layer).getRotation()), pivotX, pivotY);
					g.drawImage(image, imageLayer.getLeft(), imageLayer.getTop(), 
							imageLayer.getWidth(), imageLayer.getHeight(), null);
				}

				if(DRAW_ACTIVE_BOARDER && imageLayer.isActive()){
					Paint paint = g.getPaint();
					g.setPaint(Constants.paint.LAYER_ACTIVE_BORDER);
					g.setStroke(new BasicStroke(2));
					g.drawRect(imageLayer.getLeft(), imageLayer.getTop(), imageLayer.getWidth(), imageLayer.getHeight());
					g.setPaint(paint);
				}

				g.setTransform(transform);
			}
			else if(layer instanceof TextLayer){
				TextLayer textLayer = (TextLayer) layer;

				// TODO implement
				Paint paint = g.getPaint();
				
				g.setPaint(Constants.paint.TEXT_STANDARD);
				
				/*
				 * NOTE:
				 * Font size on the PI-Client is rendered in Pixels, so a font size of 25 pixels should
				 * here also correspond to a font size of 25 pixels.
				 * Font metrics in java graphics work a little different so we introduce a correcting factor
				 * of now 1.5 to scale the text so it font size matches the pixel height.
				 * 
				 * This factor was found by trying different values and has no formula what so ever.
				 * For other fonts than FONT_GLASS it most likely has to be determined separately
				 */
				g.setFont(Constants.font.FONT_GLASS.deriveFont((float)textLayer.getFontSize() * 1.5f));
				
				g.drawString(textLayer.getText(), textLayer.getLeft(), textLayer.getTop());
				g.setPaint(paint);
			}
		}
		if(showInstrumentMask){
			paintInstrumentMask(g);
		}

		if(showGrid){
			paintAlignementGrid(g);
		}

		Layer activeLayer = instrument.getActiveLayer();
		if(activeLayer != null && activeLayer instanceof ImageLayer){
			ImageLayer layer = (ImageLayer) activeLayer;

			Paint paint = g.getPaint();
			g.setPaint(Constants.paint.LAYER_PIVOT);
			g.fillOval(
					layer.getLeft() + layer.getPivotX() - 1,
					layer.getTop() + layer.getPivotY() - 1, 
					2, 2);
			g.setPaint(paint);
		}

		super.paint(g);
	}

	/**
	 * Paint a background grid
	 * @param g
	 */
	private void paintBackgroundGrid(Graphics2D g){
		Paint paint = g.getPaint();

		int size = Constants.integer.GRID_SIZE;
		for (int i = 0; i < instrument.getWidth() / size; i++) {
			for (int j = 0; j < instrument.getHeight() / size; j++) {
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

	/**
	 * Paint an alignement grid
	 * @param g
	 */
	private void paintAlignementGrid(Graphics2D g){
		Paint paint = g.getPaint();

		g.setPaint(Constants.paint.GRID_ALIGNMENT);
		int size = Constants.integer.GRID_SIZE * 2;
		for (int i = 1; i < instrument.getWidth() / size; i++) {
			for (int j = 1; j < instrument.getHeight() / size; j++) {
				g.fillOval(size * i, size * j, 1, 1);
			}
		}

		g.setPaint(paint);
	}

	private void paintInstrumentMask(Graphics2D g){
		Paint paint = g.getPaint();

		if(instrumentMask == null){
			String dir = new File("").getAbsolutePath() + "/assets/images";
			try {
				instrumentMask = ImageIO.read(new File(new File(dir), "shadow_general.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		g.drawImage(instrumentMask, 0, 0, 
				instrument.getWidth(), instrument.getHeight(), null);
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

			if(LAYER_CLICKABLE){
				Layer newActive = getClickedLayer(x, y);
				if (newActive != null) {
					pressedLayer = newActive;

					// deactivate layer when clicked again
					if(activeLayer != null && newActive.getId().equals(activeLayer.getId())){
						instrument.setLayersInactive();
						System.out.println("Layer "+activeLayer.getId()+" deactivated.");
						activeLayer = null;
					} else {
						activeLayer = newActive;
						instrument.setLayerActive(activeLayer);
						System.out.println("Layer "+activeLayer.getId()+" is active.");
						GraphicPanel.this.setFocused(true, null);
					}
				} else {
					instrument.setLayersInactive();
				}

				repaint();
				lastMouseButton = button;
				currentX = x;
				currentY = y;
			}
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

		for (Layer layer : instrument.getLayers()) {
			if(layer instanceof ImageLayer){
				if( (x > layer.getLeft() && x < layer.getLeft() + ((ImageLayer)layer).getWidth()) && 
						(y > layer.getTop() && y < layer.getTop() + ((ImageLayer)layer).getHeight())){
					result = layer;
				}
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
			final TextInput layerLeft = (TextInput) serializer.getNamespace().get("layer_left");
			final TextInput layerTop = (TextInput) serializer.getNamespace().get("layer_top");
			final ButtonGroup typeGroup = (ButtonGroup) serializer.getNamespace().get("layer_type");

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
					String type = (String) typeGroup.getSelection().getButtonData();

					int left = 0;
					int top = 0;
					try {
						left = Integer.parseInt(layerLeft.getText());
						top = Integer.parseInt(layerTop.getText());
					} catch(NumberFormatException e){
						// nix
					}

					if(type.equals("Text")){
						createTextLayer(layerId.getText(), 25, left, top);
					}
					else if(type.equals("Bild")){
						createImageLayer(layerId.getText(), layerImagePath.getText(), left, top);
						dialog.close();
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

	private void moveForward(Layer layer){
		instrument.moveLayerForward(layer);
		repaint();
	}

	private void moveBackward(Layer layer){
		instrument.moveLayerBackwards(layer);
		repaint();
	}

	private void createImageLayer(String id, String imagePath, int left, int top){
		int width = 0;
		int height = 0;

		File file = new File(imagePath);
		if(file.exists()){
			BufferedImage image;
			try {
				image = ImageIO.read(file);
				width = image.getWidth();
				height = image.getHeight();
			} catch (IOException | NullPointerException e) {
				e.printStackTrace();
			}
		}

		ImageLayer layer = new ImageLayer();
		layer
		.setPivotX(width / 2)
		.setPivotY(height / 2)
		.setLeft(left)
		.setTop(top)
		.setId(id);

		if(file.exists()){
			layer.setImageDay(file);
		}
		instrument.addLayer(layer);
		System.out.println(LuaScriptParser.instrumentToLua(instrument));

		repaint();
	}

	public boolean isShowGrid() {
		return showGrid;
	}

	/**
	 * Set alignment grid visible
	 * @param show
	 */
	public void setShowGrid(boolean show) {
		this.showGrid = show;
		repaint();
	}

	public boolean isShowInstrumentMask() {
		return showInstrumentMask;
	}

	/**
	 * Set instrument mask visible
	 * @param show
	 */
	public void setShowInstrumentMask(boolean show) {
		this.showInstrumentMask = show;
		repaint();
	}

	private void createTextLayer(String id, int fontSize, int left, int top){
		TextLayer layer = new TextLayer();
		layer.setId(id);
		layer.setFontSize(fontSize);
		layer.setLeft(left);
		layer.setTop(top);

		instrument.addLayer(layer);
		System.out.println(LuaScriptParser.instrumentToLua(instrument));

		repaint();
	}

	private void deleteLayer(Layer layer){
		instrument.deleteLayer(layer);
	}

	@Override
	public void update(Observable o, Object arg) {
		System.out.println("Graphic updated");
		instrument = editor.getInstrument();
		repaint();
	}
}
