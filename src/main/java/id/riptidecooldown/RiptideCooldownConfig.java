package id.riptidecooldown;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Config disimpan di config/riptide_cooldown.json */
public final class RiptideCooldownConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("riptide_cooldown.json");

	/** Lama cooldown dalam detik (0 = cooldown mati). */
	public double cooldownSeconds = 15.0;
	/** true = trident riptide yang sedang cooldown otomatis tidak bisa dipegang. */
	public boolean blockHolding = true;
	/** "always" = semua player tidak bisa chat kapan pun, "cooldown" = hanya saat trident cooldown, "off" = chat normal. */
	public String chatBlockMode = "always";
	/** Pesan peringatan yang muncul di chat player yang mencoba chat. */
	public String chatWarningMessage = "Chat Tidak Di Perbolehkan";
	/** Warna pesan peringatan (hex), default biru galaxy. */
	public String chatWarningColor = "#4F6BFF";
	/** true = netherite tidak bisa masuk chest / ender chest / shulker box (manual, hopper, dropper). */
	public boolean blockNetheriteInContainers = true;

	private static RiptideCooldownConfig instance = new RiptideCooldownConfig();

	public static RiptideCooldownConfig get() {
		return instance;
	}

	public static int warningColorRgb() {
		try {
			return Integer.parseInt(instance.chatWarningColor.replace("#", "").trim(), 16) & 0xFFFFFF;
		} catch (Exception e) {
			return 0x4F6BFF;
		}
	}

	public static int cooldownTicks() {
		return (int) Math.round(Math.max(0.0, instance.cooldownSeconds) * 20.0);
	}

	public static void load() {
		try {
			if (Files.exists(FILE)) {
				RiptideCooldownConfig loaded = GSON.fromJson(Files.readString(FILE), RiptideCooldownConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			}
			save(); // tulis ulang supaya field baru muncul
		} catch (Exception e) {
			RiptideCooldownMod.LOGGER.error("Gagal membaca config, pakai default", e);
		}
	}

	public static void save() {
		try {
			Files.createDirectories(FILE.getParent());
			Files.writeString(FILE, GSON.toJson(instance));
		} catch (IOException e) {
			RiptideCooldownMod.LOGGER.error("Gagal menyimpan config", e);
		}
	}
}
