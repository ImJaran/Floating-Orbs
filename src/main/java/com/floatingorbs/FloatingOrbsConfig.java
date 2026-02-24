package com.floatingorbs;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("floatingorbs")
public interface FloatingOrbsConfig extends Config
{
    @ConfigSection(
        name = "Prayer Orb",
        description = "Settings for the quick prayer orb",
        position = 0,
        closedByDefault = false
    )
    String prayerSection = "prayerSection";

    @ConfigSection(
        name = "Special Orb",
        description = "Settings for the special attack orb",
        position = 1,
        closedByDefault = false
    )
    String specialSection = "specialSection";

    @ConfigItem(
        keyName = "showQuickPrayerButton",
        name = "Show quick prayer orb",
        description = "Shows a movable quick prayer orb overlay",
        section = prayerSection,
        position = 0
    )
    default boolean showQuickPrayerButton()
    {
        return true;
    }

    @Range(min = 50, max = 100)
    @ConfigItem(
        keyName = "buttonScale",
        name = "Prayer overall size (%)",
        description = "Scales the whole prayer orb",
        section = prayerSection,
        position = 1
    )
    default int buttonScale()
    {
        return 100;
    }

    @ConfigItem(
        keyName = "prayerRoundButton",
        name = "Prayer round orb",
        description = "Renders prayer orb as round and keeps text centered",
        section = prayerSection,
        position = 2
    )
    default boolean prayerRoundButton()
    {
        return true;
    }

    @Range(min = 50, max = 100)
    @ConfigItem(
        keyName = "buttonWidthScale",
        name = "Prayer width scale (%)",
        description = "Scales prayer orb width (X-axis)",
        section = prayerSection,
        position = 3
    )
    default int buttonWidthScale()
    {
        return 100;
    }

    @Range(min = 50, max = 100)
    @ConfigItem(
        keyName = "buttonHeightScale",
        name = "Prayer height scale (%)",
        description = "Scales prayer orb height (Y-axis)",
        section = prayerSection,
        position = 4
    )
    default int buttonHeightScale()
    {
        return 100;
    }

    @Alpha
    @ConfigItem(
        keyName = "buttonColorOn",
        name = "Prayer color (ON)",
        description = "Background color when quick prayers are ON",
        section = prayerSection,
        position = 5
    )
    default Color buttonColorOn()
    {
        return new Color(72, 132, 235, 230);
    }

    @Alpha
    @ConfigItem(
        keyName = "buttonColorOff",
        name = "Prayer color (OFF)",
        description = "Background color when quick prayers are OFF",
        section = prayerSection,
        position = 6
    )
    default Color buttonColorOff()
    {
        return new Color(36, 57, 99, 220);
    }

    @Alpha
    @ConfigItem(
        keyName = "buttonColorBlink",
        name = "Prayer color (BLINK)",
        description = "Background color on blink frames",
        section = prayerSection,
        position = 7
    )
    default Color buttonColorBlink()
    {
        return new Color(126, 191, 255, 235);
    }

    @ConfigItem(
        keyName = "prayerPointsPosition",
        name = "Prayer points position (square)",
        description = "Position of prayer points text for square prayer orb",
        section = prayerSection,
        position = 10
    )
    default PrayerPointsPosition prayerPointsPosition()
    {
        return PrayerPointsPosition.HIDDEN;
    }

    @ConfigItem(
        keyName = "prayerRoundPointsPosition",
        name = "Prayer points position (round)",
        description = "Position of prayer points text for round prayer orb",
        section = prayerSection,
        position = 9
    )
    default RoundPointsPosition prayerRoundPointsPosition()
    {
        return RoundPointsPosition.HIDDEN;
    }

    @ConfigItem(
        keyName = "blinkWhenLowPrayer",
        name = "Prayer blink when low",
        description = "Allows prayer orb blinking when prayer points are low",
        section = prayerSection,
        position = 11
    )
    default boolean blinkWhenLowPrayer()
    {
        return false;
    }

    @ConfigItem(
        keyName = "blinkWhenOff",
        name = "Prayer blink when OFF",
        description = "Allows prayer orb blinking while quick prayers are OFF",
        section = prayerSection,
        position = 12
    )
    default boolean blinkWhenOff()
    {
        return true;
    }

    @Range(min = 100, max = 2000)
    @ConfigItem(
        keyName = "blinkIntervalMs",
        name = "Prayer blink interval (ms)",
        description = "Time between blink frames in milliseconds for prayer orb",
        section = prayerSection,
        position = 13
    )
    default int blinkIntervalMs()
    {
        return 350;
    }

    @Range(min = 1, max = 99)
    @ConfigItem(
        keyName = "lowPrayerThreshold",
        name = "Prayer low threshold",
        description = "Blinking starts at or below this prayer value",
        section = prayerSection,
        position = 14
    )
    default int lowPrayerThreshold()
    {
        return 15;
    }

