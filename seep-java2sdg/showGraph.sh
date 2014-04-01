dot -Tps test.dot -o test.ps
ps2pdf test.ps
rm test.dot
rm test.ps
evince test.pdf

