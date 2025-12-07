package ru.goth.domain.mappers;

import javax.annotation.processing.Generated;
import ru.goth.domain.dto.MythologyDto;
import ru.goth.domain.entities.Mythology;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-07T23:16:28+0300",
    comments = "version: 1.6.0, compiler: javac, environment: Java 17.0.14 (Amazon.com Inc.)"
)
public class MythologyMapperImpl implements MythologyMapper {

    @Override
    public MythologyDto toMythologyDto(Mythology mythology) {
        if ( mythology == null ) {
            return null;
        }

        MythologyDto mythologyDto = new MythologyDto();

        mythologyDto.setId( mythology.getId() );
        mythologyDto.setName( mythology.getName() );

        return mythologyDto;
    }

    @Override
    public Mythology toMythology(MythologyDto mythologyDto) {
        if ( mythologyDto == null ) {
            return null;
        }

        Mythology mythology = new Mythology();

        mythology.setId( mythologyDto.getId() );
        mythology.setName( mythologyDto.getName() );

        return mythology;
    }
}
