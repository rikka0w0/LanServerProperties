package rikka.lanserverproperties;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

/**
 * Represents an invisible widget group. By adding widgets to widget group,
 * it is no longer necessary to store a reference to whose widgets in the Screen object.
 * For a given Screen object, each widget group should have only 1 instance.
 * Widget groups are identified by their class types.
 */
public abstract class InvisibleWidgetGroup extends AbstractWidget {
	public final String name;

	public InvisibleWidgetGroup() {
		this(null);
	}

	public InvisibleWidgetGroup(String name) {
		super(0, 0, 0, 0, new TextComponent(name));
		this.name = name;
		this.active = false;
		this.visible = false;
	}

	@SuppressWarnings("unchecked")
	public static <T extends InvisibleWidgetGroup> T fromScreen(Screen screen, Class<T> groupCls) {
		if (screen == null)
			return null;

		for (GuiEventListener element: screen.children()) {
			if (element.getClass().equals(groupCls)) {
				return (T) element;
			}
		}

		return null;
	}

	@Override
	public void updateNarration(NarrationElementOutput p_169152_) {

	}
}
