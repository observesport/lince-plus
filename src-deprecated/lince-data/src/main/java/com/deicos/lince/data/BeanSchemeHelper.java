package com.deicos.lince.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * com.deicos.lince.data
 * Class BeanSchemeHelper
 * @author berto (alberto.soto@gmail.com). 13/04/2018
 *
 * Helper via jackson module to generate the jsonscheme for any bean
 *
 * TODO: 2bedone
 */
public class BeanSchemeHelper {
    protected static final Logger log = LoggerFactory.getLogger(BeanSchemeHelper.class);
    public static String generateJsonScheme(Class name){
        return null;
        /*
        com.fasterxml.jackson.module.jsonSchema.JsonSchema schema =null;
        try{
            ObjectMapper mapper = new ObjectMapper();
            SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
            mapper.acceptJsonFormatVisitor(name, visitor);
            schema = visitor.finalSchema();
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));
        }catch (Exception e){
            log.error("Exception generating json scheme for bean",e);
        }
        return schema;
        */
    }

}
