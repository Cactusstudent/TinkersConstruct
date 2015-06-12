package tconstruct.library.tinkering;


import gnu.trove.set.hash.THashSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import tconstruct.library.TinkerRegistry;
import tconstruct.library.utils.Tags;
import tconstruct.library.utils.TinkerUtil;

/**
 * The base for each Tinker tool.
 */
public abstract class TinkersItem extends Item implements ITinkerable, IModifyable {

  public final PartMaterialType[] requiredComponents;
  // used to classify what the thing can do
  protected final Set<Category> categories = new THashSet<>();

  public TinkersItem(PartMaterialType... requiredComponents) {
    this.requiredComponents = requiredComponents;

    this.setMaxStackSize(1);
    this.setHasSubtypes(true);
  }


  /* Tool Information */
  protected void addCategory(Category... categories) {
    for (Category category : categories) {
      this.categories.add(category);
    }
  }

  public boolean hasCategory(Category category) {
    return categories.contains(category);
  }

  /* Building the Item */
  public boolean validComponent(int slot, ItemStack stack) {
    if (slot > requiredComponents.length || slot < 0) {
      return false;
    }

    return requiredComponents[slot].isValid(stack);
  }

  /**
   * Builds an Itemstack of this tool with the given materials, if applicable.
   *
   * @param stacks Items to build with. Have to be in the correct order. No nulls!
   * @return The built item or null if invalid input.
   */
  public ItemStack buildItemFromStacks(ItemStack[] stacks) {
    Material[] materials = new Material[stacks.length];
    // not a valid part arrangement for tis tool
    for (int i = 0; i < stacks.length; i++) {
      if (!validComponent(i, stacks[i])) {
        return null;
      }

      materials[i] = TinkerUtil.getMaterialFromStack(stacks[i]);
    }

    return buildItem(materials);
  }

  /**
   * Builds an Itemstack of this tool with the given materials.
   *
   * @param materials Materials to build with. Have to be in the correct order. No nulls!
   * @return The built item or null if invalid input.
   */
  public ItemStack buildItem(Material[] materials) {
    ItemStack tool = new ItemStack(this);
    NBTTagCompound basetag = new NBTTagCompound();
    NBTTagCompound toolTag = buildTag(materials);
    NBTTagCompound dataTag = buildData(materials);

    basetag.setTag(Tags.BASE_DATA, dataTag);
    basetag.setTag(Tags.TOOL_DATA, toolTag);
    tool.setTagCompound(basetag);

    return tool;
  }

  /**
   * Creates an NBT Tag with the materials that were used to build the item.
   */
  private NBTTagCompound buildData(Material[] materials) {
    NBTTagCompound base = new NBTTagCompound();
    NBTTagCompound tag = new NBTTagCompound();
    for (int i = 0; i < materials.length; i++) {
      tag.setString(String.valueOf(i), materials[i].identifier);
    }

    base.setTag(Tags.BASE_MATERIALS, tag);
    base.setTag(Tags.BASE_MODIFIERS, new NBTTagCompound());

    return base;
  }

  protected abstract NBTTagCompound buildTag(Material[] materials);

  /* Information */

  @Override
  public void addInformation(ItemStack stack, EntityPlayer playerIn, List tooltip,
                             boolean advanced) {
    Collections.addAll(tooltip, this.getInformation(stack));
  }

  /* NBT loading */

  @Override
  public boolean updateItemStackNBT(NBTTagCompound nbt) {
    // when the itemstack is loaded from NBT we recalculate all the data
    if (nbt.hasKey(Tags.BASE_DATA)) {
      NBTTagCompound data = nbt.getCompoundTag(Tags.BASE_DATA);
      List<Material> materials = new LinkedList<>();
      int index = 0;
      while (data.hasKey(String.valueOf(index))) {
        // load the material from the data
        String identifier = data.getString(String.valueOf(index));
        // this will return Material.UNKNOWN if it doesn't exist (anymore)
        Material mat = TinkerRegistry.getMaterial(identifier);
        materials.add(mat);
        index++;
      }

      NBTTagCompound toolTag = buildTag(materials.toArray(new Material[materials.size()]));
      // update the tag
      nbt.setTag(Tags.TOOL_DATA, toolTag);

      // todo: ensure that traits loaded from NBT are mapped to the same string instance as the trait identifier so == lookup matches

    }

    // return value shoudln't matter since it's never checked
    return true;
  }
}
