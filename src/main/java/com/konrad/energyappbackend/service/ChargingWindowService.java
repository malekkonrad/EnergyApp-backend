package com.konrad.energyappbackend.service;

import com.konrad.energyappbackend.exception.ExternalApiException;
import com.konrad.energyappbackend.web.dto.ChargingWindowDto;

/**
 * Service for calculating optimal electric vehicle charging windows.
 *
 * <p>The service analyzes forecasted energy mix data for the next two days
 * to find the time window with the highest clean energy percentage.
 *
 * <p>Clean energy sources include: biomass, nuclear, hydro, wind, and solar.
 */
public interface ChargingWindowService {

    /**
     * Finds the optimal charging window for the next two days.
     *
     * <p>The method:
     * <ul>
     *   <li>Fetches half-hourly forecast data for the next 48 hours</li>
     *   <li>Calculates clean energy percentage for each possible window of the requested duration</li>
     *   <li>Returns the window with the highest clean energy percentage</li>
     * </ul>
     *
     * <p>The window can start on one day and end on the next.
     *
     * @param hours duration of the charging window in full hours (must be between 1 and 6)
     * @return optimal charging window with start time, end time, and average clean energy percentage
     * @throws IllegalArgumentException if hours is not between 1 and 6
     * @throws ExternalApiException if the external API is unavailable or returns invalid data
     *
     * @example
     * <pre>
     * // Find optimal 3-hour charging window
     * ChargingWindowDto window = service.findOptimalChargingWindow(3);
     * // window.start() = 2025-12-04T12:00:00Z
     * // window.end()   = 2025-12-04T15:00:00Z
     * // window.cleanEnergyShare() = 78.5
     * </pre>
     */
    ChargingWindowDto getOptimalWindow(int hours);
}
