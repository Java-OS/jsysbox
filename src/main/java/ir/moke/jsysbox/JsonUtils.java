package ir.moke.jsysbox;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class JsonUtils {
    private static final ObjectMapper om = new ObjectMapper();

    static {
        om.setVisibility(om.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false);
        om.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false);
        om.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, false);
        om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String toJson(Object o) {
        try {
            return om.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T toObject(String str, Class<T> clazz) {
        try {
            return om.readValue(str, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T toObject(byte[] bytes, Class<T> clazz) {
        return toObject(new String(bytes), clazz);
    }
    public static <T> T toObject(File file, Class<T> clazz) {
        try {
            return om.readValue(file, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static HashMap<String, Object> toMap(String str) {
        try {
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {
            };
            return om.readValue(str, typeRef);
        } catch (IOException e) {
            throw new JSysboxException(e);
        }
    }

    public static void writeToFile(File file, Object object) {
        try {
            om.writeValue(file, object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isNullOrEmpty(JsonNode node) {
        return node.isNull() && node.isEmpty();
    }
}
