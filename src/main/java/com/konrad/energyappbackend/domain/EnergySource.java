package com.konrad.energyappbackend.domain;

import com.konrad.energyappbackend.client.dto.FuelMix;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Energy sources as defined by the Carbon Intensity API.
 * Each source is categorized as clean (renewable + nuclear) or non-clean.
 *
 * This enum encapsulates all energy source logic, eliminating the need for a separate utility class.
 */
@Getter
public enum EnergySource {
    // Clean energy sources (renewable + nuclear)
    BIOMASS(true),
    NUCLEAR(true),
    HYDRO(true),
    WIND(true),
    SOLAR(true),

    // Non-clean energy sources (fossil fuels + imports)
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
     * Case-insensitive matching.
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