package orchestration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

//
// VM
//
// Stores Virtual Machine information

public class VM extends Node implements Serializable {
	private String id = UUID.randomUUID().toString(); // uuid as string
	double coresAllocated;	// VMs can request fractions of cores.
	int memoryAllocated; 	// MB of memory allocated
	String ipAddress;		// Get the IP address after booting
	String status; 			// Booting, Running, Stopped, Crashed, Terminated
	// HashSet<Integer> serviceInstanceIDs = new HashSet<Integer>();
	HashSet<ServiceInstance> serviceInstances = new HashSet<ServiceInstance>();

	public VM(VM other) {
		id = other.getID();
		coresAllocated = other.coresAllocated;
		memoryAllocated = other.memoryAllocated;
		ipAddress = other.ipAddress;
		status = other.status;
		for (ServiceInstance otherServiceInstance : other.serviceInstances) {
			serviceInstances.add(new ServiceInstance(otherServiceInstance));
		}
	}

	public VM(double coresAllocated, int memoryAllocated) {
		this.coresAllocated = coresAllocated;
		this.memoryAllocated = memoryAllocated;
	}

	public String getID() {
		return id;
	}
}
