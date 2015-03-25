/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.shared;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class CommonEditorSettings implements Settings {

	public enum TitleStyle {
		MINIMAL("minimal: Title"),
		SHORT("short: Title [MN]"),
		LONG("long: Title - Model Name");

		public final String name;

		private TitleStyle(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static final LinkedList<PropertyDescriptor> properties = new LinkedList<PropertyDescriptor>();
	private static final String prefix = "CommonEditorSettings";

	private static final String keyBackgroundColor = prefix + ".backgroundColor";
	private static final String keyShowGrid = prefix + ".showGrid";
	private static final String keyShowRulers = prefix + ".showRulers";
	private static final String keyRecentCount = prefix + ".recentCount";
	private static final String keyIconSize = prefix + ".iconSize";
	private static final String keyTitleStyle = prefix + ".titleStyle";
	private static final String keyShowAbsolutePaths = prefix + ".showAbsolutePaths";
	private static final String keyDebugClipboard = prefix + ".debugClipboard";
	private static final String keyOpenNonvisual = prefix + ".openNonvisual";

	private static final Color defaultBackgroundColor = Color.WHITE;
	private static final boolean defaultShowGrid = true;
	private static final boolean defaultShowRulers = true;
	private static final int defaultIconSize = 24;
	private static final int defaultRecentCount = 10;
	private static final TitleStyle defaultTitleStyle = TitleStyle.SHORT;
	private static final boolean defaultShowAbsolutePaths = false;
	private static final boolean defaultDebugClipboard = false;
	private static final boolean defaultOpenNonvisual = true;

	private static Color backgroundColor = defaultBackgroundColor;
	private static boolean showGrid = defaultShowGrid;
	private static boolean showRulers = defaultShowRulers;
	private static int iconSize = defaultIconSize;
	private static int recentCount = defaultRecentCount;
	private static TitleStyle titleStyle = defaultTitleStyle;
	private static boolean showAbsolutePaths = defaultShowAbsolutePaths;
	private static boolean debugClipboard = defaultDebugClipboard;
	private static boolean openNonvisual = defaultOpenNonvisual;

	public CommonEditorSettings() {
		properties.add(new PropertyDeclaration<CommonEditorSettings, Color>(
				this, "Background color", Color.class) {
			protected void setter(CommonEditorSettings object, Color value) {
				CommonEditorSettings.setBackgroundColor(value);
			}
			protected Color getter(CommonEditorSettings object) {
				return CommonEditorSettings.getBackgroundColor();
			}
		});

		properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
				this, "Show grid", Boolean.class) {
			protected void setter(CommonEditorSettings object, Boolean value) {
				CommonEditorSettings.setShowGrid(value);
			}
			protected Boolean getter(CommonEditorSettings object) {
				return CommonEditorSettings.getShowGrid();
			}
		});

		properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
				this, "Show rulers", Boolean.class) {
			protected void setter(CommonEditorSettings object, Boolean value) {
				CommonEditorSettings.setShowRulers(value);
			}
			protected Boolean getter(CommonEditorSettings object) {
				return CommonEditorSettings.getShowRulers();
			}
		});

		properties.add(new PropertyDeclaration<CommonEditorSettings, Integer>(
				this, "Icon width (pixels, 8-256)", Integer.class) {
			protected void setter(CommonEditorSettings object, Integer value) {
				CommonEditorSettings.setIconSize(value);
			}
			protected Integer getter(CommonEditorSettings object) {
				return CommonEditorSettings.getIconSize();
			}
		});

		properties.add(new PropertyDeclaration<CommonEditorSettings, Integer>(
				this, "Number of recent files (0-99)", Integer.class) {
			protected void setter(CommonEditorSettings object, Integer value) {
				CommonEditorSettings.setRecentCount(value);
			}
			protected Integer getter(CommonEditorSettings object) {
				return CommonEditorSettings.getRecentCount();
			}
		});

		properties.add(new PropertyDeclaration<CommonEditorSettings, TitleStyle>(
				this, "Model title style", TitleStyle.class) {
			protected void setter(CommonEditorSettings object, TitleStyle value) {
				CommonEditorSettings.setTitleStyle(value);
			}
			protected TitleStyle getter(CommonEditorSettings object) {
				return CommonEditorSettings.getTitleStyle();
			}
		});

		properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
				this, "Names shown with absolute paths", Boolean.class) {
			protected void setter(CommonEditorSettings object, Boolean value) {
				CommonEditorSettings.setShowAbsolutePaths(value);
			}
			protected Boolean getter(CommonEditorSettings object) {
				return CommonEditorSettings.getShowAbsolutePaths();
			}
		});

		properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
				this, "Debug clipboard", Boolean.class) {
			protected void setter(CommonEditorSettings object, Boolean value) {
				CommonEditorSettings.setDebugClipboard(value);
			}
			protected Boolean getter(CommonEditorSettings object) {
				return CommonEditorSettings.getDebugClipboard();
			}
		});

		properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
				this, "Open non-visual models (imported or transformed)", Boolean.class) {
			protected void setter(CommonEditorSettings object, Boolean value) {
				CommonEditorSettings.setOpenNonvisual(value);
			}
			protected Boolean getter(CommonEditorSettings object) {
				return CommonEditorSettings.getOpenNonvisual();
			}
		});
	}

	@Override
	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	@Override
	public void load(Config config) {
		setBackgroundColor(config.getColor(keyBackgroundColor, defaultBackgroundColor));
		setShowGrid(config.getBoolean(keyShowGrid, defaultShowGrid));
		setShowRulers(config.getBoolean(keyShowRulers, defaultShowRulers));
		setIconSize(config.getInt(keyIconSize, defaultIconSize));
		setRecentCount(config.getInt(keyRecentCount, defaultRecentCount));
		setTitleStyle(config.getEnum(keyTitleStyle, TitleStyle.class, defaultTitleStyle));
		setShowAbsolutePaths(config.getBoolean(keyShowAbsolutePaths, defaultShowAbsolutePaths));
		setDebugClipboard(config.getBoolean(keyDebugClipboard, defaultDebugClipboard));
		setOpenNonvisual(config.getBoolean(keyOpenNonvisual, defaultOpenNonvisual));
	}

	@Override
	public void save(Config config) {
		config.setColor(keyBackgroundColor, getBackgroundColor());
		config.setBoolean(keyShowGrid, getShowGrid());
		config.setBoolean(keyShowRulers, getShowRulers());
		config.setInt(keyIconSize, getIconSize());
		config.setInt(keyRecentCount, getRecentCount());
		config.setEnum(keyTitleStyle, TitleStyle.class, getTitleStyle());
		config.setBoolean(keyShowAbsolutePaths, getShowAbsolutePaths());
		config.setBoolean(keyDebugClipboard, getDebugClipboard());
		config.setBoolean(keyOpenNonvisual, getOpenNonvisual());
	}

	@Override
	public String getSection() {
		return "Common";
	}

	@Override
	public String getName() {
		return "Editor";
	}

	public static Color getBackgroundColor() {
		return backgroundColor;
	}

	public static void setBackgroundColor(Color value) {
		backgroundColor = value;
	}

	public static void setShowGrid(Boolean value) {
		showGrid = value;
	}

	public static Boolean getShowGrid() {
		return showGrid;
	}

	public static void setShowRulers(Boolean value) {
		showRulers = value;
	}

	public static Boolean getShowRulers() {
		return showRulers;
	}

	public static int getIconSize() {
		return iconSize;
	}

	public static void setIconSize(int value) {
		if (value < 8) {
			value = 8;
		}
		if (value > 256) {
			value = 256;
		}
		iconSize = value;
	}

	public static int getRecentCount() {
		return recentCount;
	}

	public static void setRecentCount(int value) {
		if(value < 0) {
			value = 0;
		}
		if (value > 99) {
			value = 99;
		}
		recentCount = value;
	}

	public static TitleStyle getTitleStyle() {
		return titleStyle;
	}

	public static void setTitleStyle(TitleStyle value) {
		titleStyle = value;
	}

	public static void setShowAbsolutePaths(boolean value) {
		showAbsolutePaths = value;
	}

	public static boolean getShowAbsolutePaths() {
		return showAbsolutePaths;
	}

	public static Boolean getDebugClipboard() {
		return debugClipboard;
	}

	public static void setDebugClipboard(Boolean value) {
		debugClipboard = value;
	}

	public static Boolean getOpenNonvisual() {
		return openNonvisual;
	}

	public static void setOpenNonvisual(Boolean value) {
		openNonvisual = value;
	}

}
