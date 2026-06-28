

source ../env.sh

echo $count
for i in $(seq 1 $count) ; do 
    echo $i
   ./test.sh & 
done

