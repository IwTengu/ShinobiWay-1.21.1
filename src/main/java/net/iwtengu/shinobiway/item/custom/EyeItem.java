package net.iwtengu.shinobiway.item.custom;

import net.iwtengu.shinobiway.component.ModDataComponents;
import net.iwtengu.shinobiway.util.EyeOwnerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class EyeItem extends Item {

    public EyeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag flag) {

        String ownerName = EyeOwnerHelper.getOwnerName(stack);

        if (ownerName != null) {
            // Показываем владельца в тултипе
            tooltipComponents.add(
                    Component.translatable("item.shinobiway.eye.owner", ownerName)
                            .withStyle(style -> style.withColor(0xAAAAAA))
            );
        } else {
            // Глаз ещё не привязан
            tooltipComponents.add(
                    Component.translatable("item.shinobiway.eye.unbound")
                            .withStyle(style -> style.withColor(0x888888))
            );
        }

        super.appendHoverText(stack, context, tooltipComponents, flag);
    }
}