#######################################
##     STATEGRA PARCKAGE - DRAFT     ##
#######################################
# Done: Alberto Labarga               #
# User: TransBio                      #
# Date: November 2018                 #
#######################################


# Upload required packages
library(STATegRa)
library(ggplot2)
library(gridExtra)
library(gplots)
library(VennDiagram)
library(sva)
library(plotrix)
library(minfi)
library(IlluminaHumanMethylation450kanno.ilmn12.hg19)
library(IlluminaHumanMethylation450kmanifest)


# Working directory
setwd("../stategra/bioconductor_package/draft/GBM/All_data/")


#######################################
# Read data                           #
#######################################

# Expression data: 12,042 x 548
rna <- read.table("GBM_expression_affy.txt", dec=".", sep="\t", header=TRUE)
colnames(rna) <- scan("GBM_expression_affy.txt", what="character", nlines=1)[-1]
rna <- rna[,-549]

# Methylation data: 485,577 x 155
met <- read.table("GBM_methylation450K.txt", dec=".", sep="\t", header=TRUE)
colnames(met) <- scan("GBM_methylation450K.txt", what="character", nlines=1)[-1]
met <- met[,-155]

# There are 89,512 probes with missing values in all the samples
aa <- apply(met,1,FUN=function(x){return(sum(is.na(x)))})
met <- met[-which(aa==154),]

# Still 13,613 missing values in the new matrix
aa <- apply(met,1,FUN=function(x){return(sum(is.na(x)))})
hist(aa, main="Histogram of NA by probes")

bb <- apply(met,2,FUN=function(x){return(sum(is.na(x)))})
hist(bb, main="Histogram of NA by samples")

met <- met[-which(aa!=0),] #382452 x 154

#Get annotation data from mathylation
ann450k <- getAnnotation(IlluminaHumanMethylation450kanno.ilmn12.hg19)
head(ann450k)

#The array contains by design 65 probes that are not meant to interrogate methylation status, but instead are designed to interrogate SNPs. By default, minfi drops these probes. The function getSnpBeta allows the user to extract the Beta values for those probes.
#Remove sex chromosomes (11648) and probes with SNP annotation (87018)
xy_linked <- which(ann450k$chr=="chrY" | ann450k$chr=="chrX") #11648 probes
target_snp <- which(!is.na(ann450k$Probe_rs)) #87018
target_cpg <- which(!is.na(ann450k$CpG_rs)) #16998
target_sbe <- which(!is.na(ann450k$SBE_rs)) #7876

rm_probes <- unique(c(xy_linked,target_snp, target_sbe, target_cpg)) #114614

met <- met[(rownames(met)%in%rownames(ann450k)[-rm_probes]),]



# miRNA data: 534 x 571
mirna <- read.table("GBM_expression_mirna.txt", dec=".", sep="\t", header=TRUE)
colnames(mirna) <- scan("GBM_expression_mirna.txt", what="character", nlines=1)[-1]
mirna <- mirna[,-572]


# Clinical data: 629 x 53
class <- read.table("GBM_all_clinical_data.txt", sep="\t", header=TRUE)
clinical <- read.table("clinical_data_samples.txt", sep="\t", header=TRUE)

clinical <- merge(clinical, class, by.x="X_PATIENT", by.y="name", all.x=TRUE, sort=FALSE)
rownames(clinical) <- clinical$sampleID

#######################################
# Repeted sample exploration          #
#######################################

# Expression data

plot(rna[,grep("TCGA-06-0137-01",colnames(rna))], main="TCGA-06-0137-01")
plot(rna[,grep("TCGA-06-0138-01",colnames(rna))], main="TCGA-06-0138-01")
plot(rna[,grep("TCGA-06-0145-01",colnames(rna))], main="TCGA-06-0145-01")
plot(rna[,grep("TCGA-06-0148-01",colnames(rna))], main="TCGA-06-0148-01")
plot(rna[,grep("TCGA-06-0154-01",colnames(rna))], main="TCGA-06-0154-01")
plot(rna[,grep("TCGA-06-0156-01",colnames(rna))], main="TCGA-06-0156-01")
plot(rna[,grep("TCGA-06-0168-01",colnames(rna))], main="TCGA-06-0168-01")
plot(rna[,grep("TCGA-06-0176-01",colnames(rna))], main="TCGA-06-0176-01")
plot(rna[,grep("TCGA-06-0208-01",colnames(rna))], main="TCGA-06-0208-01")
plot(rna[,grep("TCGA-06-0211-01",colnames(rna))], main="TCGA-06-0211-01")

colnames(rna) <- gsub("_rep1", "", colnames(rna))


# miRNA data

plot(mirna[,grep("TCGA-14-1825-01",colnames(mirna))], main="TCGA-14-1825-01")
plot(mirna[,grep("TCGA-19-1788-01",colnames(mirna))], main="TCGA-19-1788-01")
plot(mirna[,grep("TCGA-26-1799-01",colnames(mirna))], main="TCGA-26-1799-01")
plot(mirna[,grep("TCGA-27-1833-01",colnames(mirna))], main="TCGA-27-1833-01")
plot(mirna[,grep("TCGA-28-1751-01",colnames(mirna))], main="TCGA-28-1751-01")

