package io.github.openunirest.request;


import io.github.openunirest.http.*;
import io.github.openunirest.http.exceptions.UnirestException;
import io.github.openunirest.http.options.Option;
import io.github.openunirest.http.options.Options;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import static io.github.openunirest.http.BodyData.from;


class ResponseBuilder {

    public HttpResponse<JsonNode> asJson(org.apache.http.HttpResponse response) {
        return new HttpResponseImpl<>(response, from(response.getEntity(), b -> toJson(b)));
    }

    public HttpResponse<InputStream> asBinary(org.apache.http.HttpResponse response){
        return new HttpResponseImpl<>(response, from(response.getEntity(), BodyData::getRawInput));
    }

    public <T> HttpResponse<T> asObject(org.apache.http.HttpResponse response, Class<? extends T> aClass) {
        return new HttpResponseImpl<>(response, from(response.getEntity(), b -> toObject(b, aClass)));
    }

    public <T> HttpResponse<T> asObject(org.apache.http.HttpResponse response, GenericType<T> genericType) {
        return new HttpResponseImpl<>(response, from(response.getEntity(), b -> toObject(b, genericType)));
    }

    public HttpResponse<String> asString(org.apache.http.HttpResponse response) {
        return new HttpResponseImpl<>(response, from(response.getEntity(), this::toString));
    }

    private <T> T toObject(BodyData<T> b, GenericType<T> genericType) {
        ObjectMapper o = getObjectMapper();
        return o.readValue(toString(b), genericType);
    }

    private <T> T toObject(BodyData<T> b, Class<? extends T> aClass) {
        ObjectMapper o = getObjectMapper();
        return o.readValue(toString(b), aClass);
    }

    private String toString(BodyData b) {
        try {
            return new String(b.getRawBytes(), b.getCharset());
        } catch (UnsupportedEncodingException e) {
            throw new ParsingException(e);
        }
    }

    private JsonNode toJson(BodyData<JsonNode> b) {
        String jsonString = toString(b);
        return new JsonNode(jsonString);
    }

    private ObjectMapper getObjectMapper() {
        return Options.tryGet(Option.OBJECT_MAPPER, ObjectMapper.class)
                .orElseThrow(() -> new UnirestException("No Object Mapper Configured. Please configure one with Unirest.setObjectMapper"));
    }

    public static class ParsingException extends RuntimeException {

        public ParsingException(Exception e){
            super(e);
        }
    }
}
