package tk.estecka.clothgamerules;

import java.util.Optional;
import com.mojang.serialization.DataResult;
import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

public record RuleEntry<T>(
	GameRules values,
	GameRules reset,
	GameRule<T> key
){
	public Component GetDisplayName(){ return Component.translatable(key.getDescriptionId()); }

	public T GetValue()  { return values.get(key); }
	public T GetReset()  { return reset.get(key); }
	public T GetDefault(){ return key.defaultValue(); }

	public String GetStringValue()  { return key.serialize(GetValue()); }
	public String GetStringReset()  { return key.serialize(GetReset()); }
	public String GetStringDefault(){ return key.serialize(GetDefault()); }

	public void SetValue(T value) {
		this.SetStringValue(key.serialize(value));
	}

	public void SetStringValue(String value){
		DataResult<T> result = key.deserialize(value);
		if (result.isSuccess())
			values.set(key, result.getOrThrow(), null);
		else {
			ClothRulesMod.LOGGER.error(
				"Could not set Gamerule {} to \"{}\":\n{}",
				key.id(), value, result.error().get().message()
			);
		}
	}

	public Optional<Component> ErrorProvider(T value){
		return this.StringErrorProvider(key.serialize(value));
	}

	public Optional<Component> StringErrorProvider(String value){
		Component error = null;
		DataResult<T> result = key.deserialize(value);
		if (result.isError())
			error = Component.literal(result.error().get().message());

		return Optional.ofNullable(error);
	}

	public ETypeToken GetTypeToken(){
		Object type = this.GetType();
		for (ETypeToken token : ETypeToken.values())
			if (type == token.identity)
				return token;

		return ETypeToken.STRING;
	}

	public Object GetType (){
		FabricGameRuleType fabric = ((RuleTypeExtensions)(Object)key).fabric_getType();
		if (fabric != null)
			return fabric;
		else
			return key.gameRuleType();
	}
}
