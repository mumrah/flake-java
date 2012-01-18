# Unique Id generator

Based on http://blog.boundary.com/2012/01/12/Flake-A-decentralized-k-ordered-id-generation-service-in-Erlang.html
which is in turn based on Twitter''s Snowflake project: http://engineering.twitter.com/2010/06/announcing-snowflake.html

Generates unique ids based on the current epoch time, current machine identity,
and a counter. The result are mostly ordered unique ids that require no
synchronization between machines.


# Compile and run

    javac -cp .:./lib/uuid-3.2.1-SNAPSHOT.jar:./lib/commons-codec-1.6.jar src/uniq/UniqueId.java
    java -cp .:./lib/uuid-3.2.1-SNAPSHOT.jar:./lib/commons-codec-1.6.jar:src uniq.UniqueId

# Performance

Single threaded:

    $ time java -cp .:./lib/uuid-3.2.1-SNAPSHOT.jar:./lib/commons-codec-1.6.jar:src uniq.UniqueId > out
    
    real        0m10.978s
    user        0m6.234s
    sys	        0m4.262s
    
    $ wc -l out 
    516609 out

My super scientific analysis yields approximately 50 ids per millisecond.