    @ConfigItem(
        keyName = "showSpecialAttackButton",
        name = "Show special attack orb",
        description = "Shows a movable special attack orb overlay",
        section = specialSection,
        position = 0
    )
    default boolean showSpecialAttackButton()
    {
        return true;
    }

    @ConfigItem(
        keyName = "snapSpecialToPrayer",
        name = "Snap special to prayer",
        description = "Keeps special orb next to prayer orb and moves both together",
        section = specialSection,
        position = 1
    )
    default boolean snapSpecialToPrayer()
    {
        return true;
    }

    @Range(min = 0, max = 50)
    @ConfigItem(
        keyName = "snapGapPx",
        name = "Snap gap (px)",
        description = "Horizontal gap between prayer and special orb when snapped",
        section = specialSection,
        position = 2
    )
    default int snapGapPx()
    {
        return 6;
    }

    @Range(min = 50, max = 100)
    @ConfigItem(
        keyName = "specialButtonScale",
        name = "Special overall size (%)",
        description = "Scales the whole special orb",
        section = specialSection,
        position = 3
    )
    default int specialButtonScale()
    {
        return 100;
    }

    @ConfigItem(
        keyName = "specialRoundButton",
        name = "Special round orb",
        description = "Renders special orb as round and keeps text centered",
        section = specialSection,
        position = 4
    )
    default boolean specialRoundButton()
    {
        return true;
    }

    @Range(min = 50, max = 100)
    @ConfigItem(
        keyName = "specialButtonWidthScale",
        name = "Special width scale (%)",
        description = "Scales special orb width (X-axis)",
        section = specialSection,
        position = 5
    )
    default int specialButtonWidthScale()
    {
        return 100;
    }

    @Range(min = 50, max = 100)
    @ConfigItem(
        keyName = "specialButtonHeightScale",
        name = "Special height scale (%)",
        description = "Scales special orb height (Y-axis)",
        section = specialSection,
        position = 6
    )
    default int specialButtonHeightScale()
    {
        return 100;
    }

    @Alpha
    @ConfigItem(
        keyName = "specialButtonColorOn",
        name = "Special color (ON)",
        description = "Background color when special attack is ON",
        section = specialSection,
        position = 7
    )
    default Color specialButtonColorOn()
    {
        return new Color(224, 186, 67, 230);
    }

    @Alpha
    @ConfigItem(
        keyName = "specialButtonColorOff",
        name = "Special color (OFF)",
        description = "Background color when special attack is OFF",
        section = specialSection,
        position = 8
    )
    default Color specialButtonColorOff()
    {
        return new Color(108, 78, 36, 220);
    }

    @Alpha
    @ConfigItem(
        keyName = "specialButtonColorBlink",
        name = "Special color (BLINK)",
        description = "Background color on blink frames for special orb",
        section = specialSection,
        position = 9
    )
    default Color specialButtonColorBlink()
    {
        return new Color(245, 214, 113, 235);
    }

    @ConfigItem(
        keyName = "specialPointsPosition",
        name = "Special points position (square)",
        description = "Position of special points text for square special orb",
        section = specialSection,
        position = 12
    )
    default PrayerPointsPosition specialPointsPosition()
    {
        return PrayerPointsPosition.HIDDEN;
    }

    @ConfigItem(
        keyName = "specialRoundPointsPosition",
        name = "Special points position (round)",
        description = "Position of special points text for round special orb",
        section = specialSection,
        position = 11
    )
    default RoundPointsPosition specialRoundPointsPosition()
    {
        return RoundPointsPosition.HIDDEN;
    }

    @ConfigItem(
        keyName = "specialBlinkWhenLow",
        name = "Special blink when above threshold",
        description = "Allows special orb blinking when special energy is at or above threshold",
        section = specialSection,
        position = 13
    )
    default boolean specialBlinkWhenLow()
    {
        return false;
    }

    @ConfigItem(
        keyName = "specialBlinkWhenFull",
        name = "Special blink when full",
        description = "Allows special orb blinking when special energy is 100%",
        section = specialSection,
        position = 14
    )
    default boolean specialBlinkWhenFull()
    {
        return true;
    }

    @Range(min = 100, max = 2000)
    @ConfigItem(
        keyName = "specialBlinkIntervalMs",
        name = "Special blink interval (ms)",
        description = "Time between blink frames in milliseconds for special orb",
        section = specialSection,
        position = 15
    )
    default int specialBlinkIntervalMs()
    {
        return 350;
    }

    @Range(min = 0, max = 100)
    @ConfigItem(
        keyName = "specialLowThreshold",
        name = "Special high threshold",
        description = "Blinking starts at or above this special energy value",
        section = specialSection,
        position = 16
    )
    default int specialLowThreshold()
    {
        return 25;
    }
}


