package rikka.lanserverproperties;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.text.LiteralText;

/**
 * Represents an invisible widget group. By adding widgets to widget group,
 * it is no longer necessary to store a reference to whose widgets in the Screen object.
 * For a given Screen object, each widget group should have only 1 instance.
 * Widget groups are identified by their class types.
 */
public abstract class InvisibleWidgetGroup extends AbstractButtonWidget {
	public final String name;

	public InvisibleWidgetGroup() {
		this(null);
	}

	public InvisibleWidgetGroup(String name) {
		super(0, 0, 0, 0, new LiteralText(name));
		this.name = name;
		this.active = false;
		this.visible = false;
	}

	@SuppressWarnings("unchecked")
	public static <T extends InvisibleWidgetGroup> T fromScreen(Screen screen, Class<T> groupCls) {
		for (Element element: screen.children()) {
			if (element.getClass().equals(groupCls)) {
				return (T) element;
			}
		}

		return null;
	}
}
