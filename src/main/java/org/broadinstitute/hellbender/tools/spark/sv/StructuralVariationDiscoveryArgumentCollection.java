package org.broadinstitute.hellbender.tools.spark.sv;

import htsjdk.samtools.SAMFileHeader;
import org.broadinstitute.barclay.argparser.Argument;

import java.io.Serializable;


public class StructuralVariationDiscoveryArgumentCollection implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class FindBreakpointEvidenceSparkArgumentCollection implements Serializable {
        private static final long serialVersionUID = 1L;

        public static final int KMER_SIZE = 51;
        public static final int MAX_DUST_SCORE = KMER_SIZE - 2;

        //--------- parameters ----------

        @Argument(doc = "Kmer size.", fullName = "kSize")
        public int kSize = KMER_SIZE;

        @Argument(doc = "maximum kmer DUST score", fullName = "kmerMaxDUSTScore")
        public int maxDUSTScore = MAX_DUST_SCORE;

        @Argument(doc = "The minimum mapping quality for reads used to gather evidence of breakpoints.",
                fullName = "minEvidenceMapQ", optional = true)
        public int minEvidenceMapQ = 20;

        @Argument(doc = "The minimum length of the matched portion of an interesting alignment.  "+
                "Reads that don't match at least this many reference bases won't be used in gathering evidence.",
                fullName = "minEvidenceMatchLength", optional = true)
        public int minEvidenceMatchLength = 45;

        @Argument(doc = "Proper pairs have the positive strand read upstream of the negative strand read, but "+
                "we allow this much slop for short fragments.",
                fullName = "allowedShortFragmentOverhang", optional = true)
        public int allowedShortFragmentOverhang = 10;

        @Argument(doc = "Largest fragment size that will be explicitly counted in determining " +
                "fragment size statistics.", fullName = "maxTrackedFragmentLength", optional = true)
        public int maxTrackedFragmentLength = 2000;

        @Argument(doc = "Intervals with more than this much coverage are filtered out, because the reads mapped to "+
                "that interval are clearly not exclusively local to the interval.", fullName = "maxIntervalCoverage")
        public int maxIntervalCoverage = 1000;

        @Argument(doc = "Minimum weight of the corroborating read evidence to validate some single piece of evidence.",
                fullName = "minEvidenceCount")
        public int minEvidenceWeight = 15;

        @Argument(doc = "Minimum weight of the evidence that shares a distal target locus to validate the evidence.",
                fullName = "minCoherentEvidenceCount")
        public int minCoherentEvidenceWeight = 7;

        @Argument(doc = "Minimum number of localizing kmers in a valid interval.", fullName="minKmersPerInterval")
        public int minKmersPerInterval = 5;

        @Argument(doc = "KmerCleaner maximum number of intervals for a localizing kmer."+
                " If a kmer occurs in too many intervals, it isn't sufficiently local.", fullName = "cleanerMaxIntervals")
        public int cleanerMaxIntervals = 3;

        @Argument(doc = "KmerCleaner minimum kmer count for a localizing kmer."+
                "  If we see it less often than this many times, we're guessing it's erroneous.",
                fullName = "cleanerMinKmerCount")
        public int cleanerMinKmerCount = 4;

        @Argument(doc = "KmerCleaner maximum copy number (not count, but copy number) for a kmer."+
                " Kmers observed too frequently are probably mismapped or ubiquitous.", fullName = "cleanerMaxCopyNumber")
        public int cleanerMaxCopyNumber = 4;

        @Argument(doc = "Guess at the ratio of reads in the final assembly to the number reads mapped to the interval.",
                fullName = "assemblyToMappedSizeRatioGuess")
        public int assemblyToMappedSizeRatioGuess = 7;

        @Argument(doc = "Maximum FASTQ file size.", fullName = "maxFASTQSize")
        public int maxFASTQSize = 3000000;

        @Argument(doc = "Exclusion interval padding.", fullName = "exclusionIntervalPadding")
        public int exclusionIntervalPadding = 0;

        @Argument(doc = "Include read mapping location in FASTQ files.", fullName = "includeMappingLocation")
        public boolean includeMappingLocation = true;

        @Argument(doc = "Don't look for extra reads mapped outside the interval.", fullName = "intervalOnlyAssembly")
        public boolean intervalOnlyAssembly = false;

        @Argument(doc = "Weight to give external evidence.", fullName = "externalEvidenceWeight")
        public int externalEvidenceWeight = 10;

        @Argument(doc = "Uncertainty in location of external evidence.", fullName = "externalEvidenceUncertainty")
        public int externalEvidenceUncertainty = 150;

        @Argument(doc = "Adapter sequence.", fullName = "adapterSequence", optional = true)
        public String adapterSequence;

        // ---------- options -----------

        @Argument(doc = "write gfa representation of assemblies in fastqDir", fullName = "writeGFAs", optional = true)
        public boolean writeGFAs;

        // --------- locations ----------

        @Argument(doc = "bwa-mem index image file", fullName = "alignerIndexImage")
        public String alignerIndexImageFile;

        @Argument(doc = "external evidence input file", fullName = "externalEvidence", optional = true)
        public String externalEvidenceFile;

        @Argument(doc = "output file for read metadata", fullName = "readMetadata", optional = true)
        public String metadataFile;

