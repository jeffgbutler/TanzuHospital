package tanzu.hospital.encounterservice.http;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tanzu.hospital.domain.SimpleEncounter;
import tanzu.hospital.encounterservice.service.EncounterService;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class EncounterController {

    private final EncounterService encounterService;

    public EncounterController(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @GetMapping("/patientEncounters/{patientId}")
    public ResponseEntity<List<SimpleEncounter>> patientEncounters(@PathVariable("patientId") String patientId) {
        List<SimpleEncounter> answer = encounterService.findEncounters(patientId);
        return ResponseEntity.ok(answer);
    }
}
