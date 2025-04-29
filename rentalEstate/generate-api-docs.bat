@echo off
echo Generating API documentation...

REM Build the Doclet
cd doclet
call mvn clean package
cd ..

REM Run Javadoc with the Doclet
javadoc -doclet com.giang.rentalEstate.doclet.ApiDoclet -docletpath doclet/target/api-doclet-1.0.0.jar -d api-docs -sourcepath src/main/java -subpackages com.giang.rentalEstate.controller

echo API documentation generated in api-docs directory.
echo Open api-docs/index.html to view the documentation. 