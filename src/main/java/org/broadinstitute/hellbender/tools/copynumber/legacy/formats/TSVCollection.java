package org.broadinstitute.hellbender.tools.copynumber.legacy.formats;

import htsjdk.samtools.util.Locatable;
import org.broadinstitute.hellbender.exceptions.UserException;
import org.broadinstitute.hellbender.utils.SimpleInterval;
import org.broadinstitute.hellbender.utils.Utils;
import org.broadinstitute.hellbender.utils.io.IOUtils;
import org.broadinstitute.hellbender.utils.tsv.*;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class TSVCollection<T extends Locatable> {
    private final String sampleName;
    private final List<T> records;
    private final TableColumnCollection mandatoryColumns;
    private final Function<DataLine, T> dataLineToRecordFunction;
    private final BiConsumer<T, DataLine> recordAndDataLineBiConsumer;

    public TSVCollection(final String sampleName,
                         final List<T> records,
                         final TableColumnCollection mandatoryColumns,
                         final Function<DataLine, T> dataLineToRecordFunction,
                         final BiConsumer<T, DataLine> recordAndDataLineBiConsumer) {
        this.sampleName = Utils.nonNull(sampleName);
        this.records = Utils.nonNull(records);
        this.mandatoryColumns = Utils.nonNull(mandatoryColumns);
        this.dataLineToRecordFunction = Utils.nonNull(dataLineToRecordFunction);
        this.recordAndDataLineBiConsumer = Utils.nonNull(recordAndDataLineBiConsumer);
    }

    public TSVCollection(final File inputFile,
                         final TableColumnCollection mandatoryColumns,
                         final Function<DataLine, T> dataLineToRecordFunction,
                         final BiConsumer<T, DataLine> recordAndDataLineBiConsumer) {
        IOUtils.canReadFile(inputFile);
        this.mandatoryColumns = Utils.nonNull(mandatoryColumns);
        this.dataLineToRecordFunction = Utils.nonNull(dataLineToRecordFunction);
        this.recordAndDataLineBiConsumer = Utils.nonNull(recordAndDataLineBiConsumer);

        try (final TSVReader reader = new TSVReader(inputFile)) {
            TableUtils.checkMandatoryColumns(reader.columns(), mandatoryColumns, UserException.BadInput::new);
            sampleName = reader.getSampleName();
            records = reader.stream().collect(Collectors.toList());
        } catch (final IOException | UncheckedIOException e) {
            throw new UserException.CouldNotReadInputFile(inputFile, e);
        }
    }

    public String getSampleName() {
        return sampleName;
    }

    /**
     * @return  an unmodifiable view of the records contained in the collection
     */
    public List<T> getRecords() {
        return Collections.unmodifiableList(records);
    }

    /**
     * @return  a new modifiable list of {@link SimpleInterval}s corresponding to the {@link Locatable}s
     *          for each record contained in the collection
     */
    public List<SimpleInterval> getIntervals() {
        return records.stream()
                .map(r -> new SimpleInterval(r.getContig(), r.getStart(), r.getEnd()))
                .collect(Collectors.toList());
    }

    public void write(final File outputFile) {
        try (final TSVWriter writer = new TSVWriter(outputFile, sampleName)) {
            writer.writeSampleName();
            writer.writeAllRecords(records);
        } catch (final IOException e) {
            throw new UserException.CouldNotCreateOutputFile(outputFile, e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TSVCollection<?> that = (TSVCollection<?>) o;

        return sampleName.equals(that.sampleName) && records.equals(that.records);
    }

    @Override
    public int hashCode() {
        int result = sampleName.hashCode();
        result = 31 * result + records.hashCode();
        return result;
    }

    private final class TSVReader extends TableReader<T> implements NamedSampleFile {
        private final File file;

        private TSVReader(final File file) throws IOException {
            super(file);
            this.file = file;
        }

        private String getSampleName() {
            return getSampleName(file);
        }

        @Override
        protected T createRecord(final DataLine dataLine) {
            Utils.nonNull(dataLine);
            try {
                return dataLineToRecordFunction.apply(dataLine);
            } catch (final IllegalArgumentException e) {
                throw new UserException.BadInput("TSV file must have all columns specified.");
            }
        }
    }

    private final class TSVWriter extends TableWriter<T> {
        private final String sampleName;

        TSVWriter(final File file,
                  final String sampleName) throws IOException {
            super(file, mandatoryColumns);
            this.sampleName = Utils.nonNull(sampleName);
        }

        void writeSampleName() {
            try {
                writeComment(NamedSampleFile.SAMPLE_NAME_COMMENT_PREFIX + sampleName);
            } catch (final IOException e) {
                throw new UserException("Could not write sample name.");
            }
        }

        @Override
        protected void composeLine(final T record, final DataLine dataLine) {
            Utils.nonNull(record);
            Utils.nonNull(dataLine);
            recordAndDataLineBiConsumer.accept(record, dataLine);
        }
    }
}