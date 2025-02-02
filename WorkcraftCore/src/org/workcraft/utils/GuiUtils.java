package org.workcraft.utils;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.tools.GraphEditor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class GuiUtils {

    private static final int BUTTON_PREFERED_WIDTH = 100;
    private static final int BUTTON_PREFERED_HEIGHT = 25;

    public static JPanel createLabeledComponent(JComponent component, String labelText) {
        JPanel result = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        result.add(new JLabel(labelText));
        result.add(component);
        return result;
    }

    public static JPanel createWideLabeledComponent(JComponent component, String labelText) {
        JPanel result = new JPanel(createTableLayout(
                new double[]{TableLayout.PREFERRED, TableLayout.FILL},
                new double[]{TableLayout.PREFERRED}));

        result.add(new JLabel(labelText), new TableLayoutConstraints(0, 0));
        result.add(component, new TableLayoutConstraints(1, 0));
        return result;
    }

    public static void centerAndSizeToParent(Window frame, Window parent) {
        if ((frame != null) && (parent != null)) {
            Dimension parentSize = parent.getSize();
            frame.setSize(2 * parentSize.width / 3, 2 * parentSize.height / 3);
            frame.setLocationRelativeTo(parent);
        }
    }

    public static void sizeFileChooserToScreen(JFileChooser fc, DisplayMode mode) {
        int minWidth = (int) Math.round(0.2 * mode.getWidth());
        int minHeight = (int) Math.round(0.2 * mode.getHeight());
        fc.setMinimumSize(new Dimension(minWidth, minHeight));
        int preferredWidth = (int) Math.round(0.5 * mode.getWidth());
        int preferredHeight = (int) Math.round(0.5 * mode.getHeight());
        fc.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
    }

    public static void drawEditorMessage(GraphEditor editor, Graphics2D g, Color color, String message) {
        if (message != null) {
            g.setFont(UIManager.getFont("Button.font"));
            Rectangle r = g.getFont().getStringBounds(message, g.getFontRenderContext()).getBounds();
            r.x = editor.getWidth() / 2 - r.width / 2;
            r.y = editor.getHeight() - 20 - r.height;
            g.setColor(new Color(240, 240, 240, 192));
            g.fillRoundRect(r.x - 10, r.y - 10, r.width + 20, r.height + 20, 5, 5);
            g.setColor(new Color(224, 224, 224));
            g.drawRoundRect(r.x - 10, r.y - 10, r.width + 20, r.height + 20, 5, 5);
            g.setColor(color);
            LineMetrics lm = g.getFont().getLineMetrics(message, g.getFontRenderContext());
            g.drawString(message, r.x, r.y + r.height - (int) (lm.getDescent()));
        }
    }

    public static BufferedImage loadImageFromResource(String path) throws IOException {
        URL res = ClassLoader.getSystemResource(path);
        if (res == null) {
            throw new IOException("Resource not found: " + path);
        }
        return ImageIO.read(res);
    }

    public static ImageIcon createIconFromImage(String path) {
        URL res = ClassLoader.getSystemResource(path);
        if (res == null) {
            System.err.println("Missing icon: " + path);
            return null;
        }
        return new ImageIcon(res);
    }

    public static ImageIcon createIconFromSVG(String path, int height, int width, Color background) {
        try {
            System.setProperty("org.apache.batik.warn_destination", "false");
            Document document;

            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);

            document = f.createDocument(ClassLoader.getSystemResource(path).toString());

            UserAgentAdapter userAgentAdapter = new UserAgentAdapter();
            BridgeContext bridgeContext = new BridgeContext(userAgentAdapter);
            GVTBuilder builder = new GVTBuilder();

            GraphicsNode graphicsNode = builder.build(bridgeContext, document);

            double sizeY = bridgeContext.getDocumentSize().getHeight();
            double sizeX = bridgeContext.getDocumentSize().getWidth();

            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
            if (background != null) {
                g2d.setColor(background);
                g2d.fillRect(0, 0, width, height);
            }

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            double scaleX = (width - 1) / sizeX;
            double scaleY = (height - 1) / sizeY;
            double scale = Math.min(scaleX, scaleY);
            g2d.scale(scale, scale);
            g2d.translate(0.5, 0.5);

            graphicsNode.paint(g2d);
            g2d.dispose();
            return new ImageIcon(bufferedImage);
        } catch (Throwable e) {
            System.err.println("Failed to load SVG file " + path);
            System.err.println(e);
            return null;
        }
    }

    public static ImageIcon createIconFromSVG(String path) {
        int iconSize = SizeHelper.getToolIconSize();
        return createIconFromSVG(path, iconSize, iconSize);
    }

    public static ImageIcon createIconFromSVG(String path, int width, int height) {
        return createIconFromSVG(path, width, height, null);
    }

    public static Cursor createCursorFromImage(String path) {
        ImageIcon icon = createIconFromImage(path);
        return createCursorFromIcon(icon, path);
    }

    public static Cursor createCursorFromSVG(String path) {
        ImageIcon icon = createIconFromSVG(path);
        return createCursorFromIcon(icon, path);
    }

    public static Cursor createCursorFromIcon(ImageIcon icon, String name) {
        int iconSize = icon.getIconWidth();
        int crossLength = (int) Math.round(0.2 * iconSize);
        int crossWidth = (int) Math.round(0.08 * iconSize);
        int crossGap = (int) Math.round(0.05 * iconSize);
        int iconOffset = crossLength + crossGap + crossWidth + crossGap;
        Image coursorImage = new BufferedImage(iconSize + iconOffset, iconSize + iconOffset, BufferedImage.TYPE_INT_ARGB);
        Graphics g = coursorImage.getGraphics();
        Image iconImage = icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
        g.drawImage(iconImage, iconOffset, iconOffset, null);
        int posClose = crossLength + crossGap;
        int posFar = posClose + crossWidth + crossGap;
        g.setColor(Color.BLACK);
        g.fillRect(posClose, 0, crossWidth, crossLength);
        g.fillRect(posClose, posFar, crossWidth, crossLength);
        g.fillRect(0, posClose, crossLength, crossWidth);
        g.fillRect(posFar, posClose, crossLength, crossWidth);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int offset = posClose + (int) Math.round(0.5 * crossWidth);
        Point hotSpot = new Point(offset, offset);
        return toolkit.createCustomCursor(coursorImage, hotSpot, name);
    }

    public static JButton createIconButton(Icon icon, String toolTip) {
        JButton button = new JButton();
        decorateButton(button, icon, toolTip);
        return button;
    }

    public static JToggleButton createIconToggleButton(Icon icon, String toolTip) {
        JToggleButton result = new JToggleButton(icon);
        result.setToolTipText(toolTip);
        result.setMargin(new Insets(0, 0, 0, 0));
        int iconSize = SizeHelper.getToolIconSize();
        Insets insets = result.getInsets();
        int minSize = iconSize + Math.max(insets.left + insets.right, insets.top + insets.bottom);
        result.setPreferredSize(new Dimension(minSize, minSize));
        return result;
    }

    public static void decorateButton(AbstractButton button, Icon icon, String toolTip) {
        button.setIcon(icon);
        button.setToolTipText(toolTip);
        button.setMargin(new Insets(0, 0, 0, 0));
        int iconSize = SizeHelper.getToolIconSize();
        Insets insets = button.getInsets();
        int minSize = iconSize + Math.max(insets.left + insets.right, insets.top + insets.bottom);
        button.setPreferredSize(new Dimension(minSize, minSize));
    }

    public static JButton createDialogButton(String text) {
        JButton result = new JButton(text);
        Dimension dimension = result.getPreferredSize();
        int w = Math.max(dimension.width, BUTTON_PREFERED_WIDTH);
        int h = Math.max(dimension.height, BUTTON_PREFERED_HEIGHT);
        result.setPreferredSize(new Dimension(w, h));
        return result;
    }

    public static BorderLayout createBorderLayout() {
        return new BorderLayout(SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap());
    }

    public static TableLayout createTableLayout(double[] columnSizes, double[] rowSizes) {
        TableLayout result = new TableLayout(new double[][]{columnSizes, rowSizes});
        result.setHGap(SizeHelper.getLayoutHGap());
        result.setVGap(SizeHelper.getLayoutVGap());
        return result;
    }

    public static void setButtonPanelLayout(JPanel panel, Dimension buttonSize) {
        FlowLayout layout = new FlowLayout();
        panel.setLayout(layout);
        int hGap = layout.getHgap();
        int vGap = layout.getVgap();
        int buttonWidth = (int) Math.round(buttonSize.getWidth() + hGap);
        int buttonHeight = (int) Math.round(buttonSize.getHeight() + vGap);
        int buttonCount = panel.getComponentCount();
        Dimension panelSize = new Dimension(buttonWidth * buttonCount + hGap, buttonHeight + vGap);
        panel.setPreferredSize(panelSize);
        panel.setMaximumSize(panelSize);
    }

}
