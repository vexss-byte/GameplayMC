package id.riptidecooldown;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/** Memaksa trident riptide yang sedang cooldown keluar dari tangan player. */
public final class HeldItemEnforcer {
	private HeldItemEnforcer() {}

	public static boolean hasRiptide(ItemStack stack) {
		for (Holder<Enchantment> h : stack.getEnchantments().keySet()) {
			if (h.is(Enchantments.RIPTIDE)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isLocked(ServerPlayer player, ItemStack stack) {
		return stack.is(Items.TRIDENT) && hasRiptide(stack) && player.getCooldowns().isOnCooldown(stack);
	}

	public static void enforce(ServerPlayer player) {
		Inventory inv = player.getInventory();

		// Tangan kiri (offhand): pindahkan ke inventory, kalau penuh dijatuhkan.
		ItemStack off = player.getOffhandItem();
		if (isLocked(player, off)) {
			ItemStack moved = off.copy();
			player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
			if (!inv.add(moved)) {
				player.drop(moved, false);
			}
			notice(player);
		}

		// Tangan utama: geser pilihan hotbar ke slot terdekat yang bukan trident terkunci.
		if (isLocked(player, inv.getSelectedItem())) {
			int current = inv.getSelectedSlot();
			for (int k = 1; k < 9; k++) {
				int slot = (current + k) % 9;
				if (!isLocked(player, inv.getItem(slot))) {
					inv.setSelectedSlot(slot);
					player.connection.send(new ClientboundSetHeldSlotPacket(slot));
					notice(player);
					break;
				}
			}
		}
	}

	private static void notice(ServerPlayer player) {
		long left = RiptideCooldownMod.remainingSeconds(player);
		String text = left > 0
			? "Trident riptide sedang cooldown: " + left + " detik lagi"
			: "Trident riptide sedang cooldown";
		player.sendSystemMessage(Component.literal(text), true); // action bar
	}
}
