package tanzu.hospital.allergyservice.http;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tanzu.hospital.allergyservice.service.AllergyService;
import tanzu.hospital.domain.Allergy;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class AllergyController {
    private final AllergyService allergyService;

    public AllergyController(AllergyService allergyService) {
        this.allergyService = allergyService;
    }

    @GetMapping("/patientAllergies/{patientId}")
    public ResponseEntity<List<Allergy>> patientAllergy(@PathVariable("patientId") String patientId) {
        List<Allergy> answer = allergyService.findAllergyIntolerance(patientId);
        return ResponseEntity.ok(answer);
    }
}