colnames(mirna) <- gsub("_rep1", "", colnames(mirna))




#######################################
# Common data                         #
#######################################


venn.diagram(list(rna=colnames(rna), mirna=colnames(mirna)), fill=c("red","blue"), cat.fontface=4, lty=2, fontfamily=3, filename="m1.tiff")
venn.diagram(list(rna=colnames(rna), met=colnames(met)), fill=c("red","green"), cat.fontface=4, lty=2, fontfamily=3, filename="m2.tiff")
venn.diagram(list(rna=colnames(rna), met=colnames(met), mirna=colnames(mirna)), fill=c("red","green","blue"), cat.fontface=4, lty=2, fontfamily=3, filename="m3.tiff")


share_rna_mirna <- intersect(colnames(rna),colnames(mirna))
share_rna_met <- intersect(colnames(rna),colnames(met))

# First subset: rna vs mirna
rna_s1 <- rna[,share_rna_mirna] # 12,042 x 520
mirna_s1 <- mirna[,share_rna_mirna] # 534 x 520
clinical_s1 <- clinical[share_rna_mirna,] # 520 x 53

# Second subset: rna vs methylation
rna_s2 <- rna[,share_rna_met] # 12,042 x 85
met_s2 <- met[,share_rna_met] # 311,135 x 85
clinical_s2 <- clinical[share_rna_met,] # 85 x 53




# Correlation between clinical variables
clin_var <- c("GeneExp_Subtype", "gender.y", "batch_number", "race", "ethnicity", "histological_type", "days_to_death", "age_at_initial_pathologic_diagnosis", "days_to_birth", "prior_glioma", "tissue_source_site", "karnofsky_performance_score", "performance_status_scale_timing")
#Vital status i years_to_death no es poden comparar. Eliminem Vital status

M <- clinical[,clin_var]
res <- matrix(ncol=ncol(M), nrow=ncol(M))
for(i in 1:ncol(M)){
  v1 <- M[,i]
  for(j in 1:ncol(M)){
    v2 <- M[,j]
    if(is.factor(v2) & is.numeric(v1)){ res[i,j] <- log10(kruskal.test(v1,v2)$p.value)*-1 }
    if(is.factor(v1) & is.numeric(v2)){ res[i,j] <- log10(kruskal.test(v2,v1)$p.value)*-1 }
    if(is.factor(v1) & is.factor(v2)){ res[i,j] <- log10(chisq.test(v2,v1)$p.value)*-1 }
    if(is.numeric(v1) & is.numeric(v2)){ res[i,j] <- log10(cor.test(as.numeric(v2),as.numeric(v1), method="spearman")$p.value)*-1 }
  }
}
colnames(res) <- clin_var
rownames(res) <- clin_var

sel <- which(res==Inf)
res[sel] <- 0
res[sel] <- max(res)

cors <- res
cellcol<-color.scale(cbind(cors, rep(max(res)*-1,nrow(cors)),rep(min(res)*-1,nrow(cors))),0,c(0,1),c(1,0.2))[,1:ncol(cors)]
par(mar = c(10,8,4,2) + 0.1)
color2D.matplot(cors, cellcolors=cellcol, show.legend=TRUE, show.values=2,
                axes=FALSE, xlab="", ylab="")

axis(1,at=0.5:12.5,las=2,labels=colnames(cors))
axis(2,at=0.5:12.5,las=2,labels=rev(rownames(cors)))


cors <- as.matrix(table(M$batch_number, M$tissue_source_site))
cellcol<-color.scale(cors,c(0,1),c(0.5,1),c(1,0))[,1:ncol(cors)]
par(mar = c(10,8,4,2) + 0.1)
color2D.matplot(cors, cellcolors=cellcol, show.legend=TRUE, show.values=2,
                axes=FALSE, xlab="", ylab="")

axis(1,at=0.5:19.5,las=2,labels=colnames(cors))
axis(2,at=0.5:22.5,las=2,labels=rev(rownames(cors)))


clin_var <- c("GeneExp_Subtype", "gender.y", "batch_number", "race", "ethnicity", "histological_type", "days_to_death", "age_at_initial_pathologic_diagnosis", "days_to_birth", "vital_status", "prior_glioma", "tissue_source_site", "karnofsky_performance_score", "performance_status_scale_timing")

#########################################
# Data exploration and quality control  #
#########################################

# Expression data (mRNA): 12,042 x 548

pca_rna <- prcomp(t(rna))
var_prcomp <- pca_rna$sdev^2


# Plot PCs variance

plot(var_prcomp/sum(var_prcomp), xlim = c(0, 15), type = "b", pch = 16, xlab = "principal components", 
     ylab = "variance explained", main="RNA - Princial Components variance")

# Plot PCs scores

df <- data.frame(PC1=pca_rna$x[,1], PC2=pca_rna$x[,2], PC3=pca_rna$x[,3], clinical[colnames(rna),c("GeneExp_Subtype","gender.x","batch_number")])
df$batch_number <- as.character(df$batch_number)

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from RNA data")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from RNA data")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from RNA data")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from RNA data")



