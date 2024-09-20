package slimeknights.tconstruct.library.recipe.partbuilder;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.tables.TinkerTables;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

@RequiredArgsConstructor(staticName = "item")
public class ItemPartRecipeBuilder extends AbstractRecipeBuilder<ItemPartRecipeBuilder> {
  private final ResourceLocation pattern;
  private final ItemOutput result;
  @Setter @Accessors(chain = true)
  private Ingredient patternItem;
  private MaterialId materialId = IMaterial.UNKNOWN_ID;
  private int cost = 0;

  /** @deprecated use {@link #item(ResourceLocation, ItemOutput)} and {@link #material(MaterialId, int)} */
  @Deprecated
  public static ItemPartRecipeBuilder item(MaterialId material, ResourceLocation pattern, int cost, ItemOutput result) {
    return item(pattern, result).material(material, cost);
  }

  /** Sets the material Id and cost */
  public ItemPartRecipeBuilder material(MaterialId material, int cost) {
    this.materialId = material;
    this.cost = cost;
    return this;
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, Objects.requireNonNull(result.get().getItem().getRegistryName()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    ResourceLocation advancementId = buildOptionalAdvancement(id, "parts");
    consumer.accept(new Finished(id, advancementId));
  }

  private class Finished extends AbstractFinishedRecipe {
    public Finished(ResourceLocation ID, @Nullable ResourceLocation advancementID) {
      super(ID, advancementID);
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      if (!materialId.equals(IMaterial.UNKNOWN_ID)) {
        json.addProperty("material", materialId.toString());
      }
      json.addProperty("pattern", pattern.toString());
      if (patternItem != null) {
        json.add("pattern_item", patternItem.toJson());
      }
      if (cost > 0) {
        json.addProperty("cost", cost);
      }
      json.add("result", result.serialize());
    }

    @Override
    public RecipeSerializer<?> getType() {
      return TinkerTables.itemPartBuilderSerializer.get();
    }
  }
}
