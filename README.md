# Unique Id generator

Based on http://blog.boundary.com/2012/01/12/Flake-A-decentralized-k-ordered-id-generation-service-in-Erlang.html
which is in turn based on Twitter's Snowflake project: http://engineering.twitter.com/2010/06/announcing-snowflake.html

Generates unique ids based on the current epoch time, current machine identity,
and a counter. The result are mostly ordered unique ids that require no
synchronization between machines.


# Compile and run

    javac -cp .:./lib/uuid-3.2.1-SNAPSHOT.jar:./lib/commons-codec-1.6.jar src/uniq/UniqueId.java
    java -cp .:./lib/uuid-3.2.1-SNAPSHOT.jar:./lib/commons-codec-1.6.jar:src uniq.UniqueId

# Performance

2011 MacBook Pro, 2.8Ghz i7

## Cat to file

    $ time java -cp .:./lib/uuid-3.2.1-SNAPSHOT.jar:./lib/commons-codec-1.6.jar:src uniq.UniqueId 10000000 > out
    
    real    0m18.498s
    user    0m5.662s
    sys     0m12.973s

    $ ls -lh out
    -rw-r--r--  1 mumrah  staff   153M May 22 10:52 out

540 id/ms

Since each id is 128 bits, the I/O rate comes out to 8640000 bytes per second, or 8.24 MB/s.
Compare this to 66.59 MB/s which is the speed that the same system can "yes > omgyes". I guess that order of
magnitude difference is the price of safety/synchronization.

## Pipe to /dev/null

    $ time java -cp .:./lib/uuid-3.2.1-SNAPSHOT.jar:./lib/commons-codec-1.6.jar:src uniq.UniqueId 10000000 > /dev/null

    real    0m7.794s
    user    0m4.688s
    sys     0m3.253s

1280 id/ms

## Pipe to fifo, fifo to /dev/null

    $ time java -cp .:./lib/uuid-3.2.1-SNAPSHOT.jar:./lib/commons-codec-1.6.jar:src uniq.UniqueId 10000000 > foo

    real    0m18.051s
    user    0m5.594s
    sys     0m12.594s

554 id/ms


