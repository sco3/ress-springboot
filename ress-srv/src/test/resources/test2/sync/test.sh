#curl -k  'http://localhost:8009/cci/api/v1/delay?delay=1000&size=100000'
/usr/bin/time --append -f '%e'  -o times.log curl -k  'http://localhost:8009/cci/api/v1/delay?delay=10000&size=10000'  > /dev/null 2>&1 

