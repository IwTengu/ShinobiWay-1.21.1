package net.iwtengu.shinobiway.client;

import net.iwtengu.shinobiway.item.custom.HeadbandItem;
import net.iwtengu.shinobiway.ShinobiWay;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HeadbandModel extends GeoModel<HeadbandItem> {

    @Override
    public ResourceLocation getModelResource(HeadbandItem item) {
        return ResourceLocation.fromNamespaceAndPath(ShinobiWay.MOD_ID, "geo/headband.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HeadbandItem item) {
        return item.getHeadbandTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(HeadbandItem item) {
        return ResourceLocation.fromNamespaceAndPath(ShinobiWay.MOD_ID, "animations/headband.animation.json");
    }
}