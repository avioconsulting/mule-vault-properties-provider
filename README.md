# Vault Properties Provider

### Global Config
Add a single Vault Properties Provider Config global element to your application. Specify the Vault URL and properties to log in.

##### Token Connections
Attributes:

`token-connection` element attributes:
*   `vaultUrl` - URL for Vault Server
*   `vaultToken` - Token to use for authentication
*   `engineVersion` - Version of the KV engine being used (v1 or v2)

```xml
<vault-properties-provider:config name="config" >
  <vault-properties-provider:token-connection vaultUrl="http://localhost:8200" engineVersion="v2" vaultToken="s.uo18rIGCFexkcxOOJET97EPA" />
</vault-properties-provider:config>
```

##### SSL Configuration with Token Authentication (PEM)
Attributes:

`token-connection` element attributes:

*   `vaultUrl` - URL for Vault Server
*   `vaultToken` - Token to use for authentication
*   `engineVersion` - Version of the KV engine being used (v1 or v2)

`ssl-properties` element attributes:

*   pemFile - path to PEM file for vault server SSL

```xml
<vault-properties-provider:config name="config" >
  <vault-properties-provider:token-connection vaultUrl="http://localhost:8200" engineVersion="v2" vaultToken="s.uo18rIGCFexkcxOOJET97EPA" >
    <vault-properties-provider:ssl-properties pemFile="ssl/my.pem" />
  </vault-properties-provider:token-connection>
</vault-properties-provider:config>
```

##### SSL Configuration with Token Authentication (KeyStore)
Attributes:

`token-connection` element attributes:

*   `vaultUrl` - URL for Vault Server
*   `vaultToken` - Token to use for authentication
*   `engineVersion` - Version of the KV engine being used (v1 or v2)

`ssl-properties` element attributes:

*   `trustStoreFile` - path to Java trust store (JKS)

```xml
<vault-properties-provider:config name="config" >
  <vault-properties-provider:token-connection vaultUrl="http://localhost:8200" engineVersion="v2" vaultToken="s.uo18rIGCFexkcxOOJET97EPA" >
    <vault-properties-provider:ssl-properties trustStoreFile="/tmp/trustStore.jks" />
  </vault-properties-provider:token-connection>
</vault-properties-provider:config>
```

##### SSL Configuration with TLS Authentication (JKS)
Attributes:

`tls-connection` element attributes:
*   `vaultUrl` - URL for Vault Server
*   `engineVersion` - Version of the KV engine being used (v1 or v2)

`ssl-properties` element attributes:
*   `trustStorePath` - path to Java trust store (JKS)

`jks-properties` element attributes:
*   `keyStoreFile` - path to Java key store (JKS)
*   `keyStorePassword` - password for the key store


```xml
<vault-properties-provider:config name="config" >
  <vault-properties-provider:tls-connection vaultUrl="http://localhost:8200" engineVersion="v2" >
    <vault-properties-provider:ssl-properties trustStoreFile="/tmp/trustStore.jks" />
    <vault-properties-provider:jks-properties keyStoreFile="/tmp/keystore.jks" keyStorePassword="***" />
  </vault-properties-provider:tls-connection>
</vault-properties-provider:config>
```

##### SSL Configuration with TLS Authentication (PEM)
Attributes:

`tls-connection` element attributes:
*   `vaultUrl` - URL for Vault Server
*   `engineVersion` - Version of the KV engine being used (v1 or v2)

`ssl-properties` element attributes:
*   `pemFile` - path to PEM file for vault server SSL

`pem-properties` element attributes:
*   `clientPemFile` - An X.509 client certificate, for use with Vault's TLS Certificate auth backend
*   `clientKeyPemFile` - An RSA private key, for use with Vault's TLS Certificate auth backend

```xml
<vault-properties-provider:config name="config" >
  <vault-properties-provider:tls-connection vaultUrl="http://localhost:8200" engineVersion="v2" >
    <vault-properties-provider:ssl-properties pemFile="ssl/my.pem" />
    <vault-properties-provider:pem-properties clientPemFile="ssl/my_vault.pem" clientKeyPemFile="ssl/my_vault_key.pem" />
  </vault-properties-provider:tls-connection>
</vault-properties-provider:config>
```

##### IAM Configuration
Attributes:

