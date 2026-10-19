package com.utegear.dto.catalog;

import java.util.List;

public final class SwitchSoundDtos {

    private SwitchSoundDtos() {
    }

    public record SynthProfile(
            double baseFrequency,
            double clickFrequency,
            double clickGain,
            double decayMs,
            double thockFactor) {
    }

    public record SwitchProfile(
            String code,
            String name,
            String switchType,
            int actuationForceG,
            double actuationPointMm,
            double totalTravelMm,
            String noiseLevel,
            String soundCharacter,
            String recordingCondition,
            String audioSecureUrl,
            String audioPublicId,
            String colorHex,
            SynthProfile synthProfile,
            List<Long> matchedVariantIds) {

        public SwitchProfile withMatchedVariantIds(List<Long> variantIds) {
            return new SwitchProfile(
                    code,
                    name,
                    switchType,
                    actuationForceG,
                    actuationPointMm,
                    totalTravelMm,
                    noiseLevel,
                    soundCharacter,
                    recordingCondition,
                    audioSecureUrl,
                    audioPublicId,
                    colorHex,
                    synthProfile,
                    variantIds == null ? List.of() : List.copyOf(variantIds));
        }
    }

    public record KeyboardAppearance(
            String layoutType,
            boolean hasKnob,
            String caseColor,
            String alphaColor,
            String alphaTextColor,
            String modColor,
            String modTextColor,
            String accentColor,
            String accentTextColor,
            String layoutDisplayName) {
    }

    public record ProductSwitchSoundResponse(
            Long productId,
            String productName,
            String categorySlug,
            boolean supported,
            String defaultSwitchCode,
            String disclaimer,
            List<SwitchProfile> profiles,
            String layoutType,
            String themeColor,
            KeyboardAppearance appearance) {

        public ProductSwitchSoundResponse(
                Long productId,
                String productName,
                String categorySlug,
                boolean supported,
                String defaultSwitchCode,
                String disclaimer,
                List<SwitchProfile> profiles,
                String layoutType,
                String themeColor) {
            this(productId, productName, categorySlug, supported, defaultSwitchCode, disclaimer, profiles,
                    layoutType, themeColor, null);
        }

        public ProductSwitchSoundResponse(
                Long productId,
                String productName,
                String categorySlug,
                boolean supported,
                String defaultSwitchCode,
                String disclaimer,
                List<SwitchProfile> profiles) {
            this(productId, productName, categorySlug, supported, defaultSwitchCode, disclaimer, profiles,
                    "TKL", "DARK", null);
        }
    }
}
