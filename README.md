# Vault Properties Provider Extension

The Vault Properties Provider Extension allows one to use values from Vault in-line.


Add this dependency to your application pom.xml

```xml
<dependency>
  <groupId>com.avioconsulting.mule.vault.provider</groupId>
  <artifactId>mule-vault-properties-providers-module</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <classifier>mule-plugin</classifier>
</dependency>
```

### Global Config
Add a Vault Properties Provider Config global element to your application. Specify the Vault URL and properties to log in.

##### Basic Configuration
Parameters:
* vaultToken - Token to use for authentication
* kvVersion - Version of the KV engine being used

```xml
<vault-properties-provider:config name="config" vaultUrl="http://localhost:8200">
  <vault-properties-provider:basic vaultToken="s.uo18rIGCFexkcxOOJET97EPA" kvVersion="1" />
</vault-properties-provider:config>
```

##### SSL Configuration with Token Authentication (PEM)
Parameters:
basic element attributes:
* vaultToken - Token to use for authentication
* kvVersion - Version of the KV engine being used

ssl element attributes:
* pemFile - path to PEM file for vault server SSL
* useTlsAuth - false to use Token Authentication
* verifySSL - true to validate certificates

```xml
<vault-properties-provider:config name="config" vaultUrl="http://localhost:8200">
  <vault-properties-provider:basic vaultToken="s.uo18rIGCFexkcxOOJET97EPA" kvVersion="2"/>
  <vault-properties-provider:ssl pemFile="ssl/my.pem" useTlsAuth="false" verifySSL="true" />
</vault-properties-provider:config>
```

##### SSL Configuration with Token Authentication (KeyStore)
Parameters:
basic element attributes:
* vaultToken - Token to use for authentication

ssl element attributes:
* useTlsAuth - false to use Token Authentication
* verifySSL - true to validate certificats
* keyStorePath - path to Java key store (JKS)
* keyStorePassword - password for the key store
* trustStorePath - path to Java trust store (JKS)

```xml
<vault-properties-provider:config name="config" vaultUrl="http://localhost:8200">
  <vault-properties-provider:basic vaultToken="s.uo18rIGCFexkcxOOJET97EPA" kvVersion="2"/>
  <vault-properties-provider:ssl useTlsAuth="false" verifySSL="true" keyStorePath="/tmp/keystore.jks" keyStorePassword="***" trustStorePath="/tmp/trustStore.jks" />
</vault-properties-provider:config>
```

##### SSL Configuration with TLS Authentication (JKS)
Parameter:
* useTlsAuth - true to authenticate via TLS certificate (must be in key store)
* verifySSL - true to validate certificats
* keyStorePath - path to Java key store (JKS)
* keyStorePassword - password for the key store
* trustStorePath - path to Java trust store (JKS)

```xml
<vault-properties-provider:config name="config" vaultUrl="http://localhost:8200">
  <vault-properties-provider:basic kvVersion="2" />
  <vault-properties-provider:ssl useTlsAuth="true" verifySSL="true" keyStorePath="/tmp/keystore.jks" keyStorePassword="***" trustStorePath="/tmp/trustStore.jks" />
</vault-properties-provider:config>
```

##### SSL Configuration with TLS Authentication (PEM)
Parameter:
* useTlsAuth - true to authenticate via TLS certificate (must be in key store)
* verifySSL - true to validate certificats
* pemFile - path to PEM file for vault server SSL
* clientPemFile - An X.509 client certificate, for use with Vault's TLS Certificate auth backend
* clientKeyPemFile - An RSA private key, for use with Vault's TLS Certificate auth backend

```xml
<vault-properties-provider:config name="config" vaultUrl="http://localhost:8200">
  <vault-properties-provider:basic kvVersion="2" />
  <vault-properties-provider:ssl useTlsAuth="true" verifySSL="true" pemFile="ssl/vault.pem" clientPemFile="ssl/my_vault.pem" clientKeyPemFile="ssl/my_vault_key.pem"/>
</vault-properties-provider:config>
```

##### IAM Configuration
Parameters:
* iamAwsAuthMount - the Vault mount for AWS authentication
* iamVaultRole - the Vault role to login as
* iamUrl - Most likely https://sts.amazonaws.com/
* iamReqBody - The IAM request body, most likely: Action=GetCallerIdentity&Version=2011-06-15
* iamReqHeaders - IAM request headers

```xml
<vault-properties-provider:config name="config" vaultUrl="http://localhost:8200">
  <vault-properties-provider:iam iamAwsAuthMount="aws" iamVaultRole="test-role" iamUrl="https://sts.amazonaws.com/" iamReqBody="Action=GetCallerIdentity&Version=2011-06-15" iamReqHeaders="" />
</vault-properties-provider:config>
```

##### EC2 Configuration with Instance Metadata Authentication
Parameters:
* ec2AwsAuthMount - the Vault mount for AWS authentication
* ec2VaultRole - the Vault role to login as
* useInstanceMetadata - true to login with instance metadata (PKCS7 is looked up on the host) 

```xml
<vault-properties-provider:config name="config" vaultUrl="http://localhost:8200">
  <vault-properties-provider:ec2 ec2AwsAuthMount="aws" ec2VaultRole="test-role" useInstanceMetadata="true" />
</vault-properties-provider:config>
```

