package com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects;

public record Document(DocumentType documentType, String documentNumber) {

    private static final String NOT_BLANK_DOCUMENT_NUMBER_MESSAGE_KEY = "core.error.documentNumber.notBlank";
    private static final String NOT_NULL_DOCUMENT_TYPE_MESSAGE_KEY = "core.error.documentType.notNull";

    public Document {
        if (documentType == null) {
            throw new IllegalArgumentException(NOT_NULL_DOCUMENT_TYPE_MESSAGE_KEY);
        }
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new IllegalArgumentException(NOT_BLANK_DOCUMENT_NUMBER_MESSAGE_KEY);
        }
    }

    public Document(String type, String number) {
        this(DocumentType.valueOf(type.toUpperCase()), number);
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }
}
