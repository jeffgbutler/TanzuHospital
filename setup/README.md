# Setup

## Create the Kubernetes Cluster (vSphere with Tanzu, aka TKGS, Instructions)

Using the vSphere UI, create a namespace `test-namespace`.

Login to the `test-namespace` namespace with kubectl (replace the IP address and user ID below with the IP address of your vSphere
control plane and your TKGS credentials):

```shell
kubectl vsphere login --server 192.168.139.2 -u administrator@vsphere.local --insecure-skip-tls-verify

kubectl config use-context test-namespace
```

Create a cluster:

```shell
kubectl apply -f 01-CreateCluster.yml
```

Wait for the cluster to be created (about 20-30 minutes). Check progress with the following command:

```shell
kubectl get TanzuKubernetesClusters
```

Logout of the management cluster and login to your new cluster (replace the IP address and user ID below with the IP address of
your vSphere control plane and your TKGS credentials):

```shell
kubectl vsphere logout

kubectl vsphere login --server 192.168.139.2 --tanzu-kubernetes-cluster-namespace test-namespace \
  --tanzu-kubernetes-cluster-name tanzu-hospital -u administrator@vsphere.local \
  --insecure-skip-tls-verify

kubectl config use-context tanzu-hospital
```

## Install Kubeapps

We will use Kubeapps to install MySQL. Kubeapps is installed via Helm chart.

```shell
helm repo add bitnami https://charts.bitnami.com/bitnami

helm repo update
```

Create a namespace for Kubeapps:

```shell
kubectl create namespace kubeapps
```

Users - even admin users - have very little authority in TKGS clusters initially. We'll need to create role bindings
for Kubeapps before we try to install it. So run the role binding script:

```shell
kubectl apply -f 11-KubeappsRoleBindings.yml
```

Now install Kubeapps. This command will install Kubeapps in the `kubeapps` namespace and will provision a load balancer for
the UI. Once you run this command, it will take a few minutes to install. You can monitor the progress by watching
pod creation status in the `kubeapps` namespace.

```shell
helm install kubeapps --namespace kubeapps --set frontend.service.type=LoadBalancer bitnami/kubeapps
```

You can watch the progress of the Kubeapps install with the following (takes 2-3 minutes):

```shell
kubectl get all -n kubeapps
```

Create a simple service account for interacting with Kubeapps (note this is not recommended for production clusters).

```shell
kubectl create --namespace default serviceaccount kubeapps-operator

kubectl create clusterrolebinding kubeapps-operator --clusterrole=cluster-admin --serviceaccount=default:kubeapps-operator
```

Now obtain the secret for logging in to Kubeapps:

```shell
kubectl get secret $(kubectl get serviceaccount kubeapps-operator -o jsonpath='{range .secrets[*]}{.name}{"\n"}{end}' \
  | grep kubeapps-operator-token) -o jsonpath='{.data.token}' -o go-template='{{.data.token | base64decode}}' && echo
```

Save the token somewhere convenient for logging in to Kubeapps. The token will look something like the following.

```
eyJhbGciOiJSUzI1NiIsImtpZCI6InJiWlFuSHJnWUU0Y0JPZ1RIbll0WlNxZlE5WjZHVldzcmxoT3Y3ZXAwRjQifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6Imt1YmVhcHBzLW9wZXJhdG9yLXRva2VuLWY1czV3Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6Imt1YmVhcHBzLW9wZXJhdG9yIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQudWlkIjoiODFiNTczMWQtOGNiYi00MzNjLWIwOTctNTRlODEwNGU3ZmM2Iiwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50OmRlZmF1bHQ6a3ViZWFwcHMtb3BlcmF0b3IifQ.Cwoq53qXRdD98Jr28FKg5kHd9l225HY0aiS0HcCf6i_749MK7bXB5dUiHrBZWAGKIki_KlPWW8s_-Ev97o2_TVKEwLxxXAPnSouznk3DM6vFYc5NlShGWl79I6j7JEjEJE_kPtnF60SeJq0tM-4bOsUZ7uCS_jsdbBV2PPLa8S1EPBkpEaP3GDzOeRjoRf7VUdxF-iJ-tLm3hhibewzqgk8COqFzcwuv51a75zSpRjraaXTR3Eb4ioL1uY55X8d0BjSZFZf_W-j-Y3W-qaIiLyaQ4O34EMOpEiG9XJPq4DT1u5baJgnhBY2amWvjQv7f34aPAlWIASU7ixx1yusUZQ
```

