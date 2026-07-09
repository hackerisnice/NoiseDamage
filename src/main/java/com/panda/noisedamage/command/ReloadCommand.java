package com.panda.noisedamage.command;

import com.mojang.brigadier.CommandDispatcher;
import com.panda.noisedamage.config.NoiseConfig;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import static net.minecraft.server.command.CommandManager.literal;

public class ReloadCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("noisereload")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(ctx -> {
                    NoiseConfig.load();
                    ctx.getSource().sendFeedback(() -> Text.literal("Noise config reloaded successfully."), true);
                    return 1;
                })
        );
    }
}
