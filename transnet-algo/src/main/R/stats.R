#!/usr/bin/Rscript

ns <- read.delim("xref_freqs.csv");

#remove prefixes from column names
colnames(ns) <- as.vector(sapply(colnames(ns), substring, first=35));

pdf(file="histograms.pdf", width=8, height=28);
op <- par(mfrow=c(7,2));
for (i in 1:ncol(ns)) {
	hist(ns[,i], xlab="#xrefs", main=colnames(ns)[i]);
}
par(op);
dev.off();

pdf(file="boxplot.pdf", width=14, height=8);
op <- par(las=3, omi=c(1.5,0,0,0))
boxplot(ns, main="XRef frequencies per NS", ylab="#Xrefs");
par(op)
dev.off();


clusters <- read.delim("xref_clusters_sorted.csv");

pdf(file="clusters.pdf", width=7, height=10);
op <- par(las=3, omi=c(4,0,0,0));
barplot(clusters[,2], names.arg=clusters[,1],log="y",main="Namespace clusters",cex.names=.5);
par(op);
dev.off();
