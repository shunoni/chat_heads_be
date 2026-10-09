package dzwdz.chat_heads.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dzwdz.chat_heads.ChatHeads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;
import java.util.function.Supplier;

@Mixin(PlayerSkinRenderCache.class)
public abstract class PlayerSkinRenderCacheMixin {
    @Unique
    private static final String BEDROCK_TEXTURE_NAMESPACE = "viafabricplus-bedrock";

    @ModifyReturnValue(method = "createLookup", at = @At("RETURN"))
    private Supplier<PlayerSkinRenderCache.RenderInfo> chatheads$useBedrockTabListSkin(
            Supplier<PlayerSkinRenderCache.RenderInfo> original, ResolvableProfile profile) {
        var connection = Minecraft.getInstance().getConnection();
        UUID profileId = profile.partialProfile().id();
        if (connection == null || profileId == null)
            return original;

        PlayerInfo playerInfo = connection.getOnlinePlayers().stream()
                .filter(info -> profileId.equals(info.getProfile().id()))
                .findFirst()
                .orElse(null);
        if (playerInfo == null)
            return original;

        PlayerSkinRenderCache cache = (PlayerSkinRenderCache) (Object) this;
        return () -> {
            if (!ChatHeads.CONFIG.useBedrockTabListSkins())
                return original.get();

            PlayerSkin skin = playerInfo.getSkin();
            if (skin.body() == null || !BEDROCK_TEXTURE_NAMESPACE.equals(skin.body().texturePath().getNamespace()))
                return original.get();

            return cache.new RenderInfo(
                    playerInfo.getProfile(),
                    skin,
                    profile.skinPatch()
            );
        };
    }
}