// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.dropbox.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.dropbox.runtime.reader.DropboxGetBytesReader;
import org.talend.components.dropbox.runtime.reader.DropboxGetReader;
import org.talend.components.dropbox.tdropboxget.TDropboxGetProperties;
import org.talend.daikon.properties.ValidationResult;

/**
 * Unit-tests for {@link DropboxGetSource}
 */
public class DropboxGetSourceTest extends DropboxRuntimeTestBase {

    /**
     * Prepares required instances for tests
     */
    @Before
    public void setUp() {
        setupGetFileSchema();
        setupConnectionProperties();
        setupGetProperties();
    }

    /**
     * Checks {@link DropboxGetSource#initialize()} sets property values from {@link TDropboxGetProperties} without changing them
     */
    @Test
    public void testInitialize() {
        DropboxGetSource source = new DropboxGetSource();
        ValidationResult validation = source.initialize(container, getProperties);
        assertEquals(ValidationResult.OK, validation);

        boolean saveAsFile = source.isSaveAsFile();
        assertFalse(saveAsFile);
        String saveTo = source.getSaveTo();
        assertEquals("d:/test/TestFile.txt", saveTo);
        Schema actualSchema = source.getSchema();
        assertEquals(getFileSchema, actualSchema);
        boolean chunkMode = source.isChunkMode();
        assertTrue(chunkMode);
        int chunkSize = source.getChunkSize();
        assertEquals(8192, chunkSize);
    }

    /**
     * Checks {@link DropboxGetSource#createReader()} creates reader of type {@link DropboxGetReader}
     */
    @Test
    public void testCreateReader() {
        DropboxGetSource source = new DropboxGetSource();
        source.initialize(container, getProperties);
        Reader<IndexedRecord> reader = source.createReader(container);
        assertThat(reader, is(instanceOf(DropboxGetBytesReader.class)));
    }

}
