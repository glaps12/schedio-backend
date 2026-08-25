package testsupport.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityTestController {

	@GetMapping({"/actuator/health", "/v3/api-docs", "/swagger-ui.html"})
	String publicEndpoint() {
		return "public";
	}

	@GetMapping("/test/security/authenticated")
	String authenticatedGet() {
		return "authenticated";
	}

	@PostMapping("/test/security/authenticated")
	String authenticatedPost() {
		return "authenticated";
	}

	@PreAuthorize("hasRole('BUSINESS_OWNER')")
	@GetMapping("/test/security/owner")
	String owner() {
		return "owner";
	}
}