##### EC2 Configuration with PKCS7 Authentication
Parameters:
* ec2AwsAuthMount - the Vault mount for AWS authentication
* ec2VaultRole - the Vault role to login as
* pkcs7 - PKCS7 value with all \n characters removed
* useInstanceMetadata - false to login with pkcs7 property

```xml
<vault-properties-provider:config name="config" vaultUrl="http://localhost:8200">
  <vault-properties-provider:ec2 ec2AwsAuthMount="aws" ec2VaultRole="test-role" useInstanceMetadata="false" pkcs7="MIICiTCCAfICCQD6m7oRw0uXOjANBgkqhkiG9w0BAQUFADCBiDELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAldBMRAwDgYDVQQHEwdTZWF0dGxlMQ8wDQYDVQQKEwZBbWF6b24xFDASBgNVBAsTC0lBTSBDb25zb2xlMRIwEAYDVQQDEwlUZXN0Q2lsYWMxHzAdBgkqhkiG9w0BCQEWEG5vb25lQGFtYXpvbi5jb20wHhcNMTEwNDI1MjA0NTIxWhcNMTIwNDI0MjA0NTIxWjCBiDELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAldBMRAwDgYDVQQHEwdTZWF0dGxlMQ8wDQYDVQQKEwZBbWF6b24xFDASBgNVBAsTC0lBTSBDb25zb2xlMRIwEAYDVQQDEwlUZXN0Q2lsYWMxHzAdBgkqhkiG9w0BCQEWEG5vb25lQGFtYXpvbi5jb20wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAMaK0dn+a4GmWIWJ21uUSfwfEvySWtC2XADZ4nB+BLYgVIk60CpiwsZ3G93vUEIO3IyNoH/f0wYK8m9TrDHudUZg3qX4waLG5M43q7Wgc/MbQITxOUSQv7c7ugFFDzQGBzZswY6786m86gpEIbb3OhjZnzcvQAaRHhdlQWIMm2nrAgMBAAEwDQYJKoZIhvcNAQEFBQADgYEAtCu4nUhVVxYUntneD9+h8Mg9q6q+auNKyExzyLwaxlAoo7TJHidbtS4J5iNmZgXL0FkbFFBjvSfpJIlJ00zbhNYS5f6GuoEDmFJl0ZxBHjJnyp378OD8uTs7fLvjx79LjSTbNYiytVbZPQUQ5Yaxu2jXnimvw3rrszlaEXAMPLE"/>
</vault-properties-provider:config>
```

##### EC2 Configuration with Identity Document Authentication
Parameters:
* ec2AwsAuthMount - the Vault mount for AWS authentication
* ec2VaultRole - the Vault role to login as
* identityDoc - Base64-encoded EC2 instance identity document
* identityDocSignature - Base64-encoded SHA256 RSA signature of the instance identity document
* useInstanceMetadata - false to login with Identity Document Authentication

```xml
<vault-properties-provider:config name="config" vaultUrl="http://localhost:8200">
  <vault-properties-provider:ec2 ec2AwsAuthMount="aws" ec2VaultRole="test-role" useInstanceMetadata="false" identityDoc="eyAiZGV2cGF5UHJvZHVjdENvZGVzIiA6IG51bGwsICJtYXJrZXRwbGFjZVByb2R1Y3RDb2RlcyIgOiBbICIxYWJjMmRlZmdoaWprbG0zbm9wcXJzNHR1IiBdLCAiYXZhaWxhYmlsaXR5Wm9uZSIgOiAidXMtd2VzdC0yYiIsICJwcml2YXRlSXAiIDogIjEwLjE1OC4xMTIuODQiLCAidmVyc2lvbiIgOiAiMjAxNy0wOS0zMCIsICJpbnN0YW5jZUlkIiA6ICJpLTEyMzQ1Njc4OTBhYmNkZWYwIiwgImJpbGxpbmdQcm9kdWN0cyIgOiBudWxsLCAiaW5zdGFuY2VUeXBlIiA6ICJ0Mi5taWNybyIsICJhY2NvdW50SWQiIDogIjEyMzQ1Njc4OTAxMiIsICJpbWFnZUlkIiA6ICJhbWktNWZiOGM4MzUiLCAicGVuZGluZ1RpbWUiIDogIjIwMTYtMTEtMTlUMTY6MzI6MTFaIiwgImFyY2hpdGVjdHVyZSIgOiAieDg2XzY0IiwgImtlcm5lbElkIiA6IG51bGwsICJyYW1kaXNrSWQiIDogbnVsbCwgInJlZ2lvbiIgOiAidXMtd2VzdC0yIn0=" identityDocSignature="dExamplesjNQhhJan7pORLpLSr7lJEF4V2DhKGlyoYVBoUYrY9njyBCmhEayaGrhtS/AWY+LPxlVSQURF5n0gwPNCuO6ICT0fNrm5IH7w9ydyaexamplejJw8XvWPxbuRkcN0TAA1p4RtCAqm4ms=x2oALjWSCBExample="/>
</vault-properties-provider:config>
```

### Referencing Values
To reference a value, use this format:

```
${vault::<secret_engine>/<path_to_secret>.<key_name>
```

By default, version 2 of the KV engine is used.

## Example
To retrieve a value from the Key/Value secrets engine exposed as "secret", stored at "test/mule-sample", with a key of "name", use this format:

```
${vault::secret/test/mule-sample.name}
```
