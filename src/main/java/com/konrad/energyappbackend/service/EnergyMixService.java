package com.konrad.energyappbackend.service;

import com.konrad.energyappbackend.exception.ExternalApiException;
import com.konrad.energyappbackend.web.dto.DailyMixDto;

import java.util.List;

/**
 * Service for retrieving and processing energy mix data from the Carbon Intensity API.
 *
 * <p>The service fetches half-hourly generation data for the UK energy grid and aggregates
 * it by day, calculating average percentages for each energy source and the total clean
 * energy percentage.
 *
 * <p>Clean energy sources include: biomass, nuclear, hydro, wind, and solar.
 *
 * @see <a href="https://carbonintensity.github.io/api-definitions/">Carbon Intensity API</a>
 */
public interface EnergyMixService {

    /**
     * Retrieves energy mix data for three days (today, tomorrow, day after tomorrow).
     *
     * <p>The method:
     * <ul>
     *   <li>Fetches half-hourly intervals from the external API</li>
     *   <li>Groups data by date</li>
     *   <li>Aggregates (sums) percentages for each energy source per day</li>
     *   <li>Calculates the total clean energy percentage</li>
     * </ul>
     *
     * @return list of daily energy mix data, sorted by date ascending
     *         Each {@link DailyMixDto} contains:
     *         <ul>
     *           <li>date - the date for which data applies</li>
     *           <li>mix - map of energy source name to aggregated percentage</li>
     *           <li>cleanPercentage - sum of percentages from clean sources</li>
     *         </ul>
     * @throws ExternalApiException if the external API is unavailable or returns invalid data
     */
    List<DailyMixDto> getDailyMixForThreeDays();
}
