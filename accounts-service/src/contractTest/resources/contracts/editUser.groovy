package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should edit user data")
    request {
        method POST()
        url("/users/3/editAccount") {}
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