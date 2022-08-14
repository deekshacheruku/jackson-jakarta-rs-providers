package tools.jackson.jakarta.rs.cbor;

import tools.jackson.databind.*;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.dataformat.cbor.CBORFactory;
import tools.jackson.dataformat.cbor.databind.CBORMapper;
import tools.jackson.jakarta.rs.cfg.MapperConfiguratorBase;

/**
 * Helper class used to encapsulate details of configuring an
 * {@link ObjectMapper} instance to be used for data binding, as
 * well as accessing it.
 */
public class CBORMapperConfigurator
    extends MapperConfiguratorBase<CBORMapperConfigurator, CBORMapper>
{
    public CBORMapperConfigurator(CBORMapper mapper,
            AnnotationIntrospector aiOverride)
    {
        super(mapper, aiOverride);
    }

    /*
    /**********************************************************************
    /* Abstract method impls
    /**********************************************************************
     */

    @Override
    protected MapperBuilder<?,?> mapperBuilder() {
        return CBORMapper.builder(new CBORFactory());
    }
}