# Association with clinical variables
clin <- clinical[colnames(rna),]
com <- pca_rna$x[,1:20]
res <- matrix(ncol=20, nrow=length(clin_var))
for(i in 1:length(clin_var)){
  v <- clin[,clin_var[i]]
  if(is.factor(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- kruskal.test(x,v); return((log10(a$p.value))*-1)})
  }
  if(is.numeric(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- cor.test(x,v, method="spearman"); return((log10(a$p.value))*-1)})
  }
}
colnames(res) <- paste("PC",1:20, sep="") 
rownames(res) <- clin_var


cors <- res
cellcol<-color.scale(cbind(cors, rep(max(res)*-1,nrow(cors)),rep(min(res)*-1,nrow(cors))),0,c(0,1),c(1,0.2))[,1:ncol(cors)]
par(mar = c(10,8,4,2) + 0.1)
color2D.matplot(cors, cellcolors=cellcol, show.legend=TRUE, show.values=2,
                axes=FALSE, xlab="", ylab="")

axis(1,at=0.5:19.5,las=2,labels=colnames(cors))
axis(2,at=0.5:13.5,las=2,labels=rev(rownames(cors)))




# Expression data subset 1: 12,042 x 520 (intersection with miRNA)

pca_rna <- prcomp(t(rna_s1))
var_prcomp <- pca_rna$sdev^2


# Plot PCs variance

plot(var_prcomp/sum(var_prcomp), xlim = c(0, 15), type = "b", pch = 16, xlab = "principal components", 
     ylab = "variance explained", main="RNA - Princial Components variance")

# Plot PCs scores

df <- data.frame(PC1=pca_rna$x[,1], PC2=pca_rna$x[,2], PC3=pca_rna$x[,3], clinical_s1[,c("GeneExp_Subtype","gender.x","batch_number")])
df$batch_number <- as.character(df$batch_number)

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from RNA data subset 1")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from RNA data subset 1")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from RNA data subset 1")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from RNA data subset 1")


# Association with clinical variables
clin <- clinical_s1[colnames(rna_s1),]
com <- pca_rna$x[,1:20]
res <- matrix(ncol=20, nrow=length(clin_var))
for(i in 1:length(clin_var)){
  v <- clin[,clin_var[i]]
  if(is.factor(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- kruskal.test(x,v); return((log10(a$p.value))*-1)})
  }
  if(is.numeric(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- cor.test(x,v, method="spearman"); return((log10(a$p.value))*-1)})
  }
}
colnames(res) <- paste("PC",1:20, sep="") 
rownames(res) <- clin_var


cors <- res
cellcol<-color.scale(cbind(cors, rep(max(res)*-1,nrow(cors)),rep(min(res)*-1,nrow(cors))),0,c(0,1),c(1,0.2))[,1:ncol(cors)]
par(mar = c(10,8,4,2) + 0.1)
color2D.matplot(cors, cellcolors=cellcol, show.legend=TRUE, show.values=2,
                axes=FALSE, xlab="", ylab="")

axis(1,at=0.5:19.5,las=2,labels=colnames(cors))
axis(2,at=0.5:13.5,las=2,labels=rev(rownames(cors)))


# Expression data subset 2: 12,042 x 85 (intersection with methylation)

pca_rna <- prcomp(t(rna_s2))
var_prcomp <- pca_rna$sdev^2


# Plot PCs variance

plot(var_prcomp/sum(var_prcomp), xlim = c(0, 15), type = "b", pch = 16, xlab = "principal components", 
     ylab = "variance explained", main="RNA - Princial Components variance")

# Plot PCs scores

df <- data.frame(PC1=pca_rna$x[,1], PC2=pca_rna$x[,2], PC3=pca_rna$x[,3], clinical_s2[,c("GeneExp_Subtype","gender.x","batch_number")])
df$batch_number <- as.character(df$batch_number)

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from RNA data subset 2")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from RNA data subset 2")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from RNA data subset 2")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from RNA data subset 2")


# Association with clinical variables
clin <- clinical_s2[colnames(rna_s2),]
com <- pca_rna$x[,1:20]
res <- matrix(ncol=20, nrow=length(clin_var))
for(i in 1:length(clin_var)){
  v <- clin[,clin_var[i]]
  if(is.factor(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- kruskal.test(x,v); return((log10(a$p.value))*-1)})
  }
  if(is.numeric(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- cor.test(x,v, method="spearman"); return((log10(a$p.value))*-1)})
  }
}
colnames(res) <- paste("PC",1:20, sep="") 
rownames(res) <- clin_var


cors <- res
cellcol<-color.scale(cbind(cors, rep(max(res)*-1,nrow(cors)),rep(min(res)*-1,nrow(cors))),0,c(0,1),c(1,0.2))[,1:ncol(cors)]
par(mar = c(10,8,4,2) + 0.1)
color2D.matplot(cors, cellcolors=cellcol, show.legend=TRUE, show.values=2,
                axes=FALSE, xlab="", ylab="")

