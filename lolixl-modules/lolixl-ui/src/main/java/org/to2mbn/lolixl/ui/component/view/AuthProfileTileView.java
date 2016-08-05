package org.to2mbn.lolixl.ui.component.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.to2mbn.lolixl.utils.BundleUtils;
import java.io.IOException;

public class AuthProfileTileView extends BorderPane {
	private static final String FXML_LOCATION = "/ui/fxml/component/auth_profile_tile.fxml";

	@FXML
	public ImageView iconView;

	@FXML
	public BorderPane labelContainer;

	@FXML
	public Label userNameLabel;

	@FXML
	public Label profileNameLabel;

	public AuthProfileTileView() throws IOException {
		FXMLLoader loader = new FXMLLoader(BundleUtils.getResourceFromBundle(getClass(), FXML_LOCATION));
		loader.setRoot(this);
		loader.setController(this);
		loader.load();
	}
}
