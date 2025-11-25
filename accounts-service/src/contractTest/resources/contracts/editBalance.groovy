package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should edit balance")
    request {
        method POST()
        url("/accounts/3/balance") {}
        headers {
            contentType(applicationJson())
        }
        body(
                balance: 1000
        )
    }
    response {
        status OK()
    }
}
