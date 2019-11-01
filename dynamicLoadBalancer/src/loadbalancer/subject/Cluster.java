package loadbalancer.subject;

import java.util.Map;
import java.util.HashMap;
import loadbalancer.entities.Machine;
import loadbalancer.entities.Service;
import loadbalancer.observer.LoadBalancer;
import loadbalancer.observer.ServiceManager;

public class Cluster implements SubjectI{
		// Hostnames to corresponding machine instances.
		private Map<String, Machine> machines;
		Machine machine;
		Service service;
		LoadBalancer loadbalancer;
		ServiceManager servicemanager;
		/*Machine machine;*/
		public Cluster(){
			this.machines = new HashMap<>();
			loadbalancer = new LoadBalancer();
		}
		public void registerObservers(){}
		
		public void notifyAllObservers(String operation,String hostname,String serviceName, String url){

			loadbalancer.updateObservers(operation,hostname,serviceName,url);
		}
		public void notifyAllObservers(String operation,String hostname){

			loadbalancer.updateObservers(operation,hostname);
		}
		public void notifyAllObservers(String operation,String serviceName,String hostname){

			loadbalancer.updateObservers(operation,serviceName,hostname);
		}

		public void CLUSTER_OP__SCALE_UP(String hostname){

			if(machines.containsKey(hostname)){
				System.out.println(hostname +" "+"already exists in the cluster");
			}
			else{
				this.machines.put(hostname,new Machine(hostname));
				System.out.println(machines);
				System.out.println("Cluster Scaled Up");
			}
		}
		public void CLUSTER_OP__SCALE_DOWN(String hostname){

			if(machines.containsKey(hostname)){
				
				machines.remove(hostname);
				System.out.println(machines);
				System.out.println("Cluster Scaled Down");
				this.notifyAllObservers("CLUSTER_OP__SCALE_DOWN",hostname);
			}
			else{

				System.out.println(hostname+" "+"Does Not Exist In The Cluster");
			}
		}
		public void SERVICE_OP__ADD_SERVICE(String serviceName,String url,String hosts){

			String[] hostList = null;
			Map hostedServices = null;
			hostList = hosts.split(",");
			Service service = new Service(serviceName,url);
			
			for(int i = 0;i < hostList.length;i++){
				
				if(machines.containsKey(hostList[i])){
					
					machine = machines.get(hostList[i]);
					hostedServices = machine.getHostedServices();

					if(hostedServices.containsKey(serviceName)){
						System.out.println("Service already exists for"+" "+hostList[i]);
					}
					else{
						machine.setHostedServices(hostList[i],service);
						System.out.println("Service Added to"+" "+hostList[i]);	
					}
				}
				else{
					System.out.println("Service Not Added Because"+" "+hostList[i]+" "+"Does Not Exist In The Cluster");
				}
			}			

			this.notifyAllObservers("SERVICE_OP__ADD_SERVICE",hosts,serviceName,url);
		}

		public void SERVICE_OP__REMOVE_SERVICE(String serviceName){

			Map hostedServices = null;
			
			for (Map.Entry<String, Machine> entry : machines.entrySet()) {
				String hostname = entry.getKey();
				Machine machine = entry.getValue();
				hostedServices = machine.getHostedServices();

				if(hostedServices.containsKey(serviceName)){
					
					hostedServices.remove(serviceName);
					System.out.println(serviceName+" "+"removed from"+" "+hostname);
				}
			}

			System.out.println("Service Removed");
			this.notifyAllObservers("SERVICE_OP__REMOVE_SERVICE",serviceName);				
		}
		public void SERVICE_OP__ADD_INSTANCE(String serviceName,String hostname){

			Map hostedServices = null;

			for (Map.Entry<String, Machine> entry : machines.entrySet()) {
				
				String host = this.machine.getHostName();
				Machine machine = entry.getValue();
				hostedServices = machine.getHostedServices();

				if((hostedServices.containsKey(serviceName))){
					this.service = machine.getService(serviceName);	
				}
				
			}
			
			machine.setHostedServices(hostname,this.service);
			System.out.println("Instance Added");
			this.notifyAllObservers("SERVICE_OP__ADD_INSTANCE",serviceName,hostname);
		}

		public void SERVICE_OP__REMOVE_INSTANCE(String serviceName,String hostname){

			Map hostedServices = null;
			hostedServices = machine.getHostedServices();
			
			if(machines.containsKey(hostname)){

				if((hostedServices.containsKey(serviceName))){
					hostedServices.remove(serviceName);	
					System.out.println("Instance Removed"+" "+"from"+" "+hostname);
					this.notifyAllObservers("SERVICE_OP__REMOVE_INSTANCE",serviceName,hostname);
				}
				else{
					System.out.println("Instance of"+" "+serviceName+" "+"does not exist for"+" "+hostname);
				}
			}
			else{
				System.out.println(hostname+" "+"Does Not Exist In The Cluster");
			}
				
		}
		public void REQUEST(String serviceName){
			
			String[] result = new String[2];
			this.servicemanager = loadbalancer.getServiceManager(serviceName);	
			System.out.println(this.servicemanager.getHosts()+" "+this.servicemanager.getUrl());
			result = loadbalancer.requestService(serviceName);
			System.out.println(this.servicemanager.getHosts()+" "+this.servicemanager.getUrl());
			System.out.println("Final Result:"+" "+result[0]+" "+result[1]);
		}
	}