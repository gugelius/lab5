package org.arkasha.jwtspringmaven;

import org.arkasha.jwtspringmaven.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JwtSpringMavenApplicationTests {
	@Autowired
	private UserService userService;



	@Test
	void contextLoads() {
	}
}

