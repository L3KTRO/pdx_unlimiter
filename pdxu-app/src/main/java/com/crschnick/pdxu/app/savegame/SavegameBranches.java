package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.installation.Game;

public class SavegameBranches {

    public static boolean supportsBranching(SavegameEntry<?,?> e) {
        var ctx = SavegameContext.getContext(e);

        // VIC2 does not support rewriting yet for non-ironman
        if (ctx.getGame() == Game.VIC2) {
            return ctx.getInfo().isIronman();
        }

        // Stellaris branching is pointless as campaigns can't be recognized across game sessions
        if (ctx.getGame() == Game.STELLARIS) {
            return false;
        }

        return true;
    }
}