# Vault Properties Provider Extension

The Vault Properties Provider Extension allows one to use values from Vault in-line.


Add this dependency to your application pom.xml

```
<dependency>
  <groupId>com.avioconsulting.mule.vault.provider</groupId>
  <artifactId>mule-vault-properties-providers-module</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <classifier>mule-plugin</classifier>
</dependency>
```

Add a Vault Properties Provider Config global element to your application. Specify the Vault URL and properties to log in.

To reference a value, use this format:

```
${vault-properties-provider::<secret_engine>/<path_to_secret>.<key_name>
```

Only the Key/Value secret engine, version 2 is supported at this time.

## Example
To retrieve a value from the Key/Value secrets engine exposed as "secret", stored at "test/mule-sample", with a key of "name", use this format:
```
${vault-properties-provider::secret/test/mule-sample.name}
```