package id.riptidecooldown;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public final class NetheriteRule {
	private NetheriteRule() {}

	/** Semua item yang id-nya mengandung "netherite" (ingot, scrap, tools, armor, block, template). */
	public static boolean isBlocked(ItemStack stack) {
		if (stack.isEmpty() || !RiptideCooldownConfig.get().blockNetheriteInContainers) {
			return false;
		}
		return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().contains("netherite");
	}
}
