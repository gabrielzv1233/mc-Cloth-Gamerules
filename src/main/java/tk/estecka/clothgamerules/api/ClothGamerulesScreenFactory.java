package tk.estecka.clothgamerules.api;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.gamerules.GameRules;

public interface ClothGamerulesScreenFactory
{
	static public Screen CreateScreen(Screen parent, GameRules rules, Consumer<Optional<GameRules>> onClose){
		return new ClothGamerulesScreenBuilder().Parent(parent).ActiveValues(rules).OnClosed(onClose).Build();
	}

	static public Screen CreateScreen(Screen parent, GameRules rules, GameRules resetValues, Consumer<Optional<GameRules>> onClose){
		return new ClothGamerulesScreenBuilder().Parent(parent).ActiveValues(rules).ResetValues(resetValues).OnClosed(onClose).Build();
	}

	static public Screen CreateScreen(Screen parent, Component title, GameRules rules, Consumer<Optional<GameRules>> onClose){
		return new ClothGamerulesScreenBuilder().Parent(parent).Title(title).ActiveValues(rules).OnClosed(onClose).Build();
	}

	static public Screen CreateScreen(Screen parent, Component title, GameRules rules, GameRules resetValues, Consumer<Optional<GameRules>> onClose){
		return new ClothGamerulesScreenBuilder().Parent(parent).Title(title).ActiveValues(rules).ResetValues(resetValues).OnClosed(onClose).Build();
	}
}
