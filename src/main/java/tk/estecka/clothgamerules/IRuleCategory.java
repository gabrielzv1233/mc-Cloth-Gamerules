package tk.estecka.clothgamerules;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public interface IRuleCategory
{
	Component GetTitle();
	Identifier GetId();

	static public IRuleCategory Of(GameRuleCategory category){
		return new IRuleCategory() {
			@Override public Component GetTitle(){ return category.label(); }
			@Override public Identifier GetId(){ return category.id(); }
		};
	}
}
