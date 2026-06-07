package net.iwtengu.shinobiway.item;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.iwtengu.shinobiway.ShinobiWay;

import java.util.EnumMap;
import java.util.List;

public class ModArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, ShinobiWay.MOD_ID);

    public static final Holder<ArmorMaterial> HEADBAND = ARMOR_MATERIALS.register(
            "headband",
            () -> new ArmorMaterial(
                    // Защита по слотам: FEET, LEGS, CHEST, HEAD
                    new EnumMap<>(java.util.Map.of(
                            ArmorItem.Type.BOOTS, 0,
                            ArmorItem.Type.LEGGINGS, 0,
                            ArmorItem.Type.CHESTPLATE, 0,
                            ArmorItem.Type.HELMET, 2  // ← вот это важно, меняй под себя
                    )),
                    10,                          // enchantability
                    SoundEvents.ARMOR_EQUIP_LEATHER, // звук надевания
                    () -> Ingredient.EMPTY,      // материал для починки (EMPTY = нельзя чинить)
                    List.of(),                   // overlay текстуры (пусто)
                    0f,                          // toughness
                    0f                           // knockback resistance
            )
    );


}