`iam-connection` element attributes:
*   `vaultUrl` - URL for Vault Server
*   `engineVersion` - Version of the KV engine being used (v1 or v2)
*   `awsAuthMount` - the Vault mount for AWS authentication
*   `vaultRole` - the Vault role to login as
*   `iamRequestUrl` - Most likely https://sts.amazonaws.com/
*   `iamRequestBody` - The IAM request body, most likely: Action=GetCallerIdentity&Version=2011-06-15
*   `iamRequestHeaders` - IAM request headers

```xml
<vault-properties-provider:config name="config" >
  <vault-properties-provider:iam-connection vaultUrl="http://localhost:8200" 
                                            engineVersion="v2" 
                                            awsAuthMount="aws"
                                            vaultRole="test-role" 
                                            iamRequestUrl="https://sts.amazonaws.com/" 
                                            iamRequestBody="Action=GetCallerIdentity&Version=2011-06-15" 
                                            iamRequestHeaders="" />
</vault-properties-provider:config>
```

##### EC2 Configuration with Instance Metadata Authentication
Attributes:

`ec2-connection` element attributes:
*   `vaultUrl` - URL for Vault Server
*   `engineVersion` - Version of the KV engine being used (v1 or v2)
*   `awsAuthMount` - the Vault mount for AWS authentication
*   `vaultRole` - the Vault role to login as
*   `useInstanceMetadata` - true to login with instance metadata (PKCS7 is looked up on the host) 

```xml
<vault-properties-provider:config name="config" >
  <vault-properties-provider:ec2-connection vaultUrl="http://localhost:8200" engineVersion="v2" awsAuthMount="aws" vaultRole="test-role" useInstanceMetadata="true" />
</vault-properties-provider:config>
```

##### EC2 Configuration with PKCS7 Authentication
Attributes:

`ec2-connection` element attributes:
*   `vaultUrl` - URL for Vault Server
*   `engineVersion` - Version of the KV engine being used (v1 or v2)
*   `awsAuthMount` - the Vault mount for AWS authentication
*   `vaultRole` - the Vault role to login as
*   `pkcs7` - PKCS7 value with all \n characters removed

```xml
<vault-properties-provider:config name="config" >
  <vault-properties-provider:ec2-connection vaultUrl="http://localhost:8200" 
                                            engineVersion="v2" 
                                            awsAuthMount="aws" 
                                            vaultRole="test-role" 
                                            pkcs7="MIICiTCCAfICCQD6m7oRw0uXOjANBgkqhkiG9w0BAQUFADCBiDELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAldBMRAwDgYDVQQHEwdTZWF0dGxlMQ8wDQYDVQQKEwZBbWF6b24xFDASBgNVBAsTC0lBTSBDb25zb2xlMRIwEAYDVQQDEwlUZXN0Q2lsYWMxHzAdBgkqhkiG9w0BCQEWEG5vb25lQGFtYXpvbi5jb20wHhcNMTEwNDI1MjA0NTIxWhcNMTIwNDI0MjA0NTIxWjCBiDELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAldBMRAwDgYDVQQHEwdTZWF0dGxlMQ8wDQYDVQQKEwZBbWF6b24xFDASBgNVBAsTC0lBTSBDb25zb2xlMRIwEAYDVQQDEwlUZXN0Q2lsYWMxHzAdBgkqhkiG9w0BCQEWEG5vb25lQGFtYXpvbi5jb20wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAMaK0dn+a4GmWIWJ21uUSfwfEvySWtC2XADZ4nB+BLYgVIk60CpiwsZ3G93vUEIO3IyNoH/f0wYK8m9TrDHudUZg3qX4waLG5M43q7Wgc/MbQITxOUSQv7c7ugFFDzQGBzZswY6786m86gpEIbb3OhjZnzcvQAaRHhdlQWIMm2nrAgMBAAEwDQYJKoZIhvcNAQEFBQADgYEAtCu4nUhVVxYUntneD9+h8Mg9q6q+auNKyExzyLwaxlAoo7TJHidbtS4J5iNmZgXL0FkbFFBjvSfpJIlJ00zbhNYS5f6GuoEDmFJl0ZxBHjJnyp378OD8uTs7fLvjx79LjSTbNYiytVbZPQUQ5Yaxu2jXnimvw3rrszlaEXAMPLE"/>
</vault-properties-provider:config>
```

