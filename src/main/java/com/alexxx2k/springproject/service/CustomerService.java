package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.Customer;
import com.alexxx2k.springproject.domain.dto.Registration;
import com.alexxx2k.springproject.domain.entities.CustomerEntity;
import com.alexxx2k.springproject.domain.entities.CityEntity;
import com.alexxx2k.springproject.repository.CustomerRepository;
import com.alexxx2k.springproject.repository.CityRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository,
                           CityRepository cityRepository,
                           PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.cityRepository = cityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toDomainCustomer)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id)
                .map(this::toDomainCustomer);
    }

    @Transactional(readOnly = true)
    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .map(this::toDomainCustomer);
    }

    @Transactional
    public Customer registerCustomer(Registration registration) {
        validateRegistration(registration);

        // НОВАЯ ПРОВЕРКА: запрещаем регистрацию как admin
        if ("admin".equalsIgnoreCase(registration.email()) ||
                "admin".equalsIgnoreCase(registration.name())) {
            throw new IllegalArgumentException("Недопустимое имя пользователя 'admin'");
        }

        if (customerRepository.existsByEmail(registration.email())) {
            throw new IllegalArgumentException("Email '" + registration.email() + "' уже используется");
        }

        CityEntity city = getOrCreateCity(registration.cityName());

        CustomerEntity entity = new CustomerEntity(
                null,
                registration.name(),
                registration.email(),
                passwordEncoder.encode(registration.password()),
                city
        );

        CustomerEntity savedEntity = customerRepository.save(entity);
        return toDomainCustomer(savedEntity);
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer Customer) {
        CustomerEntity existingEntity = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Клиент с ID " + id + " не найден"));

        if (!existingEntity.getEmail().equals(Customer.email())) {
            if (customerRepository.existsByEmail(Customer.email())) {
                throw new IllegalArgumentException("Email '" + Customer.email() + "' уже используется");
            }
        }

        CityEntity city = getOrCreateCity(Customer.cityName());

        existingEntity.setName(Customer.name());
        existingEntity.setEmail(Customer.email());
        existingEntity.setCity(city);

        if (Customer.password() != null && !Customer.password().isEmpty()) {
            existingEntity.setPasswordHash(passwordEncoder.encode(Customer.password()));
        }

        CustomerEntity savedEntity = customerRepository.save(existingEntity);
        return toDomainCustomer(savedEntity);
    }

    @Transactional
    public void updatePassword(Long id, String newPassword) {
        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Клиент с ID " + id + " не найден"));

        entity.setPasswordHash(passwordEncoder.encode(newPassword));
        customerRepository.save(entity);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new IllegalArgumentException("Клиент с ID " + id + " не найден");
        }
        customerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    private void validateRegistration(Registration dto) {
        if (!dto.password().equals(dto.confirmPassword())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        if (dto.password().length() < 4) {
            throw new IllegalArgumentException("Пароль должен содержать минимум 4 символа");
        }
    }

    private CityEntity getOrCreateCity(String cityName) {
        return cityRepository.findByName(cityName)
                .orElseGet(() -> {
                    CityEntity newCity = new CityEntity(null, cityName, 30000L);
                    return cityRepository.save(newCity);
                });
    }

    private Customer toDomainCustomer(CustomerEntity entity) {
        return new Customer(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                null,
                entity.getCity() != null ? entity.getCity().getId() : null,
                entity.getCity() != null ? entity.getCity().getName() : null
        );
    }

    // НОВЫЙ МЕТОД: Получить ID клиента по email (для заказов)
    @Transactional(readOnly = true)
    public Long getCustomerIdByEmail(String email) {
        CustomerEntity customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Клиент с email '" + email + "' не найден"));
        return customer.getId();
    }

    // НОВЫЙ МЕТОД: Получить текущего клиента по email
    @Transactional(readOnly = true)
    public Customer getCurrentCustomer(String email) {
        return customerRepository.findByEmail(email)
                .map(this::toDomainCustomer)
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));
    }
}
