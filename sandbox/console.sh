#! /bin/bash
cd console-core
rm -rf target && maven -o
cd ../console-standard
rm -rf target && maven -o
cd ../console-framework
rm -rf target && maven -o
cd ../console-ear
maven -o clean && maven -o
cd ..
cp -rf ~/.maven/repository/portlet-api ../modules/assembly/target/geronimo-1.0-SNAPSHOT/repository/
cp -rf ~/.maven/repository/pluto ../modules/assembly/target/geronimo-1.0-SNAPSHOT/repository/
cp -rf ~/.maven/repository/geronimo/jars/geronimo-console-core*.jar ../modules/assembly/target/geronimo-1.0-SNAPSHOT/repository/geronimo/jars/
cp -rf ~/.maven/repository/geronimo-console ../modules/assembly/target/geronimo-1.0-SNAPSHOT/repository/
echo "java -jar ../modules/assembly/target/geronimo-1.0-SNAPSHOT/bin/deployer.jar --user system --password manager deploy console-ear/target/geronimo-console-1.0-SNAPSHOT.ear"
