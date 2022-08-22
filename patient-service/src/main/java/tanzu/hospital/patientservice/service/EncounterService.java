package tanzu.hospital.patientservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tanzu.hospital.domain.SimpleEncounter;

import java.util.Collections;
import java.util.List;

@Service
public class EncounterService {
    private static final Logger log = LoggerFactory.getLogger(EncounterService.class);
    private final CircuitBreaker circuitBreaker;
    private final String encounterServiceURL;
    private final RestTemplate restTemplate;

    public EncounterService(CircuitBreakerFactory<?,?> circuitBreakerFactory,
                            @Value("${hospital.services.encounter-service}") String encounterServiceURL,
                            RestTemplate restTemplate) {
        circuitBreaker = circuitBreakerFactory.create("encounter-service-cb");
        this.encounterServiceURL = encounterServiceURL;
        this.restTemplate = restTemplate;
    }

    public List<SimpleEncounter> findEncounters(String patientID) {
        return circuitBreaker.run(
                () -> findRemoteEncounters(patientID),
                this::encounterFallback
        );
    }

    private List<SimpleEncounter> findRemoteEncounters(String patientID) {
        String uri = UriComponentsBuilder.fromHttpUrl(encounterServiceURL)
                .pathSegment("api", "patientEncounters", patientID)
                .toUriString();

        ResponseEntity<List<SimpleEncounter>> res = restTemplate.exchange(uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<SimpleEncounter>>(){});

        return res.getBody();
    }

    private List<SimpleEncounter> encounterFallback(Throwable t) {
        log.error("Exception in encounter service", t);
        return Collections.emptyList();
    }
}
