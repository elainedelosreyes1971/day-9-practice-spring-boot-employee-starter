package com.afs.restapi;

import com.afs.restapi.entity.Company;
import com.afs.restapi.entity.Employee;
import com.afs.restapi.repository.CompanyJpaRepository;
import com.afs.restapi.repository.EmployeeJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CompanyJpaRepository companyJpaRepository;

    @Autowired
    private EmployeeJpaRepository employeeJpaRepository;

    @BeforeEach
    void setUp() {
        companyJpaRepository.deleteAll();
        employeeJpaRepository.deleteAll();
    }

    @Test
    void should_update_company_name() throws Exception {
        Company previousCompany = new Company(1L, "abc");
        Company savedCompany = companyJpaRepository.save(previousCompany);

        Company companyUpdateRequest = new Company(1L, "xyz");
        ObjectMapper objectMapper = new ObjectMapper();
        String updatedEmployeeJson = objectMapper.writeValueAsString(companyUpdateRequest);
        mockMvc.perform(put("/companies/{id}", savedCompany.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedEmployeeJson))
                .andExpect(MockMvcResultMatchers.status().is(204));

        Optional<Company> optionalCompany = companyJpaRepository.findById(savedCompany.getId());
        assertTrue(optionalCompany.isPresent());
        Company updatedCompany = optionalCompany.get();
        Assertions.assertEquals(savedCompany.getId(), updatedCompany.getId());
        Assertions.assertEquals(companyUpdateRequest.getName(), updatedCompany.getName());
    }

    @Test
    void should_delete_company_name() throws Exception {
        Company company = new Company(1L, "abc");
        Company savedEmployee = companyJpaRepository.save(company);

        mockMvc.perform(delete("/companies/{id}", savedEmployee.getId()))
                .andExpect(MockMvcResultMatchers.status().is(204));

        assertTrue(companyJpaRepository.findById(1L).isEmpty());
    }

    @Test
    void should_create_company() throws Exception {
        Company company = getCompany1();

        ObjectMapper objectMapper = new ObjectMapper();
        String companyRequest = objectMapper.writeValueAsString(company);
        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(companyRequest))
                .andExpect(MockMvcResultMatchers.status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(company.getName()));
    }

    @Test
    void should_find_companies() throws Exception {
        Company company = getCompany1();
        Company savedCompany = companyJpaRepository.save(company);

        mockMvc.perform(get("/companies"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(savedCompany.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(company.getName()));
    }

    @Test
    void should_find_companies_by_page() throws Exception {
        Company company1 = companyJpaRepository.save(getCompany1());
        Company company2 = companyJpaRepository.save(getCompany2());
        companyJpaRepository.save(getCompany3());

        mockMvc.perform(get("/companies")
                        .param("pageNumber", "1")
                        .param("pageSize", "2"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(company1.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(getCompany1().getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(company2.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(getCompany2().getName()))
        ;
    }

    @Test
    void should_find_company_by_id() throws Exception {
        Company company = companyJpaRepository.save(getCompany1());
        Employee employee = employeeJpaRepository.save(getEmployee(company));

        mockMvc.perform(get("/companies/{id}", company.getId()))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(company.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(company.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees.length()").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees[0].id").value(employee.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees[0].name").value(employee.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees[0].age").value(employee.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees[0].gender").value(employee.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees[0].salary").value(employee.getSalary()));
    }

    @Test
    void should_find_employees_by_companies() throws Exception {
        Company company = companyJpaRepository.save(getCompany1());
        Employee employee = employeeJpaRepository.save(getEmployee(company));

        mockMvc.perform(get("/companies/{companyId}/employees", company.getId()))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(employee.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(employee.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].age").value(employee.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].gender").value(employee.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].salary").value(employee.getSalary()));
    }

    private static Employee getEmployee(Company company) {
        Employee employee = new Employee();
        employee.setName("Bob");
        employee.setAge(22);
        employee.setGender("Male");
        employee.setSalary(10000);
        employee.setCompanyId(company.getId());
        return employee;
    }


    private static Company getCompany1() {
        Company company = new Company();
        company.setName("ABC");
        return company;
    }

    private static Company getCompany2() {
        Company company = new Company();
        company.setName("DEF");
        return company;
    }

    private static Company getCompany3() {
        Company company = new Company();
        company.setName("XYZ");
        return company;
    }
}