axis(1,at=0.5:19.5,las=2,labels=colnames(cors))
axis(2,at=0.5:13.5,las=2,labels=rev(rownames(cors)))




# Expression data (miRNA): 534 x 571

pca_mirna <- prcomp(t(mirna))
var_prcomp <- pca_mirna$sdev^2


# Plot PCs variance

plot(var_prcomp/sum(var_prcomp), xlim = c(0, 15), type = "b", pch = 16, xlab = "principal components", 
     ylab = "variance explained", main="miRNA - Princial Components variance")

# Plot PCs scores

df <- data.frame(PC1=pca_mirna$x[,1], PC2=pca_mirna$x[,2], PC3=pca_mirna$x[,3], clinical[colnames(mirna),c("GeneExp_Subtype","gender.x","batch_number")])
df$batch_number <- as.character(df$batch_number)

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from miRNA data")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from miRNA data")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from miRNA data")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from miRNA data")


# Association with clinical variables
clin <- clinical[colnames(mirna),]
com <- pca_mirna$x[,1:20]
res <- matrix(ncol=20, nrow=length(clin_var))
for(i in 1:length(clin_var)){
  v <- clin[,clin_var[i]]
  if(is.factor(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- kruskal.test(x,v); return((log10(a$p.value))*-1)})
  }
  if(is.numeric(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- cor.test(x,v, method="spearman"); return((log10(a$p.value))*-1)})
  }
}
colnames(res) <- paste("PC",1:20, sep="") 
rownames(res) <- clin_var


cors <- res
cellcol<-color.scale(cbind(cors, rep(max(res)*-1,nrow(cors)),rep(min(res)*-1,nrow(cors))),0,c(0,1),c(1,0.2))[,1:ncol(cors)]
par(mar = c(10,8,4,2) + 0.1)
color2D.matplot(cors, cellcolors=cellcol, show.legend=TRUE, show.values=2,
                axes=FALSE, xlab="", ylab="")

axis(1,at=0.5:19.5,las=2,labels=colnames(cors))
axis(2,at=0.5:13.5,las=2,labels=rev(rownames(cors)))




# Expression data subset 1: 534 x 520 (intersection with mRNA)

pca_mirna <- prcomp(t(mirna_s1))
var_prcomp <- pca_mirna$sdev^2


# Plot PCs variance

plot(var_prcomp/sum(var_prcomp), xlim = c(0, 15), type = "b", pch = 16, xlab = "principal components", 
     ylab = "variance explained", main="miRNA - Princial Components variance")

# Plot PCs scores

df <- data.frame(PC1=pca_mirna$x[,1], PC2=pca_mirna$x[,2], PC3=pca_mirna$x[,3], clinical_s1[,c("GeneExp_Subtype","gender.x","batch_number")])
df$batch_number <- as.character(df$batch_number)

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from miRNA data subset 1")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from miRNA data subset 1")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from miRNA data subset 1")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from miRNA data subset 1")


# Association with clinical variables
clin <- clinical_s1[colnames(mirna_s1),]
com <- pca_mirna$x[,1:20]
res <- matrix(ncol=20, nrow=length(clin_var))
for(i in 1:length(clin_var)){
  v <- clin[,clin_var[i]]
  if(is.factor(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- kruskal.test(x,v); return((log10(a$p.value))*-1)})
  }
  if(is.numeric(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- cor.test(x,v, method="spearman"); return((log10(a$p.value))*-1)})
  }
}
colnames(res) <- paste("PC",1:20, sep="") 
rownames(res) <- clin_var


cors <- res
cellcol<-color.scale(cbind(cors, rep(max(res)*-1,nrow(cors)),rep(min(res)*-1,nrow(cors))),0,c(0,1),c(1,0.2))[,1:ncol(cors)]
par(mar = c(10,8,4,2) + 0.1)
color2D.matplot(cors, cellcolors=cellcol, show.legend=TRUE, show.values=2,
                axes=FALSE, xlab="", ylab="")

axis(1,at=0.5:19.5,las=2,labels=colnames(cors))
axis(2,at=0.5:13.5,las=2,labels=rev(rownames(cors)))





# Methylation: 311,135 x 154

pca_met <- prcomp(t(met))
var_prcomp <- pca_met$sdev^2


# Plot PCs variance

plot(var_prcomp/sum(var_prcomp), xlim = c(0, 15), type = "b", pch = 16, xlab = "principal components", 
     ylab = "variance explained", main="methylation - Princial Components variance")

# Plot PCs scores

df <- data.frame(PC1=pca_met$x[,1], PC2=pca_met$x[,2], PC3=pca_met$x[,3], clinical[colnames(met),c("GeneExp_Subtype","gender.x","batch_number")])
df$batch_number <- as.character(df$batch_number)

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from methylation data")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from methylation data")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from methylation data")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from methylation data")



