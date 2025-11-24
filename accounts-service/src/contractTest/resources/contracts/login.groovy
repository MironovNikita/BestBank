package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should login account")
    request {
        method POST()
        url("/accounts/login") {}
        headers {
            contentType(applicationJson())
        }
        body(
                email: "test@test.ru",
                password: "Password1111",
        )
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
                id: 3,
                email: "test@test.ru",
                name: "Test"
        )
    }
}