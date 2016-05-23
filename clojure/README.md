# How configure and run tests

- 1. Edit `resources/test-config.edn` and change it to something

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

- 2. To run the tests

```
export TIMBRE_NS_BLACKLIST='["kvlt.*"]'
boot test
```

NB! The export disables logging from HTTP framework.

- 3. Tests results as junit XML files can be found under `target/` directory.

