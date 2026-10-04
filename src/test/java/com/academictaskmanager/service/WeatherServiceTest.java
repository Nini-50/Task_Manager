package com.academictaskmanager.service;

import com.academictaskmanager.dto.GeocodingResponseDto;
import com.academictaskmanager.dto.GeocodingResultDto;
import com.academictaskmanager.dto.OpenMeteoCurrentDto;
import com.academictaskmanager.dto.OpenMeteoForecastDto;
import com.academictaskmanager.dto.WeatherDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Test
    void blankLocationThrows() {
        WeatherService service = new WeatherService(mock(RestClient.class));

        assertThatThrownBy(() -> service.getCurrentWeather("  "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Set a location");
    }

    @Test
    void noGeocodingMatchThrows() {
        RestClient restClient = mock(RestClient.class, Answers.RETURNS_DEEP_STUBS);
        GeocodingResponseDto empty = new GeocodingResponseDto();
        empty.setResults(List.of());
        when(restClient.get().uri(anyString(), any(Object[].class)).retrieve().body(GeocodingResponseDto.class))
                .thenReturn(empty);

        WeatherService service = new WeatherService(restClient);

        assertThatThrownBy(() -> service.getCurrentWeather("Nowhereville"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Couldn't find a location");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void successfulLookupMapsFields() {
        // Two distinct downstream chains (geocode vs. forecast) share the same restClient.get()
        // entry point, so they're distinguished here by matching on the request URL rather than
        // relying on a single deep-stub chain, which would otherwise conflict under strict stubbing.
        RestClient restClient = mock(RestClient.class);
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        when(restClient.get()).thenReturn(uriSpec);

        RestClient.RequestHeadersSpec geocodeHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec geocodeResponseSpec = mock(RestClient.ResponseSpec.class);
        when(uriSpec.uri(argThat(url -> url != null && url.contains("geocoding-api")), any(Object[].class)))
                .thenReturn(geocodeHeadersSpec);
        when(geocodeHeadersSpec.retrieve()).thenReturn(geocodeResponseSpec);

        RestClient.RequestHeadersSpec forecastHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec forecastResponseSpec = mock(RestClient.ResponseSpec.class);
        when(uriSpec.uri(argThat(url -> url != null && url.contains("api.open-meteo.com/v1/forecast")), any(Object[].class)))
                .thenReturn(forecastHeadersSpec);
        when(forecastHeadersSpec.retrieve()).thenReturn(forecastResponseSpec);

        GeocodingResultDto place = new GeocodingResultDto();
        place.setName("Boston");
        place.setAdmin1("Massachusetts");
        place.setLatitude(42.36);
        place.setLongitude(-71.06);
        GeocodingResponseDto geocodeResponse = new GeocodingResponseDto();
        geocodeResponse.setResults(List.of(place));
        when(geocodeResponseSpec.body(GeocodingResponseDto.class)).thenReturn(geocodeResponse);

        OpenMeteoCurrentDto current = new OpenMeteoCurrentDto();
        current.setTemperature2m(72.5);
        current.setWindSpeed10m(8.0);
        current.setWeatherCode(0);
        current.setIsDay(1);
        OpenMeteoForecastDto forecastResponse = new OpenMeteoForecastDto();
        forecastResponse.setCurrent(current);
        when(forecastResponseSpec.body(OpenMeteoForecastDto.class)).thenReturn(forecastResponse);

        WeatherService service = new WeatherService(restClient);
        WeatherDto result = service.getCurrentWeather("Boston, MA");

        assertThat(result.getLocation()).isEqualTo("Boston, Massachusetts");
        assertThat(result.getTemperatureF()).isEqualTo(72.5);
        assertThat(result.getWindMph()).isEqualTo(8.0);
        assertThat(result.getCondition()).isEqualTo("Clear sky");
        assertThat(result.getEmoji()).isEqualTo("☀️");
        assertThat(result.isDay()).isTrue();
    }
}
