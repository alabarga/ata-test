

# Correlation NA - VARIABLES

library(stats)
library(ggplot2)
library(purrr)

Correlation_NA_variables = function(clinical, clin_var, clinic_var_ID_withoutNA,
                                    main="", low = "#FFFFFF", high = "#132B43", maxlogPvalue = NA){
  
  M <- clinical[,clin_var]
  res <- matrix(ncol=ncol(M), nrow=ncol(M))
  
  poss_cor_test <- possibly(cor.test, otherwise = NA)
  poss_kruskal_test <- possibly(kruskal.test, otherwise = NA)
  poss_chisq_test <- possibly(chisq.test, otherwise = NA)
  
  for(i in 1:ncol(M)){
    v1 <- as.factor(ifelse(is.na(M[, i]), 0, 1))
    for(j in 1:ncol(M)){
      v2 <- M[,j]
      if(class(v1)=="Surv"){ 
        res[i,j] <- log10(summary(coxph(v1~v2))$logtest['pvalue'])*-1 
      }else{
        if(class(v2)=="Surv"){ 
          res[i,j] <- log10(summary(coxph(v2~v1))$logtest['pvalue'])*-1 
        }
        else{
          if(is.factor(v2) & is.numeric(v1)){ 
            if (is.na(poss_kruskal_test(v1,v2))) 
            {res[i,j] <- NA}
            else { res[i,j] <- log10(poss_kruskal_test(v1,v2)$p.value)*-1 } }
          
          if(is.factor(v1) & is.numeric(v2)){ 
            if (is.na(poss_kruskal_test(v2,v1))) 
            {res[i,j] <- NA}
            else { res[i,j] <- log10(poss_kruskal_test(v2,v1)$p.value)*-1 } }
          
          if(is.factor(v1) & is.factor(v2)){ 
            if (is.na(poss_chisq_test(v2,v1)) ) 
            {res[i,j] <- NA}
            else { res[i,j] <- log10(chisq.test(v2,v1)$p.value)*-1 }}
          
          if(is.numeric(v1) & is.numeric(v2)) { 
            if (is.na(poss_cor_test(v2,v1, method="spearman")) ) 
            {res[i,j] <- NA}
            else {res[i,j] <- log10(poss_cor_test(v2,v1, method="spearman")$p.value)*-1}
          }}
      }}}
  
  colnames(res) <- clin_var
  rownames(res) <- clin_var
  
  res <- res[!(rownames(res) %in% clinic_var_ID_withoutNA), ]
  
  varNames = vector()
  varNames2 = vector()
  logcorrs = vector()
  for (i in 1:ncol(res)){
    varNames = c(varNames,rep(colnames(res)[i],nrow(res)))
    varNames2 = c(varNames2, rownames(res))
    logcorrs = c(logcorrs,res[,i])
  }
  
  
  arrayData <- data.frame(logcorrs, varNames, varNames2)
  
  # Correlation heatmap
  p = ggplot(arrayData, aes(x=varNames,y=varNames2,fill=logcorrs))+
    geom_tile()+
    theme_bw()+
    xlab("Clinical variables")+ylab("Identity variable missings clinical variables")+
    scale_fill_gradient(low=low, high=high, limits = c(0, maxlogPvalue), name = '-log10(pvalues)', na.value="cadetblue3")+
    ggtitle(main)+
    theme(legend.position="bottom", axis.text.x = element_text(angle = 90, vjust = 0.5), plot.title = element_text(hjust = 0.5))
  p
  
  # write.xlsx(res, pathname, colnames=TRUE, rownames=TRUE)
} 


Correlation_NA_variables(clinic_data_ID, var_clinic_ID, clinic_var_ID_withoutNA)







### CORRELATION BETWEEN VARIABLES ###

library(purrr)

clinCorrelation = function(clinical, clin_var,
                           main="", low = "#FFFFFF", high = "#132B43", maxlogPvalue = NA){
  
  M <- clinical[,clin_var]
  res <- matrix(ncol=ncol(M), nrow=ncol(M))
  
  poss_cor_test <- possibly(cor.test, otherwise = NA)
  poss_kruskal_test <- possibly(kruskal.test, otherwise = NA)
  poss_chisq_test <- possibly(chisq.test, otherwise = NA)
  
  for(i in 1:ncol(M)){
    v1 <- M[,i]
    for(j in 1:ncol(M)){
      v2 <- M[,j]
      if(class(v1)=="Surv"){ 
        res[i,j] <- log10(summary(coxph(v1~v2))$logtest['pvalue'])*-1 
      }else{
        if(class(v2)=="Surv"){ 
          res[i,j] <- log10(summary(coxph(v2~v1))$logtest['pvalue'])*-1 
        }
        else{
          if(is.factor(v2) & is.numeric(v1)){ 
            if (is.na(poss_kruskal_test(v1,v2))) 
            {res[i,j] <- NA}
            else { res[i,j] <- log10(poss_kruskal_test(v1,v2)$p.value)*-1 } }
          
          if(is.factor(v1) & is.numeric(v2)){ 
            if (is.na(poss_kruskal_test(v2,v1))) 
            {res[i,j] <- NA}
            else { res[i,j] <- log10(poss_kruskal_test(v2,v1)$p.value)*-1 } }
          
          if(is.factor(v1) & is.factor(v2)){ 
            if (is.na(poss_chisq_test(v2,v1)) ) 
            {res[i,j] <- NA}
            else { res[i,j] <- log10(chisq.test(v2,v1)$p.value)*-1 }}
          
          if(is.numeric(v1) & is.numeric(v2)) { 
            if (is.na(poss_cor_test(v2,v1, method="spearman")) ) 
            {res[i,j] <- NA}
            else {res[i,j] <- log10(poss_cor_test(v2,v1, method="spearman")$p.value)*-1}
          }}
      }}}
  
  colnames(res) <- clin_var
  rownames(res) <- clin_var
  
  varNames = vector()
  varNames2 = vector()
  logcorrs = vector()
  for (i in 1:ncol(res)){
    varNames = c(varNames,rep(colnames(res)[i],nrow(res)))
    varNames2 = c(varNames2, rownames(res))
    logcorrs = c(logcorrs,res[,i])
  }
  
  arrayData <- data.frame(logcorrs, varNames, varNames2)
  
  # Correlation heatmap
  p = ggplot(arrayData, aes(x=varNames,y=varNames2,fill=logcorrs))+
    geom_tile()+
    theme_bw()+
    xlab("Clinical variables")+ylab("Clinical variables")+
    scale_fill_gradient(low=low, high=high, limits = c(0, maxlogPvalue), name = '-log10(pvalues)', na.value="cadetblue3")+
    ggtitle(main)+
    theme(legend.position="bottom", axis.text.x = element_text(angle = 90, vjust = 0.5, size=13), plot.title = element_text(hjust = 0.5), 
          axis.text.y = element_text(size=13))
  
  p
  
  # write.xlsx(res, pathname, colnames=TRUE, rownames=TRUE)
} 



clinCorrelation(clinic_data_ID, colnames_clinic_data_ID)



