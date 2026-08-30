package com.andeva.atelier.platform.fleet.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.fleet.domain.model.valueobjects.AppointmentSummary;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Attribute Converter for AppointmentSummary value object. This converter handles the conversion between the AppointmentSummary type and its String representation for database storage. It ensures that when an AppointmentSummary is persisted, its value is stored as a String, and when retrieved, it is converted back to an AppointmentSummary instance.
 * The converter is set to autoApply, meaning it will be automatically applied to all attributes of type AppointmentSummary in the JPA entities without needing to specify it explicitly on each attribute.
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class AppointmentSummaryAttributeConverter implements AttributeConverter<AppointmentSummary, String> {

    /**
     * Converts an AppointmentSummary entity attribute to its String representation for database storage. If the attribute is null, it returns null; otherwise, it returns the value of the AppointmentSummary.
     * @param attribute  the entity attribute value to be converted
     * @return the String representation of the AppointmentSummary for database storage, or null if the attribute is null
     */
    @Override
    public String convertToDatabaseColumn(AppointmentSummary attribute) {
        return (attribute == null) ? null : attribute.value();
    }

    /**
     * Converts a String from the database column back to an AppointmentSummary entity attribute. If the database value is null or blank, it returns null; otherwise, it creates a new AppointmentSummary instance using the String value.
     * @param dbData  the data from the database column to be converted
     * @return an AppointmentSummary instance created from the database String value, or null if the database value is null or blank
     */
    @Override
    public AppointmentSummary convertToEntityAttribute(String dbData) {
        return (dbData == null || dbData.isBlank()) ? null : new AppointmentSummary(dbData);
    }
}