# Association with clinical variables
clin <- clinical[colnames(met),]
com <- pca_met$x[,1:20]
res <- matrix(ncol=20, nrow=length(clin_var))
for(i in 1:length(clin_var)){
  v <- clin[,clin_var[i]]
  if(is.factor(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- kruskal.test(x,v); return((log10(a$p.value))*-1)})
  }
  if(is.numeric(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- cor.test(x,v, method="spearman"); return((log10(a$p.value))*-1)})
  }
}
colnames(res) <- paste("PC",1:20, sep="") 
rownames(res) <- clin_var


cors <- res
cellcol<-color.scale(cbind(cors, rep(max(res)*-1,nrow(cors)),rep(min(res)*-1,nrow(cors))),0,c(0,1),c(1,0.2))[,1:ncol(cors)]
par(mar = c(10,8,4,2) + 0.1)
color2D.matplot(cors, cellcolors=cellcol, show.legend=TRUE, show.values=2,
                axes=FALSE, xlab="", ylab="")

axis(1,at=0.5:19.5,las=2,labels=colnames(cors))
axis(2,at=0.5:13.5,las=2,labels=rev(rownames(cors)))


#Batch effect plot, PC9.
df <- data.frame(PC1=pca_met$x[,1], PC9=pca_met$x[,9], clinical[colnames(met),c("GeneExp_Subtype","gender.x","batch_number")])
df$batch_number <- as.character(df$batch_number)
ggplot(df) +
  geom_point(aes(x=PC1, y=PC9, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from methylation data")




# Methylation data subset 1: 311,135 x 85 (intersection with mRNA)

pca_met <- prcomp(t(met_s2))
var_prcomp <- pca_met$sdev^2


# Plot PCs variance

plot(var_prcomp/sum(var_prcomp), xlim = c(0, 15), type = "b", pch = 16, xlab = "principal components", 
     ylab = "variance explained", main="methylation - Princial Components variance")

# Plot PCs scores

df <- data.frame(PC1=pca_met$x[,1], PC2=pca_met$x[,2], PC3=pca_met$x[,3], clinical_s2[,c("GeneExp_Subtype","gender.x","batch_number")])
df$batch_number <- as.character(df$batch_number)

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from methylation data subset 2")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from methylation data subset 2")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from methylation data subset 2")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from methylation data subset 2")


# Association with clinical variables
clin <- clinical_s2[colnames(met_s2),]
com <- pca_met$x[,1:20]
res <- matrix(ncol=20, nrow=length(clin_var))
for(i in 1:length(clin_var)){
  v <- clin[,clin_var[i]]
  if(is.factor(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- kruskal.test(x,v); return((log10(a$p.value))*-1)})
  }
  if(is.numeric(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- cor.test(x,v, method="spearman"); return((log10(a$p.value))*-1)})
  }
}
colnames(res) <- paste("PC",1:20, sep="") 
rownames(res) <- clin_var


cors <- res
cellcol<-color.scale(cbind(cors, rep(max(res)*-1,nrow(cors)),rep(min(res)*-1,nrow(cors))),0,c(0,1),c(1,0.2))[,1:ncol(cors)]
par(mar = c(10,8,4,2) + 0.1)
color2D.matplot(cors, cellcolors=cellcol, show.legend=TRUE, show.values=2,
                axes=FALSE, xlab="", ylab="")

axis(1,at=0.5:19.5,las=2,labels=colnames(cors))
axis(2,at=0.5:13.5,las=2,labels=rev(rownames(cors)))



#########################################
# JIVE previous batch correction        #
#########################################

# In order to see if theres a share structure refered as batch feature.

####################################################
# Integration approaches: Subset 1 (mRNA + miRNA)  #
####################################################

## Common components
common <- selectCommonComps(X=as.matrix(rna_s1), Y=as.matrix(mirna_s1), Rmax=4)
common$common  #0 common component!
grid.arrange(common$pssq, common$pratios, ncol=2)


##########################################################
# Integration approaches: Subset 1 (mRNA + methylation)  #
##########################################################

## Common components
common <- selectCommonComps(X=as.matrix(rna_s2), Y=as.matrix(met_s2), Rmax=4)
common$common  #0 common component!
grid.arrange(common$pssq, common$pratios, ncol=2)


# ATENCTION!!!! We need to first center and scale the data?

rna_s1_norm <- t(scale(t(rna_s1),center=TRUE,scale=TRUE))
mirna_s1_norm <- t(scale(t(mirna_s1),center=TRUE,scale=TRUE))

rna_s2_norm <- t(scale(t(rna_s2),center=TRUE,scale=TRUE))
met_s2_norm <- t(scale(t(met_s2),center=TRUE,scale=TRUE))

####################################################
# Integration approaches: Subset 1 (mRNA + miRNA)  #
####################################################

## Common components
common <- selectCommonComps(X=as.matrix(rna_s1_norm), Y=as.matrix(mirna_s1_norm), Rmax=4)
common$common  #0 common component!
grid.arrange(common$pssq, common$pratios, ncol=2)


##########################################################
# Integration approaches: Subset 1 (mRNA + methylation)  #
##########################################################

## Common components
common <- selectCommonComps(X=as.matrix(rna_s2_norm), Y=as.matrix(met_s2_norm), Rmax=4)
common$common  #2 common component!
grid.arrange(common$pssq, common$pratios, ncol=2)

