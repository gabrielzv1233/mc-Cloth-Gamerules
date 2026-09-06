package tk.estecka.clothgamerules.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.TextListEntry;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.TextDescriptionBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import tk.estecka.clothgamerules.ETypeToken;
import tk.estecka.clothgamerules.IRuleCategory;
import tk.estecka.clothgamerules.RuleEntry;

public final class ClothGamerulesScreenBuilder
{
	static private final FeatureFlagSet ALL_FEATURES = FeatureFlags.REGISTRY.allFlags();
	static public final Component DEFAULT_TITLE = Component.translatable("editGamerule.title");

	private GameRules rules = new GameRules(ALL_FEATURES);
	private GameRules resetValues = new GameRules(ALL_FEATURES);
	private Screen parent = null;
	private Component title = DEFAULT_TITLE;
	private Consumer<Optional<GameRules>> onClosed = (_0)->{};

	private final Map<String, GameRules> displayValues = new LinkedHashMap<>();
	{
		displayValues.put("editGamerule.default", new GameRules(ALL_FEATURES));
	}

	public ClothGamerulesScreenBuilder Parent(Screen parent) {
		this.parent = parent;
		return this;
	}

	public ClothGamerulesScreenBuilder Title(Component title) {
		this.title = title;
		return this;
	}

	public ClothGamerulesScreenBuilder OnClosed(Consumer<Optional<GameRules>> onClosed) {
		this.onClosed = onClosed;
		return this;
	}

	public ClothGamerulesScreenBuilder ActiveValues(GameRules activeValues) {
		this.rules = activeValues;
		return this;
	}

	public ClothGamerulesScreenBuilder ResetValues(GameRules resetValues){
		this.resetValues = resetValues.copy(ALL_FEATURES);
		return this;
	}

	public ClothGamerulesScreenBuilder DisplayValues(String translationKey, @Nullable GameRules values){
		if (values == null)
			this.displayValues.remove(translationKey);
		else
			this.displayValues.put(translationKey, values.copy(ALL_FEATURES));
		return this;
	}

	static private final Component WILDCARD_TITLE = Component.translatable("cloth-gamerules.wildcardTab").withStyle(ChatFormatting.YELLOW);
	static private final Component MISSING_WIDGET = Component.empty().withStyle(ChatFormatting.RED)
		.append("(")
		.append(Component.translatable("cloth-gamerules.missing_widget"))
		.append(")");

	static private class CategoryEntries {
		public final TextListEntry header;
		public final List<AbstractConfigListEntry<?>> entries = new ArrayList<>();

		CategoryEntries(ConfigEntryBuilder entryBuilder, IRuleCategory cat){
			this.header = entryBuilder.startTextDescription(cat.GetTitle()).build();
		}
	}

	public Screen Build(){
		final ConfigBuilder builder = ConfigBuilder.create();
		final ConfigEntryBuilder entries = builder.entryBuilder();

		Map<Identifier, CategoryEntries> subs = new HashMap<>();
		Map<Identifier, GameRuleCategory> vanillaCats = new HashMap<>();

		builder.setParentScreen(parent);
		builder.setTitle(title);
		builder.setSavingRunnable(() -> onClosed.accept(Optional.of(rules)));

		rules.availableRules()
		.sorted(Comparator.comparing(GameRule::id))
		.forEach(key->{
			IRuleCategory cat = GetCategory(key);
			Identifier catId = cat.GetId();

			vanillaCats.computeIfAbsent(catId, __-> key.category());
			var sub = subs.computeIfAbsent(catId, id -> new CategoryEntries(entries, cat));

			RuleEntry<?> ruleEntry = new RuleEntry<>(rules, resetValues, key);
			var field = StartRuleField(entries, ruleEntry);
			AbstractConfigListEntry<?> entry = (field != null) ? field.build() : StartMissingType(entries, key).build();
			sub.entries.add(entry);

			List<String> searchTags = new ArrayList<>();
			searchTags.add(key.getDescriptionId());
			searchTags.add(I18n.get(key.getDescriptionId()));
			entry.appendSearchTags(searchTags);
			sub.header.appendSearchTags(searchTags);
		});

		var sortedSubs = subs.entrySet().stream().sorted((a,b)->{
			Identifier idA=a.getKey(), idB=b.getKey();
			boolean isAVanilla = idA.getNamespace().equals("minecraft");
			boolean isBVanilla = idB.getNamespace().equals("minecraft");

			if (isAVanilla != isBVanilla)
				return -Boolean.compare(isAVanilla, isBVanilla);
			else {
				int diff = idA.getNamespace().compareTo(idB.getNamespace());
				if (diff != 0)
					return diff;
				else
					return idA.getPath().compareTo(idB.getPath());
			}
		});

		Map<String, ConfigCategory> tabs = new HashMap<>();
		final var wildcard = builder.getOrCreateCategory(WILDCARD_TITLE);
		for (var entry : sortedSubs.toList()) {
			Identifier id = entry.getKey();
			CategoryEntries sub = entry.getValue();
			ConfigCategory tab = tabs.computeIfAbsent(id.getNamespace(), ns -> builder.getOrCreateCategory(Component.literal(ns)));

			wildcard.addEntry(sub.header);
			tab.addEntry(sub.header);
			sub.entries.forEach(e -> {tab.addEntry(e); wildcard.addEntry(e);});
		}

		builder.setFallbackCategory(wildcard);
		return builder.build();
	}

