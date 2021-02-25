package com.vmware.employee;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeApplicationTests {
	
	@Autowired
	private MockMvc mockvc;

	@Test
	void contextLoads() {
		
	}
	
	@Test
	void testBulkEmployeeLoad() throws Exception {
		Path inputFile = Paths.get("src/test/resources/employees.txt");
		final MvcResult result = mockvc.perform(MockMvcRequestBuilders.multipart("/api/employee")
				.file("file", Files.readAllBytes(inputFile))
				.queryParam("action", "upload"))
		.andExpect(status().isOk()).andReturn();
		String response = result.getResponse().getContentAsString();
		String statusUrl = JsonPath.parse(response).read("$.statusCheckUrl");
		//wait until async processing is complete
		Thread.sleep(5000);
		final MvcResult result2 = mockvc.perform(MockMvcRequestBuilders.get(statusUrl)).andExpect(status().isOk()).andReturn();
		String response2 = result2.getResponse().getContentAsString();
		assertThat(response2).isEqualTo("Success");
	}
	
	@Test
	void testIncorrectFileFormat() throws Exception {
		Path inputFile = Paths.get("src/test/resources/employees2.txt");
		final MvcResult result = mockvc.perform(MockMvcRequestBuilders.multipart("/api/employee")
				.file("file", Files.readAllBytes(inputFile))
				.queryParam("action", "upload"))
		.andExpect(status().isOk()).andReturn();
		String response = result.getResponse().getContentAsString();
		String statusUrl = JsonPath.parse(response).read("$.statusCheckUrl");
		//wait until async processing is complete
		Thread.sleep(5000);
		final MvcResult result2 = mockvc.perform(MockMvcRequestBuilders.get(statusUrl)).andExpect(status().isOk()).andReturn();
		String response2 = result2.getResponse().getContentAsString();
		assertThat(response2).isEqualTo("Failed");
	}
	
	@Test
	void testIncorrectTaskId() throws Exception {
		mockvc.perform(MockMvcRequestBuilders.get("/api/getStatus/100")).andExpect(status().isBadRequest());	
	}

}