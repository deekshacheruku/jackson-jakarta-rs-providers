package tools.jackson.jakarta.rs.json;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.Method;

import jakarta.ws.rs.core.MediaType;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import tools.jackson.jakarta.rs.annotation.JacksonFeatures;
import tools.jackson.jakarta.rs.json.JacksonJsonProvider;

/**
 * Tests for addition of {@link JacksonFeatures}.
 */
public class TestJacksonFeatures extends JakartaRSTestBase
{
    static class Bean {
        public int a = 3;
    }

    @JacksonFeatures(serializationEnable={ SerializationFeature.WRAP_ROOT_VALUE })
    public void writeConfig() { }
        
    @JacksonFeatures(deserializationDisable={ DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES })
    public void readConfig() { }

    // Also, let's check that we can bundle annotations
    @JacksonAnnotationsInside
    @JacksonFeatures(serializationEnable={ SerializationFeature.WRAP_ROOT_VALUE })
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface FeatureBundle { }

    @FeatureBundle // should work as if all annotations from FeatureBundle were directly added
    public void writeConfig2() { }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    public void testWriteConfigs() throws Exception
    {
        JacksonJsonProvider prov = new JacksonJsonProvider();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Bean bean = new Bean();
        Method m = getClass().getDeclaredMethod("writeConfig");
        JacksonFeatures feats = m.getAnnotation(JacksonFeatures.class);
        assertNotNull(feats); // just a sanity check

        // when wrapping enabled, we get:
        prov.writeTo(bean, bean.getClass(), bean.getClass(), new Annotation[] { feats },
                MediaType.APPLICATION_JSON_TYPE, null, out);
        assertEquals("{\"Bean\":{\"a\":3}}", out.toString("UTF-8"));

        // but without, not:
        out.reset();
        prov.writeTo(bean, bean.getClass(), bean.getClass(), new Annotation[] { },
                MediaType.APPLICATION_JSON_TYPE, null, out);
        assertEquals("{\"a\":3}", out.toString("UTF-8"));
    }

    public void testWriteConfigsViaBundle() throws Exception
    {
        JacksonJsonProvider prov = new JacksonJsonProvider();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Bean bean = new Bean();
        Method m = getClass().getDeclaredMethod("writeConfig2");
        // should still enable root-wrapping
        prov.writeTo(bean, bean.getClass(), bean.getClass(), m.getAnnotations(),
                MediaType.APPLICATION_JSON_TYPE, null, out);
        assertEquals("{\"Bean\":{\"a\":3}}", out.toString("UTF-8"));
    }
    
    // [Issue-2], deserialization
    public void testReadConfigs() throws Exception
    {
        JacksonJsonProvider prov = new JacksonJsonProvider();
        Method m = getClass().getDeclaredMethod("readConfig");
        JacksonFeatures feats = m.getAnnotation(JacksonFeatures.class);
        assertNotNull(feats); // just a sanity check

        // ok: here let's verify that we can disable exception throwing unrecognized things
        @SuppressWarnings("unchecked")
        Class<Object> raw = (Class<Object>)(Class<?>)Bean.class;
        Object ob = prov.readFrom(raw, raw,
                new Annotation[] { feats },
                MediaType.APPLICATION_JSON_TYPE, null,
                new ByteArrayInputStream("{ \"foobar\" : 3 }".getBytes("UTF-8")));
        assertNotNull(ob);

        // but without setting, get the exception
        try {
            prov.readFrom(raw, raw,
                    new Annotation[] { },
                    MediaType.APPLICATION_JSON_TYPE, null,
                    new ByteArrayInputStream("{ \"foobar\" : 3 }".getBytes("UTF-8")));
            fail("Should have caught an exception");
        } catch (UnrecognizedPropertyException e) {
            verifyException(e, "Unrecognized property");
        }
    }
    
}