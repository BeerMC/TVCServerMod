package com.matata.server.mixin;

import mezz.jei.common.config.GiveMode;
import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.common.util.ServerCommandUtil;
import mezz.jei.common.platform.Services;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static net.neoforged.neoforgespi.ILaunchContext.LOGGER;


@Mixin(ServerCommandUtil.class)
public class JEIMixin {

    @Unique
    private static final String[] SUPPORTED_MATERIALS = {
            "yuushya",
            "xkdeco",
            "elegant_countryside",
            "mcwfurnitures",
            "mcwdoors",
            "mcwfences",
            "mcwwindows",
    };

    @Inject(
            method = "hasPermissionForCheatMode",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onHasPermissionForCheatMode(Player sender, IServerConfig serverConfig,
                                                    CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
        cir.cancel();
    }

    @Inject(
            method = "executeGive",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onGive(ServerPacketContext context,
                               ItemStack itemStack,
                               GiveMode giveMode, CallbackInfo cir) {
        ServerPlayer sender = context.player();
        if (itemStack.isEmpty()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Player '{} ({})' tried to give an empty ItemStack.", sender.getName(), sender.getUUID());
            }
            return;
        }
        if(sender.isCreative() || tvc$isFreeMaterial(itemStack)) {
            if (giveMode == GiveMode.INVENTORY) {
                tvc$giveToInventory(sender, itemStack);
            } else if (giveMode == GiveMode.MOUSE_PICKUP) {
                tvc$mousePickupItemStack(sender, itemStack);
            }
        }
        cir.cancel();
    }

    @Unique
    private static void tvc$mousePickupItemStack(Player sender, ItemStack itemStack) {
        AbstractContainerMenu containerMenu = sender.containerMenu;

        ItemStack itemStackCopy = itemStack.copy();
        ItemStack existingStack = containerMenu.getCarried();

        final int giveCount;
        if (tvc$canStack(existingStack, itemStack)) {
            int newCount = Math.min(existingStack.getMaxStackSize(), existingStack.getCount() + itemStack.getCount());
            giveCount = newCount - existingStack.getCount();
            if (giveCount > 0) {
                existingStack.setCount(newCount);
            }
        } else {
            containerMenu.setCarried(itemStack);
            giveCount = itemStack.getCount();
        }

        if (giveCount > 0) {
            itemStackCopy.setCount(giveCount);
            tvc$notifyGive(sender, itemStackCopy);
            containerMenu.broadcastChanges();
        }
    }

    @Unique
    private static boolean tvc$canStack(ItemStack a, ItemStack b) {
        ItemStack singleA = a.copyWithCount(1);
        ItemStack singleB = b.copyWithCount(1);
        return ItemEntity.areMergable(singleA, singleB);
    }

    @Unique
    private static void tvc$giveToInventory(Player entityplayermp, ItemStack itemStack) {
        ItemStack itemStackCopy = itemStack.copy();
        boolean flag = entityplayermp.getInventory().add(itemStack);
        if (flag && itemStack.isEmpty()) {
            itemStack.setCount(1);
            ItemEntity entityitem = entityplayermp.drop(itemStack, false);
            if (entityitem != null) {
                entityitem.makeFakeItem();
            }

            entityplayermp.level().playSound(null, entityplayermp.getX(), entityplayermp.getY(), entityplayermp.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((entityplayermp.getRandom().nextFloat() - entityplayermp.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            entityplayermp.inventoryMenu.broadcastChanges();
        } else {
            ItemEntity entityitem = entityplayermp.drop(itemStack, false);
            if (entityitem != null) {
                entityitem.setNoPickUpDelay();
                entityitem.setTarget(entityplayermp.getUUID());
            }
        }

        tvc$notifyGive(entityplayermp, itemStackCopy);
    }


    @Unique
    private static void tvc$notifyGive(Player player, ItemStack stack) {
        if (player.getServer() == null) {
            return;
        }
        CommandSourceStack commandSource = player.createCommandSourceStack();
        int count = stack.getCount();
        Component stackTextComponent = stack.getDisplayName();
        Component displayName = player.getDisplayName();
        Component message = Component.translatable("commands.give.success.single", count, stackTextComponent, displayName);
        commandSource.sendSuccess(() -> message, true);
    }

    @Unique
    private static String tvc$getDisplayModId(ItemStack ingredient) {
        return Services.PLATFORM.getItemStackHelper().getCreatorModId(ingredient)
                .or(() -> tvc$getNamespace(ingredient))
                .orElse(null);
    }

    @Unique
    private static Optional<String> tvc$getNamespace(ItemStack ingredient) {
        ResourceLocation key = RegistryUtil
                .getRegistry(Registries.ITEM)
                .getKey(ingredient.getItem());
        return Optional.ofNullable(key)
                .map(ResourceLocation::getNamespace);
    }

    @Unique
    private static boolean tvc$isFreeMaterial(ItemStack stack) {
        String namespace = tvc$getDisplayModId(stack);
        if(namespace == null) return false;
        for (String prefix : SUPPORTED_MATERIALS) {
            if (namespace.equals(prefix)) {
                return true;
            }
        }
        return false;
    }

}
