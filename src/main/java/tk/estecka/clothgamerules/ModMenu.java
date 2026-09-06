package tk.estecka.clothgamerules;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.gamerules.GameRules;
import tk.estecka.clothgamerules.api.ClothGamerulesScreenFactory;

public class ModMenu
implements ModMenuApi
{
	public ConfigScreenFactory<?> getModConfigScreenFactory(){
		return this::getModConfigScreen;
	}

	public Screen getModConfigScreen(Screen parent){
		return ClothGamerulesScreenFactory.CreateScreen(
			parent,
			Component.literal("Test Screen"),
			new GameRules(FeatureFlags.REGISTRY.allFlags()),
			noop->{}
		);
	}
}
