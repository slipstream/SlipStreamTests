BOOT = boot
BOOT_TEST = $(BOOT) --no-colors func-test

all: clojure-test
test: all

clojure-test-debug:
	cd clojure && boot --no-colors -v test

clojure-test:
	cd clojure && \
		$(BOOT_TEST)

test-clojure-deps:
	cd clojure && \
		$(BOOT) show -d

test-auth:
	cd clojure && \
		$(BOOT_TEST) -n sixsq.slipstream.authn-test $(TESTOPTS)

test-run-comp:
	cd clojure && \
		$(BOOT_TEST) -n sixsq.slipstream.run-comp-test $(TESTOPTS)

test-run-app:
	cd clojure && \
		$(BOOT_TEST) -n sixsq.slipstream.run-app-test $(TESTOPTS)

test-run-app-scale:
	cd clojure && \
	    $(BOOT_TEST) -n sixsq.slipstream.run-app-scale-test $(TESTOPTS)

clean:
	rm -rf clojure/target

