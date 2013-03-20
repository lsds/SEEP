INSTANCE=$1
#echo "copy cpuUOld"
#scp -i authEC2.pem cpuUOld ec2-user@$INSTANCE:/home/ec2-user
#echo "copy cpuU"
#scp -i authEC2.pem cpuU ec2-user@$INSTANCE:/home/ec2-user
#echo "copy ft script"
#scp -i authEC2.pem ft-script ec2-user@$INSTANCE:/home/ec2-user
echo "copy deploy-local"
scp -i ../authEC2.pem deploy-local ubuntu@$INSTANCE:/home/ubuntu
#scp -i authEC2.pem authEC2.pem ec2-user@$INSTANCE:/home/ec2-user
echo "copy jar"
scp -i ../authEC2.pem ../../dist/SEEPv0.1-20130318.jar ubuntu@$INSTANCE:/home/ubuntu
echo "copy query"
scp -i ../authEC2.pem ../../../cf-stream/dist/cf-stream.jar ubuntu@$INSTANCE:/home/ubuntu
echo "copy config-file"
scp -i ../authEC2.pem ../../config.properties ubuntu@$INSTANCE:/home/ubuntu
#echo "copy theHackerCrackdown"
#scp -i authEC2.pem theHackerCrackdown.txt ec2-user@$INSTANCE:/home/ec2-user
echo "CONNECTING"
ssh -i ../authEC2.pem ubuntu@$INSTANCE
