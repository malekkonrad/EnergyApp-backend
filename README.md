# EnergyApp Backend

RESTful API for analyzing UK energy mix for three days and finding optimal EV charging windows based on clean energy availability.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Development](#development)

---

## 🎯 Overview

This application provides two main functionalities:

1. **Energy Mix Display** - Shows current and forecasted energy mix for the UK (today, tomorrow, day after tomorrow)
2. **Optimal Charging Window** - Calculates the best time to charge an electric vehicle based on clean energy availability

The application uses the [Carbon Intensity API](https://carbonintensity.github.io/api-definitions/) to fetch real-time and forecasted energy generation data.

### Clean Energy Sources

For this project, clean energy includes:
- ✅ **Biomass** - Organic matter
- ✅ **Nuclear** - Nuclear power plants
- ✅ **Hydro** - Hydroelectric power
- ✅ **Wind** - Wind turbines
- ✅ **Solar** - Solar panels

---

## ✨ Features

- 📊 Aggregates half-hourly energy data by day
- 🔋 Finds optimal charging windows (1-6 hours)
- ♻️ Calculates clean energy percentage
- 🌍 CORS-enabled for frontend integration
- 📝 OpenAPI/Swagger documentation
- ✅ Comprehensive test coverage

---

## 🛠 Tech Stack

- **Java 17**
- **Spring Boot 3.4.1**
- **Spring WebFlux** (for WebClient)
- **Maven** (build tool)
- **JUnit 5** + **Mockito** (testing)
- **JaCoCo** (code coverage)
- **Springdoc OpenAPI** (API documentation)

---


## 📚 API Documentation

### 1. Get Energy Mix for Three Days

**Endpoint:** `GET /api/energy-mix`

**Description:** Returns aggregated energy mix data for today, tomorrow, and day after tomorrow.

**Response:**
```json
[
  {
    "date": "2025-12-04",
    "mix": {
      "biomass": 22.5,
      "nuclear": 38.0,
      "hydro": 5.0,
      "wind": 58.5,
      "solar": 11.0,
      "coal": 47.0,
      "gas": 31.5,
      "imports": 12.0,
      "other": 1.5
    },
    "cleanPercentage": 135.0
  },
  ...
]
```

**Example:**
```bash
curl http://localhost:8080/api/energy-mix
```

---

### 2. Find Optimal Charging Window

**Endpoint:** `GET /api/charging-window`

**Parameters:**
- `hours` - Duration in hours (1-6)

**Description:** Finds the time window with the highest clean energy percentage for the next two days.

**Response:**
```json
{
  "start": "2025-12-04T12:00:00Z",
  "end": "2025-12-04T15:00:00Z",
  "cleanEnergyShare": 78.5
}
```

**Examples:**
```bash
# 4-hour window
curl http://localhost:8080/api/charging-window?hours=4
```

**Validation:**
- `hours` must be between 1 and 6
- Returns `400 Bad Request` if validation fails

---


## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Internet connection (for external API calls)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd EnergyApp-backend
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the API**
    - API Base URL: `http://localhost:8080`
    - Swagger UI: `http://localhost:8080/swagger-ui.html`
    - OpenAPI Spec: `http://localhost:8080/v3/api-docs`

---

## Project structure

- ```src/main/java/.../web``` – REST controller
- ```src/main/java/.../service``` – business logic (energy mix, charging window)
- ```src/main/java/.../client``` – integration with Carbon Intensity API (WebClient)
- ```src/main/java/.../domain``` – domain model, enums (e.g. EnergySource)
- ```src/main/java/.../config``` – configuration (CORS, WebClient)
- ```src/test/java/...``` – unit & integration tests

### Key Design Decisions

1. **Clean Architecture** - Separation of concerns (web, service, domain, client)
2. **Interface Segregation** - Services defined as interfaces for testability
3. **Enum with Behavior** - `EnergySource` encapsulates clean energy logic
4. **DTO Pattern** - Separate models for API responses vs. domain
5. **WebClient over RestTemplate** - Modern reactive HTTP client

---

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Tests with Coverage

```bash
mvn clean test jacoco:report
```

### View Coverage Report

```bash
# Linux/macOS
xdg-open target/site/jacoco/index.html

# Windows
start target/site/jacoco/index.html
```

### Test Structure

- **Unit Tests** - Domain logic, service layer
- **Integration Tests** - Controller endpoints with MockMvc
- **HTTP Client Tests** - External API integration (if using MockWebServer)

### Coverage status
- Current line coverage (JaCoCo) is above 80%.
- The core business logic is fully covered:
  - aggregation of half-hour intervals into daily averages,
  - calculation of clean energy percentage,
  - optimal charging window algorithm (sliding window over 30-minute intervals).

This means the critical parts of the task (both endpoints required in the assignment) are verified by automated tests.

---

## 🔧 Development

### Configuration

Edit `src/main/resources/application.properties`:

```properties
# Server port (default: 8080)
server.port=8080

# CORS allowed origins
cors.allowed-origins=http://localhost:3000,http://localhost:4200
```

CORS configuration (local vs production)

- Local development
Set app.cors.allowed-origins in application.properties to match your frontend origin(s)
(e.g. React on http://localhost:3000, Angular on http://localhost:4200).

- Production / Render
On platforms like Render, the same property is configured via environment variable:

APP_CORS_ALLOWED_ORIGINS=https://your-frontend-url

Spring Boot uses relaxed binding, so APP_CORS_ALLOWED_ORIGINS maps directly to
app.cors.allowed-origins. This is how the deployed Web Service is configured on Render –
backend accepts requests only from the deployed frontend origin.



---

## 📝 External API Reference

This project integrates with the **Carbon Intensity API**:

- **Documentation**: https://carbonintensity.github.io/api-definitions/
- **Base URL**: `https://api.carbonintensity.org.uk`
- **Rate Limits**: None specified (use responsibly)
- **Data Intervals**: Half-hourly (30 minutes)

### Key Endpoint Used

```
GET /generation/{from}/{to}
```

Returns energy generation mix for the specified time range.

