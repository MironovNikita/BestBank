package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Get balance by account ID")
    request {
        method GET()
        urlPath("/accounts/3/balance")
    }
    response {
        status OK()
        headers { contentType(applicationJson()) }
        body(
                id: 3,
                balance: 1000
        )
    }
}
