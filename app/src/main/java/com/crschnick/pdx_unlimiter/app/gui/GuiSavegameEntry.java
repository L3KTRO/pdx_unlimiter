package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.gui.dialog.DialogHelper;
import com.crschnick.pdx_unlimiter.app.installation.GameIntegration;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameActions;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.app.util.ConverterHelper;
import com.crschnick.pdx_unlimiter.app.util.RakalyWebHelper;
import com.crschnick.pdx_unlimiter.app.util.SavegameInfoHelper;
import com.crschnick.pdx_unlimiter.app.util.SkanderbegHelper;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4Tag;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXMasonryPane;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import org.apache.commons.io.FileUtils;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.List;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;

public class GuiSavegameEntry {

    private static <T, I extends SavegameInfo<T>> Region setupTopBar(SavegameEntry<T, I> e) {
        BorderPane topBar = new BorderPane();
        topBar.getStyleClass().add(CLASS_ENTRY_BAR);
        SavegameInfoHelper.withInfoAsync(e, (info, gi) -> {
            topBar.setBackground(gi.getGuiFactory().createEntryInfoBackground(info));
        });

        {
            Label l = new Label(e.getDate().toDisplayString());
            l.getStyleClass().add(CLASS_DATE);

            var tagImage = SavegameInfoHelper.createWithIntegration(e,
                    gi -> gi.getGuiFactory().createImage(e));
            Pane tagPane = new Pane(tagImage.getValue());
            HBox tagBar = new HBox(tagPane, l);
            tagBar.getStyleClass().add(CLASS_TAG_BAR);
            tagImage.addListener((change, o, n) -> {
                Platform.runLater(() -> {
                    tagPane.getChildren().set(0, n);
                });
            });
            tagBar.setAlignment(Pos.CENTER);
            topBar.setLeft(tagBar);
        }
        {
            JFXTextField name = new JFXTextField();
            name.getStyleClass().add(CLASS_TEXT_FIELD);
            name.setAlignment(Pos.CENTER);
            name.textProperty().bindBidirectional(e.nameProperty());
            topBar.setCenter(name);
        }
        {
            HBox buttonBar = new HBox();
            buttonBar.setAlignment(Pos.CENTER);
            createButtonBar(e, buttonBar);
            buttonBar.getStyleClass().add(CLASS_BUTTON_BAR);
            topBar.setRight(buttonBar);
        }
        return topBar;
    }

    private static <T, I extends SavegameInfo<T>> void setupDragAndDrop(Region r, SavegameEntry<T, I> e) {
        r.setOnDragDetected(me -> {
            var out = SavegameActions.exportToTemp(e);
            out.ifPresent(p -> {
                Dragboard db = r.startDragAndDrop(TransferMode.COPY);
                var cc = new ClipboardContent();
                cc.putFiles(List.of(p.toFile()));
                db.setContent(cc);
            });
            me.consume();
        });
    }

    public static <T, I extends SavegameInfo<T>> Node createSavegameEntryNode(SavegameEntry<T, I> e) {
        VBox entryNode = new VBox();
        entryNode.setAlignment(Pos.CENTER);
        entryNode.setFillWidth(true);
        entryNode.getStyleClass().add(CLASS_ENTRY);
        entryNode.setOnMouseClicked(event -> SavegameManagerState.<T, I>get().selectEntry(e));

        setupDragAndDrop(entryNode, e);
        entryNode.getChildren().add(setupTopBar(e));
        Node content = createSavegameInfoNode(e);
        entryNode.getChildren().add(content);

        return entryNode;
    }

