package dev.saul.gijoneventos.register;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api-endpoint}/register")
public class RegisterController {

    private final RegisterService registerService;

    // Constructor manual (se podria utilizar @RequiredArgsConstructor de lombok para evitar escribirlo)
    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    @PostMapping("")
    public ResponseEntity<RegisterDTOResponse> registerUser(@RequestBody RegisterDTORequest dto) {
        RegisterDTOResponse response = registerService.registerUser(dto);
        return ResponseEntity.status(201).body(response);
    }
}