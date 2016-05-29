BOOT = boot --no-colors test

all: clojure-test
test: all

clojure-test-debug:
	cd clojure && boot --no-colors -v test

clojure-test:
	cd clojure && \
		{ export TIMBRE_NS_BLACKLIST='["kvlt.*"]'; \
		  $(BOOT); }

test-auth:
	cd clojure && \
		{ export TIMBRE_NS_BLACKLIST='["kvlt.*"]'; \
		  $(BOOT) -n sixsq.slipstream.authn-test; }

test-run-comp:
	cd clojure && \
		{ export TIMBRE_NS_BLACKLIST='["kvlt.*"]'; \
		  $(BOOT) -n sixsq.slipstream.run-comp-test; }

test-run-app:
	cd clojure && \
		{ export TIMBRE_NS_BLACKLIST='["kvlt.*"]'; \
		  $(BOOT) -n sixsq.slipstream.run-app-test; }

test-run-app-scale:
	cd clojure && \
		{ export TIMBRE_NS_BLACKLIST='["kvlt.*"]'; \
		  $(BOOT) -n sixsq.slipstream.run-app-scale-test; }

clean:
	rm -rf clojure/target