        @Argument(doc = "directory for evidence output", fullName = "breakpointEvidenceDir", optional = true)
        public String evidenceDir;

        @Argument(doc = "directory for evidence output", fullName = "unfilteredBreakpointEvidenceDir", optional = true)
        public String unfilteredEvidenceDir;

        @Argument(doc = "file for breakpoint intervals output", fullName = "breakpointIntervals", optional = true)
        public String intervalFile;

        @Argument(doc = "file for mapped qname intervals output", fullName = "qnameIntervalsMapped", optional = true)
        public String qNamesMappedFile;

        @Argument(doc = "file for kmer intervals output", fullName = "kmerIntervals", optional = true)
        public String kmerFile;

        @Argument(doc = "file for mapped qname intervals output", fullName = "qnameIntervalsForAssembly", optional = true)
        public String qNamesAssemblyFile;

        @Argument(doc = "output dir for assembled fastqs", fullName = "fastqDir", optional = true)
        public String fastqDir;

        @Argument(doc = "output file for non-assembled breakpoints in bedpe format",
                fullName = "targetLinkFile", optional = true)
        public String targetLinkFile;

        /**
         * This is a file that calls out the coordinates of intervals in the reference assembly to exclude from
         * consideration when calling putative breakpoints.
         * Each line is a tab-delimited interval with 1-based inclusive coordinates like this:
         *  chr1	124535434	142535434
         */
        @Argument(doc = "file of reference intervals to exclude", fullName = "exclusionIntervals", optional = true)
        public String exclusionIntervalsFile;

        /**
         * This is a path to a file of kmers that appear too frequently in the reference to be usable as probes to localize
         * reads.  We don't calculate it here, because it depends only on the reference.
         * The program FindBadGenomicKmersSpark can produce such a list for you.
         */
        @Argument(doc = "file containing ubiquitous kmer list. see FindBadGenomicKmersSpark to generate it.",
                fullName = "kmersToIgnore")
        public String kmersToIgnoreFile;

        /**
         * This is a path to a text file of contig names (one per line) that will be ignored when looking for inter-contig pairs.
         */
        @Argument(doc = "file containing alt contig names that will be ignored when looking for inter-contig pairs",
                fullName = "crossContigsToIgnore", optional = true)
        public String crossContigsToIgnoreFile;

        private static final String OUTPUT_ORDER_SHORT_NAME = "sort";
        private static final String OUTPUT_ORDER_FULL_NAME = "assembliesSortOrder";

        @Argument(doc = "sorting order to be used for the output assembly alignments SAM/BAM file",
                shortName = OUTPUT_ORDER_SHORT_NAME,
                fullName = OUTPUT_ORDER_FULL_NAME,
                optional = true)
        public SAMFileHeader.SortOrder assembliesSortOrder = SAMFileHeader.SortOrder.coordinate;
    }

    public static class DiscoverVariantsFromContigsAlignmentsSparkArgumentCollection implements Serializable {
        private static final long serialVersionUID = 1L;

        public static final int GAPPED_ALIGNMENT_BREAK_DEFAULT_SENSITIVITY = 50; // alignment with gap of size >= 50 will be broken apart.
        public static final int CHIMERIC_ALIGNMENTS_HIGHMQ_THRESHOLD = 60;
        public static final int DEFAULT_MIN_ALIGNMENT_LENGTH = 50; // Minimum flanking alignment length filters used when going through contig alignments.
        public static final int DEFAULT_ASSEMBLED_IMPRECISE_EVIDENCE_OVERLAP_UNCERTAINTY = 100;
        public static final int DEFAULT_IMPRECISE_EVIDENCE_VARIANT_CALLING_THRESHOLD = 7;

        @Argument(doc = "Minimum flanking alignment length", shortName = "minAlignLength",
                fullName = "minAlignLength", optional = true)
        public Integer minAlignLength = DEFAULT_MIN_ALIGNMENT_LENGTH;

        @Argument(doc = "vcf containing the true breakpoints used only for evaluation (not generation) of calls",
                fullName = "truthVCF", optional = true)
        public String truthVCF;

        @Argument(doc = "Uncertainty in overlap of assembled breakpoints and evidence target links.", fullName = "assemblyImpreciseEvidenceOverlapUncertainty")
        public int assemblyImpreciseEvidenceOverlapUncertainty = DEFAULT_ASSEMBLED_IMPRECISE_EVIDENCE_OVERLAP_UNCERTAINTY;

        @Argument(doc = "Number of pieces of imprecise evidence necessary to call a variant in the absence of an assembled breakpoint.", fullName = "impreciseEvidenceVariantCallingThreshold")
        public int impreciseEvidenceVariantCallingThreshold = DEFAULT_IMPRECISE_EVIDENCE_VARIANT_CALLING_THRESHOLD;

        @Argument(doc = "External CNV calls file. Should be single sample VCF, and contain only confident autosomal non-reference CNV calls (for now).", fullName = "cnvCalls", optional = true)
        public String cnvCallsFile;

        @Argument(doc = "Breakpoint padding for evaluation against truth data.", fullName = "truthIntervalPadding", optional = true)
        public int truthIntervalPadding = 50;
    }

}
