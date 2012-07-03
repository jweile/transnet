#!/usr/bin/Rscript

#
#  Copyright (C) 2011 The Roth Lab
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU Lesser General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU Lesser General Public License for more details.
#
#  You should have received a copy of the GNU Lesser General Public License
#  along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

# author: Jochen Weile <jochenweile@gmail.com>

ns <- read.delim("xref_freqs.csv");

#remove prefixes from column names
colnames(ns) <- as.vector(sapply(colnames(ns), substring, first=35));

#plot histograms for xref frequencies
pdf(file="histograms.pdf", width=8, height=28);
op <- par(mfrow=c(7,2));
for (i in 1:ncol(ns)) {
	hist(ns[,i], xlab="#xrefs", main=colnames(ns)[i]);
}
par(op);
dev.off();

#plot boxplot for xref frequencies
pdf(file="boxplot.pdf", width=14, height=8);
op <- par(las=3, omi=c(1.5,0,0,0))
boxplot(ns, main="XRef frequencies per NS", ylab="#Xrefs");
par(op)
dev.off();


clusters <- read.delim("xref_clusters_sorted.csv");

#plot cluster frequencies
pdf(file="clusters.pdf", width=7, height=10);
op <- par(las=3, omi=c(4,0,0,0));
barplot(clusters[,2], names.arg=clusters[,1],log="y",main="Namespace clusters",cex.names=.5);
par(op);
dev.off();
