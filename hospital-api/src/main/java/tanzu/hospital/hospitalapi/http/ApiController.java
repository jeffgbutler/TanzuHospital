package tanzu.hospital.hospitalapi.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tanzu.hospital.domain.PatientDetails;
import tanzu.hospital.domain.SimplePatient;
import tanzu.hospital.hospitalapi.service.PatientService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    private final PatientService patientService;
    private final String welcomeMessage;

    public ApiController(PatientService patientService, @Value("${welcome.message}") String welcomeMessage) {
        this.patientService = patientService;
        this.welcomeMessage = welcomeMessage;
    }

    @GetMapping("/welcomeMessage")
    public String welcomeMessage() {
        return welcomeMessage;
    }

    @GetMapping("/patients")
    public ResponseEntity<List<SimplePatient>> searchPatients(@RequestParam(value = "firstName", required = false) String firstName,
                                                              @RequestParam(value = "lastName", required = false) String lastName) {
        List<SimplePatient> patients = patientService.search(firstName, lastName);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/patients/{id}")
    public ResponseEntity<PatientDetails> findPatient(@PathVariable(value = "id") String id) {
        Optional<PatientDetails> patient = patientService.findPatient(id);
        return patient.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