##### EC2 Configuration with Identity Document Authentication
Attributes:

`ec2-connection` element attributes:
*   `vaultUrl` - URL for Vault Server
*   `engineVersion` - Version of the KV engine being used (v1 or v2)
*   `awsAuthMount` - the Vault mount for AWS authentication
*   `vaultRole` - the Vault role to login as

`identity-properties` element attributes:
*   `identity` - Base64-encoded EC2 instance identity document
*   `signature` - Base64-encoded SHA256 RSA signature of the instance identity document

```xml
<vault-properties-provider:config name="config" >
  <vault-properties-provider:ec2-connection vaultUrl="http://localhost:8200" engineVersion="v2" awsAuthMount="aws" vaultRole="test-role">
    <vault-properties-provider:identity-properties identity="eyAiZGV2cGF5UHJvZHVjdENvZGVzIiA6IG51bGwsICJtYXJrZXRwbGFjZVByb2R1Y3RDb2RlcyIgOiBbICIxYWJjMmRlZmdoaWprbG0zbm9wcXJzNHR1IiBdLCAiYXZhaWxhYmlsaXR5Wm9uZSIgOiAidXMtd2VzdC0yYiIsICJwcml2YXRlSXAiIDogIjEwLjE1OC4xMTIuODQiLCAidmVyc2lvbiIgOiAiMjAxNy0wOS0zMCIsICJpbnN0YW5jZUlkIiA6ICJpLTEyMzQ1Njc4OTBhYmNkZWYwIiwgImJpbGxpbmdQcm9kdWN0cyIgOiBudWxsLCAiaW5zdGFuY2VUeXBlIiA6ICJ0Mi5taWNybyIsICJhY2NvdW50SWQiIDogIjEyMzQ1Njc4OTAxMiIsICJpbWFnZUlkIiA6ICJhbWktNWZiOGM4MzUiLCAicGVuZGluZ1RpbWUiIDogIjIwMTYtMTEtMTlUMTY6MzI6MTFaIiwgImFyY2hpdGVjdHVyZSIgOiAieDg2XzY0IiwgImtlcm5lbElkIiA6IG51bGwsICJyYW1kaXNrSWQiIDogbnVsbCwgInJlZ2lvbiIgOiAidXMtd2VzdC0yIn0=" 
                                                   signature="dExamplesjNQhhJan7pORLpLSr7lJEF4V2DhKGlyoYVBoUYrY9njyBCmhEayaGrhtS/AWY+LPxlVSQURF5n0gwPNCuO6ICT0fNrm5IH7w9ydyaexamplejJw8XvWPxbuRkcN0TAA1p4RtCAqm4ms=x2oALjWSCBExample=" />
  </vault-properties-provider:ec2-connection>
</vault-properties-provider:config>
```

### Referencing Values
To reference a value, use this format:

```
${vault::<secret_engine>/<path_to_secret>.<key_name>}
```

By default, version 2 of the KV engine is used.

If an environment variable needs to be referenced within a path,  use this format within the path to the secret or the key name:
```
$[ENVIRONMENT_VARIABLE_NAME]
```
One may need to reference an environment variable in order to account reference environment-specific secrets. For instance, there is one API Key for the development environment and another for the production environment.

## Example
To retrieve a value from the Key/Value secrets engine exposed as "secret", stored at "test/mule-sample", with a key of "name", use this format:

```
${vault::secret/test/mule-sample.name}
```

It could also be specified with an environment variable like this (where an environment variable called `ENV` is set to `test`):

```
${vault::secret/$[ENV]/mule-sample.name}
```

### Publishing to a Private Exchange

To publish to a private exchange, some updates are necessary in the `pom.xml` file and your Maven `settings.xml`.

Update the `groupId` to the organization ID used by your organization on the Anypoint platform.

In addition, update the `url` in the `distributionManagement` section of the pom to the following, replacing `${orgId}` with your Organization ID:
```
https://maven.anypoint.mulesoft.com/api/v1/organizations/${orgID}/maven
```

Add a `server` for the exchange repository in your Maven `settings.xml` file with the username and password to use for AnyPoint Exchange. 

After it is published in the exchange, the dependency in a project would change to look like this:

```xml
<dependency>
    <groupId>${orgId}</groupId>
    <artifactId>vault-connector</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <classifier>mule-plugin</classifier>
</dependency>
```