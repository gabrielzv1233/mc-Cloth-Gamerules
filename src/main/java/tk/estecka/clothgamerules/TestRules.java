package tk.estecka.clothgamerules;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public class TestRules
implements ModInitializer
{
	static public enum ETestEnum {
		NONE,
		SOME,
		ALL
	}

	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().isDevelopmentEnvironment())
		{
			GameRuleCategory fabricCategory = new GameRuleCategory(Identifier.fromNamespaceAndPath("cloth-gamerules", "test_rules"));
			GameRuleBuilder.forBoolean(true).category(fabricCategory).buildAndRegister(Identifier.fromNamespaceAndPath("clothrule", "bool"));
			GameRuleBuilder.forInteger(1).category(fabricCategory).buildAndRegister(Identifier.fromNamespaceAndPath("clothrule", "int"));
			GameRuleBuilder.forDouble(1.0).category(fabricCategory).buildAndRegister(Identifier.fromNamespaceAndPath("clothrule", "double"));
			GameRuleBuilder.forEnum(ETestEnum.SOME).category(fabricCategory).buildAndRegister(Identifier.fromNamespaceAndPath("clothrule", "enum"));

			GameRuleBuilder.forInteger(1).range(0, 16)
				.category(fabricCategory).buildAndRegister(Identifier.fromNamespaceAndPath("clothrule", "int.bounded"));
			GameRuleBuilder.forDouble(1.0).range(0.1, 9.99)
				.category(fabricCategory).buildAndRegister(Identifier.fromNamespaceAndPath("clothrule", "double.bounded"));
			GameRuleBuilder.forEnum(ETestEnum.SOME).supportedValues(ETestEnum.NONE, ETestEnum.SOME)
				.category(fabricCategory).buildAndRegister(Identifier.fromNamespaceAndPath("clothrule", "enum.bounded"));
		}
	}
}
