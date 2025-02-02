package org.workcraft.plugins.builtin.settings;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.Settings;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class CommonLogSettings implements Settings {
    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CommonLogSettings";

    private static final String keyTextColor = prefix + ".textColor";
    private static final String keyInfoBackground = prefix + ".infoBackground";
    private static final String keyWarningBackground = prefix + ".warningBackground";
    private static final String keyErrorBackground = prefix + ".errorBackground";
    private static final String keyStdoutBackground = prefix + ".stdoutBackground";
    private static final String keyStderrBackground = prefix + ".stderrBackground";

    private static final Color defaultTextColor = Color.BLACK;
    private static final Color defaultInfoBackground = new Color(0.7f, 1.0f, 0.7f);
    private static final Color defaultWarningBackground = new Color(1.0f, 0.8f, 0.0f);
    private static final Color defaultErrorBackground = new Color(1.0f, 0.7f, 0.7f);
    private static final Color defaultStdoutBackground = new Color(0.9f, 0.9f, 0.9f);
    private static final Color defaultStderrBackground = new Color(1.0f, 0.9f, 0.9f);

    private static Color textColor = defaultTextColor;
    private static Color infoBackground = defaultInfoBackground;
    private static Color warningBackground = defaultWarningBackground;
    private static Color errorBackground = defaultErrorBackground;
    private static Color stdoutBackground = defaultStdoutBackground;
    private static Color stderrBackground = defaultStderrBackground;

    public CommonLogSettings() {
        properties.add(new PropertyDeclaration<CommonLogSettings, Color>(
                this, "Text color", Color.class) {
            @Override
            public void setter(CommonLogSettings object, Color value) {
                setTextColor(value);
            }
            @Override
            public Color getter(CommonLogSettings object) {
                return getTextColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonLogSettings, Color>(
                this, "Important info background", Color.class) {
            @Override
            public void setter(CommonLogSettings object, Color value) {
                setInfoBackground(value);
            }
            @Override
            public Color getter(CommonLogSettings object) {
                return getInfoBackground();
            }
        });

        properties.add(new PropertyDeclaration<CommonLogSettings, Color>(
                this, "Warning background", Color.class) {
            @Override
            public void setter(CommonLogSettings object, Color value) {
                setWarningBackground(value);
            }
            @Override
            public Color getter(CommonLogSettings object) {
                return getWarningBackground();
            }
        });

        properties.add(new PropertyDeclaration<CommonLogSettings, Color>(
                this, "Error background", Color.class) {
            @Override
            public void setter(CommonLogSettings object, Color value) {
                setErrorBackground(value);
            }
            @Override
            public Color getter(CommonLogSettings object) {
                return getErrorBackground();
            }
        });

        properties.add(new PropertyDeclaration<CommonLogSettings, Color>(
                this, "Backend stdout background", Color.class) {
            @Override
            public void setter(CommonLogSettings object, Color value) {
                setStdoutBackground(value);
            }
            @Override
            public Color getter(CommonLogSettings object) {
                return getStdoutBackground();
            }
        });

        properties.add(new PropertyDeclaration<CommonLogSettings, Color>(
                this, "Backend stderr background", Color.class) {
            @Override
            public void setter(CommonLogSettings object, Color value) {
                setStderrBackground(value);
            }
            @Override
            public Color getter(CommonLogSettings object) {
                return getStderrBackground();
            }
        });
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setTextColor(config.getColor(keyTextColor, defaultTextColor));
        setInfoBackground(config.getColor(keyInfoBackground, defaultInfoBackground));
        setWarningBackground(config.getColor(keyWarningBackground, defaultWarningBackground));
        setErrorBackground(config.getColor(keyErrorBackground, defaultErrorBackground));
        setStdoutBackground(config.getColor(keyStdoutBackground, defaultStdoutBackground));
        setStderrBackground(config.getColor(keyStderrBackground, defaultStderrBackground));
    }

    @Override
    public void save(Config config) {
        config.setColor(keyTextColor, getTextColor());
        config.setColor(keyInfoBackground, getInfoBackground());
        config.setColor(keyWarningBackground, getWarningBackground());
        config.setColor(keyErrorBackground, getErrorBackground());
        config.setColor(keyStdoutBackground, getStdoutBackground());
        config.setColor(keyStderrBackground, getStderrBackground());
    }

    @Override
    public String getSection() {
        return "Common";
    }

    @Override
    public String getName() {
        return "Log";
    }

    public static Color getTextColor() {
        return textColor;
    }

    public static void setTextColor(Color value) {
        textColor = value;
    }

    public static Color getInfoBackground() {
        return infoBackground;
    }

    public static void setInfoBackground(Color value) {
        infoBackground = value;
    }

    public static Color getWarningBackground() {
        return warningBackground;
    }

    public static void setWarningBackground(Color value) {
        warningBackground = value;
    }

    public static Color getErrorBackground() {
        return errorBackground;
    }

    public static void setErrorBackground(Color value) {
        errorBackground = value;
    }

    public static Color getStdoutBackground() {
        return stdoutBackground;
    }

    public static void setStdoutBackground(Color value) {
        stdoutBackground = value;
    }

    public static Color getStderrBackground() {
        return stderrBackground;
    }

    public static void setStderrBackground(Color value) {
        stderrBackground = value;
    }

}
