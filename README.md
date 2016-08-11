# SlipStreamTests

Check README files in the corresponding modules for how to configure and run the tests defined there.


## Running tests with make

For running functional tests with make

```
$ make <test-name> TESTOPTS=" --param value ..."  
```

where `<test-name>` is the name of the test as defined in Makefile. For example

``` 
$ make test-auth "TESTOPTS= -u user -p pass -s https://nuv.la -d ~/test-results"
  ...
$ make test-run-app-scale "TESTOPTS= -u user -p pass -s https://1.2.3.4 --insecure 
   -c exoscale-ch-gva -c atos-es1 -d ~/test-results 
   --app-uri examples/tutorials/service-testing/system 
   --comp-uri testclient"
  ...
```

The last command will run `run-app-scale` test (deploy application and scale up/down its named 
component) on two clouds `exoscale-ch-gva` and `atos-es1`. It will use 
`examples/tutorials/service-testing/system` application and will be scaling its `testclient` 
component.  The results of the tests will be stored into `~/test-results` under
`exoscale-ch-gva` and `atos-es1` sub-folders respectively.  The connections to the service will 
be made in insecure mode (i.e., no checking of the server's authenticity will be made).

For details on possible options to the functional tests run the following from project's 
`clojure` sub-directory

```
$ boot func-test -h
```

