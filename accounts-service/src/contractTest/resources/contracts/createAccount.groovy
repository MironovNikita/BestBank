package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should create account")
    request {
        method POST()
        url("/accounts/create/3") {}
        headers {
            contentType(applicationJson())
        }
        body(
                title: "Test",
                currency: "USD",
                email: "test@test.ru"
        )
    }
    response {
        status OK()
    }
}
