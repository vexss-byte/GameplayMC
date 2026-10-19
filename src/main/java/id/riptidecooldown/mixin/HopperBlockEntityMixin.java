package id.riptidecooldown.mixin;

import id.riptidecooldown.NetheriteRule;
import id.riptidecooldown.StorageRule;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hopper dan dropper memasukkan item ke container lewat HopperBlockEntity.addItem(),
 * yang selalu memanggil canPlaceItemInContainer(). Satu hook ini menutup keduanya.
 */
@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {

	@Inject(method = "canPlaceItemInContainer", at = @At("HEAD"), cancellable = true)
	private static void riptidecooldown$blockNetherite(Container container, ItemStack stack, int slot, Direction direction,
			CallbackInfoReturnable<Boolean> cir) {
		if (StorageRule.isRestricted(container) && NetheriteRule.isBlocked(stack)) {
			cir.setReturnValue(false);
		}
	}
}
