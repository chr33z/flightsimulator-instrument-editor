package de.easemotion.fie.view;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.PrintGraphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;

import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.ComponentMouseListener;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.Panel;
import org.apache.pivot.wtk.Mouse.Button;

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

	private Layer activeLayer = null;
	private Button lastMouseButton;

	private int currentX;
	private int currentY;

	public GraphicPanel(GraphicSurface surface){
		this.surface = surface;

		this.getComponentMouseButtonListeners().add(mouseMoveButtonListener);
		this.getComponentMouseListeners().add(mouseMovementListener);
		this.setMenuHandler(menuHandler);
	}

	@Override
	public void paint(Graphics2D g) {
		paintGrid(g);

		for (Layer layer : surface.getLayers()) {
			BufferedImage image;
			try {
				File file = new File(System.getProperty("user.dir") + "/" +layer.getImage());
				System.out.println();
				image = ImageIO.read(file);
				g.drawImage(image, layer.getLeft(), layer.getTop(), layer.getWidth(), layer.getHeight(), null);
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
			activeLayer = null;
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
			
			activeLayer = getClickedLayer(x, y);
			if (activeLayer != null) {
				System.out.println("clicked layer:"+activeLayer.getId());
			}

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
			// TODO Auto-generated method stub

		}

		@Override
		public boolean mouseMove(Component component, int x, int y) {
			if(activeLayer != null && lastMouseButton == Button.LEFT){
				activeLayer.setLeft(activeLayer.getLeft() + (x - currentX));
				activeLayer.setTop(activeLayer.getTop() + (y - currentY));
				repaint();
			}

			currentX = x;
			currentY = y;
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
					System.out.println("item clicked");

					Layer layer1 = new Layer()
					.setWidth(120)
					.setHeight(120)
					.setLeft(50)
					.setTop(25)
					.setId("layer"+System.currentTimeMillis())
					.setImage("assets/images/Clock_Zifferblatt.png");

					surface.addLayer(layer1);

					repaint();
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
				surface.getLayers().add(index + 1, layer);
			} catch (IndexOutOfBoundsException e){
				surface.getLayers().add(layer);
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
				surface.getLayers().add(index - 1, layer);
			} catch (IndexOutOfBoundsException e){
				surface.getLayers().add(0, layer);
			}
			repaint();
		}
	}
	
	private void deleteLayer(Layer layer){
		if(layer != null){
			Iterator<Layer> iterator = surface.getLayers().iterator();
			while (iterator.hasNext()) {
				if(layer.getId().equals(iterator.next().getId())){
					iterator.remove();
					break;
				}
			}
			repaint();
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		
	}
}