## Distinctive components
pca1 <- PCA.selection(Data=as.matrix(rna_s2), fac.sel="single%", varthreshold=0.05)
pca1$numComps # 3 components, 2 common and 1 distinctive

#Plot the explained variance and the accumulated variance of the first 10 components
pcadf1 <- data.frame(comps=factor(rep(1:10,2),levels=1:10),
                     var=unlist(c(pca1$PCAres$var.exp[1:10,])),
                     label=rep(colnames(pca1$PCAres$var.exp),each=10))
ggplot(pcadf1,aes(x=comps,y=var,fill=label))+
  geom_bar(stat="identity",position="dodge")+
  ggtitle("Expression dataset (mRNA)")+
  geom_hline(yintercept=0.05,linetype="dotted")

pca2 <- PCA.selection(Data=as.matrix(met_s2), fac.sel="single%",varthreshold=0.05)
pca2$numComps # 3 components, 2 common and 1 distinctive

#Plot the explained variance and the accumulated variance of the first 10 components
pcadf2 <- data.frame(comps=factor(rep(1:10,2),levels=1:10),
                     var=unlist(c(pca2$PCAres$var.exp[1:10,])),
                     label=rep(colnames(pca2$PCAres$var.exp),each=10))
ggplot(pcadf2,aes(x=comps,y=var,fill=label))+
  geom_bar(stat="identity",position="dodge")+
  ggtitle("Methylation")+
  geom_hline(yintercept=0.05,linetype="dotted")


# JIVE
B1 <- createOmicsExpressionSet(Data=as.matrix(rna_s2_norm), pData=clinical_s2)
B2 <- createOmicsExpressionSet(Data=as.matrix(met_s2_norm), pData=clinical_s2)

jiveRes <- omicsCompAnalysis(Input=list(B1, B2), Names=c("Expression (mRNA)", "Methylation"),
                             method="JIVE", Rcommon=2, Rspecific=c(1, 1),
                             center=TRUE, scale=TRUE, weight=TRUE)
## plot VAF
plotVAF(jiveRes)
getVAF(jiveRes)

## PCA plots - is not working!! Possibly because there's only one distinctive component
#plotRes(object=jiveRes, comps=c(1,2), what="scores", type="common",
#        combined=FALSE, block="", color="GeneExp_Subtype", shape=NULL, labels=NULL,
#        background=TRUE, palette=NULL, pointSize=4, labelSize=NULL,
#        axisSize=NULL, titleSize=NULL) 


df <- data.frame(jiveRes@scores$common, clinical_s2[,c("GeneExp_Subtype","gender.x","batch_number")])
df$batch_number <- as.character(df$batch_number)

ggplot(df) +
  geom_point(aes(x=Comp.1, y=Comp.2, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from join mRNA and methylation (data subset 2)")


ggplot(df) +
  geom_point(aes(x=Comp.1, y=Comp.2, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from join mRNA and methylation (data subset 2)")



# Association with clinical variables
clin <- clinical_s2[colnames(met_s2),]
com <- jiveRes@scores$common
res <- matrix(ncol=2, nrow=length(clin_var))
for(i in 1:length(clin_var)){
  v <- clin[,clin_var[i]]
  if(is.factor(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- kruskal.test(x,v); return((log10(a$p.value))*-1)})
  }
  if(is.numeric(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- cor.test(x,v, method="spearman"); return((log10(a$p.value))*-1)})
  }
}
colnames(res) <- paste("PC",1:2, sep="") 
rownames(res) <- clin_var


cors <- res
cellcol<-color.scale(cbind(cors, rep(max(res)*-1,nrow(cors)),rep(min(res)*-1,nrow(cors))),0,c(0,1),c(1,0.2))[,1:ncol(cors)]
par(mar = c(10,8,4,2) + 0.1)
color2D.matplot(cors, cellcolors=cellcol, show.legend=TRUE, show.values=2,
                axes=FALSE, xlab="", ylab="")

axis(1,at=0.5:1.5,las=2,labels=colnames(cors))
axis(2,at=0.5:13.5,las=2,labels=rev(rownames(cors)))




#########################################
# Batch correction                      #
#########################################

# Expression data (mRNA): 12,042 x 548

# Remove samples without batch annotation or those that are unique in a batch

samples_to_remove <- c("TCGA-27-1836-01","TCGA-32-2498-01","TCGA-32-2495-01","TCGA-28-2501-01","TCGA-28-2510-01","TCGA-16-1048-01", grep("rep",colnames(rna),value=TRUE))

rna_pre_combat <- rna[,!(colnames(rna)%in%samples_to_remove)] #12,042 x 523
clinical_combat <- clinical[colnames(rna_pre_combat),]

#Batch  1.83.0  10.71.0 111.51.0  16.74.0   2.83.0  20.71.0  26.62.0   3.82.0  38.61.0   4.87.0   5.77.0   6.81.0  62.58.0   7.81.0   79.53.0   8.80.0 
#Freq   22       28       37       47       17       46       44       18       29       35       63       35       24       19        35       24

mod <- model.matrix(~as.factor(gender.y)+as.factor(as.character(GeneExp_Subtype))+days_to_birth, data=clinical_combat)

rna_corrected <- ComBat(rna_pre_combat, batch=as.factor(as.character(clinical_combat$batch_number)), mod=mod, prior.plot=TRUE)

#Corrected data: mRNA 12,042 x 523

pca_rna <- prcomp(t(rna_corrected))
var_prcomp <- pca_rna$sdev^2


# Plot PCs variance

plot(var_prcomp/sum(var_prcomp), xlim = c(0, 15), type = "b", pch = 16, xlab = "principal components", 
     ylab = "variance explained", main="RNA - Princial Components variance")

# Plot PCs scores

df <- data.frame(PC1=pca_rna$x[,1], PC2=pca_rna$x[,2], PC3=pca_rna$x[,3], clinical_combat[colnames(rna_corrected),c("GeneExp_Subtype","gender.x","batch_number")])
df$batch_number <- as.character(df$batch_number)

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from RNA data corrected")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from RNA data corrected")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from RNA data corrected")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from RNA data corrected")



