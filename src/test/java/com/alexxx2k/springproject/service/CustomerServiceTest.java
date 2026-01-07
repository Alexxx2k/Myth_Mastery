package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.Customer;
import com.alexxx2k.springproject.domain.dto.Registration;
import com.alexxx2k.springproject.domain.entities.CustomerEntity;
import com.alexxx2k.springproject.domain.entities.CityEntity;
import com.alexxx2k.springproject.repository.CustomerRepository;
import com.alexxx2k.springproject.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

    private CityEntity testCityEntity;
    private CustomerEntity testCustomerEntity;
    private Customer testCustomer;
    private Registration testRegistration;

    @BeforeEach
    void setUp() {
        testCityEntity = new CityEntity(1L, "Москва", 5L);
        testCustomerEntity = new CustomerEntity(1L, "Иван Иванов", "ivan@test.com",
                "encodedPassword", testCityEntity);
        testCustomer = new Customer(1L, "Иван Иванов", "ivan@test.com", null, 1L, "Москва");
        testRegistration = new Registration("Иван Иванов", "ivan@test.com",
                "password123", "password123", "Москва");
    }

    @Test
    void getAllCustomers_ShouldReturnCustomers() {
        when(customerRepository.findAll()).thenReturn(List.of(testCustomerEntity));

        List<Customer> customers = customerService.getAllCustomers();

        assertEquals(1, customers.size());
        assertEquals("Иван Иванов", customers.get(0).name());
        assertEquals("ivan@test.com", customers.get(0).email());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void getCustomerById_ShouldReturnCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomerEntity));

        Optional<Customer> result = customerService.getCustomerById(1L);

        assertTrue(result.isPresent());
        assertEquals("Иван Иванов", result.get().name());
        assertEquals("ivan@test.com", result.get().email());
    }

    @Test
    void getCustomerById_ShouldReturnEmptyWhenNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.getCustomerById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getCustomerByEmail_ShouldReturnCustomer() {
        when(customerRepository.findByEmail("ivan@test.com")).thenReturn(Optional.of(testCustomerEntity));

        Optional<Customer> result = customerService.getCustomerByEmail("ivan@test.com");

        assertTrue(result.isPresent());
        assertEquals("Иван Иванов", result.get().name());
    }

    @Test
    void getCustomerByEmail_ShouldReturnEmptyWhenNotFound() {
        when(customerRepository.findByEmail("ivan@test.com")).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.getCustomerByEmail("ivan@test.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void registerCustomer_ShouldRegisterCustomer() {
        when(customerRepository.existsByEmail("ivan@test.com")).thenReturn(false);
        when(cityRepository.findByName("Москва")).thenReturn(Optional.of(testCityEntity));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(testCustomerEntity);

        Customer result = customerService.registerCustomer(testRegistration);

        assertNotNull(result);
        assertEquals("Иван Иванов", result.name());
        assertEquals("ivan@test.com", result.email());
        verify(customerRepository, times(1)).save(any(CustomerEntity.class));
    }

    @Test
    void registerCustomer_ShouldThrowWhenEmailExists() {
        when(customerRepository.existsByEmail("ivan@test.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.registerCustomer(testRegistration);
        });

        assertEquals("Email 'ivan@test.com' уже используется", exception.getMessage());
        verify(customerRepository, never()).save(any(CustomerEntity.class));
    }

    @Test
    void registerCustomer_ShouldThrowWhenPasswordMismatch() {
        Registration invalidRegistration = new Registration("Иван", "ivan@test.com",
                "pass1", "pass2", "Москва");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.registerCustomer(invalidRegistration);
        });

        assertEquals("Пароли не совпадают", exception.getMessage());
    }

    @Test
    void registerCustomer_ShouldThrowWhenPasswordTooShort() {
        Registration invalidRegistration = new Registration("Иван", "ivan@test.com",
                "123", "123", "Москва");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.registerCustomer(invalidRegistration);
        });

        assertEquals("Пароль должен содержать минимум 4 символа", exception.getMessage());
    }

    @Test
    void registerCustomer_ShouldThrowWhenAdminNameUsed() {
        Registration adminRegistration = new Registration("admin", "admin@test.com",
                "password123", "password123", "Москва");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.registerCustomer(adminRegistration);
        });

        assertEquals("Недопустимое имя пользователя 'admin'", exception.getMessage());
    }

    @Test
    void updateCustomer_ShouldUpdateCustomer() {
        Customer updatedCustomer = new Customer(1L, "Иван Петров", "ivan.new@test.com",
                "newpassword", 1L, "Москва");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomerEntity));
        when(customerRepository.existsByEmail("ivan.new@test.com")).thenReturn(false);
        when(cityRepository.findByName("Москва")).thenReturn(Optional.of(testCityEntity));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(testCustomerEntity);

        Customer result = customerService.updateCustomer(1L, updatedCustomer);

        assertNotNull(result);
        verify(customerRepository, times(1)).save(any(CustomerEntity.class));
    }

    @Test
    void updateCustomer_ShouldThrowWhenEmailExistsForDifferentCustomer() {
        Customer updatedCustomer = new Customer(1L, "Иван Петров", "existing@test.com",
                null, 1L, "Москва");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomerEntity));
        when(customerRepository.existsByEmail("existing@test.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.updateCustomer(1L, updatedCustomer);
        });

        assertEquals("Email 'existing@test.com' уже используется", exception.getMessage());
        verify(customerRepository, never()).save(any(CustomerEntity.class));
    }

    @Test
    void updateCustomer_ShouldNotThrowWhenSameEmailForSameCustomer() {
        Customer updatedCustomer = new Customer(1L, "Иван Петров", "ivan@test.com",
                null, 1L, "Москва");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomerEntity));
        when(cityRepository.findByName("Москва")).thenReturn(Optional.of(testCityEntity));
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(testCustomerEntity);

        Customer result = customerService.updateCustomer(1L, updatedCustomer);

        assertNotNull(result);
        verify(customerRepository, times(1)).save(any(CustomerEntity.class));
    }

    @Test
    void updateCustomer_ShouldThrowWhenNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.updateCustomer(1L, testCustomer);
        });

        assertEquals("Клиент с ID 1 не найден", exception.getMessage());
    }

    @Test
    void updatePassword_ShouldUpdatePassword() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomerEntity));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");

        customerService.updatePassword(1L, "newpassword");

        verify(passwordEncoder, times(1)).encode("newpassword");
        verify(customerRepository, times(1)).save(any(CustomerEntity.class));
    }

    @Test
    void updatePassword_ShouldThrowWhenNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.updatePassword(1L, "newpassword");
        });

        assertEquals("Клиент с ID 1 не найден", exception.getMessage());
    }

    @Test
    void deleteCustomer_ShouldDeleteCustomer() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(1L);

        customerService.deleteCustomer(1L);

        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCustomer_ShouldThrowWhenNotFound() {
        when(customerRepository.existsById(1L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.deleteCustomer(1L);
        });

        assertEquals("Клиент с ID 1 не найден", exception.getMessage());
        verify(customerRepository, never()).deleteById(anyLong());
    }

    @Test
    void existsByEmail_ShouldReturnTrue() {
        when(customerRepository.existsByEmail("ivan@test.com")).thenReturn(true);

        boolean exists = customerService.existsByEmail("ivan@test.com");

        assertTrue(exists);
    }

    @Test
    void existsByEmail_ShouldReturnFalse() {
        when(customerRepository.existsByEmail("ivan@test.com")).thenReturn(false);

        boolean exists = customerService.existsByEmail("ivan@test.com");

        assertFalse(exists);
    }

    @Test
    void getCustomerIdByEmail_ShouldReturnId() {
        when(customerRepository.findByEmail("ivan@test.com")).thenReturn(Optional.of(testCustomerEntity));

        Long id = customerService.getCustomerIdByEmail("ivan@test.com");

        assertEquals(1L, id);
    }

    @Test
    void getCustomerIdByEmail_ShouldThrowWhenNotFound() {
        when(customerRepository.findByEmail("ivan@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.getCustomerIdByEmail("ivan@test.com");
        });

        assertEquals("Клиент с email 'ivan@test.com' не найден", exception.getMessage());
    }

    @Test
    void getCurrentCustomer_ShouldReturnCustomer() {
        when(customerRepository.findByEmail("ivan@test.com")).thenReturn(Optional.of(testCustomerEntity));

        Customer result = customerService.getCurrentCustomer("ivan@test.com");

        assertNotNull(result);
        assertEquals("Иван Иванов", result.name());
        assertEquals("ivan@test.com", result.email());
    }

    @Test
    void getCurrentCustomer_ShouldThrowWhenNotFound() {
        when(customerRepository.findByEmail("ivan@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.getCurrentCustomer("ivan@test.com");
        });

        assertEquals("Клиент не найден", exception.getMessage());
    }

    @Test
    void registerCustomer_ShouldCreateCityIfNotExists() {
        when(customerRepository.existsByEmail("ivan@test.com")).thenReturn(false);
        when(cityRepository.findByName("НовыйГород")).thenReturn(Optional.empty());
        when(cityRepository.save(any(CityEntity.class))).thenAnswer(invocation -> {
            CityEntity city = invocation.getArgument(0);
            city.setId(2L);
            return city;
        });
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(testCustomerEntity);

        Registration newCityRegistration = new Registration("Иван", "ivan@test.com",
                "password123", "password123", "НовыйГород");

        Customer result = customerService.registerCustomer(newCityRegistration);

        assertNotNull(result);
        verify(cityRepository, times(1)).save(any(CityEntity.class));
        verify(customerRepository, times(1)).save(any(CustomerEntity.class));
    }
}
