LEIN = lein
LEIN_TEST = $(LEIN) test2junit

all: clojure-test
test: all

prepare:
	lein run -m sixsq.slipstream.prepare -- $(TESTOPTS)

test-clojure-deps:
	$(LEIN) deps :tree

clojure-test: test-clojure-deps prepare
	$(LEIN_TEST) || true
	lein run -m sixsq.slipstream.copy -- $(TESTOPTS)

test-auth: prepare
	$(LEIN_TEST) sixsq.slipstream.authn-test || true
	lein run -m sixsq.slipstream.copy -- $(TESTOPTS)

test-run-comp: prepare
	$(LEIN_TEST) sixsq.slipstream.run-comp-test || true
	lein run -m sixsq.slipstream.copy -- $(TESTOPTS)

test-run-app: prepare
	$(LEIN_TEST) sixsq.slipstream.run-app-test || true
	lein run -m sixsq.slipstream.copy -- $(TESTOPTS)

test-run-app-scale: prepare
	$(LEIN_TEST) sixsq.slipstream.run-app-scale-test || true
	lein run -m sixsq.slipstream.copy -- $(TESTOPTS)

clean:
	rm -rf clojure/target
