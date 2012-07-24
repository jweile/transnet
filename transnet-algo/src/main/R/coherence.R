#!/usr/bin/Rscript

data <- read.delim("ns_coherence.csv",header=TRUE)
colnames(data) <- sapply(colnames(data),substring,35)

pdf(file="coherence.pdf",width=7,height=14)
op <- par(mfrow=c(4,2))

for (i in 1:ncol(data)) {
	hist(data[,i],main=colnames(data)[i],xlab="#Agreeing XRefs")
}

par(op)
dev.off()

cat("Mean coherence:\n")
apply(data,2,function(x) mean(na.omit(x)))