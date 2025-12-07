package ru.goth.domain.mappers;

import javax.annotation.processing.Generated;
import ru.goth.domain.dto.StepDto;
import ru.goth.domain.entities.Step;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-07T23:16:28+0300",
    comments = "version: 1.6.0, compiler: javac, environment: Java 17.0.14 (Amazon.com Inc.)"
)
public class StepMapperImpl implements StepMapper {

    @Override
    public StepDto toStepDto(Step step) {
        if ( step == null ) {
            return null;
        }

        StepDto stepDto = new StepDto();

        stepDto.setId( step.getId() );
        stepDto.setName( step.getName() );
        stepDto.setDescription( step.getDescription() );

        return stepDto;
    }

    @Override
    public Step toStep(StepDto stepDto) {
        if ( stepDto == null ) {
            return null;
        }

        Step step = new Step();

        step.setId( stepDto.getId() );
        step.setName( stepDto.getName() );
        step.setDescription( stepDto.getDescription() );

        return step;
    }
}
