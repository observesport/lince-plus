package com.lince.observer.desktop.base.common;

import com.google.gson.Gson;
import org.json.JSONObject;

/**
 * coachgate-core-root
 * com.deicos.coachgate.core.common
 * @author berto (alberto.soto@gmail.com)in 13/02/2016.
 * Description:
 */
@Deprecated
public class JsonFieldHelper {

    /*private final TypeToken<T> typeToken = new TypeToken<T>();
    private final Type type = typeToken.getType(); // or getRawType() to return Class<? super T>

    public Type getType() {
        return type;
    }*/

    public static <T> T getValue(JSONObject item, Class<T> classItem) {
        return new Gson().fromJson(String.valueOf(item), classItem);
    }

    public static <T> JSONObject castValue(T item, Class<T> classItem) {
        return new JSONObject(new Gson().toJson(item, classItem));
    }

    /*public static <T> T getValue(JSONArray item, Class<T> classItem) {
        //return new Gson().fromJson(String.valueOf(content), new TypeToken<ArrayList<EventContentInfo>>() {}.getType());
        return null;
    }

    public static <T> JSONArray castArrayValue(T item) {
        //return new JSONObject(new Gson().toJson(item, classItem));
        return new JSONArray(new Gson().toJson(item, new TypeToken<ArrayList<T>>() {
        }.getType()));
    }*/


}
