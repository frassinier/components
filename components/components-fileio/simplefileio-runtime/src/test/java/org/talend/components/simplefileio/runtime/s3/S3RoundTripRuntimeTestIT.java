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
package org.talend.components.simplefileio.runtime.s3;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.talend.components.test.RecordSetUtil.getSimpleTestData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.adapter.beam.transform.DirectCollector;
import org.talend.components.simplefileio.SimpleFileIODatasetProperties;
import org.talend.components.simplefileio.SimpleFileIOFormat;
import org.talend.components.simplefileio.s3.S3DatasetProperties;
import org.talend.components.simplefileio.s3.input.S3InputProperties;
import org.talend.components.simplefileio.s3.output.S3OutputProperties;
import org.talend.components.test.RecordSet;
import org.talend.daikon.java8.Consumer;

import com.talend.shaded.com.amazonaws.services.s3.model.ObjectMetadata;

/**
 * Unit tests for {@link S3InputRuntime} and {@link S3OutputRuntime}, focusing on the use cases where data is written by
 * an output component, and then read by an input component.
 *
 * The input component should be able to read all files generated by the output component.
 */
public class S3RoundTripRuntimeTestIT {

    @Rule
    public TestPipeline pWrite = TestPipeline.create();

    @Rule
    public TestPipeline pRead = TestPipeline.create();

    /** Set up credentials for integration tests. */
    @Rule
    public S3TestResource s3 = S3TestResource.of();

    /**
     * Tests a round-trip on the data when writing to the data source using the given output properties, then
     * subsequently reading using the given input properties. This is the equivalent of two pipeline jobs.
     *
     * @param initialData The initial data set to write, then read.
     * @param outputProps The properties used to create the output runtime.
     * @param inputProps The properties used to create the input runtime.
     * @return The data returned from the round-trip.
     */
    protected List<IndexedRecord> runRoundTripPipelines(List<IndexedRecord> initialData, S3OutputProperties outputProps,
            S3InputProperties inputProps) {
        // Create the runtimes.
        S3OutputRuntime outputRuntime = new S3OutputRuntime();
        outputRuntime.initialize(null, outputProps);
        S3InputRuntime inputRuntime = new S3InputRuntime();
        inputRuntime.initialize(null, inputProps);

        // Create a pipeline to write the records to the output.
        {
            PCollection<IndexedRecord> input = pWrite.apply(Create.<IndexedRecord> of(initialData));
            input.apply(outputRuntime);
            pWrite.run().waitUntilFinish();
        }

        // Read the records that were written.
        try (DirectCollector<IndexedRecord> collector = DirectCollector.of()) {
            PCollection<IndexedRecord> input = pRead.apply(inputRuntime);
            input.apply(collector);
            pRead.run().waitUntilFinish();

            // Return the list of records from the round trip.
            return collector.getRecords();
        }
    }

    protected List<IndexedRecord> getSample(S3DatasetProperties datasetProperties) {
        S3DatasetRuntime datasetRuntime = new S3DatasetRuntime();
        datasetRuntime.initialize(null, datasetProperties);
        final List<IndexedRecord> samples = new ArrayList<>();
        datasetRuntime.getSample(10, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord indexedRecord) {
                samples.add(indexedRecord);
            }
        });
        return samples;
    }

    protected Schema getSchema(S3DatasetProperties datasetProperties) {
        S3DatasetRuntime datasetRuntime = new S3DatasetRuntime();
        datasetRuntime.initialize(null, datasetProperties);
        return datasetRuntime.getSchema();
    }

    public void test_noEncryption(S3DatasetProperties datasetProps) throws IOException {
        // The file that we will be creating.
        RecordSet rs = getSimpleTestData(0);

        // Configure the components.
        S3OutputProperties outputProps = new S3OutputProperties("out");
        outputProps.setDatasetProperties(datasetProps);
        S3InputProperties inputProps = new S3InputProperties("in");
        inputProps.setDatasetProperties(datasetProps);

        List<IndexedRecord> actual = runRoundTripPipelines(rs.getAllData(), outputProps, inputProps);

        List<IndexedRecord> expected = rs.getAllData();
        assertThat(actual, containsInAnyOrder(expected.toArray()));

        List<IndexedRecord> samples = getSample(datasetProps);
        assertThat(samples, containsInAnyOrder(expected.toArray()));

        Schema schema = getSchema(datasetProps);
        assertEquals(expected.get(0).getSchema(), schema);
    }

    /**
     * Basic Avro test.
     */
    @Test
    public void testAvro_noEncryption() throws IOException {
        S3DatasetProperties datasetProps = s3.createS3DatasetProperties();
        datasetProps.format.setValue(SimpleFileIOFormat.AVRO);
        test_noEncryption(datasetProps);

        // Get some object metadata from the results.
        ObjectMetadata md = s3.getObjectMetadata(datasetProps);
        assertThat(md.getSSEAlgorithm(), nullValue());
        assertThat(md.getSSEAwsKmsKeyId(), nullValue());
    }

    /**
     * Basic Csv test.
     */
    @Test
    @Ignore("columns name different, can't editable")
    public void testCsv_noEncryption() throws IOException {
        S3DatasetProperties datasetProps = s3.createS3DatasetProperties();
        datasetProps.format.setValue(SimpleFileIOFormat.CSV);
        datasetProps.recordDelimiter.setValue(SimpleFileIODatasetProperties.RecordDelimiterType.LF);
        datasetProps.fieldDelimiter.setValue(SimpleFileIODatasetProperties.FieldDelimiterType.SEMICOLON);
        test_noEncryption(datasetProps);
    }

    /**
     * Basic Parquet test.
     */
    @Test
    public void testParquet_noEncryption() throws IOException {
        S3DatasetProperties datasetProps = s3.createS3DatasetProperties();
        datasetProps.format.setValue(SimpleFileIOFormat.PARQUET);
        test_noEncryption(datasetProps);
    }

    /**
     * Basic Avro test with sseKmsEncryption.
     */
    @Test
    public void testAvro_sseKmsEncryption() throws IOException {
        S3DatasetProperties datasetProps = s3.createS3DatasetProperties(true, false);
        datasetProps.format.setValue(SimpleFileIOFormat.AVRO);
        test_noEncryption(datasetProps);

        // Get some object metadata from the results.
        ObjectMetadata md = s3.getObjectMetadata(datasetProps);
        assertThat(md.getSSEAlgorithm(), is("aws:kms"));
        assertThat(md.getSSEAwsKmsKeyId(), is(datasetProps.kmsForDataAtRest.getValue()));
    }

    /**
     * Basic Avro test with cseKmsEncryption.
     */
    @Ignore("cse not yet supported.")
    @Test
    public void testAvro_cseKmsEncryption() throws IOException {
        S3DatasetProperties datasetProps = s3.createS3DatasetProperties(false, true);
        datasetProps.format.setValue(SimpleFileIOFormat.AVRO);
        test_noEncryption(datasetProps);
    }

    /**
     * Basic Avro test with sseKmsEncryption.
     */
    @Ignore("cse not yet supported.")
    @Test
    public void testAvro_sseAndCseKmsEncryption() throws IOException {
        S3DatasetProperties datasetProps = s3.createS3DatasetProperties(true, true);
        datasetProps.format.setValue(SimpleFileIOFormat.AVRO);
        test_noEncryption(datasetProps);
    }
}
