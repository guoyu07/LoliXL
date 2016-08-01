package org.to2mbn.lolixl.ui.impl.component.view.downloads;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.to2mbn.lolixl.core.download.notify.DownloadTaskGroup;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.utils.BundleUtils;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class DownloadTaskGroupItemView extends BorderPane {
	private static final String FXML_LOCATION = "/ui/fxml/component/download_group_item.fxml";

	@FXML
	public ImageView iconView;

	@FXML
	public BorderPane infoContainer;

	@FXML
	public HBox tagContainer;

	@FXML
	public Label nameLabel;

	@FXML
	public Label statusLabel;

	private final Timer timer;
	private final DownloadTaskGroup taskGroup;
	private final TimerTask updateTask;

	public DownloadTaskGroupItemView(DownloadTaskGroup group) throws IOException {
		FXMLLoader loader = new FXMLLoader(BundleUtils.getResourceFromBundle(getClass(), FXML_LOCATION));
		loader.setRoot(this);
		loader.setController(this);
		loader.load();
		timer = new Timer(true);
		taskGroup = group;
		updateTask = new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(DownloadTaskGroupItemView.this::updateStatus);
			}
		};
		initComponent(group);
	}

	public void resumeUpdateCycle() {
		timer.cancel();
	}

	public void startUpdateCycle() {
		timer.schedule(updateTask, 0, 100);
	}

	private void initComponent(DownloadTaskGroup group) {
		iconView.imageProperty().bind(group.getDisplayableItem().getIcon());
		nameLabel.textProperty().bind(group.getDisplayableItem().getLocalizedName());
	}

	private void updateStatus() {
		StringProperty text = statusLabel.textProperty();
		if (taskGroup.isCancelled()) {
			text.bind(I18N.localize("org.to2mbn.lolixl.ui.impl.component.view.downloads.item.status.cancelled"));
		} else if (taskGroup.getException() != null) { // 切不可改变判断顺序
			text.bind(I18N.localize("org.to2mbn.lolixl.ui.impl.component.view.downloads.item.status.error"));
		} else if (taskGroup.isDone()) {
			text.bind(I18N.localize("org.to2mbn.lolixl.ui.impl.component.view.downloads.item.status.done"));
		} else {
			text.set((taskGroup.getFinishedCount() / taskGroup.getTotalCount()) + "%");
		}
	}
}
