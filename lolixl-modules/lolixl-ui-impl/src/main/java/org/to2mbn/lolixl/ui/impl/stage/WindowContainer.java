package org.to2mbn.lolixl.ui.impl.stage;

import static org.to2mbn.lolixl.utils.MathUtils.*;
import java.math.BigDecimal;
import javax.sql.rowset.spi.TransactionalWriter;
import javafx.css.PseudoClass;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class WindowContainer extends Pane {

	public static final double SHADOW_WIDTH = 15.0;
	public static final double RESIZABLE_WIDTH = 10.0;
	public static final double MOUSE_TRANSPARENT_WIDTH = SHADOW_WIDTH - RESIZABLE_WIDTH;

	private Region content;
	private Stage stage;
	private Pane shadowPane;
	private Pane resizePane;
	private Region contentNode;

	private double moveDragX = Double.NaN;
	private double moveDragY = Double.NaN;
	private double moveInitDx = Double.NaN;
	private double moveInitDy = Double.NaN;

	private double resizeDragX = Double.NaN;
	private double resizeDragY = Double.NaN;

	public WindowContainer() {
		setStyle("-fx-background-color: null;");

		shadowPane = new Pane();
		shadowPane.setStyle("-fx-background-color: null;");
		getChildren().add(shadowPane);

		resizePane = new Pane();
		resizePane.setStyle("-fx-background-color: null;");
		getChildren().add(resizePane);
	}

	public void initContent(Region contentNode) {
		this.contentNode = contentNode;
		Rectangle rect = new Rectangle();
		rect.widthProperty().bind(widthProperty().subtract(2 * SHADOW_WIDTH));
		rect.heightProperty().bind(heightProperty().subtract(2 * SHADOW_WIDTH));
		contentNode.setClip(rect);

		this.content = new Pane(contentNode);
		content.setStyle("-fx-background-color: transparent;");
		content.getStyleClass().add("xl-shadow-stage");
		getChildren().add(content);
	}

	public void initStage(Stage stage) {
		this.stage = stage;
		stage.focusedProperty().addListener((dummy, oldVal, newVal) -> content.pseudoClassStateChanged(PseudoClass.getPseudoClass("window-focused"), newVal));
		stage.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
		stage.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
		stage.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
		stage.addEventHandler(MouseEvent.MOUSE_MOVED, this::onMouseEntered);
		stage.addEventHandler(MouseEvent.MOUSE_EXITED, this::onMouseExited);
	}

	@Override
	protected void layoutChildren() {
		shadowPane.relocate(0, 0);
		shadowPane.resize(getWidth(), getHeight());
		resizePane.relocate(MOUSE_TRANSPARENT_WIDTH, MOUSE_TRANSPARENT_WIDTH);
		resizePane.resize(getWidth() - 2 * MOUSE_TRANSPARENT_WIDTH, getHeight() - 2 * MOUSE_TRANSPARENT_WIDTH);
		content.relocate(SHADOW_WIDTH, SHADOW_WIDTH);
		content.resize(getWidth() - 2 * SHADOW_WIDTH, getHeight() - 2 * SHADOW_WIDTH);
		contentNode.relocate(0, 0);
		contentNode.resize(getWidth() - 2 * SHADOW_WIDTH, getHeight() - 2 * SHADOW_WIDTH);
	}

	public void setDraggable(Node node) {
		node.setOnMousePressed(event -> {
			if (event.isPrimaryButtonDown()) {
				moveDragX = event.getScreenX();
				moveDragY = event.getScreenY();
				moveInitDx = moveDragX - stage.getX();
				moveInitDy = moveDragY - stage.getY();
				event.consume();
			} else {
				moveDragX = Double.NaN;
				moveDragY = Double.NaN;
				moveInitDx = Double.NaN;
				moveInitDy = Double.NaN;
			}
		});
		node.setOnMouseDragged(event -> {
			if (!event.isPrimaryButtonDown() || Double.isNaN(moveDragX)) {
				return;
			}
			if (event.isStillSincePress()) {
				return;
			}
			moveDragX = event.getScreenX();
			moveDragY = event.getScreenY();
			stage.setX(moveDragX - moveInitDx);
			stage.setY(moveDragY - moveInitDy);

			event.consume();
		});
		node.setOnMouseReleased(event -> {
			moveDragX = Double.NaN;
			moveDragY = Double.NaN;
			moveInitDx = Double.NaN;
			moveInitDy = Double.NaN;
		});
	}

	private void onMousePressed(MouseEvent event) {
		if (event.isPrimaryButtonDown()) {
			if (isInResizableArea(event)) {
				resizeDragX = event.getScreenX();
				resizeDragY = event.getScreenY();
				event.consume();
			}
		} else {
			resizeDragX = Double.NaN;
			resizeDragY = Double.NaN;
		}
	}

	private void onMouseDragged(MouseEvent event) {
		if (!event.isPrimaryButtonDown() || Double.isNaN(resizeDragX)) {
			return;
		}
		if (event.isStillSincePress()) {
			return;
		}
		double newX = event.getScreenX();
		double newY = event.getScreenY();
		double windowX = stage.getX() + SHADOW_WIDTH;
		double windowY = stage.getY() + SHADOW_WIDTH;
		double minWidth = stage.getMinWidth() - 2 * SHADOW_WIDTH;
		double maxWidth = stage.getMaxWidth() - 2 * SHADOW_WIDTH;
		double minHeight = stage.getMinHeight() - 2 * SHADOW_WIDTH;
		double maxHeight = stage.getMaxHeight() - 2 * SHADOW_WIDTH;
		double oldWidth = stage.getWidth() - 2 * SHADOW_WIDTH;
		double oldHeight = stage.getHeight() - 2 * SHADOW_WIDTH;

		double toHeight = Double.NaN;
		double toWidth = Double.NaN;
		double toX = Double.NaN;
		double toY = Double.NaN;

		Cursor cursor = getCursor();
		if (cursor == Cursor.N_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.NE_RESIZE) {
			toHeight = clamp(minHeight, windowY + oldHeight - newY, maxHeight);
			toY = windowY + oldHeight - toHeight;
		}
		if (cursor == Cursor.S_RESIZE || cursor == Cursor.SW_RESIZE || cursor == Cursor.SE_RESIZE) {
			toHeight = clamp(minHeight, newY - windowY, maxHeight);
		}
		if (cursor == Cursor.W_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.SW_RESIZE) {
			toWidth = clamp(minWidth, windowX + oldWidth - newX, maxWidth);
			toX = windowX + oldWidth - toWidth;
		}
		if (cursor == Cursor.E_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.SE_RESIZE) {
			toWidth = clamp(minWidth, newX - windowX, maxWidth);
		}

		if (!Double.isNaN(toX)) {
			stage.setX(toX - SHADOW_WIDTH);
		}
		if (!Double.isNaN(toY)) {
			stage.setY(toY - SHADOW_WIDTH);
		}
		if (!Double.isNaN(toWidth)) {
			stage.setWidth(toWidth + 2 * SHADOW_WIDTH);
		}
		if (!Double.isNaN(toHeight)) {
			stage.setHeight(toHeight + 2 * SHADOW_WIDTH);
		}

		resizeDragX = newX;
		resizeDragY = newY;

		event.consume();
	}

	private void onMouseReleased(MouseEvent event) {
		resizeDragX = Double.NaN;
		resizeDragY = Double.NaN;
	}

	private void onMouseEntered(MouseEvent event) {
		double x = event.getSceneX() - MOUSE_TRANSPARENT_WIDTH;
		double y = event.getSceneY() - MOUSE_TRANSPARENT_WIDTH;
		double w = content.getWidth();
		double h = content.getHeight();
		boolean left = x >= 0 && x < RESIZABLE_WIDTH;
		boolean right = x >= w + RESIZABLE_WIDTH && x < w + 2 * RESIZABLE_WIDTH;
		boolean top = y >= 0 && y < RESIZABLE_WIDTH;
		boolean bottom = y >= h + RESIZABLE_WIDTH && y < h + 2 * RESIZABLE_WIDTH;
		Cursor cursor = null;
		if (top && left) {
			cursor = Cursor.NW_RESIZE;
		} else if (top && right) {
			cursor = Cursor.NE_RESIZE;
		} else if (bottom && left) {
			cursor = Cursor.SW_RESIZE;
		} else if (bottom && right) {
			cursor = Cursor.SE_RESIZE;
		} else if (top) {
			cursor = Cursor.N_RESIZE;
		} else if (bottom) {
			cursor = Cursor.S_RESIZE;
		} else if (left) {
			cursor = Cursor.W_RESIZE;
		} else if (right) {
			cursor = Cursor.E_RESIZE;
		}
		if (cursor != null) {
			event.consume();
		}
		setCursor(cursor);
	}

	private void onMouseExited(MouseEvent event) {
		setCursor(null);
	}

	private boolean isInResizableArea(MouseEvent event) {
		double x = event.getSceneX() - MOUSE_TRANSPARENT_WIDTH;
		double y = event.getSceneY() - MOUSE_TRANSPARENT_WIDTH;
		double w = content.getWidth();
		double h = content.getHeight();
		return (x >= 0 && x < RESIZABLE_WIDTH) ||
				(x >= w + RESIZABLE_WIDTH && x < w + 2 * RESIZABLE_WIDTH) ||
				(y >= 0 && y < RESIZABLE_WIDTH) ||
				(y >= h + RESIZABLE_WIDTH && y < h + 2 * RESIZABLE_WIDTH);
	}

}