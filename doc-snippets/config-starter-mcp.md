| Property                        | Description                                      | Default                 |
|---------------------------------|--------------------------------------------------|-------------------------|
| `auth.disable`                  | Disable authentication (for development/testing) | `false`                 |
| `auth.issuers`                  | Comma-separated list of trusted OIDC issuers     |                         |
| `opa.disable`                   | Disable Open Policy Agent authorization          | `false`                 |
| `opa.base.url`                  | Base URL of the OPA server                       | `http://localhost:8181` |
| `opa.policy.package`            | OPA policy package name                          |                         |
| `opa.client.timeout`            | Read timeout for OPA client requests             | `500ms`                 |
| `opa.client.connection.timeout` | Connection timeout for OPA client requests       | `500ms`                 |
