storage "file" {
  path = "/vault/file"
}

listener "tcp" {
  address = "0.0.0.0:8200",
  tls_cert_file = "/vault/config/ssl/vault-cert.pem"
  tls_key_file = "/vault/config/ssl/vault-privkey.pem"
}

listener "tcp" {
  address = "0.0.0.0:8280"
  tls_disable = true
}

disable_mlock = true
default_lease_ttl = "24h"
max_lease_ttl = "87600h"
disable_clustering = true