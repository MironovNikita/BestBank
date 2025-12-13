package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should delete account")
    request {
        method POST()
        url("/accounts/delete") {}
        headers {
            contentType(applicationJson())
        }
        body(
                id: "2",
                currency: "RUB",
                email: "test@test.ru"
        )
    }
    response {
        status OK()
    }
}
