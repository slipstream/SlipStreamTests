clojure-test-debug:
	cd clojure && boot -v test

clojure-test:
	cd clojure && { export TIMBRE_NS_BLACKLIST='["kvlt.*"]'; boot test; }

all: clojure-test
test: all

