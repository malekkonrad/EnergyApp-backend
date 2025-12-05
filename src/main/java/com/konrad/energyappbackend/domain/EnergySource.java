package com.konrad.energyappbackend.domain;

import com.konrad.energyappbackend.client.dto.FuelMix;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Energy sources as defined by the Carbon Intensity API.
 *
 * <p>Each source is categorized as either clean (renewable + nuclear) or non-clean (fossil fuels).
 * This classification is used to calculate the clean energy percentage in the UK grid mix.
 *
 * <h3>Clean Energy Sources:</h3>
 * <ul>
 *   <li>Biomass</li>
 *   <li>Nuclear</li>
 *   <li>Hydro</li>
 *   <li>Wind</li>
 *   <li>Solar</li>
 * </ul>
 *
 * <h3>Non-Clean Energy Sources:</h3>
 * <ul>
 *   <li>Coal</li>
 *   <li>Gas</li>
 *   <li>Imports</li>
 *   <li>Other</li>
 * </ul>
 *
 * @see <a href="https://carbonintensity.github.io/api-definitions/">Carbon Intensity API Documentation</a>
 */
@Getter
public enum EnergySource {
    // Clean energy sources
    BIOMASS(true),
    NUCLEAR(true),
    HYDRO(true),
    WIND(true),
    SOLAR(true),

    // Non-clean energy sources
    COAL(false),
    GAS(false),
    IMPORTS(false),
    OTHER(false);

    private final boolean isClean;

    EnergySource(boolean isClean) {
        this.isClean = isClean;
    }

    /**
     * Parses fuel name from API response to EnergySource enum.
     *
     * <p>Matching is case-insensitive. Leading/trailing whitespace is ignored.
     *
     * @param fuelName name from API (e.g., "biomass", "COAL", "Wind")
     * @return corresponding EnergySource or null if not recognized
     */
    public static EnergySource fromFuelName(String fuelName) {
        if (fuelName == null || fuelName.isBlank()) {
            return null;
        }

        try {
            return valueOf(fuelName.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Checks if given fuel name represents a clean energy source.
     *
     * @param fuelName name from API (case-insensitive)
     * @return true if clean energy, false if non-clean or unrecognized
     */
    public static boolean isCleanEnergySource(String fuelName) {
        EnergySource source = fromFuelName(fuelName);
        return source != null && source.isClean();
    }

    /**
     * Calculates the percentage of clean energy from a fuel mix map.
     *
     * @param fuelMix map of fuel name to percentage (e.g., {"biomass": 7.5, "coal": 25.0})
     * @return clean energy percentage (0-100)
     */
    public static double calculateCleanPercentage(Map<String, Double> fuelMix) {
        if (fuelMix == null || fuelMix.isEmpty()) {
            return 0.0;
        }

        double cleanPercentage = fuelMix.entrySet().stream()
                .filter(entry -> isCleanEnergySource(entry.getKey()))
                .mapToDouble(Map.Entry::getValue)
                .sum();

        if (cleanPercentage > 100.0) {
            throw new IllegalStateException(
                    String.format("Clean energy percentage %.2f%% exceeds 100%%", cleanPercentage)
            );
        }
        return cleanPercentage;
    }

    public static double calculateCleanPercentage(List<FuelMix> generationMix) {
        Map<String, Double> fuelMap = generationMix.stream()
                .collect(Collectors.toMap(FuelMix::fuel, FuelMix::perc));
        return calculateCleanPercentage(fuelMap);
    }

    /**
     * Returns all clean energy sources.
     *
     * @return array of clean energy sources
     */
    public static EnergySource[] getCleanSources() {
        return Arrays.stream(values())
                .filter(EnergySource::isClean)
                .toArray(EnergySource[]::new);
    }

    /**
     * Returns all non-clean energy sources.
     *
     * @return array of non-clean energy sources
     */
    public static EnergySource[] getNonCleanSources() {
        return Arrays.stream(values())
                .filter(source -> !source.isClean())
                .toArray(EnergySource[]::new);
    }
}