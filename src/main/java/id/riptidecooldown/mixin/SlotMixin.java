package id.riptidecooldown.mixin;

import id.riptidecooldown.NetheriteRule;
import id.riptidecooldown.StorageRule;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
	@Shadow @Final public Container container;

	@Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
	private void riptidecooldown$blockNetherite(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (StorageRule.isRestricted(container) && NetheriteRule.isBlocked(stack)) {
			cir.setReturnValue(false);
		}
	}
}
