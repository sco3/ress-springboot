#!/bin/bash
set -e

# Start Cassandra in background, redirect its verbose output
/docker-entrypoint.sh cassandra -f > /dev/null 2>&1 &
CASSANDRA_PID=$!

# Wait for Cassandra native transport (port 9042) to be ready
echo "Waiting for Cassandra to start..."
while ! (echo > /dev/tcp/localhost/9042) > /dev/null 2>&1; do
    sleep 2
done
# Extra wait for gossip to settle
sleep 10
echo "Cassandra is ready."

# Run init script if it exists
if [ -f /init.cql ]; then
    echo "Running /init.cql..."
    cqlsh -f /init.cql
    echo "Init script completed."
fi

# Run data script if it exists
if [ -f /ress-data.cql ]; then
    echo "Running /ress-data.cql..."
    cqlsh -f /ress-data.cql
    echo "Data script completed."
fi

# Keep Cassandra in foreground
wait $CASSANDRA_PID
