#!/bin/bash

WORKER1_VMNAME="Worker1 - Custom Phone 7 - 4.3 - API 18 - 1024x600" 
WORKER1_VMID="2f77ff2f-2c63-401c-be06-2db443984b11"

WORKER2_VMNAME="Worker2 - Custom Phone 7 - 4.3 - API 18 - 1024x600"
WORKER2_VMID="138f9585-2ff3-477d-94fd-abd5e43b40d3"

WORKER3_VMNAME="Worker 3 - Custom Phone 7 - 4.3 - API 18 - 1024x600"
WORKER3_VMID="cedadee1-6c13-436d-89f3-0b75bceb6d1d"

WORKER4_VMNAME="Worker 4 - Custom Phone 7 - 4.3 - API 18 - 1024x600"
WORKER4_VMID="fc8a9429-3d31-4988-8a08-aeb1291e504a"

WORKER5_VMNAME="Worker 5 - Custom Phone 7 - 4.3 - API 18 - 1024x600"
WORKER5_VMID="21bb40b0-67eb-495f-92a7-2a261b5cbd65"

WORKER6_VMNAME="Worker 6 - Custom Phone 7 - 4.3 - API 18 - 1024x600"
WORKER6_VMID="ae3af0bf-e185-4cc8-842b-9bb07df32d55"

echo "Enabling VT-X for $WORKER1_VMID"
vboxmanage modifyvm $WORKER1_VMID --hwvirtex on 
vboxmanage modifyvm $WORKER2_VMID --hwvirtex on 
vboxmanage modifyvm $WORKER3_VMID --hwvirtex on 
vboxmanage modifyvm $WORKER4_VMID --hwvirtex on 
vboxmanage modifyvm $WORKER5_VMID --hwvirtex on 
vboxmanage modifyvm $WORKER6_VMID --hwvirtex on 

#vboxmanage showvminfo $WORKER1_VMID