    private static <T, I extends SavegameInfo<T>> void createButtonBar(SavegameEntry<T, I> e, HBox buttonBar) {
        {
            Button copy = new JFXButton();
            copy.setGraphic(new FontIcon());
            copy.setOnMouseClicked((m) -> {
                SavegameActions.copySavegame(e);
            });
            copy.getStyleClass().add(CLASS_COPY);
            GuiTooltips.install(copy, "Make a copy of the savegame");
            buttonBar.getChildren().add(copy);
        }

        {
            Button melt = new JFXButton();
            melt.setGraphic(new FontIcon());
            melt.setOnMouseClicked((m) -> {
                SavegameActions.meltSavegame(e);
            });
            melt.getStyleClass().add(CLASS_MELT);
            GuiTooltips.install(melt, "Melt savegame (Convert to Non-Ironman)");
            SavegameInfoHelper.withInfoAsync(e, (info, gi) -> {
                if (info.isBinary()) {
                    buttonBar.getChildren().add(0, melt);
                }
            });
        }

        if (SavegameStorage.EU4 != null && SavegameStorage.EU4.contains(e)) {
            SavegameEntry<Eu4Tag, Eu4SavegameInfo> eu4Entry = (SavegameEntry<Eu4Tag, Eu4SavegameInfo>) e;
            Button upload = new JFXButton();
            upload.setGraphic(new FontIcon());
            upload.setOnMouseClicked((m) -> {
                RakalyWebHelper.uploadSavegame(SavegameStorage.EU4, eu4Entry);
            });
            upload.getStyleClass().add(CLASS_ANALYZE);
            GuiTooltips.install(upload, "Upload and analyze with Rakaly.com");
            buttonBar.getChildren().add(upload);


            Button uploadSkanderbeg = new JFXButton();
            uploadSkanderbeg.setGraphic(new FontIcon());
            uploadSkanderbeg.setOnMouseClicked((m) -> {
                SkanderbegHelper.uploadSavegame(SavegameStorage.EU4, eu4Entry);
            });
            uploadSkanderbeg.getStyleClass().add(CLASS_MAP);
            GuiTooltips.install(uploadSkanderbeg, "Upload to Skanderbeg.pm");
            buttonBar.getChildren().add(uploadSkanderbeg);
        }

        if (SavegameStorage.CK3 != null && SavegameStorage.CK3.contains(e)) {
            SavegameEntry<Ck3Tag, Ck3SavegameInfo> ck3Entry = (SavegameEntry<Ck3Tag, Ck3SavegameInfo>) e;
            Button convert = new JFXButton();
            convert.setGraphic(new FontIcon());
            convert.setOnMouseClicked((m) -> {
                ConverterHelper.convertCk3ToEu4(ck3Entry);
            });
            convert.getStyleClass().add(CLASS_CONVERT);
            GuiTooltips.install(convert, "Convert to EU4 savegame");
            buttonBar.getChildren().add(convert);
        }

        if (PdxuInstallation.getInstance().isDeveloperMode()) {
            Button open = new JFXButton();
            open.setGraphic(new FontIcon());
            open.getStyleClass().add("open-button");
            GuiTooltips.install(open, "Open stored savegame location");
            buttonBar.getChildren().add(open);
            open.setOnMouseClicked((m) -> {
                SavegameActions.openCampaignEntry(e);
            });
        }

        {
            Button edit = new JFXButton();
            edit.setGraphic(new FontIcon());
            edit.setOnMouseClicked((m) -> {
                SavegameActions.editSavegame(e);
            });
            edit.getStyleClass().add(CLASS_EDIT);
            GuiTooltips.install(edit, "Edit savegame");
            SavegameInfoHelper.withInfoAsync(e, (info, gi) -> {
                if (!info.isBinary()) {
                    buttonBar.getChildren().add(0, edit);
                }
            });
        }

        {
            Button del = new JFXButton();
            del.setGraphic(new FontIcon());
            del.setOnMouseClicked((m) -> {
                if (DialogHelper.showSavegameDeleteDialog()) {
                    SavegameManagerState.<T, I>get().current().getSavegameStorage().deleteAsync(e);
                }
            });
            del.getStyleClass().add(CLASS_DELETE);
            GuiTooltips.install(del, "Delete savegame");
            buttonBar.getChildren().add(del);
        }
    }

    private static <T, I extends SavegameInfo<T>> Node createSavegameInfoNode(SavegameEntry<T, I> entry) {
        StackPane stack = new StackPane();
        JFXMasonryPane container = new JFXMasonryPane();
        container.getStyleClass().add(CLASS_CAMPAIGN_ENTRY_NODE_CONTAINER);
        container.setLayoutMode(JFXMasonryPane.LayoutMode.MASONRY);
        container.setHSpacing(10);
        container.setVSpacing(10);
        container.minHeightProperty().bind(Bindings.createDoubleBinding(
                () -> 3 * container.getCellHeight() + 2 * container.getVSpacing() + container.getPadding().getBottom() + container.getPadding().getTop(), container.paddingProperty()));
        container.setLimitRow(3);

        JFXSpinner loading = new JFXSpinner();
        loading.getStyleClass().add(CLASS_ENTRY_LOADING);
        stack.getChildren().add(container);
        if (entry.infoProperty().isNotNull().get()) {
            SavegameInfoHelper.withInfo(entry, (info, gi) -> {
                gi.getGuiFactory().fillNodeContainer(info, container);
            });
        } else {
            stack.getChildren().add(loading);
        }

        stack.sceneProperty().addListener((c, o, n) -> {
            if (n != null) {
                SavegameManagerState.<T,I>get().loadEntryAsync(entry);
            }
        });

        entry.infoProperty().addListener((c, o, n) -> {
            if (n != null) {
                SavegameInfoHelper.doWithIntegration(entry, gi -> {
                    Platform.runLater(() -> {
                        loading.setVisible(false);
                        gi.getGuiFactory().fillNodeContainer(n, container);
                    });
                });
            } else {
                Platform.runLater(() -> {
                    loading.setVisible(true);
                    container.getChildren().clear();
                });
            }
        });

        return stack;
    }
}
