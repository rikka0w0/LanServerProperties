package rikka.lanserverproperties.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.components.AbstractWidget;

@Mixin(AbstractWidget.class)
public interface WidgetHeightAccessor {
    @Accessor
    @Mutable
    public void setHeight(int height);
}
