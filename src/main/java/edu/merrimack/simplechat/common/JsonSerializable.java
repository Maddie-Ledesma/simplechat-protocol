package edu.merrimack.simplechat.common;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONType;

import java.io.InvalidObjectException;

/**
 * Adapter interface that binds project classes to merrimackutil's JSONSerializable contract.
 */
public interface JsonSerializable extends JSONSerializable {

    /**
     * Converts this object to its JSON representation.
     */
    @Override
    JSONType toJSONType();

    /**
     * Populates fields from the provided JSON structure.
     */
    @Override
    void deserialize(JSONType jsonType) throws InvalidObjectException;

    /**
     * Serializes the object to a JSON string.
     */
    @Override
    default String serialize() {
        return toJSONType().toJSON();
    }
}