	static private IRuleCategory GetCategory(GameRule<?> key){
		return IRuleCategory.Of(key.category());
	}

	private Optional<Component[]> CreateTooltip(GameRule<?> key){
		ArrayList<Component> tooltip = new ArrayList<>(4);
		String descKey = key.getDescriptionId()+".description";

		tooltip.add(Component.literal(key.id()).withStyle(ChatFormatting.YELLOW));
		if (I18n.exists(descKey))
			tooltip.add(Component.translatable(descKey));

		for (var entry : this.displayValues.entrySet()){
			tooltip.add(
				Component.translatable(entry.getKey(), entry.getValue().getAsString(key))
				.withStyle(ChatFormatting.GRAY)
			);
		}

		return Optional.of(tooltip.toArray(new Component[0]));
	}

	private <T> AbstractFieldBuilder<?,?,?> StartRuleField(ConfigEntryBuilder entryBuilder, RuleEntry<T> entry) {
		ETypeToken ruleType = entry.GetTypeToken();

		AbstractFieldBuilder<?,?,?> field = switch (ruleType) {
			case BOOL -> StartBoolField(entryBuilder, (RuleEntry<Boolean>)entry);
			case ENUM -> StartEnumField(entryBuilder, (RuleEntry<Enum>)entry);
			default -> StartStringField(entryBuilder, entry);
		};

		if (field != null)
			field.setTooltipSupplier(() -> CreateTooltip(entry.key()));

		return field;
	}

	private AbstractFieldBuilder<?,?,?> StartBoolField(ConfigEntryBuilder entryBuilder, RuleEntry<Boolean> entry) {
		return entryBuilder.startBooleanToggle(entry.GetDisplayName(), entry.GetValue())
			.setSaveConsumer(entry::SetValue)
			.setErrorSupplier(entry::ErrorProvider)
			.setDefaultValue(entry.GetReset());
	}

	private <T extends Enum<T>> AbstractFieldBuilder<?,?,?> StartEnumField(ConfigEntryBuilder entryBuilder, RuleEntry<T> entry) {
		Class<T> clazz = entry.key().defaultValue().getDeclaringClass();
		return entryBuilder.startEnumSelector(entry.GetDisplayName(), clazz, entry.GetValue())
			.setSaveConsumer(entry::SetValue)
			.setErrorSupplier(entry::ErrorProvider)
			.setDefaultValue(entry.GetReset());
	}

	private <T> AbstractFieldBuilder<?,?,?> StartStringField(ConfigEntryBuilder entryBuilder, RuleEntry<T> entry) {
		return entryBuilder.startStrField(entry.GetDisplayName(), entry.GetStringValue())
			.setSaveConsumer(entry::SetStringValue)
			.setErrorSupplier(entry::StringErrorProvider)
			.setDefaultValue(entry.GetStringReset());
	}

	@Deprecated
	private TextDescriptionBuilder StartMissingType(ConfigEntryBuilder entryBuilder, GameRule<?> key){
		Component text = Component.translatable(key.getDescriptionId()).withStyle(ChatFormatting.GRAY)
			.append(" ")
			.append(MISSING_WIDGET);

		var entry = entryBuilder.startTextDescription(text);
		entry.setTooltipSupplier(() -> CreateTooltip(key));
		return entry;
	}
}
