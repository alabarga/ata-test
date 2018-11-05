#!/bin/bash

##################################
# NAGEN VARIANT CALLING PIPELINE #
##################################

# Declare directories
NAGEN_SCRIPTS=/datos/nagen/nagen_scripts
REF_GENOME=/datos/nagen/reference_genome
GATK_RESOURCES=/datos/nagen/gatk_resources
SEQ_UNITS_DIR=/datos/nagen

# The pipeline requires name of SEQ_UNIT to analyse (SEQ_UNIT), 
# output directory (OUTPUT_DIR) and reference SEQ_UNIT (REFERENCE)

# Check input parameters
if [ $# -ne 4 ]
	then
	echo "NaGen mapping reads and variant calling pipeline"
	echo "USAGE: 1_nagen_fastq_to_bam.sh <SAMPLE_ID> <SEQ_UNIT> <OUTPUT_DIR> <REFERENCE>"
	exit 1
fi

# Input parameters
SAMPLE_ID=$1
SEQ_UNIT=$2
OUTPUT_DIR=$3
REFERENCE=$4

READS_DIR=${OUTPUT_DIR}/reads
TMP_DIR=${OUTPUT_DIR}/tmp


###################
# QUALITY CONTROL #
###################

# Perform quality control of reads

qsub -wd $OUTPUT_DIR -N QC_${SEQ_UNIT} ${NAGEN_SCRIPTS}/QualControl.sh $SEQ_UNIT 1

##############
# TRIM READS #
##############

# Remove adapters and trim reads with low quality

qsub -wd ${OUTPUT_DIR} -hold_jid QC_${SEQ_UNIT} -N trim_${SEQ_UNIT} ${NAGEN_SCRIPTS}/TrimReads.sh $SEQ_UNIT

####################################
# QUALITY CONTROL OF TRIMMED READS #
####################################

# Perform quality control of reads

qsub -wd $OUTPUT_DIR -hold_jid trim_${SEQ_UNIT} -N QC_trim_${SEQ_UNIT} ${NAGEN_SCRIPTS}/QualControl.sh $SEQ_UNIT 2


#####################
# MAPPING USING BWA #
#####################

# Map paired-end and single-end reads and aad read group info.
qsub -wd $OUTPUT_DIR -hold_jid trim_${SEQ_UNIT} -N bwa_${SEQ_UNIT} ${NAGEN_SCRIPTS}/MappingBWA.sh $SAMPLE_ID $SEQ_UNIT $REFERENCE


################################################
# SAM TO BAM, SORT BAM AND SAMTOOLS STATISTICS #
################################################

# Merge SAM files into a single sorted BAM file and produce statistics
qsub -wd $OUTPUT_DIR -hold_jid bwa_${SEQ_UNIT} -N sam2bam_${SEQ_UNIT} ${NAGEN_SCRIPTS}/Sam2Bam.sh $SAMPLE_ID $SEQ_UNIT $REFERENCE
