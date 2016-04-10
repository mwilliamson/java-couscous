.PHONY: package

package:
	mvn package -Dmaven.test.skip=true
