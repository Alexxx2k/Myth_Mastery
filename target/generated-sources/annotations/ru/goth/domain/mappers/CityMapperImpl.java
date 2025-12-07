package ru.goth.domain.mappers;

import javax.annotation.processing.Generated;
import ru.goth.domain.dto.CityDto;
import ru.goth.domain.entities.City;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-07T23:16:28+0300",
    comments = "version: 1.6.0, compiler: javac, environment: Java 17.0.14 (Amazon.com Inc.)"
)
public class CityMapperImpl implements CityMapper {

    @Override
    public CityDto toCityDto(City city) {
        if ( city == null ) {
            return null;
        }

        CityDto cityDto = new CityDto();

        cityDto.setId( city.getId() );
        cityDto.setName( city.getName() );
        cityDto.setDeliveryTime( city.getDeliveryTime() );

        return cityDto;
    }

    @Override
    public City toCity(CityDto cityDto) {
        if ( cityDto == null ) {
            return null;
        }

        City city = new City();

        city.setId( cityDto.getId() );
        city.setName( cityDto.getName() );
        city.setDeliveryTime( cityDto.getDeliveryTime() );

        return city;
    }
}
