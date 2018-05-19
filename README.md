# SlipStreamTests

This module contains SlipStream functional tests and utilities to run
them from various execution contexts.

## Run Tests with Leiningen

The most direct way to run the tests is via Leiningen. These tests use
the SlipStream Clojure API to test features of the SlipStream server.

### Configuration

The functional tests are controlled via a configuration file on the
classpath. Edit the `resources/test-config.edn` file and provide the
parameter values for the tests.  The file looks like:

```
{
 :endpoint "https://nuv.la"
 :username "your-username"
 :password "your-password"

 :connector-name "exoscale-ch-gva"

 :comp-uri "examples/tutorials/service-testing/apache"
 :app-uri "examples/tutorials/service-testing/system"
 :comp-name "testclient"

 :results-dir "your-directory"
}
```

The application in the example configuration above is publicly available 
and can be used.

### Run All Tests

To run all the tests

```
export TIMBRE_NS_BLACKLIST='["kvlt.*"]'
lein test2junit 
```

The export will disable the logging output from the underlying HTTP
library.  If you want to see the details of the HTTP requests and
responses, do not set the environmental variable.

### Run a Specific Test

To run the tests within a given namespace, use the command:

```
lein test2junit sixsq.slipstream.run-comp-test
```

The namespace you provide must exist.

### Test Results

The XML JUnit test results are written to the directory
`target/test-results`.


## Run Tests with `make`

Running the functional tests via `make` is primarily intended for
automated execution via Jenkins and SlipStream.  Nonetheless, they can
be run manually, usually for debugging. 

To invoke a test (or tests) via `make`, use the following command:

```
$ make <test-name> TESTOPTS=" --param value ..."  
```

where `<test-name>` is the name of the test as defined in Makefile or
all. For example, 

``` 
$ make test-auth "TESTOPTS= -u user -p pass -s https://nuv.la -d ~/test-results"
  ...
$ make test-run-app-scale "TESTOPTS= -u user -p pass -s https://1.2.3.4 --insecure -c exoscale-ch-gva -d ~/test-results --app-uri examples/tutorials/service-testing/system --comp-uri testclient"
  ...
```

The last command will run `run-app-scale` test (deploy application and
scale up/down its named component) on the cloud `exoscale-ch-gva`. It
will use `examples/tutorials/service-testing/system` application and
will be scaling its `testclient` component.  The results of the tests
will be stored into `~/test-results` under the `exoscale-ch-gva`
sub-folder.  The connections to the service will be made in insecure
mode (i.e., no checking of the server's authenticity will be made).

For the full set of options, run the following:

```
$ lein run -m sixsq.slipstream.prepare -- --help
```

Note that not all options are required for all functional tests. 

## Maven

The maven `pom.xml` file is simply to keep the version in sync with
the other SlipStream modules.  The functional tests cannot be run with
`mvn`. 