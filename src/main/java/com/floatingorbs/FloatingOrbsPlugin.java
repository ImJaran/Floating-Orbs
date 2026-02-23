package com.floatingorbs;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
    name = "Floating Orbs",
    description = "Movable prayer and special attack orb buttons",
    tags = {"quick prayers", "special attack", "overlay", "prayer"}
)
public class FloatingOrbsPlugin extends Plugin
{
    private static final String CONFIG_GROUP = "floatingorbs";
    private static final String QUICK_PRAYER_TARGET = "<col=ff9040>Quick-prayers</col>";
    private static final String SPECIAL_ATTACK_TARGET = "<col=00ff00>Special Attack</col>";
    private static final int SPECIAL_ATTACK_BUTTON_WIDGET_ID = InterfaceID.Orbs.SPECBUTTON;
    private static final int PRAYER_ICON_WIDGET_ID = InterfaceID.Orbs.PRAYER_ICON;
    private static final int SPECIAL_ICON_WIDGET_ID = InterfaceID.Orbs.SPECENERGY_ICON;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private MouseManager mouseManager;

    @Inject
    private SpriteManager spriteManager;

    @Inject
    private FloatingOrbsQuickPrayerOverlay prayerOverlay;

    @Inject
    private FloatingOrbsSpecialAttackOverlay specialOverlay;

    @Inject
    private FloatingOrbsConfig config;

    private boolean prayerOverlayEnabled;
    private boolean specialOverlayEnabled;
    private Point lastPrayerLocation;
    private Point lastSpecialLocation;
    private boolean syncingSnappedOrbs;

