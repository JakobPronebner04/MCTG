package utils.json;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONParser {
    public <T> T readValue(String jsonString, Class<T> objClass) {
        try {
            T obj = objClass.getDeclaredConstructor().newInstance();

            jsonString = jsonString.trim();
            if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
                jsonString = jsonString.substring(1, jsonString.length() - 1);
            }

            String[] keyValuePairs = jsonString.split(",");

            for (String pair : keyValuePairs) {
                String[] entry = pair.split(":", 2);
                if (entry.length != 2) continue;

                String fieldName = entry[0].trim();
                String fieldValue = entry[1].trim();

                if (fieldName.startsWith("\"") && fieldName.endsWith("\"")) {
                    fieldName = fieldName.substring(1, fieldName.length() - 1);
                }
                if (fieldValue.startsWith("\"") && fieldValue.endsWith("\"")) {
                    fieldValue = fieldValue.substring(1, fieldValue.length() - 1);
                }

                Field field = findField(objClass, fieldName);
                if (field == null) {
                    System.err.println("Field not found: " + fieldName);
                    continue;
                }
                field.setAccessible(true);

                setFieldValue(obj, field, fieldValue);
            }
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // JSON-Array zu Liste von Objekten
    public <T> List<T> readValueAsList(String jsonString, Class<T> objClass) {
        List<T> list = new ArrayList<>();
        try {
            jsonString = jsonString.trim();
            if (jsonString.startsWith("[") && jsonString.endsWith("]")) {
                jsonString = jsonString.substring(1, jsonString.length() - 1); // Entfernt eckige Klammern
            }

            String[] objects = jsonString.split("},\\s*\\{");
            for (String object : objects) {
                object = object.replaceFirst("^\\{", "").replaceFirst("\\}$", ""); // Entfernt geschweifte Klammern
                T parsedObject = readValue("{" + object + "}", objClass);
                if (parsedObject != null) {
                    list.add(parsedObject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // JSON-Objekt zu Map
    public Map<String, String> readValueAsMap(String jsonString) {
        Map<String, String> map = new HashMap<>();
        try {
            jsonString = jsonString.trim().replaceAll("[{}\"]", ""); // Entfernt Klammern und Anführungszeichen
            String[] keyValuePairs = jsonString.split(","); // Teilt die JSON-Paare

            for (String pair : keyValuePairs) {
                String[] entry = pair.split(":");
                if (entry.length == 2) {
                    map.put(entry[0].trim(), entry[1].trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    // Hilfsmethode: Findet ein Feld (Case-Insensitive)
    private Field findField(Class<?> objClass, String fieldName) {
        for (Field field : objClass.getDeclaredFields()) {
            if (field.getName().equalsIgnoreCase(fieldName)) {
                return field;
            }
        }
        return null; // Feld nicht gefunden
    }

    // Hilfsmethode: Setzt den Wert für ein Feld
    private void setFieldValue(Object obj, Field field, String value) throws IllegalAccessException {
        Class<?> fieldType = field.getType();
        if (fieldType == String.class) {
            field.set(obj, value);
        } else if (fieldType == int.class || fieldType == Integer.class) {
            field.set(obj, Integer.parseInt(value));
        } else if (fieldType == double.class || fieldType == Double.class) {
            field.set(obj, Double.parseDouble(value));
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            field.set(obj, Boolean.parseBoolean(value));
        } else {
            System.err.println("Unsupported field type: " + fieldType.getName());
        }
    }

    // JSON-Array zu Liste von Strings
    public List<String> readValueAsStringList(String jsonString) {
        List<String> list = new ArrayList<>();
        try {
            jsonString = jsonString.trim();
            if (jsonString.startsWith("[") && jsonString.endsWith("]")) {
                jsonString = jsonString.substring(1, jsonString.length() - 1); // Entfernt eckige Klammern
            }

            String[] items = jsonString.split(",\\s*"); // Teilt die Elemente anhand von Kommas
            for (String item : items) {
                list.add(item.trim().replaceAll("^\"|\"$", "")); // Entfernt Anführungszeichen
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Objekte oder Listen in JSON-Format umwandeln
    public String writeValueAsString(Object obj) {
        try {
            StringBuilder jsonBuilder = new StringBuilder();
            if (obj instanceof List<?>) {
                List<?> list = (List<?>) obj;
                jsonBuilder.append("[");
                for (int i = 0; i < list.size(); i++) {
                    jsonBuilder.append(writeObjectAsString(list.get(i)));
                    if (i < list.size() - 1) {
                        jsonBuilder.append(",");
                    }
                }
                jsonBuilder.append("]");
            } else {
                jsonBuilder.append(writeObjectAsString(obj));
            }
            return jsonBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}";
    }

    private String writeObjectAsString(Object obj) {
        try {
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            Field[] fields = obj.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                jsonBuilder.append("\"").append(field.getName()).append("\":");
                Object value = field.get(obj);
                if (value instanceof String) {
                    jsonBuilder.append("\"").append(value).append("\"");
                } else {
                    jsonBuilder.append(value);
                }
                if (i < fields.length - 1) {
                    jsonBuilder.append(",");
                }
            }
            jsonBuilder.append("}");
            return jsonBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}";
    }
}