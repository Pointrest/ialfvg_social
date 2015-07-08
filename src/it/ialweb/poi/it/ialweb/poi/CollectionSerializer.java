package it.ialweb.poi.it.ialweb.poi;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
 
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
 
public class CollectionSerializer<E> implements
JsonSerializer<Collection<E>>, JsonDeserializer<Collection<E>>{
 
    public JsonElement serialize(Collection<E> collection, Type type,
            JsonSerializationContext context) {
        JsonArray result = new JsonArray();
        for(E item : collection){
            result.add(context.serialize(item));
        }
        return new JsonPrimitive(result.toString());
    }
 
     
    @SuppressWarnings("unchecked")
    public Collection<E> deserialize(JsonElement element, Type type,
            JsonDeserializationContext context) throws JsonParseException {
        JsonArray items = (JsonArray) new JsonParser().parse(element.getAsString());
        ParameterizedType deserializationCollection = ((ParameterizedType) type);
        Type collectionItemType = deserializationCollection.getActualTypeArguments()[0];
        Collection<E> list = null;
         
        try {
            list = (Collection<E>)((Class<?>) deserializationCollection.getRawType()).newInstance();
            for(JsonElement e : items){
                list.add((E)context.deserialize(e, collectionItemType));
            }
        } catch (InstantiationException e) {
            throw new JsonParseException(e);
        } catch (IllegalAccessException e) {
            throw new JsonParseException(e);
        }
         
        return list;
    }
}