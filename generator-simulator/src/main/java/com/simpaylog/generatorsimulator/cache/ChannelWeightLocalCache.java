package com.simpaylog.generatorsimulator.cache;

import com.simpaylog.generatorcore.dto.CategoryType;
import com.simpaylog.generatorcore.enums.ChannelType;
import com.simpaylog.generatorcore.enums.PreferenceType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Component
public class ChannelWeightLocalCache {
    private ChannelWeightLocalCache(){}
    private static final Map<CategoryType, Map<ChannelType, Double>> BASE_WEIGHTS = new EnumMap<>(CategoryType.class);

    static {
        BASE_WEIGHTS.put(CategoryType.GROCERIES_NON_ALCOHOLIC_BEVERAGES, Map.of(
                ChannelType.CARD, 0.9,
                ChannelType.TRANSFER, 0.05,
                ChannelType.ATM, 0.05
        ));
        BASE_WEIGHTS.put(CategoryType.ALCOHOLIC_BEVERAGES_TOBACCO, Map.of(
                ChannelType.CARD, 0.85,
                ChannelType.ATM, 0.15
        ));
        BASE_WEIGHTS.put(CategoryType.HOUSING_UTILITIES_FUEL, Map.of(
                ChannelType.AUTO, 0.75,
                ChannelType.TRANSFER, 0.25
        ));
        BASE_WEIGHTS.put(CategoryType.COMMUNICATION, Map.of(
                ChannelType.AUTO, 0.85,
                ChannelType.TRANSFER, 0.15
        ));
        BASE_WEIGHTS.put(CategoryType.RECREATION_CULTURE, Map.of(
                ChannelType.CARD, 0.95,
                ChannelType.TRANSFER, 0.05
        ));
        BASE_WEIGHTS.put(CategoryType.EDUCATION, Map.of(
                ChannelType.CARD, 0.85,
                ChannelType.TRANSFER, 0.15
        ));
        BASE_WEIGHTS.put(CategoryType.FOOD_ACCOMMODATION, Map.of(
                ChannelType.CARD, 0.9,
                ChannelType.TRANSFER, 0.1
        ));
        BASE_WEIGHTS.put(CategoryType.OTHER_GOODS_SERVICES, Map.of(
                ChannelType.CARD, 0.85,
                ChannelType.TRANSFER, 0.15
        ));
    }

    private static final Map<PreferenceType, Map<ChannelType, Double>> PREFERENCE_MODIFIERS = new EnumMap<>(PreferenceType.class);

    static {
        PREFERENCE_MODIFIERS.put(PreferenceType.DEFAULT, Map.of(
                ChannelType.CARD, 0.0,
                ChannelType.AUTO, 0.0,
                ChannelType.ATM, 0.0
        ));
        PREFERENCE_MODIFIERS.put(PreferenceType.CONSUMPTION_ORIENTED, Map.of(
                ChannelType.CARD, 0.05,
                ChannelType.AUTO, -0.1,
                ChannelType.ATM, -0.05
        ));
        PREFERENCE_MODIFIERS.put(PreferenceType.SAVING_ORIENTED, Map.of(
                ChannelType.AUTO, 0.15,
                ChannelType.TRANSFER, 0.05,
                ChannelType.CARD, -0.05
        ));
        PREFERENCE_MODIFIERS.put(PreferenceType.UNPLANNED, Map.of(
                ChannelType.ATM, 0.05,
                ChannelType.AUTO, -0.05
        ));
        PREFERENCE_MODIFIERS.put(PreferenceType.INVESTMENT_ORIENTED, Map.of(
                ChannelType.TRANSFER, 0.1,
                ChannelType.CARD, -0.05
        ));
        PREFERENCE_MODIFIERS.put(PreferenceType.STABLE, Map.of(
                ChannelType.AUTO, 0.1,
                ChannelType.TRANSFER, 0.05
        ));
    }

    private static final Set<CategoryType> FORBID_ATM = Set.of(
            CategoryType.EDUCATION,
            CategoryType.RECREATION_CULTURE,
            CategoryType.CLOTHING_FOOTWEAR,
            CategoryType.HOUSEHOLD_GOODS_SERVICES,
            CategoryType.HEALTH,
            CategoryType.TRANSPORTATION,
            CategoryType.FOOD_ACCOMMODATION,
            CategoryType.OTHER_GOODS_SERVICES,
            CategoryType.GROCERIES_NON_ALCOHOLIC_BEVERAGES
    );
    private static final Map<ChannelType, Double> CHANNEL_CAPS = Map.of(
            ChannelType.ATM, 0.1,
            ChannelType.SYSTEM, 1.0,
            ChannelType.AUTO, 0.95
    );

    public static Map<ChannelType, Double> getBaseWeights(CategoryType category) {
        return BASE_WEIGHTS.getOrDefault(category, Map.of());
    }

    public static Map<ChannelType, Double> getPreferenceModifier(PreferenceType pref) {
        return PREFERENCE_MODIFIERS.getOrDefault(pref, Map.of());
    }

    public static boolean isAtmForbidden(CategoryType category) {
        return FORBID_ATM.contains(category);
    }

    public static double getChannelCap(ChannelType channel) {
        return CHANNEL_CAPS.getOrDefault(channel, 1.0);
    }

}
