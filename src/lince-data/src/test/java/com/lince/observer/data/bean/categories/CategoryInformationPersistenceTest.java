package com.lince.observer.data.bean.categories;

import com.lince.observer.data.LinceDataConstants;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * LO-345: lince-server stores observation registers with Spring Data MongoDB. A register holding a
 * CategoryInformation could be written but never read back: Spring Data bound the stored fields to
 * the only constructor, CategoryInformation(Criteria parent, String value), passing the Integer
 * parent id where a Criteria was expected (MappingInstantiationException / ClassCastException), so
 * the project could no longer be loaded or saved.
 */
class CategoryInformationPersistenceTest {

    private final MappingMongoConverter converter = converter();

    private static MappingMongoConverter converter() {
        MongoMappingContext context = new MongoMappingContext();
        context.afterPropertiesSet();
        MappingMongoConverter converter = new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, context);
        converter.afterPropertiesSet();
        return converter;
    }

    @Test
    void readsBackTheDocumentShapeStoredInProduction() {
        // Same keys and types as the node that broke production on 2026-09-28, inside
        // observationRegister[].registerData[].register[] (parent id 39; values are made up).
        // Spring Data stores the "id" property as "_id", also in nested documents.
        Document stored = new Document("_id", 39 * LinceDataConstants.CATEGORY_INFO_ID_MULTIPLIER)
                .append("code", "CRIT")
                .append("name", "Criterion name")
                .append("nodeInformation", "free text typed by the observer")
                .append("level", 1)
                .append("type", "info")
                .append("parent", 39)
                .append("_class", CategoryInformation.class.getName());

        CategoryData read = converter.read(CategoryData.class, stored);

        CategoryInformation info = assertInstanceOf(CategoryInformation.class, read);
        assertEquals(39 * LinceDataConstants.CATEGORY_INFO_ID_MULTIPLIER, info.getId());
        assertEquals(39, info.getParent());
        assertEquals("free text typed by the observer", info.getNodeInformation());
        assertEquals("Criterion name", info.getName());
        assertEquals("CRIT", info.getCode());
    }

    @Test
    void survivesAWriteReadRoundTrip() {
        Criteria criteria = new Criteria(39, "Criterion name");
        criteria.setCode("CRIT");
        CategoryInformation original = new CategoryInformation(criteria, "free text typed by the observer");

        Document written = new Document();
        converter.write(original, written);
        CategoryInformation read = converter.read(CategoryInformation.class, written);

        assertEquals(original.getId(), read.getId());
        assertEquals(original.getParent(), read.getParent());
        assertEquals(original.getNodeInformation(), read.getNodeInformation());
        assertEquals(original.getName(), read.getName());
        assertEquals(original.getCode(), read.getCode());
    }
}
