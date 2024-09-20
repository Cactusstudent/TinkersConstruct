package slimeknights.tconstruct.plugin.jsonthings.item;

import dev.gigaherz.jsonthings.things.builders.ItemBuilder;
import dev.gigaherz.jsonthings.things.items.FlexItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingLookup;
import slimeknights.tconstruct.library.tools.part.PartCastItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Tool part cast item, which adds the part cost to the tooltip */
public class FlexPartCastItem extends FlexItem {
  private final Supplier<Item> part;
  public FlexPartCastItem(Properties properties, ItemBuilder builder, Supplier<Item> part) {
    super(properties, builder.getUseAnim(), builder.getUseTime(), builder.getUseFinishMode(), builder.getAttributeModifiers(), Objects.requireNonNullElse(builder.getLore(), List.of()));
    this.part = part;
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    int cost = MaterialCastingLookup.getItemCost(part.get());
    if (cost > 0) {
      tooltip.add(new TranslatableComponent(PartCastItem.COST_KEY, cost).withStyle(ChatFormatting.GRAY));
    }
  }
}
