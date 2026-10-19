package id.riptidecooldown.mixin;

import id.riptidecooldown.NetheriteRule;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** ShulkerBoxSlot meng-override mayPlace, jadi perlu mixin sendiri. */
@Mixin(ShulkerBoxSlot.class)
public abstract class ShulkerBoxSlotMixin {

	@Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
	private void riptidecooldown$blockNetherite(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (NetheriteRule.isBlocked(stack)) {
			cir.setReturnValue(false);
		}
	}
}