    @Provides
    FloatingOrbsConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(FloatingOrbsConfig.class);
    }

    @Override
    protected void startUp()
    {
        updateOverlayState();
        log.info("FloatingOrbsPlugin started");
    }

    @Override
    protected void shutDown()
    {
        disablePrayerOverlay();
        disableSpecialOverlay();
        log.info("FloatingOrbsPlugin stopped");
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (CONFIG_GROUP.equals(event.getGroup()))
        {
            updateOverlayState();
        }
    }

    void toggleQuickPrayers()
    {
        clientThread.invoke(() ->
        {
            final boolean enabled = isQuickPrayersEnabled();
            final String option = enabled ? "Deactivate" : "Activate";
            clickOrb(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB, QUICK_PRAYER_TARGET, MenuAction.CC_OP, option, "Activate", "Deactivate");
        });
    }

    void toggleSpecialAttack()
    {
        clientThread.invoke(() ->
        {
            Widget specButton = client.getWidget(SPECIAL_ATTACK_BUTTON_WIDGET_ID);
            if (specButton == null)
            {
                specButton = client.getWidget(WidgetInfo.MINIMAP_SPEC_ORB);
            }

            if (specButton == null)
            {
                return;
            }
            clickWidgetAction(
                specButton,
                specButton.getId(),
                SPECIAL_ATTACK_TARGET,
                MenuAction.CC_OP,
                "Use Special Attack",
                "Use",
                "Activate",
                "Deactivate"
            );
        });
    }

    private void clickOrb(WidgetInfo widgetInfo, String fallbackTarget, MenuAction actionType, String preferredOption, String... fallbacks)
    {
        final Widget orb = client.getWidget(widgetInfo);
        if (orb == null)
        {
            return;
        }

        clickWidgetAction(orb, widgetInfo.getPackedId(), fallbackTarget, actionType, preferredOption, fallbacks);
    }

    private void clickWidgetAction(Widget widget, int widgetId, String fallbackTarget, MenuAction actionType, String preferredOption, String... fallbacks)
    {
        int opIndex = 1;
        String option = preferredOption;
        final String[] actions = widget.getActions();
        if (actions != null)
        {
            final String[] candidates = new String[1 + fallbacks.length];
            candidates[0] = preferredOption;
            System.arraycopy(fallbacks, 0, candidates, 1, fallbacks.length);

            boolean found = false;
            for (String candidate : candidates)
            {
                if (candidate == null)
                {
                    continue;
                }

                for (int i = 0; i < actions.length; i++)
                {
                    final String action = actions[i];
                    if (action != null && action.equalsIgnoreCase(candidate))
                    {
                        opIndex = i + 1;
                        option = action;
                        found = true;
                        break;
                    }
                }

                if (found)
                {
                    break;
                }
            }
        }

        final String target = widget.getName() != null && !widget.getName().isEmpty()
            ? widget.getName()
            : fallbackTarget;

        client.menuAction(
            -1,
            widgetId,
            actionType,
            opIndex,
            -1,
            option,
            target
        );
    }

    boolean isQuickPrayersEnabled()
    {
        return client.getVarbitValue(Varbits.QUICK_PRAYER) == 1;
    }

    boolean isPrayerOrbActive()
    {
        if (isQuickPrayersEnabled())
        {
            return true;
        }

        for (Prayer prayer : Prayer.values())
        {
            if (client.isPrayerActive(prayer))
            {
                return true;
            }
        }

        return false;
    }

    BufferedImage getPrayerOrbIconImage()
    {
        return getOrbIconImage(PRAYER_ICON_WIDGET_ID, WidgetInfo.MINIMAP_PRAYER_ORB);
    }

    int getCurrentPrayerPoints()
    {
        return client.getBoostedSkillLevel(Skill.PRAYER);
    }

    int getPrayerPercent()
    {
        final int maxPrayer = Math.max(1, client.getRealSkillLevel(Skill.PRAYER));
        final int currentPrayer = Math.max(0, Math.min(maxPrayer, getCurrentPrayerPoints()));
        return (int) Math.round((currentPrayer * 100.0) / maxPrayer);
    }

    int getPrayerButtonScale()
    {
        return config.buttonScale();
    }

    boolean isPrayerRoundButton()
    {
        return config.prayerRoundButton();
    }

    int getPrayerButtonWidthScale()
    {
        return config.buttonWidthScale();
    }

    int getPrayerButtonHeightScale()
    {
        return config.buttonHeightScale();
    }

    Color getPrayerColorOn()
    {
        return config.buttonColorOn();
    }

    Color getPrayerColorOff()
    {
        return config.buttonColorOff();
    }

    Color getPrayerColorBlink()
    {
        return config.buttonColorBlink();
    }

    PrayerPointsPosition getPrayerPointsPosition()
    {
        if (!config.prayerRoundButton())
        {
            return config.prayerPointsPosition();
        }

        return toPrayerPointsPosition(config.prayerRoundPointsPosition());
    }

    int getPrayerBlinkIntervalMs()
    {
        return config.blinkIntervalMs();
    }

    boolean prayerBlinkWhenOff()
    {
        return config.blinkWhenOff();
    }

    boolean shouldBlinkLowPrayer()
    {
        return config.blinkWhenLowPrayer() && getCurrentPrayerPoints() <= config.lowPrayerThreshold();
    }

    boolean isSpecialAttackEnabled()
    {
        return client.getVarpValue(VarPlayer.SPECIAL_ATTACK_ENABLED) == 1;
    }

    BufferedImage getSpecialOrbIconImage()
    {
        return getOrbIconImage(SPECIAL_ICON_WIDGET_ID, WidgetInfo.MINIMAP_SPEC_ORB);
    }

    int getSpecialAttackPercent()
    {
        final int raw = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT);
        final int normalized = raw > 100 ? raw / 10 : raw;
        return Math.max(0, Math.min(100, normalized));
    }

    int getSpecialButtonScale()
    {
        return config.specialButtonScale();
    }

    boolean isSpecialRoundButton()
    {
        return config.specialRoundButton();
    }

    int getSpecialButtonWidthScale()
    {
        return config.specialButtonWidthScale();
    }

    int getSpecialButtonHeightScale()
    {
        return config.specialButtonHeightScale();
    }

    Color getSpecialColorOn()
    {
        return config.specialButtonColorOn();
    }

    Color getSpecialColorOff()
    {
        return config.specialButtonColorOff();
    }

    Color getSpecialColorBlink()
    {
        return config.specialButtonColorBlink();
    }

    PrayerPointsPosition getSpecialPointsPosition()
    {
        if (!config.specialRoundButton())
        {
            return config.specialPointsPosition();
        }

        return toPrayerPointsPosition(config.specialRoundPointsPosition());
    }

    int getSpecialBlinkIntervalMs()
    {
        return config.specialBlinkIntervalMs();
    }

    boolean specialBlinkWhenFull()
    {
        return config.specialBlinkWhenFull();
    }

    boolean shouldBlinkLowSpecial()
    {
        return config.specialBlinkWhenLow() && getSpecialAttackPercent() >= config.specialLowThreshold();
    }

    boolean shouldRenderOrbs()
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return false;
        }

        return !hasBlockingInterface();
    }

    boolean snapSpecialToPrayer()
    {
        return config.snapSpecialToPrayer();
    }

    void syncSnappedSpecialToPrayer()
    {
        if (!config.snapSpecialToPrayer())
        {
            lastPrayerLocation = null;
            lastSpecialLocation = null;
            return;
        }

        final Point prayerPoint = getOverlayLocation(prayerOverlay);
        final Point specialPoint = getOverlayLocation(specialOverlay);
        final Rectangle prayerBounds = prayerOverlay.getBounds();
        final Rectangle specialBounds = specialOverlay.getBounds();
        if (prayerPoint == null || specialPoint == null || prayerBounds == null || specialBounds == null)
        {
            return;
        }

        if (syncingSnappedOrbs)
        {
            lastPrayerLocation = new Point(prayerPoint);
            lastSpecialLocation = new Point(specialPoint);
            return;
        }

        final int gap = config.snapGapPx();
        final boolean prayerMoved = lastPrayerLocation != null && !prayerPoint.equals(lastPrayerLocation);
        final boolean specialMoved = lastSpecialLocation != null && !specialPoint.equals(lastSpecialLocation);
        final Point desiredSpecialFromPrayer = new Point(prayerPoint.x + prayerBounds.width + gap, prayerPoint.y);

        Point newPrayer = null;
        Point newSpecial = null;

        if (prayerMoved && !specialMoved)
        {
            newSpecial = desiredSpecialFromPrayer;
        }
        else if (specialMoved && !prayerMoved)
        {
            newPrayer = new Point(specialPoint.x - prayerBounds.width - gap, specialPoint.y);
        }
        else if (!prayerMoved && !specialMoved && lastPrayerLocation == null && lastSpecialLocation == null)
        {
            newSpecial = desiredSpecialFromPrayer;
        }
        else if (!prayerMoved && !specialMoved && !specialPoint.equals(desiredSpecialFromPrayer))
        {
            // Keep snapped layout stable after startup/layout passes.
            newSpecial = desiredSpecialFromPrayer;
        }

        if (newPrayer != null || newSpecial != null)
        {
            syncingSnappedOrbs = true;
            if (newPrayer != null)
            {
                prayerOverlay.setPreferredLocation(newPrayer);
            }
            if (newSpecial != null)
            {
                specialOverlay.setPreferredLocation(newSpecial);
            }
            syncingSnappedOrbs = false;

            final Point updatedPrayer = getOverlayLocation(prayerOverlay);
            final Point updatedSpecial = getOverlayLocation(specialOverlay);
            lastPrayerLocation = updatedPrayer != null ? new Point(updatedPrayer) : null;
            lastSpecialLocation = updatedSpecial != null ? new Point(updatedSpecial) : null;
            return;
        }

        lastPrayerLocation = new Point(prayerPoint);
        lastSpecialLocation = new Point(specialPoint);
    }

    private BufferedImage getOrbIconImage(int iconWidgetId, WidgetInfo fallbackWidgetInfo)
    {
        int spriteId = -1;

        final Widget iconWidget = client.getWidget(iconWidgetId);
        if (iconWidget != null)
        {
            spriteId = iconWidget.getSpriteId();
        }

        if (spriteId < 0 && fallbackWidgetInfo != null)
        {
            final Widget fallbackWidget = client.getWidget(fallbackWidgetInfo);
            if (fallbackWidget != null)
            {
                spriteId = fallbackWidget.getSpriteId();
            }
        }

        if (spriteId < 0)
        {
            return null;
        }

        return spriteManager.getSprite(spriteId, 0);
    }

    private PrayerPointsPosition toPrayerPointsPosition(RoundPointsPosition roundPosition)
    {
        if (roundPosition == null)
        {
            return PrayerPointsPosition.HIDDEN;
        }

        switch (roundPosition)
        {
            case ABOVE_TEXT:
                return PrayerPointsPosition.ABOVE_TEXT;
            case BELOW_TEXT:
                return PrayerPointsPosition.BELOW_TEXT;
            case HIDDEN:
            default:
                return PrayerPointsPosition.HIDDEN;
        }
    }

    private boolean hasBlockingInterface()
    {
        return isWidgetVisible(WidgetInfo.BANK_CONTAINER)
            || isWidgetVisible(WidgetInfo.BANK_PIN_CONTAINER)
            || isWidgetVisible(WidgetInfo.GRAND_EXCHANGE_WINDOW_CONTAINER)
            || isWidgetVisible(WidgetInfo.QUESTLIST_CONTAINER)
            || isWidgetVisible(WidgetInfo.COLLECTION_LOG)
            || isWidgetVisible(WidgetInfo.ACHIEVEMENT_DIARY_CONTAINER)
            || isWidgetVisible(WidgetInfo.WORLD_MAP_VIEW)
            || isWidgetVisible(WidgetInfo.DIALOG_OPTION)
            || isWidgetVisible(WidgetInfo.DIALOG_NPC_TEXT)
            || isWidgetVisible(WidgetInfo.DIALOG_PLAYER_TEXT)
            || isWidgetVisible(WidgetInfo.DIALOG_SPRITE)
            || isWidgetVisible(WidgetInfo.DESTROY_ITEM)
            || isWidgetVisible(WidgetInfo.LEVEL_UP)
            || isWidgetVisible(WidgetInfo.QUEST_COMPLETED)
            || hasVisibleChildren(WidgetInfo.FIXED_VIEWPORT_BANK_CONTAINER)
            || hasVisibleChildren(WidgetInfo.FIXED_VIEWPORT_INTERFACE_CONTAINER)
            || hasVisibleChildren(WidgetInfo.RESIZABLE_VIEWPORT_INTERFACE_CONTAINER)
            || hasVisibleChildren(WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_INTERFACE_CONTAINER);
    }

    private boolean isWidgetVisible(WidgetInfo widgetInfo)
    {
        final Widget widget = client.getWidget(widgetInfo);
        return widget != null && !widget.isHidden();
    }

    private boolean hasVisibleChildren(WidgetInfo containerInfo)
    {
        final Widget container = client.getWidget(containerInfo);
        if (container == null || container.isHidden())
        {
            return false;
        }

        final Widget[] children = container.getChildren();
        if (children == null)
        {
            return false;
        }

        for (Widget child : children)
        {
            if (child != null && !child.isHidden())
            {
                return true;
            }
        }

        return false;
    }

    private Point getOverlayLocation(net.runelite.client.ui.overlay.Overlay overlay)
    {
        if (overlay == null)
        {
            return null;
        }

        final Point preferred = overlay.getPreferredLocation();
        if (preferred != null)
        {
            return new Point(preferred);
        }

        final Rectangle bounds = overlay.getBounds();
        if (bounds != null)
        {
            return bounds.getLocation();
        }

        return null;
    }

    private void updateOverlayState()
    {
        if (config.showQuickPrayerButton())
        {
            enablePrayerOverlay();
        }
        else
        {
            disablePrayerOverlay();
        }

        if (config.showSpecialAttackButton())
        {
            enableSpecialOverlay();
        }
        else
        {
            disableSpecialOverlay();
        }
    }

    private void enablePrayerOverlay()
    {
        if (prayerOverlayEnabled)
        {
            return;
        }

        overlayManager.add(prayerOverlay);
        mouseManager.registerMouseListener(prayerOverlay);
        prayerOverlayEnabled = true;
    }

    private void disablePrayerOverlay()
    {
        if (!prayerOverlayEnabled)
        {
            return;
        }

        mouseManager.unregisterMouseListener(prayerOverlay);
        overlayManager.remove(prayerOverlay);
        prayerOverlayEnabled = false;
    }

    private void enableSpecialOverlay()
    {
        if (specialOverlayEnabled)
        {
            return;
        }

        overlayManager.add(specialOverlay);
        mouseManager.registerMouseListener(specialOverlay);
        specialOverlayEnabled = true;
    }

    private void disableSpecialOverlay()
    {
        if (!specialOverlayEnabled)
        {
            return;
        }

        mouseManager.unregisterMouseListener(specialOverlay);
        overlayManager.remove(specialOverlay);
        specialOverlayEnabled = false;
    }
}
