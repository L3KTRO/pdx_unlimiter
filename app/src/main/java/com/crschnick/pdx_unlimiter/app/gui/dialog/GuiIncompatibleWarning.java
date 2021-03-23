package com.crschnick.pdx_unlimiter.app.gui.dialog;

import com.crschnick.pdx_unlimiter.app.core.PdxuI18n;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameMod;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameActions;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class GuiIncompatibleWarning {

    public static boolean showIncompatibleWarning(GameInstallation installation, SavegameEntry<?, ?> entry) {
        StringBuilder builder = new StringBuilder("Selected savegame is incompatible. Launching it anyway, can cause problems.\n");
        if (!SavegameActions.isVersionCompatible(entry.getInfo())) {
            builder.append("Incompatible versions:\n")
                    .append("- Game version: ")
                    .append(installation.getVersion().toString()).append("\n")
                    .append("- Savegame version: ")
                    .append(entry.getInfo().getVersion().toString());
        }

        boolean missingMods = entry.getInfo().getMods().stream()
                .map(m -> installation.getModForName(m))
                .anyMatch(Optional::isEmpty);
        if (missingMods) {
            builder.append("\nThe following Mods are missing:\n").append(entry.getInfo().getMods().stream()
                    .map(s -> {
                        var m = installation.getModForName(s);
                        return (m.isPresent() ? null : "- " + s);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n")));
        }

        boolean missingDlc = entry.getInfo().getDlcs().stream()
                .map(m -> installation.getDlcForName(m))
                .anyMatch(Optional::isEmpty);
        if (missingDlc) {
            builder.append("\n\nThe following DLCs are missing:\n").append(entry.getInfo().getDlcs().stream()
                    .map(s -> {
                        var m = installation.getDlcForName(s);
                        return (m.isPresent() ? null : "- " + s);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n")));
        }

        var launch = new ButtonType("Launch anyway");
        return GuiDialogHelper.showBlockingAlert(alert -> {
            alert.setAlertType(Alert.AlertType.WARNING);
            alert.getButtonTypes().clear();
            alert.getButtonTypes().add(ButtonType.CLOSE);
            alert.getButtonTypes().add(launch);
            alert.setTitle("Incompatible savegame");
            alert.setHeaderText(builder.toString());
        }).orElse(ButtonType.CLOSE).equals(launch);
    }

    public static Optional<Boolean> showStellarisModWarning(List<GameMod> enabledMods) {
        var launchButton = new ButtonType(PdxuI18n.get("LAUNCH"));
        var changeModsButton = new ButtonType(PdxuI18n.get("CHANGE_MODS"));

        var r = GuiDialogHelper.showBlockingAlert(alert -> {
            alert.setAlertType(Alert.AlertType.WARNING);
            alert.getButtonTypes().clear();
            alert.getButtonTypes().add(ButtonType.CLOSE);
            alert.getButtonTypes().add(launchButton);
            alert.getButtonTypes().add(changeModsButton);
            alert.setTitle(PdxuI18n.get("STELLARIS_INFO_TITLE"));

            String builder = PdxuI18n.get("STELLARIS_INFO") + enabledMods.stream()
                    .map(m -> "- " + m.getName())
                    .collect(Collectors.joining("\n"));
            alert.setHeaderText(builder);
        });

        if (r.isPresent()) {
            if (r.get().equals(launchButton)) return Optional.of(true);
            if (r.get().equals(changeModsButton)) return Optional.of(false);
        }
        return Optional.empty();
    }
}
