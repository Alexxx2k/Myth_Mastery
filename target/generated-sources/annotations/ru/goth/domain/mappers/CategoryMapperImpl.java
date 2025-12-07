package ru.goth.domain.mappers;

import javax.annotation.processing.Generated;
import ru.goth.domain.dto.CategoryDto;
import ru.goth.domain.entities.Category;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-07T23:16:28+0300",
    comments = "version: 1.6.0, compiler: javac, environment: Java 17.0.14 (Amazon.com Inc.)"
)
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public CategoryDto toCategoryDto(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryDto categoryDto = new CategoryDto();

        categoryDto.setId( category.getId() );
        categoryDto.setName( category.getName() );
        categoryDto.setHazard( category.getHazard() );
        categoryDto.setRarity( category.getRarity() );

        return categoryDto;
    }

    @Override
    public Category toCategory(CategoryDto categoryDto) {
        if ( categoryDto == null ) {
            return null;
        }

        Category category = new Category();

        category.setId( categoryDto.getId() );
        category.setName( categoryDto.getName() );
        category.setHazard( categoryDto.getHazard() );
        category.setRarity( categoryDto.getRarity() );

        return category;
    }
}
