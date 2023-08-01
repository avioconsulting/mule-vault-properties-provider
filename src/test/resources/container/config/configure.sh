#!/bin/sh

export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN='test_token'

vault kv put -mount=secret sample-secret att='Vault is working'