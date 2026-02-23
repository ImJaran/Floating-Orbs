package com.floatingorbs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import javax.inject.Inject;
import net.runelite.client.input.MouseListener;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class FloatingOrbsQuickPrayerOverlay extends Overlay implements MouseListener
{
    private static final int BASE_BUTTON_HEIGHT = 28;
    private static final int BASE_ICON_BUTTON_SIZE = 56;
    private static final int BASE_ROUND_MIN_DIAMETER = 56;

    private final FloatingOrbsPlugin plugin;

    @Inject
    private FloatingOrbsQuickPrayerOverlay(FloatingOrbsPlugin plugin)
    {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setMovable(true);
        setSnappable(true);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.shouldRenderOrbs())
        {
            return null;
        }

        plugin.syncSnappedSpecialToPrayer();

        final boolean enabled = plugin.isPrayerOrbActive();
        final int prayerPoints = plugin.getCurrentPrayerPoints();
        final float prayerRatio = Math.max(0f, Math.min(1f, plugin.getPrayerPercent() / 100f));
        final boolean roundMode = plugin.isPrayerRoundButton();
        final PrayerPointsPosition prayerPos = plugin.getPrayerPointsPosition();
        final boolean showPrayerPoints = prayerPos != PrayerPointsPosition.HIDDEN;
        final boolean stacked = prayerPos == PrayerPointsPosition.ABOVE_TEXT || prayerPos == PrayerPointsPosition.BELOW_TEXT;
        final boolean stackedLayout = roundMode ? showPrayerPoints : stacked;
        final boolean cornerMode = !roundMode && showPrayerPoints && !stacked;

        final String fallbackText = "Prayer";
        final String pointsText = Integer.toString(prayerPoints);
        final BufferedImage orbIcon = plugin.getPrayerOrbIconImage();

        final double overallScale = plugin.getPrayerButtonScale() / 100.0;
        final double widthScale = plugin.getPrayerButtonWidthScale() / 100.0;
        final double heightScale = plugin.getPrayerButtonHeightScale() / 100.0;

        final int arcSize = Math.max(8, (int) Math.round(10 * overallScale));
        final int lineGap = Math.max(1, (int) Math.round(2 * overallScale * heightScale));
        final int cornerMargin = Math.max(1, (int) Math.round(overallScale));
        final float cornerTextScale = 0.60f;

        final Font originalFont = graphics.getFont();
        float topFontSize = Math.max(11f, (float) (originalFont.getSize2D() * overallScale));
        float pointsFontSize = Math.max(10f, topFontSize - 1f);
        Font topFont = originalFont.deriveFont(topFontSize);
        Font pointsFont = originalFont.deriveFont(pointsFontSize);

        graphics.setFont(topFont);
        FontMetrics topFm = graphics.getFontMetrics();

        graphics.setFont(pointsFont);
        FontMetrics pointsFm = graphics.getFontMetrics();

        int width = Math.max(20, (int) Math.round(BASE_ICON_BUTTON_SIZE * overallScale * widthScale));
        int buttonHeight = Math.max(20, (int) Math.round(BASE_ICON_BUTTON_SIZE * overallScale * heightScale));

        if (roundMode)
        {
            final double roundScale = overallScale * Math.max(widthScale, heightScale);
            final int minRoundDiameter = Math.max(20, (int) Math.round(BASE_ROUND_MIN_DIAMETER * roundScale));
            final int diameter = Math.max(Math.max(width, buttonHeight), minRoundDiameter);
            width = diameter;
            buttonHeight = diameter;
        }

        int iconSize = Math.max(12, (int) Math.round(Math.min(width, buttonHeight) * (showPrayerPoints ? 0.42 : 0.56)));
        int iconX = (width - iconSize) / 2;
        int iconY = (buttonHeight - iconSize) / 2;

        if (roundMode)
        {
            final int maxTextWidth = Math.max(10, (int) Math.round(width * 0.76));
            final int maxTextHeight = Math.max(10, (int) Math.round(buttonHeight * 0.76));

            if (showPrayerPoints && stackedLayout)
            {
                while (topFontSize > 8f || pointsFontSize > 7f)
                {
                    topFont = originalFont.deriveFont(topFontSize);
                    pointsFont = originalFont.deriveFont(pointsFontSize);
                    graphics.setFont(topFont);
                    topFm = graphics.getFontMetrics();
                    graphics.setFont(pointsFont);
                    pointsFm = graphics.getFontMetrics();

                    final int maxLineWidth = Math.max(iconSize, pointsFm.stringWidth(pointsText));
                    final int totalHeight = topFm.getHeight() + lineGap + pointsFm.getHeight();
                    if (maxLineWidth <= maxTextWidth && totalHeight <= maxTextHeight)
                    {
                        break;
                    }

                    if (topFontSize > 8f)
                    {
                        topFontSize -= 0.5f;
                    }
                    if (pointsFontSize > 7f)
                    {
                        pointsFontSize -= 0.5f;
                    }
                }
            }
            else
            {
                pointsFont = originalFont.deriveFont(pointsFontSize);
                graphics.setFont(pointsFont);
                pointsFm = graphics.getFontMetrics();
            }
        }

        final boolean blinkAllowedByState = enabled || !plugin.prayerBlinkWhenOff();
        final boolean blinkFrame = blinkAllowedByState
            && plugin.shouldBlinkLowPrayer()
            && (System.currentTimeMillis() / plugin.getPrayerBlinkIntervalMs()) % 2 == 0;
        final Color fillColor = blinkFrame ? plugin.getPrayerColorBlink() : (enabled ? plugin.getPrayerColorOn() : plugin.getPrayerColorOff());
        final Color emptyColor = darkenColor(fillColor, 0.38f);
        final Shape buttonShape = roundMode
            ? new Ellipse2D.Float(0, 0, width, buttonHeight)
            : new RoundRectangle2D.Float(0, 0, width, buttonHeight, arcSize, arcSize);

        graphics.setColor(emptyColor);
        graphics.fill(buttonShape);

        if (prayerRatio > 0f)
        {
            final int fillTop = Math.max(0, Math.min(buttonHeight, (int) Math.round(buttonHeight * (1.0 - prayerRatio))));
            final Shape oldClip = graphics.getClip();
            graphics.setClip(buttonShape);
            graphics.setColor(fillColor);
            graphics.fillRect(0, fillTop, width, buttonHeight - fillTop);
            graphics.setClip(oldClip);
        }

        graphics.setColor(Color.BLACK);
        if (roundMode)
        {
            graphics.drawOval(0, 0, width, buttonHeight);
        }
        else
        {
            graphics.drawRoundRect(0, 0, width, buttonHeight, arcSize, arcSize);
        }

        final Color textColor = blinkFrame ? Color.BLACK : Color.WHITE;
        graphics.setColor(textColor);

        if (orbIcon != null)
        {
            graphics.drawImage(orbIcon, iconX, iconY, iconSize, iconSize, null);
        }
        else
        {
            graphics.setFont(topFont);
            final int fallbackY = ((buttonHeight - topFm.getHeight()) / 2) + topFm.getAscent();
            final int fallbackX = (width - topFm.stringWidth(fallbackText)) / 2;
            graphics.drawString(fallbackText, fallbackX, fallbackY);
        }

        if (showPrayerPoints && stackedLayout)
        {
            graphics.setFont(pointsFont);
            final int pointsWidth = pointsFm.stringWidth(pointsText);
            final int pointsX = (width - pointsWidth) / 2;
            if (prayerPos == PrayerPointsPosition.ABOVE_TEXT)
            {
                final int aboveY = Math.max(cornerMargin + pointsFm.getAscent(), iconY - Math.max(1, lineGap));
                graphics.drawString(pointsText, pointsX, aboveY);
            }
            else
            {
                final int belowY = Math.min(
                    buttonHeight - cornerMargin - pointsFm.getDescent(),
                    iconY + iconSize + pointsFm.getAscent() + Math.max(1, lineGap - 1)
                );
                graphics.drawString(pointsText, pointsX, belowY);
            }
        }
        else if (showPrayerPoints)
        {
            graphics.setFont(pointsFont);
            final int pointsWidth = cornerMode
                ? Math.max(1, Math.round(pointsFm.stringWidth(pointsText) * cornerTextScale))
                : pointsFm.stringWidth(pointsText);
            final int ascent = cornerMode
                ? Math.max(1, Math.round(pointsFm.getAscent() * cornerTextScale))
                : pointsFm.getAscent();
            final int descent = cornerMode
                ? Math.max(1, Math.round(pointsFm.getDescent() * cornerTextScale))
                : pointsFm.getDescent();
            final int topYPoints = cornerMargin + ascent;
            final int bottomYPoints = buttonHeight - cornerMargin - descent;
            final int leftX = cornerMargin;
            final int rightX = width - cornerMargin - pointsWidth;

            switch (prayerPos)
            {
                case TOP_LEFT:
                    if (cornerMode)
                    {
                        drawScaledText(graphics, pointsText, leftX, topYPoints, cornerTextScale);
                    }
                    else
                    {
                        graphics.drawString(pointsText, leftX, topYPoints);
                    }
                    break;
                case TOP_RIGHT:
                    if (cornerMode)
                    {
                        drawScaledText(graphics, pointsText, rightX, topYPoints, cornerTextScale);
                    }
                    else
                    {
                        graphics.drawString(pointsText, rightX, topYPoints);
                    }
                    break;
                case BOTTOM_LEFT:
                    if (cornerMode)
                    {
                        drawScaledText(graphics, pointsText, leftX, bottomYPoints, cornerTextScale);
                    }
                    else
                    {
                        graphics.drawString(pointsText, leftX, bottomYPoints);
                    }
                    break;
                case BOTTOM_RIGHT:
                    if (cornerMode)
                    {
                        drawScaledText(graphics, pointsText, rightX, bottomYPoints, cornerTextScale);
                    }
                    else
                    {
                        graphics.drawString(pointsText, rightX, bottomYPoints);
                    }
                    break;
                default:
                    break;
            }
        }

        graphics.setFont(originalFont);
        return new Dimension(width, buttonHeight);
    }

    @Override
    public MouseEvent mouseClicked(MouseEvent event)
    {
        return event;
    }

    @Override
    public MouseEvent mousePressed(MouseEvent event)
    {
        if (event.getButton() != MouseEvent.BUTTON1)
        {
            return event;
        }

        final Rectangle bounds = getBounds();
        if (bounds != null && bounds.contains(event.getPoint()))
        {
            plugin.toggleQuickPrayers();
            event.consume();
        }

        return event;
    }

    @Override
    public MouseEvent mouseReleased(MouseEvent event)
    {
        return event;
    }

    @Override
    public MouseEvent mouseEntered(MouseEvent event)
    {
        return event;
    }

    @Override
    public MouseEvent mouseExited(MouseEvent event)
    {
        return event;
    }

    @Override
    public MouseEvent mouseDragged(MouseEvent event)
    {
        return event;
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent event)
    {
        return event;
    }

    private void drawScaledText(Graphics2D graphics, String text, int x, int y, float scale)
    {
        final AffineTransform oldTransform = graphics.getTransform();
        graphics.translate(x, y);
        graphics.scale(scale, scale);
        graphics.drawString(text, 0, 0);
        graphics.setTransform(oldTransform);
    }

    private Color darkenColor(Color color, float factor)
    {
        final float clampedFactor = Math.max(0f, Math.min(1f, factor));
        return new Color(
            Math.max(0, Math.min(255, Math.round(color.getRed() * clampedFactor))),
            Math.max(0, Math.min(255, Math.round(color.getGreen() * clampedFactor))),
            Math.max(0, Math.min(255, Math.round(color.getBlue() * clampedFactor))),
            color.getAlpha()
        );
    }
}
