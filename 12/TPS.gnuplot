set encoding utf8
set title "OLTP/OLAP Benchmark"
set style data histogram
set style histogram cluster gap 2
set style fill solid border -1
set boxwidth 1
set datafile separator ','
set yrange [0:2100000]
set format y "%.0e"
set ylabel "TPS" enhanced
unset xtics
set key title "OLAP-Prozesse" box

set output 'TPS.pdf'
set terminal pdfcairo font 'Arial,8'
plot 'results.csv' using 2 ti col, '' using 3 ti col, '' using 4 ti col, '' using 5 ti col

set output 'TPS.png'
set terminal pngcairo font 'Arial,8'
replot
