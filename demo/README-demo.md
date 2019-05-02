# Executing the Vault Connector Demo

## Step 0: Install prerequisites

You will need Vault locally and on your PATH

## Step 1: Start Vault

Execute setupDemo.sh to kill any previously running Vault server instances and start a new instance. 

It will add a new secret at secrets/stamples/sample1, enable the transit secret engine, and create a key called demo-key

## Step 2: Execute the Demo application

Open the demo application (vault-properties-demo) with Anypoint Studio and start the application as a Mule Application

### Executing vault-properties-demoFlow

Execute the following to test the retrieval of properties from Vault

curl -X POST http://localhost:8081/getProperties

## Step 3: Stopping Vault

Execute killVault.sh to kill all running Vault servers