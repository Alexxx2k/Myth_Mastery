package ru.goth.domain.mappers;

import javax.annotation.processing.Generated;
import ru.goth.domain.dto.CustomerDto;
import ru.goth.domain.entities.Customer;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-07T23:16:28+0300",
    comments = "version: 1.6.0, compiler: javac, environment: Java 17.0.14 (Amazon.com Inc.)"
)
public class CustomerMapperImpl implements CustomerMapper {

    @Override
    public CustomerDto toCustomerDto(Customer customer) {
        if ( customer == null ) {
            return null;
        }

        CustomerDto customerDto = new CustomerDto();

        customerDto.setId( customer.getId() );
        customerDto.setName( customer.getName() );
        customerDto.setCityId( customer.getCityId() );
        customerDto.setEmail( customer.getEmail() );

        return customerDto;
    }

    @Override
    public Customer toCustomer(CustomerDto customerDto) {
        if ( customerDto == null ) {
            return null;
        }

        Customer customer = new Customer();

        customer.setId( customerDto.getId() );
        customer.setName( customerDto.getName() );
        customer.setCityId( customerDto.getCityId() );
        customer.setEmail( customerDto.getEmail() );

        return customer;
    }
}
