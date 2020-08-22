CLANG_FORMAT=node_modules/clang-format/bin/linux_x64/clang-format --style=Google

node_modules:
	npm install clang-format eslint prettier eslint-plugin-react

pretty: node_modules
	find src/main/java -iname *.java | xargs $(CLANG_FORMAT) -i
	find src/test/java -iname *.java | xargs $(CLANG_FORMAT) -i
	cd src/main/react && npx prettier --write src/**/*.js

validate: node_modules
	cd src/main/react && npx eslint src/**/*.js

package:
	mvn package