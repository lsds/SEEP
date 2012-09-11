NAME=$1
export JAVA_HOME=/usr/lib/jvm/java-6-sun/
export RAIN_HOME=/homes/rc3011/Desktop/development/java/sEEP/scripts/RainToolkit
export AWS_ACCESS_ID=AKIAJNHKQUAXCFMGVNZA
export AWS_SECRET_KEY=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
echo "Creating new machine..."

$RAIN_HOME/bin/terminate-virtual-machine -f -n $NAME
$RAIN_HOME/bin/delete-virtual-machine -n $NAME
