package id.riptidecooldown;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RiptideCooldownMod implements ModInitializer {
	public static final String MOD_ID = "riptidecooldown";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** Waktu (ms) kapan cooldown tiap player berakhir; hanya untuk menampilkan sisa waktu. */
	private static final Map<UUID, Long> COOLDOWN_END = new ConcurrentHashMap<>();

	public static void markCooldown(ServerPlayer player, int ticks) {
		COOLDOWN_END.put(player.getUUID(), System.currentTimeMillis() + ticks * 50L);
	}

	public static long remainingSeconds(ServerPlayer player) {
		Long end = COOLDOWN_END.get(player.getUUID());
		if (end == null) {
			return 0;
		}
		long ms = end - System.currentTimeMillis();
		if (ms <= 0) {
			COOLDOWN_END.remove(player.getUUID());
			return 0;
		}
		return (ms + 999) / 1000;
	}

	private static boolean tridentOnCooldown(ServerPlayer player) {
		return player.getCooldowns().isOnCooldown(new ItemStack(Items.TRIDENT));
	}

	@Override
	public void onInitialize() {
		RiptideCooldownConfig.load();

		// 1) Deteksi riptide + trident riptide yang cooldown tidak bisa dipegang
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			boolean blockHolding = RiptideCooldownConfig.get().blockHolding;
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				RiptideDetector.tick(player);
				if (blockHolding) {
					HeldItemEnforcer.enforce(player);
				}
			}
		});

		// 2) Blokir chat (default: semua player, kapan pun)
		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
			RiptideCooldownConfig cfg = RiptideCooldownConfig.get();
			String mode = cfg.chatBlockMode;
			boolean block = "always".equalsIgnoreCase(mode)
				|| ("cooldown".equalsIgnoreCase(mode) && tridentOnCooldown(sender));
			if (block) {
				sender.sendSystemMessage(Component.literal(cfg.chatWarningMessage)
					.withStyle(Style.EMPTY.withColor(RiptideCooldownConfig.warningColorRgb())));
			}
			return !block;
		});

		// 3) Command: /riptidecooldown [detik] dan /riptidecooldown chat <always|cooldown|off>
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			dispatcher.register(Commands.literal("riptidecooldown")
				// Hanya OP. Kalau baris ini error saat compile, ganti dengan versi permission yang cocok di 26.3.
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.executes(ctx -> {
					RiptideCooldownConfig c = RiptideCooldownConfig.get();
					ctx.getSource().sendSuccess(() -> Component.literal(
						"Cooldown: " + c.cooldownSeconds + " detik | Chat: " + c.chatBlockMode), false);
					return 1;
				})
				.then(Commands.argument("detik", DoubleArgumentType.doubleArg(0, 3600))
					.executes(ctx -> {
						double s = DoubleArgumentType.getDouble(ctx, "detik");
						RiptideCooldownConfig.get().cooldownSeconds = s;
						RiptideCooldownConfig.save();
						ctx.getSource().sendSuccess(() -> Component.literal("Cooldown riptide trident diset ke " + s + " detik"), true);
						return 1;
					}))
				.then(Commands.literal("chat")
					.then(Commands.literal("cooldown").executes(ctx -> setChat(ctx, "cooldown")))
					.then(Commands.literal("always").executes(ctx -> setChat(ctx, "always")))
					.then(Commands.literal("off").executes(ctx -> setChat(ctx, "off"))))));

		LOGGER.info("Riptide Cooldown aktif ({} detik)", RiptideCooldownConfig.get().cooldownSeconds);
	}

	private static int setChat(CommandContext<CommandSourceStack> ctx, String mode) {
		RiptideCooldownConfig.get().chatBlockMode = mode;
		RiptideCooldownConfig.save();
		ctx.getSource().sendSuccess(() -> Component.literal("Mode blokir chat: " + mode), true);
		return 1;
	}
}
