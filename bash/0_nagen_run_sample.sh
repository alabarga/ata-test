#!/bin/bash

# Declare directories
GENOMES_DIR=/datos/nagen
NAGEN_SCRIPTS=/datos/nagen/nagen_scripts
NAGEN_SAMPLES=/datos/nagen/nagen_samples
REF_GENOME=/datos/nagen/reference_genome
GATK_RESOURCES=/datos/nagen/gatk_resources

SAMPLE_ID=$1
REFERENCE=hs37d5
#REFERENCE=genome
BASEDIR=${NAGEN_SAMPLES}/${SAMPLE_ID}

TMP_DIR=${BASEDIR}/tmp
FASTQC_DIR=${BASEDIR}/fastqc
RECAL_DIR=${BASEDIR}/recalibration
BAM_DIR=${BASEDIR}/bam
SAM_DIR=${BASEDIR}/sam
GVCF_DIR=${BASEDIR}/gvcf
VCF_DIR=${BASEDIR}/vcf
TRIM_DIR=${BASEDIR}/trimmed_reads
STATS_DIR=${BASEDIR}/stats

READS_DIR=${BASEDIR}/reads
FASTQ_FILES=${BASEDIR}/fastq.list.txt

############################
# PREPARE REFERENCE SEQ_UNIT #
############################


# Make a symbolic link of reads

if [ ! -d $READS_DIR ]; then

    mkdir $READS_DIR
    
    N=0
    while read LINE ; do
        N=$((N+1))
        echo "file $N = $LINE"
        ln -s $GENOMES_DIR/original_reads/$LINE $READS_DIR
    done < $FASTQ_FILES

fi

if [ ! -d $TMP_DIR ]; then
 mkdir $TMP_DIR
 chmod 777 $TMP_DIR
fi

if [ ! -d $BAM_DIR ]; then
mkdir $BAM_DIR
chmod 777 $BAM_DIR
fi

if [ ! -d $SAM_DIR ]; then
mkdir $SAM_DIR
chmod 777 $SAM_DIR
fi

if [ ! -d $FASTQC_DIR ]; then
mkdir $FASTQC_DIR
chmod 777 $FASTQC_DIR
fi

if [ ! -d $STATS_DIR ]; then
mkdir $STATS_DIR
chmod 777 $STATS_DIR
fi

if [ ! -d $GVCF_DIR ]; then
mkdir $GVCF_DIR
chmod 777 $GVCF_DIR
fi

if [ ! -d $VCF_DIR ]; then
mkdir $VCF_DIR
chmod 777 $VCF_DIR
fi

if [ ! -d $RECAL_DIR ]; then
mkdir $RECAL_DIR
chmod 777 $RECAL_DIR
fi

if [ ! -d $TRIM_DIR ]; then
mkdir $TRIM_DIR
chmod 777 $TRIM_DIR
fi

JOB_IDS=()
SAM_FILES=()


for SEQ_UNIT in $(cat /home/amaillo/scriptsCancer/names.txt); do
#for SEQ_UNIT in $(/home/alabarga/names.sh $READS_DIR | sort | uniq); do
 echo "$SEQ_UNIT"
 JOB_IDS+=(sam2bam_${SEQ_UNIT})
 SAM_FILES+=(bam/${SEQ_UNIT}.sorted.bam)
 ${NAGEN_SCRIPTS}/1_nagen_fastq_to_bam.sh ${SAMPLE_ID} $SEQ_UNIT $BASEDIR $REFERENCE
 sleep 2
done

job_ids=$(IFS=","; echo "${JOB_IDS[*]}" )

bam_files=$(IFS=","; echo "${SAM_FILES[*]}" )



####################################
# MARKING DUPLICATES AND FILTERING #
####################################
 
# Marking and removing duplicates
qsub -wd $BASEDIR -hold_jid $job_ids -N RmDup_$SAMPLE_ID $NAGEN_SCRIPTS/BamRmDup.sh $SAMPLE_ID $bam_files

###################################
#           Multi QC              #
###################################

qsub -wd $BASEDIR -hold_jid RmDup_$SAMPLE_ID -N Multiqc_$SAMPLE_ID $NAGEN_SCRIPTS/MultiQC.sh $FASTQC_DIR $BAM_DIR


#From BAM to VCF Germiline
${NAGEN_SCRIPTS}/2_nagen_bam_to_vcf.sh ${SAMPLE_ID} ${REFERENCE}





