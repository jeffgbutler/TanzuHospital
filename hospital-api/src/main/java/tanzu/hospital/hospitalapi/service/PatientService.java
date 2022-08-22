package tanzu.hospital.hospitalapi.service;

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
import tanzu.hospital.domain.PatientDetails;
import tanzu.hospital.domain.SimplePatient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class PatientService {
    private static final Logger log = LoggerFactory.getLogger(PatientService.class);

    private final RestTemplate restTemplate;
    private final CircuitBreaker circuitBreaker;
    private final String patientServiceURL;

    public PatientService(CircuitBreakerFactory<?,?> circuitBreakerFactory, RestTemplate restTemplate,
                          @Value("${hospital.services.patient-service}") String patientServiceURL) {
        this.restTemplate = restTemplate;
        circuitBreaker = circuitBreakerFactory.create("patient-service-cb");
        this.patientServiceURL = patientServiceURL;
    }

    public List<SimplePatient> search(String firstName, String lastName) {
        return circuitBreaker.run(
                () -> getRemotePatients(firstName, lastName),
                this::searchFallback);
    }

    private List<SimplePatient> getRemotePatients(String firstname, String lastName) {
        String uri = UriComponentsBuilder.fromHttpUrl(patientServiceURL)
                .pathSegment("api", "patients")
                .queryParam("firstName", firstname)
                .queryParam("lastName", lastName)
                .toUriString();

        ResponseEntity<List<SimplePatient>> res = restTemplate.exchange(uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<SimplePatient>>(){});

        return res.getBody();
    }

    private List<SimplePatient> searchFallback(Throwable t) {
        log.error("Exception in patient search service", t);
        return Collections.emptyList();
    }

    public Optional<PatientDetails> findPatient(String id) {
        return circuitBreaker.run(
                () -> findRemotePatient(id),
                this::patientFallback);
    }

    private Optional<PatientDetails> findRemotePatient(String id) {
        String uri = UriComponentsBuilder.fromHttpUrl(patientServiceURL)
                .pathSegment("api", "patients", id)
                .toUriString();

        ResponseEntity<PatientDetails> res = restTemplate.getForEntity(uri, PatientDetails.class);
        if (res.getStatusCode().is2xxSuccessful()) {
            return Optional.ofNullable(res.getBody());
        } else {
            return Optional.empty();
        }
    }

    private Optional<PatientDetails> patientFallback(Throwable t) {
        log.error("Exception in patient details service", t);
        return Optional.empty();
    }
}
