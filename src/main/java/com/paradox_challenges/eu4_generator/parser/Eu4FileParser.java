package com.paradox_challenges.eu4_generator.parser;

import com.paradox_challenges.eu4_generator.format.Namespace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Eu4FileParser {

    private IronmanParser ironmanParser;

    private static ZipEntry getEntryByName(String name, ZipFile zipFile) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            if (entry.getName().equals(name)) {
                return entry;
            }
        }
        return null;
    }

    Eu4FileData parse(Path file) throws IOException {
        boolean isZipped = new ZipInputStream(Files.newInputStream(file)).getNextEntry() != null;
        if (isZipped) {
            ZipFile zipFile = new ZipFile(file.toFile());
            ZipEntry gamestate = getEntryByName("gamestate", zipFile);
            ZipEntry meta = getEntryByName("meta", zipFile);
            ZipEntry ai = getEntryByName("ai", zipFile);

            Optional<Node> gamestateNode = ironmanParser.parse(zipFile.getInputStream(gamestate));
            if (gamestateNode.isPresent()) {

            }
        }
    }
}
