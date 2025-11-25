package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should operate transfer")
    request {
        method POST()
        url("/transfer") {}
        headers {
            contentType(applicationJson())
        }
        body(
                accountIdFrom: 3,
                accountIdTo: 2,
                email: "test@test.ru",
                amount: 1000
        )
    }
    response {
        status OK()
    }
}