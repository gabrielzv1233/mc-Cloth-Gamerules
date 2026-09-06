package tk.estecka.clothgamerules.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import tk.estecka.clothgamerules.api.ClothGamerulesScreenBuilder;

@Mixin(targets="net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$MoreTab")
public class CreateWorldMoreTabMixin
{
	@Shadow @Final private CreateWorldScreen this$0;

	@Inject(method="openGameRulesScreen", at=@At("HEAD"), cancellable=true)
	private void clothGamerules$openGameRulesScreen(CallbackInfo ci){
		var uiState = this$0.getUiState();

		Minecraft.getInstance().gui.setScreen(
			new ClothGamerulesScreenBuilder()
				.Parent(this$0)
				.ActiveValues(uiState.getGameRules())
				.OnClosed(result -> result.ifPresent(uiState::setGameRules))
				.Build()
		);

		ci.cancel();
	}
}
