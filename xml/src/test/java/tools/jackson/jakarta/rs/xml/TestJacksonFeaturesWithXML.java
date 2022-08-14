package tools.jackson.jakarta.rs.xml;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.Method;

import jakarta.ws.rs.core.MediaType;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import tools.jackson.jakarta.rs.annotation.JacksonFeatures;
import tools.jackson.jakarta.rs.xml.JacksonXMLProvider;

/**
 * Tests for addition of {@link JacksonFeatures}.
 */
public class TestJacksonFeaturesWithXML extends JakartaRSTestBase
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
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    public void testWriteConfigs() throws Exception
    {
        JacksonXMLProvider prov = new JacksonXMLProvider();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Bean bean = new Bean();
        Method m = getClass().getDeclaredMethod("writeConfig");
        JacksonFeatures feats = m.getAnnotation(JacksonFeatures.class);
        assertNotNull(feats); // just a sanity check

        // 09-Oct-2013, tatu: As of 2.3, XML backend does NOT add extra wrapping
        //   any more: it is only added to JSON where it is needed; but not
        //   to XML which always basically uses wrapping.
        try {
            prov.writeTo(bean, bean.getClass(), bean.getClass(), new Annotation[] { feats },
                    MediaType.APPLICATION_JSON_TYPE, null, out);
        } catch (Exception e) {
            throw _unwrap(e);
        }
        assertEquals("<Bean><a>3</a></Bean>", out.toString("UTF-8"));

        // but without, not:
        out.reset();
        prov.writeTo(bean, bean.getClass(), bean.getClass(), new Annotation[] { },
                MediaType.APPLICATION_JSON_TYPE, null, out);
        assertEquals("<Bean><a>3</a></Bean>", out.toString("UTF-8"));
    }
    
    public void testWriteConfigsViaBundle() throws Exception
    {
        JacksonXMLProvider prov = new JacksonXMLProvider();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Bean bean = new Bean();
        Method m = getClass().getDeclaredMethod("writeConfig2");
        // should still enable root-wrapping
        prov.writeTo(bean, bean.getClass(), bean.getClass(), m.getAnnotations(),
                MediaType.APPLICATION_JSON_TYPE, null, out);
        // as per above, no extra wrapping for XML, in 2.3:
        assertEquals("<Bean><a>3</a></Bean>", out.toString("UTF-8"));
    }

    public void testReadConfigs() throws Exception
    {
        JacksonXMLProvider prov = new JacksonXMLProvider();
        Method m = getClass().getDeclaredMethod("readConfig");
        JacksonFeatures feats = m.getAnnotation(JacksonFeatures.class);
        assertNotNull(feats); // just a sanity check

        // ok: here let's verify that we can disable exception throwing unrecognized things
        @SuppressWarnings("unchecked")
        Class<Object> raw = (Class<Object>)(Class<?>)Bean.class;
        Object ob = prov.readFrom(raw, raw,
                new Annotation[] { feats },
                MediaType.APPLICATION_JSON_TYPE, null,
                new ByteArrayInputStream("<Bean><foobar>3</foobar></Bean>".getBytes("UTF-8")));
        assertNotNull(ob);

        // but without setting, get the exception
        try {
            prov.readFrom(raw, raw,
                    new Annotation[] { },
                    MediaType.APPLICATION_JSON_TYPE, null,
                    new ByteArrayInputStream("<Bean><foobar>3</foobar></Bean>".getBytes("UTF-8")));
            fail("Should have caught an exception");
        } catch (UnrecognizedPropertyException e) {
            verifyException(e, "Unrecognized property \"foobar\"");
        }
    }

    protected Exception _unwrap(Exception e) {
        while (e.getCause() instanceof Exception) {
            e = (Exception) e.getCause();
        }
        return e;
    }
}