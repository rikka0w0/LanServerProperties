package rikka.lanserverproperties.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.players.PlayerList;

@Mixin(PlayerList.class)
public interface PlayerListAccessor {
    @Accessor
    @Mutable
    public void setMaxPlayers(int maxPlayers);
}
