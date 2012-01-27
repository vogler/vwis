f=results.txt
echo -n "" > $f
cat /proc/cpuinfo >> $f
echo "------------" >> $f
for i in 0 1 2 3 10
do
	./DB $i | tee -a $f
done
