jar=$1
properties=$2
auth=$3
machines=()

for machine in "${machines[@]}";do
	scp -i $auth $jar ubuntu@"${machine}:/home/ubuntu"
	scp -i $auth $properties ubuntu@"${machine}:/home/ubuntu"
done
