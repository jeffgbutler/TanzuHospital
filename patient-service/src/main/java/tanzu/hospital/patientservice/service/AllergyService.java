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
import tanzu.hospital.domain.Allergy;

import java.util.Collections;
import java.util.List;

@Service
public class AllergyService {
    private static final Logger log = LoggerFactory.getLogger(AllergyService.class);
    private final CircuitBreaker circuitBreaker;
    private final String allergyServiceURL;
    private final RestTemplate restTemplate;

    public AllergyService(CircuitBreakerFactory<?,?> circuitBreakerFactory,
                          @Value("${hospital.services.allergy-service}") String allergyServiceURL,
                          RestTemplate restTemplate) {
        circuitBreaker = circuitBreakerFactory.create("allergy-service-cb");
        this.allergyServiceURL = allergyServiceURL;
        this.restTemplate = restTemplate;
    }

    public List<Allergy> findAllergies(String patientID) {
        return circuitBreaker.run(
                () -> findRemoteAllergies(patientID),
                this::allergyFallback
        );
    }

    private List<Allergy> findRemoteAllergies(String patientID) {
        String uri = UriComponentsBuilder.fromHttpUrl(allergyServiceURL)
                .pathSegment("api", "patientAllergies", patientID)
                .toUriString();

        ResponseEntity<List<Allergy>> res = restTemplate.exchange(uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Allergy>>(){});

        return res.getBody();
    }

    private List<Allergy> allergyFallback(Throwable t) {
        log.error("Exception in allergy service", t);
        return Collections.emptyList();
    }
}
