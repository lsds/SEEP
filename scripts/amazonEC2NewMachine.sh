NAME=$1
export JAVA_HOME=/usr/lib/jvm/java-6-sun/
export RAIN_HOME=/homes/rc3011/Desktop/development/java/sEEP/scripts/RainToolkit
export AWS_ACCESS_ID=AKIAJNHKQUAXCFMGVNZA
export AWS_SECRET_KEY=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
echo "Creating new machine..."
$RAIN_HOME/bin/delete-virtual-machine -n $NAME
#create new machine
$RAIN_HOME/bin/create-virtual-machine -n $NAME -i ami-e565ba8c -g seepP -key authEC2 -t SMALL 
echo "Starting new machine..."
sleep 3 
#start that machine
VM_ADDRESS=`$RAIN_HOME/bin/start-virtual-machine -n $NAME`
echo $VM_ADDRESS
#deploy needed files in the machine and connect
#echo "New Deployment... "
#echo $VM_ADDRESS
#bash ec2-deploy.sh $VM_ADDRESS
#echo "Executing system process..."
#execute secondary program
#java -jar proto_ft_seep-20120130.jar Sec new 146.169.5.130 
#echo "system process executed..."
#return control to java thread
#echo "done"
#exit
