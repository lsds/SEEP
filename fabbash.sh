jar=$1
properties=$2
auth=$3
machines=(ec2-23-22-103-223.compute-1.amazonaws.com ec2-23-22-198-128.compute-1.amazonaws.com ec2-54-235-231-182.compute-1.amazonaws.com ec2-54-242-86-144.compute-1.amazonaws.com ec2-184-73-74-152.compute-1.amazonaws.com ec2-23-22-194-11.compute-1.amazonaws.com ec2-54-242-83-79.compute-1.amazonaws.com ec2-75-101-183-204.compute-1.amazonaws.com ec2-107-22-133-231.compute-1.amazonaws.com ec2-107-20-99-91.compute-1.amazonaws.com)

for machine in "${machines[@]}";do
	scp -i $auth $jar ubuntu@"${machine}:/home/ubuntu"
	scp -i $auth $properties ubuntu@"${machine}:/home/ubuntu"
done
