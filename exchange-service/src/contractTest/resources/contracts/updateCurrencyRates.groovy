package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should update currencies rates")
    request {
        method POST()
        url("/exchange/update") {}
        headers {
            contentType(applicationJson())
        }
        body([
                "RUB": 1.00,
                "EUR": 2.40,
                "USD": 1.70
        ])
    }
    response {
        status OK()
    }
}
