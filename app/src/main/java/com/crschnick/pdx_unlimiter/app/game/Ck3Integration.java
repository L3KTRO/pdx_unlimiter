package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.achievement.AchievementManager;
import com.crschnick.pdx_unlimiter.app.gui.Ck3GuiFactory;
import com.crschnick.pdx_unlimiter.app.gui.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.eu4.data.Ck3Tag;
import com.crschnick.pdx_unlimiter.eu4.savegame.Ck3SavegameInfo;

import java.io.IOException;
import java.nio.file.Path;

public class Ck3Integration extends GameIntegration<Ck3Tag, Ck3SavegameInfo> {
    @Override
    public String getName() {
        return "Crusader Kings III";
    }

    @Override
    public GameInstallation getInstallation() {
        return GameInstallation.CK3;
    }

    @Override
    public AchievementManager getAchievementManager() {
        return AchievementManager.CK3;
    }

    @Override
    public GameGuiFactory getGuiFactory() {
        return new Ck3GuiFactory();
    }

    @Override
    public SavegameCache getSavegameCache() {
        return SavegameCache.CK3_CACHE;
    }
}
