package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should register account")
    request {
        method POST()
        url("/accounts/3/editAccount") {}
        headers {
            contentType(applicationJson())
        }
        body(
                email: "test@test.ru"
        )
    }
    response {
        status OK()
    }
}