# How configure and run tests

## Configuration

Edit `resources/test-config.edn` and change it to something

```
{
 :username "yourname"
 :password "yourpass"
 :serviceurl "https://nuv.la"
 :app-uri "konstan/scale/scale-test-dpl"
 :comp-name "testvm"
 }
```

The application in the example configuration above is publicly available 
and can be used.

## Running all tests

To run all the tests

```
export TIMBRE_NS_BLACKLIST='["kvlt.*"]'
boot test
```

NB! The export disables logging from HTTP framework.

## Runing a specific test suite or a test

To run all tests from `sixsq.slipstream.run-comp-test` namespace

```
boot test -n sixsq.slipstream.run-comp-test
```

To run `test-authn` test from `sixsq.slipstream.athn-test` namespace

```
boot test -n sixsq.slipstream.athn-test -f '(re-find #"test-authn" (str %))'
```

From the issue [Running a single test from namespace](https://github.com/adzerk-oss/boot-test/issues/7#issuecomment-124024517) in `boot-test`.

## Test resutls

By default tests results as junit XML files can be found under `target/` directory.  

Starting from `boot-test v1.1.2` ([option :junit-output-to must be of type str](https://github.com/adzerk-oss/boot-test/issues/24)) junit XML test results can be redirected to a custom directory under `target/` with `-j`

```
boot test -n sixsq.slipstream.run-comp-test -j exoscale-ch-gva
```
