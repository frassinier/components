// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class SnowflakeSink extends SnowflakeSourceOrSink implements Sink {

    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider().getI18nMessages(SnowflakeSink.class);

    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(SnowflakeSink.class);

    public SnowflakeSink() {
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResult validate = super.validate(container);
        // also check that the properties is the right type
        if (validate.getStatus() != Result.ERROR) {
            if (!(properties instanceof TSnowflakeOutputProperties)) {
                return new ValidationResult(Result.ERROR, i18nMessages
                        .getMessage("debug.wrongPropertiesType", TSnowflakeOutputProperties.class.getCanonicalName()));
            } // else this is the right type
        } // else already an ERROR
        return validate;
    }

    @Override
    public SnowflakeWriteOperation createWriteOperation() {
        return new SnowflakeWriteOperation(this);
    }

    /**
     * this should never be called before {@link #validate(RuntimeContainer)} is called but this should not be the case
     * anyway cause validate is called before the pipeline is created.
     *
     * @return the properties
     */
    public TSnowflakeOutputProperties getSnowflakeOutputProperties() {
        return (TSnowflakeOutputProperties) properties;
    }
}