# Expression data (miRNA): 534 x 571

# Remove samples without batch annotation or those that are unique in a batch

samples_to_remove <- c("TCGA-27-1836-01","TCGA-32-2495-01","TCGA-76-6660-01","TCGA-16-1048-01","TCGA-28-2510-01","TCGA-32-2498-01","TCGA-76-6283-01", grep("rep",colnames(mirna),value=TRUE))

mirna_pre_combat <- mirna[,!(colnames(mirna)%in%samples_to_remove)] #534 x 559
clinical_combat <- clinical[colnames(mirna_pre_combat),]

mod <- model.matrix(~as.factor(gender.y)+as.factor(as.character(GeneExp_Subtype))+days_to_birth, data=clinical_combat)

mirna_corrected <- ComBat(mirna_pre_combat, batch=as.factor(as.character(clinical_combat$batch_number)), mod=mod, prior.plot=TRUE)

#Corrected data: miRNA 534 x 559

pca_rna <- prcomp(t(mirna_corrected))
var_prcomp <- pca_rna$sdev^2


# Plot PCs variance

plot(var_prcomp/sum(var_prcomp), xlim = c(0, 15), type = "b", pch = 16, xlab = "principal components", 
     ylab = "variance explained", main="miRNA - Princial Components variance")

# Plot PCs scores

df <- data.frame(PC1=pca_rna$x[,1], PC2=pca_rna$x[,2], PC3=pca_rna$x[,3], clinical_combat[colnames(rna_corrected),c("GeneExp_Subtype","gender.x","batch_number")])
df$batch_number <- as.character(df$batch_number)

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from miRNA data corrected")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from miRNA data corrected")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from miRNA data corrected")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from miRNA data corrected")


# Association with clinical variables
clin <- clinical[colnames(met_s2),]
com <- jiveRes@scores$common
res <- matrix(ncol=2, nrow=length(clin_var))
for(i in 1:length(clin_var)){
  v <- clin[,clin_var[i]]
  if(is.factor(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- kruskal.test(x,v); return((log10(a$p.value))*-1)})
  }
  if(is.numeric(v)){
    res[i,] <- apply(com,2,FUN=function(x){a <- cor.test(x,v, method="spearman"); return((log10(a$p.value))*-1)})
  }
}
colnames(res) <- paste("PC",1:2, sep="") 
rownames(res) <- clin_var


cors <- res
cellcol<-color.scale(cbind(cors, rep(max(res)*-1,nrow(cors)),rep(min(res)*-1,nrow(cors))),0,c(0,1),c(1,0.2))[,1:ncol(cors)]
par(mar = c(10,8,4,2) + 0.1)
color2D.matplot(cors, cellcolors=cellcol, show.legend=TRUE, show.values=2,
                axes=FALSE, xlab="", ylab="")

axis(1,at=0.5:1.5,las=2,labels=colnames(cors))
axis(2,at=0.5:13.5,las=2,labels=rev(rownames(cors)))




#######################################
# Common data after batch correction  #
#######################################


venn.diagram(list(rna=colnames(rna_corrected), mirna=colnames(mirna)), fill=c("red","blue"), cat.fontface=4, lty=2, fontfamily=3, filename="m1.tiff")
venn.diagram(list(rna=colnames(rna_corrected), met=colnames(met)), fill=c("red","green"), cat.fontface=4, lty=2, fontfamily=3, filename="m2.tiff")
venn.diagram(list(rna=colnames(rna_corrected), met=colnames(met), mirna=colnames(mirna)), fill=c("red","green","blue"), cat.fontface=4, lty=2, fontfamily=3, filename="m3.tiff")


share_rna_mirna <- intersect(colnames(rna_corrected),colnames(mirna))
share_rna_met <- intersect(colnames(rna_corrected),colnames(met))

# First subset: rna vs mirna
rna_s1 <- rna_corrected[,share_rna_mirna] # 12,042 x 515
mirna_s1 <- mirna[,share_rna_mirna] # 534 x 515
clinical_s1 <- clinical[share_rna_mirna,] # 515 x 53

