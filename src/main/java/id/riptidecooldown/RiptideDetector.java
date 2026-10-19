package id.riptidecooldown;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mendeteksi peluncuran riptide tanpa mixin (nama method internal berubah-ubah antar versi).
 *
 * Logika vanilla: trident riptide baru "meluncur" kalau tombol dilepas setelah di-charge minimal
 * 10 tick DAN player ada di air/hujan. Jadi kalau pada tick sebelumnya player sedang charge
 * (>= 10 tick), lalu sekarang sudah tidak memakai item dan ada di air/hujan => riptide terjadi.
 */
public final class RiptideDetector {
	private static final int CHARGE_THRESHOLD_TICKS = 10;
	private static final Set<UUID> CHARGING = ConcurrentHashMap.newKeySet();

	private RiptideDetector() {}

	public static void tick(ServerPlayer player) {
		UUID id = player.getUUID();
		boolean usingNow = player.isUsingItem();

		if (CHARGING.contains(id) && !usingNow && player.isInWaterOrRain()) {
			int cooldown = RiptideCooldownConfig.cooldownTicks();
			if (cooldown > 0) {
				player.getCooldowns().addCooldown(new ItemStack(Items.TRIDENT), cooldown);
				RiptideCooldownMod.markCooldown(player, cooldown);
			}
		}

		boolean chargingNow = false;
		if (usingNow) {
			ItemStack use = player.getUseItem();
			chargingNow = use.is(Items.TRIDENT)
				&& HeldItemEnforcer.hasRiptide(use)
				&& player.getTicksUsingItem() >= CHARGE_THRESHOLD_TICKS;
		}

		if (chargingNow) {
			CHARGING.add(id);
		} else {
			CHARGING.remove(id);
		}
	}
}
