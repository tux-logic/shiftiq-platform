package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.operations.domain.model.valueobjects.TaskDescription;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TaskDescriptionAttributeConverter implements AttributeConverter<TaskDescription, String> {

    /**
     * Converts a TaskDescription entity attribute to its database column representation (String).
     * @param attribute  the entity attribute value to be converted
     * @return the database column representation of the TaskDescription, or null if the attribute is null
     */
    @Override
    public String convertToDatabaseColumn(TaskDescription attribute) {
        return (attribute == null) ? null : attribute.value();
    }

    /**
     * Converts a database column value (String) back to a TaskDescription entity attribute.
     * @param dbData  the data from the database column to be converted
     * @return a TaskDescription entity attribute created from the database column value, or null if the dbData is null or blank
     */
    @Override
    public TaskDescription convertToEntityAttribute(String dbData) {
        return (dbData == null || dbData.isBlank()) ? null : new TaskDescription(dbData);
    }
}
