package com.crschnick.pdx_unlimiter.app.editor.target;

import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameType;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public abstract class EditTarget {

    protected final Path file;

    public EditTarget(Path file) {
        this.file = file;
    }

    public static Optional<EditTarget> create(Path file) {
        SavegameType type = SavegameType.getTypeForFile(file);
        if (type == null) {
            return Optional.empty();
        }

        return Optional.of(new SavegameEditTarget(file, type));
    }

    public abstract Map<String, ArrayNode> parse() throws Exception;

    public abstract void write(Map<String, ArrayNode> nodeMap) throws Exception;

    public String getName() {
        return file.getFileName().toString();
    }

    public Path getFile() {
        return file;
    }
}
