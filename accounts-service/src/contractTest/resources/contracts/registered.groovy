package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("Should register account")
    request {
        method POST()
        url("/accounts/register") {}
        headers {
            contentType(applicationJson())
        }
        body(
                email: "test@test.ru",
                password: "Password1111",
                name: "Test",
                surname: "Test",
                birthdate: "1990-01-01",
                phone: "89996665522"
        )
    }
    response {
        status OK()
    }
}
