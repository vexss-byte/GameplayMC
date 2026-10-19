package id.riptidecooldown;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

public final class StorageRule {
	private StorageRule() {}

	/** Chest, trapped chest, double chest, ender chest, dan shulker box. */
	public static boolean isRestricted(Container c) {
		return c instanceof ChestBlockEntity
			|| c instanceof CompoundContainer
			|| c instanceof PlayerEnderChestContainer
			|| c instanceof ShulkerBoxBlockEntity;
	}
}
