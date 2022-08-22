package tanzu.hospital.patientservice.http;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tanzu.hospital.domain.PatientDetails;
import tanzu.hospital.domain.SimplePatient;
import tanzu.hospital.patientservice.service.PatientService;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/patients")
    public ResponseEntity<List<SimplePatient>> searchPatients(@RequestParam(value = "firstName", required = false) String firstName,
                                                              @RequestParam(value = "lastName", required = false) String lastName) {
        List<SimplePatient> patients = patientService.searchPatients(StringUtils.trimToNull(firstName), StringUtils.trimToNull(lastName));
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/patients/{id}")
    public ResponseEntity<PatientDetails> findPatient(@PathVariable(value = "id") String id) {
        Optional<PatientDetails> patient = patientService.findPatient(id);
        return patient.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
