package com.konrad.energyappbackend.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EnergySource Domain Tests")
class EnergySourceTest {

    @Test
    @DisplayName("Should identify clean energy sources correctly")
    void shouldIdentifyCleanEnergySources() {
        // Clean sources
        assertThat(EnergySource.BIOMASS.isClean()).isTrue();
        assertThat(EnergySource.NUCLEAR.isClean()).isTrue();
        assertThat(EnergySource.HYDRO.isClean()).isTrue();
        assertThat(EnergySource.WIND.isClean()).isTrue();
        assertThat(EnergySource.SOLAR.isClean()).isTrue();

        // Non-clean sources
        assertThat(EnergySource.COAL.isClean()).isFalse();
        assertThat(EnergySource.GAS.isClean()).isFalse();
        assertThat(EnergySource.IMPORTS.isClean()).isFalse();
        assertThat(EnergySource.OTHER.isClean()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "biomass, BIOMASS",
            "NUCLEAR, NUCLEAR",
            "Wind, WIND",
            "SOLAR, SOLAR",
            "coal, COAL"
    })
    @DisplayName("Should parse fuel name case-insensitively")
    void shouldParseFuelNameCaseInsensitively(String input, EnergySource expected) {
        assertThat(EnergySource.fromFuelName(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "unknown", "invalid"})
    @DisplayName("Should return null for invalid fuel names")
    void shouldReturnNullForInvalidFuelNames(String input) {
        assertThat(EnergySource.fromFuelName(input)).isNull();
    }

    @ParameterizedTest
    @CsvSource({
            "biomass, true",
            "nuclear, true",
            "wind, true",
            "coal, false",
            "gas, false",
            "unknown, false"
    })
    @DisplayName("Should check if fuel name is clean energy")
    void shouldCheckIfFuelNameIsCleanEnergy(String fuelName, boolean expected) {
        assertThat(EnergySource.isCleanEnergySource(fuelName)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should calculate clean percentage correctly")
    void shouldCalculateCleanPercentageCorrectly() {
        Map<String, Double> fuelMix = new HashMap<>();
        fuelMix.put("biomass", 10.0);
        fuelMix.put("nuclear", 20.0);
        fuelMix.put("wind", 30.0);
        fuelMix.put("coal", 25.0);
        fuelMix.put("gas", 15.0);

        double cleanPercentage = EnergySource.calculateCleanPercentage(fuelMix);

        assertThat(cleanPercentage).isEqualTo(60.0); // biomass + nuclear + wind
    }

//    @Test
//    @DisplayName("Should return 0 for null fuel mix")
//    void shouldReturnZeroForNullFuelMix() {
//        assertThat(EnergySource.calculateCleanPercentage(null)).isEqualTo(0.0);
//    }

    @Test
    @DisplayName("Should return 0 for empty fuel mix")
    void shouldReturnZeroForEmptyFuelMix() {
        assertThat(EnergySource.calculateCleanPercentage(new HashMap<>())).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should return clean energy sources")
    void shouldReturnCleanEnergySources() {
        EnergySource[] cleanSources = EnergySource.getCleanSources();

        assertThat(cleanSources)
                .hasSize(5)
                .containsExactlyInAnyOrder(
                        EnergySource.BIOMASS,
                        EnergySource.NUCLEAR,
                        EnergySource.HYDRO,
                        EnergySource.WIND,
                        EnergySource.SOLAR
                );
    }

    @Test
    @DisplayName("Should return non-clean energy sources")
    void shouldReturnNonCleanEnergySources() {
        EnergySource[] nonCleanSources = EnergySource.getNonCleanSources();

        assertThat(nonCleanSources)
                .hasSize(4)
                .containsExactlyInAnyOrder(
                        EnergySource.COAL,
                        EnergySource.GAS,
                        EnergySource.IMPORTS,
                        EnergySource.OTHER
                );
    }
}