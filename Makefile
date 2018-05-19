LEIN = lein
LEIN_TEST = $(LEIN) test2junit

all: clojure-test
test: all

prepare:
	cd clojure && lein run -m sixsq.slipstream.prepare -- $(TESTOPTS)

test-clojure-deps:
	cd clojure && $(LEIN) deps :tree

clojure-test: test-clojure-deps prepare
	cd clojure && $(LEIN_TEST) || true
	cd clojure && lein run -m sixsq.slipstream.copy -- $(TESTOPTS)

test-auth: prepare
	cd clojure && $(LEIN_TEST) sixsq.slipstream.authn-test || true
	cd clojure && lein run -m sixsq.slipstream.copy -- $(TESTOPTS)

test-run-comp: prepare
	cd clojure && $(LEIN_TEST) sixsq.slipstream.run-comp-test || true
	cd clojure && lein run -m sixsq.slipstream.copy -- $(TESTOPTS)

test-run-app: prepare
	cd clojure && $(LEIN_TEST) sixsq.slipstream.run-app-test || true
	cd clojure && lein run -m sixsq.slipstream.copy -- $(TESTOPTS)

test-run-app-scale: prepare
	cd clojure && $(LEIN_TEST) sixsq.slipstream.run-app-scale-test || true
	cd clojure && lein run -m sixsq.slipstream.copy -- $(TESTOPTS)

clean:
	rm -rf clojure/target
