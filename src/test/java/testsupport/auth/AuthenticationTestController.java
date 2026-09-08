package testsupport.auth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationTestController {

	@PreAuthorize("hasRole('BUSINESS_OWNER')")
	@GetMapping("/test/auth/owner")
	String owner() {
		return "owner";
	}
}