Get the IP address of the kubeapps service:

```shell
kubectl get svc kubeapps -n kubeapps
```

Open a browser window to Kubeapps (http://192.168.139.6), sign in with token above.

## Install MySQL

Create a namespace for the application:

```shell
kubectl create namespace tanzu-hospital
```

Apply a role binding to allow for pods to be deployed in the new namespace:

```shell
kubectl apply -f 21-RoleBinding.yml
```
Install MySQL (MariaDB) With the Kubeapps UI:

1. Login to Kubeapps if you are not already logged in
2. Set the Current context in Kubeapps to the `tanzu-hospital` namespace (you may need to refresh for the new namespace to appear)
3. Go to the "Catalog" tab and search for "mariadb"
4. Choose "mariadb"
5. Choose "Deploy" for the latest version
6. Change the name to "tanzu-hospital-mysql"
7. Change the architecture to "standalone"
8. Set the MariaBD root password to "root"
10. Set the MariaDB custom database field to "hapi_dstu3"
11. Disable persistence
12. Switch to the YAML tab
13. Find the property `serviceAccount:create` and set it to `false`
14. Find the property "configuration", add this line to the `[mysqld]` section: `lower_case_table_names=1`
15. Hit the "Deploy" button

You can watch the progress in the Kubeapps UI or with

```bash
watch kubectl get all -n tanzu-hospital
```

## Install phpMyAdmin

The phpMyAdmin tool is a user interface for interacting with MySQL databases. Install phpMyAdmin With the Kubeapps UI:

1. Login to Kubeapps if you are not already logged in
2. Set the Current context in Kubeapps to the `tanzu-hospital` namespace (you may need to refresh for the new namespace to appear)
3. Go to the "Catalog" tab and search for "phpMyAdmin"
4. Choose "phpMyAdmin"
5. Choose "Deploy" for the latest version
6. Change the name to "tanzu-hospital-phpmyadmin"
7. Switch to the YAML tab
8. Find the property `service.type` and set it to `LoadBalancer`
9. Hit the "Deploy" button

You can watch the progress in the Kubeapps UI or with

Once phpMyAdmin is installed, you can access it at the LoadBalancer IP address (http://192.169.139.7). Log in
to the MySQL instance with Server=tanzu-hospital-mysql-mariadb, root/root


## Install HAPI FIHR Server

```shell
kubectl apply -f 31-HapiFhirDeployment.yml
```

```shell
kubectl apply -f 32-HapiFhirService.yml
```

Find the external IP address of the service:

```shell
kubectl get service hapi-fhir -n tanzu-hospital
```

Add a DNS entry for "hapi-fhir.tanzuathome.net" pointing to the external IP address.





## Install Cloud Native Runtimes
In this section, we will install Tanzu Cloud Native Runtimes. First, install the pre-requisite tools.

### Install Carvel Tools

```bash
brew tap vmware-tanzu/carvel

brew install kapp ytt kbld
```

### Install Knative CLI

1. Download the Knative client for your machine from here: https://github.com/knative/client/releases
1. Rename the executable to `kn` and place it in your path (`/usr/local/bin` on MacOS/Linux)
1. Make the file executable if on MacOS/Linux (`chmod +x /usr/local/bin/kn`)
1. If you are on MacOS, allow the file to run with Gatekeeper (`sudo xattr -d com.apple.quarantine /usr/local/bin/kn`)

### Install Cloud Native Runtimes 1.0.0

**Important Note:** These instructions are tested with Cloud Native Runtimes version 1.0.0+build.44.

Install instructions https://docs.vmware.com/en/Cloud-Native-Runtimes-for-VMware-Tanzu/1.0/tanzu-cloud-native-runtimes-1-0/GUID-cnr-overview.html

1. Install the Kapp Controller in your Kubernetes Cluster (about 1 minute)

   ```shell
   kubectl apply -f 91-KappControllerRoleBinding.yml

   kapp deploy -a kc -f https://github.com/vmware-tanzu/carvel-kapp-controller/releases/latest/download/release.yml
   ```

1. Download and untar the latest cloud native runtimes binary from Tanzu Network (http://network.pivotal.io)
1. From the untarred directory, execute the following command (about 3 minutes):

   ```bash
   cnr_provider=tkgs ./bin/install.sh
   ```

1. Accept the installation defaults, then wait for the installation to finish (takes a few minutes)

1. Apply pod security policy for the new namespace:

   ```shell
   kubectl apply -f 92-KnativePSP.yml
   ```

### Setup DNS for Knative

Tanzu Cloud Native Runtimes uses Knative serving, Contour, and Envoy. This allows applications deployed
with Knative to use a standard ingress controller. In this section, we'll setup Knative and DNS so that
applications deployed with Cloud Native Runtimes will be easily exposed.

First, setup a custom domain for Knative serving by modifying and executing `93-KnativeCustomDomain.yml`. You should
replace `tanzuhospital.com` with a DNS name you can control. You will need to add a DNS "A" record for this domain.

```bash
kubectl apply -f 93-KnativeCustomDomain.yml
```

Now find the external IP address of the ingress controller with this command:

```bash
kubectl get service envoy -n contour-external
```

Add a wildcard DNS record to your DNS using the IP address and domain you configured (for example, in my setup the IP address is 192.168.139.9
and the DNS entry is "*.tanzuhospital.com")


### Knative Verification Test (Optional)
If you want to try a test application to check basic functionality of the cloud native runtimes, run the following:

```shell
kn service create helloworld-go -n tanzu-hospital \
--image gcr.io/knative-samples/helloworld-go --env TARGET='from Cloud Native Runtimes' \
--user 1001
```

After the app is deployed, it should be available at "http://helloworld-go.tanzu-hospital.tanzuhospital.com"

You can delete the test application with the following:

```shell
kn service delete helloworld-go -n tanzu-hospital
```

## Build and Deploy All Services

### Allergy Service

```shell
cd ./allergy-service

./mvnw clean spring-boot:build-image

docker push jeffgbutler/allergy-service
```

Update the digest in `./kubernetes/cnr.yml`

```shell
kubectl apply -f ./kubernetes/cnr.yml
```

Once the service comes up, test with

```shell
curl http://allergy-service.frontdoor.tanzuhospital.com/api/patientAllergies/7860 | jq
```

### Encounter Service

```shell
cd ./encounter-service

./mvnw clean spring-boot:build-image

docker push jeffgbutler/encounter-service
```

Update the digest in `./kubernetes/cnr.yml`

```shell
kubectl apply -f ./kubernetes/cnr.yml
```

Once the service comes up, test with

```shell
curl http://encounter-service.frontdoor.tanzuhospital.com/api/patientEncounters/7860 | jq
```

### Patient Service

```shell
cd ./patient-service

./mvnw clean spring-boot:build-image

docker push jeffgbutler/patient-service
```

Update the digest in `./kubernetes/cnr.yml`

```shell
kubectl apply -f ./kubernetes/cnr.yml
```

Once the service comes up, test with

```shell
curl http://patient-service.frontdoor.tanzuhospital.com/api/patients/7860 | jq
```

### Hospital API

```shell
cd ./hospital-api

./mvnw clean spring-boot:build-image

docker push jeffgbutler/hospital-api
```

Update the digest in `./kubernetes/cnr.yml`

```shell
kubectl apply -f ./kubernetes/cnr.yml
```

Once the service comes up, test with

```shell
curl http://hospital-api.frontdoor.tanzuhospital.com/api/patients/7860 | jq
```

## Uninstall Cloud Native Runtimes

Delete all applications with `kn service delete ...`

```shell
kn service delete hospital-api -n frontdoor
kn service delete patient-service -n frontdoor
kn service delete encounter-service -n frontdoor
kn service delete allergy-service -n frontdoor
```

```shell
kapp delete -a cloud-native-runtimes -n cloud-native-runtimes
```

```shell
kubectl delete ns cloud-native-runtimes
```
