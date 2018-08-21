VERSION=${1:-9.3.1.Final}
HOME=/home/gfernandes/github/infinispan/server/integration/build/target/infinispan-server*
cp ./client/config/clustered-$VERSION.xml $HOME/standalone/configuration/clustered.xml

$HOME/bin/add-user.sh -u dev -p dev -a
export  JAVA_OPTS="-Xmx12048m -Djava.net.preferIPv4Stack=true"
$HOME/bin/standalone.sh -c clustered.xml 