# Second subset: rna vs methylation
rna_s2 <- rna_corrected[,share_rna_met] # 12,042 x 83
met_s2 <- met[,share_rna_met] # 382,452 x 8
clinical_s2 <- clinical[share_rna_met,] # 83 x 53


# Expression data subset 1: 12,042 x 515 (intersection with miRNA)

pca_rna <- prcomp(t(rna_s1))
var_prcomp <- pca_rna$sdev^2


# Plot PCs variance

plot(var_prcomp/sum(var_prcomp), xlim = c(0, 15), type = "b", pch = 16, xlab = "principal components", 
     ylab = "variance explained", main="RNA - Princial Components variance")

# Plot PCs scores

df <- data.frame(PC1=pca_rna$x[,1], PC2=pca_rna$x[,2], PC3=pca_rna$x[,3], clinical_s1[,c("GeneExp_Subtype","gender.x","batch_number")])
df$batch_number <- as.character(df$batch_number)

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from RNA data corrected subset 1")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from RNA data corrected subset 1")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from RNA data corrected subset 1")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from RNA data corrected subset 1")




# Expression data subset 2: 12,042 x 83 (intersection with methylation)

pca_rna <- prcomp(t(rna_s2))
var_prcomp <- pca_rna$sdev^2


# Plot PCs variance

plot(var_prcomp/sum(var_prcomp), xlim = c(0, 15), type = "b", pch = 16, xlab = "principal components", 
     ylab = "variance explained", main="RNA - Princial Components variance")

# Plot PCs scores

df <- data.frame(PC1=pca_rna$x[,1], PC2=pca_rna$x[,2], PC3=pca_rna$x[,3], clinical_s2[,c("GeneExp_Subtype","gender.x","batch_number")])
df$batch_number <- as.character(df$batch_number)

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from RNA data corrected subset 2")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(GeneExp_Subtype)), size=5, shape=20) +
  guides(color=guide_legend("GeneExp_Subtype"),fill=guide_legend("GeneExp_Subtype")) +
  ggtitle("Principal Components from RNA data corrected subset 2")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC2, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from RNA data corrected subset 2")

ggplot(df) +
  geom_point(aes(x=PC1, y=PC3, color=factor(batch_number)), size=5, shape=20) +
  guides(color=guide_legend("batch_number"),fill=guide_legend("batch_number")) +
  ggtitle("Principal Components from RNA data corrected subset 2")




##############################################
# Tumour Type distribution through datasets  #
##############################################

# Summary of phenotypic information

# Subset 1:
group <- (clinical_s1$GeneExp_Subtype)
gg <- data.frame(subtype=levels(group), values=summary(group))
p <-ggplot(gg, aes(subtype,values))
p + geom_bar(stat = "identity", aes(fill = subtype)) +
  ggtitle("Subset 1: GBM subtype distribution")

# Subset 2:
group <- (clinical_s2$GeneExp_Subtype)
gg <- data.frame(subtype=levels(group), values=summary(group))
p <- ggplot(gg, aes(subtype,values))
p +geom_bar(stat = "identity", aes(fill = subtype)) +
  ggtitle("Subset 2: GBM subtype distribution")




####################################################
# Integration approaches: Subset 1 (mRNA + miRNA)  #
####################################################

## Common components
common <- selectCommonComps(X=as.matrix(rna_s1), Y=as.matrix(mirna_s1), Rmax=4)
common$common  #only 1 common component!
grid.arrange(common$pssq, common$pratios, ncol=2)

## Distinctive components
pca1 <- PCA.selection(Data=as.matrix(rna_s1), fac.sel="single%", varthreshold=0.05)
pca1$numComps # 4 components, 1 common and 3 distinctive

#Plot the explained variance and the accumulated variance of the first 10 components
pcadf1 <- data.frame(comps=factor(rep(1:10,2),levels=1:10),
                     var=unlist(c(pca1$PCAres$var.exp[1:10,])),
                     label=rep(colnames(pca1$PCAres$var.exp),each=10))
ggplot(pcadf1,aes(x=comps,y=var,fill=label))+
  geom_bar(stat="identity",position="dodge")+
  ggtitle("Expression dataset (mRNA)")+
  geom_hline(yintercept=0.05,linetype="dotted")

pca2 <- PCA.selection(Data=as.matrix(mirna_s1), fac.sel="single%",varthreshold=0.05)
pca2$numComps # 4 components, 1 common and 3 distinctive

#Plot the explained variance and the accumulated variance of the first 10 components
pcadf2 <- data.frame(comps=factor(rep(1:10,2),levels=1:10),
                     var=unlist(c(pca2$PCAres$var.exp[1:10,])),
                     label=rep(colnames(pca2$PCAres$var.exp),each=10))
ggplot(pcadf2,aes(x=comps,y=var,fill=label))+
  geom_bar(stat="identity",position="dodge")+
  ggtitle("Expression dataset (miRNA)")+
  geom_hline(yintercept=0.05,linetype="dotted")



